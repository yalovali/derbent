package tech.derbent.api.services.pageservice;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.domain.CProjectItemStatus;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

/** CPageServiceWithWorkflow - Base page service for entities that implement IHasStatusAndWorkflow.
 * <p>
 * This class provides workflow-aware status change handling with validation against workflow status relations. It should be used as the base class for
 * page services of entities like CActivity, CMeeting, CDecision, CRisk, and COrder.
 * <p>
 * Key features:
 * <ul>
 * <li>Validates status changes against workflow rules before applying them</li>
 * <li>Uses CProjectItemStatusService to retrieve valid status transitions</li>
 * <li>Provides user-friendly notifications for invalid status changes</li>
 * <li>Automatically saves the entity after successful status change</li>
 * </ul>
 * @param <EntityClass> Entity type that extends CProjectItem and implements IHasStatusAndWorkflow */
public abstract class CPageServiceWithWorkflow<EntityClass extends CProjectItem<EntityClass> & IHasStatusAndWorkflow<EntityClass>>
		extends CPageServiceDynamicPage<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceWithWorkflow.class);
	protected CProjectItemStatusService projectItemStatusService;

	public CPageServiceWithWorkflow(final IPageServiceImplementer<EntityClass> view) {
		super(view);
		// Get the status service from Spring context
		try {
			projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	/** Handle status change with workflow validation.
	 * <p>
	 * This method:
	 * <ol>
	 * <li>Validates the current entity exists</li>
	 * <li>Checks if the new status is in the list of valid next statuses (using workflow rules)</li>
	 * <li>Sets the new status on the entity if validation passes</li>
	 * <li>Saves the entity to persist the status change</li>
	 * <li>Shows appropriate notifications to the user</li>
	 * </ol>
	 * @param newStatus the new status selected by the user
	 * @throws Exception if the status change or save operation fails */
	@Override
	public void actionChangeStatus(final CProjectItemStatus newStatus) throws Exception {
		try {
			final EntityClass entity = view.getCurrentEntity();
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
			if (projectItemStatusService == null) {
				LOGGER.error("CProjectItemStatusService not available - cannot validate status change");
				CNotificationService.showError("Status validation service unavailable");
				return;
			}
			// Get valid next statuses from workflow
			final List<CProjectItemStatus> validStatuses = projectItemStatusService.getValidNextStatuses(entity);
			// Check if the new status is in the list of valid statuses
			final boolean isValidTransition = validStatuses.stream().anyMatch(s -> s.getId().equals(newStatus.getId()));
			if (!isValidTransition) {
				final String currentStatusName = entity.getStatus() != null ? entity.getStatus().getName() : "none";
				LOGGER.warn("Invalid status transition from '{}' to '{}' for entity ID: {}", currentStatusName, newStatus.getName(), entity.getId());
				CNotificationService.showWarning(
						String.format("Cannot change status from '%s' to '%s' - transition not allowed by workflow", currentStatusName,
								newStatus.getName()));
				return;
			}
			// Status change is valid - apply it
			final String oldStatusName = entity.getStatus() != null ? entity.getStatus().getName() : "none";
			entity.setStatus(newStatus);
			LOGGER.info("Status changed from '{}' to '{}' for entity ID: {}", oldStatusName, newStatus.getName(), entity.getId());
			// Save the entity to persist the status change
			final EntityClass savedEntity = getEntityService().save(entity);
			view.setCurrentEntity(savedEntity);
			view.populateForm();
			CNotificationService.showInfo(String.format("Status changed to '%s'", newStatus.getName()));
		} catch (final Exception e) {
			LOGGER.error("Error changing status: {}", e.getMessage(), e);
			CNotificationService.showError("Failed to change status: " + e.getMessage());
			throw e;
		}
	}
}
