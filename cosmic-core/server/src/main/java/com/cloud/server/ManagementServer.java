package com.cloud.server;

import com.cloud.host.DetailVO;
import com.cloud.host.HostVO;
import com.cloud.storage.GuestOSHypervisorVO;
import com.cloud.storage.GuestOSVO;
import com.cloud.utils.Pair;
import com.cloud.utils.component.PluggableService;
import com.cloud.vm.VirtualMachine;

/**
 */
public interface ManagementServer extends ManagementService, PluggableService {

    /**
     * returns the instance id of this management server.
     *
     * @return id of the management server
     */
    long getId();

    /**
     * Fetches the version of cloud stack
     */
    @Override
    String getVersion();

    /**
     * Retrieves a host by id
     *
     * @param hostId
     * @return Host
     */
    HostVO getHostBy(long hostId);

    DetailVO findDetail(long hostId, String name);

    String getConsoleAccessUrlRoot(long vmId);

    GuestOSVO getGuestOs(Long guestOsId);

    GuestOSHypervisorVO getGuestOsHypervisor(Long guestOsHypervisorId);

    /**
     * Returns the vnc port of the vm.
     *
     * @param VirtualMachine vm
     * @return the vnc port if found; -1 if unable to find.
     */
    Pair<String, Integer> getVncPort(VirtualMachine vm);

    public long getMemoryOrCpuCapacityByHost(Long hostId, short capacityType);

    public boolean getXenserverDeploymentsEnabled();

    public boolean getKvmDeploymentsEnabled();
}
