package tech.derbent.api.ui.component.enhanced;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

/** CComponentEntitySelection - Reusable component for selecting entities from a grid with search/filter capabilities.
 * <p>
 * This component can be embedded in dialogs, pages, or panels for entity selection functionality.
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
 * </ul>
 * @param <EntityClass> The entity type being selected */
public class CComponentEntitySelection<EntityClass extends CEntityDB<?>> extends Composite<CVerticalLayout> {

	/** Mode for handling already selected items */
	public enum AlreadySelectedMode {
		/** Hide already selected items from the available items list */
		HIDE_ALREADY_SELECTED,
		/** Show already selected items in the grid, pre-selected (visually marked) */
		SHOW_AS_SELECTED
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

	/** Callback for getting items based on entity type */
	@FunctionalInterface
	public interface ItemsProvider<T> {

		List<T> getItems(EntityTypeConfig<?> config);
	}

	protected static final Logger LOGGER = LoggerFactory.getLogger(CComponentEntitySelection.class);
	private static final long serialVersionUID = 1L;
	private List<EntityClass> allItems = new ArrayList<>();
	private List<EntityClass> alreadySelectedItems = new ArrayList<>();
	private final AlreadySelectedMode alreadySelectedMode;
	private final ItemsProvider<EntityClass> alreadySelectedProvider;
	private CButton buttonReset;
	private Method cachedGetDescriptionMethod;
	private Method cachedGetNameMethod;
	private Method cachedGetStatusMethod;
	private ComboBox<EntityTypeConfig<?>> comboBoxEntityType;
	private EntityTypeConfig<?> currentEntityType;
	private final List<EntityTypeConfig<?>> entityTypes;
	private CGrid<EntityClass> gridItems;
	private CComponentGridSearchToolbar gridSearchToolbar;
	private final ItemsProvider<EntityClass> itemsProvider;
	private Span labelSelectedCount;
	private final boolean multiSelect;
	private final Consumer<Set<EntityClass>> onSelectionChanged;
	private final Set<EntityClass> selectedItems = new HashSet<>();

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
	public CComponentEntitySelection(final List<EntityTypeConfig<?>> entityTypes, final ItemsProvider<EntityClass> itemsProvider,
			final Consumer<Set<EntityClass>> onSelectionChanged, final boolean multiSelect, final ItemsProvider<EntityClass> alreadySelectedProvider,
			final AlreadySelectedMode alreadySelectedMode) {
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

	private void applyFilters() {
		try {
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
				// ID filter
				if ((idValue != null) && !idValue.isBlank()) {
					final String itemId = item.getId() != null ? item.getId().toString() : "";
					if (!itemId.toLowerCase().contains(idValue.toLowerCase())) {
						matches = false;
					}
				}
				// Name filter - use cached method
				if (matches && (nameValue != null) && !nameValue.isBlank()) {
					final String name = getEntityName(item);
					if (!name.toLowerCase().contains(nameValue.toLowerCase())) {
						matches = false;
					}
				}
				// Description filter - use cached method
				if (matches && (descValue != null) && !descValue.isBlank()) {
					final String desc = getEntityDescription(item);
					if (!desc.toLowerCase().contains(descValue.toLowerCase())) {
						matches = false;
					}
				}
				// Status filter
				if (matches && (statusValue != null) && !statusValue.isBlank()) {
					String statusName = null;
					if (item instanceof CProjectItem) {
						final CProjectItem<?> projectItem = (CProjectItem<?>) item;
						if (projectItem.getStatus() != null) {
							statusName = projectItem.getStatus().getName();
						}
					} else {
						final Object status = getEntityStatus(item);
						if (status instanceof CEntityDB) {
							try {
								final java.lang.reflect.Method nameMethod = status.getClass().getMethod("getName");
								final Object name = nameMethod.invoke(status);
								statusName = name != null ? name.toString() : null;
							} catch (final Exception e) {
								statusName = null;
							}
						}
					}
					if ((statusName == null) || !statusName.equals(statusValue)) {
						matches = false;
					}
				}
				if (matches) {
					filtered.add(item);
				}
			}
			gridItems.setItems(filtered);
			// Restore visual selection state for already selected items
			if (multiSelect) {
				for (final EntityClass item : filtered) {
					if (selectedItems.contains(item)) {
						gridItems.select(item);
					}
				}
			}
			LOGGER.debug("Applied filters - showing {} of {} items", filtered.size(), allItems.size());
		} catch (final Exception e) {
			LOGGER.error("Error applying filters", e);
			throw new IllegalStateException("Failed to apply filters", e);
		}
	}

	/** Caches reflection methods for the current entity type for better performance. */
	private void cacheReflectionMethods(final Class<?> entityClass) {
		try {
			cachedGetNameMethod = entityClass.getMethod("getName");
		} catch (final NoSuchMethodException e) {
			cachedGetNameMethod = null;
		}
		try {
			cachedGetDescriptionMethod = entityClass.getMethod("getDescription");
		} catch (final NoSuchMethodException e) {
			cachedGetDescriptionMethod = null;
		}
		try {
			cachedGetStatusMethod = entityClass.getMethod("getStatus");
		} catch (final NoSuchMethodException e) {
			cachedGetStatusMethod = null;
		}
	}

	@SuppressWarnings ("rawtypes")
	private void configureGridColumns() {
		// Clear existing columns
		gridItems.getColumns().forEach(gridItems::removeColumn);
		if (currentEntityType == null) {
			return;
		}
		gridItems.addIdColumn(item -> item.getId(), "ID", "id");
		gridItems.addShortTextColumn(this::getEntityName, "Name", "name");
		gridItems.addLongTextColumn(this::getEntityDescription, "Description", "description");
		CGrid.styleColumnHeader(gridItems.addComponentColumn(item -> {
			try {
				return new CLabelEntity(((IHasStatusAndWorkflow) item).getStatus());
			} catch (final Exception e) {
				e.printStackTrace();
			}
			return new CLabelEntity("Error");
		}).setWidth(CGrid.WIDTH_REFERENCE).setFlexGrow(0).setSortable(true).setKey("status"), "Status");
	}

	/** Factory method for grid. */
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	protected void create_gridItems() {
		// Create CGrid using Object type with auto-columns disabled, then cast.
		// Type safety is maintained by controlling all items in the grid through itemsProvider.
		final CGrid rawGrid = new CGrid<>(Object.class);
		gridItems = rawGrid;
		gridItems.setSizeFull(); // Grid should expand
		gridItems.setHeightFull(); // Ensure full height expansion
		// Configure selection mode
		if (multiSelect) {
			gridItems.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.MULTI);
			gridItems.asMultiSelect().addValueChangeListener(e -> on_gridItems_multiSelectionChanged(e.getValue()));
		} else {
			gridItems.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
			gridItems.asSingleSelect().addValueChangeListener(e -> on_gridItems_singleSelectionChanged(e.getValue()));
		}
		// Add click listener to toggle selection in multi-select mode
		if (multiSelect) {
			gridItems.addItemClickListener(e -> on_gridItems_itemClicked(e.getItem()));
		}
	}

	/** Factory method for search toolbar layout using CComponentGridSearchToolbar. */
	protected CComponentGridSearchToolbar create_gridSearchToolbar() {
		final CComponentGridSearchToolbar toolbar = new CComponentGridSearchToolbar();
		// Add filter change listener to trigger grid filtering
		toolbar.addFilterChangeListener(criteria -> applyFilters());
		return toolbar;
	}

	/** Factory method for entity type selector layout. */
	protected HorizontalLayout create_layoutEntityTypeSelector() {
		final CHorizontalLayout layout = new CHorizontalLayout();
		layout.setWidthFull();
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		comboBoxEntityType = new ComboBox<>("Entity Type");
		comboBoxEntityType.setItems(entityTypes);
		comboBoxEntityType.setItemLabelGenerator(EntityTypeConfig::getDisplayName);
		comboBoxEntityType.setWidthFull();
		comboBoxEntityType.setRequired(true);
		comboBoxEntityType.addValueChangeListener(e -> on_comboBoxEntityType_selectionChanged(e.getValue()));
		layout.add(comboBoxEntityType);
		return layout;
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

	/** Returns the list of already selected items.
	 * @return List of already selected items (can be empty, never null) */
	public List<EntityClass> getAlreadySelectedItems() {
		return new ArrayList<>(alreadySelectedItems);
	}

	/** Returns the already selected mode configured for this component.
	 * @return The AlreadySelectedMode */
	public AlreadySelectedMode getAlreadySelectedMode() { return alreadySelectedMode; }

	/** Gets description from entity using cached method. */
	private String getEntityDescription(final EntityClass item) {
		if (cachedGetDescriptionMethod == null) {
			return "";
		}
		try {
			final Object result = cachedGetDescriptionMethod.invoke(item);
			return result != null ? result.toString() : "";
		} catch (final Exception e) {
			return "";
		}
	}

	/** Gets name from entity using cached method. */
	private String getEntityName(final EntityClass item) {
		if (cachedGetNameMethod == null) {
			return "";
		}
		try {
			final Object result = cachedGetNameMethod.invoke(item);
			return result != null ? result.toString() : "";
		} catch (final Exception e) {
			return "";
		}
	}

	/** Gets status from entity using cached method. */
	private Object getEntityStatus(final EntityClass item) {
		if (cachedGetStatusMethod == null) {
			return null;
		}
		try {
			return cachedGetStatusMethod.invoke(item);
		} catch (final Exception e) {
			return null;
		}
	}

	/** Returns the currently selected items.
	 * @return Set of selected items */
	public Set<EntityClass> getSelectedItems() { return new HashSet<>(selectedItems); }

	/** Returns whether the component is configured for multi-select.
	 * @return true if multi-select mode */
	public boolean isMultiSelect() { return multiSelect; }

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

	/** Handle reset button click. */
	protected void on_buttonReset_clicked() {
		selectedItems.clear();
		if (multiSelect) {
			gridItems.deselectAll();
		} else {
			gridItems.asSingleSelect().clear();
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
			// Cache reflection methods for the entity type
			cacheReflectionMethods(config.getEntityClass());
			// Configure grid columns for the new entity type
			configureGridColumns();
			// Load items
			allItems = itemsProvider.getItems(config);
			Check.notNull(allItems, "Items provider returned null for entity type: " + config.getDisplayName());
			LOGGER.debug("Loaded {} items for entity type {}", allItems.size(), config.getDisplayName());
			// Handle already selected items based on mode
			processAlreadySelectedItems();
			// Update status filter options
			updateStatusFilterOptions();
			// Apply filters and refresh grid
			applyFilters();
		} catch (final Exception e) {
			LOGGER.error("Error loading items for entity type {}", config != null ? config.getDisplayName() : "null", e);
			CNotificationService.showException("Error loading items", e);
			allItems = new ArrayList<>();
			gridItems.setItems(allItems);
		}
	}

	/** Handle grid item click (for toggle in multi-select mode). */
	protected void on_gridItems_itemClicked(final EntityClass item) {
		try {
			if (selectedItems.contains(item)) {
				selectedItems.remove(item);
				gridItems.deselect(item);
			} else {
				selectedItems.add(item);
				gridItems.select(item);
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
				if ((item.getId() == null) || !alreadySelectedIds.contains(item.getId())) {
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
				if ((item.getId() != null) && alreadySelectedIds.contains(item.getId())) {
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

	/** Resets the component selection state. */
	public void reset() {
		on_buttonReset_clicked();
	}

	/** Sets the entity type for the component.
	 * @param config The entity type configuration to set */
	public void setEntityType(final EntityTypeConfig<?> config) {
		if (config != null && entityTypes.contains(config)) {
			comboBoxEntityType.setValue(config);
		}
	}

	protected void setupComponent() {
		final CVerticalLayout mainLayout = getContent();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(false); // Reduce spacing between components
		mainLayout.setPadding(false);
		// Entity type selector
		final HorizontalLayout layoutEntityType = create_layoutEntityTypeSelector();
		mainLayout.add(layoutEntityType);
		// Search toolbar using CComponentGridSearchToolbar
		gridSearchToolbar = create_gridSearchToolbar();
		mainLayout.add(gridSearchToolbar);
		// Selection indicator and reset
		final HorizontalLayout layoutSelectionIndicator = create_layoutSelectionIndicator();
		mainLayout.add(layoutSelectionIndicator);
		// Grid
		create_gridItems();
		mainLayout.add(gridItems);
		mainLayout.setFlexGrow(1, gridItems); // Make grid expand
	}

	private void updateSelectionIndicator() {
		final int count = selectedItems.size();
		labelSelectedCount.setText(count + " selected");
		// Update button states based on selection
		final boolean hasSelection = count > 0;
		buttonReset.setEnabled(hasSelection);
		// Notify parent container of selection change
		if (onSelectionChanged != null) {
			onSelectionChanged.accept(new HashSet<>(selectedItems));
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
				if (status instanceof CEntityDB) {
					try {
						final java.lang.reflect.Method nameMethod = status.getClass().getMethod("getName");
						final Object name = nameMethod.invoke(status);
						if (name != null) {
							statuses.add(name.toString());
						}
					} catch (final Exception e) {
						// Ignore - status may not have getName
					}
				}
			}
		}
		gridSearchToolbar.setStatusOptions(statuses);
	}
}
