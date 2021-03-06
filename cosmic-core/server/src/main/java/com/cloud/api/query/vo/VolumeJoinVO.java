package com.cloud.api.query.vo;

import com.cloud.legacymodel.storage.TemplateType;
import com.cloud.legacymodel.storage.VMTemplateStatus;
import com.cloud.legacymodel.storage.Volume;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.model.enumeration.StorageProvisioningType;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.model.enumeration.VolumeType;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "volume_view")
public class VolumeJoinVO extends BaseViewVO implements ControlledViewEntity {

    @Column(name = "vm_state")
    @Enumerated(value = EnumType.STRING)
    protected VirtualMachine.State vmState = null;
    @Column(name = "vm_type")
    @Enumerated(value = EnumType.STRING)
    protected VirtualMachineType vmType;
    @Column(name = "display_volume", updatable = true, nullable = false)
    protected boolean displayVolume;
    @Column(name = "path")
    protected String path;
    @Column(name = "device_id")
    Long deviceId = null;
    @Column(name = "volume_type")
    @Enumerated(EnumType.STRING)
    VolumeType volumeType;
    @Column(name = "provisioning_type")
    @Enumerated(EnumType.STRING)
    StorageProvisioningType provisioningType;
    @Column(name = "size")
    long size;
    @Column(name = "min_iops")
    Long minIops;
    @Column(name = "max_iops")
    Long maxIops;
    @Column(name = "attached")
    @Temporal(value = TemporalType.TIMESTAMP)
    Date attached;
    @Column(name = "bytes_read_rate")
    Long bytesReadRate;
    @Column(name = "bytes_write_rate")
    Long bytesWriteRate;
    @Column(name = "iops_read_rate")
    Long iopsReadRate;
    @Column(name = "iops_write_rate")
    Long iopsWriteRate;
    @Column(name = "iops_total_rate")
    Long iopsTotalRate;
    @Column(name = "iops_rate_per_gb")
    Boolean iopsRatePerGb = false;
    @Column(name = "cache_mode")
    String cacheMode;
    @Column(name = "chain_info", length = 65535)
    String chainInfo;
    @Column(name = "disk_controller")
    @Enumerated(value = EnumType.STRING)
    DiskControllerType diskController;
    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "name")
    private String name;
    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    private Volume.State state;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;
    @Column(name = "account_id")
    private long accountId;
    @Column(name = "account_uuid")
    private String accountUuid;
    @Column(name = "account_name")
    private String accountName = null;
    @Column(name = "account_type")
    private short accountType;
    @Column(name = "domain_id")
    private long domainId;
    @Column(name = "domain_uuid")
    private String domainUuid;
    @Column(name = "domain_name")
    private String domainName = null;
    @Column(name = "domain_path")
    private String domainPath = null;
    @Column(name = "project_id")
    private long projectId;
    @Column(name = "project_uuid")
    private String projectUuid;
    @Column(name = "project_name")
    private String projectName;
    @Column(name = "pod_id")
    private long podId;
    @Column(name = "data_center_id")
    private long dataCenterId;
    @Column(name = "data_center_uuid")
    private String dataCenterUuid;
    @Column(name = "data_center_name")
    private String dataCenterName;
    @Column(name = "vm_id")
    private long vmId;
    @Column(name = "vm_uuid")
    private String vmUuid;
    @Column(name = "vm_name")
    private String vmName;
    @Column(name = "vm_display_name")
    private String vmDisplayName;
    @Column(name = "volume_store_size")
    private long volumeStoreSize;
    @Column(name = "created_on_store")
    private Date createdOnStore;
    @Column(name = "format")
    private ImageFormat format;
    @Column(name = "download_pct")
    private int downloadPercent;
    @Column(name = "download_state")
    @Enumerated(EnumType.STRING)
    private VMTemplateStatus downloadState;
    @Column(name = "error_str")
    private String errorString;
    @Column(name = "hypervisor_type")
    @Enumerated(value = EnumType.STRING)
    private HypervisorType hypervisorType;
    @Column(name = "disk_offering_id")
    private long diskOfferingId;
    @Column(name = "disk_offering_uuid")
    private String diskOfferingUuid;
    @Column(name = "disk_offering_name")
    private String diskOfferingName;
    @Column(name = "disk_offering_display_text")
    private String diskOfferingDisplayText;
    @Column(name = "system_use")
    private boolean systemUse;
    @Column(name = "use_local_storage")
    private boolean useLocalStorage;
    @Column(name = "pool_id")
    private long poolId;
    @Column(name = "pool_uuid")
    private String poolUuid;
    @Column(name = "pool_name")
    private String poolName;
    @Column(name = "template_id")
    private long templateId;
    @Column(name = "template_uuid")
    private String templateUuid;
    @Column(name = "template_name")
    private String templateName;
    @Column(name = "template_display_text", length = 4096)
    private String templateDisplayText;
    @Column(name = "extractable")
    private boolean extractable;
    @Column(name = "template_type")
    private TemplateType templateType;
    @Column(name = "iso_id", updatable = true, nullable = true, length = 17)
    private long isoId;
    @Column(name = "iso_uuid")
    private String isoUuid;
    @Column(name = "iso_name")
    private String isoName;
    @Column(name = "iso_display_text", length = 4096)
    private String isoDisplayText;
    @Column(name = "job_id")
    private Long jobId;
    @Column(name = "job_uuid")
    private String jobUuid;
    @Column(name = "job_status")
    private int jobStatus;
    @Column(name = "tag_id")
    private long tagId;
    @Column(name = "tag_uuid")
    private String tagUuid;
    @Column(name = "tag_key")
    private String tagKey;
    @Column(name = "tag_value")
    private String tagValue;
    @Column(name = "tag_domain_id")
    private long tagDomainId;
    @Column(name = "tag_account_id")
    private long tagAccountId;
    @Column(name = "tag_resource_id")
    private long tagResourceId;
    @Column(name = "tag_resource_uuid")
    private String tagResourceUuid;
    @Column(name = "tag_resource_type")
    @Enumerated(value = EnumType.STRING)
    private ResourceObjectType tagResourceType;
    @Column(name = "tag_customer")
    private String tagCustomer;

    public VolumeJoinVO() {
    }

    public void setVmState(final VirtualMachine.State vmState) {
        this.vmState = vmState;
    }

    public void setVmType(final VirtualMachineType vmType) {
        this.vmType = vmType;
    }

    public void setDisplayVolume(final boolean displayVolume) {
        this.displayVolume = displayVolume;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setDeviceId(final Long deviceId) {
        this.deviceId = deviceId;
    }

    public void setVolumeType(final VolumeType volumeType) {
        this.volumeType = volumeType;
    }

    public void setProvisioningType(final StorageProvisioningType provisioningType) {
        this.provisioningType = provisioningType;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public void setMinIops(final Long minIops) {
        this.minIops = minIops;
    }

    public void setMaxIops(final Long maxIops) {
        this.maxIops = maxIops;
    }

    public void setAttached(final Date attached) {
        this.attached = attached;
    }

    public void setBytesReadRate(final Long bytesReadRate) {
        this.bytesReadRate = bytesReadRate;
    }

    public void setBytesWriteRate(final Long bytesWriteRate) {
        this.bytesWriteRate = bytesWriteRate;
    }

    public void setIopsReadRate(final Long iopsReadRate) {
        this.iopsReadRate = iopsReadRate;
    }

    public void setIopsWriteRate(final Long iopsWriteRate) {
        this.iopsWriteRate = iopsWriteRate;
    }

    public void setIopsTotalRate(Long iopsTotalRate) {
        this.iopsTotalRate = iopsTotalRate;
    }

    public void setIopsRatePerGb(Boolean iopsRatePerGb) {
        this.iopsRatePerGb = iopsRatePerGb;
    }

    public void setCacheMode(final String cacheMode) {
        this.cacheMode = cacheMode;
    }

    public void setChainInfo(final String chainInfo) {
        this.chainInfo = chainInfo;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setState(final Volume.State state) {
        this.state = state;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    public void setAccountUuid(final String accountUuid) {
        this.accountUuid = accountUuid;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public void setAccountType(final short accountType) {
        this.accountType = accountType;
    }

    public void setDomainId(final long domainId) {
        this.domainId = domainId;
    }

    public void setDomainUuid(final String domainUuid) {
        this.domainUuid = domainUuid;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setDomainPath(final String domainPath) {
        this.domainPath = domainPath;
    }

    public void setProjectId(final long projectId) {
        this.projectId = projectId;
    }

    public void setProjectUuid(final String projectUuid) {
        this.projectUuid = projectUuid;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public void setPodId(final long podId) {
        this.podId = podId;
    }

    public void setDataCenterId(final long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public void setDataCenterUuid(final String dataCenterUuid) {
        this.dataCenterUuid = dataCenterUuid;
    }

    public void setDataCenterName(final String dataCenterName) {
        this.dataCenterName = dataCenterName;
    }

    public void setVmId(final long vmId) {
        this.vmId = vmId;
    }

    public void setVmUuid(final String vmUuid) {
        this.vmUuid = vmUuid;
    }

    public void setVmName(final String vmName) {
        this.vmName = vmName;
    }

    public void setVmDisplayName(final String vmDisplayName) {
        this.vmDisplayName = vmDisplayName;
    }

    public void setVolumeStoreSize(final long volumeStoreSize) {
        this.volumeStoreSize = volumeStoreSize;
    }

    public void setCreatedOnStore(final Date createdOnStore) {
        this.createdOnStore = createdOnStore;
    }

    public void setFormat(final ImageFormat format) {
        this.format = format;
    }

    public void setDownloadPercent(final int downloadPercent) {
        this.downloadPercent = downloadPercent;
    }

    public void setDownloadState(final VMTemplateStatus downloadState) {
        this.downloadState = downloadState;
    }

    public void setErrorString(final String errorString) {
        this.errorString = errorString;
    }

    public void setHypervisorType(final HypervisorType hypervisorType) {
        this.hypervisorType = hypervisorType;
    }

    public void setDiskOfferingId(final long diskOfferingId) {
        this.diskOfferingId = diskOfferingId;
    }

    public void setDiskOfferingUuid(final String diskOfferingUuid) {
        this.diskOfferingUuid = diskOfferingUuid;
    }

    public void setDiskOfferingName(final String diskOfferingName) {
        this.diskOfferingName = diskOfferingName;
    }

    public void setDiskOfferingDisplayText(final String diskOfferingDisplayText) {
        this.diskOfferingDisplayText = diskOfferingDisplayText;
    }

    public void setSystemUse(final boolean systemUse) {
        this.systemUse = systemUse;
    }

    public void setUseLocalStorage(final boolean useLocalStorage) {
        this.useLocalStorage = useLocalStorage;
    }

    public void setPoolId(final long poolId) {
        this.poolId = poolId;
    }

    public void setPoolUuid(final String poolUuid) {
        this.poolUuid = poolUuid;
    }

    public void setPoolName(final String poolName) {
        this.poolName = poolName;
    }

    public void setTemplateId(final long templateId) {
        this.templateId = templateId;
    }

    public void setTemplateUuid(final String templateUuid) {
        this.templateUuid = templateUuid;
    }

    public void setTemplateName(final String templateName) {
        this.templateName = templateName;
    }

    public void setTemplateDisplayText(final String templateDisplayText) {
        this.templateDisplayText = templateDisplayText;
    }

    public void setExtractable(final boolean extractable) {
        this.extractable = extractable;
    }

    public void setTemplateType(final TemplateType templateType) {
        this.templateType = templateType;
    }

    public void setIsoId(final long isoId) {
        this.isoId = isoId;
    }

    public void setIsoUuid(final String isoUuid) {
        this.isoUuid = isoUuid;
    }

    public void setIsoName(final String isoName) {
        this.isoName = isoName;
    }

    public void setIsoDisplayText(final String isoDisplayText) {
        this.isoDisplayText = isoDisplayText;
    }

    public void setJobId(final Long jobId) {
        this.jobId = jobId;
    }

    public void setJobUuid(final String jobUuid) {
        this.jobUuid = jobUuid;
    }

    public void setJobStatus(final int jobStatus) {
        this.jobStatus = jobStatus;
    }

    public void setTagId(final long tagId) {
        this.tagId = tagId;
    }

    public void setTagUuid(final String tagUuid) {
        this.tagUuid = tagUuid;
    }

    public void setTagKey(final String tagKey) {
        this.tagKey = tagKey;
    }

    public void setTagValue(final String tagValue) {
        this.tagValue = tagValue;
    }

    public void setTagDomainId(final long tagDomainId) {
        this.tagDomainId = tagDomainId;
    }

    public void setTagAccountId(final long tagAccountId) {
        this.tagAccountId = tagAccountId;
    }

    public void setTagResourceId(final long tagResourceId) {
        this.tagResourceId = tagResourceId;
    }

    public void setTagResourceUuid(final String tagResourceUuid) {
        this.tagResourceUuid = tagResourceUuid;
    }

    public void setTagResourceType(final ResourceObjectType tagResourceType) {
        this.tagResourceType = tagResourceType;
    }

    public void setTagCustomer(final String tagCustomer) {
        this.tagCustomer = tagCustomer;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public Long getDeviceId() {
        return this.deviceId;
    }

    public VolumeType getVolumeType() {
        return this.volumeType;
    }

    public StorageProvisioningType getProvisioningType() {
        return this.provisioningType;
    }

    public long getSize() {
        return this.size;
    }

    public Long getMinIops() {
        return this.minIops;
    }

    public Long getMaxIops() {
        return this.maxIops;
    }

    public Volume.State getState() {
        return this.state;
    }

    public Date getCreated() {
        return this.created;
    }

    public Date getAttached() {
        return this.attached;
    }

    public Date getRemoved() {
        return this.removed;
    }

    @Override
    public long getAccountId() {
        return this.accountId;
    }

    public boolean isDisplayVolume() {
        return this.displayVolume;
    }

    @Override
    public long getDomainId() {
        return this.domainId;
    }

    @Override
    public String getDomainPath() {
        return this.domainPath;
    }

    @Override
    public short getAccountType() {
        return this.accountType;
    }

    @Override
    public String getAccountUuid() {
        return this.accountUuid;
    }

    @Override
    public String getAccountName() {
        return this.accountName;
    }

    @Override
    public String getDomainUuid() {
        return this.domainUuid;
    }

    @Override
    public String getDomainName() {
        return this.domainName;
    }

    @Override
    public String getProjectUuid() {
        return this.projectUuid;
    }

    @Override
    public String getProjectName() {
        return this.projectName;
    }

    public long getProjectId() {
        return this.projectId;
    }

    public long getVmId() {
        return this.vmId;
    }

    public String getVmUuid() {
        return this.vmUuid;
    }

    public String getVmName() {
        return this.vmName;
    }

    public String getVmDisplayName() {
        return this.vmDisplayName;
    }

    public VirtualMachine.State getVmState() {
        return this.vmState;
    }

    public VirtualMachineType getVmType() {
        return this.vmType;
    }

    public long getVolumeStoreSize() {
        return this.volumeStoreSize;
    }

    public Date getCreatedOnStore() {
        return this.createdOnStore;
    }

    public ImageFormat getFormat() {
        return this.format;
    }

    public int getDownloadPercent() {
        return this.downloadPercent;
    }

    public VMTemplateStatus getDownloadState() {
        return this.downloadState;
    }

    public String getErrorString() {
        return this.errorString;
    }

    public HypervisorType getHypervisorType() {
        return this.hypervisorType;
    }

    public long getDiskOfferingId() {
        return this.diskOfferingId;
    }

    public String getDiskOfferingUuid() {
        return this.diskOfferingUuid;
    }

    public String getDiskOfferingName() {
        return this.diskOfferingName;
    }

    public String getDiskOfferingDisplayText() {
        return this.diskOfferingDisplayText;
    }

    public boolean isUseLocalStorage() {
        return this.useLocalStorage;
    }

    public Long getBytesReadRate() {
        return this.bytesReadRate;
    }

    public Long getBytesWriteRate() {
        return this.bytesWriteRate;
    }

    public Long getIopsReadRate() {
        return this.iopsReadRate;
    }

    public Long getIopsWriteRate() {
        return this.iopsWriteRate;
    }

    public Long getIopsTotalRate() {
        return iopsTotalRate;
    }

    public Boolean getIopsRatePerGb() {
        return iopsRatePerGb;
    }

    public String getCacheMode() {
        return this.cacheMode;
    }

    public long getPoolId() {
        return this.poolId;
    }

    public String getPoolUuid() {
        return this.poolUuid;
    }

    public String getPoolName() {
        return this.poolName;
    }

    public long getTemplateId() {
        return this.templateId;
    }

    public String getTemplateUuid() {
        return this.templateUuid;
    }

    public boolean isExtractable() {
        return this.extractable;
    }

    public TemplateType getTemplateType() {
        return this.templateType;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public String getTemplateDisplayText() {
        return this.templateDisplayText;
    }

    public long getIsoId() {
        return this.isoId;
    }

    public String getIsoUuid() {
        return this.isoUuid;
    }

    public String getIsoName() {
        return this.isoName;
    }

    public String getIsoDisplayText() {
        return this.isoDisplayText;
    }

    public Long getJobId() {
        return this.jobId;
    }

    public String getJobUuid() {
        return this.jobUuid;
    }

    public int getJobStatus() {
        return this.jobStatus;
    }

    public long getTagId() {
        return this.tagId;
    }

    public String getTagUuid() {
        return this.tagUuid;
    }

    public String getTagKey() {
        return this.tagKey;
    }

    public String getTagValue() {
        return this.tagValue;
    }

    public long getTagDomainId() {
        return this.tagDomainId;
    }

    public long getTagAccountId() {
        return this.tagAccountId;
    }

    public long getTagResourceId() {
        return this.tagResourceId;
    }

    public String getTagResourceUuid() {
        return this.tagResourceUuid;
    }

    public ResourceObjectType getTagResourceType() {
        return this.tagResourceType;
    }

    public String getTagCustomer() {
        return this.tagCustomer;
    }

    public long getDataCenterId() {
        return this.dataCenterId;
    }

    public String getDataCenterUuid() {
        return this.dataCenterUuid;
    }

    public String getDataCenterName() {
        return this.dataCenterName;
    }

    public long getPodId() {
        return this.podId;
    }

    public boolean isSystemUse() {
        return this.systemUse;
    }

    public String getPath() {
        return this.path;
    }

    public String getChainInfo() {
        return this.chainInfo;
    }

    @Override
    public Class<?> getEntityType() {
        return Volume.class;
    }

    public DiskControllerType getDiskController() {
        return this.diskController;
    }
}
