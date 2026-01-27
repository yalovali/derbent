package tech.derbent.plm.components.componentversion.domain;

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
import tech.derbent.plm.components.component.domain.CProjectComponent;
import tech.derbent.plm.components.componentversiontype.domain.CProjectComponentVersionType;

@Entity
@Table (name = "\"cprojectcomponentversion\"")
@AttributeOverride (name = "id", column = @Column (name = "projectcomponentversion_id"))
public class CProjectComponentVersion extends CProjectItem<CProjectComponentVersion>
		implements IHasStatusAndWorkflow<CProjectComponentVersion>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#808000"; // X11 Olive - component versions (darker)
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Component Versions";
	public static final String ENTITY_TITLE_SINGULAR = "Component Version";
	public static final String VIEW_NAME = "Component Versions View";
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "projectcomponentversion_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this entity", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "projectcomponentversion_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this entity", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Version Type", required = false, readOnly = false, description = "Type category of the version", hidden = false,
			dataProviderBean = "CProjectComponentVersionTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CProjectComponentVersionType entityType;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "projectcomponent_id", nullable = false)
	@AMetaData (
			displayName = "Component", required = true, readOnly = false, description = "Parent component", hidden = false,
			dataProviderBean = "CProjectComponentService"
	)
	private CProjectComponent projectComponent;
	@Column (nullable = true, length = 50)
	@AMetaData (displayName = "Version Number", required = false, readOnly = false, description = "Version identifier (e.g., 1.0.0)", hidden = false)
	private String versionNumber;

	/** Default constructor for JPA. */
	protected CProjectComponentVersion() {}

	public CProjectComponentVersion(final String name, final CProject<?> project) {
		super(CProjectComponentVersion.class, name, project);
		initializeDefaults();
	}

	// IHasAttachments interface methods
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	// IHasComments interface methods
	@Override
	public Set<CComment> getComments() { return comments; }

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	public CProjectComponent getProjectComponent() { return projectComponent; }

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
		if (projectComponent != null) {
			projectComponent.getName();
		}
		if (getAssignedTo() != null) {
			getAssignedTo().getLogin();
		}
		if (getCreatedBy() != null) {
			getCreatedBy().getLogin();
		}
	}

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
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
		Check.instanceOf(typeEntity, CProjectComponentVersionType.class, "Type entity must be an instance of CComponentVersionType");
		Check.notNull(getProject(), "Project must be set before assigning component version type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning component version type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning component version type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match component version project company id " + getProject().getCompany().getId());
		entityType = (CProjectComponentVersionType) typeEntity;
		updateLastModified();
	}

	public void setProjectComponent(final CProjectComponent component) {
		projectComponent = component;
		updateLastModified();
	}

	public void setVersionNumber(final String versionNumber) {
		this.versionNumber = versionNumber;
		updateLastModified();
	}
}
