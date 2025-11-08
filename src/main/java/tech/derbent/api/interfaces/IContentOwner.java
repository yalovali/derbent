package tech.derbent.api.interfaces;

import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.app.workflow.service.CWorkflowStatusRelationService;

public interface IContentOwner {

	public CEntityDB<?> createNewEntityInstance() throws Exception;
	Object getCurrentEntity();
	String getCurrentEntityIdString();
	public CAbstractService<?> getEntityService();

	/** Get the workflow status relation service. Optional - may return null.
	 * @return workflow status relation service or null if not available */
	default CWorkflowStatusRelationService getWorkflowStatusRelationService() {
		return null;
	}

	/** Called after entity is refreshed to update the UI. Default implementation sets current entity and repopulates form.
	 * @param entity the refreshed entity */
	default void onEntityRefreshed(CEntityDB<?> entity) throws Exception {
		setCurrentEntity(entity);
		populateForm();
	}

	void populateForm() throws Exception;

	/** Called after grid data has been refreshed. Default implementation does nothing. Override to implement grid refresh logic if applicable. */
	default void refreshGrid() throws Exception {
		// Default: no grid to refresh
	}

	void setCurrentEntity(Object entity);
}
