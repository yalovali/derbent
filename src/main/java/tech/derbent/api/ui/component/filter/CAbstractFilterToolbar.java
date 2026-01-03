package tech.derbent.api.ui.component.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.interfaces.IHasSelectedValueStorage;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.utils.Check;

/**
 * CAbstractFilterToolbar - Universal abstract base class for filtering toolbars.
 * <p>
 * Provides a composable, type-safe filtering framework that can be used throughout
 * the application for:
 * <ul>
 * <li>Kanban board filtering (sprint, entity type, responsible user)</li>
 * <li>Grid filtering (search, status, date range)</li>
 * <li>Master-detail filtering</li>
 * <li>Asset and budget filtering</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Design Principles:</b>
 * <ul>
 * <li><b>Composition over Inheritance</b> - Build filters from composable filter components</li>
 * <li><b>Type-safe Criteria</b> - Generic FilterCriteria system</li>
 * <li><b>Dynamic Discovery</b> - Auto-detect available options from data</li>
 * <li><b>Declarative Configuration</b> - Simple builder pattern</li>
 * <li><b>Minimal Complexity</b> - Clear, simple abstractions</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * CUniversalFilterToolbar toolbar = new CUniversalFilterToolbar()
 *     .addFilter(new CSprintFilter(sprintService))
 *     .addFilter(new CEntityTypeFilter())
 *     .addFilter(new CResponsibleUserFilter())
 *     .onFilterChange(criteria -> refreshBoard(criteria));
 * </pre>
 * </p>
 * 
 * @param <T> The entity type being filtered
 */
public abstract class CAbstractFilterToolbar<T> extends CHorizontalLayout implements IHasSelectedValueStorage {

	private static final Logger LOGGER = LoggerFactory.getLogger(CAbstractFilterToolbar.class);
	private static final long serialVersionUID = 1L;

	/** Holds all active filter components. */
	private final List<IFilterComponent<?>> filterComponents;

	/** Holds filter change listeners. */
	private final List<Consumer<FilterCriteria<T>>> filterListeners;

	/** Current filter criteria state. */
	private final FilterCriteria<T> currentCriteria;

	/** Optional clear button. */
	private Button clearButton;

	/** Flag to track if clear button should be shown. */
	private boolean showClearButton = true;

	/**
	 * Creates the abstract filter toolbar.
	 */
	protected CAbstractFilterToolbar() {
		super();
		filterComponents = new ArrayList<>();
		filterListeners = new ArrayList<>();
		currentCriteria = new FilterCriteria<>();
		initializeLayout();
	}

	/**
	 * Initializes the toolbar layout.
	 */
	private void initializeLayout() {
		setSpacing(true);
		setPadding(false);
		setAlignItems(Alignment.CENTER);
		setWidthFull();
		addClassName("filter-toolbar");
	}

	/**
	 * Adds a filter component to the toolbar.
	 * 
	 * @param filterComponent The filter component to add
	 * @return This toolbar for method chaining
	 */
	@SuppressWarnings("unchecked")
	public <F extends IFilterComponent<?>> CAbstractFilterToolbar<T> addFilterComponent(final F filterComponent) {
		Check.notNull(filterComponent, "Filter component cannot be null");
		filterComponents.add(filterComponent);

		// Add the UI component
		final Component component = filterComponent.getComponent();
		add(component);

		// Register listener for filter changes
		filterComponent.addChangeListener(value -> {
			currentCriteria.setValue(filterComponent.getFilterKey(), value);
			notifyFilterListeners();
		});

		LOGGER.debug("Added filter component: {}", filterComponent.getFilterKey());
		return this;
	}

	/**
	 * Adds a filter change listener.
	 * 
	 * @param listener The listener to notify on filter changes
	 * @return This toolbar for method chaining
	 */
	public CAbstractFilterToolbar<T> addFilterChangeListener(final Consumer<FilterCriteria<T>> listener) {
		Check.notNull(listener, "Filter listener cannot be null");
		filterListeners.add(listener);
		return this;
	}

	/**
	 * Sets whether the clear button should be shown.
	 * 
	 * @param show True to show clear button, false to hide
	 * @return This toolbar for method chaining
	 */
	public CAbstractFilterToolbar<T> setShowClearButton(final boolean show) {
		this.showClearButton = show;
		return this;
	}

	/**
	 * Builds the clear button after all filter components are added.
	 * This should be called by concrete implementations after adding all filters.
	 */
	protected void buildClearButton() {
		if (!showClearButton) {
			return;
		}
		clearButton = new Button("Clear", VaadinIcon.CLOSE_SMALL.create());
		clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
		clearButton.addClickListener(event -> clearFilters());
		clearButton.setTooltipText("Clear all filters");
		add(clearButton);
	}

	/**
	 * Clears all filters and resets to defaults.
	 */
	public void clearFilters() {
		LOGGER.debug("Clearing all filters");
		for (final IFilterComponent<?> component : filterComponents) {
			component.clearFilter();
		}
		currentCriteria.clear();
		notifyFilterListeners();
	}

	/**
	 * Gets the current filter criteria.
	 * 
	 * @return Current filter criteria
	 */
	public FilterCriteria<T> getCurrentCriteria() {
		return currentCriteria;
	}

	/**
	 * Notifies all filter change listeners.
	 */
	protected void notifyFilterListeners() {
		LOGGER.debug("Notifying {} filter listeners", filterListeners.size());
		for (final Consumer<FilterCriteria<T>> listener : filterListeners) {
			try {
				listener.accept(currentCriteria);
			} catch (final Exception e) {
				LOGGER.error("Error notifying filter listener", e);
			}
		}
	}

	/**
	 * Gets all filter components.
	 * 
	 * @return List of filter components
	 */
	protected List<IFilterComponent<?>> getFilterComponents() {
		return filterComponents;
	}

	@Override
	public String getStorageId() {
		final String componentId = getId().orElse(null);
		if (componentId == null || componentId.isBlank()) {
			throw new IllegalStateException("Component ID must be set for value persistence. "
					+ "Call setId(\"uniqueId\") before enabling persistence.");
		}
		return "filterToolbar_" + componentId;
	}

	/**
	 * Enables automatic value persistence for all filter components.
	 * <p>
	 * This method should be called by the parent component to enable automatic
	 * saving and restoring of filter selections across refreshes.
	 * </p>
	 * <p>
	 * <strong>IMPORTANT</strong>: Before calling this method, the component ID
	 * must be set using {@code setId("uniqueId")}.
	 * </p>
	 */
	public void valuePersist_enable() {
		// Enable persistence for all filter components
		final String storageId = getStorageId();
		for (final IFilterComponent<?> component : filterComponents) {
			component.enableValuePersistence(storageId);
		}
		LOGGER.debug("Value persistence enabled for filter toolbar with storage ID: {}", storageId);
	}

	@Override
	public void restoreCurrentValue() {
		// Restoration is handled automatically by individual filter components
	}

	@Override
	public void saveCurrentValue() {
		// Saving is handled automatically by individual filter components
	}

	/**
	 * FilterCriteria - Type-safe holder for filter values.
	 * <p>
	 * Uses a Map-based approach to allow flexible, dynamic filter criteria
	 * while maintaining type safety through the generic parameter.
	 * </p>
	 * 
	 * @param <T> The entity type being filtered
	 */
	public static class FilterCriteria<T> {

		private final Map<String, Object> filters;

		public FilterCriteria() {
			this.filters = new HashMap<>();
		}

		/**
		 * Sets a filter value.
		 * 
		 * @param key Filter key
		 * @param value Filter value
		 */
		public void setValue(final String key, final Object value) {
			Objects.requireNonNull(key, "Filter key cannot be null");
			filters.put(key, value);
		}

		/**
		 * Gets a filter value.
		 * 
		 * @param key Filter key
		 * @return Filter value or null if not set
		 */
		@SuppressWarnings("unchecked")
		public <V> V getValue(final String key) {
			return (V) filters.get(key);
		}

		/**
		 * Gets a filter value with a default.
		 * 
		 * @param key Filter key
		 * @param defaultValue Default value if not set
		 * @return Filter value or default
		 */
		@SuppressWarnings("unchecked")
		public <V> V getValue(final String key, final V defaultValue) {
			return (V) filters.getOrDefault(key, defaultValue);
		}

		/**
		 * Checks if a filter is set.
		 * 
		 * @param key Filter key
		 * @return True if filter has a non-null value
		 */
		public boolean hasFilter(final String key) {
			return filters.containsKey(key) && filters.get(key) != null;
		}

		/**
		 * Checks if any filters are set.
		 * 
		 * @return True if at least one filter is set
		 */
		public boolean hasAnyFilter() {
			return filters.values().stream().anyMatch(Objects::nonNull);
		}

		/**
		 * Clears all filter values.
		 */
		public void clear() {
			filters.clear();
		}

		/**
		 * Gets all filter keys.
		 * 
		 * @return Set of filter keys
		 */
		public java.util.Set<String> getFilterKeys() {
			return filters.keySet();
		}
	}
}
