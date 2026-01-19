package tech.derbent.plm.storage.storageitem.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import tech.derbent.plm.providers.provider.domain.CProvider;
import tech.derbent.plm.storage.storage.domain.CStorage;
import tech.derbent.plm.storage.storageitem.service.CStorageItemType;

/**
 * CStorageItem - Consumable inventory items stored in storage locations.
 * Tracks quantity, reorder points, and transaction history.
 */
@Entity
@Table(name = "\"cstorageitem\"")
@AttributeOverride(name = "id", column = @Column(name = "storageitem_id"))
public class CStorageItem extends CProjectItem<CStorageItem>
        implements IHasStatusAndWorkflow<CStorageItem>, IHasAttachments, IHasComments {

    public static final String DEFAULT_COLOR = "#20B2AA"; // X11 LightSeaGreen - consumables
    public static final String DEFAULT_ICON = "vaadin:package";
    public static final String ENTITY_TITLE_PLURAL = "Storage Items";
    public static final String ENTITY_TITLE_SINGULAR = "Storage Item";
    private static final Logger LOGGER = LoggerFactory.getLogger(CStorageItem.class);
    public static final String VIEW_NAME = "Storage Item View";

    // Item type
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entitytype_id", nullable = true)
    @AMetaData(
            displayName = "Item Type",
            required = false,
            readOnly = false,
            description = "Type category of storage item",
            hidden = false,
            order = 10,
            dataProviderBean = "CStorageItemTypeService",
            setBackgroundFromColor = true,
            useIcon = true)
    private CStorageItemType entityType;

    // Storage location
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storage_id", nullable = false)
    @AMetaData(
            displayName = "Storage Location",
            required = true,
            readOnly = false,
            description = "Where this item is stored",
            hidden = false,
            order = 20,
            dataProviderBean = "CStorageService")
    private CStorage storage;

    // Item identification
    @Column(nullable = true, length = 100)
    @Size(max = 100)
    @AMetaData(
            displayName = "SKU",
            required = false,
            readOnly = false,
            description = "Stock Keeping Unit identifier",
            hidden = false,
            order = 30,
            maxLength = 100)
    private String sku;

    @Column(nullable = true, length = 100)
    @Size(max = 100)
    @AMetaData(
            displayName = "Barcode",
            required = false,
            readOnly = false,
            description = "Barcode or QR code for scanning",
            hidden = false,
            order = 40,
            maxLength = 100)
    private String barcode;

    @Column(nullable = true, length = 255)
    @Size(max = 255)
    @AMetaData(
            displayName = "Manufacturer",
            required = false,
            readOnly = false,
            description = "Item manufacturer or brand",
            hidden = false,
            order = 50,
            maxLength = 255)
    private String manufacturer;

    @Column(nullable = true, length = 255)
    @Size(max = 255)
    @AMetaData(
            displayName = "Model/Part Number",
            required = false,
            readOnly = false,
            description = "Manufacturer model or part number",
            hidden = false,
            order = 60,
            maxLength = 255)
    private String modelNumber;

    // Quantity management
    @Column(nullable = false, precision = 15, scale = 2)
    @AMetaData(
            displayName = "Current Quantity",
            required = true,
            readOnly = false,
            defaultValue = "0",
            description = "Current quantity in stock",
            hidden = false,
            order = 70)
    private BigDecimal currentQuantity = BigDecimal.ZERO;

    @Column(nullable = true, length = 50)
    @Size(max = 50)
    @AMetaData(
            displayName = "Unit of Measure",
            required = false,
            readOnly = false,
            description = "Unit (pieces, kg, liters, boxes, etc.)",
            hidden = false,
            order = 80,
            maxLength = 50)
    private String unitOfMeasure;

    @Column(nullable = true, precision = 15, scale = 2)
    @AMetaData(
            displayName = "Minimum Stock Level",
            required = false,
            readOnly = false,
            description = "Minimum quantity before reorder alert",
            hidden = false,
            order = 90)
    private BigDecimal minimumStockLevel;

    @Column(nullable = true, precision = 15, scale = 2)
    @AMetaData(
            displayName = "Reorder Quantity",
            required = false,
            readOnly = false,
            description = "Quantity to order when restocking",
            hidden = false,
            order = 100)
    private BigDecimal reorderQuantity;

    @Column(nullable = true, precision = 15, scale = 2)
    @AMetaData(
            displayName = "Maximum Stock Level",
            required = false,
            readOnly = false,
            description = "Maximum quantity to maintain",
            hidden = false,
            order = 110)
    private BigDecimal maximumStockLevel;

    // Cost information
    @Column(nullable = true, precision = 15, scale = 2)
    @AMetaData(
            displayName = "Unit Cost",
            required = false,
            readOnly = false,
            description = "Cost per unit",
            hidden = false,
            order = 120)
    private BigDecimal unitCost;

    @Column(nullable = true, length = 10)
    @Size(max = 10)
    @AMetaData(
            displayName = "Currency",
            required = false,
            readOnly = false,
            defaultValue = "EUR",
            description = "Currency code (EUR, USD, etc.)",
            hidden = false,
            order = 130,
            maxLength = 10)
    private String currency = "EUR";

    // Expiration and batch tracking
    @Column(nullable = true, length = 100)
    @Size(max = 100)
    @AMetaData(
            displayName = "Batch/Lot Number",
            required = false,
            readOnly = false,
            description = "Manufacturing batch or lot number",
            hidden = false,
            order = 140,
            maxLength = 100)
    private String batchNumber;

    @Column(nullable = true)
    @AMetaData(
            displayName = "Expiration Date",
            required = false,
            readOnly = false,
            description = "Expiration or best-before date",
            hidden = false,
            order = 150)
    private LocalDate expirationDate;

    @Column(nullable = true)
    @AMetaData(
            displayName = "Track Expiration",
            required = false,
            readOnly = false,
            defaultValue = "false",
            description = "Whether to track and alert on expiration",
            hidden = false,
            order = 160)
    private Boolean trackExpiration = Boolean.FALSE;

    // Supplier information
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provider_id", nullable = true)
    @AMetaData(
            displayName = "Supplier",
            required = false,
            readOnly = false,
            description = "Preferred supplier for this item",
            hidden = false,
            order = 170,
            dataProviderBean = "CProviderService")
    private CProvider supplier;

    @Column(nullable = true, precision = 15, scale = 2)
    @AMetaData(
            displayName = "Lead Time (Days)",
            required = false,
            readOnly = false,
            description = "Typical delivery lead time in days",
            hidden = false,
            order = 180)
    private BigDecimal leadTimeDays;

    // Item properties
    @Column(nullable = true)
    @AMetaData(
            displayName = "Is Consumable",
            required = false,
            readOnly = false,
            defaultValue = "true",
            description = "Item is consumed and depletes over time",
            hidden = false,
            order = 190)
    private Boolean isConsumable = Boolean.TRUE;

    @Column(nullable = true)
    @AMetaData(
            displayName = "Requires Special Handling",
            required = false,
            readOnly = false,
            defaultValue = "false",
            description = "Hazardous or fragile materials",
            hidden = false,
            order = 200)
    private Boolean requiresSpecialHandling = Boolean.FALSE;

    @Column(nullable = true, length = 500)
    @Size(max = 500)
    @AMetaData(
            displayName = "Handling Instructions",
            required = false,
            readOnly = false,
            description = "Special handling or storage requirements",
            hidden = false,
            order = 210,
            maxLength = 500)
    private String handlingInstructions;

    // Status fields
    @Column(nullable = true)
    @AMetaData(
            displayName = "Active",
            required = false,
            readOnly = false,
            defaultValue = "true",
            description = "Item is currently stocked and available",
            hidden = false,
            order = 220)
    private Boolean isActive = Boolean.TRUE;

    @Column(nullable = true)
    @AMetaData(
            displayName = "Last Restocked Date",
            required = false,
            readOnly = true,
            description = "Date of last stock replenishment",
            hidden = false,
            order = 230)
    private LocalDate lastRestockedDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsible_user_id", nullable = true)
    @AMetaData(
            displayName = "Responsible User",
            required = false,
            readOnly = false,
            description = "User responsible for managing this item",
            hidden = false,
            order = 240,
            dataProviderBean = "CUserService")
    private CUser responsibleUser;

    // Attachments and comments
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "storageitem_id")
    private Set<CAttachment> attachments = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "storageitem_id")
    private Set<CComment> comments = new HashSet<>();

    /** Default constructor for JPA. */
    protected CStorageItem() {
        super();
    }

    public CStorageItem(final String name, final CProject project, final CStorage storage) {
        super(CStorageItem.class, name, project);
        this.storage = storage;
        initializeDefaults();
    }

    @Override
    protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
        super.copyEntityTo(target, options);

        if (target instanceof CStorageItem) {
            final CStorageItem targetItem = (CStorageItem) target;

            // Identification - make unique
            if (this.getSku() != null) {
                targetItem.setSku(this.getSku() + "_copy");
            }
            if (this.getBarcode() != null) {
                targetItem.setBarcode(this.getBarcode() + "_copy");
            }
            if (this.getBatchNumber() != null) {
                targetItem.setBatchNumber(this.getBatchNumber() + "_copy");
            }

            // Basic fields
            copyField(this::getManufacturer, targetItem::setManufacturer);
            copyField(this::getModelNumber, targetItem::setModelNumber);
            copyField(this::getUnitOfMeasure, targetItem::setUnitOfMeasure);
            copyField(this::getHandlingInstructions, targetItem::setHandlingInstructions);

            // Quantity starts at zero for copy
            targetItem.setCurrentQuantity(BigDecimal.ZERO);

            // Copy thresholds
            copyField(this::getMinimumStockLevel, targetItem::setMinimumStockLevel);
            copyField(this::getReorderQuantity, targetItem::setReorderQuantity);
            copyField(this::getMaximumStockLevel, targetItem::setMaximumStockLevel);

            // Cost information
            copyField(this::getUnitCost, targetItem::setUnitCost);
            copyField(this::getCurrency, targetItem::setCurrency);

            // Properties
            copyField(this::getIsConsumable, targetItem::setIsConsumable);
            copyField(this::getRequiresSpecialHandling, targetItem::setRequiresSpecialHandling);
            copyField(this::getTrackExpiration, targetItem::setTrackExpiration);
            copyField(this::getIsActive, targetItem::setIsActive);

            // Relations - conditional
            if (options.includesRelations()) {
                copyField(this::getStorage, targetItem::setStorage);
                copyField(this::getSupplier, targetItem::setSupplier);
                copyField(this::getResponsibleUser, targetItem::setResponsibleUser);
            }

            // Don't copy dates - expiration and last restocked are instance-specific
            // Don't copy lead time as it may change

            LOGGER.debug("Copied storage item {} with options: {}", getName(), options);
        }
    }

    // Business logic methods
    public boolean isLowStock() {
        if (minimumStockLevel == null) {
            return false;
        }
        return currentQuantity.compareTo(minimumStockLevel) <= 0;
    }

    public boolean isExpired() {
        if (!Boolean.TRUE.equals(trackExpiration) || expirationDate == null) {
            return false;
        }
        return expirationDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon(final int daysThreshold) {
        if (!Boolean.TRUE.equals(trackExpiration) || expirationDate == null) {
            return false;
        }
        final LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);
        return expirationDate.isBefore(thresholdDate) && !isExpired();
    }

    public BigDecimal getTotalValue() {
        if (unitCost == null) {
            return BigDecimal.ZERO;
        }
        return currentQuantity.multiply(unitCost);
    }

    public BigDecimal getStockPercentage() {
        if (maximumStockLevel == null || maximumStockLevel.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return currentQuantity.divide(maximumStockLevel, 2, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    protected void initializeDefaults() {
        if (currentQuantity == null) {
            currentQuantity = BigDecimal.ZERO;
        }
        if (currency == null) {
            currency = "EUR";
        }
        if (isConsumable == null) {
            isConsumable = Boolean.TRUE;
        }
        if (requiresSpecialHandling == null) {
            requiresSpecialHandling = Boolean.FALSE;
        }
        if (trackExpiration == null) {
            trackExpiration = Boolean.FALSE;
        }
        if (isActive == null) {
            isActive = Boolean.TRUE;
        }
    }

    // Getters and Setters with updateLastModified()
    public CStorageItemType getEntityType() {
        return entityType;
    }

    public void setEntityType(final CStorageItemType entityType) {
        this.entityType = entityType;
        updateLastModified();
    }

    public CStorage getStorage() {
        return storage;
    }

    public void setStorage(final CStorage storage) {
        this.storage = storage;
        updateLastModified();
    }

    public String getSku() {
        return sku;
    }

    public void setSku(final String sku) {
        this.sku = sku;
        updateLastModified();
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(final String barcode) {
        this.barcode = barcode;
        updateLastModified();
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(final String manufacturer) {
        this.manufacturer = manufacturer;
        updateLastModified();
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(final String modelNumber) {
        this.modelNumber = modelNumber;
        updateLastModified();
    }

    public BigDecimal getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(final BigDecimal currentQuantity) {
        this.currentQuantity = currentQuantity;
        updateLastModified();
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(final String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
        updateLastModified();
    }

    public BigDecimal getMinimumStockLevel() {
        return minimumStockLevel;
    }

    public void setMinimumStockLevel(final BigDecimal minimumStockLevel) {
        this.minimumStockLevel = minimumStockLevel;
        updateLastModified();
    }

    public BigDecimal getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(final BigDecimal reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
        updateLastModified();
    }

    public BigDecimal getMaximumStockLevel() {
        return maximumStockLevel;
    }

    public void setMaximumStockLevel(final BigDecimal maximumStockLevel) {
        this.maximumStockLevel = maximumStockLevel;
        updateLastModified();
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(final BigDecimal unitCost) {
        this.unitCost = unitCost;
        updateLastModified();
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
        updateLastModified();
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(final String batchNumber) {
        this.batchNumber = batchNumber;
        updateLastModified();
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(final LocalDate expirationDate) {
        this.expirationDate = expirationDate;
        updateLastModified();
    }

    public Boolean getTrackExpiration() {
        return trackExpiration;
    }

    public void setTrackExpiration(final Boolean trackExpiration) {
        this.trackExpiration = trackExpiration;
        updateLastModified();
    }

    public CProvider getSupplier() {
        return supplier;
    }

    public void setSupplier(final CProvider supplier) {
        this.supplier = supplier;
        updateLastModified();
    }

    public BigDecimal getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(final BigDecimal leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
        updateLastModified();
    }

    public Boolean getIsConsumable() {
        return isConsumable;
    }

    public void setIsConsumable(final Boolean isConsumable) {
        this.isConsumable = isConsumable;
        updateLastModified();
    }

    public Boolean getRequiresSpecialHandling() {
        return requiresSpecialHandling;
    }

    public void setRequiresSpecialHandling(final Boolean requiresSpecialHandling) {
        this.requiresSpecialHandling = requiresSpecialHandling;
        updateLastModified();
    }

    public String getHandlingInstructions() {
        return handlingInstructions;
    }

    public void setHandlingInstructions(final String handlingInstructions) {
        this.handlingInstructions = handlingInstructions;
        updateLastModified();
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(final Boolean isActive) {
        this.isActive = isActive;
        updateLastModified();
    }

    public LocalDate getLastRestockedDate() {
        return lastRestockedDate;
    }

    public void setLastRestockedDate(final LocalDate lastRestockedDate) {
        this.lastRestockedDate = lastRestockedDate;
        updateLastModified();
    }

    public CUser getResponsibleUser() {
        return responsibleUser;
    }

    public void setResponsibleUser(final CUser responsibleUser) {
        this.responsibleUser = responsibleUser;
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

    @Override
    public String toString() {
        return String.format("CStorageItem{id=%d, name=%s, sku=%s, quantity=%s %s, storage=%s}",
                getId(), getName(), sku, currentQuantity, unitOfMeasure,
                storage != null ? storage.getName() : "none");
    }
}
