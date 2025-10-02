package tech.derbent.decisions.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.annotations.StatusEntity;
import tech.derbent.api.domains.CStatus;
import tech.derbent.projects.domain.CProject;

/** CDecisionStatus - Domain entity representing decision status types. Layer: Domain (MVC) Inherits from CStatus to provide status functionality for
 * decisions. This entity defines the possible statuses a decision can have (e.g., DRAFT, UNDER_REVIEW, APPROVED, REJECTED, IMPLEMENTED). */
@StatusEntity (category = "decision", colorField = "color", nameField = "name")
@Entity
@Table (name = "cdecisionstatus", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "decision_status_id"))
public class CDecisionStatus extends CStatus<CDecisionStatus> {

	public static final String DEFAULT_COLOR = "#28a745";
	public static final String DEFAULT_ICON = "vaadin:flag";
	public static final String VIEW_NAME = "Decision Status View";
	@Column (name = "is_final", nullable = false)
	@AMetaData (
			displayName = "Is Final Status", required = true, readOnly = false, defaultValue = "false",
			description = "Indicates if this is a final status (implemented/rejected)", hidden = false, order = 4
	)
	private Boolean isFinal = Boolean.FALSE;
	@Column (name = "requires_approval", nullable = false)
	@AMetaData (
			displayName = "Requires Approval", required = true, readOnly = false, defaultValue = "false",
			description = "Whether decisions with this status require approval to proceed", hidden = false, order = 7
	)
	private Boolean requiresApproval = Boolean.FALSE;

	public CDecisionStatus() {
		super();
	}

	/** Constructor with name and description.
	 * @param name        the name of the decision status - must not be null or empty
	 * @param description detailed description of the decision status - can be null */
	public CDecisionStatus(final String name, final CProject project) {
		super(CDecisionStatus.class, name, project);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CDecisionStatus)) {
			return false;
		}
		return super.equals(o);
	}

	public Boolean getIsFinal() { return isFinal; }

	public Boolean getRequiresApproval() { return requiresApproval; }

	/** Checks if this status indicates completion of the decision process.
	 * @return true if this is a final status */
	public Boolean isCompleted() { return Boolean.TRUE.equals(isFinal); }

	/** Checks if decisions with this status are pending approval.
	 * @return true if approval is required and status is not final */
	public Boolean isPendingApproval() {
		return Boolean.TRUE.equals(requiresApproval) && !Boolean.TRUE.equals(isFinal);
	}

	public void setFinal(final Boolean isFinal) {
		this.isFinal = isFinal;
		updateLastModified();
	}

	public void setRequiresApproval(final Boolean requiresApproval) {
		this.requiresApproval = requiresApproval;
		updateLastModified();
	}

	@Override
	public String toString() {
		return getName() != null ? getName() : super.toString();
	}

	@Override
	public void initializeAllFields() {
		// TODO Auto-generated method stub
	}
}
