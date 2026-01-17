package tech.derbent.app.products.productversion.domain;

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
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;
import tech.derbent.app.products.product.domain.CProduct;
import tech.derbent.app.products.productversiontype.domain.CProductVersionType;

@Entity
@Table (name = "\"cproductversion\"")
@AttributeOverride (name = "id", column = @Column (name = "productversion_id"))
public class CProductVersion extends CProjectItem<CProductVersion> implements IHasStatusAndWorkflow<CProductVersion>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#6B8E23"; // X11 OliveDrab - product versions (darker)
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
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "productversion_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this entity", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "productversion_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this entity", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();

	public CProductVersion() {
		super();
		initializeDefaults();
	}

	public CProductVersion(final String name, final CProject project) {
		super(CProductVersion.class, name, project);
		initializeDefaults();
	}

	@Override
	public CProductVersion createClone(final tech.derbent.api.interfaces.CCloneOptions options) throws Exception {
		final CProductVersion clone = super.createClone(options);
		clone.versionNumber = versionNumber;
		clone.entityType = entityType;
		if (!options.isResetAssignments() && product != null) {
			clone.product = product;
		}
		if (options.includesComments() && comments != null && !comments.isEmpty()) {
			clone.comments = new HashSet<>();
			for (final CComment comment : comments) {
				try {
					final CComment commentClone = comment.createClone(options);
					clone.comments.add(commentClone);
				} catch (final Exception e) {
					// Silently skip failed comment clones
				}
			}
		}
		if (options.includesAttachments() && attachments != null && !attachments.isEmpty()) {
			clone.attachments = new HashSet<>();
			for (final CAttachment attachment : attachments) {
				try {
					final CAttachment attachmentClone = attachment.createClone(options);
					clone.attachments.add(attachmentClone);
				} catch (final Exception e) {
					// Silently skip failed attachment clones
				}
			}
		}
		return clone;
	}

	// IHasAttachments interface methods
	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	// IHasComments interface methods
	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
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
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(final Set<CComment> comments) {
		this.comments = comments;
		updateLastModified();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CProductVersionType.class, "Type entity must be an instance of CProductVersionType");
		Check.notNull(getProject(), "Project must be set before assigning product version type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning product version type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning product version type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match product version project company id " + getProject().getCompany().getId());
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
