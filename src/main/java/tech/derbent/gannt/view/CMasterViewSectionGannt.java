package tech.derbent.gannt.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.CProjectChangeListener;
import tech.derbent.api.views.CAbstractEntityDBPage;
import tech.derbent.api.views.grids.CMasterViewSectionBase;
import tech.derbent.gannt.view.components.CGanntGrid;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

public class CMasterViewSectionGannt<EntityClass extends CEntityDB<EntityClass>> extends CMasterViewSectionBase<EntityClass>
		implements CProjectChangeListener {

	// --- Custom Event Definition ---
	public static class SelectionChangeEvent<T extends CEntityDB<T>> extends ComponentEvent<CMasterViewSectionGannt<T>> {

		private static final long serialVersionUID = 1L;
		private final T selectedItem;

		public SelectionChangeEvent(final CMasterViewSectionGannt<T> source, final T selectedItem) {
			super(source, false);
			this.selectedItem = selectedItem;
		}

		public T getSelectedItem() { return selectedItem; }
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CMasterViewSectionGannt.class);
	private static final long serialVersionUID = 1L;
	private final CActivityService activityService;
	private CGanntGrid ganttGrid;
	private final CMeetingService meetingService;
	private final CSessionService sessionService;

	public CMasterViewSectionGannt(final Class<EntityClass> entityClass, final CAbstractEntityDBPage<EntityClass> page,
			final CSessionService sessionService, final CActivityService activityService, final CMeetingService meetingService) {
		super(entityClass, page);
		this.sessionService = sessionService;
		this.activityService = activityService;
		this.meetingService = meetingService;
		LOGGER.debug("Initializing CMasterViewSectionGannt for entity: {}", entityClass.getSimpleName());
		createMasterView();
	}

	@Override
	public void createMasterView() {
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
		// Register this component to receive project change notifications
		sessionService.addProjectChangeListener(this);
		LOGGER.debug("Registered CMasterViewSectionGannt as project change listener");
	}

	@Override
	protected void onDetach(final DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		// Unregister this component to prevent memory leaks
		sessionService.removeProjectChangeListener(this);
		LOGGER.debug("Unregistered CMasterViewSectionGannt from project change notifications");
	}

	/** Implementation of CProjectChangeListener interface. Called when the active project changes via the SessionService.
	 * @param newProject The newly selected project */
	@Override
	public void onProjectChanged(final CProject newProject) {
		LOGGER.debug("Project change notification received: {}", newProject != null ? newProject.getName() : "null");
		refreshMasterView();
	}

	@SuppressWarnings ("unchecked")
	protected void onSelectionChange(final ValueChangeEvent<?> event) {
		LOGGER.debug("Gantt chart selection changed: {}", event.getValue() != null ? event.getValue().toString() : "null");
		final EntityClass value = (EntityClass) event.getValue();
		fireEvent(new SelectionChangeEvent<>(this, value));
	}

	@Override
	public void refreshMasterView() {
		LOGGER.debug("Refreshing Gantt chart master view");
		// Get current project from session (with null safety)
		CProject currentProject = null;
		try {
			if (sessionService != null) {
				currentProject = sessionService.getActiveProject().orElse(null);
			}
		} catch (final Exception e) {
			LOGGER.error("Error getting active project from session", e);
		}
		// Remove existing grid if present
		if (ganttGrid != null) {
			remove(ganttGrid);
			ganttGrid = null;
		}
		if (currentProject == null) {
			LOGGER.debug("No active project, showing empty Gantt chart");
			return;
		}
		// Check if required services are available
		if (activityService == null || meetingService == null) {
			LOGGER.warn("Required services not available for Gantt chart (activityService: {}, meetingService: {})", activityService != null,
					meetingService != null);
			return;
		}
		// Create and display new Gantt grid for current project
		try {
			ganttGrid = new CGanntGrid(currentProject, activityService, meetingService);
			add(ganttGrid);
			LOGGER.debug("Created Gantt grid for project: {}", currentProject.getName());
		} catch (final Exception e) {
			LOGGER.error("Error creating Gantt grid for project: {}", currentProject.getName(), e);
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
	public void setDataProvider(final CallbackDataProvider<EntityClass, Void> masterQuery) {
		LOGGER.debug("Setting data provider for Gantt chart");
		// Gantt chart loads its own data through the service, so this is not used
		refreshMasterView();
	}

	@Override
	public void setItems(final List<EntityClass> filteredMeetings) {
		LOGGER.debug("Setting items with {} items for Gantt chart", filteredMeetings != null ? filteredMeetings.size() : 0);
		// Gantt chart loads its own data through the service, so this is not used
		refreshMasterView();
	}
}
