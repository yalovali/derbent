package tech.derbent.plm.deliverables.deliverable.domain;

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
import tech.derbent.plm.deliverables.deliverabletype.domain.CDeliverableType;

@Entity
@Table (name = "\"cdeliverable\"")
@AttributeOverride (name = "id", column = @Column (name = "deliverable_id"))
public class CDeliverable extends CProjectItem<CDeliverable> implements IHasStatusAndWorkflow<CDeliverable>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#BC8F8F"; // X11 RosyBrown - deliverable items (darker)
	public static final String DEFAULT_ICON = "vaadin:clipboard-check";
	public static final String ENTITY_TITLE_PLURAL = "Deliverables";
	public static final String ENTITY_TITLE_SINGULAR = "Deliverable";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CDeliverable.class);
	public static final String VIEW_NAME = "Deliverable View";
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "deliverable_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Attachments for this deliverable", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "deliverable_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this deliverable", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Deliverable Type", required = false, readOnly = false, description = "Type category of the deliverable", hidden = false,
			dataProviderBean = "CDeliverableTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CDeliverableType entityType;

	/** Default constructor for JPA. */
										/** Default constructor for JPA. */
	protected CDeliverable() {}

	public CDeliverable(final String name, final CProject<?> project) {
		super(CDeliverable.class, name, project);
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
		Check.instanceOf(typeEntity, CDeliverableType.class, "Type entity must be an instance of CDeliverableType");
		Check.notNull(getProject(), "Project must be set before assigning deliverable type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning deliverable type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning deliverable type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match deliverable project company id " + getProject().getCompany().getId());
		entityType = (CDeliverableType) typeEntity;
		updateLastModified();
	}
}
