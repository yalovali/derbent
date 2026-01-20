package tech.derbent.plm.storage.storageitem.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.domains.CTypeEntity;
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
import tech.derbent.plm.providers.provider.domain.CProvider;
import tech.derbent.plm.storage.storage.domain.CStorage;

@Entity
@Table(name = "cstorageitem")
@AttributeOverride(name = "id", column = @Column(name = "storageitem_id"))
public class CStorageItem extends CProjectItem<CStorageItem> implements IHasStatusAndWorkflow<CStorageItem>, IHasAttachments, IHasComments {

    public static final String DEFAULT_COLOR = "#008B8B";
    public static final String DEFAULT_ICON = "vaadin:archive";
    public static final String ENTITY_TITLE_PLURAL = "Storage Items";
    public static final String ENTITY_TITLE_SINGULAR = "Storage Item";
    private static final Logger LOGGER = LoggerFactory.getLogger(CStorageItem.class);
    public static final String VIEW_NAME = "Storage Item View";

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "storageitem_id")
    @AMetaData(displayName = "Attachments", required = false, readOnly = false, description = "Attachments for this item", hidden = false,
            dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent")
    private Set<CAttachment> attachments = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "storageitem_id")
    @AMetaData(displayName = "Comments", required = false, readOnly = false, description = "Comments for this item", hidden = false,
            dataProviderBean = "CCommentService", createComponentMethod = "createComponent")
    private Set<CComment> comments = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entitytype_id")
    @AMetaData(displayName = "Item Type", required = false, description = "Type of storage item",
            dataProviderBean = "CStorageItemTypeService", setBackgroundFromColor = true, useIcon = true)
    private CStorageItemType entityType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storage_id", nullable = false)
    @AMetaData(displayName = "Storage", required = true, description = "Storage location", dataProviderBean = "CStorageService")
    private CStorage storage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provider_id")
    @AMetaData(displayName = "Supplier", required = false, description = "Supplier/provider", dataProviderBean = "CProviderService")
    private CProvider provider;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsible_user_id")
    @AMetaData(displayName = "Responsible User", required = false, description = "User managing this item", dataProviderBean = "CUserService")
    private CUser responsibleUser;

    @Column(length = 100)
    @Size(max = 100)
    @AMetaData(displayName = "SKU", required = false, description = "Stock Keeping Unit", maxLength = 100)
    private String sku;

    @Column(length = 100)
    @Size(max = 100)
    @AMetaData(displayName = "Barcode", required = false, description = "Barcode", maxLength = 100)
    private String barcode;

    @Column(length = 255)
    @Size(max = 255)
    @AMetaData(displayName = "Manufacturer", required = false, description = "Manufacturer", maxLength = 255)
    private String manufacturer;

    @Column(name = "model_number", length = 255)
    @Size(max = 255)
    @AMetaData(displayName = "Model Number", required = false, description = "Model number", maxLength = 255)
    private String modelNumber;

    @Column(name = "current_quantity", precision = 19, scale = 2, nullable = false)
    @AMetaData(displayName = "Current Quantity", required = true, description = "Current stock level")
    private BigDecimal currentQuantity = BigDecimal.ZERO;

    @Column(name = "unit_of_measure", length = 50)
    @Size(max = 50)
    @AMetaData(displayName = "Unit of Measure", required = false, description = "Unit of measure", maxLength = 50)
    private String unitOfMeasure;

    @Column(name = "minimum_stock_level", precision = 19, scale = 2)
    @AMetaData(displayName = "Minimum Stock Level", required = false, description = "Minimum stock level before reorder")
    private BigDecimal minimumStockLevel;

    @Column(name = "reorder_quantity", precision = 19, scale = 2)
    @AMetaData(displayName = "Reorder Quantity", required = false, description = "Quantity to reorder")
    private BigDecimal reorderQuantity;

    @Column(name = "maximum_stock_level", precision = 19, scale = 2)
    @AMetaData(displayName = "Maximum Stock Level", required = false, description = "Maximum stock capacity")
    private BigDecimal maximumStockLevel;

    @Column(name = "unit_cost", precision = 19, scale = 2)
    @AMetaData(displayName = "Unit Cost", required = false, description = "Unit cost")
    private BigDecimal unitCost;

    @Column(name = "currency", length = 10)
    @Size(max = 10)
    @AMetaData(displayName = "Currency", required = false, description = "Currency code", maxLength = 10)
    private String currency;

    @Column(name = "batch_number", length = 100)
    @Size(max = 100)
    @AMetaData(displayName = "Batch Number", required = false, description = "Batch/lot number", maxLength = 100)
    private String batchNumber;

    @Column(name = "expiration_date")
    @AMetaData(displayName = "Expiration Date", required = false, description = "Expiration date")
    private LocalDate expirationDate;

    @Column(name = "track_expiration")
    @AMetaData(displayName = "Track Expiration", required = false, description = "Track expiration flag")
    private Boolean trackExpiration = Boolean.FALSE;

    @Column(name = "lead_time_days", precision = 19, scale = 2)
    @AMetaData(displayName = "Lead Time (days)", required = false, description = "Supplier lead time in days")
    private BigDecimal leadTimeDays;

    @Column(name = "is_consumable")
    @AMetaData(displayName = "Consumable", required = false, description = "Is consumable flag")
    private Boolean isConsumable = Boolean.TRUE;

    @Column(name = "requires_special_handling")
    @AMetaData(displayName = "Requires Special Handling", required = false, description = "Special handling needed")
    private Boolean requiresSpecialHandling = Boolean.FALSE;

    @Column(name = "handling_instructions", length = 500)
    @Size(max = 500)
    @AMetaData(displayName = "Handling Instructions", required = false, description = "Special handling instructions", maxLength = 500)
    private String handlingInstructions;

    @Column(name = "last_restocked_date")
    @AMetaData(displayName = "Last Restocked Date", required = false, description = "Last restocked date")
    private LocalDate lastRestockedDate;

    public CStorageItem() {
        super();
    }

    public CStorageItem(final String name, final CProject<?> project) {
        super(CStorageItem.class, name, project);
    }

    public CStorageItem(final String name, final CProject<?> project, final CStorage storage) {
        super(CStorageItem.class, name, project);
        this.storage = storage;
    }

    public boolean isLowStock() {
        return minimumStockLevel != null && currentQuantity != null && currentQuantity.compareTo(minimumStockLevel) <= 0;
    }

    public boolean isExpired() {
        return Boolean.TRUE.equals(trackExpiration) && expirationDate != null && expirationDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon(final int daysThreshold) {
        if (!Boolean.TRUE.equals(trackExpiration) || expirationDate == null) {
            return false;
        }
        return !expirationDate.isAfter(LocalDate.now().plusDays(daysThreshold));
    }

    public BigDecimal getTotalValue() {
        if (unitCost == null || currentQuantity == null) {
            return BigDecimal.ZERO;
        }
        return unitCost.multiply(currentQuantity);
    }

    public BigDecimal getStockPercentage() {
        if (maximumStockLevel == null || maximumStockLevel.signum() == 0 || currentQuantity == null) {
            return BigDecimal.ZERO;
        }
        return currentQuantity.multiply(BigDecimal.valueOf(100)).divide(maximumStockLevel, 2, RoundingMode.HALF_UP);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void copyEntityTo(final CEntityDB<?> target, final CAbstractService serviceTarget, final CCloneOptions options) {
        super.copyEntityTo(target, serviceTarget, options);
        if (target instanceof CStorageItem targetItem) {
            copyField(this::getEntityType, targetItem::setEntityType);
            copyField(this::getStorage, targetItem::setStorage);
            copyField(this::getProvider, targetItem::setProvider);
            copyField(this::getResponsibleUser, targetItem::setResponsibleUser);
            copyField(this::getSku, targetItem::setSku);
            copyField(this::getBarcode, targetItem::setBarcode);
            copyField(this::getManufacturer, targetItem::setManufacturer);
            copyField(this::getModelNumber, targetItem::setModelNumber);
            copyField(this::getCurrentQuantity, targetItem::setCurrentQuantity);
            copyField(this::getUnitOfMeasure, targetItem::setUnitOfMeasure);
            copyField(this::getMinimumStockLevel, targetItem::setMinimumStockLevel);
            copyField(this::getReorderQuantity, targetItem::setReorderQuantity);
            copyField(this::getMaximumStockLevel, targetItem::setMaximumStockLevel);
            copyField(this::getUnitCost, targetItem::setUnitCost);
            copyField(this::getCurrency, targetItem::setCurrency);
            copyField(this::getBatchNumber, targetItem::setBatchNumber);
            if (!options.isResetDates()) {
                copyField(this::getExpirationDate, targetItem::setExpirationDate);
                copyField(this::getLastRestockedDate, targetItem::setLastRestockedDate);
            }
            copyField(this::getTrackExpiration, targetItem::setTrackExpiration);
            copyField(this::getLeadTimeDays, targetItem::setLeadTimeDays);
            copyField(this::getIsConsumable, targetItem::setIsConsumable);
            copyField(this::getRequiresSpecialHandling, targetItem::setRequiresSpecialHandling);
            copyField(this::getHandlingInstructions, targetItem::setHandlingInstructions);
        }
    }

    public CStorage getStorage() { return storage; }
    public void setStorage(final CStorage storage) { this.storage = storage; }
    public CProvider getProvider() { return provider; }
    public void setProvider(final CProvider provider) { this.provider = provider; }
    public CUser getResponsibleUser() { return responsibleUser; }
    public void setResponsibleUser(final CUser responsibleUser) { this.responsibleUser = responsibleUser; }
    public String getSku() { return sku; }
    public void setSku(final String sku) { this.sku = sku; }
    public String getBarcode() { return barcode; }
    public void setBarcode(final String barcode) { this.barcode = barcode; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(final String manufacturer) { this.manufacturer = manufacturer; }
    public String getModelNumber() { return modelNumber; }
    public void setModelNumber(final String modelNumber) { this.modelNumber = modelNumber; }
    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(final BigDecimal currentQuantity) { this.currentQuantity = currentQuantity; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(final String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public BigDecimal getMinimumStockLevel() { return minimumStockLevel; }
    public void setMinimumStockLevel(final BigDecimal minimumStockLevel) { this.minimumStockLevel = minimumStockLevel; }
    public BigDecimal getReorderQuantity() { return reorderQuantity; }
    public void setReorderQuantity(final BigDecimal reorderQuantity) { this.reorderQuantity = reorderQuantity; }
    public BigDecimal getMaximumStockLevel() { return maximumStockLevel; }
    public void setMaximumStockLevel(final BigDecimal maximumStockLevel) { this.maximumStockLevel = maximumStockLevel; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(final BigDecimal unitCost) { this.unitCost = unitCost; }
    public String getCurrency() { return currency; }
    public void setCurrency(final String currency) { this.currency = currency; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(final String batchNumber) { this.batchNumber = batchNumber; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(final LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public Boolean getTrackExpiration() { return trackExpiration; }
    public void setTrackExpiration(final Boolean trackExpiration) { this.trackExpiration = trackExpiration; }
    public BigDecimal getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(final BigDecimal leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    public Boolean getIsConsumable() { return isConsumable; }
    public void setIsConsumable(final Boolean isConsumable) { this.isConsumable = isConsumable; }
    public Boolean getRequiresSpecialHandling() { return requiresSpecialHandling; }
    public void setRequiresSpecialHandling(final Boolean requiresSpecialHandling) { this.requiresSpecialHandling = requiresSpecialHandling; }
    public String getHandlingInstructions() { return handlingInstructions; }
    public void setHandlingInstructions(final String handlingInstructions) { this.handlingInstructions = handlingInstructions; }
    public LocalDate getLastRestockedDate() { return lastRestockedDate; }
    public void setLastRestockedDate(final LocalDate lastRestockedDate) { this.lastRestockedDate = lastRestockedDate; }

    @Override
    public CWorkflowEntity getWorkflow() { return entityType != null ? entityType.getWorkflow() : null; }

    @Override
    public CStorageItemType getEntityType() { return entityType; }

    @Override
    public void setEntityType(final CTypeEntity<?> typeEntity) {
        Check.notNull(typeEntity, "Type entity must not be null");
        Check.instanceOf(typeEntity, CStorageItemType.class, "Type entity must be an instance of CStorageItemType");
        Check.notNull(getProject(), "Project must be set before assigning storage item type");
        Check.notNull(getProject().getCompany(), "Project company must be set before assigning storage item type");
        Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning storage item type");
        Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
                + typeEntity.getCompany().getId() + " does not match storage item project company id "
                + getProject().getCompany().getId());
        entityType = (CStorageItemType) typeEntity;
        updateLastModified();
    }

    @Override
    public Set<CAttachment> getAttachments() { return attachments; }

    @Override
    public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

    @Override
    public Set<CComment> getComments() { return comments; }

    @Override
    public void setComments(final Set<CComment> comments) { this.comments = comments; }
}
