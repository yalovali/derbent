package tech.derbent.activities.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CTypeEntity;

/**
 * CActivityType - Domain entity representing activity types. Layer: Domain (MVC) Inherits
 * from CTypeEntity to provide type functionality for activities.
 */
@Entity
@Table (name = "cactivitytype")
@AttributeOverride (name = "id", column = @Column (name = "cactivitytype_id"))
public class CActivityType extends CTypeEntity {

	/**
	 * Default constructor for JPA.
	 */
	public CActivityType() {
		super();
	}

	/**
	 * Constructor with name.
	 * @param name the name of the activity type
	 */
	public CActivityType(final String name) {
		super(name);
	}

	/**
	 * Constructor with name and description.
	 * @param name        the name of the activity type
	 * @param description the description of the activity type
	 */
	public CActivityType(final String name, final String description) {
		super(name, description);
	}
}