package tech.derbent.app.sprints.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

/**
 * CSprintStatus - Type entity for categorizing sprints.
 * Provides workflow support and status management for sprints.
 * Layer: Domain (MVC)
 */
@Entity
@Table(name = "csprintstatus")
@AttributeOverride(name = "id", column = @Column(name = "sprintstatus_id"))
public class CSprintStatus extends CTypeEntity<CSprintStatus> {

	public static final String DEFAULT_COLOR = "#28a745";

	/** Default constructor for JPA. */
	public CSprintStatus() {
		super();
	}

	/** Constructor with name and project.
	 * @param name    the name of the sprint status
	 * @param project the project this status belongs to */
	public CSprintStatus(final String name, final CProject project) {
		super(CSprintStatus.class, name, project);
	}

	/** Constructor with name, project, and color.
	 * @param name    the name of the sprint status
	 * @param project the project this status belongs to
	 * @param color   the color code for this status */
	public CSprintStatus(final String name, final CProject project, final String color) {
		super(CSprintStatus.class, name, project);
		setColor(color);
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (getColor() == null || getColor().isEmpty()) {
			setColor(DEFAULT_COLOR);
		}
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
