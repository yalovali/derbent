package tech.derbent.app.sprints.service;

import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.interfaces.ISprintableItem;
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

	/** Calculates the order for inserting a new item at a specific position.
	 * @param targetItem   the item near which to insert
	 * @param dropLocation where relative to the target (ABOVE or BELOW)
	 * @return the order value for the new item */
	private int calculateInsertOrder(final CSprintItem targetItem, final GridDropLocation dropLocation) {
		if (dropLocation == GridDropLocation.BELOW) {
			// Insert after target
			return targetItem.getItemOrder() + 1;
		} else {
			// Insert before target (ABOVE or ON_TOP)
			return targetItem.getItemOrder();
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
			// Enable drag-and-drop reordering within the sprint items grid
			componentItemsSelection.enableDragAndDropReordering();
			// Enable dragging FROM sprint items back to backlog
			componentItemsSelection.setDragToBacklogEnabled(true);
			// Set up drop mode for sprint items grid to accept drops from backlog
			if (componentItemsSelection.getGrid() != null) {
				componentItemsSelection.getGrid().setDropMode(GridDropMode.BETWEEN);
				componentItemsSelection.getGrid().setRowsDraggable(true);
			}
			// Register with page service using unified auto-registration pattern
			componentItemsSelection.registerWithPageService(this);
			// Set up refresh listener
			if (componentBacklogItems != null) {
				componentItemsSelection.addRefreshListener(changedItem -> {
					LOGGER.debug("Sprint items changed, refreshing backlog");
					componentBacklogItems.refreshGrid();
				});
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
			LOGGER.warn("[DragDebug] Dragged item is not a CSprintItem: {}", firstItem != null ? firstItem.getClass().getSimpleName() : "null");
			return null;
		} catch (final Exception e) {
			LOGGER.error("[DragDebug] Error extracting sprint item from master grid event", e);
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

	/** Gets the next order number for a new sprint item.
	 * @return The next order number */
	private Integer getNextSprintItemOrder() {
		final CSprint currentSprint = getView().getCurrentEntity();
		Check.notNull(currentSprint, "Current sprint cannot be null when getting next order");
		if (currentSprint.getId() == null) {
			LOGGER.debug("Sprint is new, starting order at 1");
			return 1;
		}
		final List<CSprintItem> items = sprintItemService.findByMasterId(currentSprint.getId());
		final int nextOrder = items.size() + 1;
		LOGGER.debug("Next item order for sprint {}: {}", currentSprint.getId(), nextOrder);
		return nextOrder;
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return projectItemStatusService; }

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
		LOGGER.debug("[DragDebug] Sprint item {} deleted, item {} moved to backlog", sprintItem.getId(), item.getId());
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
			LOGGER.debug("[DragDebug] CPageServiceSprint.on_backlogItems_dragEnd: Backlog drag operation completed");
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
				LOGGER.debug("[DragDebug] CPageServiceSprint.on_backlogItems_dragStart: Item drag started from backlog: {}",
						draggedFromBacklog.getId());
			}
		}
	}

	/** Handler for drop events on backlog items grid. Removes sprint items and moves them back to backlog.
	 * @param component the backlog grid component
	 * @param value     CDragDropEvent containing drop information */
	public void on_backlogItems_drop(final Component component, final Object value) {
		Check.instanceOf(value, CDragDropEvent.class, "Drop value must be CDragDropEvent");
		Check.notNull(draggedFromSprint, "No sprint item being dragged");
		final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
		final CSprintItem sprintItem = draggedFromSprint;
		LOGGER.debug("[DragDebug] CPageServiceSprint.on_backlogItems_drop: Sprint item {} (itemId: {}) dropped on backlog", sprintItem.getId(),
				sprintItem.getItemId());
		try {
			moveSprintItemToBacklog(sprintItem, event);
			refreshAfterBacklogDrop();
			CNotificationService.showSuccess("Item removed from sprint");
		} catch (final Exception e) {
			LOGGER.error("[DragDebug] Error moving item to backlog", e);
			CNotificationService.showException("Error removing item from sprint", e);
		} finally {
			draggedFromSprint = null;
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
		LOGGER.debug("[DragDebug] CPageServiceSprint.on_masterGrid_dragEnd: Clearing dragged sprint item tracker");
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
			if (draggedFromSprint != null) {
				LOGGER.debug("[DragDebug] CPageServiceSprint.on_masterGrid_dragStart: Sprint item drag started from master grid: {} (itemId: {})",
						draggedFromSprint.getId(), draggedFromSprint.getItemId());
			} else {
				LOGGER.warn("[DragDebug] CPageServiceSprint.on_masterGrid_dragStart: Could not extract sprint item from event");
			}
		}
	}

	/** Handler for drop events on master grid. Handles reordering sprint items within widgets or dropping backlog items.
	 * <p>
	 * This handler supports two scenarios:
	 * <ol>
	 * <li>Reordering sprint items within the same sprint (internal drag-drop within widgets)</li>
	 * <li>Dropping backlog items into sprint widgets (though widgets handle this directly)</li>
	 * </ol>
	 * <p>
	 * Note: Most drop logic happens at the widget level (CComponentListSprintItems), but this handler can refresh the view after complex operations
	 * or handle cross-widget drops if needed.
	 * @param component the master grid component
	 * @param value     CDragDropEvent containing drop information */
	public void on_masterGrid_drop(final Component component, final Object value) {
		LOGGER.debug("[DragDebug] CPageServiceSprint.on_masterGrid_drop: Drop event received on master grid");
		// Most reordering is handled by individual widget components (CComponentListSprintItems)
		// This handler can be used for cross-widget operations or view-level refreshes if needed
		// For now, just log - widgets handle their own reordering via on_sprintItems_drop handlers
		if (value instanceof CDragDropEvent) {
			final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
			LOGGER.debug("[DragDebug] CPageServiceSprint.on_masterGrid_drop: Target={}, Location={}", event.getTargetItem(), event.getDropLocation());
		}
	}

	public void on_name_change(final Component component, final Object value) {
		LOGGER.info("function: on_name_change for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	public void on_sprintItems_change(final Component component, final Object value) {
		LOGGER.info("function: on_backlog_clicked for Component type");
	}

	/** Handler for drag end events on sprint items grid. Clears tracking of dragged items.
	 * @param component the sprint items grid component
	 * @param value     CDragDropEvent (dragged items not available in drag end) */
	public void on_sprintItems_dragEnd(final Component component, final Object value) {
		if (value instanceof CDragDropEvent) {
			LOGGER.debug("[DragDebug] CPageServiceSprint.on_sprintItems_dragEnd: Sprint items drag operation completed");
			draggedFromSprint = null;
		}
	}

	/** Handler for drag start events on sprint items grid. Tracks items being dragged from sprint for cross-component drag-drop.
	 * @param component the sprint items grid component
	 * @param value     CDragDropEvent containing dragged items */
	public void on_sprintItems_dragStart(final Component component, final Object value) {
		if (value instanceof CDragDropEvent) {
			final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
			if (event.getDraggedItems() != null && !event.getDraggedItems().isEmpty()) {
				draggedFromSprint = (CSprintItem) event.getDraggedItem();
				LOGGER.debug("[DragDebug] CPageServiceSprint.on_sprintItems_dragStart: Sprint item drag started: {} (itemId: {})",
						draggedFromSprint.getId(), draggedFromSprint.getItemId());
			}
		}
	}

	/** Handler for drop events on sprint items grid. Handles dropping backlog items into the sprint with position-based ordering. All drop logic is
	 * centralized here in the page service for better separation of concerns.
	 * @param component the sprint items grid component
	 * @param value     CDragDropEvent containing drop information */
	public void on_sprintItems_drop(final Component component, final Object value) {
		if (!(value instanceof CDragDropEvent) || draggedFromBacklog == null) {
			return;
		}
		final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
		final CSprintItem targetItem = (CSprintItem) event.getTargetItem();
		final GridDropLocation dropLocation = event.getDropLocation();
		final CProjectItem<?> itemToAdd = draggedFromBacklog;
		LOGGER.debug(
				"[DragDebug] CPageServiceSprint.on_sprintItems_drop: Item dropped into sprint from backlog: {} at location: {} relative to target: {}",
				itemToAdd.getId(), dropLocation, targetItem != null ? targetItem.getId() : "null");
		try {
			// Determine item type
			final String itemType = itemToAdd.getClass().getSimpleName();
			// Calculate the order for the new item
			final int newOrder;
			if (targetItem == null || dropLocation == null) {
				// No target specified - add at end
				newOrder = getNextSprintItemOrder();
			} else {
				// Insert at specific position
				newOrder = calculateInsertOrder(targetItem, dropLocation);
				// Shift existing items BEFORE inserting the new one
				shiftSprintItemsForInsert(newOrder);
			}
			// Create sprint item
			final CSprintItem sprintItem = new CSprintItem();
			sprintItem.setSprint(getView().getCurrentEntity());
			sprintItem.setItemId(itemToAdd.getId());
			sprintItem.setItemType(itemType);
			sprintItem.setItemOrder(newOrder);
			sprintItem.setItem(itemToAdd);
			// Save the new item
			sprintItemService.save(sprintItem);
			// Refresh sprint items grid
			componentItemsSelection.refreshGrid();
			// Show success notification
			CNotificationService.showSuccess("Item added to sprint");
			LOGGER.debug("[DragDebug] CPageServiceSprint.on_sprintItems_drop: Sprint item created successfully: {}", sprintItem.getId());
			// Note: componentBacklogItems will refresh via the listener
		} catch (final Exception e) {
			LOGGER.error("[DragDebug] CPageServiceSprint.on_sprintItems_drop: Error adding dropped item to sprint", e);
			CNotificationService.showException("Error adding item to sprint", e);
		} finally {
			// Clear tracker
			draggedFromBacklog = null;
		}
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
		LOGGER.debug("[DragDebug] Refreshed sprint and backlog grids");
	}
	// Helper methods for drag-drop operations

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

	/** Shifts existing sprint items to make room for a new item at the specified position. All items with order >= newOrder will be incremented by 1.
	 * @param newOrder the order value where the new item will be inserted */
	private void shiftSprintItemsForInsert(final int newOrder) {
		try {
			final CSprint currentSprint = getView().getCurrentEntity();
			final List<CSprintItem> allItems = sprintItemService.findByMasterId(currentSprint.getId());
			// Sort by order in descending order to avoid conflicts during update
			allItems.sort((a, b) -> Integer.compare(b.getItemOrder(), a.getItemOrder()));
			// Shift items with order >= newOrder
			for (final CSprintItem item : allItems) {
				if (item.getItemOrder() >= newOrder) {
					item.setItemOrder(item.getItemOrder() + 1);
					sprintItemService.save(item);
					LOGGER.debug("Shifted sprint item {} from order {} to {}", item.getId(), item.getItemOrder() - 1, item.getItemOrder());
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error shifting sprint items for insert", e);
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
		Check.notNull(targetOrder, "Target item must have sprint order");
		// Calculate new order based on drop location
		final int newOrder = (dropLocation == GridDropLocation.BELOW) ? targetOrder + 1 : targetOrder;
		sprintableItem.setSprintOrder(newOrder);
		// Save the item with new order
		saveProjectItem(item);
		// Reorder other backlog items to maintain sequence
		reorderBacklogItemsAfterInsert(newOrder, item.getId());
		LOGGER.debug("[DragDebug] Updated backlog item {} order to {}", item.getId(), newOrder);
	}
}
