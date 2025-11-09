package tech.derbent.app.gannt.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IProjectChangeListener;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CAbstractEntityDBPage;
import tech.derbent.api.views.components.CDiv;
import tech.derbent.api.views.grids.CMasterViewSectionBase;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.gannt.view.components.CGanntGrid;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;

public class CMasterViewSectionGannt<EntityClass extends CEntityDB<EntityClass>> extends CMasterViewSectionBase<EntityClass>
		implements IProjectChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(CMasterViewSectionGannt.class);
	private static final long serialVersionUID = 1L;
	private final CActivityService activityService;
	private CGanntGrid grid;
	private final CMeetingService meetingService;
	private final CPageEntityService pageEntityService;
	private final ISessionService sessionService;

	public CMasterViewSectionGannt(final Class<EntityClass> entityClass, final CAbstractEntityDBPage<EntityClass> page,
			final ISessionService sessionService, final CActivityService activityService, final CMeetingService meetingService,
			final CPageEntityService pageEntityService) throws Exception {
		super(entityClass, page);
		this.sessionService = sessionService;
		this.activityService = activityService;
		this.meetingService = meetingService;
		this.pageEntityService = pageEntityService;
		LOGGER.debug("Initializing CMasterViewSectionGannt for entity: {}", entityClass.getSimpleName());
		createMasterView();
	}

	protected Component createGridToolbar() {
		CDiv toolbar = new CDiv("Gannt Chart Toolbar");
		return toolbar;
	}

	@Override
	public void createMasterView() throws Exception {
		LOGGER.debug("Creating Gantt chart master view");
		refreshMasterView();
	}

	@Override
	public EntityClass getSelectedItem() {
		LOGGER.debug("Getting selected item from Gantt chart");
		return null; // Gantt chart doesn't have traditional selection
	}

	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		sessionService.addProjectChangeListener(this);
		LOGGER.debug("Registered CMasterViewSectionGannt as project change listener");
	}

	@Override
	protected void onDetach(final DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		sessionService.removeProjectChangeListener(this);
		LOGGER.debug("Unregistered CMasterViewSectionGannt from project change notifications");
	}

	/** Implementation of CProjectChangeListener interface. Called when the active project changes via the SessionService.
	 * @param newProject The newly selected project
	 * @throws Exception */
	@Override
	public void onProjectChanged(final CProject newProject) throws Exception {
		LOGGER.debug("Project change notification received: {}", newProject != null ? newProject.getName() : "null");
		refreshMasterView();
	}



	@Override
	public void refreshMasterView() throws Exception {
		try {
			LOGGER.debug("Refreshing Gantt chart master view");
			// Get current project from session (with null safety)
			CProject currentProject = null;
			currentProject = sessionService.getActiveProject().orElse(null);
			Check.notNull(sessionService, "Session service is not available");
			Check.notNull(currentProject, "No active project in session");
			Check.notNull(activityService, "Activity service is not available");
			Check.notNull(meetingService, "Meeting service is not available");
			removeAll();
			// Create and display new Gantt grid for current project
			Component toolbar = createGridToolbar();
			if (toolbar != null) {
				add(toolbar);
			}
			grid = new CGanntGrid(currentProject, activityService, meetingService, pageEntityService);
			// Disable selection on Gantt grid - it's for visualization only
			grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.NONE);
			add(grid);
			// add(CSOGanntChart.createGanttChart());
		} catch (final Exception e) {
			LOGGER.error("Error creating Gantt grid for project: {}", e.getMessage());
			throw e;
		}
	}

	@Override
	public void select(final EntityClass object) {
		LOGGER.debug("Selecting object in Gantt chart: {}", object != null ? object.toString() : "null");
		// Gantt chart doesn't support traditional selection, but we could implement
		// highlighting or scrolling to a specific item in the future
	}

	@Override
	public void selectLastOrFirst(final EntityClass orElse) {
		LOGGER.debug("Selecting last or first in Gantt chart, default: {}", orElse != null ? orElse.toString() : "null");
		// Gantt chart doesn't support traditional selection
	}

	@Override
	public void setDataProvider(final CallbackDataProvider<EntityClass, Void> masterQuery) throws Exception {
		LOGGER.debug("Setting data provider for Gantt chart");
		// Gantt chart loads its own data through the service, so this is not used
		refreshMasterView();
	}

	@Override
	public void setItems(final List<EntityClass> filteredMeetings) throws Exception {
		LOGGER.debug("Setting items with {} items for Gantt chart", filteredMeetings != null ? filteredMeetings.size() : 0);
		// Gantt chart loads its own data through the service, so this is not used
		refreshMasterView();
	}
}
