package tech.derbent.app.sprints.service;

import java.util.ArrayList;
import java.util.Comparator;
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
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.interfaces.IHasDragControl;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
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

	private CSprint drop_getTargetSprintFromDropTarget(final CDragDropEvent<?> event) {
		if (event.getSource() instanceof CGrid<?>) {
			// the dropped target may not be selected at all!!!
			// final CSprint sprint = (CSprint) ((CGrid<?>) event.getSource()).getSelectedEntity();
			Check.notNull(event.getTargetItem(), "Drop event target item cannot be null for sprint drop");
			if (event.getTargetItem() instanceof CSprintItem) {
				return ((CSprintItem) event.getTargetItem()).getSprint();
			} else {
				LOGGER.error("Drop event target item is not a CSprint: {}", event.getTargetItem().getClass().getSimpleName());
				return null;
			}
		} else if (event.getSource() instanceof CComponentGridEntity) {
			Check.notNull(event.getTargetItem(), "Drop event target item cannot be null for sprint drop");
			if (event.getTargetItem() instanceof CSprintItem) {
				return ((CSprintItem) event.getTargetItem()).getSprint();
			} else {
				LOGGER.error("Drop event target item is not a CSprint: {}", event.getTargetItem().getClass().getSimpleName());
				return null;
			}
		} else {
			LOGGER.error("Cannot determine target sprint from drop event source: {}", event.getSource().getClass().getSimpleName());
			return null;
		}
	}

	/** Handles reordering a backlog item within the backlog grid.
	 * @param event the drop event containing the dragged item, target position, and drop location */
	private void drop_handle_backlogItem_reorder(final CDragDropEvent<?> event) {
		try {
			LOGGER.info("[BacklogReorder] Internal backlog reordering (target: {}, dropLocation: {})",
					event.getTargetItem() != null ? event.getTargetItem().getClass().getSimpleName() : "null", event.getDropLocation());
			Check.notNull(getActiveDragStartEvent(), "No active dragged items for backlog reorder");
			final CProjectItem<?> projectItem = (CProjectItem<?>) getActiveDragStartEvent().getDraggedItems().get(0);
			Check.notNull(projectItem, "Dragged item cannot be null");
			updateBacklogItemOrder(projectItem, event);
			// Refresh backlog grid to show new order
			componentBacklogItems.refreshGrid();
			CNotificationService.showSuccess("Backlog item reordered");
		} catch (final Exception e) {
			LOGGER.error("Error reordering backlog item", e);
			CNotificationService.showException("Error reordering backlog item", e);
		}
	}

	/** Handles dropping a backlog item into a sprint widget.
	 * @param event the drop event containing target and location */
	private void drop_handle_backlogItem_toSprint(final CDragDropEvent<?> event) {
		try {
			Check.notNull(getActiveDragStartEvent(), "No active dragged items for backlog to sprint drop");
			final CSprint targetSprint = drop_getTargetSprintFromDropTarget(event);
			if (targetSprint == null) {
				LOGGER.error("Target sprint could not be determined, aborting add to sprint");
				return;
			}
			// Get dragged item from event
			final Object draggedItem = getActiveDragStartEvent().getDraggedItems().get(0);
			if (!(draggedItem instanceof CProjectItem)) {
				LOGGER.error("Dragged item is not a CProjectItem: {}", draggedItem != null ? draggedItem.getClass() : "null");
				return;
			}
			final CProjectItem<?> itemToAdd = (CProjectItem<?>) draggedItem;
			final GridDropLocation dropLocation = event.getDropLocation();
			final CSprintItem targetSprintItem = event.getTargetItem() instanceof CSprintItem ? (CSprintItem) event.getTargetItem() : null;
			drop_insertBacklogItemIntoSprint(targetSprint, itemToAdd, dropLocation, targetSprintItem);
			// refresh only needed components
			// componentBacklogItems.refreshGrid();
			refreshForEvent(event);
			// refreshAfterSprintChange();
			CNotificationService.showSuccess("Item added to sprint " + targetSprint.getName());
		} catch (final Exception e) {
			LOGGER.error("Error adding backlog item to sprint", e);
			CNotificationService.showException("Error adding item to sprint", e);
		}
	}

	/** Handles reordering sprint items within the same sprint. */
	private void drop_handle_sprintItem_reorder(final CDragDropEvent<?> event) {
		try {
			Check.notNull(event, "DragDropEvent cannot be null");
			Check.notNull(getActiveDragStartEvent(), "No active dragged items for sprint item reorder");
			final CSprintItem draggedItem = (CSprintItem) getActiveDragStartEvent().getDraggedItems().get(0);
			Check.notNull(draggedItem, "Dragged sprint item cannot be null");
			final Object targetObject = event.getTargetItem();
			if (!(targetObject instanceof CSprintItem)) {
				LOGGER.warn("Sprint item reorder skipped - target is not a sprint item (was: {})",
						targetObject != null ? targetObject.getClass().getSimpleName() : "null");
				return;
			}
			final CSprintItem targetItem = (CSprintItem) targetObject;
			if (draggedItem.getId() != null && draggedItem.getId().equals(targetItem.getId())) {
				LOGGER.debug("Sprint item reorder skipped - dragged item is target item ({})", draggedItem.getId());
				return;
			}
			final Long draggedId = draggedItem.getId();
			final Long targetId = targetItem.getId();
			if (draggedId == null || targetId == null) {
				LOGGER.warn("Sprint item reorder skipped - missing item IDs (dragged: {}, target: {})", draggedId, targetId);
				return;
			}
			final GridDropLocation dropLocation = event.getDropLocation();
			if (dropLocation == null) {
				LOGGER.warn("Sprint item reorder skipped - drop location is null");
				return;
			}
			final CSprint draggedSprint = draggedItem.getSprint();
			final CSprint targetSprint = targetItem.getSprint();
			if (draggedSprint == null || targetSprint == null || draggedSprint.getId() == null || targetSprint.getId() == null) {
				LOGGER.warn("Sprint item reorder skipped - missing sprint references (dragged sprint: {}, target sprint: {})",
						draggedSprint != null ? draggedSprint.getId() : null, targetSprint != null ? targetSprint.getId() : null);
				return;
			}
			if (!draggedSprint.getId().equals(targetSprint.getId())) {
				LOGGER.info("Sprint item reorder skipped - items belong to different sprints (dragged sprint: {}, target sprint: {})",
						draggedSprint.getId(), targetSprint.getId());
				return;
			}
			final List<CSprintItem> items = new ArrayList<>(sprintItemService.findByMasterId(draggedSprint.getId()));
			items.sort(Comparator.comparingInt(item -> item.getItemOrder() != null ? item.getItemOrder() : Integer.MAX_VALUE));
			items.removeIf(item -> item.getId() != null && item.getId().equals(draggedId));
			int targetIndex = -1;
			for (int i = 0; i < items.size(); i++) {
				if (items.get(i).getId().equals(targetItem.getId())) {
					targetIndex = i;
					break;
				}
			}
			if (targetIndex == -1) {
				LOGGER.warn("Sprint item reorder skipped - target item {} not found in sprint {}", targetItem.getId(), draggedSprint.getId());
				return;
			}
			if (dropLocation == GridDropLocation.BELOW) {
				targetIndex++;
			}
			targetIndex = Math.max(0, Math.min(targetIndex, items.size()));
			items.add(targetIndex, draggedItem);
			LOGGER.info("[SprintReorder] Reordering sprint items in sprint {}: move item {} to position {} (dropLocation: {}, target: {})",
					draggedSprint.getId(), draggedItem.getId(), targetIndex + 1, dropLocation, targetItem.getId());
			for (int i = 0; i < items.size(); i++) {
				final CSprintItem item = items.get(i);
				final int newOrder = i + 1;
				item.setItemOrder(newOrder);
				sprintItemService.save(item);
				LOGGER.debug("[SprintReorder] Saved sprint item {} with new order {}", item.getId(), newOrder);
			}
			// refreshAfterSprintChange();
			refreshForEvent(event);
			CNotificationService.showSuccess("Sprint items reordered");
		} catch (final Exception e) {
			LOGGER.error("Error reordering sprint items", e);
			CNotificationService.showException("Error reordering sprint items", e);
		}
	}

	/** Handles moving a sprint item back to the backlog (from sprint items grid to backlog).
	 * @param event the drop event containing the dragged sprint item, target position, and drop location */
	private void drop_handle_sprintItem_toBacklog(final CDragDropEvent<?> event) {
		Check.notNull(event, "DragDropEvent cannot be null");
		Check.notNull(getActiveDragStartEvent(), "No active dragged items for backlog drop");
		final CSprintItem sprintItem = (CSprintItem) getActiveDragStartEvent().getDraggedItems().get(0);
		Check.notNull(sprintItem, "Dragged sprint item cannot be null");
		final Long sourceSprintId = sprintItem.getSprint() != null ? sprintItem.getSprint().getId() : null;
		LOGGER.info("[BacklogDrop] Moving sprint item {} back to backlog (dropLocation: {}, target: {})", sprintItem.getId(), event.getDropLocation(),
				event.getTargetItem() instanceof CProjectItem ? ((CProjectItem<?>) event.getTargetItem()).getId() : null);
		try {
			drop_moveSprintItemToBacklog(sprintItem, event);
			if (sourceSprintId != null) {
				reorderSprintItemsSequential(sourceSprintId);
			}
			// Refresh grids with state preservation (selection, widget states)
			// refreshAfterBacklogDrop();
			refreshForEvent(event);
			CNotificationService.showSuccess("Item removed from sprint");
		} catch (final Exception e) {
			LOGGER.error("Error moving item to backlog", e);
			CNotificationService.showException("Error removing item from sprint", e);
		}
	}

	/** Handles moving a sprint item to a different sprint (from sprint items grid to sprint widget on master grid). This method is called when a
	 * CSprintItem is dragged from a sprint items grid and dropped onto a sprint widget in the master grid.
	 * @param event the drop event containing target and location */
	private void drop_handle_sprintItem_toSprint(final CDragDropEvent<?> event) {
		try {
			Check.notNull(event, "DragDropEvent cannot be null");
			Check.notNull(getActiveDragStartEvent(), "No active dragged items for sprint item to sprint drop");
			final CSprint targetSprint = drop_getTargetSprintFromDropTarget(event);
			if (targetSprint == null) {
				LOGGER.warn("Target sprint could not be determined for sprint item drop");
				return;
			}
			// Get dragged item from event
			final Object draggedObject = getActiveDragStartEvent().getDraggedItems().get(0);
			if (!(draggedObject instanceof CSprintItem)) {
				LOGGER.error("Dragged item is not a CSprintItem: {}", draggedObject != null ? draggedObject.getClass() : "null");
				return;
			}
			final CSprintItem draggedItem = (CSprintItem) draggedObject;
			Check.notNull(draggedItem.getSprint(), "Sprint item must have a sprint");
			final Long sourceSprintId = draggedItem.getSprint().getId();
			LOGGER.debug("Moving sprint item {} from sprint {} to sprint {}", draggedItem.getId(), sourceSprintId, targetSprint.getId());
			// Check if moving to different sprint
			if (!sourceSprintId.equals(targetSprint.getId())) {
				// Moving to different sprint
				draggedItem.setSprint(targetSprint);
				// Add at end of target sprint
				final int newOrder = getNextSprintItemOrderForSprint(targetSprint);
				draggedItem.setItemOrder(newOrder);
				LOGGER.debug("Sprint item {} moved to sprint {} with order {}", draggedItem.getId(), targetSprint.getId(), newOrder);
			} else {
				LOGGER.debug("Sprint item {} already in sprint {}, no change needed", draggedItem.getId(), targetSprint.getId());
			}
			// Save the updated item
			sprintItemService.save(draggedItem);
			if (!sourceSprintId.equals(targetSprint.getId())) {
				reorderSprintItemsSequential(sourceSprintId);
			}
			// Refresh grids
			// refreshAfterSprintChange();
			refreshForEvent(event);
			CNotificationService.showSuccess("Sprint item moved to " + targetSprint.getName());
		} catch (final Exception e) {
			LOGGER.error("Error moving sprint item to sprint", e);
			CNotificationService.showException("Error moving sprint item", e);
		}
	}

	/** Inserts a backlog item into a sprint at the correct position based on drop location and target.
	 * @param targetSprint sprint to add into
	 * @param itemToAdd    backlog item
	 * @param targetObject drop target (sprint item or sprint)
	 * @param dropLocation drop location */
	private void drop_insertBacklogItemIntoSprint(final CSprint targetSprint, final CProjectItem<?> itemToAdd, final GridDropLocation dropLocation,
			final CSprintItem targetSprintItem) {
		try {
			final List<CSprintItem> existingItems = new ArrayList<>(sprintItemService.findByMasterId(targetSprint.getId()));
			existingItems.sort(Comparator.comparingInt(item -> item.getItemOrder() != null ? item.getItemOrder() : Integer.MAX_VALUE));
			int insertIndex = existingItems.size();
			if (targetSprintItem != null) {
				for (int i = 0; i < existingItems.size(); i++) {
					if (existingItems.get(i).getId() != null && existingItems.get(i).getId().equals(targetSprintItem.getId())) {
						insertIndex = dropLocation == GridDropLocation.BELOW ? i + 1 : i;
						break;
					}
				}
			} else if (dropLocation == GridDropLocation.ABOVE) {
				// Without an explicit target item, ABOVE means prepend instead of append.
				insertIndex = 0;
			}
			insertIndex = Math.max(0, Math.min(insertIndex, existingItems.size()));
			final CSprintItem newItem = new CSprintItem();
			newItem.setSprint(targetSprint);
			newItem.setItemId(itemToAdd.getId());
			newItem.setItemType(itemToAdd.getClass().getSimpleName());
			newItem.setItem(itemToAdd);
			existingItems.add(insertIndex, newItem);
			LOGGER.info("[SprintInsert] Calculated insert index {} (existing: {})", insertIndex + 1, existingItems.size());
			for (int i = 0; i < existingItems.size(); i++) {
				final CSprintItem item = existingItems.get(i);
				final int newOrder = i + 1;
				item.setItemOrder(newOrder);
				sprintItemService.save(item);
				// LOGGER.debug("[SprintInsert] Saved sprint item {} with order {}", item.getId(), newOrder);
			}
		} catch (final Exception e) {
			LOGGER.error("Error inserting backlog item into sprint {}", targetSprint.getId(), e);
			throw e;
		}
	}

	/** Moves a sprint item back to the backlog with position-based ordering.
	 * @param sprintItem the sprint item to move
	 * @param event      the drop event containing target and location */
	private void drop_moveSprintItemToBacklog(final CSprintItem sprintItem, final CDragDropEvent<?> event) {
		final CProjectItem<?> item = sprintItem.getItem();
		Check.notNull(item, "Sprint item must have an associated project item");
		// Update sprint order if dropped at specific position
		updateBacklogItemOrder(item, event);
		// Delete sprint item (removes from sprint)
		sprintItemService.delete(sprintItem);
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

	public void on_backlogItems_dragStart(final Component component, final Object value) {
		try {
			LOGGER.info("function: on_backlogItems_dragStart for Component type: {}", component.getClass().getSimpleName());
			Check.instanceOf(value, CDragStartEvent.class, "Drop value must be CDragDropEvent");
			final CDragStartEvent<?> event = (CDragStartEvent<?>) value;
			final IHasDragControl sourceComponent = event.getSourceList().getFirst();
			LOGGER.info("=== Drag Start from Backlog === (source component: {})",
					sourceComponent != null ? sourceComponent.getClass().getSimpleName() : "null");
			setActiveDragStartEvent(event);
		} catch (final Exception e) {
			LOGGER.error("Error handling drag start on backlog items", e);
			CNotificationService.showException("Error handling drag start on backlog items", e);
		}
	}

	public void on_backlogItems_drop(final Component component, final Object value) {
		try {
			Check.notNull(getActiveDragStartEvent(), "No active dragged items for backlog drop");
			Check.instanceOf(value, CDragDropEvent.class, "Drop value must be CDragDropEvent");
			final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
			final Object draggedItem = getActiveDragStartEvent().getDraggedItems().get(0);
			LOGGER.info("=== Drop on Backlog === (draggedItem type: {}, target type: {})",
					draggedItem != null ? draggedItem.getClass().getSimpleName() : "null",
					event.getTargetItem() != null ? event.getTargetItem().getClass().getSimpleName() : "null");
			// SCENARIO 1: Internal backlog reordering (backlog → backlog)
			if (draggedItem instanceof CProjectItem) {
				drop_handle_backlogItem_reorder(event);
				return;
			}
			// SCENARIO 2: Sprint-to-backlog drop (sprint items → backlog)
			if (draggedItem instanceof CSprintItem) {
				drop_handle_sprintItem_toBacklog(event);
				return;
			}
			// SCENARIO 3: Unknown/unhandled drop scenario
			LOGGER.error("Unhandled drop on backlog - draggedItem type: {}", draggedItem != null ? draggedItem.getClass().getSimpleName() : "null");
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

	public void on_masterGrid_dragStart(final Component component, final Object value) {
		try {
			LOGGER.debug("function: on_masterGrid_dragStart for Component type: {}", component.getClass().getSimpleName());
			Check.instanceOf(value, CDragStartEvent.class, "Drop value must be CDragDropEvent");
			final CDragStartEvent<?> event = (CDragStartEvent<?>) value;
			final IHasDragControl sourceComponent = event.getSourceList().getFirst();
			LOGGER.info("=== Drag Start from Backlog === (source component: {})",
					sourceComponent != null ? sourceComponent.getClass().getSimpleName() : "null");
			setActiveDragStartEvent(event);
		} catch (final Exception e) {
			LOGGER.error("Error handling drag start on master grid", e);
			CNotificationService.showException("Error handling drag start on master grid", e);
		}
	}

	@SuppressWarnings ("unlikely-arg-type")
	public void on_masterGrid_drop(final Component component, final Object value) {
		try {
			Check.notNull(getActiveDragStartEvent(), "No active dragged items for master grid drop");
			Check.instanceOf(value, CDragDropEvent.class, "Drop value must be CDragDropEvent");
			final CDragDropEvent<?> event = (CDragDropEvent<?>) value;
			// Vaadin sets the drop target as the ComponentEvent source; drag origin lives on the stored dragStart event.
			final boolean isInternalDrag = getActiveDragStartEvent().getSourceList().contains(component);
			final Object draggedItem = getActiveDragStartEvent().getDraggedItems().get(0);
			final String dragSourceName = getActiveDragStartEvent().getSourceList().isEmpty() ? "unknown"
					: getActiveDragStartEvent().getSourceList().get(0).getClass().getSimpleName();
			LOGGER.info("=== Drop on Master Grid === (internal: {}, draggedItem type: {}, dragSource: {}, dropTarget: {})", isInternalDrag,
					draggedItem != null ? draggedItem.getClass().getSimpleName() : "null", dragSourceName,
					event.getDropTarget() != null ? event.getDropTarget().getClass().getSimpleName() : "null");
			if (draggedItem instanceof CSprintItem) {
				if (isInternalDrag && event.getTargetItem() instanceof CSprintItem) {
					final CSprintItem draggedSprintItem = (CSprintItem) draggedItem;
					final CSprintItem targetSprintItem = (CSprintItem) event.getTargetItem();
					if (draggedSprintItem.getSprint() != null && targetSprintItem.getSprint() != null && draggedSprintItem.getSprint().getId() != null
							&& draggedSprintItem.getSprint().getId().equals(targetSprintItem.getSprint().getId())) {
						drop_handle_sprintItem_reorder(event);
						return;
					}
					drop_handle_sprintItem_reorder(event);
				}
				drop_handle_sprintItem_toSprint(event);
				return;
			} else if (draggedItem instanceof CProjectItem) {
				drop_handle_backlogItem_toSprint(event);
				return;
			}
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

	private void refreshForEvent(CDragDropEvent<?> event) {
		Object first = null;
		first = event.getSourceList().get(0);
		if (first instanceof CGrid) {
			// ((CGrid<?>) first).getDataProvider().refreshAll();
			((CGrid<?>) first).refreshGrid();
		} else if (first instanceof CComponentGridEntity) {
			((CComponentGridEntity) first).refreshGrid();
		}
		final CDragStartEvent<?> startEvent = getActiveDragStartEvent();
		if (startEvent != null) {
			// first and second can be same component, so check dont refresh twice
			final Object second = startEvent.getSourceList().get(0);
			if (second instanceof CGrid && second != first) {
				((CGrid<?>) second).refreshGrid();
			} else if (second instanceof CComponentGridEntity && second != first) {
				((CComponentGridEntity) second).refreshGrid();
			}
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
						LOGGER.debug("[BacklogReorder] Shifted item {} to order {}", item.getId(), itemOrder + 1);
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error reordering backlog items after insert", e);
		}
	}

	/** Reorders all sprint items in a sprint to be sequential (1..n). */
	private void reorderSprintItemsSequential(final Long sprintId) {
		try {
			final List<CSprintItem> items = new ArrayList<>(sprintItemService.findByMasterId(sprintId));
			items.sort(Comparator.comparingInt(item -> item.getItemOrder() != null ? item.getItemOrder() : Integer.MAX_VALUE));
			for (int i = 0; i < items.size(); i++) {
				final CSprintItem item = items.get(i);
				final int newOrder = i + 1;
				item.setItemOrder(newOrder);
				sprintItemService.save(item);
				LOGGER.debug("[SprintOrder] Reordered sprint {} item {} -> order {}", sprintId, item.getId(), newOrder);
			}
		} catch (final Exception e) {
			LOGGER.error("Error reordering sprint items for sprint {}", sprintId, e);
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
			final int newOrder = getMaxBacklogOrder() + 1;
			LOGGER.info("[BacklogReorder] Dropped on empty space - assigning item {} to end of backlog (order {})", item.getId(), newOrder);
			final ISprintableItem sprintableItem = (ISprintableItem) item;
			sprintableItem.setSprintOrder(newOrder);
			saveProjectItem(item);
			return;
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
		LOGGER.info("[BacklogReorder] Placed item {} {} item {} (newOrder: {})", item.getId(),
				dropLocation == GridDropLocation.BELOW ? "below" : "above/on", targetItem.getId(), newOrder);
		// Reorder other backlog items to maintain sequence
		reorderBacklogItemsAfterInsert(newOrder, item.getId());
	}
}
