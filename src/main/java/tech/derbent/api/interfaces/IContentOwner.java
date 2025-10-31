package tech.derbent.api.interfaces;

import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.ui.notifications.CNotificationService;

/** Interface for content owners (like pages) that can provide context-specific data for content resource resolvers such as comboboxes in forms. This
 * allows FormBuilder to access methods on the current page/context owner instead of just service beans, enabling context-aware data providers.
 * Enhanced to support better access to parent container data and context, and to provide callbacks for CRUD toolbar operations.
 * @author Derbent Framework
 * @since 1.0 */
public interface IContentOwner {

	public CEntityDB<?> createNewEntityInstance() throws Exception;
	Object getCurrentEntity();
	String getCurrentEntityIdString();
	public CAbstractService<?> getEntityService();

	/** Get the notification service for displaying user messages. Optional - may return null.
	 * @return notification service or null if not available */
	default CNotificationService getNotificationService() { return null; }

	/** Get the workflow status relation service. Optional - may return null.
	 * @return workflow status relation service or null if not available */
	default tech.derbent.app.workflow.service.CWorkflowStatusRelationService getWorkflowStatusRelationService() {
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
	protected default void refreshGrid() throws Exception {
		// Default: no grid to refresh
	}

	void setCurrentEntity(Object entity);
}
