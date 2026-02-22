package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CComboBox;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.constants.CUIConstants;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;

/** Multi-column list selection component for string-based datasets with record return support.
 * <p>
 * Rows are represented as records ({@link CMultiColumnStringRow}) that can contain any number of string columns plus optional icon information. One
 * column can be marked as the return column via {@link #setReturnedColumnId(String)} to extract lightweight result values.
 * </p>
 */
public class CComponentMultiColumnListSelection extends CVerticalLayout implements
		HasValue<HasValue.ValueChangeEvent<List<CComponentMultiColumnListSelection.CMultiColumnStringRow>>,
				List<CComponentMultiColumnListSelection.CMultiColumnStringRow>>,
		HasValueAndElement<HasValue.ValueChangeEvent<List<CComponentMultiColumnListSelection.CMultiColumnStringRow>>,
				List<CComponentMultiColumnListSelection.CMultiColumnStringRow>> {

	public record CColumnDefinition(String id, String header) {

		public CColumnDefinition {
			Check.notBlank(id, "Column id cannot be blank");
			Check.notBlank(header, "Column header cannot be blank");
		}
	}

	public record CMultiColumnStringRow(String icon, String iconColor, Map<String, String> columnValues) {

		public CMultiColumnStringRow {
			Check.notNull(columnValues, "Column values cannot be null");
			columnValues = Map.copyOf(columnValues);
		}

		public String getValue(final String columnId) {
			return columnValues.getOrDefault(columnId, "");
		}
	}

	private static final String DEFAULT_GRID_HEIGHT = "300px";
	private static final String DEFAULT_VALUE_COLUMN_ID = "value";
	private static final String FILTER_ALL = "All";
	private static final String FILTER_SELECTED = "Selected";
	private static final String FILTER_UNSELECTED = "Unselected";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentMultiColumnListSelection.class);
	private static final long serialVersionUID = 1L;
	private final List<CColumnDefinition> columns = new ArrayList<>();
	private List<CMultiColumnStringRow> currentValue = new ArrayList<>();
	private CGrid<CMultiColumnStringRow> grid;
	private final List<ValueChangeListener<? super ValueChangeEvent<List<CMultiColumnStringRow>>>> listeners = new ArrayList<>();
	private boolean readOnly = false;
	private String returnedColumnId;
	private CTextField searchField;
	private CButton selectAllButton;
	private final List<CMultiColumnStringRow> selectedItems = new ArrayList<>();
	private CComboBox<String> selectionFilterCombo;
	private Span selectionSummary;
	private CButton selectNoneButton;
	private final List<CMultiColumnStringRow> sourceItems = new ArrayList<>();

	public CComponentMultiColumnListSelection() {
		this("Items");
	}

	public CComponentMultiColumnListSelection(final String title) {
		super(false, false, false);
		Check.notBlank(title, "Title cannot be null or blank");
		columns.add(new CColumnDefinition(DEFAULT_VALUE_COLUMN_ID, title));
		returnedColumnId = DEFAULT_VALUE_COLUMN_ID;
		initializeUI();
	}

	@Override
	public Registration addValueChangeListener(final ValueChangeListener<? super ValueChangeEvent<List<CMultiColumnStringRow>>> listener) {
		Check.notNull(listener, "ValueChangeListener cannot be null");
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	private void applyFilterAndRefreshView() {
		grid.setItems(getFilteredItems());
		updateSelectionSummary();
		grid.getDataProvider().refreshAll();
	}

	@Override
	public void clear() {
		selectedItems.clear();
		refreshGrid();
	}

	private void configureGridColumns() {
		grid.getColumns().forEach(grid::removeColumn);
		grid.addComponentColumn(item -> {
			final Component checkmark = selectedItems.contains(item) ? CColorUtils.createStyledIcon("vaadin:check-square-o", "#7CAF50")
					: CColorUtils.createStyledIcon("vaadin:thin-square", "#1CFFa0");
			checkmark.getStyle().set("width", "20px").set("display", "block").setMargin("0 auto").setPadding("0");
			return checkmark;
		}).setHeader("").setWidth("30px").setFlexGrow(0).setPartNameGenerator(event -> "check-column-cell");
		grid.addComponentColumn(this::createIconCell).setHeader("").setWidth("46px").setFlexGrow(0);
		// Visible column order follows 'columns' list order from setColumns(...).
		columns.forEach(column -> {
			final var gridColumn = grid.addColumn(item -> item.getValue(column.id())).setAutoWidth(true).setFlexGrow(1).setSortable(true);
			CGrid.styleColumnHeader(gridColumn, column.header());
		});
		grid.addClassName("first-column-checkbox-grid");
	}

	private Component createIconCell(final CMultiColumnStringRow row) {
		if (row.icon() == null || row.icon().isBlank()) {
			return new Span("");
		}
		final String color = row.iconColor() == null || row.iconColor().isBlank() ? "#1C88FF" : row.iconColor();
		try {
			return CColorUtils.createStyledIcon(row.icon(), color);
		} catch (final Exception e) {
			LOGGER.debug("Failed to render icon '{}' using fallback. reason={}", row.icon(), e.getMessage());
			return CColorUtils.createStyledIcon("vaadin:question-circle-o", color);
		}
	}

	private void createToolbar() {
		searchField = CTextField.createSearch("");
		searchField.setPlaceholder("Search...");
		searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
		searchField.setClearButtonVisible(true);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.setWidth("260px");
		searchField.addValueChangeListener(event -> applyFilterAndRefreshView());
		selectionFilterCombo = new CComboBox<>();
		selectionFilterCombo.setItems(FILTER_ALL, FILTER_SELECTED, FILTER_UNSELECTED);
		selectionFilterCombo.setValue(FILTER_ALL);
		selectionFilterCombo.setWidth("110px");
		selectionFilterCombo.setAllowCustomValue(false);
		selectionFilterCombo.addValueChangeListener(event -> applyFilterAndRefreshView());
		selectAllButton = CButton.createTertiary("All", VaadinIcon.CHECK.create(), event -> onSelectAllClicked());
		selectAllButton.setWidth("80px");
		selectNoneButton = CButton.createTertiary("None", VaadinIcon.CLOSE_SMALL.create(), event -> onSelectNoneClicked());
		selectNoneButton.setWidth("80px");
		selectionSummary = new Span();
		selectionSummary.getStyle().set("color", "var(--lumo-secondary-text-color)");
		selectionSummary.getStyle().set("margin-left", "auto");
		final CHorizontalLayout toolbar =
				new CHorizontalLayout(searchField, selectionFilterCombo, selectAllButton, selectNoneButton, selectionSummary);
		toolbar.setSpacing(CUIConstants.GAP_EXTRA_TINY);
		toolbar.setWidthFull();
		toolbar.setAlignItems(Alignment.CENTER);
		add(toolbar);
		updateSelectionSummary();
	}

	private void fireValueChangeEvent() {
		final List<CMultiColumnStringRow> oldValue = currentValue;
		final List<CMultiColumnStringRow> newValue = getValue();
		currentValue = new ArrayList<>(newValue);
		if (oldValue.equals(newValue)) {
			return;
		}
		final ValueChangeEvent<List<CMultiColumnStringRow>> event = new ValueChangeEvent<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public HasValue<?, List<CMultiColumnStringRow>> getHasValue() { return CComponentMultiColumnListSelection.this; }

			@Override
			public List<CMultiColumnStringRow> getOldValue() { return oldValue; }

			@Override
			public List<CMultiColumnStringRow> getValue() { return newValue; }

			@Override
			public boolean isFromClient() { return true; }
		};
		listeners.forEach(listener -> listener.valueChanged(event));
	}

	private List<CMultiColumnStringRow> getFilteredItems() {
		final String mode = getSelectionFilterMode();
		final String query = searchField != null && searchField.getValue() != null ? searchField.getValue().trim().toLowerCase() : "";
		return sourceItems.stream().filter(item -> {
			if (FILTER_SELECTED.equals(mode) && !selectedItems.contains(item)) {
				return false;
			}
			if (FILTER_UNSELECTED.equals(mode) && selectedItems.contains(item)) {
				return false;
			}
			if (!query.isEmpty()) {
				return getSearchableText(item).contains(query);
			}
			return true;
		}).collect(Collectors.toList());
	}

	public List<String> getReturnedValues() {
		if (returnedColumnId == null || returnedColumnId.isBlank()) {
			return List.of();
		}
		return selectedItems.stream().map(item -> item.getValue(returnedColumnId)).filter(value -> value != null && !value.isBlank())
				.collect(Collectors.toList());
	}

	private String getSearchableText(final CMultiColumnStringRow item) {
		return columns.stream().map(column -> item.getValue(column.id())).filter(Objects::nonNull).map(String::toLowerCase)
				.collect(Collectors.joining(" "));
	}

	private String getSelectionFilterMode() {
		final String mode = selectionFilterCombo != null ? selectionFilterCombo.getValue() : FILTER_ALL;
		return mode != null ? mode : FILTER_ALL;
	}

	@Override
	public List<CMultiColumnStringRow> getValue() { return new ArrayList<>(selectedItems); }

	private void initializeUI() {
		setSpacing(false);
		setWidthFull();
		createToolbar();
		grid = new CGrid<>(CMultiColumnStringRow.class);
		CGrid.setupGrid(grid);
		grid.setHeight(DEFAULT_GRID_HEIGHT);
		configureGridColumns();
		add(grid);
		setupEventHandlers();
	}

	@Override
	public boolean isEmpty() { return selectedItems.isEmpty(); }

	@Override
	public boolean isReadOnly() { return readOnly; }

	@Override
	public boolean isRequiredIndicatorVisible() { return false; }

	private void onSelectAllClicked() {
		if (readOnly) {
			return;
		}
		getFilteredItems().stream().filter(item -> !selectedItems.contains(item)).forEach(selectedItems::add);
		refreshGrid();
	}

	private void onSelectNoneClicked() {
		if (readOnly) {
			return;
		}
		selectedItems.removeAll(getFilteredItems());
		refreshGrid();
	}

	private void refreshGrid() {
		applyFilterAndRefreshView();
		fireValueChangeEvent();
	}

	public void setColumns(final List<CColumnDefinition> newColumns) {
		Check.notEmpty(newColumns, "Columns list cannot be empty");
		final long distinctColumnCount = newColumns.stream().map(CColumnDefinition::id).distinct().count();
		Check.isTrue(distinctColumnCount == newColumns.size(), "Duplicate column ids are not allowed");
		columns.clear();
		// Persist explicit column order from caller so rendering stays predictable.
		columns.addAll(newColumns);
		if (returnedColumnId != null && columns.stream().noneMatch(column -> column.id().equals(returnedColumnId))) {
			returnedColumnId = columns.get(0).id();
		}
		configureGridColumns();
		refreshGrid();
	}

	@Override
	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
		grid.setEnabled(!readOnly);
		selectAllButton.setEnabled(!readOnly);
		selectNoneButton.setEnabled(!readOnly);
	}

	@Override
	public void setRequiredIndicatorVisible(final boolean requiredIndicatorVisible) {
		// Intentionally not used for this component.
	}

	public void setReturnedColumnId(final String returnedColumnId) {
		Check.notBlank(returnedColumnId, "Returned column id cannot be blank");
		Check.isTrue(columns.stream().anyMatch(column -> column.id().equals(returnedColumnId)),
				"Returned column id must match one of defined columns: " + returnedColumnId);
		this.returnedColumnId = returnedColumnId;
		updateSelectionSummary();
	}

	public void setSourceItems(final List<CMultiColumnStringRow> items) {
		Check.notNull(items, "Source items list cannot be null");
		sourceItems.clear();
		sourceItems.addAll(items);
		applyFilterAndRefreshView();
	}

	private void setupEventHandlers() {
		grid.addItemClickListener(event -> {
			if (readOnly) {
				return;
			}
			final CMultiColumnStringRow item = event.getItem();
			if (selectedItems.contains(item)) {
				selectedItems.remove(item);
			} else {
				selectedItems.add(item);
			}
			refreshGrid();
		});
	}

	@Override
	public void setValue(final List<CMultiColumnStringRow> value) {
		selectedItems.clear();
		if (value != null) {
			selectedItems.addAll(value);
		}
		refreshGrid();
	}

	private void updateSelectionSummary() {
		if (selectionSummary == null) {
			return;
		}
		final String base = selectedItems.size() + " / " + sourceItems.size();
		if (returnedColumnId == null || returnedColumnId.isBlank()) {
			selectionSummary.setText(base);
			return;
		}
		selectionSummary.setText(base + " | " + returnedColumnId + ": " + getReturnedValues().size());
	}
}
