package tech.derbent.kanban.view;

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
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.interfaces.CProjectChangeListener;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/**
 * CActivityKanbanBoardView - Main Kanban board view for displaying activities. Layer: View (MVC) Displays all
 * activities of the current project grouped by activity status in a Kanban-style layout. Each activity status forms a
 * column containing activity cards grouped by type. Implements project awareness and real-time updates.
 */
@Route("activities-kanban")
@PageTitle("Activity Kanban Board")
@Menu(order = 1.2, icon = "class:tech.derbent.kanban.view.CActivityKanbanBoardView", title = "Project.Kanban")
@PermitAll
public class CActivityKanbanBoardView extends VerticalLayout implements CProjectChangeListener, CInterfaceIconSet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityKanbanBoardView.class);

    public static String getIconColorCode() {
        return "#fd7e14"; // Orange color for Kanban boards
    }

    public static String getIconFilename() {
        return "vaadin:dashboard";
    }

    private final CActivityService activityService;

    private final CSessionService sessionService;

    private H2 titleElement;

    private HorizontalLayout kanbanContainer;

    private Div emptyStateContainer;

    /**
     * Constructor for CActivityKanbanBoardView.
     * 
     * @param activityService
     *            the activity service for data operations
     * @param sessionService
     *            the session service for project context
     */
    public CActivityKanbanBoardView(final CActivityService activityService, final CSessionService sessionService) {
        LOGGER.info("Initializing CActivityKanbanBoardView");

        if (activityService == null) {
            throw new IllegalArgumentException("Activity service cannot be null");
        }

        if (sessionService == null) {
            throw new IllegalArgumentException("Session service cannot be null");
        }
        this.activityService = activityService;
        this.sessionService = sessionService;
        initializeView();
        loadKanbanData();
    }

    /**
     * Handles when an activity is dropped into a different column. Updates the activity status in the database and
     * refreshes the kanban board.
     * 
     * @param droppedActivity
     *            the activity that was dropped
     */
    private void handleActivityDropped(final CActivity droppedActivity) {
        LOGGER.info("Handling dropped activity: {} with new status: {}", droppedActivity.getName(),
                droppedActivity.getStatus().getName());

        try {
            // Update the activity status in the database
            activityService.updateEntityStatus(droppedActivity, droppedActivity.getStatus());
            // Refresh the kanban board to reflect the changes
            loadKanbanData();
            LOGGER.info("Successfully updated activity status for: {}", droppedActivity.getName());
        } catch (final Exception e) {
            LOGGER.error("Error updating activity status for: {}", droppedActivity.getName(), e);
            // Refresh the board to revert visual changes
            loadKanbanData();
        }
    }

    /**
     * Initializes the view components and layout.
     */
    private void initializeView() {
        LOGGER.debug("Initializing kanban board view layout");
        // Set CSS class for styling
        addClassName("activity-kanban-board");
        // Configure layout
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        // Create title
        titleElement = new H2("Activity Kanban Board");
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
        emptyStateContainer.add(new H2("No activities found"));
        emptyStateContainer.add(new Div("Create some activities to see them on the kanban board."));
        emptyStateContainer.setVisible(false);
        // Add components to view
        add(titleElement, kanbanContainer, emptyStateContainer);
        // Set expand ratio to make kanban container take most space
        setFlexGrow(1, kanbanContainer);
    }

    /**
     * Loads and displays kanban data for the current project.
     */
    private void loadKanbanData() {
        LOGGER.debug("Loading kanban data");
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
            titleElement.setText("Activity Kanban Board - " + project.getName());
            // Get activities grouped by status
            final Map<CActivityStatus, List<CActivity>> activitiesByStatus = activityService
                    .getActivitiesGroupedByStatus(project);

            if (activitiesByStatus.isEmpty()) {
                showEmptyState("No activities found for this project");
                return;
            }
            // Clear container and show kanban columns
            kanbanContainer.removeAll();
            emptyStateContainer.setVisible(false);
            kanbanContainer.setVisible(true);

            // Create column for each activity status
            for (final Map.Entry<CActivityStatus, List<CActivity>> entry : activitiesByStatus.entrySet()) {
                final CActivityStatus status = entry.getKey();
                final List<CActivity> activities = entry.getValue();
                LOGGER.debug("Creating column for status: {} with {} activities", status.getName(), activities.size());
                // Create column with drop handler
                final CActivityKanbanColumn column = new CActivityKanbanColumn(status, activities,
                        droppedActivity -> handleActivityDropped(droppedActivity));
                kanbanContainer.add(column);
                kanbanContainer.setFlexGrow(1, column);
            }
        } catch (final Exception e) {
            LOGGER.error("Error loading kanban data for project: {}", project.getName(), e);
            showEmptyState("Error loading activities: " + e.getMessage());
        }
    }

    /**
     * Called when the component is attached to the UI. Registers the project change listener.
     */
    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        sessionService.addProjectChangeListener(this);
        LOGGER.debug("Registered project change listener for kanban board");
    }

    /**
     * Called when the component is detached from the UI. Unregisters the project change listener to prevent memory
     * leaks.
     */
    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        sessionService.removeProjectChangeListener(this);
        LOGGER.debug("Unregistered project change listener for kanban board");
    }

    /**
     * Implementation of CProjectChangeListener interface. Called when the active project changes via the
     * SessionService.
     * 
     * @param newProject
     *            the newly selected project
     */
    @Override
    public void onProjectChanged(final CProject newProject) {
        LOGGER.debug("Project change notification received in kanban board: {}",
                newProject != null ? newProject.getName() : "null");
        loadKanbanData();
    }

    /**
     * Shows empty state with given message.
     * 
     * @param message
     *            the message to display
     */
    private void showEmptyState(final String message) {
        LOGGER.debug("Showing empty state: {}", message);
        kanbanContainer.setVisible(false);
        emptyStateContainer.removeAll();
        emptyStateContainer.add(new H2("No Activities"));
        emptyStateContainer.add(new Div(message));
        emptyStateContainer.setVisible(true);
    }
}