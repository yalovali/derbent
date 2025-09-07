package tech.derbent.base.ui;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.abstracts.domains.IDisplayView;
import tech.derbent.abstracts.interfaces.CKanbanEntity;
import tech.derbent.abstracts.interfaces.CKanbanService;
import tech.derbent.abstracts.interfaces.CKanbanStatus;
import tech.derbent.abstracts.interfaces.CProjectChangeListener;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/** CBaseKanbanBoardView - Abstract base class for Kanban board views. Layer: Base View (MVC) Provides common functionality for any kanban board
 * implementation. Handles project awareness, layout setup, and basic kanban operations.
 * @param <T> the type of entity displayed in this kanban board
 * @param <S> the type of status used for organizing entities */
public abstract class CBaseKanbanBoardView<T extends CKanbanEntity, S extends CKanbanStatus> extends VerticalLayout
		implements CProjectChangeListener, IDisplayView {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBaseKanbanBoardView.class);
	protected final CKanbanService<T, S> kanbanService;
	protected final CSessionService sessionService;
	protected H2 titleElement;
	protected HorizontalLayout kanbanContainer;
	protected Div emptyStateContainer;

	/** Constructor for CBaseKanbanBoardView.
	 * @param kanbanService  the service for data operations
	 * @param sessionService the session service for project context */
	protected CBaseKanbanBoardView(final CKanbanService<T, S> kanbanService, final CSessionService sessionService) {
		Check.notNull(kanbanService, "KanbanService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		this.kanbanService = kanbanService;
		this.sessionService = sessionService;
		initializeView();
		loadKanbanData();
	}

	/** Creates a kanban column for the given status and entities. Subclasses must implement this to create specific column types.
	 * @param status   the status for this column
	 * @param entities the entities for this column
	 * @return the created kanban column */
	protected abstract CBaseKanbanColumn<T, S> createKanbanColumn(S status, List<T> entities);
	/** Gets the CSS class name for this kanban board. Subclasses should override this to provide specific styling.
	 * @return the CSS class name */
	protected abstract String getBoardCssClass();
	/** Gets the title for this kanban board. Subclasses should override this to provide specific titles.
	 * @return the board title */
	protected abstract String getBoardTitle();

	/** Initializes the view components and layout. */
	private void initializeView() {
		// Set CSS class for styling
		addClassName(getBoardCssClass());
		// Configure layout
		setSizeFull();
		setPadding(true);
		setSpacing(true);
		// Create title
		titleElement = new H2(getBoardTitle());
		titleElement.addClassName("kanban-board-title");
		// Create kanban container
		kanbanContainer = new HorizontalLayout();
		kanbanContainer.addClassName("kanban-container");
		kanbanContainer.setSizeFull();
		kanbanContainer.setSpacing(true);
		kanbanContainer.setPadding(false);
		// Create empty state container
		emptyStateContainer = new Div();
		emptyStateContainer.addClassName("kanban-empty-state");
		emptyStateContainer.add(new H2("No items found"));
		emptyStateContainer.add(new Div("Create some items to see them on the kanban board."));
		emptyStateContainer.setVisible(false);
		// Add components to view
		add(titleElement, kanbanContainer, emptyStateContainer);
		// Set expand ratio to make kanban container take most space
		setFlexGrow(1, kanbanContainer);
	}

	/** Loads and displays kanban data for the current project. */
	protected void loadKanbanData() {
		final Optional<CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isEmpty()) {
			LOGGER.warn("No active project found for kanban board");
			showEmptyState("No active project selected");
			return;
		}
		final CProject project = activeProject.get();
		LOGGER.info("Loading kanban data for project: {}", project.getName());
		try {
			// Update title with project name
			titleElement.setText(getBoardTitle() + " - " + project.getName());
			// Get entities grouped by status
			final Map<S, List<T>> entitiesByStatus = kanbanService.getEntitiesGroupedByStatus(project.getId());
			if (entitiesByStatus.isEmpty()) {
				showEmptyState("No items found for this project");
				return;
			}
			// Clear container and show kanban columns
			kanbanContainer.removeAll();
			emptyStateContainer.setVisible(false);
			kanbanContainer.setVisible(true);
			// Create column for each status
			for (final Map.Entry<S, List<T>> entry : entitiesByStatus.entrySet()) {
				final S status = entry.getKey();
				final List<T> entities = entry.getValue();
				LOGGER.debug("Creating column for status: {} with {} entities", status.getName(), entities.size());
				final CBaseKanbanColumn<T, S> column = createKanbanColumn(status, entities);
				// Set up drag and drop handling
				column.setStatusUpdateHandler(this::onEntityStatusUpdated);
				kanbanContainer.add(column);
				kanbanContainer.setFlexGrow(1, column);
			}
		} catch (final Exception e) {
			LOGGER.error("Error loading kanban data for project: {}", project.getName(), e);
			showEmptyState("Error loading items: " + e.getMessage());
		}
	}

	/** Called when the component is attached to the UI. Registers the project change listener. */
	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		sessionService.addProjectChangeListener(this);
	}

	/** Called when the component is detached from the UI. Unregisters the project change listener to prevent memory leaks. */
	@Override
	protected void onDetach(final DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		sessionService.removeProjectChangeListener(this);
	}

	/** Handles when an entity status is updated via drag and drop. Subclasses can override this to add custom behavior.
	 * @param entity    the entity that was moved
	 * @param newStatus the new status */
	protected void onEntityStatusUpdated(final T entity, final S newStatus) {
		try {
			kanbanService.updateEntityStatus(entity, newStatus);
			// Refresh the board to reflect changes
			loadKanbanData();
		} catch (final Exception e) {
			LOGGER.error("Error updating entity status: {}", e.getMessage(), e);
			// You could show a notification here
		}
	}

	/** Implementation of CProjectChangeListener interface. Called when the active project changes via the SessionService.
	 * @param newProject the newly selected project */
	@Override
	public void onProjectChanged(final CProject newProject) {
		LOGGER.debug("Project change notification received in kanban board: {}", newProject != null ? newProject.getName() : "null");
		loadKanbanData();
	}

	/** Shows empty state with given message.
	 * @param message the message to display */
	private void showEmptyState(final String message) {
		kanbanContainer.setVisible(false);
		emptyStateContainer.removeAll();
		emptyStateContainer.add(new H2("No Items"));
		emptyStateContainer.add(new Div(message));
		emptyStateContainer.setVisible(true);
	}
}
