package tech.derbent.app.deliverables.deliverable.domain;

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
import tech.derbent.app.deliverables.deliverabletype.domain.CDeliverableType;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"cdeliverable\"")
@AttributeOverride (name = "id", column = @Column (name = "deliverable_id"))
public class CDeliverable extends CProjectItem<CDeliverable> implements IHasStatusAndWorkflow<CDeliverable> {

	public static final String DEFAULT_COLOR = "#FFFBEA"; // OpenWindows 3D Light - deliverable items
	public static final String DEFAULT_ICON = "vaadin:clipboard-check";
	public static final String ENTITY_TITLE_PLURAL = "Deliverables";
	public static final String ENTITY_TITLE_SINGULAR = "Deliverable";
	public static final String VIEW_NAME = "Deliverable View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Deliverable Type", required = false, readOnly = false, description = "Type category of the deliverable", hidden = false,
			 dataProviderBean = "CDeliverableTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CDeliverableType entityType;

	/** Default constructor for JPA. */
	public CDeliverable() {
		super();
		initializeDefaults();
	}

	public CDeliverable(final String name, final CProject project) {
		super(CDeliverable.class, name, project);
		initializeDefaults();
	}

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

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
		Check.instanceOf(typeEntity, CDeliverableType.class, "Type entity must be an instance of CDeliverableType");
		entityType = (CDeliverableType) typeEntity;
		updateLastModified();
	}
}
