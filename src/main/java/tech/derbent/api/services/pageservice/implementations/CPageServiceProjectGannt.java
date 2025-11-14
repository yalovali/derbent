package tech.derbent.api.services.pageservice.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.gannt.domain.CGanntViewEntity;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.service.CMeetingService;

public class CPageServiceProjectGannt extends CPageServiceDynamicPage<CGanntViewEntity> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectGannt.class);
	private final CActivityService activityService;
	// The current actual entity being edited (CActivity or CMeeting)
	private CProjectItem<?> currentActualEntity;
	private final CMeetingService meetingService;
	// The previous actual entity for refresh operations
	private CProjectItem<?> previousActualEntity;

	public CPageServiceProjectGannt(IPageServiceImplementer<CGanntViewEntity> view, CActivityService activityService,
			CMeetingService meetingService) {
		super(view);
		this.activityService = activityService;
		this.meetingService = meetingService;
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
			// Note: The view will handle updating the display
			// We can't call view.onEntityCreated with the new entity directly
			// because the view expects CGanntViewEntity
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
					CAbstractService service = getServiceForEntity(entity);
					service.delete(entity.getId());
					LOGGER.info("Entity deleted successfully with ID: {} of type: {}", entity.getId(), entity.getClass().getSimpleName());
					// Clear current entity
					currentActualEntity = null;
					// Refresh the grid/view
					view.selectFirstInGrid();
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
					CAbstractService service = getServiceForEntity(previousActualEntity);
					final CEntityDB<?> reloaded = (CEntityDB<?>) service.getById(previousActualEntity.getId()).orElse(null);
					if (reloaded != null) {
						currentActualEntity = (CProjectItem<?>) reloaded;
						// Let the view know to refresh display
						view.populateForm();
					} else {
						// previous entity no longer exists, clear selection
						currentActualEntity = null;
						view.selectFirstInGrid();
					}
				} else {
					currentActualEntity = null;
					view.selectFirstInGrid();
				}
				CNotificationService.showInfo("Entity reloaded.");
				return;
			}
			// Normal refresh for existing entities
			if (entity == null) {
				view.selectFirstInGrid();
				return;
			}
			CAbstractService service = getServiceForEntity(entity);
			final CEntityDB<?> reloaded = (CEntityDB<?>) service.getById(entity.getId()).orElse(null);
			if (reloaded != null) {
				currentActualEntity = (CProjectItem<?>) reloaded;
				view.populateForm();
			} else {
				currentActualEntity = null;
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
			if (view instanceof tech.derbent.api.ui.CGridViewBaseGannt) {
				tech.derbent.api.ui.CGridViewBaseGannt<?> ganttView = (tech.derbent.api.ui.CGridViewBaseGannt<?>) view;
				if (ganttView.getEntityBinder() != null) {
					try {
						ganttView.getEntityBinder().writeBean(entity);
						LOGGER.debug("Binder data written to entity before save");
					} catch (Exception e) {
						LOGGER.error("Error writing binder data: {}", e.getMessage());
						CNotificationService.showError("Validation failed: " + e.getMessage());
						return;
					}
				}
			}
			// Get the appropriate service for this entity type
			CAbstractService service = getServiceForEntity(entity);
			final CProjectItem<?> savedEntity = (CProjectItem<?>) service.save(entity);
			LOGGER.info("Entity saved successfully with ID: {} of type: {}", savedEntity.getId(), savedEntity.getClass().getSimpleName());
			// Update current entity with saved version
			currentActualEntity = savedEntity;
			// Let the view know to refresh display
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
	private <T extends CProjectItem<T>> CAbstractService<T> getServiceForEntity(CProjectItem<?> entity) {
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
	public void setCurrentActualEntity(CProjectItem<?> entity) {
		currentActualEntity = entity;
	}
}
