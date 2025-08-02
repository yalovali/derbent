package tech.derbent.meetings.domain;

import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.base.domain.CStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CMeetingStatus - Domain entity representing meeting status types. Layer: Domain (MVC)
 * Inherits from CStatus to provide status functionality for meetings. This entity defines
 * the possible statuses a meeting can have (e.g., PLANNED, IN_PROGRESS, COMPLETED,
 * CANCELLED).
 */
@Entity
@Table (name = "cmeetingstatus")
@AttributeOverride (name = "id", column = @Column (name = "cmeetingstatus_id"))
public class CMeetingStatus extends CStatus<CMeetingStatus> {

	@Column (name = "is_final", nullable = false)
	@MetaData (
		displayName = "Is Final Status", required = true, readOnly = false,
		defaultValue = "false",
		description = "Indicates if this is a final status (completed/cancelled)",
		hidden = false, order = 4
	)
	private boolean isFinal = false;

	public CMeetingStatus(final String name, final CProject project) {
		super(CMeetingStatus.class, name, project);
	}

	public CMeetingStatus(final String name, final CProject project,
		final String description, final String color, final boolean isFinal) {
		super(CMeetingStatus.class, name, project);
		setDescription(description);
		setColor(color != null ? color : "#808080");
		this.isFinal = isFinal;
	}

	@Override
	public boolean equals(final Object o) {

		if (this == o) {
			return true;
		}

		if (!(o instanceof CMeetingStatus)) {
			return false;
		}
		final CMeetingStatus that = (CMeetingStatus) o;
		return super.equals(that);
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