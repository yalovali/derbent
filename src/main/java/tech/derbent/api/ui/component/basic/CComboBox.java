package tech.derbent.api.ui.component.basic;

import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.combobox.ComboBox;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.base.session.service.ISessionService;

/** CComboBox - Custom ComboBox component with built-in automatic persistence support.
 * <p>
 * This component extends Vaadin's ComboBox with automatic value persistence capabilities, following the project's C-prefix naming convention.
 * </p>
 * <p>
 * <b>Key Features:</b>
 * <ul>
 * <li><b>Automatic Persistence:</b> Call {@link #enablePersistence(String, Function)} to automatically save and restore values across page
 * refreshes</li>
 * <li><b>Simple API:</b> One method call enables full persistence with custom serialization</li>
 * <li><b>C-Prefix Convention:</b> Follows project standard for custom components</li>
 * </ul>
 * </p>
 * <p>
 * <b>Basic Usage:</b>
 *
 * <pre>
 * CComboBox&lt;String&gt; comboBox = new CComboBox&lt;&gt;("Select Option");
 * comboBox.setItems("Option 1", "Option 2", "Option 3");
 * </pre>
 * </p>
 * <p>
 * <b>With Persistence (Enum Example):</b>
 *
 * <pre>
 * CComboBox&lt;FilterMode&gt; comboBox = new CComboBox&lt;&gt;("Filter Mode");
 * comboBox.setItems(FilterMode.values());
 * comboBox.setValue(FilterMode.ALL);
 * // Enable persistence - saves/restores enum by name
 * comboBox.enablePersistence("myFilter_mode", stored -&gt; FilterMode.valueOf(stored));
 * </pre>
 * </p>
 * <p>
 * <b>With Persistence (Complex Object Example):</b>
 *
 * <pre>
 * CComboBox&lt;EntityType&gt; comboBox = new CComboBox&lt;&gt;("Type");
 * comboBox.setItems(availableTypes);
 * // Enable persistence - saves by class name, restores by finding in current items
 * comboBox.enablePersistence("filter_entityType",
 * 		className -&gt; comboBox.getListDataView().getItems().filter(t -&gt; t.getClassName().equals(className)).findFirst().orElse(null));
 * </pre>
 * </p>
 * <p>
 * <b>How Persistence Works:</b>
 * <ol>
 * <li>On value change: Automatically saves {@code value.toString()} to session storage</li>
 * <li>On component attach: Automatically retrieves stored string and converts back using your function</li>
 * <li>Converter returns null if value is no longer valid (e.g., item removed from list)</li>
 * </ol>
 * </p>
 * @param <T> the type of items in the ComboBox */
public class CComboBox<T> extends ComboBox<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComboBox.class);
	private static final long serialVersionUID = 1L;
	private Function<String, T> persistenceConverter;
	private boolean persistenceEnabled = false;
	private String persistenceKey;
	private ISessionService sessionService;

	/** Creates an empty ComboBox. */
	public CComboBox() {
		super();
	}

	/** Creates a ComboBox with the given label.
	 * @param label the label text to set */
	public CComboBox(final String label) {
		super(label);
	}

	/** Creates a ComboBox with the given label and items.
	 * @param label the label text to set
	 * @param items the items to set */
	@SafeVarargs
	public CComboBox(final String label, final T... items) {
		super(label, items);
	}

	/** Disables automatic persistence for this ComboBox.
	 * <p>
	 * After calling this method, the ComboBox will no longer automatically save or restore its value.
	 * </p>
	 * @see #enablePersistence(String, Function) */
	public void disablePersistence() {
		persistenceEnabled = false;
		LOGGER.info("[CComboBox] Persistence disabled for key: {}", persistenceKey);
	}

	/** Enables automatic persistence for this ComboBox.
	 * <p>
	 * Once enabled, the ComboBox will automatically:
	 * <ul>
	 * <li>Save its value to session storage whenever the user (or code) changes it</li>
	 * <li>Restore its value from session storage when the component attaches to the UI</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <b>Persistence Mechanism:</b>
	 * <ul>
	 * <li><b>Save:</b> Uses {@code value.toString()} to convert value to string</li>
	 * <li><b>Restore:</b> Uses provided converter function to convert string back to value</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <b>IMPORTANT:</b> The converter function should:
	 * <ul>
	 * <li>Return the matching item from the current items list</li>
	 * <li>Return null if the stored value is no longer valid</li>
	 * <li>Handle exceptions gracefully (return null on error)</li>
	 * </ul>
	 * </p>
	 * @param storageKey The unique key to use for storing the value in session storage
	 * @param converter  Function to convert stored string back to item type (return null if not found)
	 * @throws IllegalArgumentException if storageKey is null/blank or converter is null
	 * @see #disablePersistence() */
	@SuppressWarnings ("unused")
	public void enablePersistence(final String storageKey, final Function<String, T> converter) {
		if (storageKey == null || storageKey.isBlank()) {
			throw new IllegalArgumentException("Storage key cannot be null or blank");
		}
		if (converter == null) {
			throw new IllegalArgumentException("Converter function cannot be null");
		}
		persistenceKey = storageKey;
		persistenceConverter = converter;
		persistenceEnabled = true;
		// Get session service
		if (sessionService == null) {
			sessionService = CSpringContext.getBean(ISessionService.class);
		}
		LOGGER.info("[CComboBox] Persistence enabled for key: {}", storageKey);
		// Add value change listener to save on every change
		addValueChangeListener(event -> {
			if (!event.isFromClient()) {
				LOGGER.debug("[CComboBox] Value change not from client, skipping save for key: {}", persistenceKey);
				return;
			}
			if (persistenceEnabled) {
				saveValue();
			}
		});
		// Add attach listener to restore when component is added to UI
		addAttachListener(event -> {
			if (persistenceEnabled) {
				restoreValue();
			}
		});
		// If already attached, restore immediately
		if (isAttached()) {
			restoreValue();
		}
	}

	/** Checks if persistence is enabled for this ComboBox.
	 * @return true if persistence is enabled, false otherwise */
	public boolean isPersistenceEnabled() { return persistenceEnabled; }

	/** Restores the value from session storage.
	 * <p>
	 * This method is called automatically when persistence is enabled and the component attaches. It can also be called manually if needed.
	 * </p>
	 */
	private void restoreValue() {
		if (!persistenceEnabled || sessionService == null || persistenceConverter == null) {
			return;
		}
		try {
			final Optional<String> storedValue = sessionService.getSessionValue(persistenceKey);
			if (storedValue.isPresent()) {
				final String serialized = storedValue.get();
				LOGGER.debug("[CComboBox] Restoring value '{}' for key: {}", serialized, persistenceKey);
				final T converted = persistenceConverter.apply(serialized);
				if (converted != null) {
					setValue(converted);
					LOGGER.info("[CComboBox] Restored value for key: {}", persistenceKey);
				} else {
					LOGGER.warn("[CComboBox] Stored value '{}' is no longer valid for key: {}", serialized, persistenceKey);
				}
			}
		} catch (final Exception e) {
			LOGGER.error("[CComboBox] Error restoring value for key: {}", persistenceKey, e);
		}
	}

	/** Saves the current value to session storage.
	 * <p>
	 * This method is called automatically when persistence is enabled and the value changes. It can also be called manually if needed.
	 * </p>
	 */
	private void saveValue() {
		LOGGER.debug("[CComboBox] Saving value for key: {}", persistenceKey);
		if (!persistenceEnabled || sessionService == null) {
			return;
		}
		try {
			final T value = getValue();
			if (value != null) {
				final String serialized = value.toString();
				sessionService.setSessionValue(persistenceKey, serialized);
				LOGGER.debug("[CComboBox] Saved value '{}' for key: {}", serialized, persistenceKey);
			} else {
				sessionService.removeSessionValue(persistenceKey);
				LOGGER.debug("[CComboBox] Cleared value for key: {}", persistenceKey);
			}
		} catch (final Exception e) {
			LOGGER.error("[CComboBox] Error saving value for key: {}", persistenceKey, e);
		}
	}
}
