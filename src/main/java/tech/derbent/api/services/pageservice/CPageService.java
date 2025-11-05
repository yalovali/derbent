package tech.derbent.api.services.pageservice;

import org.slf4j.Logger;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.view.CDynamicPageBase;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

public abstract class CPageService<EntityClass extends CEntityDB<EntityClass>> {

	Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CPageService.class);
	final protected CDynamicPageBase view;

	public CPageService(CDynamicPageBase view) {
		this.view = view;
	}

	@SuppressWarnings ("unchecked")
	public void actionCreate() {
		try {
			LOGGER.debug("Create action triggered for entity type: {}", getEntityClass().getSimpleName());
			// Create new instance using reflection
			final EntityClass newEntity = (EntityClass) getEntityClass().getDeclaredConstructor().newInstance();
			// Set project if the entity supports it (check for CEntityOfProject)
			if (newEntity instanceof CEntityOfProject) {
				((CEntityOfProject<?>) newEntity).setProject(getSessionService().getActiveProject().orElse(null));
			}
			// Special handling for CUser entities - create project association through CUserProjectSettings
			else if (newEntity instanceof CUser) {
				final CProject activeProject = getSessionService().getActiveProject().orElse(null);
				if (activeProject != null) {
					final CUser user = (CUser) newEntity;
					// Initialize project settings list to establish project context for display
					if (user.getProjectSettings() == null) {
						user.setProjectSettings(new java.util.ArrayList<>());
					}
					// Note: The actual CUserProjectSettings creation will be handled when the user is saved
					// This just ensures the user has the project context for dynamic page display
					LOGGER.debug("CUser entity created in context of project: {}", activeProject.getName());
				}
			}
			setCurrentEntity(newEntity);
			getNotificationService().showSuccess("New " + getEntityClass().getSimpleName() + " created. Fill in the details and click Save.");
		} catch (final Exception e) {
			LOGGER.error("Error creating new entity instance for type: {} - {}", getEntityClass().getSimpleName(), e.getMessage());
			getNotificationService().showError("Error creating new entity: " + e.getMessage());
		}
	}

	public void actionDelete() {
		LOGGER.debug("Delete action triggered");
	}

	public void actionRefresh() {
		try {
			if (getCurrentEntity() != null && ((CEntityDB<?>) getCurrentEntity()).getId() != null) {
				CEntityDB<?> reloaded = getEntityService().getById(((CEntityDB<?>) getCurrentEntity()).getId()).orElse(null);
				if (reloaded != null) {
					view.onEntityRefreshed(reloaded);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error refreshing entity: {}", e.getMessage());
		}
	}

	public void actionSave() {
		LOGGER.debug("Save action triggered");
	}

	public void bind() {}

	@SuppressWarnings ("unchecked")
	private EntityClass getCurrentEntity() { // TODO Auto-generated method stub
		return (EntityClass) view.getCurrentEntity();
	}

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
