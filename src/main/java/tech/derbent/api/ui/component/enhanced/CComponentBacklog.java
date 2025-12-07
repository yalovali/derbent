package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IDropTarget;
import tech.derbent.api.interfaces.IGridDragDropSupport;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.sprints.service.CSprintItemService;

/** CComponentBacklog - Specialized backlog component for sprint planning with drag-and-drop support.
 * <p>
 * This component provides a complete backlog management solution following agile sprint planning patterns. It displays project items (Activities,
 * Meetings) that are not yet in the current sprint, allowing users to:
 * <ul>
 * <li>View and filter backlog items by type</li>
 * <li>Reorder items by priority (drag-and-drop updates sprintOrder)</li>
 * <li>Drag items into sprint for planning (via IGridDragDropSupport interface)</li>
 * <li>Multi-select for batch operations</li>
 * </ul>
 * <p>
 * <strong>Agile Sprint Planning Workflow:</strong>
 * <ol>
 * <li>Backlog items are ordered by sprintOrder (priority)</li>
 * <li>User reorders items within backlog to adjust priority</li>
 * <li>User drags high-priority items into sprint</li>
 * <li>Sprint items are ordered by itemOrder (execution sequence)</li>
 * </ol>
 * <p>
 * <strong>Drag-and-Drop Behavior:</strong>
 * <ul>
 * <li>Internal drag-drop: Reorders items within backlog (updates sprintOrder)</li>
 * <li>External drag: Enables dragging to sprint items component</li>
 * </ul>
 * <p>
 * <strong>Usage:</strong>
 *
 * <pre>
 * CComponentBacklog backlog = new CComponentBacklog(currentSprint);
 * backlog.setDragEnabled(true); // Enable dragging to sprint
 * backlog.setDynamicHeight("600px");
 * </pre>
 * <p>
 * The component automatically configures itself with Activities and Meetings entity types. Future entity types can be easily added by extending the
 * createEntityTypes() method. */
public class CComponentBacklog extends CComponentEntitySelection<CProjectItem<?>>
		implements IGridDragDropSupport<CProjectItem<?>>, IDropTarget<CProjectItem<?>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentBacklog.class);
	private static final long serialVersionUID = 1L;

	/** Creates the already-selected provider that filters out items already in the sprint.
	 * @param sprint the sprint to check for existing items
	 * @return provider for already-selected items */
	private static ItemsProvider<CProjectItem<?>> createAlreadySelectedProvider(final CSprint sprint) {
		return config -> {
			try {
				if (sprint == null || sprint.getId() == null) {
					return new ArrayList<>();
				}
				final CSprintItemService service = CSpringContext.getBean(CSprintItemService.class);
				final List<CSprintItem> sprintItems = service.findByMasterIdWithItems(sprint.getId());
				// Filter by entity type and extract the underlying items
				final List<CProjectItem<?>> result = new ArrayList<>();
				final String targetType = config.getEntityClass().getSimpleName();
				for (final CSprintItem sprintItem : sprintItems) {
					if (sprintItem.getItem() != null && targetType.equals(sprintItem.getItemType())) {
						result.add(sprintItem.getItem());
					}
				}
				return result;
			} catch (final Exception e) {
				LOGGER.error("Error loading already selected items for backlog: {}", config.getDisplayName(), e);
				return new ArrayList<>();
			}
		};
	}

	/** Creates the list of entity type configurations for the backlog.
	 * @return list of entity type configs (CActivity, CMeeting) */
	private static List<EntityTypeConfig<?>> createEntityTypes() {
		final List<EntityTypeConfig<?>> entityTypes = new ArrayList<>();
		// Get services from Spring context
		final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
		final CMeetingService meetingService = CSpringContext.getBean(CMeetingService.class);
		entityTypes.add(new EntityTypeConfig<>("CActivity", CActivity.class, activityService));
		entityTypes.add(new EntityTypeConfig<>("CMeeting", CMeeting.class, meetingService));
		return entityTypes;
	}

	/** Creates the items provider that loads all project items ordered by sprint order.
	 * @param sprint the sprint (provides access to project)
	 * @return provider for loading items */
	@SuppressWarnings ("unchecked")
	private static ItemsProvider<CProjectItem<?>> createItemsProvider(final CSprint sprint) {
		return config -> {
			try {
				final CProject project = sprint != null ? sprint.getProject() : null;
				if (project == null) {
					LOGGER.warn("No project available for loading backlog items");
					return new ArrayList<>();
				}
				// Get services from Spring context
				final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
				final CMeetingService meetingService = CSpringContext.getBean(CMeetingService.class);
				// Load items ordered by sprintOrder for proper backlog display
				if (config.getEntityClass() == CActivity.class) {
					return (List<CProjectItem<?>>) (List<?>) activityService.listByProjectOrderedBySprintOrder(project);
				} else if (config.getEntityClass() == CMeeting.class) {
					return (List<CProjectItem<?>>) (List<?>) meetingService.listByProjectOrderedBySprintOrder(project);
				}
				return new ArrayList<>();
			} catch (final Exception e) {
				LOGGER.error("Error loading backlog items for entity type: {}", config.getDisplayName(), e);
				return new ArrayList<>();
			}
		};
	}

	/** Creates a selection handler for the backlog.
	 * @return selection handler that logs selection changes */
	private static java.util.function.Consumer<java.util.Set<CProjectItem<?>>> createSelectionHandler() {
		return selectedItems -> {
			LOGGER.debug("Backlog selection changed: {} items selected", selectedItems.size());
		};
	}

	private final CActivityService activityService;
	private boolean dragEnabled = false;
	private CProjectItem<?> draggedItem = null;
	private Consumer<CProjectItem<?>> externalDropHandler = null;
	private final CMeetingService meetingService;

	/** Constructor for backlog component.
	 * @param sprint The sprint for which to display the backlog (items NOT in this sprint) */
	public CComponentBacklog(final CSprint sprint) {
		super(createEntityTypes(), createItemsProvider(sprint), createSelectionHandler(), true, createAlreadySelectedProvider(sprint),
				AlreadySelectedMode.HIDE_ALREADY_SELECTED);
		Check.notNull(sprint, "Sprint cannot be null");
		// Get services from Spring context
		activityService = CSpringContext.getBean(CActivityService.class);
		meetingService = CSpringContext.getBean(CMeetingService.class);
		CSpringContext.getBean(CSprintItemService.class);
		LOGGER.debug("CComponentBacklog created for sprint: {}", sprint.getId());
		configureInternalDragAndDrop();
	}

	/** Configures internal drag-and-drop for reordering items within the backlog. This is separate from external drag to sprint items. */
	private void configureInternalDragAndDrop() {
		final var grid = getGrid();
		if (grid == null) {
			LOGGER.warn("Grid not available for drag-and-drop configuration");
			return;
		}
		// Enable row dragging for reordering within backlog
		grid.setRowsDraggable(true);
		// Enable drop mode for receiving drops within same grid
		grid.setDropMode(GridDropMode.BETWEEN);
		// Track dragged item for internal reordering
		grid.addDragStartListener(event -> {
			final List<CProjectItem<?>> items = event.getDraggedItems();
			if (!items.isEmpty()) {
				draggedItem = items.get(0);
				LOGGER.debug("Started dragging backlog item for reordering: {}", draggedItem.getId());
			}
		});
		// Handle drag end - notify external handler if set
		grid.addDragEndListener(event -> {
			if (dragEnabled && externalDropHandler != null && draggedItem != null) {
				// Item was dragged outside the backlog grid
				LOGGER.debug("Item dragged from backlog: {}", draggedItem.getId());
				// External handler will be called by target component's drop listener
			}
			LOGGER.debug("Drag ended from backlog");
		});
		// Handle internal drops (reordering within backlog)
		grid.addDropListener(event -> {
			final CProjectItem<?> targetItem = event.getDropTargetItem().orElse(null);
			final GridDropLocation dropLocation = event.getDropLocation();
			if (draggedItem == null || targetItem == null) {
				return;
			}
			// Check if this is an internal drop (same grid reordering)
			if (draggedItem.getId().equals(targetItem.getId())) {
				LOGGER.debug("Item dropped on itself, ignoring");
				draggedItem = null;
				return;
			}
			// Check if items are sprintable
			if (!(draggedItem instanceof ISprintableItem) || !(targetItem instanceof ISprintableItem)) {
				LOGGER.debug("Items are not sprintable, ignoring drop");
				draggedItem = null;
				return;
			}
			try {
				handleInternalReordering((ISprintableItem) draggedItem, (ISprintableItem) targetItem, dropLocation);
			} catch (final Exception e) {
				LOGGER.error("Error handling internal reordering", e);
				CNotificationService.showException("Error reordering backlog items", e);
			}
			draggedItem = null;
		});
		LOGGER.debug("Internal drag-and-drop configured for backlog reordering");
	}

	/** Gets all items currently displayed in the grid.
	 * @return list of all items in grid */
	public List<CProjectItem<?>> getAllItems() {
		final var grid = getGrid();
		if (grid == null) {
			return List.of();
		}
		return grid.getListDataView().getItems().toList();
	}

	@Override
	public Consumer<CProjectItem<?>> getDropHandler() { return externalDropHandler; }

	/** Handles internal reordering of items within the backlog when dropped.
	 * @param draggedItem  the item being dragged
	 * @param targetItem   the item it's being dropped on/near
	 * @param dropLocation where relative to target (ABOVE, BELOW, ON_TOP) */
	private void handleInternalReordering(final ISprintableItem draggedItem, final ISprintableItem targetItem, final GridDropLocation dropLocation) {
		LOGGER.debug("Handling backlog reorder: dragged={}, target={}, location={}", draggedItem.getId(), targetItem.getId(), dropLocation);
		final List<CProjectItem<?>> currentItems = getAllItems();
		if (currentItems.isEmpty()) {
			LOGGER.warn("No items in grid for reordering");
			return;
		}
		final int draggedIndex = findItemIndex(currentItems, draggedItem.getId());
		final int targetIndex = findItemIndex(currentItems, targetItem.getId());
		if (draggedIndex == -1 || targetIndex == -1) {
			LOGGER.warn("Could not find dragged or target item in backlog");
			return;
		}
		final int newPosition = calculateNewPosition(draggedIndex, targetIndex, dropLocation);
		if (draggedIndex == newPosition) {
			LOGGER.debug("Item dropped at same position, no reordering needed");
			return;
		}
		try {
			updateSprintOrdersAndSave(currentItems, draggedIndex, newPosition);
			LOGGER.debug("Reordered backlog items: moved from position {} to {}", draggedIndex, newPosition);
			refreshGrid();
			CNotificationService.showSuccess("Backlog priority updated");
		} catch (final Exception e) {
			LOGGER.error("Error reordering backlog items", e);
			CNotificationService.showException("Error reordering backlog items", e);
		}
	}

	/** Finds the index of an item in the list by its ID.
	 * @param items the list of items
	 * @param itemId the ID to search for
	 * @return the index, or -1 if not found */
	private int findItemIndex(final List<CProjectItem<?>> items, final Long itemId) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId().equals(itemId)) {
				return i;
			}
		}
		return -1;
	}

	/** Calculates the new position for a dragged item based on drop location.
	 * @param draggedIndex current index of dragged item
	 * @param targetIndex index of target item
	 * @param dropLocation where relative to target (ABOVE, BELOW)
	 * @return the new position index */
	private int calculateNewPosition(final int draggedIndex, final int targetIndex, final GridDropLocation dropLocation) {
		int newPosition = targetIndex;
		if (dropLocation == GridDropLocation.BELOW) {
			newPosition++;
		}
		// Adjust if dragging from above (because removing from above shifts indices)
		if (draggedIndex < newPosition) {
			newPosition--;
		}
		return newPosition;
	}

	/** Updates sprint orders for affected items and saves them to database.
	 * @param items all current items
	 * @param draggedIndex index of dragged item
	 * @param newPosition new position for dragged item */
	private void updateSprintOrdersAndSave(final List<CProjectItem<?>> items, final int draggedIndex, final int newPosition) {
		if (draggedIndex < newPosition) {
			updateOrdersForDownwardMove(items, draggedIndex, newPosition);
		} else {
			updateOrdersForUpwardMove(items, draggedIndex, newPosition);
		}
	}

	/** Updates sprint orders when moving an item down in the list.
	 * @param items all current items
	 * @param draggedIndex original position
	 * @param newPosition target position */
	private void updateOrdersForDownwardMove(final List<CProjectItem<?>> items, final int draggedIndex, final int newPosition) {
		for (int i = 0; i < items.size(); i++) {
			final CProjectItem<?> item = items.get(i);
			if (!(item instanceof ISprintableItem)) continue;

			if (i == draggedIndex) {
				((ISprintableItem) item).setSprintOrder(newPosition + 1);
				saveItem(item);
			} else if (i > draggedIndex && i <= newPosition) {
				((ISprintableItem) item).setSprintOrder(i);
				saveItem(item);
			}
		}
	}

	/** Updates sprint orders when moving an item up in the list.
	 * @param items all current items
	 * @param draggedIndex original position
	 * @param newPosition target position */
	private void updateOrdersForUpwardMove(final List<CProjectItem<?>> items, final int draggedIndex, final int newPosition) {
		for (int i = 0; i < items.size(); i++) {
			final CProjectItem<?> item = items.get(i);
			if (!(item instanceof ISprintableItem)) continue;

			if (i == draggedIndex) {
				((ISprintableItem) item).setSprintOrder(newPosition + 1);
				saveItem(item);
			} else if (i >= newPosition && i < draggedIndex) {
				((ISprintableItem) item).setSprintOrder(i + 2);
				saveItem(item);
			}
		}
	}

	/** Saves an item using the appropriate service.
	 * @param item the item to save */
	private void saveItem(final CProjectItem<?> item) {
		if (item instanceof CActivity) {
			activityService.save((CActivity) item);
		} else if (item instanceof CMeeting) {
			meetingService.save((CMeeting) item);
		}
	}

	@Override
	public boolean isDragEnabled() { return dragEnabled; }

	@Override
	public boolean isDropEnabled() {
		// Backlog can always receive drops from sprint items
		return true;
	}

	@Override
	public void setDragEnabled(final boolean enabled) {
		dragEnabled = enabled;
		final var grid = getGrid();
		if (grid != null) {
			grid.setRowsDraggable(enabled);
			LOGGER.debug("External drag from backlog {}", enabled ? "enabled" : "disabled");
		}
	}

	// IGridDragDropSupport - For dragging TO sprint
	@Override
	public void setDropHandler(final Consumer<CProjectItem<?>> handler) {
		externalDropHandler = handler;
		LOGGER.debug("External drop handler {} for backlog", handler != null ? "set" : "cleared");
	}
}
