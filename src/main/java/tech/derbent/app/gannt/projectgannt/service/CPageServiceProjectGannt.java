package tech.derbent.app.gannt.projectgannt.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.gannt.ganntviewentity.domain.CGanntViewEntity;
import tech.derbent.app.gannt.ganntviewentity.view.CGridViewBaseGannt;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

public class CPageServiceProjectGannt extends CPageServiceDynamicPage<CGanntViewEntity> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectGannt.class);
	private final CActivityService activityService;
	// The current actual entity being edited (CActivity or CMeeting)
	private CProjectItem<?> currentActualEntity;
	private final CMeetingService meetingService;
	// The previous actual entity for refresh operations
	private CProjectItem<?> previousActualEntity;
	protected CProjectItemStatusService projectItemStatusService;

	public CPageServiceProjectGannt(final IPageServiceImplementer<CGanntViewEntity> view, final CActivityService activityService,
			final CMeetingService meetingService) {
		super(view);
		this.activityService = activityService;
		this.meetingService = meetingService;
		// Get the status service from Spring context for workflow validation
		try {
			projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	/** Handle status change with workflow validation for Gantt entities.
	 * <p>
	 * This method handles status changes for both CActivity and CMeeting entities in the Gantt view. It validates the status transition against
	 * workflow rules before applying the change.
	 * @param newStatus the new status selected by the user
	 * @throws Exception if the status change or save operation fails */
	@Override
	public void actionChangeStatus(final CProjectItemStatus newStatus) throws Exception {
		try {
			final CProjectItem<?> entity = currentActualEntity;
			if (entity == null) {
				LOGGER.warn("No current entity for status change operation");
				CNotificationService.showWarning("Please select an item to change status.");
				return;
			}
			if (newStatus == null) {
				LOGGER.warn("Null status provided for status change");
				CNotificationService.showWarning("Invalid status selected");
				return;
			}
			LOGGER.debug("Change status action triggered for entity ID: {} (type: {}) to status: {}", entity.getId(),
					entity.getClass().getSimpleName(), newStatus.getName());
			// Validate workflow if entity supports it (both CActivity and CMeeting do)
			if (entity instanceof IHasStatusAndWorkflow) {
				if (projectItemStatusService == null) {
					LOGGER.error("CProjectItemStatusService not available - cannot validate status change");
					CNotificationService.showError("Status validation service unavailable");
					return;
				}
				// Get valid next statuses from workflow
				final List<CProjectItemStatus> validStatuses = projectItemStatusService.getValidNextStatuses((IHasStatusAndWorkflow<?>) entity);
				// Check if the new status is in the list of valid statuses
				final boolean isValidTransition = validStatuses.stream().anyMatch(s -> s.getId().equals(newStatus.getId()));
				if (!isValidTransition) {
					final String currentStatusName = entity.getStatus() != null ? entity.getStatus().getName() : "none";
					LOGGER.warn("Invalid status transition from '{}' to '{}' for entity ID: {}", currentStatusName, newStatus.getName(),
							entity.getId());
					CNotificationService.showWarning(String.format("Cannot change status from '%s' to '%s' - transition not allowed by workflow",
							currentStatusName, newStatus.getName()));
					return;
				}
			}
			// Status change is valid - apply it
			final String oldStatusName = entity.getStatus() != null ? entity.getStatus().getName() : "none";
			entity.setStatus(newStatus);
			LOGGER.info("Status changed from '{}' to '{}' for entity ID: {} (type: {})", oldStatusName, newStatus.getName(), entity.getId(),
					entity.getClass().getSimpleName());
			// Save the entity with new status - ALWAYS save after status change
			@SuppressWarnings ("rawtypes")
			final CAbstractService service = getServiceForEntity(entity);
			@SuppressWarnings ("unchecked")
			final CProjectItem<?> savedEntity = (CProjectItem<?>) service.save(entity);
			LOGGER.info("Entity status saved successfully for ID: {} to status: {}", savedEntity.getId(), newStatus.getName());
			// Update current entity with saved version
			currentActualEntity = savedEntity;
			// Refresh the view to show updated status
			view.onEntitySaved(null);
			view.populateForm();
			CNotificationService.showInfo(String.format("Status changed to '%s'", newStatus.getName()));
		} catch (final Exception e) {
			LOGGER.error("Error changing status: {}", e.getMessage(), e);
			CNotificationService.showError("Failed to change status: " + e.getMessage());
			throw e;
		}
	}

	@Override
	public void actionCreate() throws Exception {
		try {
			LOGGER.debug("Create action triggered for Gantt view");
			// For Gantt view, we need to know what type of entity to create
			// Currently, we'll default to creating an Activity
			// This could be enhanced to show a dialog to select entity type
			previousActualEntity = currentActualEntity;
			final CActivity newEntity = activityService.newEntity();
			activityService.initializeNewEntity(newEntity);
			// Update the current actual entity
			currentActualEntity = newEntity;
			// Populate the form with the new entity
			view.populateForm();
			LOGGER.info("New activity entity created for Gantt view");
		} catch (final Exception e) {
			LOGGER.error("Error creating new entity instance for Gantt view: {}", e.getMessage());
			throw e;
		}
	}

	@Override
	public void actionDelete() throws Exception {
		try {
			final CProjectItem<?> entity = currentActualEntity;
			if (entity == null || entity.getId() == null) {
				CNotificationService.showWarning("Please select an item to delete.");
				return;
			}
			LOGGER.debug("Delete action triggered for entity type: {} with ID: {}", entity.getClass().getSimpleName(), entity.getId());
			// Show confirmation dialog
			CNotificationService.showConfirmationDialog("Delete selected item?", () -> {
				try {
					@SuppressWarnings ({
							"rawtypes"
					})
					final CAbstractService service = getServiceForEntity(entity);
					service.delete(entity.getId());
					LOGGER.info("Entity deleted successfully with ID: {} of type: {}", entity.getId(), entity.getClass().getSimpleName());
					// Clear current entity
					currentActualEntity = null;
					previousActualEntity = null;
					// Refresh the grid to reload data after deletion
					try {
						view.refreshGrid();
					} catch (final Exception e) {
						LOGGER.error("Error refreshing grid after delete: {}", e.getMessage(), e);
					}
					// Select first item in grid after deletion
					view.selectFirstInGrid();
					CNotificationService.showDeleteSuccess();
				} catch (final Exception ex) {
					CNotificationService.showException("Error deleting entity with ID:" + entity.getId(), ex);
					LOGGER.error("Error deleting entity: {}", ex.getMessage(), ex);
				}
			});
		} catch (final Exception e) {
			LOGGER.error("Error during delete action: {}", e.getMessage());
			throw e;
		}
	}

	@Override
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	public void actionRefresh() throws Exception {
		try {
			final CProjectItem<?> entity = currentActualEntity;
			LOGGER.debug("Refresh action triggered for entity: {} of type: {}", entity != null ? entity.getId() : "null",
					entity != null ? entity.getClass().getSimpleName() : "null");
			// Check if current entity is a new unsaved entity (no ID)
			if (entity != null && entity.getId() == null) {
				// Discard the new entity and restore previous selection
				if (previousActualEntity != null && previousActualEntity.getId() != null) {
					final CAbstractService service = getServiceForEntity(previousActualEntity);
					final CEntityDB<?> reloaded = (CEntityDB<?>) service.getById(previousActualEntity.getId()).orElse(null);
					if (reloaded != null) {
						currentActualEntity = (CProjectItem<?>) reloaded;
						// Refresh grid and populate form
						try {
							view.refreshGrid();
						} catch (final Exception e) {
							LOGGER.error("Error refreshing grid: {}", e.getMessage(), e);
						}
						view.populateForm();
					} else {
						// previous entity no longer exists, clear selection
						currentActualEntity = null;
						try {
							view.refreshGrid();
						} catch (final Exception e) {
							LOGGER.error("Error refreshing grid: {}", e.getMessage(), e);
						}
						view.selectFirstInGrid();
					}
				} else {
					currentActualEntity = null;
					try {
						view.refreshGrid();
					} catch (final Exception e) {
						LOGGER.error("Error refreshing grid: {}", e.getMessage(), e);
					}
					view.selectFirstInGrid();
				}
				CNotificationService.showInfo("Entity reloaded.");
				return;
			}
			// Normal refresh for existing entities
			if (entity == null) {
				try {
					view.refreshGrid();
				} catch (final Exception e) {
					LOGGER.error("Error refreshing grid: {}", e.getMessage(), e);
				}
				view.selectFirstInGrid();
				return;
			}
			final CAbstractService service = getServiceForEntity(entity);
			final CEntityDB<?> reloaded = (CEntityDB<?>) service.getById(entity.getId()).orElse(null);
			if (reloaded != null) {
				currentActualEntity = (CProjectItem<?>) reloaded;
				// Refresh grid to show latest data
				try {
					view.refreshGrid();
				} catch (final Exception e) {
					LOGGER.error("Error refreshing grid: {}", e.getMessage(), e);
				}
				view.populateForm();
			} else {
				currentActualEntity = null;
				try {
					view.refreshGrid();
				} catch (final Exception e) {
					LOGGER.error("Error refreshing grid: {}", e.getMessage(), e);
				}
				view.selectFirstInGrid();
			}
			CNotificationService.showInfo("Entity reloaded.");
		} catch (final Exception e) {
			LOGGER.error("Error refreshing entity: {}", e.getMessage());
			throw e;
		}
	}

	@Override
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	public void actionSave() throws Exception {
		try {
			final CProjectItem<?> entity = currentActualEntity;
			LOGGER.debug("Save action triggered for entity: {} of type: {}", entity != null ? entity.getId() : "null",
					entity != null ? entity.getClass().getSimpleName() : "null");
			if (entity == null) {
				LOGGER.warn("No current entity for save operation");
				CNotificationService.showWarning("No entity selected for save");
				return;
			}
			// Write binder data to entity if binder is available
			// The view should have an entityBinder for the actual entity
			// We need to cast to access it since it's not in the interface
			if (view instanceof CGridViewBaseGannt) {
				final CGridViewBaseGannt<?> ganttView = (CGridViewBaseGannt<?>) view;
				if (ganttView.getEntityBinder() != null) {
					try {
						ganttView.getEntityBinder().writeBean(entity);
						LOGGER.debug("Binder data written to entity before save");
					} catch (final Exception e) {
						LOGGER.error("Error writing binder data: {}", e.getMessage());
						CNotificationService.showError("Validation failed: " + e.getMessage());
						return;
					}
				}
			}
			// Get the appropriate service for this entity type
			final CAbstractService service = getServiceForEntity(entity);
			final CProjectItem<?> savedEntity = (CProjectItem<?>) service.save(entity);
			LOGGER.info("Entity saved successfully with ID: {} of type: {}", savedEntity.getId(), savedEntity.getClass().getSimpleName());
			// Update current entity with saved version
			currentActualEntity = savedEntity;
			// Let the view know to refresh display
			// cannot set real entity here, as view expects CGanntViewEntity
			view.onEntitySaved(null);
			// set it here for further editing
			// view.setCurrentEntity(savedEntity);
			view.populateForm();
			CNotificationService.showSaveSuccess();
		} catch (final Exception e) {
			LOGGER.error("Error saving entity: {}", e.getMessage(), e);
			CNotificationService.showException("Error saving entity", e);
			throw e;
		}
	}

	/** Gets the current actual entity being edited.
	 * @return The current actual entity (CActivity or CMeeting) */
	public CProjectItem<?> getCurrentActualEntity() { return currentActualEntity; }

	/** Gets the appropriate service for the current entity type.
	 * @param entity The entity to get the service for
	 * @return The service for the entity type */
	@SuppressWarnings ("unchecked")
	private <T extends CProjectItem<T>> CAbstractService<T> getServiceForEntity(final CProjectItem<?> entity) {
		Check.notNull(entity, "Entity cannot be null when getting service");
		if (entity instanceof CActivity) {
			return (CAbstractService<T>) activityService;
		} else if (entity instanceof CMeeting) {
			return (CAbstractService<T>) meetingService;
		} else {
			throw new IllegalArgumentException("Unsupported entity type: " + entity.getClass().getSimpleName());
		}
	}

	/** Updates the current actual entity being edited. This should be called by the view when a Gantt item is selected.
	 * @param entity The actual entity (CActivity or CMeeting) */
	public void setCurrentActualEntity(final CProjectItem<?> entity) {
		currentActualEntity = entity;
	}
}
