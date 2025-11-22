package tech.derbent.api.services.pageservice;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

public interface IPageServiceHasStatusAndWorkflow<EntityClass extends CEntityDB<EntityClass>> {

	static final Logger LOGGER = LoggerFactory.getLogger(IPageServiceHasStatusAndWorkflow.class);

	default void actionChangeStatus(final CProjectItemStatus newStatus) {
		try {
			final EntityClass entity = getView().getCurrentEntity();
			if (entity == null) {
				LOGGER.warn("No current entity for status change operation");
				CNotificationService.showWarning("No entity selected for status change");
				return;
			}
			if (newStatus == null) {
				LOGGER.warn("Null status provided for status change");
				CNotificationService.showWarning("Invalid status selected");
				return;
			}
			// Validate that the new status is allowed by workflow rules
			if (getProjectItemStatusService() == null) {
				LOGGER.error("CProjectItemStatusService not available - cannot validate status change");
				CNotificationService.showError("Status validation service unavailable");
				return;
			}
			// Get valid next statuses from workflow
			final List<CProjectItemStatus> validStatuses = getProjectItemStatusService().getValidNextStatuses((IHasStatusAndWorkflow<?>) entity);
			// Check if the new status is in the list of valid statuses
			final boolean isValidTransition = validStatuses.stream().anyMatch(s -> s.getId().equals(newStatus.getId()));
			if (!isValidTransition) {
				final String currentStatusName =
						((IHasStatusAndWorkflow<?>) entity).getStatus() != null ? ((IHasStatusAndWorkflow<?>) entity).getStatus().getName() : "none";
				LOGGER.warn("Invalid status transition from '{}' to '{}' for entity ID: {}", currentStatusName, newStatus.getName(), entity.getId());
				CNotificationService.showWarning(String.format("Cannot change status from '%s' to '%s' - transition not allowed by workflow",
						currentStatusName, newStatus.getName()));
				return;
			}
			// Status change is valid - apply it
			final String oldStatusName =
					((IHasStatusAndWorkflow<?>) entity).getStatus() != null ? ((IHasStatusAndWorkflow<?>) entity).getStatus().getName() : "none";
			((IHasStatusAndWorkflow<?>) entity).setStatus(newStatus);
			LOGGER.info("Status changed from '{}' to '{}' for entity ID: {}", oldStatusName, newStatus.getName(), entity.getId());
			// Save the entity to persist the status change
			final EntityClass savedEntity = getEntityService().save(entity);
			LOGGER.info("Entity saved successfully with ID: {}", savedEntity.getId());
			setCurrentEntity(savedEntity);
			// Notify view that entity was saved - this triggers grid refresh
			getView().onEntitySaved(savedEntity);
			getView().populateForm();
			CNotificationService.showInfo(String.format("Status changed to '%s'", newStatus.getName()));
		} catch (final Exception e) {
			LOGGER.error("Error changing status: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to change status.", e);
			// throw e;
		}
	}

	default List<CProjectItemStatus> getAvailableStatusesForProjectItem() {
		LOGGER.debug("Retrieving available statuses for current entity");
		final EntityClass entity = getView().getCurrentEntity();
		if (entity == null) {
			LOGGER.warn("No current entity for retrieving available statuses");
			return List.of();
		}
		Check.notNull(getProjectItemStatusService(), "CProjectItemStatusService cannot be null");
		return getProjectItemStatusService().getValidNextStatuses((IHasStatusAndWorkflow<?>) entity);
	}

	CAbstractService<EntityClass> getEntityService();
	CProjectItemStatusService getProjectItemStatusService();
	IPageServiceImplementer<EntityClass> getView();
	void setCurrentEntity(final EntityClass entity);
}
