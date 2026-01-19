package tech.derbent.api.ui.component.basic;

import java.util.Optional;
import org.slf4j.Logger;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.base.session.service.ISessionService;

/** IHasValuePersistence - Interface for components with automatic value persistence.
 * <p>
 * This interface provides default methods for automatic value persistence following the same pattern used by CComboBox and CTextField. Components
 * implementing this interface get automatic save/restore functionality with minimal code.
 * </p>
 * <p>
 * <b>Benefits:</b>
 * <ul>
 * <li>Consistent persistence pattern across all components</li>
 * <li>Minimal boilerplate code</li>
 * <li>Automatic save on user changes only</li>
 * <li>Automatic restore on component attach</li>
 * </ul>
 * </p>
 * <p>
 * <b>Implementation Example:</b>
 *
 * <pre>
 * public class CMyComponent extends SomeVaadinComponent implements IHasValuePersistence&lt;String&gt; {
 * 
 * 	private static final Logger LOGGER = LoggerFactory.getLogger(CMyComponent.class);
 * 	private String persistenceKey;
 * 	private boolean persistenceEnabled = false;
 * 	private ISessionService sessionService;
 *
 * 	&#64;Override
 * 	public Logger getLogger() { return LOGGER; }
 *
 * 	&#64;Override
 * 	public String getPersistenceKey() { return persistenceKey; }
 *
 * 	&#64;Override
 * 	public void setPersistenceKey(String key) { this.persistenceKey = key; }
 *
 * 	&#64;Override
 * 	public boolean isPersistenceEnabled() { return persistenceEnabled; }
 *
 * 	&#64;Override
 * 	public void setPersistenceEnabled(boolean enabled) { this.persistenceEnabled = enabled; }
 *
 * 	&#64;Override
 * 	public ISessionService getSessionService() {
 * 		if (sessionService == null) {
 * 			sessionService = CSpringContext.getBean(ISessionService.class);
 * 		}
 * 		return sessionService;
 * 	}
 *
 * 	&#64;Override
 * 	public String serializeValue(String value) {
 * 		return value; // Simple string serialization
 * 	}
 *
 * 	&#64;Override
 * 	public String deserializeValue(String storedValue) {
 * 		return storedValue; // Simple string deserialization
 * 	}
 * }
 * </pre>
 * </p>
 * @param <T> The type of values this component handles */
public interface IHasValuePersistence<T> extends HasValue<HasValue.ValueChangeEvent<T>, T> {

	/** Default method to deserialize a stored string value back to component type.
	 * <p>
	 * Override this method if your component needs custom deserialization logic.
	 * </p>
	 * @param storedValue The stored string value
	 * @return The deserialized value, or null if deserialization fails */
	default T deserializeValue(final String storedValue) {
		// Default implementation - component should override if needed
		@SuppressWarnings ("unchecked")
		final T result = (T) storedValue;
		return result;
	}

	/** Gets the logger for this component.
	 * <p>
	 * Component must provide its own logger instance.
	 * </p>
	 * @return The logger instance */
	Logger getLogger();

	/** Gets the session service for this component.
	 * <p>
	 * Default implementation retrieves from Spring context. Component can override if needed.
	 * </p>
	 * @return The session service instance */
	default ISessionService getSessionService() { return CSpringContext.getBean(ISessionService.class); }

	/** Disables automatic persistence for this component.
	 * <p>
	 * After calling this method, the component will no longer automatically save or restore its value.
	 * </p>
	 * @see #persist_enable(String) */
	default void persist_disable() {
		persist_setEnabled(false);
		getLogger().info("[{}] Persistence disabled for key: {}", getClass().getSimpleName(), persist_getKey());
	}

	/** Enables automatic persistence for this component.
	 * <p>
	 * Once enabled, the component will automatically:
	 * <ul>
	 * <li>Save its value to session storage whenever the user changes it</li>
	 * <li>Restore its value from session storage when the component attaches to the UI</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <b>IMPORTANT:</b> Only user changes (isFromClient() == true) trigger automatic saving to prevent loops during restoration.
	 * </p>
	 * @param storageKey The unique key to use for storing the value in session storage
	 * @throws IllegalArgumentException if storageKey is null or blank
	 * @see #persist_disable() */
	@SuppressWarnings ("unused")
	default void persist_enable(final String storageKey) {
		if (storageKey == null || storageKey.isBlank()) {
			throw new IllegalArgumentException("Storage key cannot be null or blank");
		}
		persist_setKey(storageKey);
		persist_setEnabled(true);
		getLogger().info("[{}] Persistence enabled for key: {}", getClass().getSimpleName(), storageKey);
		// Add value change listener to save on every change
		addValueChangeListener(event -> {
			if (!event.isFromClient()) {
				getLogger().debug("[{}] Value change not from client, skipping save for key: {}", getClass().getSimpleName(), persist_getKey());
				return;
			}
			if (persist_isEnabled()) {
				persist_saveValue();
			}
		});
		// Add attach listener to restore when component is added to UI
		if (this instanceof Component) {
			((Component) this).addAttachListener(event -> {
				if (persist_isEnabled()) {
					persist_restoreValue();
				}
			});
			// If already attached, restore immediately
			if (((Component) this).isAttached()) {
				persist_restoreValue();
			}
		}
	}

	/** Gets the persistence key for this component.
	 * @return The storage key */
	String persist_getKey();
	/** Checks if persistence is enabled for this component.
	 * @return true if persistence is enabled, false otherwise */
	boolean persist_isEnabled();

	/** Restores the value from session storage.
	 * <p>
	 * This method is called automatically when persistence is enabled and the component attaches.
	 * </p>
	 */
	default void persist_restoreValue() {
		if (!persist_isEnabled() || getSessionService() == null) {
			return;
		}
		try {
			final Optional<String> storedValue = getSessionService().getSessionValue(persist_getKey());
			if (storedValue.isPresent()) {
				final String serialized = storedValue.get();
				getLogger().debug("[{}] Restoring value '{}' for key: {}", getClass().getSimpleName(), serialized, persist_getKey());
				final T value = deserializeValue(serialized);
				if (value != null) {
					setValue(value);
					getLogger().info("[{}] Restored value for key: {}", getClass().getSimpleName(), persist_getKey());
				} else {
					getLogger().warn("[{}] Could not deserialize stored value '{}' for key: {}", getClass().getSimpleName(), serialized,
							persist_getKey());
				}
			}
		} catch (final Exception e) {
			getLogger().error("[{}] Error restoring value for key: {}", getClass().getSimpleName(), persist_getKey(), e);
		}
	}

	/** Saves the current value to session storage.
	 * <p>
	 * This method is called automatically when persistence is enabled and the value changes.
	 * </p>
	 */
	default void persist_saveValue() {
		getLogger().debug("[{}] Saving value for key: {}", getClass().getSimpleName(), persist_getKey());
		if (!persist_isEnabled() || getSessionService() == null) {
			return;
		}
		try {
			final T value = getValue();
			if (value != null) {
				final String serialized = serializeValue(value);
				if (serialized != null && !serialized.isBlank()) {
					getSessionService().setSessionValue(persist_getKey(), serialized);
					getLogger().debug("[{}] Saved value '{}' for key: {}", getClass().getSimpleName(), serialized, persist_getKey());
					return;
				}
			}
			// Value is null or empty - remove from storage
			getSessionService().removeSessionValue(persist_getKey());
			getLogger().debug("[{}] Cleared value for key: {}", getClass().getSimpleName(), persist_getKey());
		} catch (final Exception e) {
			getLogger().error("[{}] Error saving value for key: {}", getClass().getSimpleName(), persist_getKey(), e);
		}
	}

	/** Sets whether persistence is enabled.
	 * @param enabled true to enable, false to disable */
	void persist_setEnabled(boolean enabled);
	/** Sets the persistence key.
	 * @param key The storage key */
	void persist_setKey(String key);

	/** Default method to serialize a component value to string for storage.
	 * <p>
	 * Override this method if your component needs custom serialization logic.
	 * </p>
	 * @param value The component value
	 * @return The serialized string value, or null if value cannot be serialized */
	default String serializeValue(final T value) {
		// Default implementation uses toString()
		return value != null ? value.toString() : null;
	}
}
