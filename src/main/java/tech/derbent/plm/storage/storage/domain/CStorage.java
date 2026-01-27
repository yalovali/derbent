package tech.derbent.plm.storage.storage.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;
import tech.derbent.plm.storage.storagetype.domain.CStorageType;

@Entity
@Table (name = "cstorage")
@AttributeOverride (name = "id", column = @Column (name = "storage_id"))
public class CStorage extends CProjectItem<CStorage> implements IHasStatusAndWorkflow<CStorage>, IHasAttachments, IHasComments, IHasLinks {

	public static final String DEFAULT_COLOR = "#006699";
	public static final String DEFAULT_ICON = "vaadin:warehouse";
	public static final String ENTITY_TITLE_PLURAL = "Storages";
	public static final String ENTITY_TITLE_SINGULAR = "Storage";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CStorage.class);
	public static final String VIEW_NAME = "Storage View";
	@Column (name = "active")
	@AMetaData (displayName = "Active", required = false, description = "Is this storage active")
	private Boolean active = Boolean.TRUE;
	@Column (length = 500)
	@Size (max = 500)
	@AMetaData (displayName = "Address", required = false, description = "Address of the storage location", maxLength = 500)
	private String address;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "storage_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Attachments for this storage", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@Column (name = "bin_code", length = 255)
	@Size (max = 255)
	@AMetaData (displayName = "Bin Code", required = false, description = "Bin/shelf code", maxLength = 255)
	private String binCode;
	@Column (length = 255)
	@Size (max = 255)
	@AMetaData (displayName = "Building", required = false, description = "Building identifier", maxLength = 255)
	private String building;
	@Column (name = "capacity", precision = 19, scale = 2)
	@AMetaData (displayName = "Capacity", required = false, description = "Maximum capacity", maxLength = 255)
	private BigDecimal capacity;
	@Column (name = "capacity_unit", length = 50)
	@Size (max = 50)
	@AMetaData (displayName = "Capacity Unit", required = false, description = "Unit for capacity (m3, pallets, etc.)", maxLength = 50)
	private String capacityUnit;
	@Column (name = "climate_control", length = 255)
	@Size (max = 255)
	@AMetaData (displayName = "Climate Control", required = false, description = "Climate control details", maxLength = 255)
	private String climateControl;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "storage_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this storage", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (name = "current_utilization", precision = 19, scale = 2)
	@AMetaData (displayName = "Current Utilization", required = false, description = "Current utilization value", maxLength = 255)
	private BigDecimal currentUtilization = BigDecimal.ZERO;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id")
	@AMetaData (
			displayName = "Storage Type", required = false, description = "Type of storage (warehouse, room, shelf)",
			dataProviderBean = "CStorageTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CStorageType entityType;
	@Column (length = 255)
	@Size (max = 255)
	@AMetaData (displayName = "Floor", required = false, description = "Floor or level", maxLength = 255)
	private String floor;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "storage_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related entities linked to this storage", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "parent_storage_id")
	@AMetaData (displayName = "Parent Storage", required = false, description = "Parent storage for hierarchy", dataProviderBean = "CStorageService")
	private CStorage parentStorage;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "responsible_user_id")
	@AMetaData (displayName = "Responsible User", required = false, description = "Person managing this storage", dataProviderBean = "CUserService")
	private CUser responsibleUser;
	@Column (name = "secure_storage")
	@AMetaData (displayName = "Secure Storage", required = false, description = "Whether this storage is secured")
	private Boolean secureStorage = Boolean.FALSE;
	@Column (name = "temperature_control", length = 255)
	@Size (max = 255)
	@AMetaData (displayName = "Temperature Control", required = false, description = "Temperature control details", maxLength = 255)
	private String temperatureControl;
	@Column (length = 255)
	@Size (max = 255)
	@AMetaData (displayName = "Zone", required = false, description = "Zone or area", maxLength = 255)
	private String zone;
	/** Default constructor for JPA. */
	protected CStorage() {
	}

	public CStorage(final String name, final CProject<?> project) {
		super(CStorage.class, name, project);
		initializeDefaults();
	}

	@Override
	@SuppressWarnings ("rawtypes")
	protected void copyEntityTo(final CEntityDB<?> target, final CAbstractService serviceTarget, final CCloneOptions options) {
		super.copyEntityTo(target, serviceTarget, options);
		if (!(target instanceof final CStorage targetStorage)) {
			return;
		}
		copyField(this::getEntityType, targetStorage::setEntityType);
		copyField(this::getParentStorage, targetStorage::setParentStorage);
		copyField(this::getResponsibleUser, targetStorage::setResponsibleUser);
		copyField(this::getCapacity, targetStorage::setCapacity);
		copyField(this::getCapacityUnit, targetStorage::setCapacityUnit);
		copyField(this::getCurrentUtilization, targetStorage::setCurrentUtilization);
		copyField(this::getAddress, targetStorage::setAddress);
		copyField(this::getBuilding, targetStorage::setBuilding);
		copyField(this::getFloor, targetStorage::setFloor);
		copyField(this::getZone, targetStorage::setZone);
		copyField(this::getBinCode, targetStorage::setBinCode);
		copyField(this::getTemperatureControl, targetStorage::setTemperatureControl);
		copyField(this::getClimateControl, targetStorage::setClimateControl);
		copyField(this::getSecureStorage, targetStorage::setSecureStorage);
		copyField(this::getActive, targetStorage::setActive);
	}

	@Override
	public Boolean getActive() { return active; }

	public String getAddress() { return address; }

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	public String getBinCode() { return binCode; }

	public String getBuilding() { return building; }

	public BigDecimal getCapacity() { return capacity; }

	public String getCapacityUnit() { return capacityUnit; }

	public String getClimateControl() { return climateControl; }

	@Override
	public Set<CComment> getComments() { return comments; }

	public BigDecimal getCurrentUtilization() { return currentUtilization; }

	@Override
	public CStorageType getEntityType() { return entityType; }

	public String getFloor() { return floor; }

	@Override
	public Set<CLink> getLinks() { return links; }

	public String getLocationPath() {
		if (parentStorage == null) {
			return getName();
		}
		return parentStorage.getLocationPath() + " > " + getName();
	}

	public CStorage getParentStorage() { return parentStorage; }

	public CUser getResponsibleUser() { return responsibleUser; }

	public Boolean getSecureStorage() { return secureStorage; }

	public String getTemperatureControl() { return temperatureControl; }

	public BigDecimal getUtilizationPercentage() {
		if (capacity == null || capacity.signum() == 0 || currentUtilization == null) {
			return BigDecimal.ZERO;
		}
		return currentUtilization.multiply(BigDecimal.valueOf(100)).divide(capacity, 2, RoundingMode.HALF_UP);
	}

	@Override
	public CWorkflowEntity getWorkflow() { return entityType != null ? entityType.getWorkflow() : null; }

	public String getZone() { return zone; }

	private final void initializeDefaults() {
		address = "";
		binCode = "";
		building = "";
		floor = "";
		zone = "";
		capacity = BigDecimal.ZERO;
		capacityUnit = "";
		temperatureControl = "";
		climateControl = "";
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public void setActive(final Boolean active) { this.active = active; }

	public void setAddress(final String address) { this.address = address; }

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setBinCode(final String binCode) { this.binCode = binCode; }

	public void setBuilding(final String building) { this.building = building; }

	public void setCapacity(final BigDecimal capacity) { this.capacity = capacity; }

	public void setCapacityUnit(final String capacityUnit) { this.capacityUnit = capacityUnit; }

	public void setClimateControl(final String climateControl) { this.climateControl = climateControl; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setCurrentUtilization(final BigDecimal currentUtilization) { this.currentUtilization = currentUtilization; }

	@Override
	public void setEntityType(final CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CStorageType.class, "Type entity must be an instance of CStorageType");
		Check.notNull(getProject(), "Project must be set before assigning storage type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning storage type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning storage type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match storage project company id " + getProject().getCompany().getId());
		entityType = (CStorageType) typeEntity;
		updateLastModified();
	}

	public void setFloor(final String floor) { this.floor = floor; }

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setParentStorage(final CStorage parentStorage) { this.parentStorage = parentStorage; }

	public void setResponsibleUser(final CUser responsibleUser) { this.responsibleUser = responsibleUser; }

	public void setSecureStorage(final Boolean secureStorage) { this.secureStorage = secureStorage; }

	public void setTemperatureControl(final String temperatureControl) { this.temperatureControl = temperatureControl; }

	public void setZone(final String zone) { this.zone = zone; }
}
