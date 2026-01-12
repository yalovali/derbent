package tech.derbent.api.interfaces;

import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.workflow.service.CWorkflowStatusRelationService;

public interface IContentOwner {

	public CEntityDB<?> createNewEntityInstance() throws Exception;
	CEntityDB<?> getValue();
	String getCurrentEntityIdString();
	public CAbstractService<?> getEntityService();

	default CWorkflowStatusRelationService getWorkflowStatusRelationService() { return null; }

	void populateForm() throws Exception;

	default void refreshGrid() throws Exception {
		// Default: no grid to refresh
	}

	void setValue(CEntityDB<?> entity);
}
