package tech.derbent.users.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;

/**
 * CUserType - Domain entity representing user types. Layer: Domain (MVC) Inherits from
 * CEntityOfProject to provide project-aware type functionality for users.
 */
@Entity
@Table (name = "cusertype")
@AttributeOverride (name = "id", column = @Column (name = "cusertype_id"))
public class CUserType extends CEntityOfProject<CUserType> {

	/**
	 * Default constructor for JPA.
	 */
	public CUserType() {
		super();
	}

	/**
	 * Constructor with name and project.
	 * @param name    the name of the user type
	 * @param project the project this type belongs to
	 */
	public CUserType(final String name, final CProject project) {
		super(CUserType.class, name, project);
	}
}