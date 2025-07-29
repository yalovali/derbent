package tech.derbent.activities.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;

/**
 * CActivityType - Domain entity representing activity types. Layer: Domain (MVC) Inherits
 * from CEntityOfProject to provide project-aware type functionality for activities.
 */
@Entity
@Table (name = "cactivitytype")
@AttributeOverride (name = "id", column = @Column (name = "cactivitytype_id"))
public class CActivityType extends CEntityOfProject {

	/**
	 * Default constructor for JPA.
	 */
	public CActivityType() {
		super();
	}

	/**
	 * Constructor with name and project.
	 * @param name the name of the activity type
	 * @param project the project this type belongs to
	 */
	public CActivityType(final String name, final CProject project) {
		super(name, project);
	}

	/**
	 * Constructor with name, description and project.
	 * @param name        the name of the activity type
	 * @param description the description of the activity type
	 * @param project     the project this type belongs to
	 */
	public CActivityType(final String name, final String description, final CProject project) {
		super(name, project);
		setDescription(description);
	}
}