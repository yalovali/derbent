package tech.derbent.users.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CTypeEntity;

/**
 * CUserType - Domain entity representing user types. Layer: Domain (MVC) Inherits from
 * CTypeEntity to provide type functionality for users.
 */
@Entity
@Table (name = "cusertype")
@AttributeOverride (name = "id", column = @Column (name = "cusertype_id"))
public class CUserType extends CTypeEntity {

	/**
	 * Default constructor for JPA.
	 */
	public CUserType() {
		super();
	}

	/**
	 * Constructor with name.
	 * @param name the name of the user type
	 */
	public CUserType(final String name) {
		super(name);
	}

	/**
	 * Constructor with name and description.
	 * @param name        the name of the user type
	 * @param description the description of the user type
	 */
	public CUserType(final String name, final String description) {
		super(name, description);
	}
}