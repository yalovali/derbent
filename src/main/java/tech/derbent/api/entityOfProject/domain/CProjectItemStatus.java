package tech.derbent.api.entityOfProject.domain;

import java.util.Objects;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CStatus;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "cprojectitemstatus", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cprojectitemstatus_id"))
public class CProjectItemStatus extends CStatus<CProjectItemStatus> {

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
	public CProjectItemStatus() {
		super();
		setColor(DEFAULT_COLOR);
		// Initialize with default values for JPA
		finalStatus = Boolean.FALSE;
	}

	public CProjectItemStatus(final String name, final CProject project) {
		super(CProjectItemStatus.class, name, project);
		setColor(DEFAULT_COLOR);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CProjectItemStatus)) {
			return false;
		}
		return super.equals(o);
	}

	public Boolean getFinalStatus() { return finalStatus; }

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), finalStatus);
	}

	@Override
	public void initializeAllFields() {
		// Initialize lazy-loaded entity relationships from parent class (CEntityOfProject)
		if (getProject() != null) {
			getProject().getName(); // Trigger project loading
		}
		if (getAssignedTo() != null) {
			getAssignedTo().getLogin(); // Trigger assigned user loading
		}
		if (getCreatedBy() != null) {
			getCreatedBy().getLogin(); // Trigger creator loading
		}
	}

	public void setFinalStatus(final Boolean finalStatus) { this.finalStatus = finalStatus; }

	@Override
	public String toString() {
		return getName() != null ? getName() : super.toString();
	}
}
