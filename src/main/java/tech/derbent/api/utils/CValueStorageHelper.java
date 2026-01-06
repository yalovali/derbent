package tech.derbent.api.utils;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.base.session.service.ISessionService;

/** CValueStorageHelper - Utility for enabling automatic value persistence on UI components.
 * <p>
 * This helper provides transparent, automatic saving and restoring of component values across refreshes and page reloads. Once enabled, the component
 * will automatically:
 * <ul>
 * <li>Save its value whenever the user changes it (not on programmatic changes)</li>
 * <li>Restore its value when attached to the UI</li>
 * <li>Handle conversion between component values and storage strings</li>
 * </ul>
 * </p>
 * <h3>Key Benefits:</h3>
 * <ul>
 * <li><b>Automatic</b> - No manual save/restore calls needed</li>
 * <li><b>Transparent</b> - Works seamlessly with Vaadin's component lifecycle</li>
 * <li><b>User-focused</b> - Only saves user changes, not programmatic updates</li>
 * <li><b>Simple</b> - One method call to enable full persistence</li>
 * </ul>
 * <h3>When to Use:</h3>
 * <p>
 * <b>Use CValueStorageHelper for:</b>
 * <ul>
 * <li>Standard Vaadin components (ComboBox, TextField, etc.)</li>
 * <li>Simple value persistence (single value per component)</li>
 * <li>Filter components in toolbars</li>
 * <li>Any component where automatic save-on-change is appropriate</li>
 * </ul>
 * </p>
 * <p>
 * <b>Use {@link tech.derbent.api.interfaces.IHasSelectedValueStorage} instead for:</b>
 * <ul>
 * <li>Complex components with multiple related values</li>
 * <li>Custom save/restore timing requirements</li>
 * <li>Non-standard state that doesn't fit the value model</li>
 * </ul>
 * </p>
 * <h3>Supported Components:</h3>
 * <ul>
 * <li><b>ComboBox</b> - Stores selected item (by toString() / custom converter)</li>
 * <li><b>TextField</b> - Stores entered text value</li>
 * <li><b>Any HasValue component</b> - With custom serializer/converter</li>
 * </ul>
 * <h3>Usage Examples:</h3>
 * <h4>Example 1: ComboBox with Simple Enum</h4>
 *
 * <pre>
 * ComboBox&lt;ResponsibleFilterMode&gt; comboBox = new ComboBox&lt;&gt;("Responsible");
 * comboBox.setItems(ResponsibleFilterMode.values());
 * // Enable persistence - uses toString() for save, valueOf() for restore
 * CValueStorageHelper.valuePersist_enable(comboBox, "filter_responsible", storedString -&gt; {
 * 	try {
 * 		return ResponsibleFilterMode.valueOf(storedString);
 * 	} catch (IllegalArgumentException e) {
 * 		return ResponsibleFilterMode.ALL; // Safe default
 * 	}
 * });
 * </pre>
 *
 * <h4>Example 2: ComboBox with Complex Objects</h4>
 *
 * <pre>
 * ComboBox&lt;EntityType&gt; comboBox = new ComboBox&lt;&gt;("Type");
 * comboBox.setItems(availableTypes);
 * comboBox.setItemLabelGenerator(EntityType::getDisplayName);
 * // Enable persistence - stores by entity class name
 * CValueStorageHelper.valuePersist_enable(comboBox, "filter_entityType", className -&gt; {
 * 	// Find matching type in current items
 * 	return comboBox.getListDataView().getItems().filter(type -&gt; type.getEntityClass().getName().equals(className)).findFirst().orElse(null);
 * });
 * </pre>
 *
 * <h4>Example 3: TextField</h4>
 *
 * <pre>
 * TextField nameFilter = new TextField("Name");
 * // Enable persistence - simple string storage
 * CValueStorageHelper.valuePersist_enable(nameFilter, "filter_name");
 * </pre>
 *
 * <h4>Example 4: Custom Serialization</h4>
 *
 * <pre>
 * ComboBox&lt;Project&gt; comboBox = new ComboBox&lt;&gt;("Project");
 * // Enable persistence with custom serializer and converter
 * CValueStorageHelper.valuePersist_enable(comboBox, "filter_project", project -&gt; project.getId().toString(), // Save as ID
 * 		idString -&gt; findProjectById(Long.parseLong(idString)) // Restore by ID
 * );
 * </pre>
 *
 * <h3>How It Works:</h3>
 * <ol>
 * <li><b>Setup:</b> Call valuePersist_enable() with component, storage ID, and converter</li>
 * <li><b>Save:</b> When user changes value, automatically saves toString() to session</li>
 * <li><b>Restore:</b> When component attaches, automatically restores using converter</li>
 * <li><b>Validation:</b> Converter can return null if stored value is no longer valid</li>
 * </ol>
 * <h3>Important Notes:</h3>
 * <ul>
 * <li><b>User Changes Only:</b> Only saves when event.isFromClient() is true</li>
 * <li><b>Null Handling:</b> Converter should return null for invalid stored values</li>
 * <li><b>Storage ID:</b> Must be unique per component within the session</li>
 * <li><b>Initial Value:</b> If no stored value exists, current value is saved as default</li>
 * </ul>
 * @see tech.derbent.api.interfaces.IHasSelectedValueStorage
 * @see tech.derbent.base.session.service.ISessionService */
public class CValueStorageHelper {

	/** Converter interface for transforming values to/from storage format.
	 * @param <T> The component value type */
	@FunctionalInterface
	public interface ValueConverter<T> {

		/** Converts a value from storage string format to the component's type.
		 * @param storedValue The stored string value
		 * @return The converted value, or null if conversion fails */
		T fromString(String storedValue);
	}

	/** Converter interface for transforming values to storage format.
	 * @param <T> The component value type */
	@FunctionalInterface
	public interface ValueSerializer<T> {

		/** Converts a component value to storage string format.
		 * @param value The component value
		 * @return The string representation for storage, or null if value cannot be serialized */
		String toString(T value);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CValueStorageHelper.class);
	private static final String STORAGE_ENABLED_KEY = CValueStorageHelper.class.getName() + ".enabled";

	/** Disables automatic value persistence for a component.
	 * <p>
	 * This method removes the persistence behavior and cleans up the stored value.
	 * </p>
	 * @param component The component to disable persistence for */
	public static void valuePersist_disable(final Component component) {
		Check.notNull(component, "Component cannot be null");
		ComponentUtil.setData(component, STORAGE_ENABLED_KEY, null);
		LOGGER.debug("Disabled auto-persistence for component: {}", component.getId().orElse("unknown"));
	}

	/** Enables automatic value persistence for a ComboBox.
	 * <p>
	 * This is a convenience method for ComboBox components that use toString() for serialization and require a custom converter for deserialization.
	 * </p>
	 * @param <T>       The type of items in the ComboBox
	 * @param comboBox  The ComboBox to enable persistence for
	 * @param storageId The unique storage identifier
	 * @param converter Converter to transform stored string back to item type */
	public static <T> void valuePersist_enable(final ComboBox<T> comboBox, final String storageId, final ValueConverter<T> converter) {
		Check.notNull(comboBox, "ComboBox cannot be null");
		Check.notBlank(storageId, "Storage ID cannot be blank");
		Check.notNull(converter, "Converter cannot be null");
		valuePersist_enable(comboBox, storageId, value -> value != null ? value.toString() : null, converter);
	}

	/** Enables automatic value persistence for a component with custom serialization.
	 * <p>
	 * This is the most flexible method that allows custom conversion logic for both saving (to string) and restoring (from string) values.
	 * </p>
	 * @param <T>        The type of values in the component
	 * @param component  The component to enable persistence for (must implement HasValue)
	 * @param storageId  The unique storage identifier
	 * @param serializer Function to convert component value to storage string
	 * @param converter  Function to convert storage string back to component value */
	public static <T> void valuePersist_enable(final HasValue<?, T> component, final String storageId, final ValueSerializer<T> serializer,
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
		LOGGER.info("[ValuePersistence] CValueStorageHelper: Enabling persistence for storage ID: {}", storageId);
		// Add value change listener to save on change
		component.addValueChangeListener(event -> {
			if (!event.isFromClient()) {
				// Skip programmatic changes to avoid saving during restore
				LOGGER.debug("[ValuePersistence] CValueStorageHelper: Skipping save for storage ID '{}' - change not from client", storageId);
				return;
			}
			try {
				final T value = event.getValue();
				LOGGER.info("[ValuePersistence] CValueStorageHelper: Value changed from client for storage ID '{}', value: {}", storageId, value);
				if (value != null) {
					final String serialized = serializer.toString(value);
					if (serialized != null) {
						sessionService.setSessionValue(storageId, serialized);
						LOGGER.info("[ValuePersistence] CValueStorageHelper: Auto-saved value '{}' for storage ID: {}", serialized, storageId);
					}
				} else {
					sessionService.removeSessionValue(storageId);
					LOGGER.info("[ValuePersistence] CValueStorageHelper: Cleared stored value for storage ID: {}", storageId);
				}
			} catch (final Exception e) {
				LOGGER.error("[ValuePersistence] CValueStorageHelper: Error auto-saving value for storage ID: {}", storageId, e);
			}
		});
		// Add attach listener to restore value when component is added to UI
		vaadinComponent.addAttachListener(event -> {
			LOGGER.info("[ValuePersistence] CValueStorageHelper: Component attached for storage ID '{}', restoring value", storageId);
			valuePersist_restoreValue(component, storageId, converter, sessionService);
		});
		// Add detach listener to clean up (optional - could be removed if values should persist)
		vaadinComponent.addDetachListener(event -> {
			LOGGER.info("[ValuePersistence] CValueStorageHelper: Component detached for storage ID '{}', stored value remains", storageId);
		});
		// If component is already attached, restore value immediately
		if (vaadinComponent.isAttached()) {
			LOGGER.info("[ValuePersistence] CValueStorageHelper: Component already attached for storage ID '{}', restoring value immediately",
					storageId);
			valuePersist_restoreValue(component, storageId, converter, sessionService);
		}
		LOGGER.info("[ValuePersistence] CValueStorageHelper: Enabled auto-persistence for component with storage ID: {}", storageId);
	}

	/** Enables automatic value persistence for a TextField.
	 * <p>
	 * This is a convenience method for TextField components that use simple string values.
	 * </p>
	 * @param textField The TextField to enable persistence for
	 * @param storageId The unique storage identifier */
	public static void valuePersist_enable(final TextField textField, final String storageId) {
		Check.notNull(textField, "TextField cannot be null");
		Check.notBlank(storageId, "Storage ID cannot be blank");
		valuePersist_enable(textField, storageId, value -> value, value -> value);
	}

	/** Checks if a component has auto-persistence enabled.
	 * @param component The component to check
	 * @return true if auto-persistence is enabled, false otherwise */
	public static boolean valuePersist_isAutoPersistenceEnabled(final Component component) {
		Check.notNull(component, "Component cannot be null");
		final Object enabled = ComponentUtil.getData(component, STORAGE_ENABLED_KEY);
		return Boolean.TRUE.equals(enabled);
	}

	/** Restores a component's value from storage.
	 * <p>
	 * This method sets the value without triggering user-facing side effects by checking if the value change event is from the client. This prevents
	 * cascading updates like SQL queries and form population during automatic restoration.
	 * </p>
	 * <p>
	 * If no stored value exists but the component has a current value, that value is saved as the initial default to ensure persistence works on
	 * subsequent refreshes.
	 * </p>
	 * @param <T>            The type of values in the component
	 * @param component      The component to restore value for
	 * @param storageId      The storage identifier
	 * @param converter      Function to convert storage string back to component value
	 * @param sessionService The session service for retrieving stored values */
	private static <T> void valuePersist_restoreValue(final HasValue<?, T> component, final String storageId, final ValueConverter<T> converter,
			final ISessionService sessionService) {
		try {
			final Optional<String> storedValue = sessionService.getSessionValue(storageId);
			LOGGER.info("[ValuePersistence] CValueStorageHelper: Restoring value for storage ID '{}', storedValue present: {}", storageId,
					storedValue.isPresent());
			if (storedValue.isPresent()) {
				LOGGER.info("[ValuePersistence] CValueStorageHelper: Found stored value '{}' for storage ID '{}'", storedValue.get(), storageId);
				final T value = converter.fromString(storedValue.get());
				if (value != null) {
					// Set value programmatically - this will NOT trigger isFromClient() listeners
					// Components should check event.isFromClient() to distinguish user actions from programmatic updates
					component.setValue(value);
					LOGGER.info("[ValuePersistence] CValueStorageHelper: Auto-restored value '{}' for storage ID: {}", value, storageId);
				} else {
					LOGGER.warn("[ValuePersistence] CValueStorageHelper: Could not convert stored value '{}' for storage ID: {}", storedValue.get(),
							storageId);
				}
			} else {
				// No stored value exists - check if component has a current value and save it as initial default
				final T currentValue = component.getValue();
				LOGGER.info("[ValuePersistence] CValueStorageHelper: No stored value for storage ID '{}', current value: {}", storageId,
						currentValue);
				if (currentValue != null) {
					final String serialized = currentValue.toString();
					if (serialized != null && !serialized.isBlank()) {
						sessionService.setSessionValue(storageId, serialized);
						LOGGER.info("[ValuePersistence] CValueStorageHelper: Saved initial value '{}' as default for storage ID: {}", serialized,
								storageId);
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.error("[ValuePersistence] CValueStorageHelper: Error auto-restoring value for storage ID: {}", storageId, e);
		}
	}
}
