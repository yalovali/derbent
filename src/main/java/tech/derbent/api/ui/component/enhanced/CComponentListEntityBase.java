package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IGridComponent;
import tech.derbent.api.interfaces.IGridRefreshListener;
import tech.derbent.api.interfaces.IHasDragControl;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.interfaces.ISelectionOwner;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CDragEndEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.screens.service.IOrderedEntity;
import tech.derbent.api.screens.service.IOrderedEntityService;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.dialogs.CDialogEntitySelection;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

/** CComponentListEntityBase - Generic base component for managing ordered lists of child entities within a master (parent) entity context, with full
 * CRUD operations.
 * <p>
 * This component follows the Master-Detail pattern where:
 * <ul>
 * <li>MasterEntity is the parent (e.g., CSprint, CDetailSection)</li>
 * <li>ChildEntity is the child (e.g., CSprintItem, CDetailLines)</li>
 * </ul>
 * <p>
 * Features:
 * <ul>
 * <li>Grid display with selectable items</li>
 * <li>CRUD operations (Create, Read, Update, Delete)</li>
 * <li>Move up/down functionality for ordering</li>
 * <li>Add/Edit with dialog support</li>
 * <li>Toolbar with action buttons</li>
 * <li>Notification handling</li>
 * <li>Service-based data access</li>
 * </ul>
 * <p>
 * Subclasses must implement:
 * <ul>
 * <li>{@link #configureGrid(CGrid)} - Configure grid columns and appearance</li>
 * <li>{@link #createNewEntity()} - Create a new entity instance</li>
 * <li>{@link #openEditDialog(Object, Consumer, boolean)} - Open edit dialog for entity</li>
 * <li>{@link #loadItems(CEntityDB)} - Load child items for master entity</li>
 * <li>{@link #getNextOrder()} - Get the next order number for new items</li>
 * </ul>
 * @param <MasterEntity> The master/parent entity type
 * @param <ChildEntity>  The child entity type extending CEntityDB and IOrderedEntity */
public abstract class CComponentListEntityBase<MasterEntity extends CEntityDB<?>, ChildEntity extends CEntityDB<?> & IOrderedEntity>
		extends VerticalLayout
		implements IContentOwner, IGridComponent<ChildEntity>, IGridRefreshListener<ChildEntity>, IPageServiceAutoRegistrable, IHasDragControl {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListEntityBase.class);
	private static final long serialVersionUID = 1L;

	/** Checks if the required indicator is visible.
	 * @return false (required indicator not currently implemented) */
	public static boolean isRequiredIndicatorVisible() { return false; }

	protected CButton buttonAdd;
	protected CButton buttonAddFromList;
	protected CButton buttonDelete;
	protected CButton buttonMoveDown;
	protected CButton buttonMoveUp;
	protected final IOrderedEntityService<ChildEntity> childService;
	// Drag control state
	private final Set<ComponentEventListener<CDragEndEvent>> dragEndListeners = new HashSet<>();
	private final Set<ComponentEventListener<CDragStartEvent>> dragStartListeners = new HashSet<>();
	private final Set<ComponentEventListener<CDragDropEvent>> dropListeners = new HashSet<>();
	protected final Class<ChildEntity> entityClass;
	protected CGrid<ChildEntity> grid;
	protected CHorizontalLayout layoutToolbar;
	protected MasterEntity masterEntity;
	protected final Class<MasterEntity> masterEntityClass;
	private ChildEntity previousValue = null;
	private boolean readOnly = false;
	// Refresh listeners for the update-and-notify pattern
	private final List<Consumer<ChildEntity>> refreshListeners = new ArrayList<>();
	protected ChildEntity selectedItem;
	// Owner interfaces for notifying parent components
	private ISelectionOwner<ChildEntity> selectionOwner;
	protected boolean useDynamicHeight = false;
	// Selection change listeners
	private final List<ValueChangeListener<? super ValueChangeEvent<ChildEntity>>> valueChangeListeners = new ArrayList<>();

	/** Constructor for the entity list component.
	 * @param title             The title to display above the grid
	 * @param masterEntityClass The class of the master entity type
	 * @param entityClass       The class of the child entity type
	 * @param childService      The service for CRUD operations */
	protected CComponentListEntityBase(final String title, final Class<MasterEntity> masterEntityClass, final Class<ChildEntity> entityClass,
			final IOrderedEntityService<ChildEntity> childService) {
		super();
		Check.notBlank(title, "Title cannot be blank");
		Check.notNull(masterEntityClass, "Master entity class cannot be null");
		Check.notNull(entityClass, "Entity class cannot be null");
		Check.notNull(childService, "Entity service cannot be null");
		this.masterEntityClass = masterEntityClass;
		this.entityClass = entityClass;
		this.childService = childService;
		// LOGGER.debug("Creating CComponentListEntityBase for entity class: {} with master: {}", entityClass.getSimpleName(),
		// masterEntityClass.getSimpleName());
		// Initialize UI components
		initializeComponents(title);
	}

	/** Adds an "Add From List" button to the toolbar that opens an entity selection dialog. This method creates a button that allows users to select
	 * items from a list of available entity types using a dialog with search/filter capabilities.
	 * <p>
	 * The button is added to the toolbar alongside the existing add button.
	 * @param dialogTitle     The title of the selection dialog
	 * @param entityTypes     List of entity type configurations for the dialog
	 * @param itemsProvider   Provider for loading items based on entity type
	 * @param multiSelect     True for multi-select, false for single-select
	 * @param onItemsSelected Callback invoked when items are selected from the dialog */
	@SuppressWarnings ({})
	protected CButton addButtonFromList(final String dialogTitle, final List<CComponentEntitySelection.EntityTypeConfig<?>> entityTypes,
			final CComponentEntitySelection.ItemsProvider<?> itemsProvider, final boolean multiSelect, final Consumer<List<?>> onItemsSelected) {
		return addButtonFromList(dialogTitle, entityTypes, itemsProvider, multiSelect, onItemsSelected, null,
				CComponentEntitySelection.AlreadySelectedMode.HIDE_ALREADY_SELECTED);
	}

	/** Adds an "Add From List" button to the toolbar that opens an entity selection dialog with already-selected items support. This method creates a
	 * button that allows users to select items from a list of available entity types using a dialog with search/filter capabilities.
	 * <p>
	 * The button is added to the toolbar alongside the existing add button.
	 * @param dialogTitle             The title of the selection dialog
	 * @param entityTypes             List of entity type configurations for the dialog
	 * @param itemsProvider           Provider for loading items based on entity type
	 * @param multiSelect             True for multi-select, false for single-select
	 * @param onItemsSelected         Callback invoked when items are selected from the dialog
	 * @param alreadySelectedProvider Provider for already-selected items (can be null)
	 * @param alreadySelectedMode     Mode for handling already-selected items */
	@SuppressWarnings ({
			"unused"
	})
	protected CButton addButtonFromList(final String dialogTitle, final List<CComponentEntitySelection.EntityTypeConfig<?>> entityTypes,
			final CComponentEntitySelection.ItemsProvider<?> itemsProvider, final boolean multiSelect, final Consumer<List<?>> onItemsSelected,
			final CComponentEntitySelection.ItemsProvider<?> alreadySelectedProvider,
			final CComponentEntitySelection.AlreadySelectedMode alreadySelectedMode) {
		Check.notBlank(dialogTitle, "Dialog title cannot be blank");
		Check.notEmpty(entityTypes, "Entity types cannot be empty");
		Check.notNull(itemsProvider, "Items provider cannot be null");
		Check.notNull(onItemsSelected, "Selection callback cannot be null");
		LOGGER.debug("Adding 'Add From List' button with {} entity types", entityTypes.size());
		// Create the button
		final CButton button = new CButton(VaadinIcon.LIST_SELECT.create());
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.setTooltipText("Add from list");
		button.addClickListener(event -> on_buttonFromList_clicked(dialogTitle, entityTypes, itemsProvider, onItemsSelected, multiSelect,
				alreadySelectedProvider, alreadySelectedMode));
		return button;
	}

	// IGridRefreshListener implementation
	/** Adds a listener to be notified when this component's grid data changes. Implements IGridRefreshListener.addRefreshListener()
	 * @param listener Consumer called when data changes (receives the changed item if available, or null) */
	@Override
	public void addRefreshListener(final Consumer<ChildEntity> listener) {
		Check.notNull(listener, "Refresh listener cannot be null");
		refreshListeners.add(listener);
	}

	/** Registers a value change listener for selection changes.
	 * @param listener The value change listener to register
	 * @return Registration object to remove the listener */
	public Registration addValueChangeListener(final ValueChangeListener<? super ValueChangeEvent<ChildEntity>> listener) {
		Check.notNull(listener, "ValueChangeListener cannot be null");
		valueChangeListeners.add(listener);
		LOGGER.debug("Added value change listener to {}", entityClass.getSimpleName());
		return () -> valueChangeListeners.remove(listener);
	}

	/** Clears the selection. Equivalent to calling setSelectedItem(null). */
	public void clear() {
		LOGGER.debug("Clearing selection");
		setSelectedItem(null);
	}

	/** Clear the grid. Implements IGridComponent.clearGrid() */
	@Override
	@SuppressWarnings ("unchecked")
	public void clearGrid() {
		LOGGER.debug("Clearing grid");
		grid.setItems();
		selectedItem = null;
		updateButtonStates(false);
	}

	/** Configure the grid columns and appearance. Subclasses must implement this to define their specific columns. Implements
	 * IGridComponent.configureGrid()
	 * @param grid1 The grid to configure */
	@Override
	public abstract void configureGrid(CGrid<ChildEntity> grid1);

	
	protected CButton create_buttonAdd() {
		final CButton button = new CButton(VaadinIcon.PLUS.create());
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.addClickListener(event -> on_buttonAdd_clicked());
		return button;
	}

	
	protected CButton create_buttonDelete() {
		final CButton button = new CButton(VaadinIcon.TRASH.create());
		button.addThemeVariants(ButtonVariant.LUMO_ERROR);
		button.addClickListener(event -> on_buttonDelete_clicked());
		button.setEnabled(false);
		return button;
	}

	
	private CButton create_buttonMoveDown() {
		final CButton button = new CButton(VaadinIcon.ARROW_DOWN.create());
		button.addClickListener(event -> on_buttonMoveDown_clicked());
		button.setEnabled(false);
		return button;
	}

	
	private CButton create_buttonMoveUp() {
		final CButton button = new CButton(VaadinIcon.ARROW_UP.create());
		button.addClickListener(event -> on_buttonMoveUp_clicked());
		button.setEnabled(false);
		return button;
	}

	/** Create and configure the grid component. */
	
	protected void createGrid() {
		grid = new CGrid<>(entityClass);
		grid.setSelectionMode(CGrid.SelectionMode.SINGLE);
		grid.setWidthFull(); // Enable grid to expand horizontally with container
		if (useDynamicHeight) {
			grid.setDynamicHeight();
		} else {
			grid.setHeightFull();
			grid.setMinHeight("120px");
		}
		grid.setRefreshConsumer(event -> grid_refresh_consumer());
		configureGrid(grid);
		grid.asSingleSelect().addValueChangeListener(e -> on_gridItems_selected(e.getValue()));
		// Add double-click listener
		grid.addItemDoubleClickListener(e -> on_gridItems_doubleClicked(e.getItem()));
		// Add internal drag event listeners for debug logging
		setupChildDragDropForwarding(grid);
	}

	protected abstract ChildEntity createNewEntity();

	/** Creates a new entity instance. This operation is not supported for some components that handle entity creation through a selection dialog, not
	 * directly.
	 * @return Never returns - always throws UnsupportedOperationException
	 * @throws UnsupportedOperationException always - use the appropriate selection dialog or override on_buttonAdd_clicked() */
	@Override
	public CEntityDB<?> createNewEntityInstance() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Entity items must be created through a selection dialog. Override on_buttonAdd_clicked() method.");
	}

	/** Create the toolbar with action buttons. */
	protected void createToolbar(final String titleText) {
		final CHorizontalLayout layoutButtons = new CHorizontalLayout();
		layoutButtons.setSizeUndefined();
		layoutToolbar = new CHorizontalLayout();
		layoutToolbar.setSpacing(true);
		// Create title
		final CH3 title = new CH3(titleText);
		// Create buttons
		buttonAdd = create_buttonAdd();
		buttonDelete = create_buttonDelete();
		buttonMoveUp = create_buttonMoveUp();
		buttonMoveDown = create_buttonMoveDown();
		layoutButtons.add(buttonAdd, buttonDelete, buttonMoveUp, buttonMoveDown);
		layoutToolbar.add(layoutButtons, title);
		// LOGGER.debug("Toolbar created with CRUD buttons");
	}

	@Override
	public Set<ComponentEventListener<CDragEndEvent>> drag_getDragEndListeners() {
		return dragEndListeners;
	}

	@Override
	public Set<ComponentEventListener<CDragStartEvent>> drag_getDragStartListeners() {
		return dragStartListeners;
	}

	@Override
	public Set<ComponentEventListener<CDragDropEvent>> drag_getDropListeners() {
		return dropListeners;
	}

	/** Enables or disables drag-and-drop functionality for the grid.
	 * <p>
	 * When enabled, rows in the grid can be dragged. When disabled, drag operations are blocked but the grid can still receive drop events if drop is
	 * enabled.
	 * @param enabled true to enable drag, false to disable */
	@Override
	public void drag_setDragEnabled(final boolean enabled) {
		grid.drag_setDragEnabled(enabled); // Use CGrid's IHasDragControl method
	}

	/** Enables or disables drop functionality for the grid.
	 * <p>
	 * When enabled, the grid can accept drop operations. When disabled, drops are blocked. This is independent of drag functionality - a grid can
	 * accept drops without being draggable.
	 * @param enabled true to enable drop, false to disable */
	@Override
	public void drag_setDropEnabled(final boolean enabled) {
		grid.drag_setDropEnabled(enabled); // Use CGrid's IHasDragControl method
	}

	/** Fires a value change event to all registered listeners.
	 * @param newValue   the new value
	 * @param fromClient whether the change originated from the client */
	protected void fireValueChangeEvent(final ChildEntity newValue, final boolean fromClient) {
		final ChildEntity oldValue = previousValue;
		previousValue = newValue;
		if (!valueChangeListeners.isEmpty()) {
			LOGGER.debug("Firing value change event: old={}, new={}, fromClient={}", oldValue != null ? oldValue.getId() : "null",
					newValue != null ? newValue.getId() : "null", fromClient);
			final ValueChangeEvent<ChildEntity> event = new ValueChangeEvent<ChildEntity>() {

				private static final long serialVersionUID = 1L;

				@Override
				public HasValue<?, ChildEntity> getHasValue() { return null; }

				@Override
				public ChildEntity getOldValue() { return oldValue; }

				@Override
				public ChildEntity getValue() { return newValue; }

				@Override
				public boolean isFromClient() { return fromClient; }
			};
			for (final ValueChangeListener<? super ValueChangeEvent<ChildEntity>> listener : valueChangeListeners) {
				try {
					listener.valueChanged(event);
				} catch (final Exception e) {
					LOGGER.error("Error notifying value change listener", e);
				}
			}
		}
	}

	/** Gets the add from list button, if created.
	 * @return The add from list button, or null if not created */
	public CButton getButtonAddFromList() { return buttonAddFromList; }

	/** Returns the current selected child entity.
	 * @return The current child selection */
	public ChildEntity getChildValue() { return selectedItem; }

	/** Returns the default component name based on the entity class.
	 * <p>
	 * Default implementation converts entity class simple name to camelCase (e.g., "CSprintItem" becomes "sprintItem"). Subclasses should override to
	 * provide a more meaningful name (e.g., "sprintItems", "backlogItems").
	 * @return The component name for method binding */
	@Override
	public String getComponentName() {
		// Default: use entity class simple name in camelCase (e.g., "sprintItem")
		// Subclasses should override to provide better names (e.g., "sprintItems")
		final String className = entityClass.getSimpleName();
		// Remove "C" prefix if present
		final String withoutPrefix = className.startsWith("C") ? className.substring(1) : className;
		// Convert first letter to lowercase
		return withoutPrefix.substring(0, 1).toLowerCase() + withoutPrefix.substring(1);
	}

	/** Returns the current sprint ID as a string.
	 * @return The ID string or null if no sprint is set */
	@Override
	public String getCurrentEntityIdString() {
		return getMasterEntity() != null && getMasterEntity().getId() != null ? getMasterEntity().getId().toString() : null;
	}
	// ==================== IHasDragStart, IHasDragEnd, IHasDrop Implementation ====================

	/** Get the entity service.
	 * @return The service */
	@Override
	public CAbstractService<?> getEntityService() { return (CAbstractService<?>) childService; }

	/** Get the grid component. Implements IGridComponent.getGrid()
	 * @return The grid */
	@Override
	public CGrid<ChildEntity> getGrid() { return grid; }

	/** Get the toolbar layout component.
	 * @return The toolbar layout */
	public CHorizontalLayout getLayoutToolbar() { return layoutToolbar; }

	protected MasterEntity getMasterEntity() { return masterEntity; }

	/** Get the next order number for a new item. Subclasses must implement this to provide appropriate ordering.
	 * @return The next order number */
	protected abstract Integer getNextOrder();

	/** Get the currently selected item.
	 * @return The selected item, or null if none selected */
	public ChildEntity getSelectedItem() { return selectedItem; }

	/** Returns the current master entity.
	 * @return The current master entity being edited */
	@Override
	public CEntityDB<?> getValue() { return getMasterEntity(); }

	public void grid_refresh_consumer() {
		final MasterEntity master = getMasterEntity();
		Check.notNull(master, "Master entity cannot be null when refreshing grid");
		final ChildEntity currentValue = grid.asSingleSelect().getValue();
		final List<ChildEntity> items = loadItems(master);
		Check.notNull(items, "Loaded items cannot be null");
		// LOGGER.debug("Refreshing grid with {} items", items.size());
		grid.setItems(items);
		grid.asSingleSelect().setValue(currentValue);
	}
	// Selection handling

	/** Handle edit operation for selected item.
	 * @param item The item to edit */
	protected void handleEdit(final ChildEntity item) {
		try {
			Check.notNull(item, "Item to edit cannot be null");
			LOGGER.debug("Opening edit dialog for item: {}", item.getId());
			openEditDialog(item, this::handleSave, false);
		} catch (final Exception ex) {
			LOGGER.error("Error opening edit dialog", ex);
			CNotificationService.showException("Error editing item", ex);
		}
	}

	/** Handle save operation. Saves the entity and refreshes the grid.
	 * @param entity The entity to save */
	protected void handleSave(final ChildEntity entity) {
		try {
			Check.notNull(entity, "Entity to save cannot be null");
			LOGGER.debug("Saving entity: {}", entity.getId() != null ? entity.getId() : "new");
			childService.save(entity);
			refreshGrid();
			grid.asSingleSelect().clear();
			CNotificationService.showSaveSuccess();
		} catch (final CValidationException validationException) {
			LOGGER.warn("Validation error saving entity", validationException);
			CNotificationService.showValidationException(validationException);
		} catch (final Exception e) {
			LOGGER.error("Error saving entity", e);
			CNotificationService.showException("Error saving item", e);
		}
	}

	/** Initialize all UI components.
	 * @param titleText The title text to display */
	protected void initializeComponents(final String titleText) {
		setSpacing(false); // Reduced spacing between components
		setPadding(false);
		setWidthFull();
		createGrid();
		createToolbar(titleText);
		add(layoutToolbar, grid);
		// LOGGER.debug("UI components initialized for {}", entityClass.getSimpleName());
	}

	/** Checks if the selection is empty.
	 * @return true if no item is selected, false otherwise */
	public boolean isEmpty() { return selectedItem == null; }

	/** Checks if the component is read-only.
	 * @return true if read-only, false otherwise */
	public boolean isReadOnly() { return readOnly; }

	/** Load items for the given master entity. Subclasses must implement this to define how items are loaded.
	 * @param master The master/parent entity
	 * @return List of items to display */
	protected abstract List<ChildEntity> loadItems(MasterEntity master);

	/** Moves an item down in the ordering by calling the service's moveItemDown method. This is the standard way to move items down and should be
	 * used by both button clicks and drag-drop reordering.
	 * @param item the item to move down */
	protected void moveItemDown(final ChildEntity item) {
		Check.notNull(item, "Item to move down cannot be null");
		childService.moveItemDown(item);
	}

	/** Moves an item up in the ordering by calling the service's moveItemUp method. This is the standard way to move items up and should be used by
	 * both button clicks and drag-drop reordering.
	 * @param item the item to move up */
	protected void moveItemUp(final ChildEntity item) {
		Check.notNull(item, "Item to move up cannot be null");
		Check.notNull(item.getId(), "Item must be saved before moving");
		LOGGER.debug("Moving item up: {}", item.getId());
		childService.moveItemUp(item);
	}

	/** Notifies all registered listeners that this component's data has changed. This is called AFTER the component has updated its own data and
	 * refreshed its grid. Implements IGridRefreshListener.notifyRefreshListeners()
	 * @param changedItem The item that changed, or null if multiple items changed or change is general */
	@Override
	public void notifyRefreshListeners(final ChildEntity changedItem) {
		if (!refreshListeners.isEmpty()) {
			LOGGER.debug("Notifying {} refresh listeners about data change in {}", refreshListeners.size(), entityClass.getSimpleName());
			for (final Consumer<ChildEntity> listener : refreshListeners) {
				try {
					listener.accept(changedItem);
				} catch (final Exception e) {
					LOGGER.error("Error notifying refresh listener", e);
				}
			}
		}
	}

	/** Sets the drag owner to be notified when drag operations start. /** Notifies the selection owner about selection changes. Should be called
	 * whenever selection changes in the component. */
	protected void notifySelectionOwner() {
		if (selectionOwner != null) {
			final Set<ChildEntity> selected = new HashSet<>();
			if (selectedItem != null) {
				selected.add(selectedItem);
			}
			selectionOwner.onSelectionChanged(selected);
		}
	}

	/** Handle add button click. Creates a new entity and opens the edit dialog. */
	protected void on_buttonAdd_clicked() {
		try {
			LOGGER.debug("Add button clicked");
			// Check master entity is valid
			final MasterEntity master = getMasterEntity();
			Check.notNull(master, "Master entity cannot be null when adding items");
			Check.notNull(master.getId(), "Master entity must be saved before adding items");
			// Create new entity
			final ChildEntity newEntity = createNewEntity();
			Check.notNull(newEntity, "Created entity cannot be null");
			// Open edit dialog
			openEditDialog(newEntity, this::handleSave, true);
		} catch (final Exception ex) {
			LOGGER.error("Error handling add operation", ex);
			CNotificationService.showException("Error adding item", ex);
		}
	}

	/** Handle delete button click. Deletes the selected item after confirmation. */
	protected void on_buttonDelete_clicked() {
		Check.notNull(selectedItem, "No item selected for deletion");
		Check.notNull(selectedItem.getId(), "Cannot delete unsaved item");
		try {
			LOGGER.debug("Deleting item: {}", selectedItem.getId());
			childService.delete(selectedItem);
			refreshGrid();
			grid.asSingleSelect().clear();
			CNotificationService.showDeleteSuccess();
		} catch (final Exception e) {
			LOGGER.error("Error deleting item", e);
			CNotificationService.showException("Error deleting item", e);
		}
	}

	/** Handle click on the "Add From List" button. Opens the entity selection dialog.
	 * @param dialogTitle             The title of the selection dialog
	 * @param entityTypes             List of entity type configurations for the dialog
	 * @param itemsProvider           Provider for loading items based on entity type
	 * @param onItemsSelected         Callback invoked when items are selected from the dialog
	 * @param multiSelect             True for multi-select, false for single-select
	 * @param alreadySelectedProvider Provider for already-selected items (can be null)
	 * @param alreadySelectedMode     Mode for handling already-selected items */
	@SuppressWarnings ({
			"unchecked", "rawtypes", "static-method"
	})
	protected void on_buttonFromList_clicked(final String dialogTitle, final List<CComponentEntitySelection.EntityTypeConfig<?>> entityTypes,
			final CComponentEntitySelection.ItemsProvider<?> itemsProvider, final Consumer<List<?>> onItemsSelected, final boolean multiSelect,
			final CComponentEntitySelection.ItemsProvider<?> alreadySelectedProvider,
			final CComponentEntitySelection.AlreadySelectedMode alreadySelectedMode) {
		try {
			LOGGER.debug("Opening entity selection dialog: {}", dialogTitle);
			// Use raw types for dialog creation due to complex generic constraints
			final CDialogEntitySelection dialog = new CDialogEntitySelection(dialogTitle, entityTypes, itemsProvider, onItemsSelected, multiSelect,
					alreadySelectedProvider, alreadySelectedMode);
			dialog.open();
		} catch (final Exception ex) {
			LOGGER.error("Error opening entity selection dialog", ex);
			CNotificationService.showException("Error opening selection dialog", ex);
		}
	}

	/** Handle move down button click. Moves the selected item down in the order. */
	protected void on_buttonMoveDown_clicked() {
		Check.notNull(selectedItem, "No item selected for move down");
		try {
			LOGGER.debug("Moving item down: {}", selectedItem.getId());
			moveItemDown(selectedItem);
			refreshGrid();
		} catch (final Exception e) {
			LOGGER.error("Error moving item down", e);
			CNotificationService.showException("Error moving item down", e);
		}
	}

	/** Handle move up button click. Moves the selected item up in the order. */
	protected void on_buttonMoveUp_clicked() {
		Check.notNull(selectedItem, "No item selected for move up");
		try {
			LOGGER.debug("Moving item up: {}", selectedItem.getId());
			moveItemUp(selectedItem);
			refreshGrid();
		} catch (final Exception e) {
			LOGGER.error("Error moving item up", e);
			CNotificationService.showException("Error moving item up", e);
		}
	}

	/** Handle double-click on a grid item. Opens the edit dialog by default.
	 * @param item The double-clicked item */
	protected void on_gridItems_doubleClicked(final ChildEntity item) {
		try {
			Check.notNull(item, "Double-clicked item cannot be null");
			LOGGER.debug("Double-clicked item: {}", item.getId());
			selectedItem = item;
			updateButtonStates(true);
			handleEdit(item);
		} catch (final Exception ex) {
			LOGGER.error("Error opening edit dialog", ex);
			CNotificationService.showException("Error opening edit dialog", ex);
		}
	}

	/** Handle selection change in the grid.
	 * @param item The selected item (can be null) */
	protected void on_gridItems_selected(final ChildEntity item) {
		try {
			// LOGGER.debug("Selection changed to: {}", item != null ? item.getId() : "null");
			selectedItem = item;
			updateButtonStates(item != null);
			// Notify selection owner if set
			notifySelectionOwner();
			// Fire selection change event
			fireValueChangeEvent(item, true);
		} catch (final Exception ex) {
			LOGGER.error("Error processing selection change", ex);
			CNotificationService.showException("Error processing selection", ex);
		}
	}

	/** Open an edit dialog for the entity. Subclasses must implement this to provide appropriate edit dialogs.
	 * @param entity       The entity to edit
	 * @param saveCallback Callback to invoke when saving
	 * @param isNew        True if this is a new entity */
	protected abstract void openEditDialog(ChildEntity entity, Consumer<ChildEntity> saveCallback, boolean isNew);
	// Owner interface setters and notification methods

	@Override
	public void populateForm() {
		LOGGER.debug("Populating form - refreshing grid if master entity is set");
		if (getMasterEntity() != null && getMasterEntity().getId() != null) {
			refreshGrid();
		} else {
			clearGrid();
		}
	}

	/** Refresh the grid to show updated data. */
	@Override
	public void refreshGrid() {
		grid.refreshGrid();
	}

	/** Registers this component with the page service for automatic event binding.
	 * <p>
	 * This default implementation provides automatic registration using a component name derived from the entity class. Subclasses can override
	 * getComponentName() to provide a custom name for method binding.
	 * <p>
	 * Note: This method only registers the component. The actual method binding happens when CPageService.bind() is called, which occurs once during
	 * page initialization.
	 * @param pageService The page service to register with */
	@Override
	public void registerWithPageService(final CPageService<?> pageService) {
		Check.notNull(pageService, "Page service cannot be null");
		final String componentName = getComponentName();
		pageService.registerComponent(componentName, this);
		LOGGER.debug("[BindDebug] {} auto-registered with page service as '{}' (binding will occur during CPageService.bind())",
				getClass().getSimpleName(), componentName);
	}

	/** Removes a previously added refresh listener. Implements IGridRefreshListener.removeRefreshListener()
	 * @param listener The listener to remove */
	@Override
	public void removeRefreshListener(final Consumer<ChildEntity> listener) {
		if (listener != null) {
			refreshListeners.remove(listener);
			LOGGER.debug("Removed refresh listener from {}", entityClass.getSimpleName());
		}
	}

	/** Sets the selected child entity.
	 * @param item The item to select */
	public void setChildValue(final ChildEntity item) {
		setSelectedItem(item);
	}

	/** Enable dynamic height mode for the grid. When enabled, the grid will size to its content (no minimum height) with an optional maximum height.
	 * This must be called before the component is initialized.
	 * @param maxHeight Optional maximum height (e.g., "400px", "50vh"), or null for no maximum */
	public void setDynamicHeight(final String maxHeight) {
		useDynamicHeight = true;
		setSizeUndefined();
		setWidthFull();
		this.setMinHeight("60px");
		this.setMaxHeight(maxHeight);
		grid.setDynamicHeight();
	}

	/** Sets the read-only state of this component. When read-only, users cannot change the selection.
	 * @param readOnly true to make read-only, false to make editable */
	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
		// Update button states
		if (readOnly) {
			buttonAdd.setEnabled(false);
			buttonDelete.setEnabled(false);
			buttonMoveUp.setEnabled(false);
			buttonMoveDown.setEnabled(false);
			if (buttonAddFromList != null) {
				buttonAddFromList.setEnabled(false);
			}
		} else {
			updateButtonStates(selectedItem != null);
			buttonAdd.setEnabled(true);
			if (buttonAddFromList != null) {
				buttonAddFromList.setEnabled(true);
			}
		}
		grid.setEnabled(!readOnly);
	}

	/** Set the currently selected item.
	 * @param item The item to select */
	public void setSelectedItem(final ChildEntity item) {
		LOGGER.debug("Setting selection programmatically: {}", item != null ? item.getId() : "null");
		grid.asSingleSelect().setValue(item);
		selectedItem = item;
		updateButtonStates(item != null);
		// Fire selection change event (not from client)
		fireValueChangeEvent(item, false);
	}

	/** Sets the current master entity for this component. Called by CFormBuilder when the binder's entity changes.
	 * @param entity The entity to set (expected to be of type MasterEntity) */
	@Override
	@SuppressWarnings ("unchecked")
	public void setValue(final CEntityDB<?> entity) {
		if (entity == null) {
			// LOGGER.debug("setValue called with null - clearing grid");
			masterEntity = null;
			clearGrid();
		} else if (masterEntityClass.isInstance(entity)) {
			// LOGGER.debug("setValue called with {} - setting master entity", entity.getClass().getSimpleName());
			masterEntity = (MasterEntity) entity;
			refreshGrid();
		} else {
			LOGGER.warn("setValue called with unexpected entity type: {} (expected {}) - ignoring", entity.getClass().getSimpleName(),
					masterEntityClass.getSimpleName());
		}
	}

	/** Update the enabled state of action buttons based on selection.
	 * @param hasSelection True if an item is selected */
	protected void updateButtonStates(final boolean hasSelection) {
		buttonDelete.setEnabled(hasSelection);
		buttonMoveUp.setEnabled(hasSelection);
		buttonMoveDown.setEnabled(hasSelection);
	}
}
