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
import tech.derbent.app.components.component.domain.CComponent;
import tech.derbent.app.components.componentversiontype.domain.CComponentVersionType;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"ccomponentversion\"")
@AttributeOverride (name = "id", column = @Column (name = "componentversion_id"))
public class CComponentVersion extends CProjectItem<CComponentVersion> implements IHasStatusAndWorkflow<CComponentVersion> {

	public static final String DEFAULT_COLOR = "#6D4C41";
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Component Versions";
	public static final String ENTITY_TITLE_SINGULAR = "Component Version";
	public static final String VIEW_NAME = "Component Versions View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "component_id", nullable = false)
	@AMetaData (
			displayName = "Component", required = true, readOnly = false, description = "Parent component", hidden = false,
			dataProviderBean = "CComponentService"
	)
	private CComponent component;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Version Type", required = false, readOnly = false, description = "Type category of the version", hidden = false,
			dataProviderBean = "CComponentVersionTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CComponentVersionType entityType;
	@Column (nullable = true, length = 50)
	@AMetaData (displayName = "Version Number", required = false, readOnly = false, description = "Version identifier (e.g., 1.0.0)", hidden = false)
	private String versionNumber;

	public CComponentVersion() {
		super();
		initializeDefaults();
	}

	public CComponentVersion(final String name, final CProject project) {
		super(CComponentVersion.class, name, project);
		initializeDefaults();
	}

	public CComponent getComponent() { return component; }

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

	public void setComponent(final CComponent component) {
		this.component = component;
		updateLastModified();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.instanceOf(typeEntity, CComponentVersionType.class, "Type entity must be an instance of CComponentVersionType");
		entityType = (CComponentVersionType) typeEntity;
		updateLastModified();
	}

	public void setVersionNumber(final String versionNumber) {
		this.versionNumber = versionNumber;
		updateLastModified();
	}
}
