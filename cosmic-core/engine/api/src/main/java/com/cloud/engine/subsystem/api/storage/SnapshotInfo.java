package com.cloud.engine.subsystem.api.storage;

import com.cloud.legacymodel.storage.ObjectInDataStoreStateMachine;
import com.cloud.storage.Snapshot;

public interface SnapshotInfo extends DataObject, Snapshot {
    SnapshotInfo getParent();

    String getPath();

    SnapshotInfo getChild();

    VolumeInfo getBaseVolume();

    void addPayload(Object data);

    Object getPayload();

    Long getDataCenterId();

    ObjectInDataStoreStateMachine.State getStatus();

    boolean isRevertable();

    long getPhysicalSize();
}
