package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;

/**
 * CComponentGridSearchToolbar - A reusable search toolbar component for entity grids.
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
public class CComponentGridSearchToolbar extends CHorizontalLayout {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentGridSearchToolbar.class);
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_DEBOUNCE_DELAY_MS = 300;

	/**
	 * Filter criteria holder for the toolbar.
	 */
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
			return ((idFilter != null) && !idFilter.isBlank()) || ((nameFilter != null) && !nameFilter.isBlank())
					|| ((descriptionFilter != null) && !descriptionFilter.isBlank()) || ((statusFilter != null) && !statusFilter.isBlank());
		}

		public void setDescriptionFilter(final String descriptionFilter) { this.descriptionFilter = descriptionFilter != null ? descriptionFilter : ""; }

		public void setIdFilter(final String idFilter) { this.idFilter = idFilter != null ? idFilter : ""; }

		public void setNameFilter(final String nameFilter) { this.nameFilter = nameFilter != null ? nameFilter : ""; }

		public void setStatusFilter(final String statusFilter) { this.statusFilter = statusFilter != null ? statusFilter : ""; }
	}

	/**
	 * Configuration for which fields to show in the toolbar.
	 */
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

	private Button clearButton;
	private final ToolbarConfig config;
	private final FilterCriteria currentFilters = new FilterCriteria();
	private TextField descriptionFilter;
	private final List<Consumer<FilterCriteria>> filterListeners = new ArrayList<>();
	private TextField idFilter;
	private TextField nameFilter;
	private ComboBox<String> statusFilter;

	/**
	 * Creates a search toolbar with default configuration (all filters visible).
	 */
	public CComponentGridSearchToolbar() {
		this(new ToolbarConfig().showAll());
	}

	/**
	 * Creates a search toolbar with custom configuration.
	 *
	 * @param config Configuration for visible fields
	 */
	public CComponentGridSearchToolbar(final ToolbarConfig config) {
		super();
		Check.notNull(config, "Toolbar config cannot be null");
		this.config = config;
		initializeUI();
	}

	/**
	 * Adds a listener that is notified when filter criteria change.
	 *
	 * @param listener The listener to add
	 */
	public void addFilterChangeListener(final Consumer<FilterCriteria> listener) {
		Check.notNull(listener, "Filter listener cannot be null");
		filterListeners.add(listener);
	}

	/**
	 * Clears all filter fields.
	 */
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

	private TextField createTextField(final String label, final String placeholder, final VaadinIcon icon, final String width,
			final Consumer<String> onChange) {
		final TextField field = new TextField(label);
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

	/**
	 * Gets the current filter criteria.
	 *
	 * @return The current filter criteria
	 */
	public FilterCriteria getCurrentFilters() { return currentFilters; }

	/**
	 * Gets the description filter text field.
	 *
	 * @return The description filter field, or null if not shown
	 */
	public TextField getDescriptionFilter() { return descriptionFilter; }

	/**
	 * Gets the ID filter text field.
	 *
	 * @return The ID filter field, or null if not shown
	 */
	public TextField getIdFilter() { return idFilter; }

	/**
	 * Gets the name filter text field.
	 *
	 * @return The name filter field, or null if not shown
	 */
	public TextField getNameFilter() { return nameFilter; }

	/**
	 * Gets the status filter combo box.
	 *
	 * @return The status filter combo box, or null if not shown
	 */
	public ComboBox<String> getStatusFilter() { return statusFilter; }

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
			statusFilter = new ComboBox<>("Status");
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
			clearButton = new Button(VaadinIcon.CLOSE_CIRCLE.create());
			clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
			clearButton.setTooltipText("Clear all filters");
			clearButton.addClickListener(e -> clearFilters());
			add(clearButton);
		}
		// Apply styling
		addClassName("grid-search-toolbar");
		CAuxillaries.setId(this);
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

	/**
	 * Removes a filter change listener.
	 *
	 * @param listener The listener to remove
	 */
	public void removeFilterChangeListener(final Consumer<FilterCriteria> listener) {
		filterListeners.remove(listener);
	}

	/**
	 * Sets the available status options for the status filter.
	 *
	 * @param statuses Set of status names to show
	 */
	public void setStatusOptions(final Set<String> statuses) {
		if (statusFilter != null) {
			Check.notNull(statuses, "Status options cannot be null");
			statusFilter.setItems(statuses);
			LOGGER.debug("Set {} status options", statuses.size());
		}
	}
}
