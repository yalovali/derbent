package tech.derbent.api.ui.component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.dialogs.CEntitySelectionDialog;
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
 * Implements IContentOwner to receive automatic entity updates from the form builder's binder when a sprint is selected. */
public class CComponentListSprintItems extends CComponentListEntityBase<CSprint, CSprintItem> {

	// Item type constants
	private static final String ITEM_TYPE_ACTIVITY = "CActivity";
	private static final String ITEM_TYPE_MEETING = "CMeeting";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListSprintItems.class);
	private static final long serialVersionUID = 1L;
	private final CActivityService activityService;
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
		LOGGER.debug("CComponentListSprintItems created");
		// Setup the "Add From List" button with entity selection dialog
		setupAddFromListButton();
	}

	/**
	 * Sets up the "Add From List" button that opens the new entity selection dialog
	 * with multi-select support and search/filter capabilities.
	 */
	@SuppressWarnings ("unchecked")
	private void setupAddFromListButton() {
		// Create entity type configurations
		final List<CEntitySelectionDialog.EntityTypeConfig<?>> entityTypes = new ArrayList<>();
		entityTypes.add(new CEntitySelectionDialog.EntityTypeConfig<>("Activities", CActivity.class, activityService));
		entityTypes.add(new CEntitySelectionDialog.EntityTypeConfig<>("Meetings", CMeeting.class, meetingService));
		// Create items provider that loads items based on entity type
		final CEntitySelectionDialog.ItemsProvider<CProjectItem<?>> itemsProvider = config -> {
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
		// Add the button from list with multi-select support
		addButtonFromList("Select Items to Add to Sprint", entityTypes, itemsProvider, true, selectedItems -> {
			LOGGER.debug("Selected {} items from entity selection dialog", selectedItems.size());
			int addedCount = 0;
			for (final Object obj : selectedItems) {
				if (!(obj instanceof CProjectItem)) {
					continue;
				}
				final CProjectItem<?> item = (CProjectItem<?>) obj;
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
				refreshGrid();
				CNotificationService.showSuccess(addedCount + " items added to sprint");
			}
		});
	}

	@Override
	protected void configureGrid(final CGrid<CSprintItem> grid) {
		Check.notNull(grid, "Grid cannot be null");
		LOGGER.debug("Configuring grid columns for CSprintItem");
		grid.addColumn(CSprintItem::getId).setHeader("Id").setWidth("80px");
		grid.addColumn(CSprintItem::getItemOrder).setHeader("Order").setWidth("80px");
		grid.addColumn(CSprintItem::getItemType).setHeader("Type").setWidth("120px");
		grid.addColumn(item -> {
			if (item.getItem() != null) {
				return item.getItem().getName();
			}
			return "Item " + item.getItemId();
		}).setHeader("Name").setAutoWidth(true);
		// Use addStatusColumn to display status with color and icon
		grid.addStatusColumn(item -> {
			if ((item.getItem() != null) && (item.getItem().getStatus() != null)) {
				return item.getItem().getStatus();
			}
			return null;
		}, "Status", "status");
	}

	/** Populates the component by refreshing the grid with sprint items. This method is called automatically by CFormBuilder when the binder's entity
	 * changes. */
	@Override
	protected CSprintItem createNewEntity() {
		Check.fail("Not used in this context - the component handles entity creation via the selection dialog");
		return null;
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
	protected void handleDoubleClick(final CSprintItem item) {
		// Override to prevent edit dialog from opening on double-click
		Check.notNull(item, "Double-clicked item cannot be null");
		LOGGER.debug("Double-clicked sprint item: {} (edit not supported)", item.getId());
		setSelectedItem(item);
		updateButtonStates(true);
		// Don't call openEditDialog - just select the item
	}

	/** Load available items for the selected type.
	 * @param type     The item type
	 * @param comboBox The combo box to populate */
	protected void loadAvailableItems(final String type, final ComboBox<CProjectItem<?>> comboBox) {
		Check.notNull(type, "Type cannot be null");
		Check.notNull(comboBox, "ComboBox cannot be null");
		Check.notNull(getMasterEntity(), "Current sprint cannot be null");
		Check.notNull(getMasterEntity().getProject(), "Sprint project cannot be null");
		final CProject project = getMasterEntity().getProject();
		LOGGER.debug("Loading available items of type {} for project {}", type, project.getId());
		try {
			List<? extends CProjectItem<?>> items;
			if (ITEM_TYPE_ACTIVITY.equals(type)) {
				items = activityService.listByProject(project);
				LOGGER.debug("Loaded {} activities", items.size());
			} else if (ITEM_TYPE_MEETING.equals(type)) {
				items = meetingService.listByProject(project);
				LOGGER.debug("Loaded {} meetings", items.size());
			} else {
				LOGGER.warn("Unknown item type: {}", type);
				CNotificationService.showWarning("Unknown item type: " + type);
				return;
			}
			// Cast is safe because both CActivity and CMeeting extend CProjectItem
			@SuppressWarnings ("unchecked")
			final List<CProjectItem<?>> projectItems = (List<CProjectItem<?>>) items;
			comboBox.setItems(projectItems);
		} catch (final Exception e) {
			LOGGER.error("Error loading available items", e);
			CNotificationService.showException("Error loading items", e);
		}
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
	protected void on_addButton_clicked() {
		try {
			LOGGER.debug("Add button clicked for sprint items");
			// Check master entity is valid
			final CSprint master = getMasterEntity();
			Check.notNull(master, "Master sprint cannot be null when adding items");
			Check.notNull(master.getId(), "Master sprint must be saved before adding items");
			// Open type and item selection dialog
			openItemSelectionDialog();
		} catch (final Exception ex) {
			LOGGER.error("Error handling add operation", ex);
			CNotificationService.showException("Error adding item", ex);
		}
	}

	@Override
	protected void openEditDialog(final CSprintItem entity, final Consumer<CSprintItem> saveCallback, final boolean isNew) {
		Check.notNull(entity, "Entity cannot be null when opening edit dialog");
		LOGGER.warn("Edit operation not supported for sprint items - ID: {}", entity.getId());
		CNotificationService
				.showWarning("Sprint items cannot be edited directly. Please delete this item and add a new one if you need to change it.");
		// Sprint items are simple references (itemId + itemType)
		// There's nothing meaningful to edit - users should delete and re-add instead
	}

	/** Open the item selection dialog to choose type and item to add. */
	protected void openItemSelectionDialog() {
		Check.notNull(getMasterEntity(), "Current sprint cannot be null when opening selection dialog");
		Check.notNull(getMasterEntity().getProject(), "Sprint must have a project");
		LOGGER.debug("Opening item selection dialog for sprint: {}", getMasterEntity().getId());
		final Dialog dialog = new Dialog();
		dialog.setHeaderTitle("Select Item to Add");
		dialog.setWidth("600px");
		// Create form layout
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setPadding(false);
		// Type selection
		final ComboBox<String> typeComboBox = new ComboBox<>("Item Type");
		typeComboBox.setItems(ITEM_TYPE_ACTIVITY, ITEM_TYPE_MEETING);
		typeComboBox.setPlaceholder("Select type...");
		typeComboBox.setRequired(true);
		typeComboBox.setWidthFull();
		// Item selection
		final ComboBox<CProjectItem<?>> itemComboBox = new ComboBox<>("Item");
		itemComboBox.setPlaceholder("Select item...");
		itemComboBox.setRequired(true);
		itemComboBox.setWidthFull();
		itemComboBox.setEnabled(false);
		itemComboBox.setItemLabelGenerator(item -> {
			if (item == null) {
				return "";
			}
			return item.getName() + " (ID: " + item.getId() + ")";
		});
		// When type is selected, load available items
		typeComboBox.addValueChangeListener(e -> {
			final String selectedType = e.getValue();
			if (selectedType != null) {
				LOGGER.debug("Type selected: {}", selectedType);
				loadAvailableItems(selectedType, itemComboBox);
				itemComboBox.setEnabled(true);
			} else {
				itemComboBox.setEnabled(false);
				itemComboBox.clear();
			}
		});
		layout.add(typeComboBox, itemComboBox);
		// Create buttons
		final Button saveButton = new Button("Add", VaadinIcon.CHECK.create());
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(e -> {
			final String type = typeComboBox.getValue();
			final CProjectItem<?> item = itemComboBox.getValue();
			if ((type == null) || (item == null)) {
				CNotificationService.showWarning("Please select both type and item");
				return;
			}
			try {
				LOGGER.debug("Creating sprint item: type={}, itemId={}", type, item.getId());
				// Create sprint item
				final CSprintItem sprintItem = new CSprintItem();
				sprintItem.setSprint(getMasterEntity());
				sprintItem.setItemId(item.getId());
				sprintItem.setItemType(type);
				sprintItem.setItemOrder(getNextOrder());
				sprintItem.setItem(item);
				// Save
				handleSave(sprintItem);
				dialog.close();
			} catch (final Exception ex) {
				LOGGER.error("Error creating sprint item", ex);
				CNotificationService.showException("Error adding item", ex);
			}
		});
		final Button cancelButton = new Button("Cancel", e -> dialog.close());
		final HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
		buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		buttonLayout.setWidthFull();
		dialog.add(layout);
		dialog.getFooter().add(buttonLayout);
		dialog.open();
		LOGGER.debug("Item selection dialog opened");
	}
}
