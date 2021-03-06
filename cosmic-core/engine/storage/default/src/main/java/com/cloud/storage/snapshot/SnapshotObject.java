package com.cloud.storage.snapshot;

import com.cloud.engine.subsystem.api.storage.DataObjectInStore;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.SnapshotDataFactory;
import com.cloud.engine.subsystem.api.storage.SnapshotInfo;
import com.cloud.engine.subsystem.api.storage.SnapshotStrategy;
import com.cloud.engine.subsystem.api.storage.SnapshotStrategy.SnapshotOperation;
import com.cloud.engine.subsystem.api.storage.StorageStrategyFactory;
import com.cloud.engine.subsystem.api.storage.VolumeDataFactory;
import com.cloud.engine.subsystem.api.storage.VolumeInfo;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.CopyCmdAnswer;
import com.cloud.legacymodel.communication.answer.CreateObjectAnswer;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.NoTransitionException;
import com.cloud.legacymodel.storage.ObjectInDataStoreStateMachine;
import com.cloud.legacymodel.to.DataTO;
import com.cloud.legacymodel.to.SnapshotObjectTO;
import com.cloud.legacymodel.to.VolumeObjectTO;
import com.cloud.model.enumeration.DataObjectType;
import com.cloud.model.enumeration.DataStoreRole;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.storage.Snapshot;
import com.cloud.storage.SnapshotVO;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.datastore.ObjectInDataStoreManager;
import com.cloud.storage.datastore.db.SnapshotDataStoreDao;
import com.cloud.storage.datastore.db.SnapshotDataStoreVO;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.db.QueryBuilder;
import com.cloud.utils.db.SearchCriteria.Op;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnapshotObject implements SnapshotInfo {
    private static final Logger s_logger = LoggerFactory.getLogger(SnapshotObject.class);
    @Inject
    protected SnapshotDao snapshotDao;
    @Inject
    protected VolumeDao volumeDao;
    @Inject
    protected VolumeDataFactory volFactory;
    @Inject
    protected SnapshotStateMachineManager stateMachineMgr;
    @Inject
    SnapshotDataFactory snapshotFactory;
    @Inject
    ObjectInDataStoreManager objectInStoreMgr;
    @Inject
    SnapshotDataStoreDao snapshotStoreDao;
    @Inject
    StorageStrategyFactory storageStrategyFactory;
    private SnapshotVO snapshot;
    private DataStore store;
    private Object payload;
    private String installPath; // temporarily set installPath before passing to resource for entries with empty installPath for object store migration case

    public SnapshotObject() {

    }

    public static SnapshotObject getSnapshotObject(final SnapshotVO snapshot, final DataStore store) {
        final SnapshotObject snapObj = ComponentContext.inject(SnapshotObject.class);
        snapObj.configure(snapshot, store);
        return snapObj;
    }

    protected void configure(final SnapshotVO snapshot, final DataStore store) {
        this.snapshot = snapshot;
        this.store = store;
    }

    public DataStore getStore() {
        return store;
    }

    @Override
    public SnapshotInfo getParent() {

        final SnapshotDataStoreVO snapStoreVO = snapshotStoreDao.findByStoreSnapshot(store.getRole(), store.getId(), snapshot.getId());
        Long parentId = null;
        if (snapStoreVO != null) {
            parentId = snapStoreVO.getParentSnapshotId();
            if (parentId != null && parentId != 0) {
                return snapshotFactory.getSnapshot(parentId, store);
            }
        }

        return null;
    }

    @Override
    public String getPath() {
        if (installPath != null) {
            return installPath;
        }

        final DataObjectInStore objectInStore = objectInStoreMgr.findObject(this, getDataStore());
        if (objectInStore != null) {
            return objectInStore.getInstallPath();
        }
        return null;
    }

    @Override
    public SnapshotInfo getChild() {
        final QueryBuilder<SnapshotDataStoreVO> sc = QueryBuilder.create(SnapshotDataStoreVO.class);
        sc.and(sc.entity().getDataStoreId(), Op.EQ, store.getId());
        sc.and(sc.entity().getRole(), Op.EQ, store.getRole());
        sc.and(sc.entity().getState(), Op.NIN, State.Destroying, State.Destroyed, State.Error);
        sc.and(sc.entity().getParentSnapshotId(), Op.EQ, getId());
        final SnapshotDataStoreVO vo = sc.find();
        if (vo == null) {
            return null;
        }
        return snapshotFactory.getSnapshot(vo.getId(), store);
    }

    @Override
    public VolumeInfo getBaseVolume() {
        return volFactory.getVolume(snapshot.getVolumeId());
    }

    @Override
    public void addPayload(final Object data) {
        payload = data;
    }

    @Override
    public Object getPayload() {
        return payload;
    }

    @Override
    public Long getDataCenterId() {
        return snapshot.getDataCenterId();
    }

    @Override
    public ObjectInDataStoreStateMachine.State getStatus() {
        return objectInStoreMgr.findObject(this, store).getObjectInStoreState();
    }

    @Override
    public boolean isRevertable() {
        final SnapshotStrategy snapshotStrategy = storageStrategyFactory.getSnapshotStrategy(snapshot, SnapshotOperation.REVERT);
        if (snapshotStrategy != null) {
            return true;
        }

        return false;
    }

    @Override
    public long getPhysicalSize() {
        long physicalSize = 0;
        final SnapshotDataStoreVO snapshotStore = snapshotStoreDao.findBySnapshot(snapshot.getId(), DataStoreRole.Image);
        if (snapshotStore != null) {
            physicalSize = snapshotStore.getPhysicalSize();
        }
        return physicalSize;
    }

    @Override
    public long getId() {
        return snapshot.getId();
    }

    @Override
    public String getUri() {
        return snapshot.getUuid();
    }

    @Override
    public DataTO getTO() {
        final DataTO to = store.getDriver().getTO(this);
        if (to == null) {
            SnapshotObjectTO snapshotObjectTO = new SnapshotObjectTO();

            snapshotObjectTO.setPath(this.getPath());
            snapshotObjectTO.setId(this.getId());
            final VolumeInfo vol = this.getBaseVolume();
            if (vol != null) {
                snapshotObjectTO.setVolume((VolumeObjectTO) vol.getTO());
                snapshotObjectTO.setVmName(vol.getAttachedVmName());
            }

            SnapshotInfo parentSnapshot = this.getParent();
            final ArrayList<String> parentsArry = new ArrayList<>();
            if (parentSnapshot != null) {
                snapshotObjectTO.setParentSnapshotPath(parentSnapshot.getPath());
                while (parentSnapshot != null) {
                    parentsArry.add(parentSnapshot.getPath());
                    parentSnapshot = parentSnapshot.getParent();
                }
                snapshotObjectTO.setParents(parentsArry.toArray(new String[parentsArry.size()]));
                ArrayUtils.reverse(snapshotObjectTO.getParents());
            }

            snapshotObjectTO.setDataStore(this.getDataStore().getTO());
            snapshotObjectTO.setName(this.getName());
            snapshotObjectTO.setHypervisorType(this.getHypervisorType());
            snapshotObjectTO.setQuiescevm(false);

            return snapshotObjectTO;
        }
        return to;
    }

    @Override
    public DataStore getDataStore() {
        return store;
    }

    @Override
    public Long getSize() {
        return snapshot.getSize();
    }

    @Override
    public DataObjectType getType() {
        return DataObjectType.SNAPSHOT;
    }

    @Override
    public String getUuid() {
        return snapshot.getUuid();
    }

    @Override
    public boolean delete() {
        if (store != null) {
            return store.delete(this);
        }
        return true;
    }

    @Override
    public void processEvent(final ObjectInDataStoreStateMachine.Event event) {
        try {
            objectInStoreMgr.update(this, event);
        } catch (final Exception e) {
            s_logger.debug("Failed to update state:" + e.toString());
            throw new CloudRuntimeException("Failed to update state: " + e.toString());
        } finally {
            if (event == ObjectInDataStoreStateMachine.Event.OperationFailed) {
                objectInStoreMgr.deleteIfNotReady(this);
            }
        }
    }

    @Override
    public void processEvent(final ObjectInDataStoreStateMachine.Event event, final Answer answer) {
        try {
            final SnapshotDataStoreVO snapshotStore = snapshotStoreDao.findByStoreSnapshot(getDataStore().getRole(), getDataStore().getId(), getId());
            if (answer instanceof CreateObjectAnswer) {
                final SnapshotObjectTO snapshotTO = (SnapshotObjectTO) ((CreateObjectAnswer) answer).getData();
                snapshotStore.setInstallPath(snapshotTO.getPath());
                snapshotStoreDao.update(snapshotStore.getId(), snapshotStore);
            } else if (answer instanceof CopyCmdAnswer) {
                final SnapshotObjectTO snapshotTO = (SnapshotObjectTO) ((CopyCmdAnswer) answer).getNewData();
                snapshotStore.setInstallPath(snapshotTO.getPath());
                if (snapshotTO.getPhysicalSize() != null) {
                    // For S3 delta snapshot, physical size is currently not set
                    snapshotStore.setPhysicalSize(snapshotTO.getPhysicalSize());
                }
                if (snapshotTO.getParentSnapshotPath() == null) {
                    snapshotStore.setParentSnapshotId(0L);
                }
                snapshotStoreDao.update(snapshotStore.getId(), snapshotStore);

                // update side-effect of snapshot operation
                if (snapshotTO.getVolume() != null && snapshotTO.getVolume().getPath() != null) {
                    final VolumeVO vol = volumeDao.findByUuid(snapshotTO.getVolume().getUuid());
                    if (vol != null) {
                        s_logger.info("Update volume path change due to snapshot operation, volume " + vol.getId() + " path: " + vol.getPath() + "->" +
                                snapshotTO.getVolume().getPath());
                        vol.setPath(snapshotTO.getVolume().getPath());
                        volumeDao.update(vol.getId(), vol);
                    } else {
                        s_logger.error("Cound't find the original volume with uuid: " + snapshotTO.getVolume().getUuid());
                    }
                }
            } else {
                throw new CloudRuntimeException("Unknown answer: " + answer.getClass());
            }
        } catch (final RuntimeException ex) {
            if (event == ObjectInDataStoreStateMachine.Event.OperationFailed) {
                objectInStoreMgr.deleteIfNotReady(this);
            }
            throw ex;
        }
        this.processEvent(event);
    }

    @Override
    public void incRefCount() {
        if (store == null) {
            return;
        }

        if (store.getRole() == DataStoreRole.Image || store.getRole() == DataStoreRole.ImageCache) {
            final SnapshotDataStoreVO store = snapshotStoreDao.findByStoreSnapshot(this.store.getRole(), this.store.getId(), getId());
            store.incrRefCnt();
            store.setLastUpdated(new Date());
            snapshotStoreDao.update(store.getId(), store);
        }
    }

    @Override
    public void decRefCount() {
        if (store == null) {
            return;
        }
        if (store.getRole() == DataStoreRole.Image || store.getRole() == DataStoreRole.ImageCache) {
            final SnapshotDataStoreVO store = snapshotStoreDao.findByStoreSnapshot(this.store.getRole(), this.store.getId(), getId());
            store.decrRefCnt();
            store.setLastUpdated(new Date());
            snapshotStoreDao.update(store.getId(), store);
        }
    }

    @Override
    public Long getRefCount() {
        if (store == null) {
            return null;
        }
        if (store.getRole() == DataStoreRole.Image || store.getRole() == DataStoreRole.ImageCache) {
            final SnapshotDataStoreVO store = snapshotStoreDao.findByStoreSnapshot(this.store.getRole(), this.store.getId(), getId());
            return store.getRefCnt();
        }
        return null;
    }

    public void setPath(final String installPath) {
        this.installPath = installPath;
    }

    @Override
    public long getAccountId() {
        return snapshot.getAccountId();
    }

    @Override
    public long getVolumeId() {
        return snapshot.getVolumeId();
    }

    @Override
    public String getName() {
        return snapshot.getName();
    }

    @Override
    public Date getCreated() {
        return snapshot.getCreated();
    }

    @Override
    public Type getRecurringType() {
        return snapshot.getRecurringType();
    }

    @Override
    public State getState() {
        return snapshot.getState();
    }

    @Override
    public HypervisorType getHypervisorType() {
        return snapshot.getHypervisorType();
    }

    @Override
    public boolean isRecursive() {
        return snapshot.isRecursive();
    }

    @Override
    public short getsnapshotType() {
        return snapshot.getsnapshotType();
    }

    @Override
    public long getDomainId() {
        return snapshot.getDomainId();
    }

    public void processEvent(final Snapshot.Event event) throws NoTransitionException {
        stateMachineMgr.processEvent(snapshot, event);
    }

    public SnapshotVO getSnapshotVO() {
        return snapshot;
    }

    @Override
    public Class<?> getEntityType() {
        return Snapshot.class;
    }
}
