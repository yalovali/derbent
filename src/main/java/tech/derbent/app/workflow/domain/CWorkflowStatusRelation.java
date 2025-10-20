package tech.derbent.app.workflow.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CStatus;

@Entity
@Table (name = "cworkflowstatusrelation", uniqueConstraints = @UniqueConstraint (columnNames = {
		"workflow_id", "from_status_id", "to_status_id"
})) // table name for the entity
@AttributeOverride (name = "id", column = @Column (name = "cworkflowstatusrelation_id"))
public class CWorkflowStatusRelation extends CEntityDB<CWorkflowStatusRelation> {

	private static final long serialVersionUID = 1L;
	@Column (name = "from_status_id", nullable = false)
	@AMetaData (
			displayName = "From Status", required = true, readOnly = false, description = "The status from which the transition starts",
			hidden = false, order = 1, setBackgroundFromColor = true, useIcon = true, dataProviderBean = "context",
			dataProviderMethod = "getAvailableProjects"
	)
	private CStatus fromStatus;
	@Column (name = "to_status_id", nullable = false)
	@AMetaData (
			displayName = "To Status", required = true, readOnly = false, description = "The status to which the transition goes", hidden = false,
			order = 2, setBackgroundFromColor = true, useIcon = true, dataProviderBean = "context", dataProviderMethod = "getAvailableProjects"
	)
	private CStatus toStatus;
	@Column (name = "workflow_id", nullable = false)
	@AMetaData (
			displayName = "Workflow", required = true, readOnly = false, description = "The workflow this status relation belongs to", hidden = false,
			order = 3, setBackgroundFromColor = true, useIcon = true, dataProviderBean = "context", dataProviderMethod = "getAvailableProjects"
	)
	private CWorkflowEntity workflow;

	public CWorkflowStatusRelation() {
		super(CWorkflowStatusRelation.class);
	}
}
