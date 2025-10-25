package tech.derbent.api.domains;

import tech.derbent.app.workflow.domain.CWorkflowEntity;

public interface IHasStatusAndWorkflow<EntityClass> {

	CTypeEntity<?> getEntityType();
	CProjectItemStatus getStatus();
	CWorkflowEntity getWorkflow();
	void setEntityType(CTypeEntity<?> typeEntity);
	void setStatus(CProjectItemStatus status);
}
