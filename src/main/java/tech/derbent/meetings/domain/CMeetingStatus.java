package tech.derbent.meetings.domain;

import java.util.Objects;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.annotations.StatusEntity;
import tech.derbent.api.domains.CStatus;
import tech.derbent.api.interfaces.CKanbanStatus;
import tech.derbent.projects.domain.CProject;

/** CMeetingStatus - Domain entity representing meeting status types. Layer: Domain (MVC) Inherits from CStatus to provide status functionality for
 * meetings. This entity defines the possible statuses a meeting can have (e.g., PLANNED, IN_PROGRESS, COMPLETED, CANCELLED). */
@StatusEntity (category = "meeting", colorField = "color", nameField = "name")
@Entity
@Table (name = "cmeetingstatus", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cmeetingstatus_id"))
public class CMeetingStatus extends CStatus<CMeetingStatus> implements CKanbanStatus {

	public static final String DEFAULT_COLOR = "#28a745";
	public static final String DEFAULT_ICON = "vaadin:flag";
	public static final String VIEW_NAME = "Meeting Status View";
	@Column (name = "is_final", nullable = false)
	@AMetaData (
			displayName = "Is Final Status", required = true, readOnly = false, defaultValue = "false",
			description = "Indicates if this is a final status (completed/cancelled)", hidden = false, order = 4
	)
	private Boolean finalStatus = false;

	/** Default constructor for JPA. */
	public CMeetingStatus() {
		super();
		finalStatus = Boolean.FALSE;
		setColor(DEFAULT_COLOR);
	}

	public CMeetingStatus(final String name, final CProject project) {
		super(CMeetingStatus.class, name, project);
		setColor(DEFAULT_COLOR);
		finalStatus = Boolean.FALSE;
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

	public Boolean getFinalStatus() { return finalStatus; }

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), finalStatus);
	}

	public void setFinalStatus(final Boolean finalStatus) { this.finalStatus = finalStatus; }

	@Override
	public String toString() {
		return getName() != null ? getName() : super.toString();
	}
}
