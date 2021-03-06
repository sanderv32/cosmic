package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.storage.StoragePool;
import com.cloud.legacymodel.to.StorageFilerTO;
import com.cloud.model.enumeration.VolumeType;

public class MigrateVolumeCommand extends Command {

    long volumeId;
    String volumePath;
    StorageFilerTO pool;
    String attachedVmName;
    VolumeType volumeType;

    public MigrateVolumeCommand(final long volumeId, final String volumePath, final StoragePool pool, final int timeout) {
        this.volumeId = volumeId;
        this.volumePath = volumePath;
        this.pool = new StorageFilerTO(pool);
        this.setWait(timeout);
    }

    public MigrateVolumeCommand(final long volumeId, final String volumePath, final StoragePool pool, final String attachedVmName, final VolumeType volumeType, final int
            timeout) {
        this.volumeId = volumeId;
        this.volumePath = volumePath;
        this.pool = new StorageFilerTO(pool);
        this.attachedVmName = attachedVmName;
        this.volumeType = volumeType;
        this.setWait(timeout);
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public String getVolumePath() {
        return volumePath;
    }

    public long getVolumeId() {
        return volumeId;
    }

    public StorageFilerTO getPool() {
        return pool;
    }

    public String getAttachedVmName() {
        return attachedVmName;
    }

    public VolumeType getVolumeType() {
        return volumeType;
    }
}
