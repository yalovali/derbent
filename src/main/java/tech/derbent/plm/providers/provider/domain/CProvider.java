package tech.derbent.plm.providers.provider.domain;

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
import tech.derbent.plm.providers.providertype.domain.CProviderType;

@Entity
@Table (name = "cprovider")
@AttributeOverride (name = "id", column = @Column (name = "provider_id"))
public class CProvider extends CProjectItem<CProvider> implements IHasStatusAndWorkflow<CProvider>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#696969"; // X11 DimGray - external providers (darker)
	public static final String DEFAULT_ICON = "vaadin:handshake";
	public static final String ENTITY_TITLE_PLURAL = "Providers";
	public static final String ENTITY_TITLE_SINGULAR = "Provider";
	public static final String VIEW_NAME = "Provider View";
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "provider_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Attachments for this provider", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "provider_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this provider", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Provider Type", required = false, readOnly = false, description = "Type category of the provider", hidden = false,
			dataProviderBean = "CProviderTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CProviderType entityType;

	/** Default constructor for JPA. */
	public CProvider() {
		super();
		initializeDefaults();
	}

	public CProvider(final String name, final CProject<?> project) {
		super(CProvider.class, name, project);
		initializeDefaults();
	}

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	@Override
	public Set<CComment> getComments() { return comments; }

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

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
		Check.instanceOf(typeEntity, CProviderType.class, "Type entity must be an instance of CProviderType");
		Check.notNull(getProject(), "Project must be set before assigning provider type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning provider type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning provider type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match provider project company id " + getProject().getCompany().getId());
		entityType = (CProviderType) typeEntity;
		updateLastModified();
	}
}
