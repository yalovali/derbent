package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IDropTarget;
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
		implements IEntitySelectionDialogSupport<CProjectItem<?>>, IDropTarget<CProjectItem<?>> {

	// Item type constants
	private static final String ITEM_TYPE_ACTIVITY = "CActivity";
	private static final String ITEM_TYPE_MEETING = "CMeeting";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListSprintItems.class);
	private static final long serialVersionUID = 1L;
	private final CActivityService activityService;
	// Drop target support
	private Consumer<CProjectItem<?>> dropHandler = null;
	// Services for loading items
	private final CMeetingService meetingService;
	// Listener for item changes
	private Consumer<CSprintItem> onItemChangeListener;

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
		LOGGER.debug("CComponentListSprintItems created");
	}

	/** Handles adding a dropped item to the sprint. This is called by the drop handler.
	 * @param item the project item to add to the sprint */
	public void addDroppedItem(final CProjectItem<?> item) {
		try {
			Check.notNull(item, "Dropped item cannot be null");
			LOGGER.debug("Adding dropped item to sprint: {} ({})", item.getId(), item.getClass().getSimpleName());
			// Determine item type
			final String itemType = item.getClass().getSimpleName();
			// Create sprint item
			final CSprintItem sprintItem = new CSprintItem();
			sprintItem.setSprint(getMasterEntity());
			sprintItem.setItemId(item.getId());
			sprintItem.setItemType(itemType);
			sprintItem.setItemOrder(getNextOrder());
			sprintItem.setItem(item);
			// Save
			childService.save(sprintItem);
			// Refresh grid
			refreshGrid();
			// Show success notification
			CNotificationService.showSuccess("Item added to sprint");
			// Notify listener if set
			if (onItemChangeListener != null) {
				onItemChangeListener.accept(sprintItem);
			}
		} catch (final Exception e) {
			LOGGER.error("Error adding dropped item to sprint", e);
			CNotificationService.showException("Error adding item to sprint", e);
		}
	}

	@Override
	protected void configureGrid(final CGrid<CSprintItem> grid) {
		Check.notNull(grid, "Grid cannot be null");
		LOGGER.debug("Configuring grid columns for CSprintItem");
		// Use CGrid helper methods for consistent column creation
		grid.addIdColumn(CSprintItem::getId, "ID", "id");
		grid.addIntegerColumn(CSprintItem::getItemOrder, "Order", "order");
		grid.addShortTextColumn(CSprintItem::getItemType, "Type", "type");
		grid.addShortTextColumn(item -> {
			if (item.getItem() != null) {
				return item.getItem().getName();
			}
			return "Item " + item.getItemId();
		}, "Name", "name");
		// Use addEntityColumn to display status with color and icon
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
	 * @return the drop handler, or null if not set */
	@Override
	public Consumer<CProjectItem<?>> getDropHandler() { return dropHandler; }

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
					// Notify listener if set
					if (onItemChangeListener != null) {
						onItemChangeListener.accept(sprintItem);
					}
				} catch (final Exception e) {
					LOGGER.error("Error adding item {} to sprint", item.getId(), e);
				}
			}
			if (addedCount > 0) {
				refreshGrid();
				CNotificationService.showSuccess(addedCount + " items added to sprint");
			}
		};
	}

	/** Checks if dropping is currently enabled for this component.
	 * @return true if drops are enabled (handler is set), false otherwise */
	@Override
	public boolean isDropEnabled() { return dropHandler != null; }

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
			refreshGrid();
			getGridItems().asSingleSelect().clear();
			CNotificationService.showDeleteSuccess();
			// Notify listener if set
			if (onItemChangeListener != null) {
				onItemChangeListener.accept(itemToDelete);
			}
		} catch (final Exception e) {
			LOGGER.error("Error deleting sprint item", e);
			CNotificationService.showException("Error deleting item", e);
		}
	}
	// IDropTarget implementation

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

	/** Sets the handler to be called when an item is dropped into this component. Also configures the grid to accept drops.
	 * @param handler the consumer to handle dropped items */
	@Override
	public void setDropHandler(final Consumer<CProjectItem<?>> handler) {
		dropHandler = handler;
		if (getGridItems() != null) {
			if (handler != null) {
				// Enable drop mode on the grid
				getGridItems().setDropMode(GridDropMode.BETWEEN);
				// Add drop listener to handle the drop operation
				getGridItems().addDropListener(event -> {
					try {
						// Get the drop location
						final GridDropLocation dropLocation = event.getDropLocation();
						LOGGER.debug("Item dropped on sprint items grid at location: {}", dropLocation);
						// Note: The actual dropped item is tracked in the drag source component
						// and passed via the drop handler callback from the source component
					} catch (final Exception e) {
						LOGGER.error("Error handling drop on sprint items grid", e);
						CNotificationService.showException("Error handling drop", e);
					}
				});
				LOGGER.debug("Drop handler set and grid configured for drops");
			} else {
				// Disable drop mode
				getGridItems().setDropMode(null);
				LOGGER.debug("Drop handler removed");
			}
		}
	}

	/** Sets a listener to be notified when sprint items are added or removed.
	 * @param listener the listener to be called when an item changes */
	public void setOnItemChangeListener(final Consumer<CSprintItem> listener) {
		onItemChangeListener = listener;
	}
}
