package tech.derbent.api.views;

import java.lang.reflect.InvocationTargetException;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.interfaces.IProjectChangeListener;
import tech.derbent.api.services.CAbstractNamedEntityService;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.views.components.CVerticalLayout;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.CSessionService;

/** Abstract project-aware MD page that filters entities by the currently active project. Implements CProjectChangeListener to receive immediate
 * notifications when the active project changes. */
public abstract class CProjectAwareMDPage<EntityClass extends CEntityOfProject<EntityClass>> extends CAbstractNamedEntityPage<EntityClass>
		implements IProjectChangeListener {

	private static final long serialVersionUID = 1L;
	protected final CSessionService sessionService;

	protected CProjectAwareMDPage(final Class<EntityClass> entityClass, final CAbstractNamedEntityService<EntityClass> entityService,
			final CSessionService sessionService, final CDetailSectionService screenService) {
		super(entityClass, entityService, sessionService, screenService);
		this.sessionService = sessionService;
		// Now that sessionService is set, we can populate the grid
		refreshProjectAwareGrid();
	}

	@Override
	protected EntityClass createNewEntity() {
		final String name = "New Item";
		final CProject project = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No current project set in session"));
		return ((CEntityOfProjectService<EntityClass>) entityService).newEntity(name, project);
	}

	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		// Register this component to receive project change notifications
		sessionService.addProjectChangeListener(this);
	}

	/** Called when the component is detached from the UI. Unregisters the project change listener to prevent memory leaks. */
	@Override
	protected void onDetach(final DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		// Unregister this component to prevent memory leaks
		sessionService.removeProjectChangeListener(this);
	}

	/** Implementation of CProjectChangeListener interface. Called when the active project changes via the SessionService.
	 * @param newProject The newly selected project */
	@Override
	public void onProjectChanged(final CProject newProject) {
		LOGGER.debug("Project change notification received: {}", newProject != null ? newProject.getName() : "null");
		refreshProjectAwareGrid();
	}

	/** Refreshes the grid with project-aware data. */
	protected void refreshProjectAwareGrid() {
		LOGGER.debug("Refreshing project-aware grid");
		if ((sessionService == null) || (masterViewSection == null)) {
			// Not fully initialized yet
			return;
		}
		masterViewSection.refreshMasterView();
	}

	/** Sets the project for the entity. */
	public void setProjectForEntity(final EntityClass entity, final CProject project) {
		assert entity != null : "Entity must not be null";
		assert project != null : "Project must not be null";
		if (entity instanceof CEntityOfProject) {
			entity.setProject(project);
		} else {
			throw new IllegalArgumentException("Entity must implement CEntityOfProject interface");
		}
	}

	@Override
	protected void updateDetailsComponent()
			throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, Exception {
		final CVerticalLayout formLayout = CFormBuilder.buildForm(entityClass, getBinder());
		getBaseDetailsLayout().add(formLayout);
	}
}
