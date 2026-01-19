package tech.derbent.app.kanban.kanbanline.view;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropEvent;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.data.provider.Query;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBacklog;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.service.CPageServiceKanbanLine;
import tech.derbent.app.sprints.domain.CSprintItem;

/** CComponentKanbanColumnBacklog - A specialized kanban column that displays the project backlog.
 * <p>
 * This component extends CComponentKanbanColumn to provide a dedicated backlog column in the kanban board. Unlike regular kanban columns that display
 * sprint items filtered by status, the backlog column displays all project items that are not assigned to any sprint. The backlog is always displayed
 * in compact mode (narrow, 220px width) with only name column and type/name filters visible.
 * </p>
 * <h3>Key Features:</h3>
 * <ul>
 * <li><b>Embedded Backlog Component:</b> Uses CComponentBacklog in compact mode to display items not in sprints</li>
 * <li><b>Fixed Compact Display:</b> Always displays in narrow format (220px) with simplified grid and filters</li>
 * <li><b>Drag Source:</b> Items can be dragged from backlog to kanban columns (adds to sprint)</li>
 * <li><b>Drop Target:</b> Items can be dropped on backlog (removes from sprint)</li>
 * <li><b>Default Creation:</b> Automatically created as first column in kanban board</li>
 * </ul>
 * <h3>Drag-Drop Behavior:</h3>
 * <ul>
 * <li><b>Drag from backlog to column:</b> Adds item to sprint + sets status + assigns to column</li>
 * <li><b>Drop on backlog from column:</b> Removes item from sprint (returns to backlog)</li>
 * <li><b>Backlog internal reorder:</b> Changes sprintOrder field for backlog display order</li>
 * </ul>
 * <h3>Usage Pattern:</h3>
 *
 * <pre>
 * // Create backlog column for a project (always in compact mode)
 * CComponentKanbanColumnBacklog backlogColumn = new CComponentKanbanColumnBacklog(project);
 * // Enable drag-drop
 * backlogColumn.drag_setDragEnabled(true);
 * backlogColumn.drag_setDropEnabled(true);
 * // Add to kanban board (typically as first column)
 * kanbanBoard.add(backlogColumn);
 * </pre>
 *
 * <h3>Integration with CPageServiceKanbanLine:</h3>
 * <p>
 * The page service must distinguish between drops onto backlog vs regular columns to handle sprint membership changes correctly. Use instanceof
 * checks to identify backlog column drops.
 * </p>
 * @see CComponentKanbanColumn Parent class for standard kanban columns
 * @see CComponentBacklog The embedded backlog component for item display
 * @see CPageServiceKanbanLine Handles drag-drop events and sprint management */
public class CComponentKanbanColumnBacklog extends CComponentKanbanColumn {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentKanbanColumnBacklog.class);
	private static final long serialVersionUID = 1L;
	/** Backlog component embedded in this column */
	private final CComponentBacklog backlogComponent;
	/** Drop target for handling drops onto the backlog area */
	private DropTarget<CVerticalLayout> backlogDropTarget;
	/** The project context for loading backlog items */
	private final CProject project;

	/** Creates a backlog kanban column for the specified project. The backlog component is always created in compact mode for narrow display.
	 * @param project The project whose backlog items should be displayed (required)
	 * @throws IllegalArgumentException if project is null */
	@SuppressWarnings ("unused")
	public CComponentKanbanColumnBacklog(final CProject project) {
		super();
		Check.notNull(project, "Project cannot be null for backlog column");
		this.project = project;
		LOGGER.debug("Creating backlog kanban column for project: {}", project.getName());
		// Set the backlog column header title to make it visible
		setBacklogColumnHeader();
		// Create backlog component in compact mode (always true for narrow display in kanban board)
		backlogComponent = new CComponentBacklog(project, true);
		// Listen for backlog changes to update story point total
		backlogComponent.addRefreshListener(event -> refreshBacklogStoryPointTotal());
		// Add backlog component to the column
		add(backlogComponent);
		// Set up drag-drop for backlog items
		setupBacklogDragDrop();
		getStyle().set("background-color", "#F0F4F8");
		refreshBacklogStoryPointTotal();
	}

	/** Hook executed after drag-drop events are processed. Used to refresh the backlog display after items are added/removed. */
	@Override
	public void drag_checkEventAfterPass(final CEvent event) {
		super.drag_checkEventAfterPass(event);
		LOGGER.debug("[BacklogDrag] Completed drag propagation for backlog column");
		// Refresh backlog after drag-drop to show updated item list
		if (event instanceof CDragDropEvent) {
			refreshComponent();
		}
	}

	@Override
	public void drag_checkEventBeforePass(final CEvent event) {
		super.drag_checkEventBeforePass(event);
		Check.notNull(event, "Drag event cannot be null for backlog column");
		// For drop events on backlog, mark the target as the backlog column itself
		// This allows the page service to identify drops onto backlog vs regular columns
		if (event instanceof final CDragDropEvent dropEvent) {
			if (dropEvent.getTargetItem() == null) {
				// Mark this as a backlog drop by setting a special marker
				dropEvent.setTargetItem(this);
			}
		}
		LOGGER.debug("[BacklogDrag] Propagating {} event for backlog column", event.getClass().getSimpleName());
	}

	/** Creates a drop listener for the backlog area. Handles drops of sprint items onto the backlog (removes from sprint).
	 * @return Drop event listener for backlog area */
	@SuppressWarnings ("unused")
	private ComponentEventListener<DropEvent<CVerticalLayout>> drag_on_backlog_drop() {
		return event -> {
			try {
				LOGGER.debug("Handling backlog drop event");
				// Create drop event and propagate to page service
				// The page service will detect this is a backlog drop and handle sprint removal
				final CDragDropEvent dropEvent = new CDragDropEvent(getId().orElse("BacklogColumn"), this, this, // Target item is the backlog column
																													// itself
						null, // No specific drop location for backlog
						true // Drop is allowed
				);
				notifyEvents(dropEvent);
			} catch (final Exception e) {
				LOGGER.error("Error handling backlog drop event", e);
			}
		};
	}

	/** Gets the embedded backlog component. Useful for accessing backlog grid, items, and refresh methods.
	 * @return The CComponentBacklog instance */
	public CComponentBacklog getBacklogComponent() { return backlogComponent; }

	/** Gets the project context for this backlog column.
	 * @return The project whose backlog is displayed */
	public CProject getProject() { return project; }

	/** Refreshes story point total for backlog items. */
	private void refreshBacklogStoryPointTotal() {
		if (backlogComponent == null || backlogComponent.getGrid() == null) {
			return;
		}
		try {
			final List<?> items = backlogComponent.getGrid().getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
			long totalStoryPoints = 0;
			for (final Object item : items) {
				if (item instanceof final ISprintableItem sprintableItem) {
					final Long sp = sprintableItem.getStoryPoint();
					if (sp != null) {
						totalStoryPoints += sp;
					}
				}
			}
			if (totalStoryPoints > 0) {
				storyPointTotalLabel.setText(totalStoryPoints + " SP");
				if (!headerLayout.getChildren().anyMatch(c -> c == storyPointTotalLabel)) {
					headerLayout.add(storyPointTotalLabel);
				}
				storyPointTotalLabel.setVisible(true);
			} else {
				storyPointTotalLabel.setVisible(false);
			}
		} catch (final Exception e) {
			LOGGER.error("Error calculating backlog story points", e);
		}
	}

	/** Overrides parent refreshComponent to refresh backlog instead of post-its. The backlog column doesn't display traditional post-its but uses the
	 * backlog grid. */
	@Override
	public void refreshComponent() {
		// Don't call super.refreshComponent() as we don't use the itemsLayout
		// Instead, refresh the embedded backlog component
		LOGGER.debug("Refreshing backlog column component");
		backlogComponent.refreshComponent();
		refreshBacklogStoryPointTotal();
	}

	/** Override to prevent story point total display in backlog column. */
	@Override
	protected void refreshStoryPointTotal() {
		// Delegate to backlog-specific method
		refreshBacklogStoryPointTotal();
	}

	/** Sets the header title for the backlog column to make it visible. */
	private void setBacklogColumnHeader() {
		// Set the title to "Backlog" and hide story points total for backlog
		title.setText("Backlog");
		statusesLabel.setText("");
	}

	/** Overrides parent refreshItems to use backlog component. Regular kanban columns filter sprint items; backlog shows non-sprint items. */
	@Override
	public void setItems(final List<CSprintItem> items) {
		// Backlog column doesn't use sprint items - it displays backlog items
		// The backlog component manages its own data loading based on project
		LOGGER.debug("setItems called on backlog column - ignoring as backlog manages its own data");
	}

	/** Sets up drag-drop functionality for backlog items. Configures both drag (from backlog) and drop (to backlog) capabilities. Also sets up
	 * selection notification forwarding to parent components. */
	private void setupBacklogDragDrop() {
		// Enable dragging from backlog grid (backlog component handles this)
		backlogComponent.drag_setDragEnabled(true);
		// Forward drag events from backlog component to this column
		// This allows the page service to handle backlog item drags
		setupChildDragDropForwarding(backlogComponent);
		// Forward selection events from backlog component to parent (kanban board)
		// This allows the board to display selected item details in the entity view
		setupSelectionNotification(backlogComponent);
		// Set up drop target on the entire backlog column
		// This allows dropping sprint items onto the backlog to remove them from sprint
		backlogDropTarget = DropTarget.create(this);
		backlogDropTarget.setDropEffect(DropEffect.MOVE);
		backlogDropTarget.addDropListener(drag_on_backlog_drop());
		backlogDropTarget.setActive(true);
		LOGGER.debug("Backlog drag-drop and selection notification configured");
	}
}
