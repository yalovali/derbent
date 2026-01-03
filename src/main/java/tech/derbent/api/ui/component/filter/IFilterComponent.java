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
	 * Enables automatic value persistence for this filter component.
	 * <p>
	 * When enabled, the filter's selected value is automatically:
	 * <ul>
	 * <li>Saved to session storage whenever the user changes it</li>
	 * <li>Restored from session storage when the component is attached to the UI</li>
	 * <li>Preserved across page refreshes, navigation, and component recreations</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <b>Implementation Pattern:</b><br>
	 * Most filter components should delegate to {@link tech.derbent.api.utils.CValueStorageHelper#valuePersist_enable}
	 * with a converter function that knows how to restore the value from a string.
	 * </p>
	 * <p>
	 * <b>Example Implementation:</b>
	 * <pre>
	 * &#64;Override
	 * public void enableValuePersistence(final String storageId) {
	 *     CValueStorageHelper.valuePersist_enable(
	 *         comboBox, 
	 *         storageId + "_" + FILTER_KEY,
	 *         storedString -&gt; {
	 *             // Convert stored string back to the option type
	 *             return findOptionByString(storedString);
	 *         }
	 *     );
	 * }
	 * </pre>
	 * </p>
	 * <p>
	 * <b>Storage ID Convention:</b><br>
	 * The storage ID should be unique within the filter toolbar. Common pattern:
	 * {@code storageId + "_" + FILTER_KEY}
	 * </p>
	 * 
	 * @param storageId The base storage identifier from the parent toolbar (must not be null or blank)
	 * @see tech.derbent.api.utils.CValueStorageHelper
	 */
	void enableValuePersistence(String storageId);
}
