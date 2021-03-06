package com.cloud.network.resource;

import com.cloud.common.agent.IAgentControl;
import com.cloud.common.resource.ServerResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.PingCommand;
import com.cloud.legacymodel.communication.command.startup.StartupCommand;
import com.cloud.legacymodel.communication.command.startup.StartupNiciraNvpCommand;
import com.cloud.model.enumeration.HostType;
import com.cloud.network.nicira.ControlClusterStatus;
import com.cloud.network.nicira.ControlClusterStatus.ClusterRoleConfig;
import com.cloud.network.nicira.DestinationNatRule;
import com.cloud.network.nicira.Match;
import com.cloud.network.nicira.NatRule;
import com.cloud.network.nicira.NiciraNvpApi;
import com.cloud.network.nicira.NiciraNvpApiException;
import com.cloud.network.nicira.SourceNatRule;
import com.cloud.network.utils.CommandRetryUtility;
import com.cloud.utils.nicira.nvp.plugin.NiciraNvpApiVersion;
import com.cloud.utils.rest.CosmicRESTException;
import com.cloud.utils.rest.HttpClientHelper;

import javax.naming.ConfigurationException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NiciraNvpResource implements ServerResource {

    public static final int NAME_MAX_LEN = 40;
    public static final int NUM_RETRIES = 2;
    private static final Logger s_logger = LoggerFactory.getLogger(NiciraNvpResource.class);
    private static final int MAX_REDIRECTS = 5;

    private String name;
    private String guid;
    private String zoneId;

    private final List<NiciraNvpApi> niciraNvpApis = new ArrayList<>();
    private int activeNiciraNvpApi = 0;
    private NiciraNvpUtilities niciraNvpUtilities;
    private CommandRetryUtility retryUtility;

    public NiciraNvpApi getNiciraNvpApi() {
        return this.niciraNvpApis.get(this.activeNiciraNvpApi);
    }

    protected NiciraNvpApi createNiciraNvpApi(final String host, final String username, final String password) throws CosmicRESTException {
        try {
            return NiciraNvpApi.create().host(host).username(username).password(password).httpClient(HttpClientHelper.createHttpClient(MAX_REDIRECTS)).build();
        } catch (final KeyManagementException e) {
            throw new CosmicRESTException("Could not create HTTP client", e);
        } catch (final NoSuchAlgorithmException e) {
            throw new CosmicRESTException("Could not create HTTP client", e);
        } catch (final KeyStoreException e) {
            throw new CosmicRESTException("Could not create HTTP client", e);
        }
    }

    public NiciraNvpUtilities getNiciraNvpUtilities() {
        return this.niciraNvpUtilities;
    }

    @Override
    public boolean configure(final String ignoredName, final Map<String, Object> params) throws ConfigurationException {

        this.name = (String) params.get("name");
        if (this.name == null) {
            throw new ConfigurationException("Unable to find name");
        }

        this.guid = (String) params.get("guid");
        if (this.guid == null) {
            throw new ConfigurationException("Unable to find the guid");
        }

        this.zoneId = (String) params.get("zoneId");
        if (this.zoneId == null) {
            throw new ConfigurationException("Unable to find zone");
        }

        final String ips = (String) params.get("ip");
        if (ips == null) {
            throw new ConfigurationException("Unable to find IPs");
        }

        final String adminuser = (String) params.get("adminuser");
        if (adminuser == null) {
            throw new ConfigurationException("Unable to find admin username");
        }

        final String adminpass = (String) params.get("adminpass");
        if (adminpass == null) {
            throw new ConfigurationException("Unable to find admin password");
        }

        this.niciraNvpUtilities = NiciraNvpUtilities.getInstance();
        this.retryUtility = CommandRetryUtility.getInstance();
        this.retryUtility.setServerResource(this);

        try {
            for (final String ip : ips.split(",")) {
                this.niciraNvpApis.add(createNiciraNvpApi(ip, adminuser, adminpass));
            }
        } catch (final CosmicRESTException e) {
            throw new ConfigurationException("Could not create a Nicira Nvp API client: " + e.getMessage());
        }

        return true;
    }

    public CommandRetryUtility getRetryUtility() {
        return this.retryUtility;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(final String name) {
        // TODO Auto-generated method stub
    }

    @Override
    public HostType getType() {
        // Think up a better name for this Type?
        return HostType.L2Networking;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public StartupCommand[] initialize() {
        final StartupNiciraNvpCommand sc = new StartupNiciraNvpCommand();
        sc.setGuid(this.guid);
        sc.setName(this.name);
        sc.setDataCenter(this.zoneId);
        sc.setPod("");
        sc.setPrivateIpAddress("");
        sc.setStorageIpAddress("");
        sc.setVersion(NiciraNvpResource.class.getPackage().getImplementationVersion());
        return new StartupCommand[]{sc};
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public PingCommand getCurrentStatus(final long id) {
        try {
            final ControlClusterStatus ccs = getNiciraNvpApi().getControlClusterStatus();
            getApiProviderMajorityVersion(ccs);
            if (!"stable".equals(ccs.getClusterStatus())) {
                s_logger.error("ControlCluster state is not stable: " + ccs.getClusterStatus());
                rotateNiciraNvpApi();
                return null;
            }
        } catch (final NiciraNvpApiException e) {
            s_logger.error("getControlClusterStatus failed", e);
            rotateNiciraNvpApi();
            return null;
        }
        return new PingCommand(HostType.L2Networking, id);
    }

    private void rotateNiciraNvpApi() {
        s_logger.info("Rotating NSX API endpoint");

        getNiciraNvpApi().recreate();

        int active = ++this.activeNiciraNvpApi;

        if (active >= this.niciraNvpApis.size()) {
            active = 0;
        }

        this.activeNiciraNvpApi = active;
    }

    private void getApiProviderMajorityVersion(final ControlClusterStatus ccs) {
        final ClusterRoleConfig[] configuredRoles = ccs.getConfiguredRoles();
        if (configuredRoles != null) {
            final String apiProviderMajorityVersion = searchApiProvider(configuredRoles);
            NiciraNvpApiVersion.setNiciraApiVersion(apiProviderMajorityVersion);
            NiciraNvpApiVersion.logNiciraApiVersion();
        }
    }

    private String searchApiProvider(final ClusterRoleConfig[] configuredRoles) {
        for (int i = 0; i < configuredRoles.length; i++) {
            if (configuredRoles[i].getRole() != null && configuredRoles[i].getRole().equals("api_provider")) {
                return configuredRoles[i].getMajorityVersion();
            }
        }
        return null;
    }

    @Override
    public Answer executeRequest(final Command cmd) {
        final NiciraNvpRequestWrapper wrapper = NiciraNvpRequestWrapper.getInstance();
        try {
            return wrapper.execute(cmd, this);
        } catch (final Exception e) {
            s_logger.debug("Received unsupported command " + cmd.toString());
            s_logger.debug("Got error " + e.toString());
            return Answer.createUnsupportedCommandAnswer(cmd);
        }
    }

    @Override
    public void disconnected() {
    }

    @Override
    public IAgentControl getAgentControl() {
        return null;
    }

    @Override
    public void setAgentControl(final IAgentControl agentControl) {
    }

    public String natRuleToString(final NatRule rule) {

        final StringBuilder natRuleStr = new StringBuilder();
        natRuleStr.append("Rule ");
        natRuleStr.append(rule.getUuid());
        natRuleStr.append(" (");
        natRuleStr.append(rule.getType());
        natRuleStr.append(") :");
        final Match m = rule.getMatch();
        natRuleStr.append("match (");
        natRuleStr.append(m.getProtocol());
        natRuleStr.append(" ");
        natRuleStr.append(m.getSourceIpAddresses());
        natRuleStr.append(" [");
        natRuleStr.append(m.getSourcePort());
        natRuleStr.append(" ] -> ");
        natRuleStr.append(m.getDestinationIpAddresses());
        natRuleStr.append(" [");
        natRuleStr.append(m.getDestinationPort());
        natRuleStr.append(" ]) -->");
        if ("SourceNatRule".equals(rule.getType())) {
            natRuleStr.append(((SourceNatRule) rule).getToSourceIpAddressMin());
            natRuleStr.append("-");
            natRuleStr.append(((SourceNatRule) rule).getToSourceIpAddressMax());
            natRuleStr.append(" [");
            natRuleStr.append(((SourceNatRule) rule).getToSourcePort());
            natRuleStr.append(" ])");
        } else {
            natRuleStr.append(((DestinationNatRule) rule).getToDestinationIpAddress());
            natRuleStr.append(" [");
            natRuleStr.append(((DestinationNatRule) rule).getToDestinationPort());
            natRuleStr.append(" ])");
        }
        return natRuleStr.toString();
    }

    public String truncate(final String string, final int length) {
        if (string.length() <= length) {
            return string;
        } else {
            return string.substring(0, length);
        }
    }

    public NatRule[] generatePortForwardingRulePair(final String insideIp, final int[] insidePorts, final String outsideIp, final int[] outsidePorts,
                                                    final String protocol) {
        // Start with a basic static nat rule, then add port and protocol details
        final NatRule[] rulepair = generateStaticNatRulePair(insideIp, outsideIp);

        ((DestinationNatRule) rulepair[0]).setToDestinationPort(insidePorts[0]);
        rulepair[0].getMatch().setDestinationPort(outsidePorts[0]);
        rulepair[0].setOrder(50);
        rulepair[0].getMatch().setEthertype("IPv4");
        if ("tcp".equals(protocol)) {
            rulepair[0].getMatch().setProtocol(6);
        } else if ("udp".equals(protocol)) {
            rulepair[0].getMatch().setProtocol(17);
        }

        ((SourceNatRule) rulepair[1]).setToSourcePort(outsidePorts[0]);
        rulepair[1].getMatch().setSourcePort(insidePorts[0]);
        rulepair[1].setOrder(50);
        rulepair[1].getMatch().setEthertype("IPv4");
        if ("tcp".equals(protocol)) {
            rulepair[1].getMatch().setProtocol(6);
        } else if ("udp".equals(protocol)) {
            rulepair[1].getMatch().setProtocol(17);
        }

        return rulepair;
    }

    public NatRule[] generateStaticNatRulePair(final String insideIp, final String outsideIp) {
        final NatRule[] rulepair = new NatRule[2];
        rulepair[0] = new DestinationNatRule();
        rulepair[0].setType("DestinationNatRule");
        rulepair[0].setOrder(100);
        rulepair[1] = new SourceNatRule();
        rulepair[1].setType("SourceNatRule");
        rulepair[1].setOrder(100);

        Match m = new Match();
        m.setDestinationIpAddresses(outsideIp);
        rulepair[0].setMatch(m);
        ((DestinationNatRule) rulepair[0]).setToDestinationIpAddress(insideIp);

        // create matching snat rule
        m = new Match();
        m.setSourceIpAddresses(insideIp);
        rulepair[1].setMatch(m);
        ((SourceNatRule) rulepair[1]).setToSourceIpAddressMin(outsideIp);
        ((SourceNatRule) rulepair[1]).setToSourceIpAddressMax(outsideIp);

        return rulepair;
    }

    @Override
    public void setConfigParams(final Map<String, Object> params) {
        // TODO Auto-generated method stub
    }

    @Override
    public Map<String, Object> getConfigParams() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRunLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setRunLevel(final int level) {
        // TODO Auto-generated method stub
    }
}
