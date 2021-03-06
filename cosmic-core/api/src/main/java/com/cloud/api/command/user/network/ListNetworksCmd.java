package com.cloud.api.command.user.network;

import com.cloud.acl.RoleType;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListTaggedResourcesCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.NetworkResponse;
import com.cloud.api.response.PhysicalNetworkResponse;
import com.cloud.api.response.VpcResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.legacymodel.network.Network;
import com.cloud.legacymodel.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listNetworks", group = APICommandGroup.NetworkService, description = "Lists all available networks.", responseObject = NetworkResponse.class, responseView = ResponseView
        .Restricted, entityType =
        {Network.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListNetworksCmd extends BaseListTaggedResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListNetworksCmd.class.getName());
    private static final String s_name = "listnetworksresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = NetworkResponse.class, description = "list networks by ID")
    private Long id;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "the zone ID of the network")
    private Long zoneId;

    @Parameter(name = ApiConstants.TYPE, type = CommandType.STRING, description = "the type of the network. Supported values are: isolated and shared")
    private String guestIpType;

    @Parameter(name = ApiConstants.IS_SYSTEM, type = CommandType.BOOLEAN, description = "true if network is system, false otherwise")
    private Boolean isSystem;

    @Parameter(name = ApiConstants.ACL_TYPE, type = CommandType.STRING, description = "list networks by ACL (access control list) type. Supported values are account and domain")
    private String aclType;

    @Parameter(name = ApiConstants.TRAFFIC_TYPE, type = CommandType.STRING, description = "type of the traffic")
    private String trafficType;

    @Parameter(name = ApiConstants.PHYSICAL_NETWORK_ID, type = CommandType.UUID, entityType = PhysicalNetworkResponse.class, description = "list networks by physical network id")
    private Long physicalNetworkId;

    @Parameter(name = ApiConstants.SUPPORTED_SERVICES, type = CommandType.LIST, collectionType = CommandType.STRING, description = "list networks supporting certain services")
    private List<String> supportedServices;

    @Parameter(name = ApiConstants.RESTART_REQUIRED, type = CommandType.BOOLEAN, description = "list networks by restartRequired")
    private Boolean restartRequired;

    @Parameter(name = ApiConstants.SPECIFY_IP_RANGES, type = CommandType.BOOLEAN, description = "true if need to list only networks which support specifying IP ranges")
    private Boolean specifyIpRanges;

    @Parameter(name = ApiConstants.VPC_ID, type = CommandType.UUID, entityType = VpcResponse.class, description = "List networks by VPC")
    private Long vpcId;

    @Parameter(name = ApiConstants.CAN_USE_FOR_DEPLOY, type = CommandType.BOOLEAN, description = "list networks available for VM deployment")
    private Boolean canUseForDeploy;

    @Parameter(name = ApiConstants.FOR_VPC, type = CommandType.BOOLEAN, description = "the network belongs to VPC")
    private Boolean forVpc;

    @Parameter(name = ApiConstants.DISPLAY_NETWORK, type = CommandType.BOOLEAN, description = "list resources by display flag; only ROOT admin is eligible to pass this " +
            "parameter", since = "4.4", authorized = {RoleType.Admin})
    private Boolean display;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public String getGuestIpType() {
        return guestIpType;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public String getAclType() {
        return aclType;
    }

    public String getTrafficType() {
        return trafficType;
    }

    public Long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    public List<String> getSupportedServices() {
        return supportedServices;
    }

    public Boolean getRestartRequired() {
        return restartRequired;
    }

    public Boolean getSpecifyIpRanges() {
        return specifyIpRanges;
    }

    public Long getVpcId() {
        return vpcId;
    }

    public Boolean canUseForDeploy() {
        return canUseForDeploy;
    }

    public Boolean getForVpc() {
        return forVpc;
    }

    @Override
    public Boolean getDisplay() {
        if (display != null) {
            return display;
        }
        return super.getDisplay();
    }

    @Override
    public void execute() {
        final Pair<List<? extends Network>, Integer> networks = _networkService.searchForNetworks(this);
        final ListResponse<NetworkResponse> response = new ListResponse<>();
        final List<NetworkResponse> networkResponses = new ArrayList<>();
        for (final Network network : networks.first()) {
            final NetworkResponse networkResponse = _responseGenerator.createNetworkResponse(ResponseView.Restricted, network);
            networkResponses.add(networkResponse);
        }
        response.setResponses(networkResponses, networks.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }
}
