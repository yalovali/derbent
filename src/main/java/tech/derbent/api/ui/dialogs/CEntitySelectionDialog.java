package tech.derbent.api.ui.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CGridCell;
import tech.derbent.api.ui.component.CButton;
import tech.derbent.api.ui.component.CHorizontalLayout;
import tech.derbent.api.ui.component.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;

/**
 * CEntitySelectionDialog - Dialog for selecting entities from a grid with search/filter capabilities.
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
 * </ul>
 *
 * @param <T> The entity type being selected
 */
public class CEntitySelectionDialog<T extends CEntityDB<T>> extends Dialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEntitySelectionDialog.class);
	private static final long serialVersionUID = 1L;

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
		public String toString() { return displayName; }
	}

	/** Callback for getting items based on entity type */
	@FunctionalInterface
	public interface ItemsProvider<T> {

		List<T> getItems(EntityTypeConfig<?> config);
	}

	// Dialog components
	private com.vaadin.flow.component.grid.Grid<T> grid;
	private ComboBox<EntityTypeConfig<?>> entityTypeComboBox;
	private TextField idFilter;
	private TextField nameFilter;
	private TextField descriptionFilter;
	private ComboBox<String> statusFilter;
	private Span selectedCountLabel;
	private Button resetButton;
	private Button selectButton;

	// Configuration
	private final boolean multiSelect;
	private final List<EntityTypeConfig<?>> entityTypes;
	private final Consumer<List<T>> onSelection;
	private final ItemsProvider<T> itemsProvider;

	// Selection state - persists across filtering
	private final Set<T> selectedItems = new HashSet<>();
	private List<T> allItems = new ArrayList<>();
	private EntityTypeConfig<?> currentEntityType;

	/**
	 * Creates an entity selection dialog.
	 *
	 * @param title         Dialog title
	 * @param entityTypes   Available entity types for selection
	 * @param itemsProvider Provider for loading items based on entity type
	 * @param onSelection   Callback when selection is confirmed
	 * @param multiSelect   True for multi-select, false for single-select
	 */
	public CEntitySelectionDialog(final String title, final List<EntityTypeConfig<?>> entityTypes, final ItemsProvider<T> itemsProvider,
			final Consumer<List<T>> onSelection, final boolean multiSelect) {
		super();
		Check.notBlank(title, "Dialog title cannot be blank");
		Check.notEmpty(entityTypes, "Entity types cannot be empty");
		Check.notNull(itemsProvider, "Items provider cannot be null");
		Check.notNull(onSelection, "Selection callback cannot be null");
		this.entityTypes = entityTypes;
		this.itemsProvider = itemsProvider;
		this.onSelection = onSelection;
		this.multiSelect = multiSelect;
		setupDialog(title);
	}

	private void setupDialog(final String title) {
		setHeaderTitle(title);
		setModal(true);
		setCloseOnEsc(true);
		setCloseOnOutsideClick(false);
		setWidth("900px");
		setHeight("700px");
		setResizable(true);
		final VerticalLayout mainLayout = new CVerticalLayout(true, true, false);
		mainLayout.setSizeFull();
		// Entity type selector
		final HorizontalLayout typeLayout = createEntityTypeSelector();
		mainLayout.add(typeLayout);
		// Search toolbar
		final HorizontalLayout searchToolbar = createSearchToolbar();
		mainLayout.add(searchToolbar);
		// Selection indicator and reset
		final HorizontalLayout selectionIndicator = createSelectionIndicator();
		mainLayout.add(selectionIndicator);
		// Grid
		createGrid();
		mainLayout.add(grid);
		mainLayout.setFlexGrow(1, grid);
		add(mainLayout);
		// Footer buttons
		setupFooterButtons();
		// Apply styling
		applyDialogStyling();
		// Select first entity type if available
		if (!entityTypes.isEmpty()) {
			entityTypeComboBox.setValue(entityTypes.get(0));
		}
	}

	private HorizontalLayout createEntityTypeSelector() {
		final HorizontalLayout layout = new CHorizontalLayout();
		layout.setWidthFull();
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		entityTypeComboBox = new ComboBox<>("Entity Type");
		entityTypeComboBox.setItems(entityTypes);
		entityTypeComboBox.setItemLabelGenerator(EntityTypeConfig::getDisplayName);
		entityTypeComboBox.setWidthFull();
		entityTypeComboBox.setRequired(true);
		entityTypeComboBox.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				onEntityTypeChanged(e.getValue());
			}
		});
		layout.add(entityTypeComboBox);
		return layout;
	}

	private HorizontalLayout createSearchToolbar() {
		final HorizontalLayout toolbar = new CHorizontalLayout();
		toolbar.setWidthFull();
		toolbar.setSpacing(true);
		toolbar.setPadding(false);
		toolbar.setAlignItems(FlexComponent.Alignment.END);
		// ID filter
		idFilter = new TextField("ID");
		idFilter.setPlaceholder("Filter by ID...");
		idFilter.setPrefixComponent(VaadinIcon.KEY.create());
		idFilter.setClearButtonVisible(true);
		idFilter.setValueChangeMode(ValueChangeMode.LAZY);
		idFilter.setValueChangeTimeout(300);
		idFilter.setWidth("100px");
		idFilter.addValueChangeListener(e -> applyFilters());
		// Name filter
		nameFilter = new TextField("Name");
		nameFilter.setPlaceholder("Filter by name...");
		nameFilter.setPrefixComponent(VaadinIcon.SEARCH.create());
		nameFilter.setClearButtonVisible(true);
		nameFilter.setValueChangeMode(ValueChangeMode.LAZY);
		nameFilter.setValueChangeTimeout(300);
		nameFilter.setWidth("200px");
		nameFilter.addValueChangeListener(e -> applyFilters());
		// Description filter
		descriptionFilter = new TextField("Description");
		descriptionFilter.setPlaceholder("Filter by description...");
		descriptionFilter.setPrefixComponent(VaadinIcon.FILE_TEXT.create());
		descriptionFilter.setClearButtonVisible(true);
		descriptionFilter.setValueChangeMode(ValueChangeMode.LAZY);
		descriptionFilter.setValueChangeTimeout(300);
		descriptionFilter.setWidth("200px");
		descriptionFilter.addValueChangeListener(e -> applyFilters());
		// Status filter
		statusFilter = new ComboBox<>("Status");
		statusFilter.setPlaceholder("All statuses");
		statusFilter.setClearButtonVisible(true);
		statusFilter.setWidth("150px");
		statusFilter.addValueChangeListener(e -> applyFilters());
		// Clear filters button
		final Button clearFiltersButton = new Button(VaadinIcon.CLOSE_CIRCLE.create());
		clearFiltersButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		clearFiltersButton.setTooltipText("Clear all filters");
		clearFiltersButton.addClickListener(e -> clearFilters());
		toolbar.add(idFilter, nameFilter, descriptionFilter, statusFilter, clearFiltersButton);
		return toolbar;
	}

	private HorizontalLayout createSelectionIndicator() {
		final HorizontalLayout layout = new CHorizontalLayout();
		layout.setWidthFull();
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		// Selected count indicator
		final Icon selectedIcon = VaadinIcon.CHECK_SQUARE.create();
		selectedIcon.setSize("16px");
		selectedIcon.setColor("#1976D2");
		selectedCountLabel = new Span("0 selected");
		selectedCountLabel.getStyle().set("font-weight", "500").set("color", "#1976D2").set("margin-right", "10px");
		// Reset button
		resetButton = new Button("Reset", VaadinIcon.REFRESH.create());
		resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
		resetButton.setTooltipText("Clear all selected items");
		resetButton.addClickListener(e -> resetSelection());
		resetButton.setEnabled(false);
		layout.add(selectedIcon, selectedCountLabel, resetButton);
		return layout;
	}

	@SuppressWarnings ("unchecked")
	private void createGrid() {
		// Create a Grid with Object type and cast to T 
		// This is safe because we control all the items that go into the grid
		grid = (com.vaadin.flow.component.grid.Grid<T>) (Object) new com.vaadin.flow.component.grid.Grid<>(Object.class, false);
		grid.setSizeFull();
		grid.setMinHeight("300px");
		// Apply CGrid-like styling
		grid.addThemeVariants(com.vaadin.flow.component.grid.GridVariant.LUMO_NO_BORDER, com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES,
				com.vaadin.flow.component.grid.GridVariant.LUMO_COMPACT);
		// Configure selection mode
		if (multiSelect) {
			grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.MULTI);
			grid.asMultiSelect().addValueChangeListener(e -> {
				// Add new selections, but don't remove items that were previously selected
				// This allows selections to persist across filtering
				for (final T item : e.getValue()) {
					if (!selectedItems.contains(item)) {
						selectedItems.add(item);
					}
				}
				updateSelectionIndicator();
			});
		} else {
			grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
			grid.asSingleSelect().addValueChangeListener(e -> {
				selectedItems.clear();
				if (e.getValue() != null) {
					selectedItems.add(e.getValue());
				}
				updateSelectionIndicator();
			});
		}
		// Add click listener to toggle selection in multi-select mode
		if (multiSelect) {
			grid.addItemClickListener(e -> {
				final T item = e.getItem();
				if (selectedItems.contains(item)) {
					selectedItems.remove(item);
					grid.deselect(item);
				} else {
					selectedItems.add(item);
					grid.select(item);
				}
				updateSelectionIndicator();
			});
		}
	}

	private void configureGridColumns() {
		// Clear existing columns
		grid.getColumns().forEach(grid::removeColumn);
		if (currentEntityType == null) {
			return;
		}
		// Add selection indicator column for multi-select
		if (multiSelect) {
			grid.addComponentColumn(item -> {
				if (selectedItems.contains(item)) {
					return CColorUtils.createStyledIcon("vaadin:check-square-o", "#4CAF50");
				} else {
					return CColorUtils.createStyledIcon("vaadin:thin-square", "#9E9E9E");
				}
			}).setHeader("").setWidth("40px").setFlexGrow(0);
		}
		// ID column
		grid.addColumn(item -> item.getId() != null ? item.getId().toString() : "").setHeader("ID").setWidth(CGrid.WIDTH_ID).setFlexGrow(0)
				.setSortable(true).setKey("id");
		// Name column - check if entity has getName method
		grid.addColumn(item -> {
			try {
				final java.lang.reflect.Method method = item.getClass().getMethod("getName");
				final Object result = method.invoke(item);
				return result != null ? result.toString() : "";
			} catch (final Exception e) {
				return "";
			}
		}).setHeader("Name").setWidth(CGrid.WIDTH_SHORT_TEXT).setFlexGrow(1).setSortable(true).setKey("name");
		// Description column - check if entity has getDescription method
		grid.addColumn(item -> {
			try {
				final java.lang.reflect.Method method = item.getClass().getMethod("getDescription");
				final Object result = method.invoke(item);
				return result != null ? result.toString() : "";
			} catch (final Exception e) {
				return "";
			}
		}).setHeader("Description").setWidth(CGrid.WIDTH_LONG_TEXT).setFlexGrow(1).setSortable(true).setKey("description");
		// Status column with color support for CProjectItem entities
		grid.addComponentColumn(item -> {
			final CGridCell statusCell = new CGridCell();
			statusCell.setShowIcon(true);
			try {
				if (item instanceof CProjectItem) {
					final CProjectItem<?> projectItem = (CProjectItem<?>) item;
					if (projectItem.getStatus() != null) {
						statusCell.setStatusValue(projectItem.getStatus());
					} else {
						statusCell.setText("No Status");
					}
				} else {
					// Try to get status via reflection
					final java.lang.reflect.Method method = item.getClass().getMethod("getStatus");
					final Object status = method.invoke(item);
					if (status instanceof CEntityDB) {
						statusCell.setStatusValue((CEntityDB<?>) status);
					} else if (status != null) {
						statusCell.setText(status.toString());
					} else {
						statusCell.setText("N/A");
					}
				}
			} catch (final Exception e) {
				statusCell.setText("N/A");
			}
			return statusCell;
		}).setHeader("Status").setWidth(CGrid.WIDTH_REFERENCE).setFlexGrow(0).setSortable(true).setKey("status");
	}

	private void setupFooterButtons() {
		selectButton = CButton.createPrimary("Select", VaadinIcon.CHECK.create(), e -> confirmSelection());
		selectButton.setEnabled(false);
		final CButton cancelButton = CButton.createTertiary("Cancel", VaadinIcon.CLOSE.create(), e -> close());
		final HorizontalLayout buttonLayout = new CHorizontalLayout();
		buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		buttonLayout.add(selectButton, cancelButton);
		getFooter().add(buttonLayout);
	}

	private void applyDialogStyling() {
		getElement().getStyle().set("border", "2px solid #1976D2");
		getElement().getStyle().set("border-radius", "12px");
		getElement().getStyle().set("box-shadow", "0 4px 20px rgba(25, 118, 210, 0.3)");
		getElement().getStyle().set("background", "linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)");
	}

	@SuppressWarnings ("unchecked")
	private void onEntityTypeChanged(final EntityTypeConfig<?> config) {
		LOGGER.debug("Entity type changed to: {}", config.getDisplayName());
		currentEntityType = config;
		// Clear selection when entity type changes
		selectedItems.clear();
		updateSelectionIndicator();
		// Configure grid columns for the new entity type
		configureGridColumns();
		// Load items
		try {
			allItems = itemsProvider.getItems(config);
			LOGGER.debug("Loaded {} items for entity type {}", allItems.size(), config.getDisplayName());
			// Update status filter options
			updateStatusFilterOptions();
			// Apply filters and refresh grid
			applyFilters();
		} catch (final Exception e) {
			LOGGER.error("Error loading items for entity type {}", config.getDisplayName(), e);
			CNotificationService.showException("Error loading items", e);
			allItems = new ArrayList<>();
			grid.setItems(allItems);
		}
	}

	private void updateStatusFilterOptions() {
		final Set<String> statuses = new HashSet<>();
		for (final T item : allItems) {
			try {
				if (item instanceof CProjectItem) {
					final CProjectItem<?> projectItem = (CProjectItem<?>) item;
					if (projectItem.getStatus() != null) {
						statuses.add(projectItem.getStatus().getName());
					}
				} else {
					final java.lang.reflect.Method method = item.getClass().getMethod("getStatus");
					final Object status = method.invoke(item);
					if (status instanceof CEntityDB) {
						final java.lang.reflect.Method nameMethod = status.getClass().getMethod("getName");
						final Object name = nameMethod.invoke(status);
						if (name != null) {
							statuses.add(name.toString());
						}
					}
				}
			} catch (final Exception e) {
				// Ignore - entity may not have status
			}
		}
		statusFilter.setItems(statuses);
	}

	private void applyFilters() {
		final String idValue = idFilter.getValue();
		final String nameValue = nameFilter.getValue();
		final String descValue = descriptionFilter.getValue();
		final String statusValue = statusFilter.getValue();
		final List<T> filtered = new ArrayList<>();
		for (final T item : allItems) {
			boolean matches = true;
			// ID filter
			if ((idValue != null) && !idValue.isBlank()) {
				final String itemId = item.getId() != null ? item.getId().toString() : "";
				if (!itemId.toLowerCase().contains(idValue.toLowerCase())) {
					matches = false;
				}
			}
			// Name filter
			if (matches && (nameValue != null) && !nameValue.isBlank()) {
				try {
					final java.lang.reflect.Method method = item.getClass().getMethod("getName");
					final Object result = method.invoke(item);
					final String name = result != null ? result.toString() : "";
					if (!name.toLowerCase().contains(nameValue.toLowerCase())) {
						matches = false;
					}
				} catch (final Exception e) {
					matches = false;
				}
			}
			// Description filter
			if (matches && (descValue != null) && !descValue.isBlank()) {
				try {
					final java.lang.reflect.Method method = item.getClass().getMethod("getDescription");
					final Object result = method.invoke(item);
					final String desc = result != null ? result.toString() : "";
					if (!desc.toLowerCase().contains(descValue.toLowerCase())) {
						matches = false;
					}
				} catch (final Exception e) {
					matches = false;
				}
			}
			// Status filter
			if (matches && (statusValue != null) && !statusValue.isBlank()) {
				try {
					String statusName = null;
					if (item instanceof CProjectItem) {
						final CProjectItem<?> projectItem = (CProjectItem<?>) item;
						if (projectItem.getStatus() != null) {
							statusName = projectItem.getStatus().getName();
						}
					} else {
						final java.lang.reflect.Method method = item.getClass().getMethod("getStatus");
						final Object status = method.invoke(item);
						if (status instanceof CEntityDB) {
							final java.lang.reflect.Method nameMethod = status.getClass().getMethod("getName");
							final Object name = nameMethod.invoke(status);
							statusName = name != null ? name.toString() : null;
						}
					}
					if ((statusName == null) || !statusName.equals(statusValue)) {
						matches = false;
					}
				} catch (final Exception e) {
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
			for (final T item : filtered) {
				if (selectedItems.contains(item)) {
					grid.select(item);
				}
			}
		}
		LOGGER.debug("Applied filters - showing {} of {} items", filtered.size(), allItems.size());
	}

	private void clearFilters() {
		idFilter.clear();
		nameFilter.clear();
		descriptionFilter.clear();
		statusFilter.clear();
		applyFilters();
	}

	private void resetSelection() {
		selectedItems.clear();
		if (multiSelect) {
			grid.deselectAll();
		} else {
			grid.asSingleSelect().clear();
		}
		updateSelectionIndicator();
		LOGGER.debug("Selection reset");
	}

	private void updateSelectionIndicator() {
		final int count = selectedItems.size();
		selectedCountLabel.setText(count + " selected");
		resetButton.setEnabled(count > 0);
		selectButton.setEnabled(count > 0);
	}

	private void confirmSelection() {
		if (selectedItems.isEmpty()) {
			CNotificationService.showWarning("Please select at least one item");
			return;
		}
		LOGGER.debug("Confirming selection of {} items", selectedItems.size());
		onSelection.accept(new ArrayList<>(selectedItems));
		close();
	}

	/**
	 * Returns the currently selected items.
	 *
	 * @return List of selected items
	 */
	public List<T> getSelectedItems() { return new ArrayList<>(selectedItems); }

	/**
	 * Returns whether the dialog is configured for multi-select.
	 *
	 * @return true if multi-select mode
	 */
	public boolean isMultiSelect() { return multiSelect; }
}
