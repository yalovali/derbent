package tech.derbent.api.ui.component.filter;

import java.util.function.Consumer;
import com.vaadin.flow.component.Component;

/**
 * IFilterComponent - Interface for composable filter components.
 * <p>
 * Each filter component represents a single filtering criterion (e.g., entity type, sprint, responsible user).
 * Components can be composed together in a filter toolbar to create complex filtering UIs.
 * </p>
 * 
 * @param <T> The type of value this filter produces
 */
public interface IFilterComponent<T> {

	/**
	 * Gets the UI component to display.
	 * 
	 * @return The Vaadin component (ComboBox, TextField, etc.)
	 */
	Component getComponent();

	/**
	 * Gets the unique key for this filter.
	 * Used to store filter values in FilterCriteria map.
	 * 
	 * @return The filter key (e.g., "entityType", "sprint", "responsibleUser")
	 */
	String getFilterKey();

	/**
	 * Gets the current filter value.
	 * 
	 * @return The current filter value or null if not set
	 */
	T getValue();

	/**
	 * Sets the filter value.
	 * 
	 * @param value The value to set
	 */
	void setValue(T value);

	/**
	 * Clears the filter to its default state.
	 */
	void clearFilter();

	/**
	 * Adds a listener that is notified when the filter value changes.
	 * 
	 * @param listener The change listener
	 */
	void addChangeListener(Consumer<T> listener);

	/**
	 * Enables value persistence for this filter component.
	 * 
	 * @param storageId The storage ID for persistence
	 */
	void enableValuePersistence(String storageId);
}
