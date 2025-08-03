package tech.derbent.activities.domain;

import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.base.domain.CStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CActivityStatus - Domain entity representing activity status types. Layer: Domain (MVC)
 * Inherits from CStatus to provide status functionality for activities. This entity
 * defines the possible statuses an activity can have (e.g., TODO, IN_PROGRESS, DONE).
 */
@Entity
@Table (name = "cactivitystatus")
@AttributeOverride (name = "id", column = @Column (name = "cactivitystatus_id"))
public class CActivityStatus extends CStatus<CActivityStatus> {

	@Column (name = "is_final", nullable = false)
	@MetaData (
		displayName = "Is Final Status", required = true, readOnly = false,
		defaultValue = "false",
		description = "Indicates if this is a final status (completed/cancelled)",
		hidden = false, order = 4
	)
	private boolean isFinal = false;

	/**
	 * Default constructor for JPA.
	 */
	public CActivityStatus() {
		super();
		// Initialize with default values for JPA
		this.isFinal = false;
	}

	public CActivityStatus(final String name, final CProject project) {
		super(CActivityStatus.class, name, project);
	}

	public CActivityStatus(final String name, final CProject project,
		final String description, final String color, final boolean isFinal) {
		super(CActivityStatus.class, name, project);
		setDescription(description);
		setColor(color);
		this.isFinal = isFinal;
	}

	@Override
	public boolean equals(final Object o) {

		if (this == o) {
			return true;
		}

		if (!(o instanceof CActivityStatus)) {
			return false;
		}
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), isFinal);
	}

	public boolean isFinal() { return isFinal; }

	public void setFinal(final boolean isFinal) { this.isFinal = isFinal; }

	@Override
	public String toString() {
		return getName() != null ? getName() : super.toString();
	}
}