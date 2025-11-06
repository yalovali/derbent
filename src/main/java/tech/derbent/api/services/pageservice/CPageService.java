package tech.derbent.api.services.pageservice;

import org.slf4j.Logger;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.view.CDynamicPageBase;
import tech.derbent.base.session.service.ISessionService;

public abstract class CPageService<EntityClass extends CEntityDB<EntityClass>> {

	private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CPageService.class);
	private EntityClass previousEntity;
	final protected CDynamicPageBase view;

	public CPageService(CDynamicPageBase view) {
		this.view = view;
		setPreviousEntity(null);
	}

	public void actionCreate() {
		try {
			LOGGER.debug("Create action triggered for entity type: {}", getEntityClass().getSimpleName());
			setPreviousEntity(getCurrentEntity());
			final EntityClass newEntity = getEntityService().newEntity();
			getEntityService().initializeNewEntity(newEntity);
			view.onEntityCreated(newEntity);
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
					view.onEntityDeleted(entity);
				} catch (final Exception ex) {
					LOGGER.error("Error deleting entity with ID: {}", entity.getId(), ex);
					getNotificationService().showDeleteError();
					// getNotificationService().showE
				}
			});
		} catch (final Exception e) {
			LOGGER.error("Unexpected error during delete action", e);
			getNotificationService().showError("Failed to delete item: " + e.getMessage());
		}
	}

	@SuppressWarnings ("unchecked")
	public void actionRefresh() {
		try {
			final EntityClass entity = getCurrentEntity();
			LOGGER.debug("Refresh action triggered for entity: {}", entity != null ? entity.getId() : "null");
			// Check if current entity is a new unsaved entity (no ID)
			if (entity != null && entity.getId() == null) {
				// Discard the new entity and restore previous selection
				if (previousEntity != null) {
					final CEntityDB<?> reloaded = getEntityService().getById(previousEntity.getId()).orElse(null);
					if (reloaded != null) {
						setCurrentEntity((EntityClass) reloaded);
						view.onEntityRefreshed(reloaded);
					} else {
						// previous entity no longer exists, clear selection
						view.selectFirstInGrid();
					}
				} else {
					view.selectFirstInGrid();
				}
				getNotificationService().showInfo("Entity reloaded.");
				return;
			}
			// Normal refresh for existing entities
			if (entity == null) {
				view.selectFirstInGrid();
				return;
			}
			final CEntityDB<?> reloaded = getEntityService().getById(entity.getId()).orElse(null);
			if (reloaded != null) {
				view.onEntityRefreshed(reloaded);
			} else {
				view.selectFirstInGrid();
			}
			getNotificationService().showInfo("Entity reloaded.");
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
			if (view.getBinder() != null) {
				view.getBinder().writeBean(entity);
			}
			final EntityClass savedEntity = getEntityService().save(entity);
			LOGGER.info("Entity saved successfully with ID: {}", savedEntity.getId());
			setCurrentEntity(savedEntity);
			view.onEntitySaved(savedEntity);
			view.populateForm();
			getNotificationService().showSaveSuccess();
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

	public EntityClass getPreviousEntity() { return previousEntity; }

	protected ISessionService getSessionService() { return view.getSessionService(); }

	protected void setCurrentEntity(EntityClass entity) {
		view.setCurrentEntity(entity);
	}

	public void setPreviousEntity(EntityClass previousEntity) { this.previousEntity = previousEntity; }
}
