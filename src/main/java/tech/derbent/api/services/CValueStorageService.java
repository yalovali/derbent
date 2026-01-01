package tech.derbent.api.services;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.server.VaadinSession;
import tech.derbent.api.utils.Check;

/**
 * CValueStorageService - Service for storing and retrieving component values in the user session.
 * <p>
 * This service provides a centralized mechanism for persisting UI component values across
 * refreshes and page reloads within a user's session. Values are stored in the VaadinSession
 * and are specific to each user's session.
 * </p>
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Session-based storage (values persist across component refreshes)</li>
 * <li>Thread-safe using ConcurrentHashMap</li>
 * <li>Type-safe retrieval with generic methods</li>
 * <li>Automatic cleanup on session end</li>
 * <li>Support for any serializable value type</li>
 * </ul>
 * <h3>Usage Example:</h3>
 * <pre>
 * // Store a value
 * CValueStorageService.storeValue("entityType_backlog", "CActivity");
 * 
 * // Retrieve a value
 * Optional&lt;String&gt; value = CValueStorageService.retrieveValue("entityType_backlog");
 * 
 * // Remove a value
 * CValueStorageService.removeValue("entityType_backlog");
 * 
 * // Clear all values
 * CValueStorageService.clearAll();
 * </pre>
 * 
 * @see tech.derbent.api.interfaces.IHasSelectedValueStorage
 */
@Service
public class CValueStorageService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CValueStorageService.class);
	private static final String STORAGE_KEY = CValueStorageService.class.getName() + ".valueStorage";

	/**
	 * Clears all stored values for the current session.
	 * <p>
	 * This method removes all key-value pairs from the storage map. It should be called
	 * during session cleanup or when you want to reset all stored component values.
	 * </p>
	 */
	public static void clearAll() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			LOGGER.warn("No active VaadinSession, cannot clear stored values");
			return;
		}
		final Map<String, Object> storage = getStorageMap(session);
		if (storage != null) {
			storage.clear();
			LOGGER.debug("Cleared all stored values from session");
		}
	}

	/**
	 * Gets or creates the storage map for the current session.
	 * 
	 * @param session The VaadinSession to get the storage map from
	 * @return The storage map (never null)
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> getOrCreateStorageMap(final VaadinSession session) {
		Check.notNull(session, "VaadinSession cannot be null");
		Map<String, Object> storage = (Map<String, Object>) session.getAttribute(STORAGE_KEY);
		if (storage == null) {
			storage = new ConcurrentHashMap<>();
			session.setAttribute(STORAGE_KEY, storage);
			LOGGER.debug("Created new storage map for session");
		}
		return storage;
	}

	/**
	 * Gets the storage map if it exists, or null if not.
	 * 
	 * @param session The VaadinSession to get the storage map from
	 * @return The storage map, or null if it doesn't exist
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> getStorageMap(final VaadinSession session) {
		return (Map<String, Object>) session.getAttribute(STORAGE_KEY);
	}

	/**
	 * Removes a stored value from the current session.
	 * <p>
	 * This method removes the value associated with the given key. If the key doesn't
	 * exist, this method does nothing.
	 * </p>
	 * 
	 * @param key The storage key (must not be null or blank)
	 */
	public static void removeValue(final String key) {
		Check.notBlank(key, "Storage key cannot be null or blank");
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			LOGGER.warn("No active VaadinSession, cannot remove value for key: {}", key);
			return;
		}
		final Map<String, Object> storage = getStorageMap(session);
		if (storage != null) {
			storage.remove(key);
			LOGGER.debug("Removed stored value for key: {}", key);
		}
	}

	/**
	 * Retrieves a stored value from the current session.
	 * <p>
	 * This method attempts to retrieve and cast the stored value to the expected type.
	 * If the value doesn't exist or the session is not available, returns an empty Optional.
	 * </p>
	 * <h4>Type Safety:</h4>
	 * The caller is responsible for ensuring the type parameter matches the stored value type.
	 * If the type doesn't match, a ClassCastException will be thrown.
	 * 
	 * @param <T> The expected type of the stored value
	 * @param key The storage key (must not be null or blank)
	 * @return Optional containing the stored value, or empty if not found
	 * @throws ClassCastException if the stored value cannot be cast to type T
	 */
	@SuppressWarnings("unchecked")
	public static <T> Optional<T> retrieveValue(final String key) {
		Check.notBlank(key, "Storage key cannot be null or blank");
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			LOGGER.debug("No active VaadinSession, cannot retrieve value for key: {}", key);
			return Optional.empty();
		}
		final Map<String, Object> storage = getStorageMap(session);
		if (storage == null) {
			return Optional.empty();
		}
		final Object value = storage.get(key);
		if (value != null) {
			LOGGER.debug("Retrieved stored value for key: {}", key);
			return Optional.of((T) value);
		}
		return Optional.empty();
	}

	/**
	 * Stores a value in the current session.
	 * <p>
	 * The value is stored in the VaadinSession and will be available until the session ends
	 * or the value is explicitly removed. The value should be serializable to support
	 * session serialization in cluster environments.
	 * </p>
	 * 
	 * @param key   The storage key (must not be null or blank)
	 * @param value The value to store (can be null to clear the value)
	 */
	public static void storeValue(final String key, final Object value) {
		Check.notBlank(key, "Storage key cannot be null or blank");
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			LOGGER.warn("No active VaadinSession, cannot store value for key: {}", key);
			return;
		}
		final Map<String, Object> storage = getOrCreateStorageMap(session);
		if (value == null) {
			storage.remove(key);
			LOGGER.debug("Removed stored value for key: {}", key);
		} else {
			storage.put(key, value);
			LOGGER.debug("Stored value for key: {}", key);
		}
	}
}
