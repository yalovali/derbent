package tech.derbent.app.sprints.service;

import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.screens.view.CComponentGridEntity;
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
			componentBacklogItems.setDropEnabled(true);
			// Register with page service using unified auto-registration pattern
			componentBacklogItems.registerWithPageService(this);
		}
		return componentBacklogItems;
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
		LOGGER.info("[DropTargetDebug] Determining target sprint - targetItem: {}, dropTarget: {}",
				targetItem != null
						? targetItem.getClass().getSimpleName() + "#" + (targetItem instanceof CEntityDB ? ((CEntityDB<?>) targetItem).getId() : "?")
						: "null",
				event.getDropTarget() != null ? event.getDropTarget().getClass().getSimpleName() : "null");
		if (targetItem == null) {
			// no target sprintitem under mouse
			Check.instanceOf(event.getDropTarget(), CComponentGridEntity.class, "Drop target must be CComponentGridEntity when target item is null");
			// Dropped on empty area of grid - treat as dropping on the sprint itself
			final CComponentGridEntity dropTargetGrid = (CComponentGridEntity) event.getDropTarget();
			final CSprint sprint = (CSprint) dropTargetGrid.getSelectedItem();
			LOGGER.info("[DropTargetDebug] No target item - using selected sprint from grid: Sprint#{}", sprint != null ? sprint.getId() : "null");
			return sprint;
		}
		if (targetItem instanceof CSprint) {
			LOGGER.info("[DropTargetDebug] Target is Sprint#{}", ((CSprint) targetItem).getId());
			return (CSprint) targetItem;
		} else if (targetItem instanceof CSprintItem) {
			final CSprint sprint = ((CSprintItem) targetItem).getSprint();
			LOGGER.info("[DropTargetDebug] Target is CSprintItem#{}, belongs to Sprint#{}", ((CSprintItem) targetItem).getId(),
					sprint != null ? sprint.getId() : "null");
			return sprint;
		} else {
			LOGGER.warn("[DropTargetDebug] Target is not a Sprint or SprintItem (it's {}), cannot add backlog item to sprint",
					targetItem.getClass().getSimpleName());
			return null;
		}
	}

	/** Handles reordering a backlog item within the backlog grid.
	 * @param event the drop event containing the dragged item, target position, and drop location */
	private void handleBacklogItemReorder(final CDragDropEvent<?> event) {
		LOGGER.info("Internal backlog reordering");
		try {
			final CProjectItem<?> projectItem = (CProjectItem<?>) event.getDraggedItem();
			Check.notNull(projectItem, "Dragged item cannot be null");
			updateBacklogItemOrder(projectItem, event);
			// Refresh backlog grid to show new order
			if (componentBacklogItems != null) {
				componentBacklogItems.refreshGrid();
			}
			CNotificationService.showSuccess("Backlog item reordered");
		} catch (final Exception e) {
			LOGGER.error("Error reordering backlog item", e);
			CNotificationService.showException("Error reordering backlog item", e);
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
			// Get dragged item from event
			final Object draggedItem = event.getDraggedItem();
			if (!(draggedItem instanceof CProjectItem)) {
				LOGGER.error("Dragged item is not a CProjectItem: {}", draggedItem != null ? draggedItem.getClass() : "null");
				return;
			}
			final CProjectItem<?> itemToAdd = (CProjectItem<?>) draggedItem;
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
		}
	}

	/** Handles moving a sprint item to a different sprint (from sprint items grid to sprint widget on master grid).
	 * This method is called when a CSprintItem is dragged from a sprint items grid and dropped onto a sprint widget in the master grid.
	 * @param event the drop event containing target and location */
	private void handleSprintItemToSprintDrop(final CDragDropEvent<?> event) {
		try {
			final Object targetItem = event.getTargetItem();
			if (!(targetItem instanceof CSprint)) {
				LOGGER.warn("Target is not a Sprint, cannot move sprint item");
				return;
			}
			final CSprint targetSprint = (CSprint) targetItem;
			// Get dragged item from event
			final Object draggedObject = event.getDraggedItem();
			if (!(draggedObject instanceof CSprintItem)) {
				LOGGER.error("Dragged item is not a CSprintItem: {}", draggedObject != null ? draggedObject.getClass() : "null");
				return;
			}
			final CSprintItem draggedItem = (CSprintItem) draggedObject;
			Check.notNull(draggedItem.getSprint(), "Sprint item must have a sprint");
			LOGGER.debug("Moving sprint item {} from sprint {} to sprint {}", 
					draggedItem.getId(), 
					draggedItem.getSprint().getId(), 
					targetSprint.getId());
			// Check if moving to different sprint
			if (!draggedItem.getSprint().getId().equals(targetSprint.getId())) {
				// Moving to different sprint
				draggedItem.setSprint(targetSprint);
				// Add at end of target sprint
				final int newOrder = getNextSprintItemOrderForSprint(targetSprint);
				draggedItem.setItemOrder(newOrder);
				LOGGER.debug("Sprint item {} moved to sprint {} with order {}", 
						draggedItem.getId(), targetSprint.getId(), newOrder);
			} else {
				LOGGER.debug("Sprint item {} already in sprint {}, no change needed", 
						draggedItem.getId(), targetSprint.getId());
			}
			// Save the updated item
			sprintItemService.save(draggedItem);
			// Refresh grids
			refreshAfterSprintChange();
			CNotificationService.showSuccess("Sprint item moved to " + targetSprint.getName());
		} catch (final Exception e) {
			LOGGER.error("Error moving sprint item to sprint", e);
			CNotificationService.showException("Error moving sprint item", e);
		}
	}

	/** Handles moving a sprint item back to the backlog (from sprint items grid to backlog).
	 * @param event the drop event containing the dragged sprint item, target position, and drop location */
	private void handleSprintItemToBacklogDrop(final CDragDropEvent<?> event) {
		final CSprintItem sprintItem = (CSprintItem) event.getDraggedItem();
		Check.notNull(sprintItem, "Dragged sprint item cannot be null");
		LOGGER.info("Moving sprint item {} back to backlog", sprintItem.getId());
		try {
			moveSprintItemToBacklog(sprintItem, event);
			// Refresh grids with state preservation (selection, widget states)
			refreshAfterBacklogDrop();
			CNotificationService.showSuccess("Item removed from sprint");
		} catch (final Exception e) {
			LOGGER.error("Error moving item to backlog", e);
			CNotificationService.showException("Error removing item from sprint", e);
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
		if (selectedItems.isEmpty() || selectedItems.size() > 1) {
			componentItemDetails.setValue(null);
			return;
		}
		final Object itemObject = selectedItems.iterator().next();
		Check.instanceOf(itemObject, CEntityNamed.class, "Value must be of type CEntityNamed<?>");
		final CEntityNamed<?> item = (CEntityNamed<?>) itemObject;
		componentItemDetails.setValue(item);
	}

	@SuppressWarnings ("unlikely-arg-type")
	public void on_backlogItems_drop(final Component component, final Object value) {
		try {
			Check.instanceOf(value, CDragDropEvent.class, "Drop value must be CDragDropEvent");
			final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
			// Check if this is an internal drag (source list contains the drop target itself)
			// When target and source are the same, it's a reordering request
			final boolean isInternalDrag = event.getSourceList().contains(component);
			final Object draggedItem = event.getDraggedItem();
			LOGGER.info("=== Drop on Backlog === (internal: {}, draggedItem type: {})", isInternalDrag,
					draggedItem != null ? draggedItem.getClass().getSimpleName() : "null");
			
			// SCENARIO 1: Internal backlog reordering (backlog → backlog)
			if (isInternalDrag && draggedItem instanceof CProjectItem) {
				handleBacklogItemReorder(event);
				return;
			}
			
			// SCENARIO 2: Sprint-to-backlog drop (sprint items → backlog)
			if (draggedItem instanceof CSprintItem) {
				handleSprintItemToBacklogDrop(event);
				return;
			}
			
			// SCENARIO 3: Unknown/unhandled drop scenario
			LOGGER.error("Unhandled drop on backlog - draggedItem type: {}, isInternal: {}",
					draggedItem != null ? draggedItem.getClass().getSimpleName() : "null", isInternalDrag);
		} catch (final Exception e) {
			LOGGER.error("Error handling drop on backlog", e);
			CNotificationService.showException("Error handling drop on backlog", e);
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

	public void on_masterGrid_drop(final Component component, final Object value) {
		try {
			Check.instanceOf(value, CDragDropEvent.class, "Drop value must be CDragDropEvent");
			final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
			final boolean isInternalDrag = event.getSourceList().contains(component);
			final Object draggedItem = event.getDraggedItem();
			LOGGER.info("=== Drop on Master Grid === (internal: {}, draggedItem type: {})", isInternalDrag,
					draggedItem != null ? draggedItem.getClass().getSimpleName() : "null");
			
			// SCENARIO 1: Internal drag on master grid (reordering sprints themselves)
			if (isInternalDrag && draggedItem instanceof CSprint) {
				LOGGER.info("Internal sprint reordering not yet implemented");
				CNotificationService.showInfo("Sprint reordering will be implemented in a future update");
				return;
			}
			
			// SCENARIO 2: Drag from backlog to master grid (add backlog item to sprint)
			if (draggedItem instanceof CProjectItem) {
				handleBacklogToSprintDrop(event);
				return;
			}
			
			// SCENARIO 3: Drag sprint item from sprint items grid to different sprint
			if (draggedItem instanceof CSprintItem) {
				handleSprintItemToSprintDrop(event);
				return;
			}
			
			// Unhandled scenario
			LOGGER.error("Unhandled drop on master grid - draggedItem type: {}, isInternal: {}",
					draggedItem != null ? draggedItem.getClass().getSimpleName() : "null", isInternalDrag);
		} catch (final Exception e) {
			LOGGER.error("Error handling drop on master grid", e);
			CNotificationService.showException("Error handling drop on master grid", e);
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
	/** Refreshes all grids after a backlog drop operation with state preservation.
	 * <p>
	 * This method ensures proper state preservation during refresh:
	 * <ol>
	 * <li>Widget state is automatically saved in CComponentGridEntity.unregisterAllWidgetComponents()</li>
	 * <li>All grids are refreshed with new data</li>
	 * <li>Widget state is automatically restored in CComponentWidgetEntity.initializeWidget()</li>
	 * <li>Grid selection is preserved in CComponentGridEntity.refreshGridData()</li>
	 * </ol>
	 * </p>
	 * <p>
	 * <b>Components refreshed:</b>
	 * <ul>
	 * <li>Sprint items grid (componentItemsSelection)</li>
	 * <li>Backlog grid (componentBacklogItems)</li>
	 * <li>Master grid showing sprint widgets (getView().refreshGrid())</li>
	 * </ul>
	 * </p>
	 */
	private void refreshAfterBacklogDrop() {
		// Refresh sprint items list
		if (componentItemsSelection != null) {
			componentItemsSelection.refreshGrid();
		}
		// Refresh backlog
		if (componentBacklogItems != null) {
			componentBacklogItems.refreshGrid();
		}
		// Refresh master grid to update sprint widgets
		// This will trigger widget state save/restore automatically
		try {
			getView().refreshGrid();
		} catch (final Exception e) {
			LOGGER.error("Error refreshing master grid after backlog drop", e);
		}
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
		final int newOrder = dropLocation == GridDropLocation.BELOW ? targetOrder + 1 : targetOrder;
		sprintableItem.setSprintOrder(newOrder);
		// Save the item with new order
		saveProjectItem(item);
		// Reorder other backlog items to maintain sequence
		reorderBacklogItemsAfterInsert(newOrder, item.getId());
	}
}
