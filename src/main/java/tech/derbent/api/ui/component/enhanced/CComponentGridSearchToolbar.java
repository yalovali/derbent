package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.interfaces.IHasSelectedValueStorage;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CComboBox;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.utils.CValueStorageHelper;
import tech.derbent.api.utils.Check;

/** CComponentGridSearchToolbar - A reusable search toolbar component for entity grids.
 * <p>
 * Provides filter fields for common entity properties:
 * <ul>
 * <li>ID - Filter by entity ID</li>
 * <li>Name - Filter by entity name</li>
 * <li>Description - Filter by entity description</li>
 * <li>Status - Filter by entity status (dropdown)</li>
 * </ul>
 * <p>
 * Features:
 * <ul>
 * <li>Debounced text input to avoid excessive filtering</li>
 * <li>Clear all filters button</li>
 * <li>Configurable visible fields</li>
 * <li>Callback-based filter notification</li>
 * </ul>
 */
public class CComponentGridSearchToolbar extends CHorizontalLayout implements IHasSelectedValueStorage {

	/** Filter criteria holder for the toolbar. */
	public static class FilterCriteria {

		private String descriptionFilter = "";
		private String idFilter = "";
		private String nameFilter = "";
		private String statusFilter = "";

		public String getDescriptionFilter() { return descriptionFilter; }

		public String getIdFilter() { return idFilter; }

		public String getNameFilter() { return nameFilter; }

		public String getStatusFilter() { return statusFilter; }

		public boolean hasAnyFilter() {
			return idFilter != null && !idFilter.isBlank() || nameFilter != null && !nameFilter.isBlank()
					|| descriptionFilter != null && !descriptionFilter.isBlank() || statusFilter != null && !statusFilter.isBlank();
		}

		public void setDescriptionFilter(final String descriptionFilter) {
			this.descriptionFilter = descriptionFilter != null ? descriptionFilter : "";
		}

		public void setIdFilter(final String idFilter) { this.idFilter = idFilter != null ? idFilter : ""; }

		public void setNameFilter(final String nameFilter) { this.nameFilter = nameFilter != null ? nameFilter : ""; }

		public void setStatusFilter(final String statusFilter) { this.statusFilter = statusFilter != null ? statusFilter : ""; }
	}

	/** Configuration for which fields to show in the toolbar. */
	public static class ToolbarConfig {

		private boolean showClearButton = true;
		private boolean showDescriptionFilter = true;
		private boolean showIdFilter = true;
		private boolean showNameFilter = true;
		private boolean showStatusFilter = true;

		public ToolbarConfig hideAll() {
			showIdFilter = false;
			showNameFilter = false;
			showDescriptionFilter = false;
			showStatusFilter = false;
			showClearButton = false;
			return this;
		}

		public boolean isShowClearButton() { return showClearButton; }

		public boolean isShowDescriptionFilter() { return showDescriptionFilter; }

		public boolean isShowIdFilter() { return showIdFilter; }

		public boolean isShowNameFilter() { return showNameFilter; }

		public boolean isShowStatusFilter() { return showStatusFilter; }

		public ToolbarConfig setClearButton(final boolean show) {
			showClearButton = show;
			return this;
		}

		public ToolbarConfig setDescriptionFilter(final boolean show) {
			showDescriptionFilter = show;
			return this;
		}

		public ToolbarConfig setIdFilter(final boolean show) {
			showIdFilter = show;
			return this;
		}

		public ToolbarConfig setNameFilter(final boolean show) {
			showNameFilter = show;
			return this;
		}

		public ToolbarConfig setStatusFilter(final boolean show) {
			showStatusFilter = show;
			return this;
		}

		public ToolbarConfig showAll() {
			showIdFilter = true;
			showNameFilter = true;
			showDescriptionFilter = true;
			showStatusFilter = true;
			showClearButton = true;
			return this;
		}
	}

	private static final int DEFAULT_DEBOUNCE_DELAY_MS = 300;
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentGridSearchToolbar.class);
	private static final long serialVersionUID = 1L;
	private CButton clearButton;
	private final ToolbarConfig config;
	private final FilterCriteria currentFilters = new FilterCriteria();
	private CTextField descriptionFilter;
	private final List<Consumer<FilterCriteria>> filterListeners = new ArrayList<>();
	private CTextField idFilter;
	private CTextField nameFilter;
	private CComboBox<String> statusFilter;

	/** Creates a search toolbar with default configuration (all filters visible).
	 * <p>
	 * <strong>IMPORTANT</strong>: If you plan to use {@link #valuePersist_enable()}, you MUST call {@code setId("context_gridSearchToolbar")} after
	 * creating this component and before enabling persistence.
	 * </p>
	 */
	public CComponentGridSearchToolbar() {
		this(new ToolbarConfig().showAll());
	}

	/** Creates a search toolbar with custom configuration.
	 * <p>
	 * <strong>IMPORTANT</strong>: If you plan to use {@link #valuePersist_enable()}, you MUST call {@code setId("context_gridSearchToolbar")} after
	 * creating this component and before enabling persistence.
	 * </p>
	 * @param config Configuration for visible fields */
	public CComponentGridSearchToolbar(final ToolbarConfig config) {
		super();
		Check.notNull(config, "Toolbar config cannot be null");
		this.config = config;
		initializeUI();
	}

	/** Adds a listener that is notified when filter criteria change.
	 * @param listener The listener to add */
	public void addFilterChangeListener(final Consumer<FilterCriteria> listener) {
		Check.notNull(listener, "Filter listener cannot be null");
		filterListeners.add(listener);
	}

	/** Clears all filter fields. */
	public void clearFilters() {
		LOGGER.debug("Clearing all filters");
		if (idFilter != null) {
			idFilter.clear();
		}
		if (nameFilter != null) {
			nameFilter.clear();
		}
		if (descriptionFilter != null) {
			descriptionFilter.clear();
		}
		if (statusFilter != null) {
			statusFilter.clear();
		}
		// Note: onChange callbacks will fire and notify listeners
	}

	private CTextField createTextField(final String label, final String placeholder, final VaadinIcon icon, final String width,
			final Consumer<String> onChange) {
		final CTextField field = new CTextField(label);
		field.setPlaceholder(placeholder);
		if (icon != null) {
			field.setPrefixComponent(icon.create());
		}
		field.setClearButtonVisible(true);
		field.setValueChangeMode(ValueChangeMode.LAZY);
		field.setValueChangeTimeout(DEFAULT_DEBOUNCE_DELAY_MS);
		field.setWidth(width);
		field.addValueChangeListener(e -> {
			onChange.accept(e.getValue());
			notifyListeners();
		});
		return field;
	}

	/** Gets the current filter criteria.
	 * @return The current filter criteria */
	public FilterCriteria getCurrentFilters() { return currentFilters; }

	/** Gets the description filter text field.
	 * @return The description filter field, or null if not shown */
	public TextField getDescriptionFilter() { return descriptionFilter; }

	/** Gets the ID filter text field.
	 * @return The ID filter field, or null if not shown */
	public TextField getIdFilter() { return idFilter; }

	/** Gets the name filter text field.
	 * @return The name filter field, or null if not shown */
	public TextField getNameFilter() { return nameFilter; }

	/** Gets the status filter combo box.
	 * @return The status filter combo box, or null if not shown */
	public ComboBox<String> getStatusFilter() { return statusFilter; }

	@Override
	public String getValuePersistId() {
		// CRITICAL: Component ID must be set explicitly for value persistence to work
		// If ID is not set, persistence will fail across component recreations
		final String componentId = getId().orElse(null);
		if (componentId == null || componentId.isBlank()) {
			throw new IllegalStateException("Component ID must be set explicitly for value persistence. "
					+ "Call setId(\"uniqueId\") in the constructor or use CAuxillaries.setId(this) " + "to enable automatic value persistence for "
					+ getClass().getSimpleName());
		}
		return "gridSearchToolbar_" + componentId;
	}

	
	private void initializeUI() {
		setSpacing(true);
		setPadding(false);
		setAlignItems(Alignment.END);
		setWidthFull();
		// ID filter
		if (config.isShowIdFilter()) {
			idFilter = createTextField("ID", "Filter by ID...", VaadinIcon.KEY, "100px", value -> currentFilters.setIdFilter(value));
			add(idFilter);
		}
		// Name filter
		if (config.isShowNameFilter()) {
			nameFilter = createTextField("Name", "Filter by name...", VaadinIcon.SEARCH, "180px", value -> currentFilters.setNameFilter(value));
			add(nameFilter);
		}
		// Description filter
		if (config.isShowDescriptionFilter()) {
			descriptionFilter = createTextField("Description", "Filter by description...", VaadinIcon.FILE_TEXT, "180px",
					value -> currentFilters.setDescriptionFilter(value));
			add(descriptionFilter);
		}
		// Status filter
		if (config.isShowStatusFilter()) {
			statusFilter = new CComboBox<>("Status");
			statusFilter.setPlaceholder("All statuses");
			statusFilter.setClearButtonVisible(true);
			statusFilter.setWidth("150px");
			statusFilter.addValueChangeListener(e -> {
				currentFilters.setStatusFilter(e.getValue());
				notifyListeners();
			});
			add(statusFilter);
		}
		// Clear button
		if (config.isShowClearButton()) {
			clearButton = CButton.createTertiary("", VaadinIcon.CLOSE_CIRCLE.create(), event -> clearFilters());
			clearButton.setTooltipText("Clear all filters");
			add(clearButton);
		}
		// Apply styling
		addClassName("grid-search-toolbar");
		// NOTE: Component ID is NOT set here - parent must call setId() if persistence is needed
		LOGGER.debug("CComponentGridSearchToolbar initialized with config - ID: {}, Name: {}, Desc: {}, Status: {}", config.isShowIdFilter(),
				config.isShowNameFilter(), config.isShowDescriptionFilter(), config.isShowStatusFilter());
	}

	private void notifyListeners() {
		LOGGER.debug("Notifying {} filter listeners", filterListeners.size());
		for (final Consumer<FilterCriteria> listener : filterListeners) {
			try {
				listener.accept(currentFilters);
			} catch (final Exception e) {
				LOGGER.error("Error notifying filter listener", e);
			}
		}
	}
	// ==================== IHasSelectedValueStorage Implementation ====================

	/** Removes a filter change listener.
	 * @param listener The listener to remove */
	public void removeFilterChangeListener(final Consumer<FilterCriteria> listener) {
		filterListeners.remove(listener);
	}

	@Override
	public void restoreCurrentValue() {
		// Restoration is handled automatically by CValueStorageHelper when components attach
		// This method is here for interface compliance
	}

	@Override
	public void saveCurrentValue() {
		// Saving is handled automatically by CValueStorageHelper on value changes
		// This method is here for interface compliance
	}

	public void setDescriptionFilterLabel(final String label) {
		if (descriptionFilter != null && label != null) {
			descriptionFilter.setLabel(label);
		}
	}

	public void setDescriptionFilterPlaceholder(final String placeholder) {
		if (descriptionFilter != null && placeholder != null) {
			descriptionFilter.setPlaceholder(placeholder);
		}
	}

	public void setIdFilterLabel(final String label) {
		if (idFilter != null && label != null) {
			idFilter.setLabel(label);
		}
	}

	public void setIdFilterPlaceholder(final String placeholder) {
		if (idFilter != null && placeholder != null) {
			idFilter.setPlaceholder(placeholder);
		}
	}

	public void setNameFilterLabel(final String label) {
		if (nameFilter != null && label != null) {
			nameFilter.setLabel(label);
		}
	}

	public void setNameFilterPlaceholder(final String placeholder) {
		if (nameFilter != null && placeholder != null) {
			nameFilter.setPlaceholder(placeholder);
		}
	}

	public void setStatusFilterLabel(final String label) {
		if (statusFilter != null && label != null) {
			statusFilter.setLabel(label);
		}
	}

	public void setStatusFilterPlaceholder(final String placeholder) {
		if (statusFilter != null && placeholder != null) {
			statusFilter.setPlaceholder(placeholder);
		}
	}

	/** Sets the available status options for the status filter.
	 * @param statuses Set of status names to show */
	public void setStatusOptions(final Set<String> statuses) {
		if (statusFilter != null) {
			Check.notNull(statuses, "Status options cannot be null");
			statusFilter.setItems(statuses);
			LOGGER.debug("Set {} status options", statuses.size());
		}
	}

	/** Enables automatic value persistence for all filter fields.
	 * <p>
	 * <strong>IMPORTANT</strong>: Before calling this method, the parent component MUST set an explicit, stable ID using
	 * {@code setId("contextSpecificId")}. Failure to do so will result in an {@link IllegalStateException} being thrown.
	 * </p>
	 * <p>
	 * This method should be called by the parent component to enable automatic saving and restoring of filter values across refreshes. Once enabled:
	 * <ul>
	 * <li>Filter values are saved on every change</li>
	 * <li>Filter values are restored when component is attached to UI</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Example usage in parent page:
	 *
	 * <pre>
	 * gridSearchToolbar = new CComponentGridSearchToolbar();
	 * gridSearchToolbar.setId("activities_gridSearchToolbar"); // REQUIRED
	 * gridSearchToolbar.enableValuePersistence();
	 * </pre>
	 * </p>
	 * @throws IllegalStateException if component ID is not set before calling this method
	 * @see #getValuePersistId() */
	public void valuePersist_enable() {
		// Validate ID is set before enabling persistence (fail-fast)
		final String componentId = getId().orElse(null);
		if (componentId == null || componentId.isBlank()) {
			throw new IllegalStateException("Component ID must be set before enabling value persistence. "
					+ "Call setId(\"contextSpecificId\") before calling enableValuePersistence(). "
					+ "Example: gridSearchToolbar.setId(\"activities_gridSearchToolbar\");");
		}
		LOGGER.debug("Enabling value persistence for grid search toolbar with storage ID: {}", getValuePersistId());
		// Enable persistence for ID filter
		if (idFilter != null) {
			CValueStorageHelper.valuePersist_enable(idFilter, getValuePersistId() + "_id");
		}
		// Enable persistence for Name filter
		if (nameFilter != null) {
			CValueStorageHelper.valuePersist_enable(nameFilter, getValuePersistId() + "_name");
		}
		// Enable persistence for Description filter
		if (descriptionFilter != null) {
			CValueStorageHelper.valuePersist_enable(descriptionFilter, getValuePersistId() + "_description");
		}
		// Enable persistence for Status filter
		if (statusFilter != null) {
			CValueStorageHelper.valuePersist_enable(statusFilter, getValuePersistId() + "_status", value -> value, value -> value);
		}
		LOGGER.debug("Value persistence enabled for grid search toolbar with storage ID: {}", getValuePersistId());
	}
}
