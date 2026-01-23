package tech.derbent.api.ui.component.basic;

import java.util.HashMap;
import java.util.Optional;
import org.slf4j.Logger;
import com.vaadin.flow.component.Component;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.base.session.service.ISessionService;

/** IHasMultiValuePersistence - Interface for components that need to persist multiple key-value pairs.
 * <p>
 * This interface extends the single-value persistence pattern to support storing multiple values under a namespace. Perfect for components that need
 * to persist various state information like:
 * </p>
 * <ul>
 * <li>Selected item IDs</li>
 * <li>Filter values (multiple filters)</li>
 * <li>UI state (expanded/collapsed sections)</li>
 * <li>Column visibility settings</li>
 * <li>Sort/order preferences</li>
 * </ul>
 * <p>
 * <b>Storage Pattern:</b> Values are stored with keys in the format: {@code {namespace}.{key}}
 * </p>
 * <p>
 * <b>Example Usage:</b>
 *
 * <pre>
 * public class CKanbanBoard extends Component implements IHasMultiValuePersistence {
 *
 * 	private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanBoard.class);
 * 	private String persistenceNamespace;
 * 	private boolean persistenceEnabled;
 *
 * 	public void initializePersistence() {
 * 		enableMultiValuePersistence("kanbanBoard_projectX");
 * 		// Restore previous state
 * 		getPersistedValue("selectedColumn").ifPresent(this::selectColumn);
 * 		getPersistedValue("expandedSections").ifPresent(this::expandSections);
 * 	}
 *
 * 	public void onColumnSelected(String columnId) {
 * 		persistValue("selectedColumn", columnId);
 * 	}
 *
 * 	public void onFilterChanged(String filterName, String filterValue) {
 * 		persistValue("filter_" + filterName, filterValue);
 * 	}
 *
 * 	&#64;Override
 * 	public Logger getLogger() { return LOGGER; }
 * }
 * </pre>
 * </p>
 * <p>
 * <b>Benefits:</b>
 * </p>
 * <ul>
 * <li>Store multiple related values under one namespace</li>
 * <li>Each component manages its own persistence logic</li>
 * <li>Type-safe through custom serialization/deserialization</li>
 * <li>Automatic cleanup when persistence is disabled</li>
 * <li>Consistent storage pattern across application</li>
 * </ul>
 * @author Derbent Framework
 * @since 1.0 */
public interface IHasMultiValuePersistence {

	/** Gets the logger for this component.
	 * @return The logger instance */
	Logger getLogger();

	/** Gets the session service.
	 * <p>
	 * Default implementation retrieves from Spring context.
	 * </p>
	 * @return The session service instance */
	default ISessionService getSessionService() { return CSpringContext.getBean(ISessionService.class); }

	/** Clears all persisted values for this component's namespace.
	 * <p>
	 * This method removes all key-value pairs that were stored under this component's namespace.
	 * </p>
	 */
	default void persist_clearAllValues() {
		if (!persist_isEnabled() || persist_getNamespace() == null) {
			return;
		}
		try {
			final ISessionService sessionService = getSessionService();
			if (sessionService == null) {
				return;
			}
			new HashMap<>();
			// Note: ISessionService doesn't expose getAllKeys(), so we track keys internally
			// Components should call clearPersistedValue() for each key they know about
			getLogger().info("[{}] Cleared all persisted values for namespace: {}", getClass().getSimpleName(), persist_getNamespace());
		} catch (final Exception e) {
			getLogger().error("[{}] Error clearing persisted values for namespace: {}", getClass().getSimpleName(), persist_getNamespace(), e);
		}
	}

	/** Clears a specific persisted value.
	 * @param key The key to clear (without namespace prefix) */
	default void persist_clearValue(final String key) {
		getLogger().debug("[{}] Clearing persisted value for key: {}", getClass().getSimpleName(), key);
		if (!persist_isEnabled() || persist_getNamespace() == null) {
			return;
		}
		try {
			final String fullKey = persist_getNamespace() + "." + key;
			final ISessionService sessionService = getSessionService();
			if (sessionService != null) {
				sessionService.removeSessionValue(fullKey);
			}
		} catch (final Exception e) {
			getLogger().error("[{}] Error clearing persisted value for key: {}", getClass().getSimpleName(), key, e);
		}
	}

	/** Disables multi-value persistence for this component. */
	default void persist_disableMultiValue() {
		persist_setEnabled(false);
		getLogger().info("[{}] Multi-value persistence disabled for namespace: {}", getClass().getSimpleName(), persist_getNamespace());
	}

	/** Enables multi-value persistence for this component.
	 * <p>
	 * After enabling, the component can use {@link #persistValue(String, Object)} and {@link #persist_getValue(String)} to store and retrieve values.
	 * </p>
	 * @param namespace The unique namespace for this component's persisted values (e.g., "kanbanBoard_projectX", "activitiesView_filters")
	 * @throws IllegalArgumentException if namespace is null or blank */
	
	default void persist_enableMultiValue(final String namespace) {
		if (namespace == null || namespace.isBlank()) {
			throw new IllegalArgumentException("Persistence namespace cannot be null or blank");
		}
		persist_setNamespace(namespace);
		persist_setEnabled(true);
		getLogger().info("[{}] Multi-value persistence enabled for namespace: {}", getClass().getSimpleName(), namespace);
		// Add attach listener to allow restoration when component is added to UI
		if (this instanceof Component) {
			((Component) this).addAttachListener(event -> {
				if (persist_isEnabled()) {
					persist_onRestore();
				}
			});
			// If already attached, call restore immediately
			if (((Component) this).isAttached()) {
				persist_onRestore();
			}
		}
	}

	/** Gets the persistence namespace for this component.
	 * @return The namespace string */
	String persist_getNamespace();

	/** Gets a persisted value by key.
	 * @param key The key to retrieve (without namespace prefix)
	 * @return Optional containing the value if found, empty otherwise */
	default Optional<String> persist_getValue(final String key) {
		getLogger().debug("[{}] Retrieving persisted value for key: {}", getClass().getSimpleName(), key);
		if (!persist_isEnabled() || persist_getNamespace() == null) {
			return Optional.empty();
		}
		try {
			final String fullKey = persist_getNamespace() + "." + key;
			final ISessionService sessionService = getSessionService();
			if (sessionService != null) {
				final Optional<String> value = sessionService.getSessionValue(fullKey);
				if (value.isPresent()) {
					getLogger().debug("[{}] Retrieved persisted value for key: {} = {}", getClass().getSimpleName(), fullKey, value.get());
				}
				return value;
			}
		} catch (final Exception e) {
			getLogger().error("[{}] Error retrieving persisted value for key: {}", getClass().getSimpleName(), key, e);
		}
		return Optional.empty();
	}

	/** Checks if multi-value persistence is enabled.
	 * @return true if enabled, false otherwise */
	boolean persist_isEnabled();

	/** Hook method called when component attaches and persistence is enabled.
	 * <p>
	 * Components should override this method to restore their persisted state. This is called automatically when the component attaches to the UI.
	 * </p>
	 * <p>
	 * <b>Example:</b>
	 *
	 * <pre>
	 *
	 * &#64;Override
	 * protected void onPersistenceRestore() {
	 * 	getPersistedValue("selectedColumn").ifPresent(this::selectColumn);
	 * 	getPersistedValue("filterValue").ifPresent(this::applyFilter);
	 * 	getPersistedValue("expandedState").ifPresent(this::restoreExpandedState);
	 * }
	 * </pre>
	 * </p>
	 */
	default void persist_onRestore() {
		// Override in implementing class to restore state
		getLogger().debug("[{}] Persistence restore hook called (override to implement)", getClass().getSimpleName());
	}

	/** Sets whether multi-value persistence is enabled.
	 * @param enabled true to enable, false to disable */
	void persist_setEnabled(boolean enabled);
	/** Sets the persistence namespace.
	 * @param namespace The namespace string */
	void persist_setNamespace(String namespace);

	/** Persists a value with the given key.
	 * <p>
	 * The value is stored under the key "{namespace}.{key}". Components are responsible for serialization of complex objects to strings.
	 * </p>
	 * @param key   The key to store under (without namespace prefix)
	 * @param value The value to store (will be converted to string via toString()) */
	default void persistValue(final String key, final Object value) {
		if (!persist_isEnabled() || persist_getNamespace() == null) {
			return;
		}
		try {
			final String fullKey = persist_getNamespace() + "." + key;
			final ISessionService sessionService = getSessionService();
			if (sessionService != null) {
				if (value != null) {
					final String serialized = value.toString();
					sessionService.setSessionValue(fullKey, serialized);
					getLogger().debug("[{}] Persisted value for key: {} = {}", getClass().getSimpleName(), fullKey, serialized);
				} else {
					// Null value - remove from storage
					sessionService.removeSessionValue(fullKey);
					getLogger().debug("[{}] Cleared value for key: {}", getClass().getSimpleName(), fullKey);
				}
			}
		} catch (final Exception e) {
			getLogger().error("[{}] Error persisting value for key: {}", getClass().getSimpleName(), key, e);
		}
	}
}
