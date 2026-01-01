package tech.derbent.app.kanban.kanbanline.view;

import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropEvent;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBacklog;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprintItem;

/**
 * CComponentKanbanColumnBacklog - A specialized kanban column that displays the project backlog.
 * 
 * <p>This component extends CComponentKanbanColumn to provide a dedicated backlog column in the kanban board.
 * Unlike regular kanban columns that display sprint items filtered by status, the backlog column displays
 * all project items that are not assigned to any sprint.</p>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><b>Embedded Backlog Component:</b> Uses CComponentBacklog to display items not in sprints</li>
 *   <li><b>Compact Mode:</b> Toggle between normal and narrow display modes for space optimization</li>
 *   <li><b>Drag Source:</b> Items can be dragged from backlog to kanban columns (adds to sprint)</li>
 *   <li><b>Drop Target:</b> Items can be dropped on backlog (removes from sprint)</li>
 *   <li><b>Default Creation:</b> Automatically created as first column in kanban board</li>
 * </ul>
 * 
 * <h3>Drag-Drop Behavior:</h3>
 * <ul>
 *   <li><b>Drag from backlog to column:</b> Adds item to sprint + sets status + assigns to column</li>
 *   <li><b>Drop on backlog from column:</b> Removes item from sprint (returns to backlog)</li>
 *   <li><b>Backlog internal reorder:</b> Changes sprintOrder field for backlog display order</li>
 * </ul>
 * 
 * <h3>Compact Mode:</h3>
 * <p>When compact mode is enabled, the backlog column displays in a narrower format suitable for
 * displaying alongside multiple kanban columns. This is useful for boards with many status columns.</p>
 * 
 * <h3>Usage Pattern:</h3>
 * <pre>
 * // Create backlog column for a project
 * CComponentKanbanColumnBacklog backlogColumn = new CComponentKanbanColumnBacklog(project);
 * 
 * // Enable compact mode for narrow display
 * backlogColumn.setCompactMode(true);
 * 
 * // Enable drag-drop
 * backlogColumn.drag_setDragEnabled(true);
 * backlogColumn.drag_setDropEnabled(true);
 * 
 * // Add to kanban board (typically as first column)
 * kanbanBoard.add(backlogColumn);
 * </pre>
 * 
 * <h3>Integration with CPageServiceKanbanLine:</h3>
 * <p>The page service must distinguish between drops onto backlog vs regular columns to handle
 * sprint membership changes correctly. Use instanceof checks to identify backlog column drops.</p>
 * 
 * @see CComponentKanbanColumn Parent class for standard kanban columns
 * @see CComponentBacklog The embedded backlog component for item display
 * @see CPageServiceKanbanLine Handles drag-drop events and sprint management
 */
public class CComponentKanbanColumnBacklog extends CComponentKanbanColumn {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentKanbanColumnBacklog.class);
	private static final long serialVersionUID = 1L;

	/** Backlog component embedded in this column */
	private CComponentBacklog backlogComponent;
	
	/** Toolbar for backlog-specific controls (compact mode toggle, etc.) */
	private final CHorizontalLayout backlogToolbar;
	
	/** Button to toggle compact mode */
	private final CButton buttonCompactMode;
	
	/** Indicates whether compact mode is active (narrow display) */
	private boolean compactMode = false;
	
	/** Drop target for handling drops onto the backlog area */
	private DropTarget<CVerticalLayout> backlogDropTarget;
	
	/** The project context for loading backlog items */
	private final CProject project;

	/**
	 * Creates a backlog kanban column for the specified project.
	 * 
	 * @param project The project whose backlog items should be displayed (required)
	 * @throws IllegalArgumentException if project is null
	 */
	public CComponentKanbanColumnBacklog(final CProject project) {
		super();
		Check.notNull(project, "Project cannot be null for backlog column");
		this.project = project;
		
		LOGGER.debug("Creating backlog kanban column for project: {}", project.getName());
		
		// Create toolbar for backlog controls first
		backlogToolbar = new CHorizontalLayout();
		backlogToolbar.setSpacing(true);
		backlogToolbar.setPadding(false);
		backlogToolbar.setWidthFull();
		
		// Create compact mode toggle button
		buttonCompactMode = create_buttonCompactMode();
		backlogToolbar.add(buttonCompactMode);
		
		// Add toolbar first
		add(backlogToolbar);
		
		// Create backlog component with initial compact mode (false)
		backlogComponent = new CComponentBacklog(project, compactMode);
		
		// Add backlog component to the column
		add(backlogComponent);
		
		// Configure column styling for backlog
		applyBacklogStyling();
		
		// Set up drag-drop for backlog items
		setupBacklogDragDrop();
	}

	/**
	 * Applies backlog-specific styling to distinguish it from regular kanban columns.
	 * Uses a different background color and border to indicate it's the backlog area.
	 */
	private void applyBacklogStyling() {
		// Override default kanban column styling with backlog-specific colors
		getStyle().set("background-color", "#F5F5F5");  // Light gray background
		getStyle().set("border", "2px dashed #BDBDBD");  // Dashed border to distinguish from regular columns
		getStyle().set("border-radius", "10px");
		addClassName("kanban-column-backlog");
	}

	/**
	 * Creates the compact mode toggle button.
	 * When clicked, switches between normal and narrow display modes.
	 * 
	 * @return Configured compact mode button
	 */
	protected CButton create_buttonCompactMode() {
		final CButton button = new CButton(VaadinIcon.COMPRESS.create());
		button.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
		button.setTooltipText("Toggle compact mode");
		button.addClickListener(e -> on_buttonCompactMode_clicked());
		return button;
	}

	@Override
	public void drag_checkEventBeforePass(final CEvent event) {
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

	/**
	 * Hook executed after drag-drop events are processed.
	 * Used to refresh the backlog display after items are added/removed.
	 */
	@Override
	public void drag_checkEventAfterPass(final CEvent event) {
		LOGGER.debug("[BacklogDrag] Completed drag propagation for backlog column");
		
		// Refresh backlog after drag-drop to show updated item list
		if (event instanceof CDragDropEvent) {
			refreshBacklog();
		}
	}

	/**
	 * Creates a drop listener for the backlog area.
	 * Handles drops of sprint items onto the backlog (removes from sprint).
	 * 
	 * @return Drop event listener for backlog area
	 */
	private ComponentEventListener<DropEvent<CVerticalLayout>> drag_on_backlog_drop() {
		return event -> {
			try {
				LOGGER.debug("Handling backlog drop event");
				
				// Create drop event and propagate to page service
				// The page service will detect this is a backlog drop and handle sprint removal
				final CDragDropEvent dropEvent = new CDragDropEvent(
					getId().orElse("BacklogColumn"), 
					this, 
					this,  // Target item is the backlog column itself
					null,  // No specific drop location for backlog
					true   // Drop is allowed
				);
				
				notifyEvents(dropEvent);
			} catch (final Exception e) {
				LOGGER.error("Error handling backlog drop event", e);
			}
		};
	}

	/**
	 * Gets the embedded backlog component.
	 * Useful for accessing backlog grid, items, and refresh methods.
	 * 
	 * @return The CComponentBacklog instance
	 */
	public CComponentBacklog getBacklogComponent() {
		return backlogComponent;
	}

	/**
	 * Gets the project context for this backlog column.
	 * 
	 * @return The project whose backlog is displayed
	 */
	public CProject getProject() {
		return project;
	}

	/**
	 * Checks if compact mode is currently enabled.
	 * 
	 * @return true if compact mode is active, false otherwise
	 */
	public boolean isCompactMode() {
		return compactMode;
	}

	/**
	 * Handles compact mode button click.
	 * Toggles between normal and narrow display modes.
	 */
	protected void on_buttonCompactMode_clicked() {
		setCompactMode(!compactMode);
	}

	/**
	 * Refreshes the backlog component to reload items from database.
	 * Called after drag-drop operations or when sprint items change.
	 */
	public void refreshBacklog() {
		LOGGER.debug("Refreshing backlog component");
		if (backlogComponent != null) {
			backlogComponent.refreshComponent();
		}
	}

	/**
	 * Overrides parent refreshComponent to refresh backlog instead of post-its.
	 * The backlog column doesn't display traditional post-its but uses the backlog grid.
	 */
	@Override
	protected void refreshComponent() {
		// Don't call super.refreshComponent() as we don't use the itemsLayout
		// Instead, refresh the embedded backlog component
		refreshBacklog();
	}

	/**
	 * Overrides parent refreshItems to use backlog component.
	 * Regular kanban columns filter sprint items; backlog shows non-sprint items.
	 */
	@Override
	public void setItems(final List<CSprintItem> items) {
		// Backlog column doesn't use sprint items - it displays backlog items
		// The backlog component manages its own data loading based on project
		LOGGER.debug("setItems called on backlog column - ignoring as backlog manages its own data");
	}

	/**
	 * Sets compact mode for the backlog column.
	 * In compact mode, the column displays narrower to save horizontal space.
	 * 
	 * @param compact true to enable compact mode, false for normal mode
	 */
	public void setCompactMode(final boolean compact) {
		if (this.compactMode == compact) {
			return; // No change needed
		}
		
		this.compactMode = compact;
		
		// Update button icon and tooltip
		if (compact) {
			// Narrow width for compact display
			setWidth("220px");
			buttonCompactMode.setIcon(VaadinIcon.EXPAND.create());
			buttonCompactMode.setTooltipText("Expand backlog");
			LOGGER.debug("Backlog column set to compact mode");
		} else {
			// Normal width for full backlog display
			setWidth("320px");
			buttonCompactMode.setIcon(VaadinIcon.COMPRESS.create());
			buttonCompactMode.setTooltipText("Compact backlog");
			LOGGER.debug("Backlog column set to normal mode");
		}
		
		// Recreate backlog component with new compact mode
		remove(backlogComponent);
		backlogComponent = new CComponentBacklog(project, compact);
		setupBacklogDragDrop();
		add(backlogComponent);
	}

	/**
	 * Sets up drag-drop functionality for backlog items.
	 * Configures both drag (from backlog) and drop (to backlog) capabilities.
	 * Also sets up selection notification forwarding to parent components.
	 */
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
