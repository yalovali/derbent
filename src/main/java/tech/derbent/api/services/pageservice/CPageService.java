package tech.derbent.api.services.pageservice;

import org.slf4j.Logger;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;

public abstract class CPageService<EntityClass extends CEntityDB<EntityClass>> {

	private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CPageService.class);
	private EntityClass previousEntity;
	final protected IPageServiceImplementer<EntityClass> view;

	public CPageService(final IPageServiceImplementer<EntityClass> view) {
		this.view = view;
		setPreviousEntity(null);
	}

	/** Handle status change action triggered from the CRUD toolbar.
	 * <p>
	 * Default implementation for entities without workflow support. Simply sets the new status on the current entity. Subclasses should override this
	 * method to implement workflow-aware status validation.
	 * @param newStatus the new status selected by the user
	 * @throws Exception if the status change fails */
	public void actionChangeStatus(final CProjectItemStatus newStatus) throws Exception {
		LOGGER.debug("Base actionChangeStatus called - entity type does not support workflow status changes");
	}

	public void actionCreate() throws Exception {
		try {
			setPreviousEntity(getCurrentEntity());
			final EntityClass newEntity = getEntityService().newEntity();
			getEntityService().initializeNewEntity(newEntity);
			view.onEntityCreated(newEntity);
		} catch (final Exception e) {
			LOGGER.error("Error creating new entity instance for type: {} - {}", getEntityClass().getSimpleName(), e.getMessage());
			throw e;
		}
	}

	public void actionDelete() throws Exception {
		try {
			final EntityClass entity = getCurrentEntity();
			if (entity == null || entity.getId() == null) {
				CNotificationService.showWarning("Please select an item to delete.");
				return;
			}
			// Show confirmation dialog
			CNotificationService.showConfirmationDialog("Delete selected item?", () -> {
				try {
					getEntityService().delete(entity.getId());
					LOGGER.info("Entity deleted successfully with ID: {}", entity.getId());
					view.onEntityDeleted(entity);
				} catch (final Exception ex) {
					CNotificationService.showException("Error deleting entity with ID:" + entity.getId(), ex);
				}
			});
		} catch (final Exception e) {
			LOGGER.error("Error during delete action: {}", e.getMessage());
			throw e;
		}
	}

	@SuppressWarnings ("unchecked")
	public void actionRefresh() throws Exception {
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
						view.onEntityRefreshed((EntityClass) reloaded);
					} else {
						// previous entity no longer exists, clear selection
						view.selectFirstInGrid();
					}
				} else {
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
			final CEntityDB<?> reloaded = getEntityService().getById(entity.getId()).orElse(null);
			if (reloaded != null) {
				view.onEntityRefreshed((EntityClass) reloaded);
			} else {
				view.selectFirstInGrid();
			}
			CNotificationService.showInfo("Entity reloaded.");
		} catch (final Exception e) {
			LOGGER.error("Error refreshing entity: {}", e.getMessage());
			throw e;
		}
	}

	public void actionSave() throws Exception {
		try {
			final EntityClass entity = getCurrentEntity();
			LOGGER.debug("Save action triggered for entity: {}", entity != null ? entity.getId() : "null");
			if (entity == null) {
				LOGGER.warn("No current entity for save operation");
				CNotificationService.showWarning("No entity selected for save");
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
			CNotificationService.showSaveSuccess();
		} catch (final Exception e) {
			LOGGER.error("Error saving entity: {}", e.getMessage());
			throw e;
		}
	}

	public void bind() {}

	private EntityClass getCurrentEntity() { return view.getCurrentEntity(); }

	protected Class<?> getEntityClass() { return view.getEntityClass(); }

	protected CAbstractService<EntityClass> getEntityService() {
		Check.notNull(view, "View is not set in page service");
		return view.getEntityService();
	}

	public EntityClass getPreviousEntity() { return previousEntity; }

	protected ISessionService getSessionService() { return view.getSessionService(); }

	protected void setCurrentEntity(final EntityClass entity) {
		view.setCurrentEntity(entity);
	}

	public void setPreviousEntity(final EntityClass previousEntity) { this.previousEntity = previousEntity; }
}
