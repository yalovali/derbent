package tech.derbent.api.ui.component.filter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import tech.derbent.api.utils.CValueStorageHelper;

/**
 * CAbstractFilterComponent - Abstract base class for filter components.
 * <p>
 * Provides common implementation for IFilterComponent interface,
 * reducing boilerplate in concrete filter implementations.
 * </p>
 * 
 * @param <T> The type of value this filter produces
 */
public abstract class CAbstractFilterComponent<T> implements IFilterComponent<T> {

	private final String filterKey;
	private final List<Consumer<T>> changeListeners;
	protected Component uiComponent;
	protected T currentValue;

	/**
	 * Creates a filter component with the given key.
	 * 
	 * @param filterKey The unique key for this filter
	 */
	protected CAbstractFilterComponent(final String filterKey) {
		if (filterKey == null || filterKey.isBlank()) {
			throw new IllegalArgumentException("Filter key cannot be null or blank");
		}
		this.filterKey = filterKey;
		this.changeListeners = new ArrayList<>();
	}

	@Override
	public final String getFilterKey() {
		return filterKey;
	}

	@Override
	public final Component getComponent() {
		if (uiComponent == null) {
			uiComponent = createComponent();
		}
		return uiComponent;
	}

	/**
	 * Creates the UI component for this filter.
	 * Subclasses must implement this to provide the actual UI component.
	 * 
	 * @return The Vaadin component (ComboBox, TextField, etc.)
	 */
	protected abstract Component createComponent();

	@Override
	public T getValue() {
		return currentValue;
	}

	@Override
	public void setValue(final T value) {
		this.currentValue = value;
		updateComponentValue(value);
	}

	/**
	 * Updates the UI component with the new value.
	 * Subclasses must implement this to update their specific component type.
	 * 
	 * @param value The new value
	 */
	protected abstract void updateComponentValue(T value);

	@Override
	public void addChangeListener(final Consumer<T> listener) {
		if (listener != null) {
			changeListeners.add(listener);
		}
	}

	/**
	 * Notifies all change listeners of a value change.
	 * 
	 * @param newValue The new filter value
	 */
	protected void notifyChangeListeners(final T newValue) {
		this.currentValue = newValue;
		for (final Consumer<T> listener : changeListeners) {
			try {
				listener.accept(newValue);
			} catch (final Exception e) {
				// Log but don't fail on listener errors
				System.err.println("Error in filter change listener: " + e.getMessage());
			}
		}
	}

	/**
	 * Enables automatic value persistence for this filter component.
	 * <p>
	 * <b>Default Implementation:</b> Does nothing. Subclasses that want to support value
	 * persistence MUST override this method and delegate to 
	 * {@link CValueStorageHelper#valuePersist_enable}.
	 * </p>
	 * <p>
	 * <b>Why Override:</b> Value persistence allows filter selections to survive:
	 * <ul>
	 * <li>Component refreshes (e.g., when updating available options)</li>
	 * <li>Page refreshes (browser F5)</li>
	 * <li>Navigation away and back to the same view</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <b>Implementation Template:</b>
	 * <pre>
	 * &#64;Override
	 * public void enableValuePersistence(final String storageId) {
	 *     CValueStorageHelper.valuePersist_enable(
	 *         myComboBox,
	 *         storageId + "_" + FILTER_KEY,
	 *         storedValue -&gt; {
	 *             // Convert the stored string back to your option type
	 *             // Return null if the value is no longer valid
	 *             return findOptionByIdentifier(storedValue);
	 *         }
	 *     );
	 * }
	 * </pre>
	 * </p>
	 * <p>
	 * <b>Key Points for Implementers:</b>
	 * <ol>
	 * <li>Use {@code storageId + "_" + FILTER_KEY} as the persistence key</li>
	 * <li>The converter function receives the stored string (from toString())</li>
	 * <li>Return null from converter if the stored value is no longer valid</li>
	 * <li>CValueStorageHelper handles all the complexity (save on change, restore on attach)</li>
	 * </ol>
	 * </p>
	 * 
	 * @param storageId The base storage identifier from the parent toolbar
	 * @see CValueStorageHelper#valuePersist_enable(ComboBox, String, CValueStorageHelper.ValueConverter)
	 */
	@Override
	public void valuePersist_enable(final String storageId) {
		// Default implementation does nothing
		// Subclasses can override to enable persistence for their specific component
	}
}
