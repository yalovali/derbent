package tech.derbent.api.interfaces;

import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.app.workflow.service.CWorkflowStatusRelationService;

public interface IContentOwner {

	public CEntityDB<?> createNewEntityInstance() throws Exception;
	CEntityDB<?> getCurrentEntity();
	String getCurrentEntityIdString();
	public CAbstractService<?> getEntityService();

	default CWorkflowStatusRelationService getWorkflowStatusRelationService() { return null; }

	void populateForm() throws Exception;

	default void refreshGrid() throws Exception {
		// Default: no grid to refresh
	}

	void setCurrentEntity(CEntityDB<?> entity);
}
