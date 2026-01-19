package tech.derbent.plm.assets.asset.domain;

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
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.services.copy.CCloneOptions;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.assets.assettype.domain.CAssetType;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.providers.provider.domain.CProvider;

@Entity
@Table (name = "\"casset\"")
@AttributeOverride (name = "id", column = @Column (name = "asset_id"))
public class CAsset extends CProjectItem<CAsset> implements IHasStatusAndWorkflow<CAsset>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#708090"; // X11 SlateGray - owned items (darker)
	public static final String DEFAULT_ICON = "vaadin:briefcase";
	public static final String ENTITY_TITLE_PLURAL = "Assets";
	public static final String ENTITY_TITLE_SINGULAR = "Asset";
	private static final Logger LOGGER = LoggerFactory.getLogger(CAsset.class);
	public static final String VIEW_NAME = "Asset View";
	
	// Basic asset information
	@Column (nullable = true, length = 255)
	@Size (max = 255)
	@AMetaData (
			displayName = "Brand", required = false, readOnly = false, description = "Manufacturer or brand of the asset", hidden = false,
			order = 20, maxLength = 255
	)
	private String brand;
	
	@Column (nullable = true, length = 255)
	@Size (max = 255)
	@AMetaData (
			displayName = "Model", required = false, readOnly = false, description = "Model name or number of the asset", hidden = false,
			order = 30, maxLength = 255
	)
	private String model;
	
	@Column (nullable = true, length = 255)
	@Size (max = 255)
	@AMetaData (
			displayName = "Serial Number", required = false, readOnly = false, description = "Unique serial number from manufacturer",
			hidden = false, order = 40, maxLength = 255
	)
	private String serialNumber;
	
	@Column (nullable = true, length = 255)
	@Size (max = 255)
	@AMetaData (
			displayName = "Inventory Number", required = false, readOnly = false,
			description = "Internal inventory tracking number", hidden = false, order = 50, maxLength = 255
	)
	private String inventoryNumber;
	
	@Column (nullable = true, length = 500)
	@Size (max = 500)
	@AMetaData (
			displayName = "Location", required = false, readOnly = false,
			description = "Physical location of the asset (building, room, etc.)", hidden = false, order = 60, maxLength = 500
	)
	private String location;
	
	// Relationships
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Asset Type", required = false, readOnly = false, description = "Type category of the asset", hidden = false,
			order = 10, dataProviderBean = "CAssetTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CAssetType entityType;
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "provider_id", nullable = true)
	@AMetaData (
			displayName = "Provider", required = false, readOnly = false, description = "Supplier or vendor of the asset", hidden = false,
			order = 70, dataProviderBean = "CProviderService"
	)
	private CProvider provider;
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "parent_asset_id", nullable = true)
	@AMetaData (
			displayName = "Parent Asset", required = false, readOnly = false,
			description = "Parent asset if this is a component of another asset", hidden = false, order = 80,
			dataProviderBean = "CAssetService"
	)
	private CAsset parentAsset;
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "user_id", nullable = true)
	@AMetaData (
			displayName = "User", required = false, readOnly = false, description = "User currently using or responsible for the asset",
			hidden = false, order = 90, dataProviderBean = "CUserService"
	)
	private CUser user;
	
	// Dates
	@Column (name = "installation_date", nullable = true)
	@AMetaData (
			displayName = "Installation Date", required = false, readOnly = false, description = "Date when asset was installed or deployed",
			hidden = false, order = 100
	)
	private LocalDate installationDate;
	
	@Column (name = "decommissioning_date", nullable = true)
	@AMetaData (
			displayName = "Decommissioning Date", required = false, readOnly = false,
			description = "Date when asset was decommissioned or retired", hidden = false, order = 110
	)
	private LocalDate decommissioningDate;
	
	// Financial information
	@Column (name = "purchase_value", nullable = true, precision = 19, scale = 2)
	@AMetaData (
			displayName = "Purchase Value", required = false, readOnly = false, description = "Original purchase value of the asset",
			hidden = false, order = 120
	)
	private BigDecimal purchaseValue;
	
	@Column (name = "untaxed_amount", nullable = true, precision = 19, scale = 2)
	@AMetaData (
			displayName = "Untaxed Amount", required = false, readOnly = false, description = "Purchase amount before taxes", hidden = false,
			order = 130
	)
	private BigDecimal untaxedAmount;
	
	@Column (name = "full_amount", nullable = true, precision = 19, scale = 2)
	@AMetaData (
			displayName = "Full Amount", required = false, readOnly = false, description = "Total purchase amount including all taxes and fees",
			hidden = false, order = 140
	)
	private BigDecimal fullAmount;
	
	// Warranty and depreciation
	@Column (name = "warranty_duration", nullable = true)
	@AMetaData (
			displayName = "Warranty Duration (months)", required = false, readOnly = false,
			description = "Duration of warranty coverage in months", hidden = false, order = 150
	)
	private Integer warrantyDuration;
	
	@Column (name = "warranty_end_date", nullable = true)
	@AMetaData (
			displayName = "Warranty End Date", required = false, readOnly = false, description = "Date when warranty expires", hidden = false,
			order = 160
	)
	private LocalDate warrantyEndDate;
	
	@Column (name = "depreciation_period", nullable = true)
	@AMetaData (
			displayName = "Depreciation Period (years)", required = false, readOnly = false,
			description = "Expected depreciation period in years", hidden = false, order = 170
	)
	private Integer depreciationPeriod;
	
	@Column (name = "need_insurance", nullable = true)
	@AMetaData (
			displayName = "Need Insurance", required = false, readOnly = false, description = "Whether this asset requires insurance coverage",
			hidden = false, order = 180
	)
	private Boolean needInsurance;
	
	// Collections
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "asset_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Attachments for this asset", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "asset_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this asset", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();

	/** Default constructor for JPA. */
	public CAsset() {
		super();
		initializeDefaults();
	}

	public CAsset(final String name, final CProject<?> project) {
		super(CAsset.class, name, project);
		initializeDefaults();
	}

	/** Copies asset fields to target using copyField pattern. Override to add more fields. Always call super.copyEntityTo() first!
	 * @param target        The target entity
	 * @param serviceTarget The service for target entity
	 * @param options       Clone options */
	@Override
	protected void copyEntityTo(final CEntityDB<?> target, @SuppressWarnings ("rawtypes") final CAbstractService serviceTarget,
			final CCloneOptions options) {
		// STEP 1: ALWAYS call parent first
		super.copyEntityTo(target, serviceTarget, options);
		
		// STEP 2: Type-check target
		if (target instanceof final CAsset targetAsset) {
			// STEP 3: Copy basic fields (always)
			copyField(this::getBrand, targetAsset::setBrand);
			copyField(this::getModel, targetAsset::setModel);
			copyField(this::getSerialNumber, targetAsset::setSerialNumber);
			copyField(this::getInventoryNumber, targetAsset::setInventoryNumber);
			copyField(this::getLocation, targetAsset::setLocation);
			
			// STEP 4: Handle unique fields (make unique!)
			if (this.getSerialNumber() != null && !this.getSerialNumber().isEmpty()) {
				targetAsset.setSerialNumber(this.getSerialNumber() + "_copy");
			}
			if (this.getInventoryNumber() != null && !this.getInventoryNumber().isEmpty()) {
				targetAsset.setInventoryNumber(this.getInventoryNumber() + "_copy");
			}
			
			// STEP 5: Handle dates (conditional)
			if (!options.isResetDates()) {
				copyField(this::getInstallationDate, targetAsset::setInstallationDate);
				copyField(this::getDecommissioningDate, targetAsset::setDecommissioningDate);
				copyField(this::getWarrantyEndDate, targetAsset::setWarrantyEndDate);
			}
			
			// STEP 6: Handle relations (conditional)
			if (options.includesRelations()) {
				copyField(this::getProvider, targetAsset::setProvider);
				copyField(this::getParentAsset, targetAsset::setParentAsset);
				copyField(this::getUser, targetAsset::setUser);
			}
			
			// STEP 7: Copy financial fields
			copyField(this::getPurchaseValue, targetAsset::setPurchaseValue);
			copyField(this::getUntaxedAmount, targetAsset::setUntaxedAmount);
			copyField(this::getFullAmount, targetAsset::setFullAmount);
			
			// STEP 8: Copy warranty and depreciation info
			copyField(this::getWarrantyDuration, targetAsset::setWarrantyDuration);
			copyField(this::getDepreciationPeriod, targetAsset::setDepreciationPeriod);
			copyField(this::getNeedInsurance, targetAsset::setNeedInsurance);
			
			// STEP 9: Log for debugging
			LOGGER.debug("Copied asset '{}' with options: {}", getName(), options);
		}
	}

	// Getters and Setters
	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	public String getBrand() { return brand; }

	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

	public LocalDate getDecommissioningDate() { return decommissioningDate; }

	public Integer getDepreciationPeriod() { return depreciationPeriod; }

	@Override
	public CAssetType getEntityType() { return entityType; }

	public BigDecimal getFullAmount() { return fullAmount; }

	public LocalDate getInstallationDate() { return installationDate; }

	public String getInventoryNumber() { return inventoryNumber; }

	public String getLocation() { return location; }

	public String getModel() { return model; }

	public Boolean getNeedInsurance() { return needInsurance; }

	public CAsset getParentAsset() { return parentAsset; }

	public CProvider getProvider() { return provider; }

	public BigDecimal getPurchaseValue() { return purchaseValue; }

	public String getSerialNumber() { return serialNumber; }

	public BigDecimal getUntaxedAmount() { return untaxedAmount; }

	public CUser getUser() { return user; }

	public Integer getWarrantyDuration() { return warrantyDuration; }

	public LocalDate getWarrantyEndDate() { return warrantyEndDate; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (needInsurance == null) {
			needInsurance = false;
		}
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setBrand(final String brand) {
		this.brand = brand;
		updateLastModified();
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setDecommissioningDate(final LocalDate decommissioningDate) {
		this.decommissioningDate = decommissioningDate;
		updateLastModified();
	}

	public void setDepreciationPeriod(final Integer depreciationPeriod) {
		this.depreciationPeriod = depreciationPeriod;
		updateLastModified();
	}

	@Override
	public void setEntityType(final CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CAssetType.class, "Type entity must be an instance of CAssetType");
		Check.notNull(getProject(), "Project must be set before assigning asset type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning asset type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning asset type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match asset project company id " + getProject().getCompany().getId());
		entityType = (CAssetType) typeEntity;
		updateLastModified();
	}

	public void setFullAmount(final BigDecimal fullAmount) {
		this.fullAmount = fullAmount;
		updateLastModified();
	}

	public void setInstallationDate(final LocalDate installationDate) {
		this.installationDate = installationDate;
		updateLastModified();
	}

	public void setInventoryNumber(final String inventoryNumber) {
		this.inventoryNumber = inventoryNumber;
		updateLastModified();
	}

	public void setLocation(final String location) {
		this.location = location;
		updateLastModified();
	}

	public void setModel(final String model) {
		this.model = model;
		updateLastModified();
	}

	public void setNeedInsurance(final Boolean needInsurance) {
		this.needInsurance = needInsurance;
		updateLastModified();
	}

	public void setParentAsset(final CAsset parentAsset) {
		this.parentAsset = parentAsset;
		updateLastModified();
	}

	public void setProvider(final CProvider provider) {
		this.provider = provider;
		updateLastModified();
	}

	public void setPurchaseValue(final BigDecimal purchaseValue) {
		this.purchaseValue = purchaseValue;
		updateLastModified();
	}

	public void setSerialNumber(final String serialNumber) {
		this.serialNumber = serialNumber;
		updateLastModified();
	}

	public void setUntaxedAmount(final BigDecimal untaxedAmount) {
		this.untaxedAmount = untaxedAmount;
		updateLastModified();
	}

	public void setUser(final CUser user) {
		this.user = user;
		updateLastModified();
	}

	public void setWarrantyDuration(final Integer warrantyDuration) {
		this.warrantyDuration = warrantyDuration;
		updateLastModified();
	}

	public void setWarrantyEndDate(final LocalDate warrantyEndDate) {
		this.warrantyEndDate = warrantyEndDate;
		updateLastModified();
	}
}
