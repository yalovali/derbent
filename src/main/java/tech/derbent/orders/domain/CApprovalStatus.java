package tech.derbent.orders.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "capprovalstatus")
@AttributeOverride (name = "id", column = @Column (name = "approval_status_id"))
public class CApprovalStatus extends CEntityOfProject<CApprovalStatus> {

	public CApprovalStatus(final String name, final CProject project) {
		super(CApprovalStatus.class, name, project);
	}

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return null;
	}
}
