package tech.derbent.activities.domain;

import java.util.Objects;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.annotations.StatusEntity;
import tech.derbent.api.domains.CStatus;
import tech.derbent.api.interfaces.IKanbanStatus;
import tech.derbent.projects.domain.CProject;

/** CActivityStatus - Domain entity representing activity status types. Layer: Domain (MVC) Inherits from CStatus to provide status functionality for
 * activities. This entity defines the possible statuses an activity can have (e.g., TODO, IN_PROGRESS, DONE). */
@StatusEntity (category = "activity", colorField = "color", nameField = "name")
@Entity
@Table (name = "cactivitystatus", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cactivitystatus_id"))
public class CActivityStatus extends CStatus<CActivityStatus> implements IKanbanStatus {

	public static final String DEFAULT_COLOR = "#28a745";
	public static final String DEFAULT_ICON = "vaadin:flag";
	public static final String VIEW_NAME = "Activity Statuses View";
	@Column (name = "is_final", nullable = false)
	@AMetaData (
			displayName = "Is Final Status", required = true, readOnly = false, defaultValue = "false",
			description = "Indicates if this is a final status (completed/cancelled)", hidden = true, order = 4
	)
	private Boolean finalStatus = Boolean.FALSE;

	/** Default constructor for JPA. */
	public CActivityStatus() {
		super();
		setColor(DEFAULT_COLOR);
		// Initialize with default values for JPA
		finalStatus = Boolean.FALSE;
	}

	public CActivityStatus(final String name, final CProject project) {
		super(CActivityStatus.class, name, project);
		setColor(DEFAULT_COLOR);
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

	@Override
	public void initializeAllFields() {
		// TODO Auto-generated method stub
		
	}
}
