package tech.derbent.app.products.productversion.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.utils.Check;
import tech.derbent.app.products.product.domain.CProduct;
import tech.derbent.app.products.productversiontype.domain.CProductVersionType;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"cproductversion\"")
@AttributeOverride (name = "id", column = @Column (name = "productversion_id"))
public class CProductVersion extends CProjectItem<CProductVersion> implements IHasStatusAndWorkflow<CProductVersion> {

	public static final String DEFAULT_COLOR = "#0097A7";
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Product Versions";
	public static final String ENTITY_TITLE_SINGULAR = "Product Version";
	public static final String VIEW_NAME = "Product Versions View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Version Type", required = false, readOnly = false, description = "Type category of the version", hidden = false,
			dataProviderBean = "CProductVersionTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CProductVersionType entityType;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "product_id", nullable = false)
	@AMetaData (
			displayName = "Product", required = true, readOnly = false, description = "Parent product", hidden = false,
			dataProviderBean = "CProductService"
	)
	private CProduct product;
	@Column (nullable = true, length = 50)
	@AMetaData (displayName = "Version Number", required = false, readOnly = false, description = "Version identifier (e.g., 1.0.0)", hidden = false)
	private String versionNumber;

	public CProductVersion() {
		super();
		initializeDefaults();
	}

	public CProductVersion(final String name, final CProject project) {
		super(CProductVersion.class, name, project);
		initializeDefaults();
	}

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	public CProduct getProduct() { return product; }

	public String getVersionNumber() { return versionNumber; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	public void initializeAllFields() {
		if (getProject() != null) {
			getProject().getName();
		}
		if (product != null) {
			product.getName();
		}
		if (getAssignedTo() != null) {
			getAssignedTo().getLogin();
		}
		if (getCreatedBy() != null) {
			getCreatedBy().getLogin();
		}
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.instanceOf(typeEntity, CProductVersionType.class, "Type entity must be an instance of CProductVersionType");
		entityType = (CProductVersionType) typeEntity;
		updateLastModified();
	}

	public void setProduct(final CProduct product) {
		this.product = product;
		updateLastModified();
	}

	public void setVersionNumber(final String versionNumber) {
		this.versionNumber = versionNumber;
		updateLastModified();
	}
}
