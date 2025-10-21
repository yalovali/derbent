package tech.derbent.app.workflow.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.annotations.CSpringAuxillaries;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.app.activities.domain.CActivityStatus;
import tech.derbent.app.roles.domain.CUserProjectRole;

@Entity
@Table (name = "cworkflowstatusrelation", uniqueConstraints = @UniqueConstraint (columnNames = {
		"workflow_id", "from_status_id", "to_status_id"
})) // table name for the entity
@AttributeOverride (name = "id", column = @Column (name = "cworkflowstatusrelation_id"))
public class CWorkflowStatusRelation extends CEntityDB<CWorkflowStatusRelation> {

	public static final String VIEW_NAME = "Workflow Status Relations View";
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "from_status_id", nullable = false)
	@AMetaData (
			displayName = "From Status", required = true, readOnly = false, description = "The status from which the transition starts",
			hidden = false, order = 1, setBackgroundFromColor = true, useIcon = true, dataProviderBean = "CActivityStatusService"
	)
	private CActivityStatus fromStatus;
	@ManyToMany (fetch = FetchType.LAZY)
	@JoinTable (
			name = "cworkflowstatusrelation_roles", joinColumns = @JoinColumn (name = "cworkflowstatusrelation_id"),
			inverseJoinColumns = @JoinColumn (name = "role_id")
	)
	@AMetaData (
			displayName = "User Roles", required = false, readOnly = false,
			description = "The user roles allowed to make this transition (allowed transition roles)", hidden = false, order = 4,
			setBackgroundFromColor = true, useIcon = true, dataProviderBean = "CUserProjectRoleService", useGridSelection = true
	)
	private List<CUserProjectRole> roles = new ArrayList<>();
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "to_status_id", nullable = false)
	@AMetaData (
			displayName = "To Status", required = true, readOnly = false, description = "The status to which the transition goes", hidden = false,
			order = 2, setBackgroundFromColor = true, useIcon = true, dataProviderBean = "CActivityStatusService"
	)
	private CActivityStatus toStatus;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "workflow_id", nullable = false)
	@AMetaData (
			displayName = "Workflow", required = true, readOnly = false, description = "The workflow this status relation belongs to", hidden = false,
			order = 3, setBackgroundFromColor = true, useIcon = true, dataProviderBean = "CWorkflowEntityService"
	)
	private CWorkflowEntity workflow;

	public CWorkflowStatusRelation() {
		super(CWorkflowStatusRelation.class);
	}

	public CActivityStatus getFromStatus() { return fromStatus; }

	public List<CUserProjectRole> getRoles() { return roles; }

	public CActivityStatus getToStatus() { return toStatus; }

	public CWorkflowEntity getWorkflow() { return workflow; }

	@Override
	public void initializeAllFields() {
		// initialize lazily loaded fields
		if (workflow != null) {
			workflow.getName();
		}
		if (fromStatus != null) {
			fromStatus.getName();
		}
		if (toStatus != null) {
			toStatus.getName();
		}
		if (roles != null) {
			roles.forEach(role -> role.getName());
		}
	}

	public void setFromStatus(final CActivityStatus fromStatus) { this.fromStatus = fromStatus; }

	public void setRoles(final List<CUserProjectRole> roles) { this.roles = roles != null ? roles : new ArrayList<>(); }

	public void setToStatus(final CActivityStatus toStatus) { this.toStatus = toStatus; }

	public void setWorkflow(final CWorkflowEntity workflow) { this.workflow = workflow; }

	@Override
	public String toString() {
		return String.format("WorkflowStatusRelation[workflow id=%s, from status id=%s, to status id=%s, roles=%s]",
				workflow != null ? CSpringAuxillaries.safeGetId(workflow) : null,
				fromStatus != null ? CSpringAuxillaries.safeGetId(fromStatus) : null,
				toStatus != null ? CSpringAuxillaries.safeGetId(toStatus) : null,
				roles != null ? roles.stream().map(CSpringAuxillaries::safeToString).collect(java.util.stream.Collectors.joining(", ")) : "[]");
	}
}
