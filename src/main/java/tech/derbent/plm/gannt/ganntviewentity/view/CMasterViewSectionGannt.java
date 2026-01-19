package tech.derbent.plm.gannt.ganntviewentity.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.view.CAbstractEntityDBPage;
import tech.derbent.api.grid.view.CMasterViewSectionBase;
import tech.derbent.api.interfaces.IProjectChangeListener;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.gannt.ganntviewentity.view.components.CGanntGrid;
import tech.derbent.plm.meetings.service.CMeetingService;
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

	@Override
	public void createMasterView() throws Exception {
		LOGGER.debug("Creating Gantt chart master view");
		CProject<?> currentProject = null;
		currentProject = sessionService.getActiveProject().orElse(null);
		grid = new CGanntGrid(currentProject, activityService, meetingService, pageEntityService);
		grid.asSingleSelect().addValueChangeListener(this::onSelectionChange);
		add(grid);
		refreshMasterView();
	}

	/** Gets the Gantt grid instance for direct access to grid operations.
	 * @return The Gantt grid */
	public CGanntGrid getGrid() { return grid; }

	@Override
	public EntityClass getSelectedItem() {
		LOGGER.debug("Getting selected item from Gantt chart");
		// CGanttItem is selected in the grid, but we return null since it's a DTO wrapper
		// The actual entity (Activity/Meeting) should be accessed via CGanttItem.getEntity()
		return null;
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
	public void onProjectChanged(final CProject<?> newProject) throws Exception {
		LOGGER.debug("Project change notification received: {}", newProject != null ? newProject.getName() : "null");
		try {
			refreshMasterView();
		} catch (final Exception e) {
			LOGGER.error("Error refreshing Gantt chart on project change: {}", e.getMessage(), e);
			CNotificationService.showErrorDialog(e);
		}
	}

	@SuppressWarnings ("unchecked")
	protected void onSelectionChange(final ValueChangeEvent<?> event) {
		LOGGER.debug("Gantt chart selection changed: {}", event.getValue() != null ? event.getValue().toString() : "null");
		final EntityClass value = (EntityClass) event.getValue();
		fireEvent(new SelectionChangeEvent<>(this, value));
	}

	@Override
	public void refreshMasterView() throws Exception {
		try {
			LOGGER.debug("Refreshing Gantt chart master view");
			removeAllButGrid();
			// add(CSOGanntChart.createGanttChart());
		} catch (final Exception e) {
			LOGGER.error("Error creating Gantt grid for project: {}", e.getMessage(), e);
			throw e;
		}
	}

	public void removeAllButGrid() {
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
