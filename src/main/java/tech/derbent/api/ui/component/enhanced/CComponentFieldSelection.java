package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.annotations.CDataProviderResolver;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CEntityLabel;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.utils.Check;

public class CComponentFieldSelection<MasterEntity, DetailEntity> extends CHorizontalLayout
		implements HasValue<HasValue.ValueChangeEvent<List<DetailEntity>>, List<DetailEntity>>,
		HasValueAndElement<HasValue.ValueChangeEvent<List<DetailEntity>>, List<DetailEntity>> {

	private static final String DEFAULT_GRID_HEIGHT = "250px";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentFieldSelection.class);
	private static final long serialVersionUID = 1L;
	private CButton addButton;
	private Grid<DetailEntity> availableGrid;
	IContentOwner contentOwner;
	private List<DetailEntity> currentValue = new ArrayList<>();
	CDataProviderResolver dataProviderResolver;
	private CButton downButton;
	EntityFieldInfo fieldInfo;
	private ItemLabelGenerator<DetailEntity> itemLabelGenerator = Object::toString;
	private final List<ValueChangeListener<? super ValueChangeEvent<List<DetailEntity>>>> listeners = new ArrayList<>();
	private final List<DetailEntity> notselectedItems = new ArrayList<>();
	private boolean readOnly = false;
	private CButton removeButton;
	private Grid<DetailEntity> selectedGrid;
	private final List<DetailEntity> selectedItems = new ArrayList<>();
	private final List<DetailEntity> sourceItems = new ArrayList<>();
	private CButton upButton;

	public CComponentFieldSelection() {
		this(null, null, null, "Available Items", "Selected Items");
	}

	public CComponentFieldSelection(final CDataProviderResolver dataProviderResolver, final IContentOwner contentOwner,
			final EntityFieldInfo fieldInfo, final String availableTitle, final String selectedTitle) {
		super();
		Check.notBlank(availableTitle, "Available title cannot be null or blank");
		Check.notBlank(selectedTitle, "Selected title cannot be null or blank");
		this.contentOwner = contentOwner;
		this.fieldInfo = fieldInfo;
		this.dataProviderResolver = dataProviderResolver;
		initializeUI(availableTitle, selectedTitle);
	}

	private void addSelectedItem() {
		LOGGER.debug("Adding selected item from available grid");
		final DetailEntity selected = availableGrid.asSingleSelect().getValue();
		Check.notNull(selected, "No item selected to add");
		final int selectionIndex = notselectedItems.indexOf(selected);
		final DetailEntity currentSelection = selectedGrid.asSingleSelect().getValue();
		if (currentSelection != null) {
			// Insert below the current selection
			final int insertIndex = selectedItems.indexOf(currentSelection) + 1;
			selectedItems.add(insertIndex, selected);
			LOGGER.debug("Inserted item below selection at index: {}", insertIndex);
		} else {
			// Add to end if no selection
			selectedItems.add(selected);
			LOGGER.debug("Added item to end of list");
		}
		populateForm();
		availableGrid.asSingleSelect().setValue(selectionIndex < notselectedItems.size() ? notselectedItems.get(selectionIndex)
				: notselectedItems.isEmpty() ? null : notselectedItems.get(notselectedItems.size() - 1));
		selectedGrid.asSingleSelect().setValue(selected);
	}

	@Override
	public Registration addValueChangeListener(final ValueChangeListener<? super ValueChangeEvent<List<DetailEntity>>> listener) {
		Check.notNull(listener, "ValueChangeListener cannot be null");
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	@Override
	public void clear() {
		try {
			LOGGER.debug("Clearing all selected items");
			selectedItems.clear();
			populateForm();
		} catch (final Exception e) {
			LOGGER.error("Failed to clear selected items:" + e.getMessage());
			throw e;
		}
	}

	/** Configures a single column for the grid with color-aware rendering. If the item is a CEntityNamed, it will be rendered with its color and icon
	 * using CEntityLabel. Otherwise, it falls back to text rendering.
	 * @param grid   The Grid to configure (must not be null)
	 * @param header The column header text
	 * @throws IllegalArgumentException if grid is null */
	private void configureGridColumn(final Grid<DetailEntity> grid, final String header) {
		Check.notNull(grid, "Grid cannot be null");
		final var column = grid.addComponentColumn(item -> {
			try {
				if (item == null) {
					LOGGER.warn("Rendering null item in grid - returning N/A placeholder");
					return new Span("N/A");
				}
				if (item instanceof CEntityNamed) {
					try {
						return new CEntityLabel((CEntityNamed<?>) item);
					} catch (final Exception e) {
						LOGGER.error("Failed to create CEntityLabel for entity: {}", item, e);
						throw new IllegalStateException("Failed to render entity with color: " + item, e);
					}
				} else {
					final String text = itemLabelGenerator != null ? itemLabelGenerator.apply(item) : item.toString();
					return new Span(text);
				}
			} catch (final Exception e) {
				LOGGER.error("Error rendering item in field selection: {}", item, e);
				final String fallbackText = item != null ? item.toString() : "Error";
				return new Span(fallbackText);
			}
		}).setAutoWidth(true).setFlexGrow(1);
		CGrid.styleColumnHeader(column, header);
	}

	/** Creates and configures a grid for field selection with common styling and behavior.
	 * @param header The header text for the grid column
	 * @return Configured Grid instance */
	private Grid<DetailEntity> createAndSetupGrid(final String header) {
		final Grid<DetailEntity> grid = new Grid<>();
		CGrid.setupGrid(grid);
		grid.setHeight(DEFAULT_GRID_HEIGHT);
		configureGridColumn(grid, header);
		return grid;
	}

	/** Fires a value change event to listeners. */
	private void fireValueChangeEvent() {
		LOGGER.debug("Firing value change event - old value size: {}, new value size: {}", currentValue.size(), selectedItems.size());
		final List<DetailEntity> oldValue = currentValue;
		final List<DetailEntity> newValue = getValue();
		currentValue = new ArrayList<>(newValue);
		if (!oldValue.equals(newValue)) {
			LOGGER.info("Value changed from {} to {} selected items - notifying {} listeners", oldValue.size(), newValue.size(), listeners.size());
			final ValueChangeEvent<List<DetailEntity>> event = new ValueChangeEvent<List<DetailEntity>>() {

				private static final long serialVersionUID = 1L;

				@Override
				public HasValue<?, List<DetailEntity>> getHasValue() { return CComponentFieldSelection.this; }

				@Override
				public List<DetailEntity> getOldValue() { return oldValue; }

				@Override
				public List<DetailEntity> getValue() { return newValue; }

				@Override
				public boolean isFromClient() { return true; }
			};
			listeners.forEach(listener -> listener.valueChanged(event));
		} else {
			LOGGER.debug("Value unchanged, not firing event to listeners");
		}
	}

	/** Returns the currently selected items.
	 * @return List of selected items in order (never null) */
	public List<DetailEntity> getSelectedItems() { return new ArrayList<>(selectedItems); }

	// HasValue implementation
	/** Returns the current value as a list of selected items. The order is preserved from the selectedItems list, which is critical for fields with
	 * @OrderColumn annotation.
	 * @return List of selected items in order (never null) */
	@Override
	public List<DetailEntity> getValue() { return new ArrayList<>(selectedItems); }

	private void initializeUI(final String availableTitle, final String selectedTitle) {
		// LOGGER.debug("Initializing field selection UI with titles: '{}' and '{}'", availableTitle, selectedTitle);
		Check.notBlank(availableTitle, "Available title cannot be null or blank");
		Check.notBlank(selectedTitle, "Selected title cannot be null or blank");
		setSpacing(true);
		setWidthFull();
		// Create left and right sections
		final CVerticalLayout leftLayout = new CVerticalLayout(false, false, false);
		final CVerticalLayout rightLayout = new CVerticalLayout(false, false, false);
		// leftLayout.setWidth("50%");
		// rightLayout.setWidth("50%");
		availableGrid = createAndSetupGrid("Available Items");
		selectedGrid = createAndSetupGrid("Selected Items");
		leftLayout.add(availableGrid);
		rightLayout.add(selectedGrid);
		// Control buttons with icons
		addButton = new CButton("Add", VaadinIcon.ARROW_RIGHT.create());
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.setEnabled(false);
		addButton.setTooltipText("Add selected item to the list");
		final CHorizontalLayout controlButtons = new CHorizontalLayout(addButton);
		controlButtons.setSpacing(true);
		leftLayout.add(controlButtons);
		// Order buttons with icons
		upButton = new CButton("Move Up", VaadinIcon.ARROW_UP.create());
		upButton.setEnabled(false);
		upButton.setTooltipText("Move selected item up in the order");
		downButton = new CButton("Move Down", VaadinIcon.ARROW_DOWN.create());
		downButton.setEnabled(false);
		downButton.setTooltipText("Move selected item down in the order");
		removeButton = new CButton("Remove", VaadinIcon.ARROW_LEFT.create());
		removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		removeButton.setEnabled(false);
		removeButton.setTooltipText("Remove selected item from the list");
		final CHorizontalLayout orderButtons = new CHorizontalLayout(removeButton, upButton, downButton);
		orderButtons.setSpacing(true);
		rightLayout.add(orderButtons);
		// Add layouts to main component
		this.add(leftLayout, rightLayout);
		setupEventHandlers();
	}

	@Override
	public boolean isEmpty() { return selectedItems.isEmpty(); }

	@Override
	public boolean isReadOnly() { return readOnly; }

	@Override
	public boolean isRequiredIndicatorVisible() { return false; }

	/** Moves the selected item down in the order. */
	private void moveDown() {
		LOGGER.debug("Moving selected item down in order");
		final DetailEntity selected = selectedGrid.asSingleSelect().getValue();
		if (selected != null) {
			final int index = selectedItems.indexOf(selected);
			if (index < (selectedItems.size() - 1)) {
				selectedItems.remove(index);
				selectedItems.add(index + 1, selected);
				populateForm();
				selectedGrid.asSingleSelect().setValue(selected);
			}
		}
	}

	private void moveUp() {
		LOGGER.debug("Moving selected item up in order");
		final DetailEntity selected = selectedGrid.asSingleSelect().getValue();
		if (selected != null) {
			final int index = selectedItems.indexOf(selected);
			if (index > 0) {
				selectedItems.remove(index);
				selectedItems.add(index - 1, selected);
				populateForm();
				selectedGrid.asSingleSelect().setValue(selected);
			}
		}
	}

	private void populateForm() {
		try {
			Check.notNull(sourceItems, "Source items list cannot be null");
			Check.notNull(selectedItems, "Selected items list cannot be null");
			Check.notNull(notselectedItems, "Not selected items list cannot be null");
			LOGGER.debug("Refreshing available and selected item lists - {} selected, {} total source items", selectedItems.size(),
					sourceItems.size());
			notselectedItems.clear();
			// Use robust comparison: filter out items that are present in selectedItems
			// This uses equals() method which should work correctly for both entities and primitives (like String)
			notselectedItems.addAll(sourceItems);
			notselectedItems.removeIf(nulledItem -> {
				try {
					final String labelA = itemLabelGenerator != null ? itemLabelGenerator.apply(nulledItem) : nulledItem.toString();
					final boolean isSelected = selectedItems.stream().anyMatch(searchItem -> {
						try {
							final String labelB = itemLabelGenerator != null ? itemLabelGenerator.apply(searchItem) : searchItem.toString();
							return labelA.equals(labelB);
						} catch (final Exception compareEx) {
							LOGGER.error("Error comparing items during removal: {} vs {}", nulledItem, searchItem, compareEx);
							return false; // On error, assume not equal
						}
					});
					return isSelected; // Remove if selected
				} catch (final Exception removeEx) {
					LOGGER.error("Error removing nulled item: {}", nulledItem, removeEx);
					return false; // On error, keep the item in available list
				}
			});
			availableGrid.setItems(notselectedItems);
			selectedGrid.setItems(selectedItems);
			availableGrid.asSingleSelect().setValue(notselectedItems.isEmpty() ? null : notselectedItems.get(0));
			selectedGrid.asSingleSelect().setValue(selectedItems.isEmpty() ? null : selectedItems.get(0));
			fireValueChangeEvent();
		} catch (final Exception e) {
			LOGGER.error("Error populating form in field selection component: {}", e.getMessage(), e);
			throw new IllegalStateException("Failed to populate field selection form", e);
		}
	}

	private void removeSelectedItem() {
		LOGGER.debug("Removing selected item from selected grid");
		final DetailEntity selected = selectedGrid.asSingleSelect().getValue();
		Check.notNull(selected, "No item selected to remove");
		final int selectionIndex = selectedItems.indexOf(selected);
		selectedItems.remove(selected);
		populateForm();
		selectedGrid.asSingleSelect().setValue(selectionIndex < selectedItems.size() ? selectedItems.get(selectionIndex)
				: (selectedItems.isEmpty() ? null : selectedItems.get(selectedItems.size() - 1)));
	}

	public void setItemLabelGenerator(final ItemLabelGenerator<DetailEntity> itemLabelGenerator) {
		try {
			this.itemLabelGenerator = itemLabelGenerator != null ? itemLabelGenerator : Object::toString;
			// Refresh grids to use new label generator
			// Note: We need to reconfigure the columns
			availableGrid.getColumns().forEach(availableGrid::removeColumn);
			selectedGrid.getColumns().forEach(selectedGrid::removeColumn);
			configureGridColumn(availableGrid, "Available Items");
			configureGridColumn(selectedGrid, "Selected Items");
			// Refresh data
			populateForm();
		} catch (final Exception e) {
			LOGGER.error("Failed to set item label generator:" + e.getMessage());
			throw e;
		}
	}

	@Override
	public void setReadOnly(final boolean readOnly) {
		LOGGER.debug("Setting read-only mode to: {}", readOnly);
		this.readOnly = readOnly;
		updateButtonStates();
	}

	@Override
	public void setRequiredIndicatorVisible(final boolean requiredIndicatorVisible) {
		// Could be implemented if needed
	}

	public void setSourceItems(final List<DetailEntity> items) {
		try {
			Check.notNull(items, "Source items list cannot be null");
			LOGGER.info("Setting {} source items for selection component", items.size());
			sourceItems.clear();
			sourceItems.addAll(items);
			sourceItems.sort((a, b) -> {
				try {
					final String labelA = itemLabelGenerator != null ? itemLabelGenerator.apply(a) : a.toString();
					final String labelB = itemLabelGenerator != null ? itemLabelGenerator.apply(b) : b.toString();
					return labelA.compareToIgnoreCase(labelB);
				} catch (final Exception e) {
					LOGGER.error("Error comparing items for sorting: {} vs {}", a, b, e);
					return 0; // Treat as equal on error
				}
			});
			populateForm();
		} catch (final Exception e) {
			LOGGER.error("Failed to set source items: {}", e.getMessage(), e);
			throw e;
		}
	}

	/** Sets up double-click support on grids for quick item movement. */
	private void setupDoubleClickSupport() {
		// Add double-click listener to available grid
		availableGrid.addItemDoubleClickListener(event -> {
			try {
				LOGGER.debug("Handling double-click on available grid");
				final DetailEntity item = event.getItem();
				if ((item != null) && !readOnly) {
					availableGrid.asSingleSelect().setValue(item);
					addSelectedItem();
				}
			} catch (final Exception ex) {
				LOGGER.error("Error handling double-click on available grid", ex);
			}
		});
		// Add double-click listener to selected grid
		selectedGrid.addItemDoubleClickListener(event -> {
			try {
				LOGGER.debug("Handling double-click on selected grid");
				final DetailEntity item = event.getItem();
				if ((item != null) && !readOnly) {
					selectedGrid.asSingleSelect().setValue(item);
					removeSelectedItem();
				}
			} catch (final Exception ex) {
				LOGGER.error("Error handling double-click on selected grid", ex);
			}
		});
	}

	/** Sets up event handlers for buttons and grid selections. */
	private void setupEventHandlers() {
		LOGGER.debug("Setting up event handlers for field selection component");
		// Enable/disable buttons based on selection - Use asSingleSelect() for consistent behavior
		availableGrid.asSingleSelect().addValueChangeListener(e -> {
			LOGGER.debug("Available grid selection changed");
			final boolean hasSelection = (e.getValue() != null) && !readOnly;
			addButton.setEnabled(hasSelection);
		});
		selectedGrid.asSingleSelect().addValueChangeListener(e -> {
			LOGGER.debug("Selected grid selection changed");
			final boolean hasSelection = (e.getValue() != null) && !readOnly;
			removeButton.setEnabled(hasSelection);
			upButton.setEnabled(hasSelection);
			downButton.setEnabled(hasSelection);
		});
		// Button actions
		addButton.addClickListener(e -> {
			try {
				addSelectedItem();
			} catch (final Exception ex) {
				LOGGER.error("Error adding item to selected grid", ex);
				throw new IllegalStateException("Failed to add item", ex);
			}
		});
		removeButton.addClickListener(e -> {
			try {
				removeSelectedItem();
			} catch (final Exception ex) {
				LOGGER.error("Error removing item from selected grid", ex);
				throw new IllegalStateException("Failed to remove item", ex);
			}
		});
		upButton.addClickListener(e -> {
			try {
				moveUp();
			} catch (final Exception ex) {
				LOGGER.error("Error moving item up", ex);
				throw new IllegalStateException("Failed to move item up", ex);
			}
		});
		downButton.addClickListener(e -> {
			try {
				moveDown();
			} catch (final Exception ex) {
				LOGGER.error("Error moving item down", ex);
				throw new IllegalStateException("Failed to move item down", ex);
			}
		});
		// Double-click support for quick movement
		setupDoubleClickSupport();
	}

	@Override
	public void setValue(final List<DetailEntity> value) {
		try {
			LOGGER.info("Binder triggered setValue on CComponentFieldSelection - reading {} selected items from entity field",
					value != null ? value.size() : 0);
			selectedItems.clear();
			// Only update source items if dataProviderResolver is available
			if ((dataProviderResolver != null) && (fieldInfo != null)) {
				updateSourceItems();
			}
			if (value != null) {
				// Preserve the order from the entity's list field
				selectedItems.addAll(value);
				LOGGER.debug("Selected items loaded from binder: {}",
						selectedItems.stream().map(Object::toString).collect(java.util.stream.Collectors.joining(", ")));
			}
			populateForm();
		} catch (final Exception e) {
			LOGGER.error("Failed to set value in CComponentFieldSelection: {}", e.getMessage(), e);
		}
	}

	/** Updates button enabled states based on current selections and read-only mode. */
	private void updateButtonStates() {
		LOGGER.debug("Updating button states based on read-only mode and selections");
		if (readOnly) {
			// If read-only, disable all buttons
			addButton.setEnabled(false);
			removeButton.setEnabled(false);
			upButton.setEnabled(false);
			downButton.setEnabled(false);
		} else {
			// Update based on selection state
			addButton.setEnabled(availableGrid.getSelectionModel().getFirstSelectedItem().isPresent());
			removeButton.setEnabled(selectedGrid.getSelectionModel().getFirstSelectedItem().isPresent());
			upButton.setEnabled(selectedGrid.getSelectionModel().getFirstSelectedItem().isPresent());
			downButton.setEnabled(selectedGrid.getSelectionModel().getFirstSelectedItem().isPresent());
		}
	}

	private void updateSourceItems() throws Exception {
		try {
			LOGGER.debug("Updating source items using DataProviderResolver for field: {}", fieldInfo.getFieldName());
			sourceItems.clear();
			Check.notNull(dataProviderResolver, "DataProviderResolver for field " + fieldInfo.getFieldName());
			final List<?> rawList = dataProviderResolver.resolveDataList(contentOwner, fieldInfo);
			Check.notNull(rawList, "Items for field " + fieldInfo.getFieldName() + " of type " + fieldInfo.getJavaType());
			// Set items as list (typed at runtime)
			@SuppressWarnings ("unchecked")
			final List<DetailEntity> items = rawList.stream().map(e -> (DetailEntity) e).collect(Collectors.toList());
			setSourceItems(items);
		} catch (final Exception e) {
			LOGGER.error("Failed to update source items for field {}: {}", fieldInfo.getFieldName(), e.getMessage());
			throw e;
		}
	}
}
