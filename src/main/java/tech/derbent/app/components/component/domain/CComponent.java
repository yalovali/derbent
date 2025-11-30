package tech.derbent.app.components.component.domain;

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
import tech.derbent.app.components.componenttype.domain.CComponentType;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"ccomponent\"")
@AttributeOverride (name = "id", column = @Column (name = "component_id"))
public class CComponent extends CProjectItem<CComponent> implements IHasStatusAndWorkflow<CComponent> {

	public static final String DEFAULT_COLOR = "#808000"; // X11 Olive - component parts (darker)
	public static final String DEFAULT_ICON = "vaadin:cogs";
	public static final String ENTITY_TITLE_PLURAL = "Components";
	public static final String ENTITY_TITLE_SINGULAR = "Component";
	public static final String VIEW_NAME = "Components View";
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
	private CComponentType entityType;

	public CComponent() {
		super();
		initializeDefaults();
	}

	public CComponent(final String name, final CProject project) {
		super(CComponent.class, name, project);
		initializeDefaults();
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

	public void setComponentCode(final String componentCode) {
		this.componentCode = componentCode;
		updateLastModified();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.instanceOf(typeEntity, CComponentType.class, "Type entity must be an instance of CComponentType");
		entityType = (CComponentType) typeEntity;
		updateLastModified();
	}
}
