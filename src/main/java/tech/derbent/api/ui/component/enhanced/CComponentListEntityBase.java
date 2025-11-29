package tech.derbent.api.ui.component.enhanced;

import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.screens.service.IOrderedEntity;
import tech.derbent.api.screens.service.IOrderedEntityService;
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
		extends VerticalLayout implements IContentOwner {
	protected static final Logger LOGGER = LoggerFactory.getLogger(CComponentListEntityBase.class);
	private static final long serialVersionUID = 1L;

	/** Creates an entity type configuration for use with addButtonFromList.
	 * @param displayName The display name for the entity type
	 * @param entityClass The entity class
	 * @param service     The service for the entity type
	 * @param <T>         The entity type
	 * @return EntityTypeConfig instance */
	protected static <T extends CEntityDB<T>> CDialogEntitySelection.EntityTypeConfig<T> createEntityTypeConfig(final String displayName,
			final Class<T> entityClass, final CAbstractService<T> service) {
		return new CDialogEntitySelection.EntityTypeConfig<>(displayName, entityClass, service);
	}

	protected CButton buttonAdd;
	protected CButton buttonAddFromList;
	protected final IOrderedEntityService<ChildEntity> childService;
	protected CButton buttonDelete;
	protected final Class<ChildEntity> entityClass;
	// Components
	protected CGrid<ChildEntity> gridItems;
	protected MasterEntity masterEntity;
	protected final Class<MasterEntity> masterEntityClass;
	protected CButton buttonMoveDown;
	protected CButton buttonMoveUp;
	// Data management
	protected ChildEntity selectedItem;
	protected CHorizontalLayout layoutToolbar;

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
		LOGGER.debug("Creating CComponentListEntityBase for entity class: {} with master: {}", entityClass.getSimpleName(),
				masterEntityClass.getSimpleName());
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
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	protected CButton addButtonFromList(final String dialogTitle, final List<CDialogEntitySelection.EntityTypeConfig<?>> entityTypes,
			final CDialogEntitySelection.ItemsProvider<?> itemsProvider, final boolean multiSelect, final Consumer<List<?>> onItemsSelected) {
		Check.notBlank(dialogTitle, "Dialog title cannot be blank");
		Check.notEmpty(entityTypes, "Entity types cannot be empty");
		Check.notNull(itemsProvider, "Items provider cannot be null");
		Check.notNull(onItemsSelected, "Selection callback cannot be null");
		LOGGER.debug("Adding 'Add From List' button with {} entity types", entityTypes.size());
		// Create the button
		final CButton button = new CButton(VaadinIcon.LIST_SELECT.create());
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.setTooltipText("Add from list");
		button.addClickListener(e -> {
			try {
				LOGGER.debug("Opening entity selection dialog: {}", dialogTitle);
				// Use raw types for dialog creation due to complex generic constraints
				final CDialogEntitySelection dialog =
						new CDialogEntitySelection(dialogTitle, entityTypes, itemsProvider, onItemsSelected, multiSelect);
				dialog.open();
			} catch (final Exception ex) {
				LOGGER.error("Error opening entity selection dialog", ex);
				CNotificationService.showException("Error opening selection dialog", ex);
			}
		});
		return button;
	}

	/** Clear the grid. */
	@SuppressWarnings ("unchecked")
	public void clearGrid() {
		LOGGER.debug("Clearing grid");
		gridItems.setItems();
		selectedItem = null;
		updateButtonStates(false);
	}

	/** Configure the grid columns and appearance. Subclasses must implement this to define their specific columns.
	 * @param grid The grid to configure */
	protected abstract void configureGrid(CGrid<ChildEntity> grid);

	protected CButton create_buttonAdd() {
		final CButton button = new CButton(VaadinIcon.PLUS.create());
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.addClickListener(e -> on_buttonAdd_clicked());
		return button;
	}

	protected CButton create_buttonDelete() {
		final CButton button = new CButton(VaadinIcon.TRASH.create());
		button.addThemeVariants(ButtonVariant.LUMO_ERROR);
		button.addClickListener(e -> on_buttonDelete_clicked());
		button.setEnabled(false);
		return button;
	}

	private CButton create_buttonMoveDown() {
		final CButton button = new CButton(VaadinIcon.ARROW_DOWN.create());
		button.addClickListener(e -> on_buttonMoveDown_clicked());
		button.setEnabled(false);
		return button;
	}

	private CButton create_buttonMoveUp() {
		final CButton button = new CButton(VaadinIcon.ARROW_UP.create());
		button.addClickListener(e -> on_buttonMoveUp_clicked());
		button.setEnabled(false);
		return button;
	}

	/** Create and configure the grid component. */
	protected void createGrid() {
		gridItems = new CGrid<>(entityClass);
		gridItems.setSelectionMode(CGrid.SelectionMode.SINGLE);
		gridItems.setHeightFull();
		gridItems.setMinHeight("250px");
		// Configure grid columns - subclass responsibility
		configureGrid(gridItems);
		// Add selection listener
		gridItems.asSingleSelect().addValueChangeListener(e -> on_gridItems_selected(e.getValue()));
		// Add double-click listener
		gridItems.addItemDoubleClickListener(e -> on_gridItems_doubleClicked(e.getItem()));
		LOGGER.debug("Grid created and configured for {}", entityClass.getSimpleName());
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
		LOGGER.debug("Toolbar created with CRUD buttons");
	}

	/** Gets the add from list button, if created.
	 * @return The add from list button, or null if not created */
	public CButton getButtonAddFromList() { return buttonAddFromList; }

	/** Get the entity service.
	 * @return The service */
	@Override
	public CAbstractService<?> getChildService() { return (CAbstractService<?>) childService; }

	/** Returns the current sprint entity.
	 * @return The current sprint being edited */
	@Override
	public CEntityDB<?> getCurrentEntity() { return getMasterEntity(); }

	/** Returns the current sprint ID as a string.
	 * @return The ID string or null if no sprint is set */
	@Override
	public String getCurrentEntityIdString() {
		return (getMasterEntity() != null) && (getMasterEntity().getId() != null) ? getMasterEntity().getId().toString() : null;
	}

	/** Get the grid component.
	 * @return The grid */
	public CGrid<ChildEntity> getGridItems() { return gridItems; }

	protected MasterEntity getMasterEntity() { return masterEntity; }

	/** Get the next order number for a new item. Subclasses must implement this to provide appropriate ordering.
	 * @return The next order number */
	protected abstract Integer getNextOrder();

	/** Get the currently selected item.
	 * @return The selected item, or null if none selected */
	public ChildEntity getSelectedItem() { return selectedItem; }

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
			gridItems.asSingleSelect().clear();
			CNotificationService.showSaveSuccess();
		} catch (final Exception e) {
			LOGGER.error("Error saving entity", e);
			CNotificationService.showException("Error saving item", e);
		}
	}

	/** Handle selection change in the grid.
	 * @param item The selected item (can be null) */
	protected void on_gridItems_selected(final ChildEntity item) {
		try {
			LOGGER.debug("Selection changed to: {}", item != null ? item.getId() : "null");
			selectedItem = item;
			updateButtonStates(item != null);
		} catch (final Exception ex) {
			LOGGER.error("Error processing selection change", ex);
			CNotificationService.showException("Error processing selection", ex);
		}
	}

	/** Initialize all UI components.
	 * @param titleText The title text to display */
	protected void initializeComponents(final String titleText) {
		setSpacing(true);
		setPadding(false);
		setWidthFull();
		createGrid();
		createToolbar(titleText);
		add(layoutToolbar, gridItems);
		LOGGER.debug("UI components initialized for {}", entityClass.getSimpleName());
	}

	/** Load items for the given master entity. Subclasses must implement this to define how items are loaded.
	 * @param master The master/parent entity
	 * @return List of items to display */
	protected abstract List<ChildEntity> loadItems(MasterEntity master);

	protected void moveItemDown(final ChildEntity item) {
		Check.notNull(item, "Item to move down cannot be null");
		childService.moveItemDown(item);
	}

	protected void moveItemUp(final ChildEntity item) {
		Check.notNull(item, "Item to move up cannot be null");
		Check.notNull(item.getId(), "Item must be saved before moving");
		LOGGER.debug("Moving CSprintItem up: {}", item.getId());
		childService.moveItemUp(item);
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
			gridItems.asSingleSelect().clear();
			CNotificationService.showDeleteSuccess();
		} catch (final Exception e) {
			LOGGER.error("Error deleting item", e);
			CNotificationService.showException("Error deleting item", e);
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

	/** Open an edit dialog for the entity. Subclasses must implement this to provide appropriate edit dialogs.
	 * @param entity       The entity to edit
	 * @param saveCallback Callback to invoke when saving
	 * @param isNew        True if this is a new entity */
	protected abstract void openEditDialog(ChildEntity entity, Consumer<ChildEntity> saveCallback, boolean isNew);

	@Override
	public void populateForm() {
		LOGGER.debug("populateForm called - refreshing sprint items grid");
		if ((getMasterEntity() != null) && (getMasterEntity().getId() != null)) {
			refreshGrid();
		} else {
			clearGrid();
		}
	}

	/** Refresh the grid to show updated data. */
	@Override
	public void refreshGrid() {
		final MasterEntity master = getMasterEntity();
		Check.notNull(master, "Master entity cannot be null when refreshing grid");
		final ChildEntity currentValue = gridItems.asSingleSelect().getValue();
		final List<ChildEntity> items = loadItems(master);
		Check.notNull(items, "Loaded items cannot be null");
		LOGGER.debug("Refreshing grid with {} items", items.size());
		gridItems.setItems(items);
		gridItems.asSingleSelect().setValue(currentValue);
	}

	/** Sets the current master entity for this component. Called by CFormBuilder when the binder's entity changes.
	 * @param entity The entity to set (expected to be of type MasterEntity) */
	@Override
	@SuppressWarnings ("unchecked")
	public void setCurrentEntity(final CEntityDB<?> entity) {
		if (entity == null) {
			LOGGER.debug("setCurrentEntity called with null - clearing grid");
			this.masterEntity = null;
			clearGrid();
		} else if (masterEntityClass.isInstance(entity)) {
			LOGGER.debug("setCurrentEntity called with {} - setting master entity", entity.getClass().getSimpleName());
			this.masterEntity = (MasterEntity) entity;
			refreshGrid();
		} else {
			LOGGER.warn("setCurrentEntity called with unexpected entity type: {} (expected {}) - ignoring", entity.getClass().getSimpleName(),
					masterEntityClass.getSimpleName());
		}
	}

	/** Set the master entity directly.
	 * @param master The master entity to set */
	protected void setMasterEntity(final MasterEntity master) {
		this.masterEntity = master;
		if (master == null) {
			clearGrid();
		} else if (master.getId() != null) {
			refreshGrid();
		}
	}

	/** Set the currently selected item.
	 * @param item The item to select */
	public void setSelectedItem(final ChildEntity item) {
		gridItems.asSingleSelect().setValue(item);
	}

	/** Update the enabled state of action buttons based on selection.
	 * @param hasSelection True if an item is selected */
	protected void updateButtonStates(final boolean hasSelection) {
		buttonDelete.setEnabled(hasSelection);
		buttonMoveUp.setEnabled(hasSelection);
		buttonMoveDown.setEnabled(hasSelection);
	}
}
