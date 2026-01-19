package tech.derbent.plm.components.component.domain;

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
import tech.derbent.plm.components.componenttype.domain.CProjectComponentType;

@Entity
@Table (name = "\"cprojectcomponent\"")
@AttributeOverride (name = "id", column = @Column (name = "projectcomponent_id"))
public class CProjectComponent extends CProjectItem<CProjectComponent>
		implements IHasStatusAndWorkflow<CProjectComponent>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#808000"; // X11 Olive - component parts (darker)
	public static final String DEFAULT_ICON = "vaadin:cogs";
	public static final String ENTITY_TITLE_PLURAL = "Components";
	public static final String ENTITY_TITLE_SINGULAR = "Component";
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectComponent.class);
	public static final String VIEW_NAME = "Components View";
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "projectcomponent_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this entity", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "projectcomponent_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this entity", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (nullable = true, length = 100)
	@AMetaData (
			displayName = "Component Code", required = false, readOnly = false, description = "Unique component code or identifier", hidden = false
	)
	private String componentCode;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Component Type", required = false, readOnly = false, description = "Type category of the component", hidden = false,
			dataProviderBean = "CComponentTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CProjectComponentType entityType;

	public CProjectComponent() {
		super();
		initializeDefaults();
	}

	public CProjectComponent(final String name, final CProject<?> project) {
		super(CProjectComponent.class, name, project);
		initializeDefaults();
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

	public String getComponentCode() { return componentCode; }

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

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

	public void setComponentCode(final String componentCode) {
		this.componentCode = componentCode;
		updateLastModified();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CProjectComponentType.class, "Type entity must be an instance of CComponentType");
		Check.notNull(getProject(), "Project must be set before assigning component type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning component type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning component type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match component project company id " + getProject().getCompany().getId());
		entityType = (CProjectComponentType) typeEntity;
		updateLastModified();
	}
}
