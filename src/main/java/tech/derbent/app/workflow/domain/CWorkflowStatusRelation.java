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
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.app.roles.domain.CUserProjectRole;

@Entity
@Table (name = "cworkflowstatusrelation", uniqueConstraints = @UniqueConstraint (columnNames = {
		"workflow_id", "from_status_id", "to_status_id"
})) // table name for the entity
@AttributeOverride (name = "id", column = @Column (name = "cworkflowstatusrelation_id"))
public class CWorkflowStatusRelation extends CEntityDB<CWorkflowStatusRelation> {

	public static final String DEFAULT_COLOR = "#C3B79F"; // OpenWindows 3D Shadow - workflow transitions
	public static final String DEFAULT_ICON = "vaadin:connect";
	public static final String VIEW_NAME = "Workflow Status Relations View";
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "from_status_id", nullable = false)
	@AMetaData (
			displayName = "From Status", required = true, readOnly = false, description = "The status from which the transition starts",
			hidden = false, setBackgroundFromColor = true, useIcon = true, dataProviderBean = "CProjectItemStatusService"
	)
	private CProjectItemStatus fromStatus;
	// Field to indicate if this is an initial status (used when creating new items)
	@Column (name = "is_initial_status", nullable = false)
	@AMetaData (
			displayName = "Is Initial Status", required = false, readOnly = false, defaultValue = "false",
			description = "Indicates if this status is an initial/start status for new items", hidden = false
	)
	private Boolean initialStatus = Boolean.FALSE;
	@ManyToMany (fetch = FetchType.LAZY)
	@JoinTable (
			name = "cworkflowstatusrelation_roles", joinColumns = @JoinColumn (name = "cworkflowstatusrelation_id"),
			inverseJoinColumns = @JoinColumn (name = "role_id")
	)
	@AMetaData (
			displayName = "User Roles", required = false, readOnly = false,
			description = "The user roles allowed to make this transition (allowed transition roles)", hidden = false, setBackgroundFromColor = true,
			useIcon = true, dataProviderBean = "CUserProjectRoleService", useGridSelection = true
	)
	private List<CUserProjectRole> roles = new ArrayList<>();
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "to_status_id", nullable = false)
	@AMetaData (
			displayName = "To Status", required = true, readOnly = false, description = "The status to which the transition goes", hidden = false,
			setBackgroundFromColor = true, useIcon = true, dataProviderBean = "CProjectItemStatusService"
	)
	private CProjectItemStatus toStatus;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "workflow_id", nullable = false)
	@AMetaData (
			displayName = "Workflow", required = true, readOnly = false, description = "The workflow this status relation belongs to", hidden = false,
			setBackgroundFromColor = true, useIcon = true, dataProviderBean = "CWorkflowEntityService"
	)
	private CWorkflowEntity workflowentity;

	public CWorkflowStatusRelation() {
		super(CWorkflowStatusRelation.class);
	}

	public CProjectItemStatus getFromStatus() { return fromStatus; }

	public Boolean getInitialStatus() { return initialStatus != null ? initialStatus : Boolean.FALSE; }

	public List<CUserProjectRole> getRoles() { return roles; }

	public CProjectItemStatus getToStatus() { return toStatus; }

	public CWorkflowEntity getWorkflowEntity() { return workflowentity; }

	public void setFromStatus(final CProjectItemStatus fromStatus) { this.fromStatus = fromStatus; }

	public void setInitialStatus(Boolean initialStatus) { this.initialStatus = initialStatus != null ? initialStatus : Boolean.FALSE; }

	public void setRoles(final List<CUserProjectRole> roles) { this.roles = roles != null ? roles : new ArrayList<>(); }

	public void setToStatus(final CProjectItemStatus toStatus) { this.toStatus = toStatus; }

	public void setWorkflowEntity(final CWorkflowEntity workflowentity) { this.workflowentity = workflowentity; }

	@Override
	public String toString() {
		return String.format("WorkflowStatusRelation[workflow id=%s, from status id=%s, to status id=%s, roles=%s]",
				workflowentity != null ? CSpringAuxillaries.safeGetId(workflowentity) : null,
				fromStatus != null ? CSpringAuxillaries.safeGetId(fromStatus) : null,
				toStatus != null ? CSpringAuxillaries.safeGetId(toStatus) : null,
				roles != null ? roles.stream().map(CSpringAuxillaries::safeToString).collect(java.util.stream.Collectors.joining(", ")) : "[]");
	}
}
