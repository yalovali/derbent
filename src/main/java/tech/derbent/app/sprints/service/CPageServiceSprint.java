package tech.derbent.app.sprints.service;

import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.screens.view.CComponentGridEntity;
import tech.derbent.api.services.pageservice.CDragDropEvent;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.enhanced.CComponentBacklog;
import tech.derbent.api.ui.component.enhanced.CComponentItemDetails;
import tech.derbent.api.ui.component.enhanced.CComponentListSprintItems;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.sprints.view.CComponentWidgetSprint;

/** CPageServiceSprint - Page service for Sprint management UI. Handles UI events and interactions for sprint views. */
public class CPageServiceSprint extends CPageServiceDynamicPage<CSprint>
		implements IPageServiceHasStatusAndWorkflow<CSprint>, IComponentWidgetEntityProvider<CSprint> {

	/** Enum defining the types of drag-drop operations in sprint planning. */
	private enum DragDropOperationType {
		/** Reordering items within the backlog */
		BACKLOG_REORDER,
		/** Moving a sprint item back to backlog */
		SPRINT_TO_BACKLOG,
		/** Adding a backlog item to a sprint */
		BACKLOG_TO_SPRINT,
		/** Reordering items within a sprint */
		SPRINT_REORDER,
		/** Unknown or unsupported operation */
		UNKNOWN
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceSprint.class);
	private CActivityService activityService;
	private CComponentBacklog componentBacklogItems;
	private CComponentItemDetails componentItemDetails;
	private CComponentListSprintItems componentItemsSelection;
	// Track items being dragged across components
	private CProjectItem<?> draggedFromBacklog = null;
	private CSprintItem draggedFromSprint = null;
	private CMeetingService meetingService;
	private CProjectItemStatusService projectItemStatusService;
	private CSprintItemService sprintItemService;

	public CPageServiceSprint(final IPageServiceImplementer<CSprint> view) {
		super(view);
		// Initialize the service from Spring context
		try {
			projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
			activityService = CSpringContext.getBean(CActivityService.class);
			meetingService = CSpringContext.getBean(CMeetingService.class);
			sprintItemService = CSpringContext.getBean(CSprintItemService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	public CComponentItemDetails createItemDetailsComponent() throws Exception {
		if (componentItemDetails == null) {
			componentItemDetails = new CComponentItemDetails(getSessionService());
		}
		return componentItemDetails;
	}

	public CComponentListSprintItems createSpritActivitiesComponent() {
		if (componentItemsSelection == null) {
			componentItemsSelection = new CComponentListSprintItems(sprintItemService, activityService, meetingService);
			// Enable drag-and-drop for sprint items grid (uses parent class methods)
			componentItemsSelection.setDragEnabled(true);
			componentItemsSelection.setDropEnabled(true);
			// Register with page service using unified auto-registration pattern
			componentItemsSelection.registerWithPageService(this);
			// Set up refresh listener
			if (componentBacklogItems != null) {
				componentItemsSelection.addRefreshListener(changedItem -> componentBacklogItems.refreshGrid());
			}
		}
		return componentItemsSelection;
	}

	/** Creates and configures the backlog items component for displaying items not in the sprint.
	 * @return configured CComponentBacklog component */
	public CComponentBacklog createSpritBacklogComponent() {
		final CSprint currentSprint = getView().getCurrentEntity();
		if (componentBacklogItems == null) {
			componentBacklogItems = new CComponentBacklog(currentSprint);
			componentBacklogItems.setDragEnabled(true);
			// Note: Drop is already enabled in CComponentBacklog constructor (GridDropMode.BETWEEN)
			// Register with page service using unified auto-registration pattern
			componentBacklogItems.registerWithPageService(this);
		}
		return componentBacklogItems;
	}

	/** Helper method to extract CSprintItem from master grid drag event.
	 * <p>
	 * The master grid contains widgets, which contain sprint item lists. The dragged items in the event are CSprintItem objects from those nested
	 * lists. This method safely extracts the first sprint item.
	 * @param event the drag-drop event from master grid
	 * @return the extracted CSprintItem, or null if extraction fails */
	private CSprintItem extractSprintItemFromMasterGridEvent(final CDragDropEvent<?> event) {
		if (event == null || event.getDraggedItems() == null || event.getDraggedItems().isEmpty()) {
			return null;
		}
		try {
			final Object firstItem = event.getDraggedItem();
			if (firstItem instanceof CSprintItem) {
				return (CSprintItem) firstItem;
			}
			LOGGER.warn("Dragged item is not a CSprintItem: {}", firstItem != null ? firstItem.getClass().getSimpleName() : "null");
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error extracting sprint item from master grid event", e);
			return null;
		}
	}

	/** Creates a widget component for displaying the given sprint entity.
	 * @param item the sprint to create a widget for
	 * @return the CComponentWidgetSprint component */
	@Override
	public Component getComponentWidget(final CSprint item) {
		return new CComponentWidgetSprint(item);
	}

	/** Gets the maximum sprint order value from all backlog items.
	 * @return the maximum sprint order, or 0 if no items have sprint order */
	private int getMaxBacklogOrder() {
		try {
			final List<CProjectItem<?>> allBacklogItems = componentBacklogItems.getAllItems();
			int maxOrder = 0;
			for (final CProjectItem<?> item : allBacklogItems) {
				if (item instanceof ISprintableItem) {
					final Integer order = ((ISprintableItem) item).getSprintOrder();
					if (order != null && order > maxOrder) {
						maxOrder = order;
					}
				}
			}
			return maxOrder;
		} catch (final Exception e) {
			LOGGER.error("Error getting max backlog order", e);
			return 0;
		}
	}

	/** Gets the next available order for sprint items in a specific sprint.
	 * @param sprint the sprint to get next order for
	 * @return the next order value */
	private int getNextSprintItemOrderForSprint(final CSprint sprint) {
		try {
			final List<CSprintItem> items = sprintItemService.findByMasterId(sprint.getId());
			if (items.isEmpty()) {
				return 1;
			}
			return items.stream().mapToInt(CSprintItem::getItemOrder).max().orElse(0) + 1;
		} catch (final Exception e) {
			LOGGER.error("Error getting next sprint item order", e);
			return 1;
		}
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return projectItemStatusService; }

	private CSprint getTargetSprintFromDropTarget(final CDragDropEvent<?> event, final Object targetItem) {
		if (targetItem == null) {
			// no target sprintitem under mouse
			if (event.getDropTarget() instanceof CComponentGridEntity) {
				// Dropped on empty area of grid - treat as dropping on the sprint itself
				final CComponentGridEntity dropTargetGrid = (CComponentGridEntity) event.getDropTarget();
				return (CSprint) dropTargetGrid.getSelectedItem();
			} else {
				LOGGER.warn("[DragDebug] Target item is null and drop location is not ON_TOP, cannot add backlog item to sprint");
				return null;
			}
		}
		if (targetItem instanceof CSprint) {
			return (CSprint) targetItem;
		} else if (targetItem instanceof CSprintItem) {
			return ((CSprintItem) targetItem).getSprint();
		} else {
			LOGGER.warn("[DragDebug] Target is not a Sprint or SprintItem, cannot add backlog item to sprint");
			return null;
		}
	}

	/** Handles dropping a backlog item into a sprint widget.
	 * @param event the drop event containing target and location */
	private void handleBacklogToSprintDrop(final CDragDropEvent<?> event) {
		try {
			final Object targetItem = event.getTargetItem();
			CSprint targetSprint = null;
			targetSprint = getTargetSprintFromDropTarget(event, targetItem);
			if (targetSprint == null) {
				LOGGER.error("Target sprint could not be determined, aborting add to sprint");
				return;
			}
			final CProjectItem<?> itemToAdd = draggedFromBacklog;
			// Determine item type
			final String itemType = itemToAdd.getClass().getSimpleName();
			// Calculate order - add at end of sprint items
			final int newOrder = getNextSprintItemOrderForSprint(targetSprint);
			// Create sprint item
			final CSprintItem sprintItem = new CSprintItem();
			sprintItem.setSprint(targetSprint);
			sprintItem.setItemId(itemToAdd.getId());
			sprintItem.setItemType(itemType);
			sprintItem.setItemOrder(newOrder);
			sprintItem.setItem(itemToAdd);
			// Save the new item
			sprintItemService.save(sprintItem);
			// Refresh both grids
			refreshAfterSprintChange();
			CNotificationService.showSuccess("Item added to sprint " + targetSprint.getName());
		} catch (final Exception e) {
			LOGGER.error("Error adding backlog item to sprint", e);
			CNotificationService.showException("Error adding item to sprint", e);
		} finally {
			draggedFromBacklog = null;
		}
	}

	/** Handles reordering a sprint item within its sprint or moving to another sprint.
	 * @param event the drop event containing target and location */
	private void handleSprintItemReorder(final CDragDropEvent<?> event) {
		try {
			final Object targetItem = event.getTargetItem();
			if (!(targetItem instanceof CSprint)) {
				LOGGER.warn("Target is not a Sprint, cannot reorder");
				return;
			}
			final CSprint targetSprint = (CSprint) targetItem;
			final CSprintItem draggedItem = draggedFromSprint;
			// Check if moving to different sprint or reordering within same sprint
			if (!draggedItem.getSprint().getId().equals(targetSprint.getId())) {
				// Moving to different sprint
				draggedItem.setSprint(targetSprint);
				// Add at end of target sprint
				final int newOrder = getNextSprintItemOrderForSprint(targetSprint);
				draggedItem.setItemOrder(newOrder);
			}
			// Save the updated item
			sprintItemService.save(draggedItem);
			// Refresh grids
			refreshAfterSprintChange();
			CNotificationService.showSuccess("Sprint item updated");
		} catch (final Exception e) {
			LOGGER.error("Error reordering sprint item", e);
			CNotificationService.showException("Error reordering sprint item", e);
		} finally {
			draggedFromSprint = null;
		}
	}

	/** Moves a sprint item back to the backlog with position-based ordering.
	 * @param sprintItem the sprint item to move
	 * @param event      the drop event containing target and location */
	private void moveSprintItemToBacklog(final CSprintItem sprintItem, final CDragDropEvent<?> event) {
		final CProjectItem<?> item = sprintItem.getItem();
		Check.notNull(item, "Sprint item must have an associated project item");
		// Update sprint order if dropped at specific position
		updateBacklogItemOrder(item, event);
		// Delete sprint item (removes from sprint)
		sprintItemService.delete(sprintItem);
	}

	public void on_backlogItems_change(final Component component, final Object value) {
		LOGGER.info("function: on_backlog_clicked for Component type");
		if (componentItemDetails == null) {
			return;
		}
		if (value == null) {
			componentItemDetails.setValue(null);
		}
		Check.instanceOf(value, HashSet.class, "Value must be of type HashSet<?>");
		final HashSet<?> selectedItems = (HashSet<?>) value;
		if (selectedItems.isEmpty() || (selectedItems.size() > 1)) {
			componentItemDetails.setValue(null);
			return;
		}
		final Object itemObject = selectedItems.iterator().next();
		Check.instanceOf(itemObject, CEntityNamed.class, "Value must be of type CEntityNamed<?>");
		final CEntityNamed<?> item = (CEntityNamed<?>) itemObject;
		componentItemDetails.setValue(item);
	}

	/** Handler for drag end events on backlog items grid. Clears tracking of dragged items.
	 * @param component the backlog grid component
	 * @param value     CDragDropEvent (dragged items not available in drag end) */
	public void on_backlogItems_dragEnd(final Component component, final Object value) {
		if (value instanceof CDragDropEvent) {
			draggedFromBacklog = null;
		}
	}

	/** Handler for drag start events on backlog items grid. Tracks items being dragged from backlog for cross-component drag-drop.
	 * @param component the backlog grid component
	 * @param value     CDragDropEvent containing dragged items */
	public void on_backlogItems_dragStart(final Component component, final Object value) {
		if (value instanceof CDragDropEvent) {
			final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
			if (event.getDraggedItems() != null && !event.getDraggedItems().isEmpty()) {
				draggedFromBacklog = (CProjectItem<?>) event.getDraggedItem();
			}
		}
	}

	/** Determines the type of drag-drop operation based on source and destination.
	 * @param event the drag-drop event
	 * @return the drag-drop operation type */
	private DragDropOperationType determineDragDropOperation(final CDragDropEvent<?> event) {
		final Object dragSource = event.getDragSource();
		final Object dropTarget = event.getDropTarget();
		
		// Scenario 1: Backlog → Backlog (internal reordering)
		if (dragSource instanceof CComponentBacklog && dropTarget instanceof CComponentBacklog) {
			return DragDropOperationType.BACKLOG_REORDER;
		}
		
		// Scenario 2: Sprint Items → Backlog (remove from sprint)
		if (dragSource instanceof CComponentListSprintItems && dropTarget instanceof CComponentBacklog) {
			return DragDropOperationType.SPRINT_TO_BACKLOG;
		}
		
		// Scenario 3: Backlog → Sprint Items (add to sprint)
		if (dragSource instanceof CComponentBacklog && dropTarget instanceof CComponentListSprintItems) {
			return DragDropOperationType.BACKLOG_TO_SPRINT;
		}
		
		// Scenario 4: Sprint Items → Sprint Items (reorder within sprint)
		if (dragSource instanceof CComponentListSprintItems && dropTarget instanceof CComponentListSprintItems) {
			return DragDropOperationType.SPRINT_REORDER;
		}
		
		// Unknown scenario
		LOGGER.warn("Unknown drag-drop scenario: source={}, target={}", 
			dragSource != null ? dragSource.getClass().getSimpleName() : "null",
			dropTarget != null ? dropTarget.getClass().getSimpleName() : "null");
		return DragDropOperationType.UNKNOWN;
	}

	/** Handler for drop events on backlog items grid. Handles both internal reordering and sprint-to-backlog drops.
	 * @param component the backlog grid component
	 * @param value     CDragDropEvent containing drop information */
	public void on_backlogItems_drop(final Component component, final Object value) {
		Check.instanceOf(value, CDragDropEvent.class, "Drop value must be CDragDropEvent");
		final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
		
		// NEW REQUIREMENT: Log comprehensive drag source information
		LOGGER.debug("[DragSourceDebug] ========== BACKLOG DROP EVENT START ==========");
		LOGGER.debug("[DragSourceDebug] Drop target component: {}", component != null ? component.getClass().getSimpleName() : "null");
		LOGGER.debug("[DragSourceDebug] Drag source: {}", event.getDragSource() != null ? event.getDragSource().getClass().getSimpleName() : "null");
		LOGGER.debug("[DragSourceDebug] Drop target: {}", event.getDropTarget() != null ? event.getDropTarget().getClass().getSimpleName() : "null");
		LOGGER.debug("[DragSourceDebug] Dragged items count: {}", event.getDraggedItems() != null ? event.getDraggedItems().size() : 0);
		if (event.getDraggedItems() != null && !event.getDraggedItems().isEmpty()) {
			LOGGER.debug("[DragSourceDebug] First dragged item type: {}", event.getDraggedItem() != null ? event.getDraggedItem().getClass().getSimpleName() : "null");
			LOGGER.debug("[DragSourceDebug] First dragged item: {}", event.getDraggedItem());
		}
		LOGGER.debug("[DragSourceDebug] Target item: {}", event.getTargetItem());
		LOGGER.debug("[DragSourceDebug] Drop location: {}", event.getDropLocation());
		LOGGER.debug("[DragSourceDebug] draggedFromBacklog field: {}", draggedFromBacklog != null ? draggedFromBacklog.getClass().getSimpleName() + "#" + draggedFromBacklog.getId() : "null");
		LOGGER.debug("[DragSourceDebug] draggedFromSprint field: {}", draggedFromSprint != null ? "CSprintItem#" + draggedFromSprint.getId() : "null");
		
		// Log component hierarchy from drag source
		final List<Component> hierarchy = event.getDragSourceHierarchy();
		LOGGER.debug("[DragSourceDebug] Drag source hierarchy (source to root):");
		for (int i = 0; i < hierarchy.size(); i++) {
			final Component comp = hierarchy.get(i);
			LOGGER.debug("[DragSourceDebug]   [{}] {}", i, comp.getClass().getSimpleName());
		}
		
		// Determine operation type based on source and destination
		final DragDropOperationType operationType = determineDragDropOperation(event);
		LOGGER.debug("[DragSourceDebug] Determined operation type: {}", operationType);
		LOGGER.debug("[DragSourceDebug] ========== BACKLOG DROP EVENT END ==========");
		
		// Handle based on operation type
		switch (operationType) {
			case BACKLOG_REORDER:
				// Internal backlog reordering is handled by CComponentBacklog itself
				LOGGER.debug("Internal backlog reordering detected - letting CComponentBacklog handle it");
				return;
				
			case SPRINT_TO_BACKLOG:
				// Move sprint item back to backlog
				// KEEP FAST ERROR DETECTION: Check immediately fails if sprintItem is null
				final CSprintItem sprintItem = draggedFromSprint;
				Check.notNull(sprintItem, "Sprint item cannot be null when dropping from sprint to backlog - drag source: " 
					+ (event.getDragSource() != null ? event.getDragSource().getClass().getSimpleName() : "null"));
				
				try {
					moveSprintItemToBacklog(sprintItem, event);
					refreshAfterBacklogDrop();
					CNotificationService.showSuccess("Item removed from sprint");
				} catch (final Exception e) {
					LOGGER.error("Error moving item to backlog", e);
					CNotificationService.showException("Error removing item from sprint", e);
				} finally {
					draggedFromSprint = null;
				}
				break;
				
			case BACKLOG_TO_SPRINT:
			case SPRINT_REORDER:
			case UNKNOWN:
			default:
				LOGGER.warn("Unexpected drag-drop operation on backlog: {}", operationType);
				break;
		}
	}

	public void on_description_blur(final Component component, final Object value) {
		LOGGER.info("function: on_description_blur for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	public void on_description_focus(final Component component, final Object value) {
		LOGGER.info("function: on_description_focus for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	/** Handler for drag end events on master grid. Clears tracking of dragged sprint items.
	 * @param component the master grid component
	 * @param value     CDragDropEvent (dragged items not available in drag end) */
	public void on_masterGrid_dragEnd(final Component component, final Object value) {
		draggedFromSprint = null;
	}

	/** Handler for drag start events on master grid. Extracts and tracks sprint items being dragged from widgets in the grid.
	 * <p>
	 * The master grid contains widgets (CComponentWidgetSprint), which contain sprint item lists (CComponentListSprintItems). When dragging from
	 * these nested components, we need to extract the sprint item from the event data to track it for cross-component drops (e.g., dragging from
	 * master grid to backlog).
	 * @param component the master grid component
	 * @param value     CDragDropEvent containing dragged sprint items */
	public void on_masterGrid_dragStart(final Component component, final Object value) {
		if (value instanceof CDragDropEvent) {
			final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
			draggedFromSprint = extractSprintItemFromMasterGridEvent(event);
			if (draggedFromSprint == null) {
				LOGGER.warn("Could not extract sprint item from master grid drag event");
			}
		}
	}

	/** Handler for drop events on master grid. Handles reordering sprint items within widgets or dropping backlog items.
	 * <p>
	 * This handler supports two scenarios:
	 * <ol>
	 * <li>Reordering sprint items within the same sprint (internal drag-drop within widgets)</li>
	 * <li>Dropping backlog items into sprint widgets</li>
	 * </ol>
	 * <p>
	 * Note: Most drop logic happens at the widget level (CComponentListSprintItems), but this handler can refresh the view after complex operations
	 * or handle cross-widget drops if needed.
	 * @param component the master grid component
	 * @param value     CDragDropEvent containing drop information */
	public void on_masterGrid_drop(final Component component, final Object value) {
		if (!(value instanceof CDragDropEvent)) {
			LOGGER.warn("Drop value is not CDragDropEvent");
			return;
		}
		final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
		// Check if dropping backlog item into sprint widget
		if (draggedFromBacklog != null) {
			handleBacklogToSprintDrop(event);
			return;
		}
		// Check if reordering sprint item within same sprint
		if (draggedFromSprint != null) {
			handleSprintItemReorder(event);
			return;
		}
	}

	public void on_name_change(final Component component, final Object value) {
		LOGGER.info("function: on_name_change for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	public void on_status_change(final Component component, final Object value) {
		LOGGER.info("function: on_status_change for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	@Override
	public void populateForm() {
		LOGGER.debug("populateForm called - CComponentListSprintItems receives entity updates via IContentOwner interface");
	}

	/** Refreshes UI components after backlog drop operation. */
	private void refreshAfterBacklogDrop() {
		componentItemsSelection.refreshGrid(); // Refresh sprint items
		componentBacklogItems.refreshGrid(); // Refresh backlog
	}
	// Helper methods for drag-drop operations

	/** Refreshes all relevant grids after a sprint change. */
	private void refreshAfterSprintChange() {
		// Refresh sprint items list if visible
		if (componentItemsSelection != null) {
			componentItemsSelection.refreshGrid();
		}
		// Refresh backlog
		if (componentBacklogItems != null) {
			componentBacklogItems.refreshGrid();
		}
		// Refresh master grid to update sprint widgets
		try {
			getView().refreshGrid();
		} catch (final Exception e) {
			LOGGER.error("Error refreshing master grid after sprint change", e);
		}
	}

	/** Reorders backlog items after inserting a new item at a specific sprint order position. All items with sprintOrder >= newOrder need to be
	 * shifted up by 1.
	 * @param newOrder      the sprint order value of the newly inserted item
	 * @param excludeItemId the ID of the item being inserted (to exclude it from reordering) */
	private void reorderBacklogItemsAfterInsert(final int newOrder, final Long excludeItemId) {
		try {
			final List<CProjectItem<?>> allBacklogItems = componentBacklogItems.getAllItems();
			// Shift items with sprintOrder >= newOrder
			for (final CProjectItem<?> item : allBacklogItems) {
				if (item instanceof ISprintableItem && !item.getId().equals(excludeItemId)) {
					final ISprintableItem sprintableItem = (ISprintableItem) item;
					final Integer itemOrder = sprintableItem.getSprintOrder();
					if (itemOrder != null && itemOrder >= newOrder) {
						sprintableItem.setSprintOrder(itemOrder + 1);
						// Save the updated item
						if (item instanceof CActivity) {
							activityService.save((CActivity) item);
						} else if (item instanceof CMeeting) {
							meetingService.save((CMeeting) item);
						}
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error reordering backlog items after insert", e);
		}
	}

	/** Saves a project item (Activity or Meeting).
	 * @param item the project item to save */
	private void saveProjectItem(final CProjectItem<?> item) {
		if (item instanceof CActivity) {
			activityService.save((CActivity) item);
		} else if (item instanceof CMeeting) {
			meetingService.save((CMeeting) item);
		}
	}

	/** Updates the backlog item's sprint order based on drop position.
	 * @param item  the project item to reorder
	 * @param event the drop event containing target and location */
	private void updateBacklogItemOrder(final CProjectItem<?> item, final CDragDropEvent<?> event) {
		Check.instanceOf(item, ISprintableItem.class, "Item must be ISprintableItem to reorder");
		final CProjectItem<?> targetItem = (CProjectItem<?>) event.getTargetItem();
		final GridDropLocation dropLocation = event.getDropLocation();
		if (targetItem == null || dropLocation == null) {
			return; // Dropped on empty space, keep current order
		}
		Check.instanceOf(targetItem, ISprintableItem.class, "Target item must be ISprintableItem");
		final ISprintableItem sprintableItem = (ISprintableItem) item;
		final ISprintableItem targetSprintableItem = (ISprintableItem) targetItem;
		final Integer targetOrder = targetSprintableItem.getSprintOrder();
		// Handle null sprint order: assign to end of backlog
		if (targetOrder == null) {
			LOGGER.warn("Target item {} has null sprint order, assigning dropped item to end of backlog", targetItem.getId());
			final int maxOrder = getMaxBacklogOrder();
			sprintableItem.setSprintOrder(maxOrder + 1);
			saveProjectItem(item);
			return;
		}
		// Calculate new order based on drop location
		final int newOrder = (dropLocation == GridDropLocation.BELOW) ? targetOrder + 1 : targetOrder;
		sprintableItem.setSprintOrder(newOrder);
		// Save the item with new order
		saveProjectItem(item);
		// Reorder other backlog items to maintain sequence
		reorderBacklogItemsAfterInsert(newOrder, item.getId());
	}
}
