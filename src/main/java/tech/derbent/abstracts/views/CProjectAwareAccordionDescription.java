package tech.derbent.abstracts.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.interfaces.CProjectChangeListener;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/**
 * Project-aware accordion description that refreshes content when the active project
 * changes. This base class implements CProjectChangeListener to handle project switching
 * scenarios where panels need to update their content based on the new project context.
 * Layer: View (MVC) Purpose: Extends CAccordionDescription with project change awareness
 * for proper panel refresh
 */
public abstract class CProjectAwareAccordionDescription<
	EntityClass extends CEntityDB<EntityClass>> extends CAccordionDBEntity<EntityClass>
	implements CProjectChangeListener {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CProjectAwareAccordionDescription.class);

	protected final CSessionService sessionService;

	/**
	 * Constructor with custom title for CProjectAwareAccordionDescription.
	 * @param title                custom title for the accordion panel
	 * @param currentEntity        current entity instance
	 * @param beanValidationBinder validation binder
	 * @param entityClass          entity class type
	 * @param entityService        service for the entity
	 * @param sessionService       session service for project change notifications
	 */
	public CProjectAwareAccordionDescription(final String title,
		final EntityClass currentEntity,
		final CEnhancedBinder<EntityClass> beanValidationBinder,
		final Class<EntityClass> entityClass,
		final CAbstractService<EntityClass> entityService,
		final CSessionService sessionService) {
		super(title, currentEntity, beanValidationBinder, entityClass, entityService);
		this.sessionService = sessionService;
		LOGGER.debug("Created project-aware accordion panel with title '{}': {}", title,
			getClass().getSimpleName());
	}

	/**
	 * Gets the session service for subclasses to use.
	 * @return the session service
	 */
	public CSessionService getSessionService() { return sessionService; }

	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);

		// Register for project change notifications
		if (sessionService != null) {
			sessionService.addProjectChangeListener(this);
			LOGGER.debug("Registered project change listener for panel: {}",
				getClass().getSimpleName());
		}
	}

	@Override
	protected void onDetach(final DetachEvent detachEvent) {
		super.onDetach(detachEvent);

		// Unregister to prevent memory leaks
		if (sessionService != null) {
			sessionService.removeProjectChangeListener(this);
			LOGGER.debug("Unregistered project change listener for panel: {}",
				getClass().getSimpleName());
		}
	}

	/**
	 * Called when the active project changes. Implementations should refresh their
	 * content to reflect the new project context.
	 * @param newProject The newly selected project, or null if no project is active
	 */
	@Override
	public void onProjectChanged(final CProject newProject) {
		LOGGER.debug("Project change notification received in panel {}: {}",
			getClass().getSimpleName(),
			newProject != null ? newProject.getName() : "null");

		// If no entity is currently selected, clear the panel content
		if (getCurrentEntity() == null) {
			LOGGER.debug("No entity selected, clearing panel content for project change");
			refreshPanelForProjectChange(newProject);
		}
		else // Entity is selected - check if it belongs to the new project
		if (shouldRefreshForProject(getCurrentEntity(), newProject)) {
			LOGGER.debug("Entity project context changed, refreshing panel content");
			refreshPanelForProjectChange(newProject);
		}
	}

	/**
	 * Refreshes the panel content for a project change. Default implementation clears the
	 * base layout and recreates content. Subclasses can override for custom behavior.
	 * @param newProject the newly selected project
	 */
	protected void refreshPanelForProjectChange(final CProject newProject) {
		LOGGER.debug("Refreshing panel content for project change: {}",
			newProject != null ? newProject.getName() : "null");

		try {
			// Clear existing content
			removeAllFromContent();

			// Recreate content if entity is available
			if (getCurrentEntity() != null) {
				createPanelContent();
			}
		} catch (final Exception e) {
			LOGGER.error("Error refreshing panel content for project change in {}: {}",
				getClass().getSimpleName(), e.getMessage(), e);
		}
	}

	/**
	 * Determines if the panel should refresh based on the entity and new project. Default
	 * implementation always returns true. Subclasses can override for specific logic.
	 * @param entity     the current entity
	 * @param newProject the newly selected project
	 * @return true if the panel should refresh
	 */
	public boolean shouldRefreshForProject(final EntityClass entity,
		final CProject newProject) {
		// Default: always refresh on project change
		return true;
	}
}