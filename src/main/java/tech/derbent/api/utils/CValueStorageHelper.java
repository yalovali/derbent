package tech.derbent.api.utils;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.interfaces.IHasSelectedValueStorage;
import tech.derbent.base.session.service.ISessionService;

/**
 * CValueStorageHelper - Utility for enabling automatic value persistence on UI components.
 * <p>
 * This helper integrates with components that implement IHasSelectedValueStorage to provide
 * transparent, automatic saving and restoring of component values across refreshes and page
 * reloads. Once enabled, the component will automatically:
 * <ul>
 * <li>Save its value whenever it changes</li>
 * <li>Restore its value when attached to the UI</li>
 * <li>Clean up stored values when detached (optional)</li>
 * </ul>
 * </p>
 * <h3>Supported Components:</h3>
 * <ul>
 * <li>ComboBox - Stores selected item (by string representation)</li>
 * <li>TextField - Stores entered text value</li>
 * <li>Any component that implements HasValue and IHasSelectedValueStorage</li>
 * </ul>
 * <h3>Usage Examples:</h3>
 * <pre>
 * // Enable automatic persistence on a ComboBox
 * ComboBox&lt;EntityType&gt; comboBox = new ComboBox&lt;&gt;("Type");
 * CValueStorageHelper.enableAutoPersistence(comboBox, storageId, 
 *     value -&gt; value.getDisplayName(),  // Convert to string
 *     name -&gt; findEntityType(name));    // Convert from string
 * 
 * // Enable automatic persistence on a TextField
 * TextField nameFilter = new TextField("Name");
 * CValueStorageHelper.enableAutoPersistence(nameFilter, "filter_name");
 * </pre>
 * 
 * @see IHasSelectedValueStorage
 * @see ISessionService
 */
public class CValueStorageHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(CValueStorageHelper.class);
	private static final String STORAGE_ENABLED_KEY = CValueStorageHelper.class.getName() + ".enabled";

	/**
	 * Converter interface for transforming values to/from storage format.
	 * 
	 * @param <T> The component value type
	 */
	@FunctionalInterface
	public interface ValueConverter<T> {
		/**
		 * Converts a value from storage string format to the component's type.
		 * 
		 * @param storedValue The stored string value
		 * @return The converted value, or null if conversion fails
		 */
		T fromString(String storedValue);
	}

	/**
	 * Converter interface for transforming values to storage format.
	 * 
	 * @param <T> The component value type
	 */
	@FunctionalInterface
	public interface ValueSerializer<T> {
		/**
		 * Converts a component value to storage string format.
		 * 
		 * @param value The component value
		 * @return The string representation for storage, or null if value cannot be serialized
		 */
		String toString(T value);
	}

	/**
	 * Disables automatic value persistence for a component.
	 * <p>
	 * This method removes the persistence behavior and cleans up the stored value.
	 * </p>
	 * 
	 * @param component The component to disable persistence for
	 */
	public static void disableAutoPersistence(final Component component) {
		Check.notNull(component, "Component cannot be null");
		ComponentUtil.setData(component, STORAGE_ENABLED_KEY, null);
		LOGGER.debug("Disabled auto-persistence for component: {}", component.getId().orElse("unknown"));
	}

	/**
	 * Enables automatic value persistence for a ComboBox.
	 * <p>
	 * This is a convenience method for ComboBox components that use toString() for serialization
	 * and require a custom converter for deserialization.
	 * </p>
	 * 
	 * @param <T>       The type of items in the ComboBox
	 * @param comboBox  The ComboBox to enable persistence for
	 * @param storageId The unique storage identifier
	 * @param converter Converter to transform stored string back to item type
	 */
	public static <T> void enableAutoPersistence(final ComboBox<T> comboBox, final String storageId, final ValueConverter<T> converter) {
		Check.notNull(comboBox, "ComboBox cannot be null");
		Check.notBlank(storageId, "Storage ID cannot be blank");
		Check.notNull(converter, "Converter cannot be null");
		enableAutoPersistence(comboBox, storageId, value -> value != null ? value.toString() : null, converter);
	}

	/**
	 * Enables automatic value persistence for a component with custom serialization.
	 * <p>
	 * This is the most flexible method that allows custom conversion logic for both
	 * saving (to string) and restoring (from string) values.
	 * </p>
	 * 
	 * @param <T>        The type of values in the component
	 * @param component  The component to enable persistence for (must implement HasValue)
	 * @param storageId  The unique storage identifier
	 * @param serializer Function to convert component value to storage string
	 * @param converter  Function to convert storage string back to component value
	 */
	@SuppressWarnings("unchecked")
	public static <T> void enableAutoPersistence(final HasValue<?, T> component, final String storageId, final ValueSerializer<T> serializer,
			final ValueConverter<T> converter) {
		Check.notNull(component, "Component cannot be null");
		Check.notBlank(storageId, "Storage ID cannot be blank");
		Check.notNull(serializer, "Serializer cannot be null");
		Check.notNull(converter, "Converter cannot be null");
		if (!(component instanceof Component)) {
			throw new IllegalArgumentException("Component must extend Vaadin Component class");
		}
		final Component vaadinComponent = (Component) component;
		// Get session service
		final ISessionService sessionService = CSpringContext.getBean(ISessionService.class);
		// Mark component as having auto-persistence enabled
		ComponentUtil.setData(vaadinComponent, STORAGE_ENABLED_KEY, true);
		// Add value change listener to save on change
		component.addValueChangeListener(event -> {
			if (!event.isFromClient()) {
				// Skip programmatic changes to avoid saving during restore
				return;
			}
			try {
				final T value = event.getValue();
				if (value != null) {
					final String serialized = serializer.toString(value);
					if (serialized != null) {
						sessionService.setSessionValue(storageId, serialized);
						LOGGER.debug("Auto-saved value for storage ID: {}", storageId);
					}
				} else {
					sessionService.removeSessionValue(storageId);
					LOGGER.debug("Cleared stored value for storage ID: {}", storageId);
				}
			} catch (final Exception e) {
				LOGGER.error("Error auto-saving value for storage ID: {}", storageId, e);
			}
		});
		// Add attach listener to restore value when component is added to UI
		vaadinComponent.addAttachListener(event -> restoreValue(component, storageId, converter, sessionService));
		// Add detach listener to clean up (optional - could be removed if values should persist)
		vaadinComponent.addDetachListener(event -> {
			LOGGER.debug("Component detached, stored value remains for storage ID: {}", storageId);
		});
		// If component is already attached, restore value immediately
		if (vaadinComponent.isAttached()) {
			restoreValue(component, storageId, converter, sessionService);
		}
		LOGGER.debug("Enabled auto-persistence for component with storage ID: {}", storageId);
	}

	/**
	 * Enables automatic value persistence for a TextField.
	 * <p>
	 * This is a convenience method for TextField components that use simple string values.
	 * </p>
	 * 
	 * @param textField The TextField to enable persistence for
	 * @param storageId The unique storage identifier
	 */
	public static void enableAutoPersistence(final TextField textField, final String storageId) {
		Check.notNull(textField, "TextField cannot be null");
		Check.notBlank(storageId, "Storage ID cannot be blank");
		enableAutoPersistence(textField, storageId, value -> value, value -> value);
	}

	/**
	 * Checks if a component has auto-persistence enabled.
	 * 
	 * @param component The component to check
	 * @return true if auto-persistence is enabled, false otherwise
	 */
	public static boolean isAutoPersistenceEnabled(final Component component) {
		Check.notNull(component, "Component cannot be null");
		final Object enabled = ComponentUtil.getData(component, STORAGE_ENABLED_KEY);
		return Boolean.TRUE.equals(enabled);
	}

	/**
	 * Restores a component's value from storage.
	 * <p>
	 * This method sets the value without triggering user-facing side effects by checking
	 * if the value change event is from the client. This prevents cascading updates like
	 * SQL queries and form population during automatic restoration.
	 * </p>
	 * <p>
	 * If no stored value exists but the component has a current value, that value is
	 * saved as the initial default to ensure persistence works on subsequent refreshes.
	 * </p>
	 * 
	 * @param <T>       The type of values in the component
	 * @param component The component to restore value for
	 * @param storageId The storage identifier
	 * @param converter Function to convert storage string back to component value
	 * @param sessionService The session service for retrieving stored values
	 */
	private static <T> void restoreValue(final HasValue<?, T> component, final String storageId, final ValueConverter<T> converter,
			final ISessionService sessionService) {
		try {
			final Optional<String> storedValue = sessionService.getSessionValue(storageId);
			if (storedValue.isPresent()) {
				final T value = converter.fromString(storedValue.get());
				if (value != null) {
					// Set value programmatically - this will NOT trigger isFromClient() listeners
					// Components should check event.isFromClient() to distinguish user actions from programmatic updates
					component.setValue(value);
					LOGGER.debug("Auto-restored value for storage ID: {}", storageId);
				} else {
					LOGGER.debug("Could not convert stored value for storage ID: {}", storageId);
				}
			} else {
				// No stored value exists - check if component has a current value and save it as initial default
				final T currentValue = component.getValue();
				if (currentValue != null) {
					final String serialized = currentValue.toString();
					if (serialized != null && !serialized.isBlank()) {
						sessionService.setSessionValue(storageId, serialized);
						LOGGER.debug("Saved initial value as default for storage ID: {}", storageId);
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error auto-restoring value for storage ID: {}", storageId, e);
		}
	}
}
