package tech.derbent.app.components.componentversion.domain;

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
import tech.derbent.app.components.component.domain.CProjectComponent;
import tech.derbent.app.components.componentversiontype.domain.CProjectComponentVersionType;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"cprojectcomponentversion\"")
@AttributeOverride (name = "id", column = @Column (name = "projectcomponentversion_id"))
public class CProjectComponentVersion extends CProjectItem<CProjectComponentVersion> implements IHasStatusAndWorkflow<CProjectComponentVersion> {

	public static final String DEFAULT_COLOR = "#808000"; // X11 Olive - component versions (darker)
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Component Versions";
	public static final String ENTITY_TITLE_SINGULAR = "Component Version";
	public static final String VIEW_NAME = "Component Versions View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "projectcomponent_id", nullable = false)
	@AMetaData (
			displayName = "Component", required = true, readOnly = false, description = "Parent component", hidden = false,
			dataProviderBean = "CProjectComponentService"
	)
	private CProjectComponent component;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Version Type", required = false, readOnly = false, description = "Type category of the version", hidden = false,
			dataProviderBean = "CProjectComponentVersionTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CProjectComponentVersionType entityType;
	@Column (nullable = true, length = 50)
	@AMetaData (displayName = "Version Number", required = false, readOnly = false, description = "Version identifier (e.g., 1.0.0)", hidden = false)
	private String versionNumber;

	public CProjectComponentVersion() {
		super();
		initializeDefaults();
	}

	public CProjectComponentVersion(final String name, final CProject project) {
		super(CProjectComponentVersion.class, name, project);
		initializeDefaults();
	}

	public CProjectComponent getComponent() { return component; }

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

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
		if (component != null) {
			component.getName();
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

	public void setComponent(final CProjectComponent component) {
		this.component = component;
		updateLastModified();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.instanceOf(typeEntity, CProjectComponentVersionType.class, "Type entity must be an instance of CComponentVersionType");
		entityType = (CProjectComponentVersionType) typeEntity;
		updateLastModified();
	}

	public void setVersionNumber(final String versionNumber) {
		this.versionNumber = versionNumber;
		updateLastModified();
	}
}
