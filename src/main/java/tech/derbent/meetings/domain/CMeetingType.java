package tech.derbent.meetings.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;

/**
 * CMeetingType - Domain entity representing meeting types. Layer: Domain (MVC) Inherits
 * from CEntityOfProject to provide project-aware type functionality for meetings.
 */
@Entity
@Table (name = "cmeetingtype")
@AttributeOverride (name = "id", column = @Column (name = "cmeetingtype_id"))
public class CMeetingType extends CEntityOfProject {

	/**
	 * Default constructor for JPA.
	 */
	public CMeetingType() {
		super();
	}

	/**
	 * Constructor with name and project.
	 * @param name the name of the meeting type
	 * @param project the project this type belongs to
	 */
	public CMeetingType(final String name, final CProject project) {
		super(name, project);
	}

	/**
	 * Constructor with name, description and project.
	 * @param name        the name of the meeting type
	 * @param description the description of the meeting type
	 * @param project     the project this type belongs to
	 */
	public CMeetingType(final String name, final String description, final CProject project) {
		super(name, project);
		setDescription(description);
	}
}