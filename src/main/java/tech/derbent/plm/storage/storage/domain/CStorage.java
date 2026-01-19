package tech.derbent.plm.storage.storage.domain;

import java.math.BigDecimal;
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
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.services.copy.CCloneOptions;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.storage.storagetype.domain.CStorageType;

/**
 * CStorage - Storage location entity representing physical storage locations (warehouses, rooms, bins).
 * Supports hierarchical organization with parent-child relationships and capacity management.
 */
@Entity
@Table(name = "\"cstorage\"")
@AttributeOverride(name = "id", column = @Column(name = "storage_id"))
public class CStorage extends CProjectItem<CStorage> implements IHasStatusAndWorkflow<CStorage>, IHasAttachments, IHasComments {

    public static final String DEFAULT_COLOR = "#4682B4"; // X11 SteelBlue - storage locations
    public static final String DEFAULT_ICON = "vaadin:storage";
    public static final String ENTITY_TITLE_PLURAL = "Storage Locations";
    public static final String ENTITY_TITLE_SINGULAR = "Storage Location";
    private static final Logger LOGGER = LoggerFactory.getLogger(CStorage.class);
    public static final String VIEW_NAME = "Storage Location View";

    // Storage type
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entitytype_id", nullable = true)
    @AMetaData(
            displayName = "Storage Type",
            required = false,
            readOnly = false,
            description = "Type category of storage location (warehouse, room, bin, etc.)",
            hidden = false,
            order = 10,
            dataProviderBean = "CStorageTypeService",
            setBackgroundFromColor = true,
            useIcon = true)
    private CStorageType entityType;

    // Location information
    @Column(nullable = true, length = 500)
    @Size(max = 500)
    @AMetaData(
            displayName = "Address",
            required = false,
            readOnly = false,
            description = "Physical address of storage location",
            hidden = false,
            order = 20,
            maxLength = 500)
    private String address;

    @Column(nullable = true, length = 255)
    @Size(max = 255)
    @AMetaData(
            displayName = "Building",
            required = false,
            readOnly = false,
            description = "Building identifier",
            hidden = false,
            order = 30,
            maxLength = 255)
    private String building;

    @Column(nullable = true, length = 255)
    @Size(max = 255)
    @AMetaData(
            displayName = "Floor",
            required = false,
            readOnly = false,
            description = "Floor level",
            hidden = false,
            order = 40,
            maxLength = 255)
    private String floor;

    @Column(nullable = true, length = 255)
    @Size(max = 255)
    @AMetaData(
            displayName = "Zone/Aisle",
            required = false,
            readOnly = false,
            description = "Zone or aisle identifier",
            hidden = false,
            order = 50,
            maxLength = 255)
    private String zone;

    @Column(nullable = true, length = 255)
    @Size(max = 255)
    @AMetaData(
            displayName = "Bin/Shelf Code",
            required = false,
            readOnly = false,
            description = "Specific bin, shelf, or rack identifier",
            hidden = false,
            order = 60,
            maxLength = 255)
    private String binCode;

    // Capacity management
    @Column(nullable = true, precision = 15, scale = 2)
    @AMetaData(
            displayName = "Capacity",
            required = false,
            readOnly = false,
            description = "Maximum capacity (units, volume, or weight)",
            hidden = false,
            order = 70)
    private BigDecimal capacity;

    @Column(nullable = true, length = 50)
    @Size(max = 50)
    @AMetaData(
            displayName = "Capacity Unit",
            required = false,
            readOnly = false,
            description = "Unit of capacity measurement (items, m3, kg, etc.)",
            hidden = false,
            order = 80,
            maxLength = 50)
    private String capacityUnit;

    @Column(nullable = true, precision = 15, scale = 2)
    @AMetaData(
            displayName = "Current Utilization",
            required = false,
            readOnly = true,
            description = "Current usage of capacity",
            hidden = false,
            order = 90)
    private BigDecimal currentUtilization;

    // Hierarchy - parent storage location
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_storage_id", nullable = true)
    @AMetaData(
            displayName = "Parent Storage",
            required = false,
            readOnly = false,
            description = "Parent storage location (e.g., warehouse contains rooms)",
            hidden = false,
            order = 100,
            dataProviderBean = "CStorageService")
    private CStorage parentStorage;

    // Responsible person
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsible_user_id", nullable = true)
    @AMetaData(
            displayName = "Responsible User",
            required = false,
            readOnly = false,
            description = "User responsible for this storage location",
            hidden = false,
            order = 110,
            dataProviderBean = "CUserService")
    private CUser responsibleUser;

    // Storage conditions
    @Column(nullable = true, length = 255)
    @Size(max = 255)
    @AMetaData(
            displayName = "Temperature Control",
            required = false,
            readOnly = false,
            description = "Temperature requirements (e.g., room temp, refrigerated, frozen)",
            hidden = false,
            order = 120,
            maxLength = 255)
    private String temperatureControl;

    @Column(nullable = true, length = 255)
    @Size(max = 255)
    @AMetaData(
            displayName = "Climate Control",
            required = false,
            readOnly = false,
            description = "Humidity or other environmental controls",
            hidden = false,
            order = 130,
            maxLength = 255)
    private String climateControl;

    @Column(nullable = true)
    @AMetaData(
            displayName = "Secure Storage",
            required = false,
            readOnly = false,
            defaultValue = "false",
            description = "Whether this is a secure/locked storage area",
            hidden = false,
            order = 140)
    private Boolean secureStorage = Boolean.FALSE;

    // Operational status
    @Column(nullable = true)
    @AMetaData(
            displayName = "Active",
            required = false,
            readOnly = false,
            defaultValue = "true",
            description = "Whether storage location is currently active",
            hidden = false,
            order = 150)
    private Boolean isActive = Boolean.TRUE;

    // Attachments (photos, layouts, documentation)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "storage_id")
    private Set<CAttachment> attachments = new HashSet<>();

    // Comments (notes, history)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "storage_id")
    private Set<CComment> comments = new HashSet<>();

    /** Default constructor for JPA. */
    protected CStorage() {
        super();
    }

    public CStorage(final String name, final CProject project) {
        super(CStorage.class, name, project);
        initializeDefaults();
    }

    @Override
    protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
        super.copyEntityTo(target, options);

        if (target instanceof CStorage) {
            final CStorage targetStorage = (CStorage) target;

            // Basic fields - always copy
            copyField(this::getAddress, targetStorage::setAddress);
            copyField(this::getBuilding, targetStorage::setBuilding);
            copyField(this::getFloor, targetStorage::setFloor);
            copyField(this::getZone, targetStorage::setZone);

            // Make bin code unique
            if (this.getBinCode() != null) {
                targetStorage.setBinCode(this.getBinCode() + "_copy");
            }

            // Capacity information
            copyField(this::getCapacity, targetStorage::setCapacity);
            copyField(this::getCapacityUnit, targetStorage::setCapacityUnit);
            // Don't copy current utilization - starts at 0

            // Storage conditions
            copyField(this::getTemperatureControl, targetStorage::setTemperatureControl);
            copyField(this::getClimateControl, targetStorage::setClimateControl);
            copyField(this::getSecureStorage, targetStorage::setSecureStorage);
            copyField(this::getIsActive, targetStorage::setIsActive);

            // Relations - conditional
            if (options.includesRelations()) {
                copyField(this::getParentStorage, targetStorage::setParentStorage);
                copyField(this::getResponsibleUser, targetStorage::setResponsibleUser);
            }

            LOGGER.debug("Copied storage location {} with options: {}", getName(), options);
        }
    }

    // Getters and Setters
    public CStorageType getEntityType() {
        return entityType;
    }

    public void setEntityType(final CStorageType entityType) {
        this.entityType = entityType;
        updateLastModified();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
        updateLastModified();
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(final String building) {
        this.building = building;
        updateLastModified();
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(final String floor) {
        this.floor = floor;
        updateLastModified();
    }

    public String getZone() {
        return zone;
    }

    public void setZone(final String zone) {
        this.zone = zone;
        updateLastModified();
    }

    public String getBinCode() {
        return binCode;
    }

    public void setBinCode(final String binCode) {
        this.binCode = binCode;
        updateLastModified();
    }

    public BigDecimal getCapacity() {
        return capacity;
    }

    public void setCapacity(final BigDecimal capacity) {
        this.capacity = capacity;
        updateLastModified();
    }

    public String getCapacityUnit() {
        return capacityUnit;
    }

    public void setCapacityUnit(final String capacityUnit) {
        this.capacityUnit = capacityUnit;
        updateLastModified();
    }

    public BigDecimal getCurrentUtilization() {
        return currentUtilization;
    }

    public void setCurrentUtilization(final BigDecimal currentUtilization) {
        this.currentUtilization = currentUtilization;
        updateLastModified();
    }

    public CStorage getParentStorage() {
        return parentStorage;
    }

    public void setParentStorage(final CStorage parentStorage) {
        this.parentStorage = parentStorage;
        updateLastModified();
    }

    public CUser getResponsibleUser() {
        return responsibleUser;
    }

    public void setResponsibleUser(final CUser responsibleUser) {
        this.responsibleUser = responsibleUser;
        updateLastModified();
    }

    public String getTemperatureControl() {
        return temperatureControl;
    }

    public void setTemperatureControl(final String temperatureControl) {
        this.temperatureControl = temperatureControl;
        updateLastModified();
    }

    public String getClimateControl() {
        return climateControl;
    }

    public void setClimateControl(final String climateControl) {
        this.climateControl = climateControl;
        updateLastModified();
    }

    public Boolean getSecureStorage() {
        return secureStorage;
    }

    public void setSecureStorage(final Boolean secureStorage) {
        this.secureStorage = secureStorage;
        updateLastModified();
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(final Boolean isActive) {
        this.isActive = isActive;
        updateLastModified();
    }

    @Override
    public Set<CAttachment> getAttachments() {
        return attachments;
    }

    @Override
    public void setAttachments(final Set<CAttachment> attachments) {
        this.attachments = attachments;
        updateLastModified();
    }

    @Override
    public Set<CComment> getComments() {
        return comments;
    }

    @Override
    public void setComments(final Set<CComment> comments) {
        this.comments = comments;
        updateLastModified();
    }

    protected void initializeDefaults() {
        if (secureStorage == null) {
            secureStorage = Boolean.FALSE;
        }
        if (isActive == null) {
            isActive = Boolean.TRUE;
        }
        if (currentUtilization == null) {
            currentUtilization = BigDecimal.ZERO;
        }
    }

    /**
     * Calculate capacity utilization percentage.
     *
     * @return utilization percentage (0-100) or null if capacity not set
     */
    public BigDecimal getUtilizationPercentage() {
        if (capacity == null || capacity.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        if (currentUtilization == null) {
            return BigDecimal.ZERO;
        }
        return currentUtilization.divide(capacity, 2, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    /**
     * Get full location path (e.g., "Warehouse A > Room 101 > Shelf 5").
     *
     * @return hierarchical location path
     */
    public String getLocationPath() {
        final StringBuilder path = new StringBuilder();
        if (parentStorage != null) {
            path.append(parentStorage.getLocationPath()).append(" > ");
        }
        path.append(getName());
        return path.toString();
    }

    @Override
    public String toString() {
        return String.format("CStorage{id=%d, name=%s, type=%s, path=%s}", getId(), getName(),
                entityType != null ? entityType.getName() : "none", getLocationPath());
    }
}
