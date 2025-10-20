package tech.derbent.app.activities.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.interfaces.IKanbanType;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "cactivitytype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cactivitytype_id"))
public class CActivityType extends CTypeEntity<CActivityType> implements IKanbanType {

	public static final String DEFAULT_COLOR = "#17a2b8";
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String VIEW_NAME = "Activity Type Management";

	/** Default constructor for JPA. */
	public CActivityType() {
		super();
	}

	public CActivityType(final String name, final CProject project) {
		super(CActivityType.class, name, project);
	}

	@Override
	public void initializeAllFields() {
		// Initialize lazy-loaded entity relationships from parent class (CEntityOfProject)
		if (getProject() != null) {
			getProject().getName(); // Trigger project loading
		}
		if (getAssignedTo() != null) {
			getAssignedTo().getLogin(); // Trigger assigned user loading
		}
		if (getCreatedBy() != null) {
			getCreatedBy().getLogin(); // Trigger creator loading
		}
	}
}
