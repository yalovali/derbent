package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.dnd.GridDragEndEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IEntitySelectionDialogSupport;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.dialogs.CDialogEntitySelection;
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

/** CComponentListSprintItems - Component for managing CSprintItems in a CSprint. Provides full CRUD functionality for sprint items with ordering and
 * type selection. Supports drag and drop from backlog to sprint items.
 * <p>
 * Features inherited from CComponentListEntityBase:
 * <ul>
 * <li>Grid display with ID, Order, Type, Name columns</li>
 * <li>Add operation with type selector dialog (CActivity or CMeeting)</li>
 * <li>Edit/Delete operations</li>
 * <li>Move up/down for reordering</li>
 * <li>Selection management</li>
 * <li>Drop target for receiving items from backlog via drag and drop</li>
 * </ul>
 * <p>
 * Implements IContentOwner to receive automatic entity updates from the form builder's binder when a sprint is selected.
 * <p>
 * Implements IEntitySelectionDialogSupport to provide standardized entity selection dialog configuration.
 * <p>
 * Implements IDropTarget to receive dropped items from backlog component. */
public class CComponentListSprintItems extends CComponentListEntityBase<CSprint, CSprintItem>
		implements IEntitySelectionDialogSupport<CProjectItem<?>> {

	// Item type constants
	private static final String ITEM_TYPE_ACTIVITY = "CActivity";
	private static final String ITEM_TYPE_MEETING = "CMeeting";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListSprintItems.class);
	private static final long serialVersionUID = 1L;
	private final CActivityService activityService;
	// Drag source support for reverse drag to backlog (manual implementation)
	private boolean dragEnabledToBacklog = false;
	// Services for loading items
	private final CMeetingService meetingService;

	/** Constructor for CComponentListSprintItems.
	 * @param sprintItemService The service for CSprintItem operations
	 * @param activityService   The service for loading activities
	 * @param meetingService    The service for loading meetings */
	public CComponentListSprintItems(final CSprintItemService sprintItemService, final CActivityService activityService,
			final CMeetingService meetingService) {
		super("Sprint Items", CSprint.class, CSprintItem.class, sprintItemService);
		Check.notNull(sprintItemService, "SprintItemService cannot be null");
		Check.notNull(activityService, "ActivityService cannot be null");
		Check.notNull(meetingService, "MeetingService cannot be null");
		this.activityService = activityService;
		this.meetingService = meetingService;
		// Enable dynamic height so grid resizes with content
		setDynamicHeight("600px");
		LOGGER.debug("CComponentListSprintItems created with dynamic height enabled");
	}

	@Override
	public void configureGrid(final CGrid<CSprintItem> grid) {
		Check.notNull(grid, "Grid cannot be null");
		// LOGGER.debug("Configuring grid columns for CSprintItem");
		grid.addIdColumn(CSprintItem::getId, "ID", "id");
		// grid.addIntegerColumn(CSprintItem::getItemOrder, "Order", "order");
		grid.addShortTextColumn(CSprintItem::getItemType, "Type", "type");
		// Use expanding column for Name to fill remaining width
		grid.addShortTextColumn(item -> {
			return item.getItem().getName();
		}, "Name", "name");
		grid.addExpandingLongTextColumn(item -> {
			return item.getItem().getDescriptionShort();
		}, "description", "Description");
		// Add story points column
		grid.addIntegerColumn(item -> {
			if (item.getItem() instanceof tech.derbent.api.interfaces.ISprintableItem) {
				final Long storyPoint = ((tech.derbent.api.interfaces.ISprintableItem) item.getItem()).getStoryPoint();
				return storyPoint != null ? storyPoint.intValue() : null;
			}
			return null;
		}, "Story Points", "storyPoint");
		try {
			grid.addEntityColumn(item -> {
				return item.getItem().getStatus();
			}, "Status", "status", CSprintItem.class);
		} catch (final Exception e) {
			LOGGER.error("Error adding status column: {}", e.getMessage(), e);
		}
	}

	@Override
	protected CButton create_buttonAdd() {
		// Use the base class implementation but with a list select icon
		final CButton buttonAdd = new CButton(VaadinIcon.LIST_SELECT.create());
		buttonAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonAdd.setTooltipText("Add items to sprint");
		buttonAdd.addClickListener(e -> on_buttonAdd_clicked());
		return buttonAdd;
	}

	/** Populates the component by refreshing the grid with sprint items. This method is called automatically by CFormBuilder when the binder's entity
	 * changes. */
	@Override
	protected CSprintItem createNewEntity() {
		Check.fail("Not used in this context - the component handles entity creation via the selection dialog");
		return null;
	}

	/** Enables drag-and-drop reordering within the sprint items grid. This allows users to reorder sprint items by dragging and dropping them within
	 * the grid. The itemOrder field is automatically updated to reflect the new order. */
	public void enableDragAndDropReordering() {
		final CGrid<CSprintItem> grid = getGrid();
		if (grid == null) {
			LOGGER.warn("Grid not available for drag-and-drop configuration");
			return;
		}
		// Enable row dragging for internal reordering
		grid.setRowsDraggable(true);
		// Track the dragged item
		final CSprintItem[] draggedItem = new CSprintItem[1];
		// Add drag start listener
		grid.addDragStartListener(event -> {
			draggedItem[0] = event.getDraggedItems().isEmpty() ? null : event.getDraggedItems().get(0);
			if (draggedItem[0] != null) {
				LOGGER.debug("Started dragging sprint item: {} (order: {})", draggedItem[0].getId(), draggedItem[0].getItemOrder());
			}
		});
		// Add drag end listener
		grid.addDragEndListener(event -> {
			LOGGER.debug("Drag ended");
			draggedItem[0] = null;
		});
		// Add drop listener for internal reordering
		grid.addDropListener(event -> {
			final CSprintItem targetItem = event.getDropTargetItem().orElse(null);
			final GridDropLocation dropLocation = event.getDropLocation();
			if (draggedItem[0] == null || targetItem == null) {
				LOGGER.debug("Drag or target item is null, ignoring drop");
				return;
			}
			if (draggedItem[0].getId().equals(targetItem.getId())) {
				LOGGER.debug("Item dropped on itself, ignoring");
				return;
			}
			try {
				handleInternalReordering(draggedItem[0], targetItem, dropLocation);
			} catch (final Exception e) {
				LOGGER.error("Error handling internal reordering", e);
				CNotificationService.showException("Error reordering sprint items", e);
			}
		});
		LOGGER.debug("Drag-and-drop reordering enabled for sprint items grid");
	}

	@Override
	public CComponentEntitySelection.AlreadySelectedMode getAlreadySelectedMode() {
		return CComponentEntitySelection.AlreadySelectedMode.HIDE_ALREADY_SELECTED;
	}

	/** Returns a provider for already-selected items (items currently in the sprint). This returns items based on the current sprint's members to
	 * enable the dialog to hide or pre-select them.
	 * @return ItemsProvider that returns the current sprint's items filtered by entity type */
	@Override
	public CComponentEntitySelection.ItemsProvider<CProjectItem<?>> getAlreadySelectedProvider() {
		final CComponentEntitySelection.ItemsProvider<CProjectItem<?>> provider = config -> {
			try {
				final CSprint sprint = getMasterEntity();
				if ((sprint == null) || (sprint.getId() == null)) {
					LOGGER.debug("No sprint available for already selected items");
					return new ArrayList<>();
				}
				// Get current sprint items
				final CSprintItemService service = (CSprintItemService) childService;
				final List<CSprintItem> sprintItems = service.findByMasterIdWithItems(sprint.getId());
				// Filter by entity type and extract the underlying items
				final List<CProjectItem<?>> result = new ArrayList<>();
				final String targetType = config.getEntityClass().getSimpleName();
				for (final CSprintItem sprintItem : sprintItems) {
					if ((sprintItem.getItem() != null) && targetType.equals(sprintItem.getItemType())) {
						result.add(sprintItem.getItem());
					}
				}
				LOGGER.debug("Found {} already selected items of type {}", result.size(), targetType);
				return result;
			} catch (final Exception e) {
				LOGGER.error("Error loading already selected items for entity type: {}", config.getDisplayName(), e);
				return new ArrayList<>();
			}
		};
		return provider;
	}

	/** Returns the component name for method binding.
	 * <p>
	 * This component uses "sprintItems" as its name, enabling automatic binding to page service handlers like on_sprintItems_dragStart,
	 * on_sprintItems_drop, etc.
	 * @return The component name "sprintItems" */
	@Override
	public String getComponentName() { return "sprintItems"; }

	@Override
	public List<CComponentEntitySelection.EntityTypeConfig<?>> getDialogEntityTypes() {
		final List<CComponentEntitySelection.EntityTypeConfig<?>> entityTypes = new ArrayList<>();
		entityTypes.add(new CComponentEntitySelection.EntityTypeConfig<>(ITEM_TYPE_ACTIVITY, CActivity.class, activityService));
		entityTypes.add(new CComponentEntitySelection.EntityTypeConfig<>(ITEM_TYPE_MEETING, CMeeting.class, meetingService));
		return entityTypes;
	}

	@Override
	public String getDialogTitle() { return "Select Items to Add to Sprint"; }

	/** Gets the current drop handler.
	 * @return the drop handler, or null if not set
	 * @deprecated Drop handling moved to CPageService */
	@Deprecated
	public Consumer<CProjectItem<?>> getDropHandler() { return null; }

	@Override
	@SuppressWarnings ("unchecked")
	public CComponentEntitySelection.ItemsProvider<CProjectItem<?>> getItemsProvider() {
		final CComponentEntitySelection.ItemsProvider<CProjectItem<?>> itemsProvider = config -> {
			try {
				final CProject project = getMasterEntity() != null ? getMasterEntity().getProject() : null;
				if (project == null) {
					LOGGER.warn("No project available for loading items");
					return new ArrayList<>();
				}
				if (config.getEntityClass() == CActivity.class) {
					return (List<CProjectItem<?>>) (List<?>) activityService.listByProject(project);
				} else if (config.getEntityClass() == CMeeting.class) {
					return (List<CProjectItem<?>>) (List<?>) meetingService.listByProject(project);
				}
				return new ArrayList<>();
			} catch (final Exception e) {
				LOGGER.error("Error loading items for entity type: {}", config.getDisplayName(), e);
				return new ArrayList<>();
			}
		};
		return itemsProvider;
	}

	@Override
	protected Integer getNextOrder() {
		Check.notNull(getMasterEntity(), "Current sprint cannot be null when getting next order");
		if (getMasterEntity().getId() == null) {
			LOGGER.debug("Sprint is new, starting order at 1");
			return 1;
		}
		final CSprintItemService service = (CSprintItemService) childService;
		final List<CSprintItem> items = service.findByMasterId(getMasterEntity().getId());
		final int nextOrder = items.size() + 1;
		LOGGER.debug("Next item order for sprint {}: {}", getMasterEntity().getId(), nextOrder);
		return nextOrder;
	}

	@Override
	public Consumer<List<CProjectItem<?>>> getSelectionHandler() {
		return selectedItems -> {
			LOGGER.debug("Selected {} items from entity selection dialog", selectedItems.size());
			int addedCount = 0;
			for (final CProjectItem<?> item : selectedItems) {
				try {
					// Determine item type
					final String itemType = item.getClass().getSimpleName();
					// Create sprint item
					final CSprintItem sprintItem = new CSprintItem();
					sprintItem.setSprint(getMasterEntity());
					sprintItem.setItemId(item.getId());
					sprintItem.setItemType(itemType);
					sprintItem.setItemOrder(getNextOrder() + addedCount);
					sprintItem.setItem(item);
					// Save
					childService.save(sprintItem);
					addedCount++;
				} catch (final Exception e) {
					LOGGER.error("Error adding item {} to sprint", item.getId(), e);
				}
			}
			if (addedCount > 0) {
				// Update self: refresh grid
				refreshGrid();
				CNotificationService.showSuccess(addedCount + " items added to sprint");
				// Notify listeners once for batch add (Update-Then-Notify pattern)
				notifyRefreshListeners(null);
			}
		};
	}

	/** Handles reordering of sprint items when dropped within the same grid. Uses the standard service move methods for consistency with up/down
	 * buttons.
	 * @param draggedItem  the sprint item being dragged
	 * @param targetItem   the sprint item it's being dropped on/near
	 * @param dropLocation where relative to target (ABOVE, BELOW, ON_TOP) */
	private void handleInternalReordering(final CSprintItem draggedItem, final CSprintItem targetItem, final GridDropLocation dropLocation) {
		LOGGER.debug("Handling internal reorder: dragged={} (order={}), target={} (order={}), location={}", draggedItem.getId(),
				draggedItem.getItemOrder(), targetItem.getId(), targetItem.getItemOrder(), dropLocation);
		// Get current items from grid
		final List<CSprintItem> currentItems = getGrid().getListDataView().getItems().toList();
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
		// Use the generalized service-based reordering from base class
		// This ensures consistency with up/down buttons
		final int moves = reorderItemByMoving(draggedItem, draggedIndex, newPosition, currentItems);
		if (moves > 0) {
			LOGGER.debug("Reordered sprint item using {} service move operations", moves);
			// Update self and refresh grid
			refreshGrid();
			CNotificationService.showSuccess("Sprint items reordered");
			// Notify listeners (Update-Then-Notify pattern)
			notifyRefreshListeners(draggedItem);
		}
	}

	/** Checks if dragging to backlog is enabled.
	 * @return true if drag to backlog is enabled */
	public boolean isDragToBacklogEnabled() { return dragEnabledToBacklog; }

	/** Checks if dropping is currently enabled for this component.
	 * @return true if drops are enabled (handler is set), false otherwise
	 * @deprecated Drop handling moved to CPageService */
	@Deprecated
	public boolean isDropEnabled() { return false; }

	@Override
	protected List<CSprintItem> loadItems(final CSprint master) {
		Check.notNull(master, "Master sprint cannot be null when loading items");
		LOGGER.debug("Loading sprint items for sprint: {}", master.getId() != null ? master.getId() : "null");
		if (master.getId() == null) {
			LOGGER.debug("Master sprint is new, returning empty list");
			return List.of();
		}
		final CSprintItemService service = (CSprintItemService) childService;
		final List<CSprintItem> items = service.findByMasterIdWithItems(master.getId());
		Check.notNull(items, "Loaded sprint items cannot be null");
		LOGGER.debug("Loaded {} sprint items", items.size());
		return items;
	}

	@Override
	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	protected void on_buttonAdd_clicked() {
		try {
			LOGGER.debug("Opening entity selection dialog: {}", getDialogTitle());
			// Use interface methods for dialog configuration
			// Raw types are required due to complex generic constraints between CDialogEntitySelection and Consumer
			final CDialogEntitySelection<CProjectItem<?>> dialog = new CDialogEntitySelection(getDialogTitle(), getDialogEntityTypes(),
					getItemsProvider(), getSelectionHandler(), isMultiSelect(), getAlreadySelectedProvider(), getAlreadySelectedMode());
			dialog.open();
		} catch (final Exception ex) {
			LOGGER.error("Error opening entity selection dialog", ex);
			CNotificationService.showException("Error opening selection dialog", ex);
		}
	}

	/** Handle delete button click. Deletes the selected item after confirmation and notifies listeners. */
	@Override
	protected void on_buttonDelete_clicked() {
		Check.notNull(getSelectedItem(), "No item selected for deletion");
		Check.notNull(getSelectedItem().getId(), "Cannot delete unsaved item");
		try {
			LOGGER.debug("Deleting sprint item: {}", getSelectedItem().getId());
			final CSprintItem itemToDelete = getSelectedItem();
			childService.delete(itemToDelete);
			// Update self: refresh grid
			refreshGrid();
			getGrid().asSingleSelect().clear();
			CNotificationService.showDeleteSuccess();
			// Notify listeners (Update-Then-Notify pattern)
			notifyRefreshListeners(itemToDelete);
		} catch (final Exception e) {
			LOGGER.error("Error deleting sprint item", e);
			CNotificationService.showException("Error deleting item", e);
		}
	}
	// IDropTarget implementation

	/** Handle drag end event from the internal grid. Logs the event for debugging drag-drop propagation chain.
	 * @param event The GridDragEndEvent from the grid */
	@Override
	protected void on_grid_dragEnd(final GridDragEndEvent<CSprintItem> event) {
		super.on_grid_dragEnd(event);
		try {
			LOGGER.debug("[DragDebug] CComponentListSprintItems: dragEnd - source=grid");
		} catch (final Exception ex) {
			LOGGER.error("Error in drag end handler", ex);
		}
	}

	/** Handle drag start event from the internal grid. Logs the event for debugging drag-drop propagation chain.
	 * @param event The GridDragStartEvent from the grid */
	@Override
	protected void on_grid_dragStart(final GridDragStartEvent<CSprintItem> event) {
		super.on_grid_dragStart(event);
		try {
			final int itemCount = event.getDraggedItems() != null ? event.getDraggedItems().size() : 0;
			final Long firstItemId =
					!event.getDraggedItems().isEmpty() && event.getDraggedItems().get(0) != null ? event.getDraggedItems().get(0).getId() : null;
			LOGGER.debug("[DragDebug] CComponentListSprintItems: dragStart - source=grid, items={}, firstItemId={}", itemCount, firstItemId);
		} catch (final Exception ex) {
			LOGGER.error("Error in drag start handler", ex);
		}
	}

	@Override
	protected void on_gridItems_doubleClicked(final CSprintItem item) {
		// Override to prevent edit dialog from opening on double-click
		Check.notNull(item, "Double-clicked item cannot be null");
		LOGGER.debug("Double-clicked sprint item: {} (edit not supported)", item.getId());
		setSelectedItem(item);
		updateButtonStates(true);
		// Don't call openEditDialog - just select the item
	}

	@Override
	protected void openEditDialog(final CSprintItem entity, final Consumer<CSprintItem> saveCallback, final boolean isNew) {
		Check.notNull(entity, "Entity cannot be null when opening edit dialog");
		LOGGER.warn("Edit operation not supported for sprint items - ID: {}", entity.getId());
		CNotificationService
				.showWarning("Sprint items cannot be edited directly. Please delete this item and add a new one if you need to change it.");
	}

	// Drag support for reverse drag to backlog (manual, not via interface to avoid method erasure conflicts)
	/** Enables or disables dragging sprint items back to backlog.
	 * @param enabled true to enable dragging to backlog */
	public void setDragToBacklogEnabled(final boolean enabled) {
		dragEnabledToBacklog = enabled;
		final var grid = getGrid();
		if (grid != null) {
			grid.setRowsDraggable(enabled);
			LOGGER.debug("Drag to backlog from sprint items {}", enabled ? "enabled" : "disabled");
		}
	}
}
