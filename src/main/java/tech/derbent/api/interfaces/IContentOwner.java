package tech.derbent.api.interfaces;

import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.workflow.service.CWorkflowStatusRelationService;

public interface IContentOwner extends IHasPopulateForm {
	public CEntityDB<?> createNewEntityInstance() throws Exception;

	String getCurrentEntityIdString();

	public CAbstractService<?> getEntityService();

	CEntityDB<?> getValue();

	default CWorkflowStatusRelationService getWorkflowStatusRelationService() { return null; }

	default void refreshGrid() throws Exception {
		// Default: no grid to refresh
	}

	void setValue(CEntityDB<?> entity);
}
