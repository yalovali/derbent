package tech.derbent.app.products.product.domain;

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
import tech.derbent.app.products.producttype.domain.CProductType;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"cproduct\"")
@AttributeOverride (name = "id", column = @Column (name = "product_id"))
public class CProduct extends CProjectItem<CProduct> implements IHasStatusAndWorkflow<CProduct> {

	public static final String DEFAULT_COLOR = "#00BCD4";
	public static final String DEFAULT_ICON = "vaadin:package";
	public static final String VIEW_NAME = "Products View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Product Type", required = false, readOnly = false, description = "Type category of the product", hidden = false, order = 2,
			dataProviderBean = "CProductTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CProductType entityType;
	@Column (nullable = true, length = 100)
	@AMetaData (
			displayName = "Product Code", required = false, readOnly = false, description = "Unique product code or SKU", hidden = false, order = 3
	)
	private String productCode;

	/** Default constructor for JPA. */
	public CProduct() {
		super();
		initializeDefaults();
	}

	public CProduct(final String name, final CProject project) {
		super(CProduct.class, name, project);
		initializeDefaults();
	}

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	public String getProductCode() { return productCode; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.instanceOf(typeEntity, CProductType.class, "Type entity must be an instance of CProductType");
		entityType = (CProductType) typeEntity;
		updateLastModified();
	}

	public void setProductCode(final String productCode) {
		this.productCode = productCode;
		updateLastModified();
	}
}
