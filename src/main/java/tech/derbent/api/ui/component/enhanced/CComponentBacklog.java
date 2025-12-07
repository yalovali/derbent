package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
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

/** CComponentBacklog - Specialized component for backlog item selection with drag-and-drop reordering support.
 * <p>
 * This component extends CComponentEntitySelection to provide a complete backlog management solution for sprint planning. It comes pre-configured with
 * support for Activities and Meetings, with easy extensibility for future entity types.
 * <p>
 * Features:
 * <ul>
 * <li>Pre-configured entity types (CActivity, CMeeting) - easily extensible</li>
 * <li>Automatic filtering of items already in sprint</li>
 * <li>Drag-and-drop reordering within the backlog (updates sprintOrder field)</li>
 * <li>Multi-select support for batch operations</li>
 * <li>Automatic service integration via Spring context</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * CComponentBacklog backlog = new CComponentBacklog(currentSprint);
 * backlog.setDynamicHeight("600px");
 * </pre>
 * <p>
 * The component automatically:
 * - Loads activities and meetings from the current project
 * - Hides items already in the sprint
 * - Enables drag-and-drop for reordering
 * - Updates sprintOrder when items are reordered */
public class CComponentBacklog extends CComponentEntitySelection<CProjectItem<?>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentBacklog.class);
	private static final long serialVersionUID = 1L;
	private final CActivityService activityService;
	private final CMeetingService meetingService;
	private final CSprintItemService sprintItemService;
	private CProjectItem<?> draggedItem = null;
	private CSprint sprint;

	/** Constructor for backlog component.
	 * @param sprint The sprint for which to display the backlog (items NOT in this sprint) */
	public CComponentBacklog(final CSprint sprint) {
		super(createEntityTypes(), createItemsProvider(sprint), createSelectionHandler(), true, createAlreadySelectedProvider(sprint),
				AlreadySelectedMode.HIDE_ALREADY_SELECTED);
		Check.notNull(sprint, "Sprint cannot be null");
		this.sprint = sprint;
		// Get services from Spring context
		this.activityService = CSpringContext.getBean(CActivityService.class);
		this.meetingService = CSpringContext.getBean(CMeetingService.class);
		this.sprintItemService = CSpringContext.getBean(CSprintItemService.class);
		LOGGER.debug("CComponentBacklog created for sprint: {}", sprint.getId());
		configureDragAndDrop();
	}

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

	/** Configures drag-and-drop functionality for the grid. This enables internal reordering of sprintable items. */
	private void configureDragAndDrop() {
		final var grid = getGrid();
		if (grid == null) {
			LOGGER.warn("Grid not available for drag-and-drop configuration");
			return;
		}
		// Enable row dragging
		grid.setRowsDraggable(true);
		// Enable drop mode
		grid.setDropMode(GridDropMode.BETWEEN);
		// Add drag start listener
		grid.addDragStartListener(event -> {
			draggedItem = event.getDraggedItems().isEmpty() ? null : event.getDraggedItems().get(0);
			if (draggedItem != null) {
				LOGGER.debug("Started dragging item: {}", draggedItem.getId());
			}
		});
		// Add drag end listener
		grid.addDragEndListener(event -> {
			LOGGER.debug("Drag ended");
			draggedItem = null;
		});
		// Add drop listener
		grid.addDropListener(event -> {
			final CProjectItem<?> targetItem = event.getDropTargetItem().orElse(null);
			final GridDropLocation dropLocation = event.getDropLocation();
			if (draggedItem == null || targetItem == null) {
				LOGGER.debug("Drag or target item is null, ignoring drop");
				return;
			}
			// Check if items are sprintable
			if (!(draggedItem instanceof ISprintableItem) || !(targetItem instanceof ISprintableItem)) {
				LOGGER.debug("Items are not sprintable, ignoring drop");
				return;
			}
			try {
				handleReordering((ISprintableItem) draggedItem, (ISprintableItem) targetItem, dropLocation);
			} catch (final Exception e) {
				LOGGER.error("Error handling drop for reordering", e);
				CNotificationService.showException("Error reordering items", e);
			}
		});
		LOGGER.debug("Drag-and-drop configured for backlog grid");
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

	/** Handles reordering of items when dropped.
	 * @param draggedItem  the item being dragged
	 * @param targetItem   the item it's being dropped on/near
	 * @param dropLocation where relative to target (ABOVE, BELOW, ON_TOP) */
	private void handleReordering(final ISprintableItem draggedItem, final ISprintableItem targetItem, final GridDropLocation dropLocation) {
		LOGGER.debug("Handling reorder: dragged={}, target={}, location={}", draggedItem.getId(), targetItem.getId(), dropLocation);
		// Get current items list
		final List<CProjectItem<?>> currentItems = getAllItems();
		if (currentItems.isEmpty()) {
			LOGGER.warn("No items in grid for reordering");
			return;
		}
		// Find indices
		int draggedIndex = -1;
		int targetIndex = -1;
		for (int i = 0; i < currentItems.size(); i++) {
			if (currentItems.get(i).getId().equals(draggedItem.getId())) {
				draggedIndex = i;
			}
			if (currentItems.get(i).getId().equals(targetItem.getId())) {
				targetIndex = i;
			}
		}
		if (draggedIndex == -1 || targetIndex == -1) {
			LOGGER.warn("Could not find dragged or target item in list");
			return;
		}
		// Calculate new position
		int newPosition = targetIndex;
		if (dropLocation == GridDropLocation.BELOW) {
			newPosition = targetIndex + 1;
		}
		// Adjust if dragging from above
		if (draggedIndex < newPosition) {
			newPosition--;
		}
		if (draggedIndex == newPosition) {
			LOGGER.debug("Item dropped at same position, no reordering needed");
			return;
		}
		// Reorder the list
		final CProjectItem<?> item = currentItems.remove(draggedIndex);
		currentItems.add(newPosition, item);
		// Update sprint orders and save
		for (int i = 0; i < currentItems.size(); i++) {
			final CProjectItem<?> currentItem = currentItems.get(i);
			if (currentItem instanceof ISprintableItem) {
				((ISprintableItem) currentItem).setSprintOrder(i + 1); // Start from 1
				// Save the updated sprintOrder
				try {
					if (currentItem instanceof CActivity) {
						activityService.save((CActivity) currentItem);
					} else if (currentItem instanceof CMeeting) {
						meetingService.save((CMeeting) currentItem);
					}
				} catch (final Exception e) {
					LOGGER.error("Error saving updated sprintOrder for item {}", currentItem.getId(), e);
				}
			}
		}
		LOGGER.debug("Reordered items: moved from position {} to {}", draggedIndex, newPosition);
		// Refresh the grid
		refresh();
		CNotificationService.showSuccess("Backlog items reordered");
	}
}
