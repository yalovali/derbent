package tech.derbent.app.decisions.domain;

import java.util.Objects;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.annotations.StatusEntity;
import tech.derbent.api.domains.CStatus;
import tech.derbent.api.interfaces.IKanbanStatus;
import tech.derbent.app.projects.domain.CProject;

/** CDecisionStatus - Domain entity representing decision status types. Layer: Domain (MVC) Inherits from CStatus to provide status functionality for
 * decisions. This entity defines the possible statuses a decision can have (e.g., PENDING, APPROVED, REJECTED, IMPLEMENTED). */
@StatusEntity (category = "decision", colorField = "color", nameField = "name")
@Entity
@Table (name = "cdecisionstatus", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cdecisionstatus_id"))
public class CDecisionStatus extends CStatus<CDecisionStatus> implements IKanbanStatus {

	public static final String DEFAULT_COLOR = "#28a745";
	public static final String DEFAULT_ICON = "vaadin:flag";
	public static final String VIEW_NAME = "Decision Status View";
	@Column (name = "is_final", nullable = false)
	@AMetaData (
			displayName = "Is Final Status", required = true, readOnly = false, defaultValue = "false",
			description = "Indicates if this is a final status (completed/cancelled)", hidden = false, order = 4
	)
	private Boolean finalStatus = false;

	/** Default constructor for JPA. */
	public CDecisionStatus() {
		super();
		finalStatus = Boolean.FALSE;
		setColor(DEFAULT_COLOR);
	}

	public CDecisionStatus(final String name, final CProject project) {
		super(CDecisionStatus.class, name, project);
		setColor(DEFAULT_COLOR);
		finalStatus = Boolean.FALSE;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CDecisionStatus)) {
			return false;
		}
		final CDecisionStatus that = (CDecisionStatus) o;
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
}
