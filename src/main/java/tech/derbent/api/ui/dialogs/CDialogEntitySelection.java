package tech.derbent.api.ui.dialogs;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CGridCell;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;

/** CDialogEntitySelection - Dialog for selecting entities from a grid with search/filter capabilities.
 * <p>
 * Extends CDialog to follow the standard dialog pattern in the application.
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
public class CDialogEntitySelection<EntityClass extends CEntityDB<?>> extends CDialog {

	/** Mode for handling already selected items in the dialog */
	public enum AlreadySelectedMode {
		/** Hide already selected items from the available items list */
		HIDE_ALREADY_SELECTED,
		/** Show already selected items in the grid, pre-selected (visually marked) */
		SHOW_AS_SELECTED
	}

	/** Entity type configuration for the dialog */
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

	private static final long serialVersionUID = 1L;
	// Already selected items configuration
	private final AlreadySelectedMode alreadySelectedMode;
	private final ItemsProvider<EntityClass> alreadySelectedProvider;
	private List<EntityClass> alreadySelectedItems = new ArrayList<>();
	private List<EntityClass> allItems = new ArrayList<>();
	private CButton buttonCancel;
	private CButton buttonReset;
	private CButton buttonSelect;
	private java.lang.reflect.Method cachedGetDescriptionMethod;
	// Cached reflection methods for performance
	private Method cachedGetNameMethod;
	private Method cachedGetStatusMethod;
	private ComboBox<EntityTypeConfig<?>> comboBoxEntityType;
	private ComboBox<String> comboBoxStatusFilter;
	private EntityTypeConfig<?> currentEntityType;
	// Dialog configuration
	private final String dialogTitle;
	private final List<EntityTypeConfig<?>> entityTypes;
	// Dialog components - following typeName convention
	private Grid<EntityClass> gridItems;
	private final ItemsProvider<EntityClass> itemsProvider;
	private Span labelSelectedCount;
	// Configuration
	private final boolean multiSelect;
	private final Consumer<List<EntityClass>> onSelection;
	// Selection state - persists across filtering
	private final Set<EntityClass> selectedItems = new HashSet<>();
	private TextField textFieldDescriptionFilter;
	private TextField textFieldIdFilter;
	private TextField textFieldNameFilter;

	/** Creates an entity selection dialog.
	 * @param title         Dialog title
	 * @param entityTypes   Available entity types for selection
	 * @param itemsProvider Provider for loading items based on entity type
	 * @param onSelection   Callback when selection is confirmed
	 * @param multiSelect   True for multi-select, false for single-select */
	public CDialogEntitySelection(final String title, final List<EntityTypeConfig<?>> entityTypes, final ItemsProvider<EntityClass> itemsProvider,
			final Consumer<List<EntityClass>> onSelection, final boolean multiSelect) {
		this(title, entityTypes, itemsProvider, onSelection, multiSelect, null, AlreadySelectedMode.HIDE_ALREADY_SELECTED);
	}

	/** Creates an entity selection dialog with support for already-selected items.
	 * @param title                   Dialog title
	 * @param entityTypes             Available entity types for selection
	 * @param itemsProvider           Provider for loading items based on entity type
	 * @param onSelection             Callback when selection is confirmed
	 * @param multiSelect             True for multi-select, false for single-select
	 * @param alreadySelectedProvider Provider for already-selected items (can be null). Returns items that are already members of the container.
	 * @param alreadySelectedMode     Mode for handling already-selected items */
	public CDialogEntitySelection(final String title, final List<EntityTypeConfig<?>> entityTypes, final ItemsProvider<EntityClass> itemsProvider,
			final Consumer<List<EntityClass>> onSelection, final boolean multiSelect, final ItemsProvider<EntityClass> alreadySelectedProvider,
			final AlreadySelectedMode alreadySelectedMode) {
		super();
		Check.notBlank(title, "Dialog title cannot be blank");
		Check.notEmpty(entityTypes, "Entity types cannot be empty");
		Check.notNull(itemsProvider, "Items provider cannot be null");
		Check.notNull(onSelection, "Selection callback cannot be null");
		Check.notNull(alreadySelectedMode, "Already selected mode cannot be null");
		this.dialogTitle = title;
		this.entityTypes = entityTypes;
		this.itemsProvider = itemsProvider;
		this.onSelection = onSelection;
		this.multiSelect = multiSelect;
		this.alreadySelectedProvider = alreadySelectedProvider;
		this.alreadySelectedMode = alreadySelectedMode;
		try {
			setupDialog();
			// Override default width from CDialog
			setWidth("900px");
			setHeight("700px");
			setResizable(true);
			// Select first entity type if available
			if (!entityTypes.isEmpty()) {
				comboBoxEntityType.setValue(entityTypes.get(0));
			}
		} catch (final Exception e) {
			LOGGER.error("Error setting up entity selection dialog", e);
			CNotificationService.showException("Error creating dialog", e);
		}
	}

	private void applyFilters() {
		final String idValue = textFieldIdFilter.getValue();
		final String nameValue = textFieldNameFilter.getValue();
		final String descValue = textFieldDescriptionFilter.getValue();
		final String statusValue = comboBoxStatusFilter.getValue();
		final List<EntityClass> filtered = new ArrayList<>();
		for (final EntityClass item : allItems) {
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

	private void configureGridColumns() {
		// Clear existing columns
		gridItems.getColumns().forEach(gridItems::removeColumn);
		if (currentEntityType == null) {
			return;
		}
		// Add selection indicator column for multi-select
		if (multiSelect) {
			gridItems.addComponentColumn(item -> {
				if (selectedItems.contains(item)) {
					return CColorUtils.createStyledIcon("vaadin:check-square-o", "#4CAF50");
				} else {
					return CColorUtils.createStyledIcon("vaadin:thin-square", "#9E9E9E");
				}
			}).setHeader("").setWidth("40px").setFlexGrow(0);
		}
		// ID column
		gridItems.addColumn(item -> item.getId() != null ? item.getId().toString() : "").setHeader("ID").setWidth(CGrid.WIDTH_ID).setFlexGrow(0)
				.setSortable(true).setKey("id");
		// Name column - use cached method
		gridItems.addColumn(this::getEntityName).setHeader("Name").setWidth(CGrid.WIDTH_SHORT_TEXT).setFlexGrow(1).setSortable(true).setKey("name");
		// Description column - use cached method
		gridItems.addColumn(this::getEntityDescription).setHeader("Description").setWidth(CGrid.WIDTH_LONG_TEXT).setFlexGrow(1).setSortable(true)
				.setKey("description");
		// Status column with color support for CProjectItem entities
		gridItems.addComponentColumn(item -> {
			final CGridCell statusCell = new CGridCell();
			statusCell.setShowIcon(true);
			if (item instanceof CProjectItem) {
				final CProjectItem<?> projectItem = (CProjectItem<?>) item;
				if (projectItem.getStatus() != null) {
					statusCell.setStatusValue(projectItem.getStatus());
				} else {
					statusCell.setText("No Status");
				}
			} else {
				// Use cached method
				final Object status = getEntityStatus(item);
				if (status instanceof CEntityDB) {
					statusCell.setStatusValue((CEntityDB<?>) status);
				} else if (status != null) {
					statusCell.setText(status.toString());
				} else {
					statusCell.setText("N/A");
				}
			}
			return statusCell;
		}).setHeader("Status").setWidth(CGrid.WIDTH_REFERENCE).setFlexGrow(0).setSortable(true).setKey("status");
	}

	/** Factory method for cancel button. */
	protected CButton create_buttonCancel() {
		return CButton.createTertiary("Cancel", VaadinIcon.CLOSE.create(), e -> on_buttonCancel_clicked());
	}

	/** Factory method for select button. */
	protected CButton create_buttonSelect() {
		final CButton button = CButton.createPrimary("Select", VaadinIcon.CHECK.create(), e -> on_buttonSelect_clicked());
		button.setEnabled(false);
		return button;
	}

	/** Factory method for grid. */
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	protected void create_gridItems() {
		// Create Grid using Object type with auto-columns disabled, then cast.
		// Type safety is maintained by controlling all items in the grid through itemsProvider.
		final com.vaadin.flow.component.grid.Grid rawGrid = new com.vaadin.flow.component.grid.Grid<>(Object.class, false);
		gridItems = rawGrid;
		gridItems.setSizeFull();
		gridItems.setMinHeight("300px");
		// Apply CGrid-like styling
		gridItems.addThemeVariants(com.vaadin.flow.component.grid.GridVariant.LUMO_NO_BORDER,
				com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES, com.vaadin.flow.component.grid.GridVariant.LUMO_COMPACT);
		// Configure selection mode
		if (multiSelect) {
			gridItems.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.MULTI);
			gridItems.asMultiSelect().addValueChangeListener(e -> on_gridItems_multiSelected(e.getValue()));
		} else {
			gridItems.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
			gridItems.asSingleSelect().addValueChangeListener(e -> on_gridItems_singleSelected(e.getValue()));
		}
		// Add click listener to toggle selection in multi-select mode
		if (multiSelect) {
			gridItems.addItemClickListener(e -> on_gridItems_clicked(e.getItem()));
		}
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
		comboBoxEntityType.addValueChangeListener(e -> on_comboBoxEntityType_changed(e.getValue()));
		layout.add(comboBoxEntityType);
		return layout;
	}

	/** Factory method for search toolbar layout. */
	protected HorizontalLayout create_layoutSearchToolbar() {
		final CHorizontalLayout toolbar = new CHorizontalLayout();
		toolbar.setWidthFull();
		toolbar.setSpacing(true);
		toolbar.setPadding(false);
		toolbar.setAlignItems(FlexComponent.Alignment.END);
		// ID filter
		textFieldIdFilter = new TextField("ID");
		textFieldIdFilter.setPlaceholder("Filter by ID...");
		textFieldIdFilter.setPrefixComponent(VaadinIcon.KEY.create());
		textFieldIdFilter.setClearButtonVisible(true);
		textFieldIdFilter.setValueChangeMode(ValueChangeMode.LAZY);
		textFieldIdFilter.setValueChangeTimeout(300);
		textFieldIdFilter.setWidth("100px");
		textFieldIdFilter.addValueChangeListener(e -> on_textFieldIdFilter_changed());
		// Name filter
		textFieldNameFilter = new TextField("Name");
		textFieldNameFilter.setPlaceholder("Filter by name...");
		textFieldNameFilter.setPrefixComponent(VaadinIcon.SEARCH.create());
		textFieldNameFilter.setClearButtonVisible(true);
		textFieldNameFilter.setValueChangeMode(ValueChangeMode.LAZY);
		textFieldNameFilter.setValueChangeTimeout(300);
		textFieldNameFilter.setWidth("200px");
		textFieldNameFilter.addValueChangeListener(e -> on_textFieldNameFilter_changed());
		// Description filter
		textFieldDescriptionFilter = new TextField("Description");
		textFieldDescriptionFilter.setPlaceholder("Filter by description...");
		textFieldDescriptionFilter.setPrefixComponent(VaadinIcon.FILE_TEXT.create());
		textFieldDescriptionFilter.setClearButtonVisible(true);
		textFieldDescriptionFilter.setValueChangeMode(ValueChangeMode.LAZY);
		textFieldDescriptionFilter.setValueChangeTimeout(300);
		textFieldDescriptionFilter.setWidth("200px");
		textFieldDescriptionFilter.addValueChangeListener(e -> on_textFieldDescriptionFilter_changed());
		// Status filter
		comboBoxStatusFilter = new ComboBox<>("Status");
		comboBoxStatusFilter.setPlaceholder("All statuses");
		comboBoxStatusFilter.setClearButtonVisible(true);
		comboBoxStatusFilter.setWidth("150px");
		comboBoxStatusFilter.addValueChangeListener(e -> on_comboBoxStatusFilter_changed());
		// Clear filters button
		final CButton buttonClearFilters = new CButton(VaadinIcon.CLOSE_CIRCLE.create());
		buttonClearFilters.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		buttonClearFilters.setTooltipText("Clear all filters");
		buttonClearFilters.addClickListener(e -> on_buttonClearFilters_clicked());
		toolbar.add(textFieldIdFilter, textFieldNameFilter, textFieldDescriptionFilter, comboBoxStatusFilter, buttonClearFilters);
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
	public String getDialogTitleString() { return dialogTitle; }

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
	// Event handlers following on_xxx_eventType convention

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

	@Override
	protected Icon getFormIcon() { return VaadinIcon.LIST_SELECT.create(); }

	@Override
	protected String getFormTitleString() { return "Select Items"; }

	/** Returns the currently selected items.
	 * @return List of selected items */
	public List<EntityClass> getSelectedItems() { return new ArrayList<>(selectedItems); }

	/** Returns whether the dialog is configured for multi-select.
	 * @return true if multi-select mode */
	public boolean isMultiSelect() { return multiSelect; }

	/** Handle cancel button click. */
	protected void on_buttonCancel_clicked() {
		close();
	}

	/** Handle clear filters button click. */
	protected void on_buttonClearFilters_clicked() {
		textFieldIdFilter.clear();
		textFieldNameFilter.clear();
		textFieldDescriptionFilter.clear();
		comboBoxStatusFilter.clear();
		applyFilters();
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

	/** Handle select button click. */
	protected void on_buttonSelect_clicked() {
		if (selectedItems.isEmpty()) {
			CNotificationService.showWarning("Please select at least one item");
			return;
		}
		LOGGER.debug("Confirming selection of {} items", selectedItems.size());
		onSelection.accept(new ArrayList<>(selectedItems));
		close();
	}

	/** Handle status filter combobox value change. */
	protected void on_comboBoxStatusFilter_changed() {
		try {
			applyFilters();
		} catch (final Exception e) {
			LOGGER.error("Error applying status filter", e);
			CNotificationService.showException("Error applying filter", e);
		}
	}

	/** Handle description filter text field value change. */
	protected void on_textFieldDescriptionFilter_changed() {
		try {
			applyFilters();
		} catch (final Exception e) {
			LOGGER.error("Error applying description filter", e);
			CNotificationService.showException("Error applying filter", e);
		}
	}

	/** Handle ID filter text field value change. */
	protected void on_textFieldIdFilter_changed() {
		try {
			applyFilters();
		} catch (final Exception e) {
			LOGGER.error("Error applying ID filter", e);
			CNotificationService.showException("Error applying filter", e);
		}
	}

	/** Handle name filter text field value change. */
	protected void on_textFieldNameFilter_changed() {
		try {
			applyFilters();
		} catch (final Exception e) {
			LOGGER.error("Error applying name filter", e);
			CNotificationService.showException("Error applying filter", e);
		}
	}

	/** Handle entity type combobox change. */
	protected void on_comboBoxEntityType_changed(final EntityTypeConfig<?> config) {
		if (config == null) {
			return;
		}
		LOGGER.debug("Entity type changed to: {}", config.getDisplayName());
		currentEntityType = config;
		// Clear selection when entity type changes
		selectedItems.clear();
		// Load already selected items if provider is available
		loadAlreadySelectedItems(config);
		updateSelectionIndicator();
		// Cache reflection methods for the entity type
		cacheReflectionMethods(config.getEntityClass());
		// Configure grid columns for the new entity type
		configureGridColumns();
		// Load items
		try {
			allItems = itemsProvider.getItems(config);
			LOGGER.debug("Loaded {} items for entity type {}", allItems.size(), config.getDisplayName());
			// Handle already selected items based on mode
			processAlreadySelectedItems();
			// Update status filter options
			updateStatusFilterOptions();
			// Apply filters and refresh grid
			applyFilters();
		} catch (final Exception e) {
			LOGGER.error("Error loading items for entity type {}", config.getDisplayName(), e);
			CNotificationService.showException("Error loading items", e);
			allItems = new ArrayList<>();
			gridItems.setItems(allItems);
		}
	}

	/** Handle grid item click (for toggle in multi-select mode). */
	protected void on_gridItems_clicked(final EntityClass item) {
		if (selectedItems.contains(item)) {
			selectedItems.remove(item);
			gridItems.deselect(item);
		} else {
			selectedItems.add(item);
			gridItems.select(item);
		}
		updateSelectionIndicator();
	}

	/** Handle grid multi-select value change. */
	protected void on_gridItems_multiSelected(final Set<EntityClass> value) {
		// Add new selections, but don't remove items that were previously selected
		// This allows selections to persist across filtering
		for (final EntityClass item : value) {
			if (!selectedItems.contains(item)) {
				selectedItems.add(item);
			}
		}
		updateSelectionIndicator();
	}

	/** Handle grid single-select value change. */
	protected void on_gridItems_singleSelected(final EntityClass value) {
		selectedItems.clear();
		if (value != null) {
			selectedItems.add(value);
		}
		updateSelectionIndicator();
	}

	@Override
	protected void setupButtons() {
		buttonSelect = create_buttonSelect();
		buttonCancel = create_buttonCancel();
		buttonLayout.add(buttonSelect, buttonCancel);
	}

	@Override
	protected void setupContent() {
		// Entity type selector
		final HorizontalLayout layoutEntityType = create_layoutEntityTypeSelector();
		mainLayout.add(layoutEntityType);
		// Search toolbar
		final HorizontalLayout layoutSearchToolbar = create_layoutSearchToolbar();
		mainLayout.add(layoutSearchToolbar);
		// Selection indicator and reset
		final HorizontalLayout layoutSelectionIndicator = create_layoutSelectionIndicator();
		mainLayout.add(layoutSelectionIndicator);
		// Grid
		create_gridItems();
		mainLayout.add(gridItems);
		mainLayout.setFlexGrow(1, gridItems);
		// Make the layout fill available space
		mainLayout.setSizeFull();
	}

	private void updateSelectionIndicator() {
		final int count = selectedItems.size();
		labelSelectedCount.setText(count + " selected");
		buttonReset.setEnabled(count > 0);
		buttonSelect.setEnabled(count > 0);
	}

	private void updateStatusFilterOptions() {
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
		comboBoxStatusFilter.setItems(statuses);
	}

	/** Loads already selected items from the provider if available.
	 * @param config The current entity type configuration */
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

	/** Process already selected items based on the configured mode. This method either filters out already selected items from allItems
	 * (HIDE_ALREADY_SELECTED mode) or adds them to selectedItems to show them as pre-selected (SHOW_AS_SELECTED mode). */
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
				for (final EntityClass item : allItems) {
					if ((item.getId() != null) && alreadySelectedIds.contains(item.getId())) {
						selectedItems.add(item);
					}
				}
				LOGGER.debug("Pre-selected {} already selected items", selectedItems.size());
				updateSelectionIndicator();
				break;
			default:
				break;
		}
	}

	/** Returns the already selected mode configured for this dialog.
	 * @return The AlreadySelectedMode */
	public AlreadySelectedMode getAlreadySelectedMode() { return alreadySelectedMode; }

	/** Returns the list of already selected items.
	 * @return List of already selected items (can be empty, never null) */
	public List<EntityClass> getAlreadySelectedItems() { return new ArrayList<>(alreadySelectedItems); }
}
