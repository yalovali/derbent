package tech.derbent.api.ui.component;

import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

/**
 * CComponentListEntityBase - Generic base component for managing ordered lists of entities with CRUD operations.
 * 
 * <p>Features:
 * <ul>
 * <li>Grid display with selectable items</li>
 * <li>CRUD operations (Create, Read, Update, Delete)</li>
 * <li>Move up/down functionality for ordering</li>
 * <li>Add/Edit with dialog support</li>
 * <li>Toolbar with action buttons</li>
 * <li>Notification handling</li>
 * <li>Service-based data access</li>
 * </ul>
 * 
 * <p>Subclasses must implement:
 * <ul>
 * <li>{@link #configureGrid(CGrid)} - Configure grid columns and appearance</li>
 * <li>{@link #createNewEntity()} - Create a new entity instance</li>
 * <li>{@link #openEditDialog(Object, Consumer, boolean)} - Open edit dialog for entity</li>
 * <li>{@link #getMasterEntity()} - Get the master/parent entity</li>
 * <li>{@link #getNextOrder()} - Get the next order number for new items</li>
 * </ul>
 * 
 * @param <T> The entity type extending CEntityDB
 * @param <M> The master/parent entity type
 */
public abstract class CComponentListEntityBase<T extends CEntityDB<T>, M extends CEntityDB<M>> extends VerticalLayout {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CComponentListEntityBase.class);
	private static final long serialVersionUID = 1L;

	// Components
	protected CGrid<T> grid;
	protected H3 title;
	protected HorizontalLayout toolbar;
	protected Button addButton;
	protected Button deleteButton;
	protected Button moveUpButton;
	protected Button moveDownButton;

	// Data management
	protected T selectedItem;
	protected final Class<T> entityClass;
	protected final CAbstractService<T> entityService;

	/**
	 * Constructor for the entity list component.
	 * 
	 * @param title The title to display above the grid
	 * @param entityClass The class of the entity type
	 * @param entityService The service for CRUD operations
	 */
	protected CComponentListEntityBase(final String title, final Class<T> entityClass, final CAbstractService<T> entityService) {
		super();
		Check.notBlank(title, "Title cannot be blank");
		Check.notNull(entityClass, "Entity class cannot be null");
		Check.notNull(entityService, "Entity service cannot be null");
		
		this.entityClass = entityClass;
		this.entityService = entityService;
		
		LOGGER.debug("Creating CComponentListEntityBase for entity class: {}", entityClass.getSimpleName());
		
		// Initialize UI components
		initializeComponents(title);
	}

	/**
	 * Initialize all UI components.
	 * 
	 * @param titleText The title text to display
	 */
	protected void initializeComponents(final String titleText) {
		setSpacing(true);
		setPadding(false);
		setWidthFull();
		
		// Create title
		title = new H3(titleText);
		
		// Create grid
		createGrid();
		
		// Create toolbar
		createToolbar();
		
		// Add components to layout
		add(title, toolbar, grid);
		
		LOGGER.debug("UI components initialized for {}", entityClass.getSimpleName());
	}

	/**
	 * Create and configure the grid component.
	 */
	protected void createGrid() {
		grid = new CGrid<>(entityClass);
		grid.setSelectionMode(CGrid.SelectionMode.SINGLE);
		grid.setHeightFull();
		grid.setMinHeight("250px");
		
		// Configure grid columns - subclass responsibility
		configureGrid(grid);
		
		// Add selection listener
		grid.asSingleSelect().addValueChangeListener(e -> handleSelectionChange(e.getValue()));
		
		// Add double-click listener
		grid.addItemDoubleClickListener(e -> handleDoubleClick(e.getItem()));
		
		LOGGER.debug("Grid created and configured for {}", entityClass.getSimpleName());
	}

	/**
	 * Configure the grid columns and appearance.
	 * Subclasses must implement this to define their specific columns.
	 * 
	 * @param grid The grid to configure
	 */
	protected abstract void configureGrid(CGrid<T> grid);

	/**
	 * Create the toolbar with action buttons.
	 */
	protected void createToolbar() {
		toolbar = new HorizontalLayout();
		toolbar.setSpacing(true);
		
		// Create buttons
		addButton = new Button(getAddButtonLabel(), VaadinIcon.PLUS.create());
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.addClickListener(e -> handleAdd());
		
		deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
		deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		deleteButton.addClickListener(e -> handleDelete());
		deleteButton.setEnabled(false);
		
		moveUpButton = new Button("Move Up", VaadinIcon.ARROW_UP.create());
		moveUpButton.addClickListener(e -> handleMoveUp());
		moveUpButton.setEnabled(false);
		
		moveDownButton = new Button("Move Down", VaadinIcon.ARROW_DOWN.create());
		moveDownButton.addClickListener(e -> handleMoveDown());
		moveDownButton.setEnabled(false);
		
		// Add buttons to toolbar
		toolbar.add(addButton, deleteButton, moveUpButton, moveDownButton);
		
		LOGGER.debug("Toolbar created with CRUD buttons");
	}

	/**
	 * Get the label for the add button.
	 * Override to customize.
	 * 
	 * @return The button label
	 */
	protected String getAddButtonLabel() {
		return "Add";
	}

	/**
	 * Handle selection change in the grid.
	 * 
	 * @param item The selected item (can be null)
	 */
	protected void handleSelectionChange(final T item) {
		try {
			LOGGER.debug("Selection changed to: {}", item != null ? item.getId() : "null");
			selectedItem = item;
			updateButtonStates(item != null);
		} catch (final Exception ex) {
			LOGGER.error("Error processing selection change", ex);
			CNotificationService.showException("Error processing selection", ex);
		}
	}

	/**
	 * Handle double-click on a grid item.
	 * Opens the edit dialog by default.
	 * 
	 * @param item The double-clicked item
	 */
	protected void handleDoubleClick(final T item) {
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

	/**
	 * Update the enabled state of action buttons based on selection.
	 * 
	 * @param hasSelection True if an item is selected
	 */
	protected void updateButtonStates(final boolean hasSelection) {
		deleteButton.setEnabled(hasSelection);
		moveUpButton.setEnabled(hasSelection);
		moveDownButton.setEnabled(hasSelection);
	}

	/**
	 * Handle add button click.
	 * Creates a new entity and opens the edit dialog.
	 */
	protected void handleAdd() {
		try {
			LOGGER.debug("Add button clicked");
			
			// Check master entity is valid
			final M master = getMasterEntity();
			Check.notNull(master, "Master entity cannot be null when adding items");
			Check.notNull(master.getId(), "Master entity must be saved before adding items");
			
			// Create new entity
			final T newEntity = createNewEntity();
			Check.notNull(newEntity, "Created entity cannot be null");
			
			// Open edit dialog
			openEditDialog(newEntity, this::handleSave, true);
			
		} catch (final Exception ex) {
			LOGGER.error("Error handling add operation", ex);
			CNotificationService.showException("Error adding item", ex);
		}
	}

	/**
	 * Handle edit operation for selected item.
	 * 
	 * @param item The item to edit
	 */
	protected void handleEdit(final T item) {
		try {
			Check.notNull(item, "Item to edit cannot be null");
			LOGGER.debug("Opening edit dialog for item: {}", item.getId());
			openEditDialog(item, this::handleSave, false);
		} catch (final Exception ex) {
			LOGGER.error("Error opening edit dialog", ex);
			CNotificationService.showException("Error editing item", ex);
		}
	}

	/**
	 * Handle delete button click.
	 * Deletes the selected item after confirmation.
	 */
	protected void handleDelete() {
		Check.notNull(selectedItem, "No item selected for deletion");
		Check.notNull(selectedItem.getId(), "Cannot delete unsaved item");
		
		try {
			LOGGER.debug("Deleting item: {}", selectedItem.getId());
			entityService.delete(selectedItem);
			refreshGrid();
			grid.asSingleSelect().clear();
			CNotificationService.showDeleteSuccess();
		} catch (final Exception e) {
			LOGGER.error("Error deleting item", e);
			CNotificationService.showException("Error deleting item", e);
		}
	}

	/**
	 * Handle move up button click.
	 * Moves the selected item up in the order.
	 */
	protected void handleMoveUp() {
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

	/**
	 * Handle move down button click.
	 * Moves the selected item down in the order.
	 */
	protected void handleMoveDown() {
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

	/**
	 * Move an item up in the order.
	 * Subclasses can override to implement specific ordering logic.
	 * 
	 * @param item The item to move up
	 */
	protected abstract void moveItemUp(T item);

	/**
	 * Move an item down in the order.
	 * Subclasses can override to implement specific ordering logic.
	 * 
	 * @param item The item to move down
	 */
	protected abstract void moveItemDown(T item);

	/**
	 * Handle save operation.
	 * Saves the entity and refreshes the grid.
	 * 
	 * @param entity The entity to save
	 */
	protected void handleSave(final T entity) {
		try {
			Check.notNull(entity, "Entity to save cannot be null");
			LOGGER.debug("Saving entity: {}", entity.getId() != null ? entity.getId() : "new");
			entityService.save(entity);
			refreshGrid();
			grid.asSingleSelect().clear();
			CNotificationService.showSaveSuccess();
		} catch (final Exception e) {
			LOGGER.error("Error saving entity", e);
			CNotificationService.showException("Error saving item", e);
		}
	}

	/**
	 * Refresh the grid to show updated data.
	 */
	public void refreshGrid() {
		final M master = getMasterEntity();
		Check.notNull(master, "Master entity cannot be null when refreshing grid");
		
		final T currentValue = grid.asSingleSelect().getValue();
		final List<T> items = loadItems(master);
		Check.notNull(items, "Loaded items cannot be null");
		
		LOGGER.debug("Refreshing grid with {} items", items.size());
		grid.setItems(items);
		grid.asSingleSelect().setValue(currentValue);
	}

	/**
	 * Clear the grid.
	 */
	public void clearGrid() {
		LOGGER.debug("Clearing grid");
		grid.setItems();
		selectedItem = null;
		updateButtonStates(false);
	}

	/**
	 * Load items for the given master entity.
	 * Subclasses must implement this to define how items are loaded.
	 * 
	 * @param master The master/parent entity
	 * @return List of items to display
	 */
	protected abstract List<T> loadItems(M master);

	/**
	 * Create a new entity instance.
	 * Subclasses must implement this to create appropriate entity instances.
	 * 
	 * @return A new entity instance
	 */
	protected abstract T createNewEntity();

	/**
	 * Open an edit dialog for the entity.
	 * Subclasses must implement this to provide appropriate edit dialogs.
	 * 
	 * @param entity The entity to edit
	 * @param saveCallback Callback to invoke when saving
	 * @param isNew True if this is a new entity
	 */
	protected abstract void openEditDialog(T entity, Consumer<T> saveCallback, boolean isNew);

	/**
	 * Get the master/parent entity that owns these items.
	 * Subclasses must implement this.
	 * 
	 * @return The master entity
	 */
	protected abstract M getMasterEntity();

	/**
	 * Get the next order number for a new item.
	 * Subclasses must implement this to provide appropriate ordering.
	 * 
	 * @return The next order number
	 */
	protected abstract Integer getNextOrder();

	/**
	 * Get the currently selected item.
	 * 
	 * @return The selected item, or null if none selected
	 */
	public T getSelectedItem() {
		return selectedItem;
	}

	/**
	 * Set the currently selected item.
	 * 
	 * @param item The item to select
	 */
	public void setSelectedItem(final T item) {
		grid.asSingleSelect().setValue(item);
	}

	/**
	 * Get the grid component.
	 * 
	 * @return The grid
	 */
	public CGrid<T> getGrid() {
		return grid;
	}

	/**
	 * Get the entity service.
	 * 
	 * @return The service
	 */
	public CAbstractService<T> getEntityService() {
		return entityService;
	}
}
