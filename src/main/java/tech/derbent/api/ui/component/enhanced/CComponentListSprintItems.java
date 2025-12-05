package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
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
 * type selection.
 * <p>
 * Features inherited from CComponentListEntityBase:
 * <ul>
 * <li>Grid display with ID, Order, Type, Name columns</li>
 * <li>Add operation with type selector dialog (CActivity or CMeeting)</li>
 * <li>Edit/Delete operations</li>
 * <li>Move up/down for reordering</li>
 * <li>Selection management</li>
 * </ul>
 * <p>
 * Implements IContentOwner to receive automatic entity updates from the form builder's binder when a sprint is selected.
 * <p>
 * Implements IEntitySelectionDialogSupport to provide standardized entity selection dialog configuration. */
public class CComponentListSprintItems extends CComponentListEntityBase<CSprint, CSprintItem>
		implements IEntitySelectionDialogSupport<CProjectItem<?>> {

	// Item type constants
	private static final String ITEM_TYPE_ACTIVITY = "CActivity";
	private static final String ITEM_TYPE_MEETING = "CMeeting";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListSprintItems.class);
	private static final long serialVersionUID = 1L;
	private final CActivityService activityService;
	// Services for loading items
	private final CMeetingService meetingService;
	// Listener for item changes
	private Consumer<CSprintItem> onItemChangeListener;
	// Flag to control whether grid is shown in widget (managed by parent)
	private boolean isInWidgetMode = false;

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

	/** Configure this component for use in widget mode. In widget mode, the grid uses dynamic height to expand to content.
	 * @param maxHeight The maximum height for the grid (e.g., "400px", "50vh") */
	public void configureForWidgetMode(final String maxHeight) {
		this.isInWidgetMode = true;
		setDynamicHeight(maxHeight);
		LOGGER.debug("CComponentListSprintItems configured for widget mode with max height: {}", maxHeight);
	}

	@Override
	protected void configureGrid(final CGrid<CSprintItem> grid) {
		Check.notNull(grid, "Grid cannot be null");
		LOGGER.debug("Configuring grid columns for CSprintItem");
		CGrid.styleColumnHeader(grid.addColumn(CSprintItem::getId).setWidth("80px"), "Id");
		CGrid.styleColumnHeader(grid.addColumn(CSprintItem::getItemOrder).setWidth("80px"), "Order");
		CGrid.styleColumnHeader(grid.addColumn(CSprintItem::getItemType).setWidth("120px"), "Type");
		CGrid.styleColumnHeader(grid.addColumn(item -> {
			if (item.getItem() != null) {
				return item.getItem().getName();
			}
			return "Item " + item.getItemId();
		}).setAutoWidth(true), "Name");
		// Use addEntityColumn to display status with color and icon
		try {
			grid.addEntityColumn(item -> {
				return item.getItem().getStatus();
			}, "Status", "status", CSprintItem.class);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		CGrid.styleColumnHeader(grid.addColumn(CSprintItem::getItemType).setWidth("120px"), "Type");
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
	public CDialogEntitySelection.AlreadySelectedMode getAlreadySelectedMode() {
		return CDialogEntitySelection.AlreadySelectedMode.HIDE_ALREADY_SELECTED;
	}

	/** Returns a provider for already-selected items (items currently in the sprint). This returns items based on the current sprint's members to
	 * enable the dialog to hide or pre-select them.
	 * @return ItemsProvider that returns the current sprint's items filtered by entity type */
	@Override
	public CDialogEntitySelection.ItemsProvider<CProjectItem<?>> getAlreadySelectedProvider() {
		final CDialogEntitySelection.ItemsProvider<CProjectItem<?>> provider = config -> {
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
	public List<CDialogEntitySelection.EntityTypeConfig<?>> getDialogEntityTypes() {
		final List<CDialogEntitySelection.EntityTypeConfig<?>> entityTypes = new ArrayList<>();
		entityTypes.add(new CDialogEntitySelection.EntityTypeConfig<>(ITEM_TYPE_ACTIVITY, CActivity.class, activityService));
		entityTypes.add(new CDialogEntitySelection.EntityTypeConfig<>(ITEM_TYPE_MEETING, CMeeting.class, meetingService));
		return entityTypes;
	}

	@Override
	public String getDialogTitle() { return "Select Items to Add to Sprint"; }

	@Override
	@SuppressWarnings ("unchecked")
	public CDialogEntitySelection.ItemsProvider<CProjectItem<?>> getItemsProvider() {
		final CDialogEntitySelection.ItemsProvider<CProjectItem<?>> itemsProvider = config -> {
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

	@Override
	protected void on_gridItems_doubleClicked(final CSprintItem item) {
		// Override to prevent edit dialog from opening on double-click
		Check.notNull(item, "Double-clicked item cannot be null");
		LOGGER.debug("Double-clicked sprint item: {} (edit not supported)", item.getId());
		setSelectedItem(item);
		updateButtonStates(true);
		// Don't call openEditDialog - just select the item
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

	@Override
	protected void openEditDialog(final CSprintItem entity, final Consumer<CSprintItem> saveCallback, final boolean isNew) {
		Check.notNull(entity, "Entity cannot be null when opening edit dialog");
		LOGGER.warn("Edit operation not supported for sprint items - ID: {}", entity.getId());
		CNotificationService
				.showWarning("Sprint items cannot be edited directly. Please delete this item and add a new one if you need to change it.");
	}

	/** Sets a listener to be notified when sprint items are added or removed.
	 * @param listener the listener to be called when an item changes */
	public void setOnItemChangeListener(final Consumer<CSprintItem> listener) {
		this.onItemChangeListener = listener;
	}
}
