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
import tech.derbent.app.sprints.service.CSprintItemService;
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
			
			// Register the component itself (not just the grid) for automatic drag-drop method binding
			// This enables CPageService to automatically bind on_sprintItems_dragStart/dragEnd/drop handlers
			registerComponent("sprintItems", componentItemsSelection);
			// Re-bind methods to include the newly registered component
			bindMethods(this);
			LOGGER.debug("[DragDebug] Sprint items component registered and drag-drop handlers bound");
			
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
			
			// Register the component itself for automatic drag-drop method binding
			// This enables CPageService to automatically bind on_backlogItems_dragStart/dragEnd handlers
			registerComponent("backlogItems", componentBacklogItems);
			// Re-bind methods to include the newly registered component
			bindMethods(this);
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

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return projectItemStatusService; }

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

	/** Handler for drop events on backlog items grid. Handles dropping sprint items back into the backlog with position-based ordering.
	 * All drop logic is centralized here in the page service for better separation of concerns.
	 * @param component the backlog grid component
	 * @param value     CDragDropEvent containing drop information */
	public void on_backlogItems_drop(final Component component, final Object value) {
		if (!(value instanceof CDragDropEvent) || draggedFromSprint == null) {
			return;
		}
		
		final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
		final CProjectItem<?> targetBacklogItem = (CProjectItem<?>) event.getTargetItem();
		final GridDropLocation dropLocation = event.getDropLocation();
		final CSprintItem sprintItem = draggedFromSprint;
		final CProjectItem<?> item = sprintItem.getItem();
		
		LOGGER.debug("[DragDebug] CPageServiceSprint.on_backlogItems_drop: Sprint item dropped back into backlog: {} (itemId: {}) at location: {} relative to target: {}",
				sprintItem.getId(), item != null ? item.getId() : "null", dropLocation, targetBacklogItem != null ? targetBacklogItem.getId() : "null");
		
		if (item == null) {
			LOGGER.warn("Sprint item has no associated project item, cannot move to backlog");
			draggedFromSprint = null;
			return;
		}
		
		try {
			// Set sprint order based on drop position
			if (targetBacklogItem != null && dropLocation != null && item instanceof ISprintableItem) {
				final ISprintableItem sprintableItem = (ISprintableItem) item;
				final ISprintableItem targetSprintableItem =
						targetBacklogItem instanceof ISprintableItem ? (ISprintableItem) targetBacklogItem : null;
				if (targetSprintableItem != null) {
					// Calculate new sprint order based on drop location
					final Integer targetOrder = targetSprintableItem.getSprintOrder();
					if (targetOrder != null) {
						if (dropLocation == GridDropLocation.BELOW) {
							sprintableItem.setSprintOrder(targetOrder + 1);
						} else {
							sprintableItem.setSprintOrder(targetOrder);
						}
						// Save the updated sprint order
						if (item instanceof CActivity) {
							activityService.save((CActivity) item);
						} else if (item instanceof CMeeting) {
							meetingService.save((CMeeting) item);
						}
						// Reorder other backlog items
						reorderBacklogItemsAfterInsert(sprintableItem.getSprintOrder(), item.getId());
					}
				}
			}
			
			// Remove from sprint
			sprintItemService.delete(sprintItem);
			
			// Refresh both grids
			componentItemsSelection.refreshGrid();
			componentBacklogItems.refreshGrid();
			
			CNotificationService.showSuccess("Item removed from sprint");
			
			LOGGER.debug("[DragDebug] CPageServiceSprint.on_backlogItems_drop: Item successfully moved to backlog");
		} catch (final Exception e) {
			LOGGER.error("[DragDebug] CPageServiceSprint.on_backlogItems_drop: Error moving item to backlog", e);
			CNotificationService.showException("Error removing item from sprint", e);
		} finally {
			// Clear tracker
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

	/** Handler for drop events on sprint items grid. Handles dropping backlog items into the sprint with position-based ordering.
	 * All drop logic is centralized here in the page service for better separation of concerns.
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
		
		LOGGER.debug("[DragDebug] CPageServiceSprint.on_sprintItems_drop: Item dropped into sprint from backlog: {} at location: {} relative to target: {}",
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
	
	/** Shifts existing sprint items to make room for a new item at the specified position.
	 * All items with order >= newOrder will be incremented by 1.
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

	public void on_status_change(final Component component, final Object value) {
		LOGGER.info("function: on_status_change for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	@Override
	public void populateForm() {
		LOGGER.debug("populateForm called - CComponentListSprintItems receives entity updates via IContentOwner interface");
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
							meetingService.save((tech.derbent.app.meetings.domain.CMeeting) item);
						}
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error reordering backlog items after insert", e);
		}
	}
}
