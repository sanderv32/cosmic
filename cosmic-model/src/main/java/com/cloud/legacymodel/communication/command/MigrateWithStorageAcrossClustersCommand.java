package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.StorageFilerTO;
import com.cloud.legacymodel.to.VirtualMachineTO;
import com.cloud.legacymodel.to.VolumeTO;
import com.cloud.legacymodel.utils.Pair;

import java.util.List;

public class MigrateWithStorageAcrossClustersCommand extends Command {

    private VirtualMachineTO vm;
    private List<Pair<VolumeTO, StorageFilerTO>> volumeMapping;
    private String destinationIpAddress;

    public MigrateWithStorageAcrossClustersCommand(
            final VirtualMachineTO vm,
            final List<Pair<VolumeTO, StorageFilerTO>> volumeMapping,
            final String destinationIpAddress
    ) {
        this.vm = vm;
        this.volumeMapping = volumeMapping;
        this.destinationIpAddress = destinationIpAddress;
    }

    public VirtualMachineTO getVirtualMachine() {
        return vm;
    }

    public List<Pair<VolumeTO, StorageFilerTO>> getVolumeMapping() {
        return volumeMapping;
    }

    public String getDestinationIpAddress() {
        return destinationIpAddress;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
