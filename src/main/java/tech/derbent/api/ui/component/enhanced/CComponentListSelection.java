package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.annotations.CDataProviderResolver;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.ui.component.basic.CEntityLabel;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;

/** Generic list selection component for selecting items from a source list with colorful icons and checkmarks. This component displays items in a
 * single grid with a checkbox-style selection (showing a tick for selected items). Unlike CComponentFieldSelection, this component does NOT support
 * ordering and provides a simpler click-to-select/deselect interface.
 * <p>
 * Features:
 * <ul>
 * <li>Color-aware rendering for CEntityNamed entities (displays with colors and icons)</li>
 * <li>Text rendering for non-entity types (strings, numbers, etc.)</li>
 * <li>Click to toggle selection (selected items shown with tick icon)</li>
 * <li>Full Vaadin binder integration with List support</li>
 * <li>Read-only mode support</li>
 * <li>Uses CGrid for better selection handling and consistency</li>
 * </ul>
 * <p>
 * Usage Pattern:
 * <ol>
 * <li>Call setSourceItems(allAvailableItems) to provide the complete list of items that can be selected</li>
 * <li>Binder will call setValue(entity.getFieldValue()) to set currently selected items from the entity</li>
 * <li>Component displays all items with visual indication of selection</li>
 * </ol>
 * @param <MasterEntity> The master entity type (e.g., CWorkflowStatusRelation)
 * @param <DetailEntity> The detail entity type to select (e.g., CUserProjectRole) */
public class CComponentListSelection<MasterEntity, DetailEntity> extends CVerticalLayout
		implements HasValue<HasValue.ValueChangeEvent<List<DetailEntity>>, List<DetailEntity>>,
		HasValueAndElement<HasValue.ValueChangeEvent<List<DetailEntity>>, List<DetailEntity>> {

	private static final String DEFAULT_GRID_HEIGHT = "300px";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListSelection.class);
	private static final long serialVersionUID = 1L;
	IContentOwner contentOwner;
	private List<DetailEntity> currentValue = new ArrayList<>();
	CDataProviderResolver dataProviderResolver;
	EntityFieldInfo fieldInfo;
	private CGrid<DetailEntity> grid;
	private final List<ValueChangeListener<? super ValueChangeEvent<List<DetailEntity>>>> listeners = new ArrayList<>();
	private boolean readOnly = false;
	private final List<DetailEntity> selectedItems = new ArrayList<>();
	private final List<DetailEntity> sourceItems = new ArrayList<>();

	/** Creates a new list selection component with default title. */
	public CComponentListSelection() {
		this(null, null, null, "Items", Object.class);
	}

	/** Creates a new list selection component with custom title.
	 * @param dataProviderResolver the data provider resolver
	 * @param contentOwner         the content owner
	 * @param fieldInfo            the field information
	 * @param title                Title for the items grid (must not be null or blank)
	 * @param class1
	 * @throws IllegalArgumentException if title is null or blank */
	public CComponentListSelection(CDataProviderResolver dataProviderResolver, IContentOwner contentOwner, EntityFieldInfo fieldInfo, String title,
			Class<?> class1) {
		super(false, false, false);
		Check.notBlank(title, "Title cannot be null or blank");
		this.contentOwner = contentOwner;
		this.fieldInfo = fieldInfo;
		this.dataProviderResolver = dataProviderResolver;
		initializeUI(title, class1);
	}

	/** Adds a value change listener.
	 * @param listener The listener to add (must not be null)
	 * @return Registration for removing the listener
	 * @throws IllegalArgumentException if listener is null */
	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super ValueChangeEvent<List<DetailEntity>>> listener) {
		Check.notNull(listener, "ValueChangeListener cannot be null");
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	/** Clears all selected items.
	 * @throws IllegalStateException if refresh fails */
	@Override
	public void clear() {
		try {
			LOGGER.debug("Clearing all selected items");
			selectedItems.clear();
			refreshGrid();
		} catch (final Exception e) {
			LOGGER.error("Failed to clear selected items:" + e.getMessage());
			throw e;
		}
	}

	/** Configures columns for the grid following standard pattern with color-aware rendering and selection indicator. If the item is a CEntityNamed,
	 * it will be rendered with its color and icon using CEntityLabel. Otherwise, it falls back to text rendering. A separate column shows a checkmark
	 * for selected items.
	 * @param grid1  The Grid to configure (must not be null)
	 * @param header The header text for the entity column
	 * @throws IllegalArgumentException if grid is null */
	protected void configureGrid(final Grid<DetailEntity> grid1, final String header) {
		Check.notNull(grid1, "Grid cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		LOGGER.debug("Configuring grid columns with header: {}", header);
		// Selection indicator column (checkmark for selected items)
		grid1.addComponentColumn(item -> {
			final String width = "20px";
			Component checkmark;
			if (selectedItems.contains(item)) {
				checkmark = CColorUtils.createStyledIcon("vaadin:check-square-o", "#7CAF50");
			} else {
				checkmark = CColorUtils.createStyledIcon("vaadin:thin-square", "#1CFFa0");
			}
			checkmark.getStyle().set("width", width).set("display", "block").setMargin("0 auto").setPadding("0");
			return checkmark;
		}).setHeader("").setWidth("30px").setFlexGrow(0).setPartNameGenerator( event -> "check-column-cell");
		// Item display column (with color and icon for CEntityNamed)
		final var column = grid1.addComponentColumn(item -> {
			try {
				return new CEntityLabel((CEntityNamed<?>) item);
			} catch (final Exception e) {
				LOGGER.error("Error creating entity label: {}", e.getMessage());
			}
			return new Span("N/A");
		}).setAutoWidth(true).setFlexGrow(1);
		CGrid.styleColumnHeader(column, header);
		grid1.addClassName("first-column-checkbox-grid");
	}

	/** Creates and configures a grid for list selection with common styling and behavior following standard pattern.
	 * @param header The header text for the grid column
	 * @param class1 The entity class
	 * @return Configured Grid instance */
	private CGrid<DetailEntity> createAndSetupGrid(final String header, final Class<?> class1) {
		final CGrid<DetailEntity> grid1 = new CGrid<DetailEntity>(class1);
		CGrid.setupGrid(grid1);
		grid1.setHeight(DEFAULT_GRID_HEIGHT);
		configureGrid(grid1, header);
		return grid1;
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
				public HasValue<?, List<DetailEntity>> getHasValue() { return CComponentListSelection.this; }

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
	 * @return List of selected items (never null) */
	public List<DetailEntity> getSelectedItems() { return new ArrayList<>(selectedItems); }

	// HasValue implementation
	/** Returns the current value as a list of selected items.
	 * @return List of selected items (never null) */
	@Override
	public List<DetailEntity> getValue() { return new ArrayList<>(selectedItems); }

	private void initializeUI(String title, Class<?> class1) {
		LOGGER.debug("Initializing list selection UI with title: '{}'", title);
		Check.notBlank(title, "Title cannot be null or blank");
		setSpacing(true);
		setWidthFull();
		grid = createAndSetupGrid(title, class1);
		this.add(grid);
		setupEventHandlers();
	}

	@Override
	public boolean isEmpty() { return selectedItems.isEmpty(); }

	@Override
	public boolean isReadOnly() { return readOnly; }

	@Override
	public boolean isRequiredIndicatorVisible() { return false; }

	/** Refreshes the grid display after selection changes. */
	private void refreshGrid() {
		try {
			Check.notNull(sourceItems, "Source items list cannot be null");
			Check.notNull(selectedItems, "Selected items list cannot be null");
			LOGGER.debug("Refreshing grid - {} selected, {} total source items", selectedItems.size(), sourceItems.size());
			grid.getDataProvider().refreshAll();
			fireValueChangeEvent();
		} catch (final Exception e) {
			LOGGER.error("Error refreshing grid in list selection component: {}", e.getMessage(), e);
			throw new IllegalStateException("Failed to refresh list selection grid", e);
		}
	}

	@SuppressWarnings ("unused")
	public void setItemLabelGenerator(final ItemLabelGenerator<DetailEntity> itemLabelGenerator) {
		try {
			grid.getColumns().forEach(grid::removeColumn);
			configureGrid(grid, "Items");
			refreshGrid();
		} catch (final Exception e) {
			LOGGER.error("Failed to set item label generator: {}", e.getMessage());
			throw e;
		}
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		LOGGER.debug("Setting read-only mode to: {}", readOnly);
		this.readOnly = readOnly;
		grid.setEnabled(!readOnly);
	}

	@Override
	public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
		// Could be implemented if needed
	}

	public void setSourceItems(List<DetailEntity> items) {
		try {
			Check.notNull(items, "Source items list cannot be null");
			LOGGER.info("Setting {} source items for list selection component", items.size());
			sourceItems.clear();
			sourceItems.addAll(items);
			sourceItems.sort((a, b) -> {
				try {
					final String labelA = a.toString();
					final String labelB = b.toString();
					return labelA.compareToIgnoreCase(labelB);
				} catch (final Exception e) {
					LOGGER.error("Error comparing items for sorting: {} vs {}", a, b, e);
					return 0; // Treat as equal on error
				}
			});
			grid.setItems(sourceItems);
			refreshGrid();
		} catch (final Exception e) {
			LOGGER.error("Failed to set source items: {}", e.getMessage(), e);
			throw e;
		}
	}

	/** Sets up event handlers for grid selections. */
	private void setupEventHandlers() {
		LOGGER.debug("Setting up event handlers for list selection component");
		// Click to toggle selection
		grid.addItemClickListener(event -> {
			try {
				if (!readOnly) {
					final DetailEntity item = event.getItem();
					if (selectedItems.contains(item)) {
						selectedItems.remove(item);
						LOGGER.debug("Deselected item: {}", item);
					} else {
						selectedItems.add(item);
						LOGGER.debug("Selected item: {}", item);
					}
					refreshGrid();
				}
			} catch (final Exception ex) {
				LOGGER.error("Error handling click on grid item", ex);
			}
		});
	}

	@Override
	public void setValue(List<DetailEntity> value) {
		try {
			Check.notNull(fieldInfo, "Field info cannot be null before setting value");
			Check.notNull(dataProviderResolver, "Data provider resolver cannot be null before setting value");
			LOGGER.info("Binder triggered setValue on CComponentListSelection - reading {} selected items from entity field",
					value != null ? value.size() : 0);
			selectedItems.clear();
			// Only update source items if dataProviderResolver is available
			if (dataProviderResolver != null && fieldInfo != null) {
				updateSourceItems();
			}
			if (value != null) {
				selectedItems.addAll(value);
				LOGGER.debug("Selected items loaded from binder: {}", selectedItems.stream().map(Object::toString).collect(Collectors.joining(", ")));
			}
			refreshGrid();
		} catch (final Exception e) {
			LOGGER.error("Failed to set value in CComponentListSelection: {}", e.getMessage(), e);
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
