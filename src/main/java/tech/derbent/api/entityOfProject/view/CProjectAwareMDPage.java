package tech.derbent.api.entityOfProject.view;

import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.entity.service.CEntityNamedService;
import tech.derbent.api.entity.view.CAbstractNamedEntityPage;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.interfaces.IProjectChangeListener;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.ui.component.basic.CVerticalLayoutTop;
import tech.derbent.api.utils.Check;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;

/** Abstract project-aware MD page that filters entities by the currently active project. Implements CProjectChangeListener to receive immediate
 * notifications when the active project changes. */
public abstract class CProjectAwareMDPage<EntityClass extends CEntityOfProject<EntityClass>> extends CAbstractNamedEntityPage<EntityClass>
		implements IProjectChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectAwareMDPage.class);
	private static final long serialVersionUID = 1L;
	protected CProject currentProject;

	protected CProjectAwareMDPage(final Class<EntityClass> entityClass, final CEntityNamedService<EntityClass> entityService,
			final ISessionService sessionService, final CDetailSectionService screenService) throws Exception {
		super(entityClass, entityService, sessionService, screenService);
		// Now that sessionService is set, we can populate the grid
		refreshProjectAwareGrid();
	}

	@Override
	protected EntityClass createNewEntity() {
		final String name = "New Item";
		final CProject<?> project = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No current project set in session"));
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
	 * @param newProject The newly selected project
	 * @throws Exception */
	@Override
	public void onProjectChanged(final CProject newProject) throws Exception {
		if ((currentProject != null) && (newProject != null) && currentProject.getId().equals(newProject.getId())) {
			// No change in project
			return;
		}
		currentProject = newProject;
		LOGGER.debug("Project change notification received: {}", newProject != null ? newProject.getName() : "null");
		refreshProjectAwareGrid();
	}

	/** Refreshes the grid with project-aware data.
	 * @throws Exception */
	protected void refreshProjectAwareGrid() throws Exception {
		LOGGER.debug("Refreshing project-aware grid");
		if ((sessionService == null) || (masterViewSection == null)) {
			// Not fully initialized yet
			return;
		}
		masterViewSection.refreshMasterView();
	}

	/** Sets the project for the entity. */
	public void setProjectForEntity(final EntityClass entity, final CProject<?> project) {
		assert entity != null : "Entity must not be null";
		assert project != null : "Project must not be null";
		Check.instanceOf(entity, CEntityOfProject.class, "Entity must implement CEntityOfProject interface");
		entity.setProject(project);
	}

	@Override
	protected void updateDetailsComponent()
			throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, Exception {
		final CVerticalLayoutTop formLayout = CFormBuilder.buildForm(entityClass, getBinder(), null, this);
		getBaseDetailsLayout().add(formLayout);
	}
}
