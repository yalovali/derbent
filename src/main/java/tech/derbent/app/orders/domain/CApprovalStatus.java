package tech.derbent.app.orders.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.domains.CStatus;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "capprovalstatus")
@AttributeOverride (name = "id", column = @Column (name = "approval_status_id"))
public class CApprovalStatus extends CStatus<CApprovalStatus> {

	public static final String DEFAULT_COLOR = "#28a745";
	public static final String DEFAULT_ICON = "vaadin:check";
	public static final String VIEW_NAME = "Approval Status View";

	/** Default constructor for JPA. */
	public CApprovalStatus() {
		super();
		setColor(DEFAULT_COLOR);
	}

	public CApprovalStatus(final String name, final CProject project) {
		super(CApprovalStatus.class, name, project);
		setColor(DEFAULT_COLOR);
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
