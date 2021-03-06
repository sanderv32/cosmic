package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.StorageFilerTO;
import com.cloud.legacymodel.to.VirtualMachineTO;
import com.cloud.legacymodel.to.VolumeTO;
import com.cloud.legacymodel.utils.Pair;

import java.util.List;

public class MigrateWithStorageReceiveCommand extends Command {
    VirtualMachineTO vm;
    List<Pair<VolumeTO, StorageFilerTO>> volumeToFiler;

    public MigrateWithStorageReceiveCommand(final VirtualMachineTO vm, final List<Pair<VolumeTO, StorageFilerTO>> volumeToFiler) {
        this.vm = vm;
        this.volumeToFiler = volumeToFiler;
    }

    public VirtualMachineTO getVirtualMachine() {
        return vm;
    }

    public List<Pair<VolumeTO, StorageFilerTO>> getVolumeToFiler() {
        return volumeToFiler;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
