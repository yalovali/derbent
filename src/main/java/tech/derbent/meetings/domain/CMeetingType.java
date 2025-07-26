package tech.derbent.meetings.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CTypeEntity;

/**
 * CMeetingType - Domain entity representing meeting types. Layer: Domain (MVC) Inherits
 * from CTypeEntity to provide type functionality for meetings.
 */
@Entity
@Table (name = "cmeetingtype")
@AttributeOverride (name = "id", column = @Column (name = "cmeetingtype_id"))
public class CMeetingType extends CTypeEntity {

	/**
	 * Default constructor for JPA.
	 */
	public CMeetingType() {
		super();
	}

	/**
	 * Constructor with name.
	 * @param name the name of the meeting type
	 */
	public CMeetingType(final String name) {
		super(name);
	}

	/**
	 * Constructor with name and description.
	 * @param name        the name of the meeting type
	 * @param description the description of the meeting type
	 */
	public CMeetingType(final String name, final String description) {
		super(name, description);
	}

	@Override
	public boolean equals(final Object o) {

		if (this == o) {
			return true;
		}

		if (!(o instanceof CMeetingType)) {
			return false;
		}
		final CMeetingType that = (CMeetingType) o;
		return super.equals(that);
	}
}