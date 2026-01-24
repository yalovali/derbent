package tech.derbent.plm.products.product.domain;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.products.producttype.domain.CProductType;

@Entity
@Table (name = "\"cproduct\"")
@AttributeOverride (name = "id", column = @Column (name = "product_id"))
public class CProduct extends CProjectItem<CProduct> implements IHasStatusAndWorkflow<CProduct>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#6B8E23"; // X11 OliveDrab - product entities (darker)
	public static final String DEFAULT_ICON = "vaadin:package";
	public static final String ENTITY_TITLE_PLURAL = "Products";
	public static final String ENTITY_TITLE_SINGULAR = "Product";
	public static final String VIEW_NAME = "Products View";
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "product_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Attachments for this product", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "product_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this product", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Product Type", required = false, readOnly = false, description = "Type category of the product", hidden = false,
			dataProviderBean = "CProductTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CProductType entityType;
	@Column (nullable = true, length = 100)
	@AMetaData (displayName = "Product Code", required = false, readOnly = false, description = "Unique product code or SKU", hidden = false)
	private String productCode;

	/** Default constructor for JPA. */
	public CProduct() {
		super();
		initializeDefaults();
	}

	public CProduct(final String name, final CProject<?> project) {
		super(CProduct.class, name, project);
		initializeDefaults();
	}

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	@Override
	public Set<CComment> getComments() { return comments; }

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	public String getProductCode() { return productCode; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CProductType.class, "Type entity must be an instance of CProductType");
		Check.notNull(getProject(), "Project must be set before assigning product type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning product type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning product type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match product project company id " + getProject().getCompany().getId());
		entityType = (CProductType) typeEntity;
		updateLastModified();
	}

	public void setProductCode(final String productCode) {
		this.productCode = productCode;
		updateLastModified();
	}
}
