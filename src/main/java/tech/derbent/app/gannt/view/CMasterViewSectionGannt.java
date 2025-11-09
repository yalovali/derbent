package tech.derbent.app.gannt.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IProjectChangeListener;
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
		CProject currentProject = null;
		currentProject = sessionService.getActiveProject().orElse(null);
		grid = new CGanntGrid(currentProject, activityService, meetingService, pageEntityService);
		// Gantt chart uses CGanttItem DTO, not actual entities, so selection is disabled to prevent binding errors
		// CGanttItem is a read-only display wrapper and should not trigger detail view editing
		grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.NONE);
		add(grid);
		refreshMasterView();
	}

	@Override
	public EntityClass getSelectedItem() {
		LOGGER.debug("Getting selected item from Gantt chart");
		return null; // Gantt chart doesn't support selection - CGanttItem is a DTO, not an editable entity
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
		try {
			refreshMasterView();
		} catch (final Exception e) {
			LOGGER.error("Error refreshing Gantt chart on project change: {}", e.getMessage(), e);
			// USER ENTRY POINT: Display exception to user
			tech.derbent.api.ui.notifications.CNotifications.showErrorDialog(e);
		}
	}

	@SuppressWarnings ("unchecked")
	protected void onSelectionChange(final ValueChangeEvent<?> event) {
		// NOTE: This method is no longer used as selection is disabled in Gantt grid
		// CGanttItem is a DTO wrapper, not an editable entity, so selection would cause binding errors
		LOGGER.debug("Gantt chart selection changed (deprecated): {}", event.getValue() != null ? event.getValue().toString() : "null");
	}

	@Override
	public void refreshMasterView() throws Exception {
		try {
			LOGGER.debug("Refreshing Gantt chart master view");
			removeAllButGrid();
			// Create and display new Gantt grid for current project
			Component toolbar = createGridToolbar();
			if (toolbar != null) {
				add(toolbar);
			}
			// add(CSOGanntChart.createGanttChart());
		} catch (final Exception e) {
			LOGGER.error("Error creating Gantt grid for project: {}", e.getMessage(), e);
			throw e;
		}
	}

	public void removeAllButGrid() {
		// iteraate over all components and remove all but the grid
		getChildren().forEach(component -> {
			if (component != grid) {
				remove(component);
			}
		});
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
