package com.cloud.storage.motion;

import com.cloud.agent.AgentManager;
import com.cloud.engine.subsystem.api.storage.CopyCommandResult;
import com.cloud.engine.subsystem.api.storage.DataMotionStrategy;
import com.cloud.engine.subsystem.api.storage.DataObject;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.StrategyPriority;
import com.cloud.engine.subsystem.api.storage.VolumeDataFactory;
import com.cloud.engine.subsystem.api.storage.VolumeInfo;
import com.cloud.framework.async.AsyncCompletionCallback;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.MigrateWithStorageAnswer;
import com.cloud.legacymodel.communication.command.MigrateWithStorageCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.exceptions.AgentUnavailableException;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.OperationTimedoutException;
import com.cloud.legacymodel.storage.StoragePool;
import com.cloud.legacymodel.to.StorageFilerTO;
import com.cloud.legacymodel.to.VirtualMachineTO;
import com.cloud.legacymodel.to.VolumeObjectTO;
import com.cloud.legacymodel.to.VolumeTO;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.datastore.db.PrimaryDataStoreDao;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.VMInstanceDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHyperVisorStorageMotionStrategy implements DataMotionStrategy {
    private static final Logger s_logger = LoggerFactory.getLogger(AbstractHyperVisorStorageMotionStrategy.class);
    @Inject
    protected AgentManager agentMgr;
    @Inject
    protected VolumeDao volDao;
    @Inject
    protected VolumeDataFactory volFactory;
    @Inject
    protected PrimaryDataStoreDao storagePoolDao;
    @Inject
    protected VMInstanceDao instanceDao;

    @Override
    public StrategyPriority canHandle(final DataObject srcData, final DataObject destData) {
        return StrategyPriority.CANT_HANDLE;
    }

    @Override
    public StrategyPriority canHandle(final Map<VolumeInfo, DataStore> volumeMap, final Host srcHost, final Host destHost) {
        return StrategyPriority.CANT_HANDLE;
    }

    @Override
    public Void copyAsync(final DataObject srcData, final DataObject destData, final Host destHost, final AsyncCompletionCallback<CopyCommandResult> callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Void copyAsync(final DataObject srcData, final DataObject destData, final AsyncCompletionCallback<CopyCommandResult> callback) {
        final CopyCommandResult result = new CopyCommandResult(null, null);
        result.setResult("Unsupported operation requested for copying data.");
        callback.complete(result);

        return null;
    }

    @Override
    public Void copyAsync(final Map<VolumeInfo, DataStore> volumeMap, final VirtualMachineTO vmTo, final Host srcHost, final Host destHost,
                          final AsyncCompletionCallback<CopyCommandResult> callback) {
        Answer answer = null;
        String errMsg = null;
        try {
            final VMInstanceVO instance = instanceDao.findById(vmTo.getId());
            if (instance != null) {
                if (srcHost.getClusterId().equals(destHost.getClusterId())) {
                    answer = migrateVmWithVolumesWithinCluster(instance, vmTo, srcHost, destHost, volumeMap);
                } else {
                    answer = migrateVmWithVolumesAcrossCluster(instance, vmTo, srcHost, destHost, volumeMap);
                }
            } else {
                throw new CloudRuntimeException("Unsupported operation requested for moving data.");
            }
        } catch (final Exception e) {
            s_logger.error("copy failed", e);
            errMsg = e.toString();
        }

        final CopyCommandResult result = new CopyCommandResult(null, answer);
        result.setResult(errMsg);
        callback.complete(result);
        return null;
    }

    private Answer migrateVmWithVolumesWithinCluster(
            final VMInstanceVO vm,
            final VirtualMachineTO to,
            final Host srcHost,
            final Host destHost,
            final Map<VolumeInfo, DataStore> volumeToPool
    ) throws AgentUnavailableException {

        // Initiate migration of a virtual machine with it's volumes.
        try {
            final List<Pair<VolumeTO, StorageFilerTO>> volumeToFilerto = buildVolumeMapping(volumeToPool);

            final MigrateWithStorageCommand command = new MigrateWithStorageCommand(to, volumeToFilerto, destHost.getGuid());
            final MigrateWithStorageAnswer answer = (MigrateWithStorageAnswer) agentMgr.send(destHost.getId(), command);
            if (answer == null) {
                s_logger.error("Migration with storage of vm " + vm + " failed.");
                throw new CloudRuntimeException("Error while migrating the vm " + vm + " to host " + destHost);
            } else if (!answer.getResult()) {
                s_logger.error("Migration with storage of vm " + vm + " failed. Details: " + answer.getDetails());
                throw new CloudRuntimeException("Error while migrating the vm " + vm + " to host " + destHost + ". " + answer.getDetails());
            } else {
                // Update the volume details after migration.
                updateVolumePathsAfterMigration(volumeToPool, answer.getVolumeTos());
            }

            return answer;
        } catch (final OperationTimedoutException e) {
            s_logger.error("Error while migrating vm " + vm + " to host " + destHost, e);
            throw new AgentUnavailableException("Operation timed out on storage motion for " + vm, destHost.getId());
        }
    }

    protected abstract Answer migrateVmWithVolumesAcrossCluster(
            final VMInstanceVO vm,
            final VirtualMachineTO to,
            final Host srcHost,
            final Host destHost,
            final Map<VolumeInfo, DataStore> volumeToPool
    ) throws AgentUnavailableException;

    protected List<Pair<VolumeTO, StorageFilerTO>> buildVolumeMapping(final Map<VolumeInfo, DataStore> volumeToPool) {
        final List<Pair<VolumeTO, StorageFilerTO>> volumeToFilerto = new ArrayList<>();

        for (final Map.Entry<VolumeInfo, DataStore> entry : volumeToPool.entrySet()) {
            final VolumeInfo volume = entry.getKey();
            final StoragePool storagePool = storagePoolDao.findById(volume.getPoolId());
            final VolumeTO volumeTo =
                    new VolumeTO(volume.getId(), volume.getVolumeType(), storagePool.getPoolType(), storagePool.getUuid(), volume.getName(), volume.getFolder(), volume.getPath(),
                            volume.getSize(), volume.getChainInfo(), volume.getFormat(), volume.getProvisioningType());
            final StorageFilerTO filerTo = new StorageFilerTO((StoragePool) entry.getValue());
            volumeToFilerto.add(new Pair<>(volumeTo, filerTo));
        }

        return volumeToFilerto;
    }

    protected void updateVolumePathsAfterMigration(final Map<VolumeInfo, DataStore> volumeToPool, final List<VolumeObjectTO> volumeTos) {
        for (final Map.Entry<VolumeInfo, DataStore> entry : volumeToPool.entrySet()) {
            boolean updated = false;
            final VolumeInfo volume = entry.getKey();
            final StoragePool pool = (StoragePool) entry.getValue();
            for (final VolumeObjectTO volumeTo : volumeTos) {
                if (volume.getId() == volumeTo.getId()) {
                    final VolumeVO volumeVO = volDao.findById(volume.getId());
                    final Long oldPoolId = volumeVO.getPoolId();
                    volumeVO.setPath(volumeTo.getPath());
                    volumeVO.setFolder(pool.getPath());
                    volumeVO.setPodId(pool.getPodId());
                    volumeVO.setPoolId(pool.getId());
                    volumeVO.setLastPoolId(oldPoolId);
                    volDao.update(volume.getId(), volumeVO);
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                s_logger.error("Volume path wasn't updated for volume " + volume + " after it was migrated.");
            }
        }
    }
}
