package tech.derbent.api.services.pageservice;

import org.slf4j.Logger;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.view.CDynamicPageBase;
import tech.derbent.base.session.service.ISessionService;

public abstract class CPageService<EntityClass extends CEntityDB<EntityClass>> {

	Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CPageService.class);
	final protected CDynamicPageBase view;

	public CPageService(CDynamicPageBase view) {
		this.view = view;
	}

	public void actionCreate() {
		try {
			LOGGER.debug("Create action triggered for entity type: {}", getEntityClass().getSimpleName());
			// Create and initialize new entity using service
			final EntityClass newEntity = getEntityService().newEntity();
			getEntityService().initializeNewEntity(newEntity);
			// Set current entity and populate form
			setCurrentEntity(newEntity);
			view.populateForm();
			getNotificationService().showSuccess("New " + getEntityClass().getSimpleName() + " created. Fill in the details and click Save.");
		} catch (final Exception e) {
			LOGGER.error("Error creating new entity instance for type: {} - {}", getEntityClass().getSimpleName(), e.getMessage());
			LOGGER.error("exception:", e);
			getNotificationService().showError("Error creating new entity: " + e.getMessage());
		}
	}

	public void actionDelete() {
		try {
			final EntityClass entity = getCurrentEntity();
			LOGGER.debug("Delete action triggered for entity: {}", entity != null ? entity.getId() : "null");
			if (entity == null || entity.getId() == null) {
				getNotificationService().showWarning("Please select an item to delete.");
				return;
			}
			// Show confirmation dialog
			getNotificationService().showConfirmationDialog("Delete selected item?", () -> {
				try {
					getEntityService().delete(entity.getId());
					LOGGER.info("Entity deleted successfully with ID: {}", entity.getId());
					// Clear current entity
					setCurrentEntity(null);
					// Refresh grid and form
					view.refreshGrid();
					view.populateForm();
					// Show success notification
					getNotificationService().showDeleteSuccess();
				} catch (final Exception ex) {
					LOGGER.error("Error deleting entity with ID: {}", entity.getId(), ex);
					getNotificationService().showDeleteError();
				}
			});
		} catch (final Exception e) {
			LOGGER.error("Unexpected error during delete action", e);
			getNotificationService().showError("Failed to delete item: " + e.getMessage());
		}
	}

	public void actionRefresh() {
		try {
			final EntityClass entity = getCurrentEntity();
			LOGGER.debug("Refresh action triggered for entity: {}", entity != null ? entity.getId() : "null");
			if (entity == null || entity.getId() == null) {
				getNotificationService().showWarning("Please select an item to refresh.");
				return;
			}
			final CEntityDB<?> reloaded = getEntityService().getById(entity.getId()).orElse(null);
			if (reloaded != null) {
				view.onEntityRefreshed(reloaded);
				getNotificationService().showInfo("Entity refreshed successfully");
				LOGGER.info("Entity refreshed successfully with ID: {}", reloaded.getId());
			} else {
				getNotificationService().showWarning("Entity not found. It may have been deleted.");
				LOGGER.warn("Entity with ID {} not found during refresh", entity.getId());
			}
		} catch (final Exception e) {
			LOGGER.error("Error refreshing entity: {}", e.getMessage(), e);
			getNotificationService().showError("Failed to refresh entity: " + e.getMessage());
		}
	}

	public void actionSave() {
		try {
			final EntityClass entity = getCurrentEntity();
			LOGGER.debug("Save action triggered for entity: {}", entity != null ? entity.getId() : "null");
			if (entity == null) {
				LOGGER.warn("No current entity for save operation");
				getNotificationService().showWarning("No entity selected for save");
				return;
			}
			// Write form data to entity using binder
			if (view.getBinder() != null) {
				view.getBinder().writeBean(entity);
			}
			// Save entity
			final EntityClass savedEntity = getEntityService().save(entity);
			LOGGER.info("Entity saved successfully with ID: {}", savedEntity.getId());
			// Update current entity with saved version (includes generated ID)
			setCurrentEntity(savedEntity);
			// Notify view that entity was saved (triggers grid refresh and selection)
			view.onEntitySaved(savedEntity);
			// Populate form with saved entity
			view.populateForm();
			// Show success notification
			getNotificationService().showSaveSuccess();
		} catch (final org.springframework.orm.ObjectOptimisticLockingFailureException exception) {
			LOGGER.error("Optimistic locking failure during save", exception);
			getNotificationService().showOptimisticLockingError();
		} catch (final com.vaadin.flow.data.binder.ValidationException validationException) {
			LOGGER.error("Validation error during save", validationException);
			getNotificationService().showWarning("Failed to save the data. Please check that all required fields are filled and values are valid.");
		} catch (final Exception exception) {
			LOGGER.error("Unexpected error during save operation", exception);
			getNotificationService().showError("An unexpected error occurred while saving: " + exception.getMessage());
		}
	}

	public void bind() {}

	@SuppressWarnings ("unchecked")
	private EntityClass getCurrentEntity() { return (EntityClass) view.getCurrentEntity(); }

	protected Class<?> getEntityClass() { return view.getEntityClass(); }

	@SuppressWarnings ("unchecked")
	protected CAbstractService<EntityClass> getEntityService() {
		Check.notNull(view, "View is not set in page service");
		return (CAbstractService<EntityClass>) view.getEntityService();
	}

	protected CNotificationService getNotificationService() { return view.getNotificationService(); }

	protected ISessionService getSessionService() { return view.getSessionService(); }

	protected void setCurrentEntity(EntityClass entity) {
		view.setCurrentEntity(entity);
	}
}
