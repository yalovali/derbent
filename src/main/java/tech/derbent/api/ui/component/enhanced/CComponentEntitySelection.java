package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.interfaces.IGridComponent;
import tech.derbent.api.interfaces.IGridRefreshListener;
import tech.derbent.api.interfaces.IHasDragControl;
import tech.derbent.api.interfaces.ISelectionOwner;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CDragEndEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

/** CComponentEntitySelection - Reusable component for selecting entities from a grid with search/filter capabilities.
 * <p>
 * This component can be embedded in dialogs, pages, or panels for entity selection functionality. Implements HasValue interface for full Vaadin
 * binder integration.
 * <p>
 * Features:
 * <ul>
 * <li>Entity type selection dropdown</li>
 * <li>Grid with colored status display</li>
 * <li>Search toolbar with ID, Name, Description, Status filters</li>
 * <li>Single or multi-select mode</li>
 * <li>Selected item count indicator</li>
 * <li>Reset button for clearing selection</li>
 * <li>Selected items persist across grid filtering</li>
 * <li>Support for already-selected items with two modes: hide or show as pre-selected</li>
 * <li>Refresh listener support via IGridRefreshListener interface for component notifications</li>
 * <li>HasValue interface for binder integration and value change events</li>
 * </ul>
 * @param <EntityClass> The entity type being selected */
public class CComponentEntitySelection<EntityClass extends CEntityDB<?>> extends Composite<CVerticalLayout> implements IGridComponent<EntityClass>,
		IGridRefreshListener<EntityClass>, HasValue<HasValue.ValueChangeEvent<Set<EntityClass>>, Set<EntityClass>>, IHasDragControl {

	/** Mode for handling already selected items - re-exported from CComponentEntitySelection for backward compatibility */
	public static enum AlreadySelectedMode {

		/** Hide already selected items from the available items list */
		HIDE_ALREADY_SELECTED,
		/** Show already selected items in the grid, pre-selected (visually marked) */
		SHOW_AS_SELECTED;

		/** Create from component enum */
		public static AlreadySelectedMode fromComponentMode(final CComponentEntitySelection.AlreadySelectedMode mode) {
			return AlreadySelectedMode.valueOf(mode.name());
		}

		/** Convert to component enum */
		public CComponentEntitySelection.AlreadySelectedMode toComponentMode() {
			return CComponentEntitySelection.AlreadySelectedMode.valueOf(name());
		}
	}

	/** Entity type configuration */
	public static class EntityTypeConfig<E extends CEntityDB<E>> {

		private final String displayName;
		private final Class<E> entityClass;
		private final CAbstractService<E> service;

		public EntityTypeConfig(final String displayName, final Class<E> entityClass, final CAbstractService<E> service) {
			Check.notBlank(displayName, "Display name cannot be blank");
			Check.notNull(entityClass, "Entity class cannot be null");
			Check.notNull(service, "Service cannot be null");
			this.displayName = displayName;
			this.entityClass = entityClass;
			this.service = service;
		}

		public String getDisplayName() { return displayName; }

		public Class<E> getEntityClass() { return entityClass; }

		public CAbstractService<E> getService() { return service; }

		@Override
		public String toString() {
			return displayName;
		}
	}

	/** Callback for getting items based on entity type - re-exported from CComponentEntitySelection for backward compatibility */
	@FunctionalInterface
	public interface ItemsProvider<T> {

		List<T> getItems(EntityTypeConfig<?> config);

		/** Convert to component provider */
		@SuppressWarnings ("unchecked")
		default CComponentEntitySelection.ItemsProvider<T> toComponentProvider() {
			return componentConfig -> {
				@SuppressWarnings ("rawtypes")
				final EntityTypeConfig dialogConfig =
						new EntityTypeConfig(componentConfig.getDisplayName(), componentConfig.getEntityClass(), componentConfig.getService());
				return this.getItems(dialogConfig);
			};
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentEntitySelection.class);
	private static final long serialVersionUID = 1L;
	private List<EntityClass> allItems = new ArrayList<>();
	private List<EntityClass> alreadySelectedItems = new ArrayList<>();
	private final AlreadySelectedMode alreadySelectedMode;
	private final ItemsProvider<EntityClass> alreadySelectedProvider;
	private CButton buttonReset;
	private ComboBox<EntityTypeConfig<?>> comboBoxEntityType;
	private EntityTypeConfig<?> currentEntityType;
	// Keep current selection snapshot to compute old/new values when firing events
	private Set<EntityClass> currentSelectionSnapshot = new HashSet<>();
	// Drag control state
	private final Set<ComponentEventListener<CDragEndEvent>> dragEndListeners = new HashSet<>();
	private final Set<ComponentEventListener<CDragStartEvent>> dragStartListeners = new HashSet<>();
	private final Set<ComponentEventListener<CDragDropEvent>> dropListeners = new HashSet<>();
	private final List<EntityTypeConfig<?>> entityTypes;
	private CGrid<EntityClass> grid;
	private CComponentFilterToolbar gridSearchToolbar;
	private final ItemsProvider<EntityClass> itemsProvider;
	private Span labelSelectedCount;
	private final boolean multiSelect;
	private final Consumer<Set<EntityClass>> onSelectionChanged;
	// Refresh listeners for the update-and-notify pattern
	private final List<Consumer<EntityClass>> refreshListeners = new ArrayList<>();
	private final Set<EntityClass> selectedItems = new HashSet<>();
	// Selection listeners registered by creators (HasValue interface)
	private final List<HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<Set<EntityClass>>>> selectionListeners = new ArrayList<>();
	// Owner interfaces for notifying parent components
	private ISelectionOwner<EntityClass> selectionOwner;

	/** Creates an entity selection component.
	 * @param entityTypes        Available entity types for selection
	 * @param itemsProvider      Provider for loading items based on entity type
	 * @param onSelectionChanged Callback when selection changes
	 * @param multiSelect        True for multi-select, false for single-select */
	public CComponentEntitySelection(final List<EntityTypeConfig<?>> entityTypes, final ItemsProvider<EntityClass> itemsProvider,
			final Consumer<Set<EntityClass>> onSelectionChanged, final boolean multiSelect) {
		this(entityTypes, itemsProvider, onSelectionChanged, multiSelect, null, AlreadySelectedMode.HIDE_ALREADY_SELECTED);
	}

	/** Creates an entity selection component with support for already-selected items.
	 * @param entityTypes             Available entity types for selection
	 * @param itemsProvider           Provider for loading items based on entity type
	 * @param onSelectionChanged      Callback when selection changes
	 * @param multiSelect             True for multi-select, false for single-select
	 * @param alreadySelectedProvider Provider for already-selected items (can be null)
	 * @param alreadySelectedMode     Mode for handling already-selected items */
	private CComponentEntitySelection(final List<EntityTypeConfig<?>> entityTypes, final ItemsProvider<EntityClass> itemsProvider,
			final Consumer<Set<EntityClass>> onSelectionChanged, final boolean multiSelect, final ItemsProvider<EntityClass> alreadySelectedProvider,
			final AlreadySelectedMode alreadySelectedMode) {
		this(entityTypes, itemsProvider, onSelectionChanged, multiSelect, alreadySelectedProvider, alreadySelectedMode, true);
	}

	public CComponentEntitySelection(final List<EntityTypeConfig<?>> entityTypes, final ItemsProvider<EntityClass> itemsProvider,
			final Consumer<Set<EntityClass>> onSelectionChanged, final boolean multiSelect, final ItemsProvider<EntityClass> alreadySelectedProvider,
			final AlreadySelectedMode alreadySelectedMode, boolean runSetupComponent) {
		super();
		Check.notEmpty(entityTypes, "Entity types cannot be empty");
		Check.notNull(itemsProvider, "Items provider cannot be null");
		Check.notNull(onSelectionChanged, "Selection callback cannot be null");
		Check.notNull(alreadySelectedMode, "Already selected mode cannot be null");
		this.entityTypes = entityTypes;
		this.itemsProvider = itemsProvider;
		this.onSelectionChanged = onSelectionChanged;
		this.multiSelect = multiSelect;
		this.alreadySelectedProvider = alreadySelectedProvider;
		this.alreadySelectedMode = alreadySelectedMode;
		if (runSetupComponent) {
			try {
				setupComponent();
				// Select first entity type if available
				if (!entityTypes.isEmpty()) {
					comboBoxEntityType.setValue(entityTypes.get(0));
				}
			} catch (final Exception e) {
				LOGGER.error("Error setting up entity selection component", e);
				CNotificationService.showException("Error creating component", e);
			}
		}
	}

	// IGridRefreshListener implementation
	/** Adds a listener to be notified when this component's grid data changes. Implements IGridRefreshListener.addRefreshListener()
	 * @param listener Consumer called when data changes (receives the changed item if available, or null) */
	@Override
	public void addRefreshListener(final Consumer<EntityClass> listener) {
		Check.notNull(listener, "Refresh listener cannot be null");
		refreshListeners.add(listener);
		LOGGER.debug("Added refresh listener to CComponentEntitySelection");
	}

	/** Registers a selection change listener. Implements HasValue.addValueChangeListener().
	 * @param listener Listener to be notified when selection changes
	 * @return Registration that can be used to remove the listener */
	@Override
	public Registration addValueChangeListener(final HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<Set<EntityClass>>> listener) {
		Check.notNull(listener, "ValueChangeListener cannot be null");
		selectionListeners.add(listener);
		return () -> selectionListeners.remove(listener);
	}

	private void applyFilters() {
		try {
			if (currentEntityType == null) {
				LOGGER.debug("applyFilters skipped - no current entity type selected");
				return;
			}
			// Always reload items from provider so the grid reflects latest DB state.
			allItems = itemsProvider.getItems(currentEntityType);
			Check.notNull(allItems, "Items provider returned null for entity type: " + currentEntityType.getDisplayName());
			LOGGER.debug("Loaded {} items for entity type {}", allItems.size(), currentEntityType.getDisplayName());
			processAlreadySelectedItems();
			updateStatusFilterOptions();
			Check.notNull(gridSearchToolbar, "Grid search toolbar must be initialized");
			final CComponentGridSearchToolbar.FilterCriteria criteria = gridSearchToolbar.getCurrentFilters();
			Check.notNull(criteria, "Filter criteria cannot be null");
			final String idValue = criteria.getIdFilter();
			final String nameValue = criteria.getNameFilter();
			final String descValue = criteria.getDescriptionFilter();
			final String statusValue = criteria.getStatusFilter();
			final List<EntityClass> filtered = new ArrayList<>();
			for (final EntityClass item : allItems) {
				Check.notNull(item, "Item in allItems cannot be null");
				boolean matches = true;
				// ID filter - search in "id" field (use mutable list for matchesFilter)
				if (matches && idValue != null && !idValue.isBlank()) {
					if (!item.matchesFilter(idValue, new ArrayList<>(List.of("id")))) {
						matches = false;
					}
				}
				// Name filter - search in "name" field (use mutable list for matchesFilter)
				if (matches && nameValue != null && !nameValue.isBlank()) {
					if (!item.matchesFilter(nameValue, new ArrayList<>(List.of("name")))) {
						matches = false;
					}
				}
				// Description filter - search in "description" field (use mutable list for matchesFilter)
				if (matches && descValue != null && !descValue.isBlank()) {
					if (!item.matchesFilter(descValue, new ArrayList<>(List.of("description")))) {
						matches = false;
					}
				}
				// Status filter - search in "status" field (use mutable list for matchesFilter)
				if (matches && statusValue != null && !statusValue.isBlank()) {
					if (!item.matchesFilter(statusValue, new ArrayList<>(List.of("status")))) {
						matches = false;
					}
				}
				if (matches) {
					filtered.add(item);
				}
			}
			grid.setItems(filtered);
			// Restore visual selection state for already selected items
			if (multiSelect) {
				for (final EntityClass item : filtered) {
					if (selectedItems.contains(item)) {
						grid.select(item);
					}
				}
			}
			// LOGGER.debug("Applied filters - showing {} of {} items", filtered.size(), allItems.size());
		} catch (final Exception e) {
			LOGGER.error("Error applying filters", e);
			throw new IllegalStateException("Failed to apply filters", e);
		}
	}

	/** Clears the selection. Equivalent to calling setValue with an empty set. */
	@Override
	public void clear() {
		LOGGER.debug("Clearing all selected items");
		selectedItems.clear();
		if (multiSelect) {
			grid.deselectAll();
		} else {
			grid.asSingleSelect().clear();
		}
		updateSelectionIndicator();
	}

	/** Clears all items from the grid. Implements IGridComponent.clearGrid() */
	@Override
	public void clearGrid() {
		try {
			LOGGER.debug("Clearing grid");
			allItems = new ArrayList<>();
			selectedItems.clear();
			grid.setItems(allItems);
			updateSelectionIndicator();
		} catch (final Exception e) {
			LOGGER.error("Error clearing grid", e);
			CNotificationService.showException("Error clearing grid", e);
		}
	}

	/** Configure grid columns following standard CGrid pattern. Implements IGridComponent.configureGrid()
	 * @param grid1 The grid to configure (must not be null) */
	@Override
	@SuppressWarnings ("rawtypes")
	public void configureGrid(final CGrid<EntityClass> grid1) {
		Check.notNull(grid1, "Grid cannot be null");
		// Clear existing columns
		grid1.getColumns().forEach(grid1::removeColumn);
		if (currentEntityType == null) {
			return;
		}
		LOGGER.debug("Configuring grid columns for entity type: {}", currentEntityType.getDisplayName());
		grid1.addIdColumn(item -> item.getId(), "ID", "id");
		grid1.addShortTextColumn(this::getEntityName, "Name", "name");
		// Use expanding column for description to fill remaining width
		grid1.addExpandingLongTextColumn(this::getEntityDescription, "Description", "description");
		CGrid.styleColumnHeader(grid1.addComponentColumn(item -> {
			try {
				return new CLabelEntity(((IHasStatusAndWorkflow) item).getStatus());
			} catch (final Exception e) {
				e.printStackTrace();
			}
			return new CLabelEntity("Error");
		}).setWidth(CGrid.WIDTH_REFERENCE).setFlexGrow(0).setSortable(true).setKey("status"), "Status");
	}

	/** Factory method for entity type selector layout.
	 * @param layout */
	protected void create_combobox_typeSelector() {
		comboBoxEntityType = new ComboBox<>("Entity Type");
		comboBoxEntityType.setItems(entityTypes);
		comboBoxEntityType.setItemLabelGenerator(EntityTypeConfig::getDisplayName);
		comboBoxEntityType.setWidth("150px");
		comboBoxEntityType.setRequired(true);
		comboBoxEntityType.addValueChangeListener(e -> on_comboBoxEntityType_selectionChanged(e.getValue()));
	}

	/** Factory method for grid following standard pattern. */
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	protected void create_gridItems() {
		Check.isTrue(grid == null, "Grid should only be created once");
		// Create CGrid using Object type with auto-columns disabled, then cast.
		// Type safety is maintained by controlling all items in the grid through itemsProvider.
		final CGrid rawGrid = new CGrid<>(Object.class);
		grid = rawGrid;
		// Configure size
		grid.setSizeFull(); // Grid should expand
		grid.setHeightFull(); // Ensure full height expansion
		grid.setMinHeight("120px");
		// Configure selection mode
		if (multiSelect) {
			grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.MULTI);
			grid.asMultiSelect().addValueChangeListener(e -> on_gridItems_multiSelectionChanged(e.getValue()));
		} else {
			grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
			grid.asSingleSelect().addValueChangeListener(e -> on_gridItems_singleSelectionChanged(e.getValue()));
		}
		// Add click listener to toggle selection in multi-select mode
		if (multiSelect) {
			grid.addItemClickListener(e -> on_gridItems_itemClicked(e.getItem()));
		}
		grid.setRefreshConsumer(e -> applyFilters());
		// Set up drag-drop event forwarding from grid to this component
		setupChildDragDropForwarding(grid);
		// Note: configureGrid() is called later when entity type is selected
		LOGGER.debug("Grid created for entity selection component");
	}

	/** Factory method for search toolbar layout using CComponentFilterToolbar. */
	protected CComponentFilterToolbar create_gridSearchToolbar() {
		final CComponentFilterToolbar toolbar = new CComponentFilterToolbar();
		return toolbar;
	}

	/** Factory method for selection indicator layout. */
	protected HorizontalLayout create_layoutSelectionIndicator() {
		final CHorizontalLayout layout = new CHorizontalLayout();
		layout.setWidthFull();
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		// Selected count indicator
		final Icon selectedIcon = VaadinIcon.CHECK_SQUARE.create();
		selectedIcon.setSize("16px");
		selectedIcon.setColor("#1976D2");
		labelSelectedCount = new Span("0 selected");
		labelSelectedCount.getStyle().set("font-weight", "500").set("color", "#1976D2").set("margin-right", "10px");
		// Reset button
		buttonReset = new CButton("Reset", VaadinIcon.REFRESH.create());
		buttonReset.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
		buttonReset.setTooltipText("Clear all selected items");
		buttonReset.addClickListener(e -> on_buttonReset_clicked());
		buttonReset.setEnabled(false);
		layout.add(selectedIcon, labelSelectedCount, buttonReset);
		return layout;
	}

	@Override
	public void drag_checkEventAfterPass(CEvent event) {
		// TODO Auto-generated method stub
	}

	@Override
	public void drag_checkEventBeforePass(CEvent event) {
		LOGGER.debug("Drag event check before pass: {} comp id:{} event type:{}", event, getId(), event.getClass().getSimpleName());
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

	@Override
	public boolean drag_isDropAllowed(CDragStartEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void drag_setDragEnabled(final boolean enabled) {
		grid.drag_setDragEnabled(enabled);
		LOGGER.debug("[DragDebug] Drag {} for entity selection", enabled ? "enabled" : "disabled");
	}

	@Override
	public void drag_setDropEnabled(final boolean enabled) {
		grid.drag_setDropEnabled(enabled);
		LOGGER.debug("[DragDebug] Drop {} for entity selection", enabled ? "enabled" : "disabled");
	}

	/** Returns the list of already selected items.
	 * @return List of already selected items (can be empty, never null) */
	public List<EntityClass> getAlreadySelectedItems() {
		return new ArrayList<>(alreadySelectedItems);
	}

	/** Returns the already selected mode configured for this component.
	 * @return The AlreadySelectedMode */
	public AlreadySelectedMode getAlreadySelectedMode() { return alreadySelectedMode; }
	// ==================== IHasDragStart, IHasDragEnd, IHasDrop Implementation ====================

	/** Gets description from entity. Entity must extend CEntityNamed. */
	private String getEntityDescription(final EntityClass item) {
		Check.notNull(item, "Item cannot be null");
		Check.instanceOf(item, CEntityNamed.class, "Item must be of type CEntityNamed to access description");
		final String description = ((CEntityNamed<?>) item).getDescription();
		return description != null ? description : "";
	}

	/** Gets name from entity. Entity must extend CEntityNamed. */
	private String getEntityName(final EntityClass item) {
		Check.notNull(item, "Item cannot be null");
		Check.instanceOf(item, CEntityNamed.class, "Item must be of type CEntityNamed to access name");
		final String name = ((CEntityNamed<?>) item).getName();
		return name != null ? name : "";
	}

	/** Gets status from entity. Entity must implement IHasStatusAndWorkflow. */
	@SuppressWarnings ("rawtypes")
	private Object getEntityStatus(final EntityClass item) {
		Check.notNull(item, "Item cannot be null");
		Check.instanceOf(item, IHasStatusAndWorkflow.class, "Item must implement IHasStatusAndWorkflow to access status");
		return ((IHasStatusAndWorkflow) item).getStatus();
	}

	/** Gets the grid component for external configuration (e.g., drag and drop).
	 * @return the CGrid instance */
	@Override
	public CGrid<EntityClass> getGrid() { return grid; }

	/** Returns the currently selected items.
	 * <p>
	 * Note: This method is functionally equivalent to {@link #getValue()} from the HasValue interface. Both methods return the same set of selected
	 * items. Use getValue() when working with Vaadin binders, or getSelectedItems() for direct access in application code.
	 * @return Set of selected items (never null) */
	public Set<EntityClass> getSelectedItems() { return new HashSet<>(selectedItems); }

	/** Gets the current value of this component (the selected items).
	 * @return Set of currently selected items (never null) */
	@Override
	public Set<EntityClass> getValue() { return new HashSet<>(selectedItems); }

	/** Checks if the selection is empty.
	 * @return true if no items are selected, false otherwise */
	@Override
	public boolean isEmpty() { return selectedItems.isEmpty(); }

	/** Returns whether the component is configured for multi-select.
	 * @return true if multi-select mode */
	public boolean isMultiSelect() { return multiSelect; }

	/** Checks if the component is read-only.
	 * @return false (read-only mode not currently implemented) */
	@Override
	public boolean isReadOnly() { return false; }

	/** Checks if the required indicator is visible.
	 * @return false (required indicator not currently implemented) */
	@Override
	public boolean isRequiredIndicatorVisible() { return false; }

	/** Loads already selected items from the provider if available. */
	private void loadAlreadySelectedItems(final EntityTypeConfig<?> config) {
		alreadySelectedItems = new ArrayList<>();
		if (alreadySelectedProvider == null) {
			return;
		}
		try {
			final List<EntityClass> items = alreadySelectedProvider.getItems(config);
			if (items != null) {
				alreadySelectedItems = new ArrayList<>(items);
				LOGGER.debug("Loaded {} already selected items for entity type {}", alreadySelectedItems.size(), config.getDisplayName());
			}
		} catch (final Exception e) {
			LOGGER.error("Error loading already selected items for entity type {}", config.getDisplayName(), e);
			alreadySelectedItems = new ArrayList<>();
		}
	}

	/** Notifies all registered listeners that this component's data has changed. This is called AFTER the component has updated its own data and
	 * refreshed its grid. Implements IGridRefreshListener.notifyRefreshListeners()
	 * @param changedItem The item that changed, or null if multiple items changed or change is general */
	@Override
	public void notifyRefreshListeners(final EntityClass changedItem) {
		if (!refreshListeners.isEmpty()) {
			LOGGER.debug("Notifying {} refresh listeners about data change in CComponentEntitySelection", refreshListeners.size());
			for (final Consumer<EntityClass> listener : refreshListeners) {
				try {
					listener.accept(changedItem);
				} catch (final Exception e) {
					LOGGER.error("Error notifying refresh listener", e);
				}
			}
		}
	}

	/** Notifies the selection owner about selection changes. Should be called whenever selection changes in the component. */
	protected void notifySelectionOwner() {
		if (selectionOwner != null) {
			selectionOwner.onSelectionChanged(new HashSet<>(selectedItems));
		}
	}

	/** Handle reset button click. */
	protected void on_buttonReset_clicked() {
		selectedItems.clear();
		if (multiSelect) {
			grid.deselectAll();
		} else {
			grid.asSingleSelect().clear();
		}
		updateSelectionIndicator();
		LOGGER.debug("Selection reset");
	}

	/** Handle entity type combobox selection change. */
	protected void on_comboBoxEntityType_selectionChanged(final EntityTypeConfig<?> config) {
		try {
			Check.notNull(config, "Entity type config cannot be null");
			LOGGER.debug("Entity type changed to: {}", config.getDisplayName());
			currentEntityType = config;
			loadAlreadySelectedItems(config);
			updateSelectionIndicator();
			// Configure grid columns for the new entity type
			configureGrid(grid);
			// Apply filters and refresh grid
			applyFilters();
		} catch (final Exception e) {
			LOGGER.error("Error loading items for entity type {}", config != null ? config.getDisplayName() : "null", e);
			CNotificationService.showException("Error loading items", e);
			allItems = new ArrayList<>();
			grid.setItems(allItems);
		}
	}

	/** Handle grid item click (for toggle in multi-select mode). */
	protected void on_gridItems_itemClicked(final EntityClass item) {
		try {
			if (selectedItems.contains(item)) {
				selectedItems.remove(item);
				grid.deselect(item);
			} else {
				selectedItems.add(item);
				grid.select(item);
			}
			updateSelectionIndicator();
		} catch (final Exception e) {
			LOGGER.error("Error handling grid item click", e);
			CNotificationService.showException("Error selecting item", e);
		}
	}

	/** Handle grid multi-select value change. */
	protected void on_gridItems_multiSelectionChanged(final Set<EntityClass> value) {
		try {
			// Add new selections, but don't remove items that were previously selected
			// This allows selections to persist across filtering
			for (final EntityClass item : value) {
				if (!selectedItems.contains(item)) {
					selectedItems.add(item);
				}
			}
			updateSelectionIndicator();
		} catch (final Exception e) {
			LOGGER.error("Error handling grid multi-selection change", e);
			CNotificationService.showException("Error selecting items", e);
		}
	}
	// Owner interface setters and notification methods

	/** Handle grid single-select value change. */
	protected void on_gridItems_singleSelectionChanged(final EntityClass value) {
		try {
			selectedItems.clear();
			if (value != null) {
				selectedItems.add(value);
			}
			updateSelectionIndicator();
		} catch (final Exception e) {
			LOGGER.error("Error handling grid single-selection change", e);
			CNotificationService.showException("Error selecting item", e);
		}
	}

	/** Process already selected items based on the configured mode. This method either filters out already selected items from allItems
	 * (HIDE_ALREADY_SELECTED mode) or adds them to selectedItems to show them as pre-selected (SHOW_AS_SELECTED mode).
	 * <p>
	 * In single-select mode with SHOW_AS_SELECTED, only the first matching item will be pre-selected. */
	private void processAlreadySelectedItems() {
		if (alreadySelectedItems.isEmpty()) {
			return;
		}
		// Create a set of already selected item IDs for efficient lookup
		final Set<Object> alreadySelectedIds = new HashSet<>();
		for (final EntityClass item : alreadySelectedItems) {
			if (item.getId() != null) {
				alreadySelectedIds.add(item.getId());
			}
		}
		switch (alreadySelectedMode) {
		case HIDE_ALREADY_SELECTED:
			// Filter out already selected items from the available items list
			final List<EntityClass> filteredItems = new ArrayList<>();
			for (final EntityClass item : allItems) {
				if (item.getId() == null || !alreadySelectedIds.contains(item.getId())) {
					filteredItems.add(item);
				}
			}
			final int removedCount = allItems.size() - filteredItems.size();
			allItems = filteredItems;
			LOGGER.debug("Hidden {} already selected items from available items list", removedCount);
			break;
		case SHOW_AS_SELECTED:
			// Pre-select the already selected items (they will be visually marked in the grid)
			// In single-select mode, only pre-select the first matching item
			for (final EntityClass item : allItems) {
				if (item.getId() != null && alreadySelectedIds.contains(item.getId())) {
					selectedItems.add(item);
					// In single-select mode, only allow one item to be pre-selected
					if (!multiSelect) {
						break;
					}
				}
			}
			LOGGER.debug("Pre-selected {} already selected items", selectedItems.size());
			updateSelectionIndicator();
			break;
		default:
			break;
		}
	}

	/** Refreshes the grid to reflect updated data. Should be called after items are added/removed externally. Implements
	 * IGridComponent.refreshGrid() */
	@Override
	public void refreshGrid() {
		try {
			if (currentEntityType != null) {
				on_comboBoxEntityType_selectionChanged(currentEntityType);
			}
		} catch (final Exception e) {
			LOGGER.error("Error refreshing component", e);
			CNotificationService.showException("Error refreshing component", e);
		}
	}

	/** Removes a previously added refresh listener. Implements IGridRefreshListener.removeRefreshListener()
	 * @param listener The listener to remove */
	@Override
	public void removeRefreshListener(final Consumer<EntityClass> listener) {
		if (listener != null) {
			refreshListeners.remove(listener);
			LOGGER.debug("Removed refresh listener from CComponentEntitySelection");
		}
	}

	public void setDynamicHeight(final String maxHeight) {
		getContent().setSizeUndefined();
		getContent().setWidthFull();
		getContent().setMinHeight("60px");
		getContent().setMaxHeight(maxHeight);
		grid.setDynamicHeight();
	}
	// HasValue interface implementation

	/** Sets the entity type for the component.
	 * @param config The entity type configuration to set */
	public void setEntityType(final EntityTypeConfig<?> config) {
		if (config != null && entityTypes.contains(config)) {
			comboBoxEntityType.setValue(config);
		}
	}

	/** Sets the read-only state of this component. When read-only, users cannot change the selection.
	 * @param readOnly true to make read-only, false to make editable */
	@Override
	public void setReadOnly(final boolean readOnly) {
		// Note: CComponentEntitySelection doesn't currently support read-only mode
		// This could be implemented by disabling the grid and controls
		LOGGER.debug("setReadOnly({}) called - not currently implemented", readOnly);
	}

	/** Sets whether the required indicator should be visible.
	 * @param requiredIndicatorVisible true to show required indicator, false to hide */
	@Override
	public void setRequiredIndicatorVisible(final boolean requiredIndicatorVisible) {
		// Note: CComponentEntitySelection doesn't currently support required indicator
		// This could be implemented by adding a visual indicator to the component
		LOGGER.debug("setRequiredIndicatorVisible({}) called - not currently implemented", requiredIndicatorVisible);
	}

	/** Sets the selection owner to be notified of selection changes.
	 * @param owner the selection owner (can be null) */
	public void setSelectionOwner(final ISelectionOwner<EntityClass> owner) {
		selectionOwner = owner;
	}

	protected void setupComponent() {
		final CVerticalLayout mainLayout = getContent();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(false); // Reduce spacing between components
		mainLayout.setPadding(false);
		// Entity type selector
		create_combobox_typeSelector();
		gridSearchToolbar = create_gridSearchToolbar();
		// Add filter change listener to trigger grid filtering
		gridSearchToolbar.addFilterChangeListener(criteria -> applyFilters());
		gridSearchToolbar.addComponentAsFirst(comboBoxEntityType);
		mainLayout.add(gridSearchToolbar);
		// Selection indicator and reset (only for multi-select mode)
		if (multiSelect) {
			final HorizontalLayout layoutSelectionIndicator = create_layoutSelectionIndicator();
			mainLayout.add(layoutSelectionIndicator);
		}
		// Grid
		create_gridItems();
		mainLayout.add(grid);
		mainLayout.setFlexGrow(1, grid); // Make grid expand
	}

	/** Sets the value of this component (the selected items). This will update the selection and fire value change events.
	 * @param value Set of items to select (must not be null) */
	@Override
	public void setValue(final Set<EntityClass> value) {
		Check.notNull(value, "Value cannot be null");
		LOGGER.debug("Setting value with {} items", value.size());
		selectedItems.clear();
		selectedItems.addAll(value);
		// Update grid selection
		if (multiSelect) {
			grid.deselectAll();
			for (final EntityClass item : selectedItems) {
				grid.select(item);
			}
		} else if (!selectedItems.isEmpty()) {
			grid.asSingleSelect().setValue(selectedItems.iterator().next());
		} else {
			grid.asSingleSelect().clear();
		}
		updateSelectionIndicator();
	}

	private void updateSelectionIndicator() {
		final int count = selectedItems.size();
		// Update UI components only in multi-select mode
		if (multiSelect && labelSelectedCount != null && buttonReset != null) {
			labelSelectedCount.setText(count + " selected");
			// Update button states based on selection
			final boolean hasSelection = count > 0;
			buttonReset.setEnabled(hasSelection);
		}
		// Notify parent container of selection change
		if (onSelectionChanged != null) {
			onSelectionChanged.accept(new HashSet<>(selectedItems));
		}
		// Notify selection owner if set
		notifySelectionOwner();
		// Notify registered selection listeners (HasValue interface)
		try {
			final Set<EntityClass> oldValue = new HashSet<>(currentSelectionSnapshot);
			final Set<EntityClass> newValue = new HashSet<>(selectedItems);
			if (!oldValue.equals(newValue)) {
				currentSelectionSnapshot = new HashSet<>(newValue);
				final HasValue.ValueChangeEvent<Set<EntityClass>> event = new HasValue.ValueChangeEvent<Set<EntityClass>>() {

					private static final long serialVersionUID = 1L;

					@Override
					public HasValue<?, Set<EntityClass>> getHasValue() { return CComponentEntitySelection.this; }

					@Override
					public Set<EntityClass> getOldValue() { return oldValue; }

					@Override
					public Set<EntityClass> getValue() { return newValue; }

					@Override
					public boolean isFromClient() { return true; }
				};
				for (final HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<Set<EntityClass>>> listener : selectionListeners) {
					try {
						listener.valueChanged(event);
					} catch (final Exception e) {
						LOGGER.error("Error notifying selection listener", e);
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error while notifying selection listeners", e);
		}
	}

	private void updateStatusFilterOptions() {
		Check.notNull(gridSearchToolbar, "Grid search toolbar must be initialized");
		final Set<String> statuses = new HashSet<>();
		for (final EntityClass item : allItems) {
			if (item instanceof CProjectItem) {
				final CProjectItem<?> projectItem = (CProjectItem<?>) item;
				if (projectItem.getStatus() != null) {
					statuses.add(projectItem.getStatus().getName());
				}
			} else {
				final Object status = getEntityStatus(item);
				if (status instanceof CEntityNamed) {
					final String name = ((CEntityNamed<?>) status).getName();
					if (name != null) {
						statuses.add(name);
					}
				}
			}
		}
		gridSearchToolbar.setStatusOptions(statuses);
	}
}
