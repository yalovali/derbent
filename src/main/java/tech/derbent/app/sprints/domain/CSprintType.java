package tech.derbent.app.sprints.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

/** CSprintType - Domain entity representing sprint types. Layer: Domain (MVC) Inherits from CEntityOfProject to provide project-aware type
 * functionality for sprints. */
@Entity
@Table(name = "csprinttype", uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames = {
		"name", "project_id"
}))
@AttributeOverride(name = "id", column = @Column(name = "csprinttype_id"))
public class CSprintType extends CTypeEntity<CSprintType> {

	public static final String DEFAULT_COLOR = "#28a745";
	public static final String DEFAULT_ICON = "vaadin:calendar-clock";
	public static final String ENTITY_TITLE_PLURAL = "Sprint Types";
	public static final String ENTITY_TITLE_SINGULAR = "Sprint Type";
	public static final String VIEW_NAME = "Sprint Types View";

	/** Default constructor for JPA. */
	public CSprintType() {
		super();
	}

	public CSprintType(final String name, final CProject project) {
		super(CSprintType.class, name, project);
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
