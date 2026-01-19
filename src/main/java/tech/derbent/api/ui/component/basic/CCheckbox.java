package tech.derbent.api.ui.component.basic;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.checkbox.Checkbox;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.base.session.service.ISessionService;

/** CCheckbox - Enhanced base class for checkboxes in the application with automatic persistence support.
 * <p>
 * This component extends Vaadin's Checkbox with automatic value persistence capabilities, following the same pattern as CTextField and CComboBox.
 * </p>
 * <p>
 * <b>Key Features:</b>
 * <ul>
 * <li><b>Automatic Persistence:</b> Call {@link #enablePersistence(String)} to automatically save and restore values across page refreshes</li>
 * <li><b>Simple API:</b> One method call enables full persistence</li>
 * <li><b>C-Prefix Convention:</b> Follows project standard for custom components</li>
 * </ul>
 * </p>
 * <p>
 * <b>Basic Usage:</b>
 *
 * <pre>
 * CCheckbox checkbox = new CCheckbox("Active");
 * checkbox.setValue(true);
 * </pre>
 * </p>
 * <p>
 * <b>With Persistence:</b>
 *
 * <pre>
 * CCheckbox checkbox = new CCheckbox("Show Completed");
 * checkbox.enablePersistence("myView_showCompleted");
 * // Value will automatically persist across page refreshes
 * </pre>
 * </p>
 */
public class CCheckbox extends Checkbox {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCheckbox.class);
	private static final long serialVersionUID = 1L;
	private boolean persistenceEnabled = false;
	private String persistenceKey;
	private ISessionService sessionService;

	public CCheckbox() {
		super();
		initializeComponent();
	}

	public CCheckbox(final boolean initialValue) {
		super(initialValue);
		initializeComponent();
	}

	public CCheckbox(final String label) {
		super(label);
		initializeComponent();
	}

	public CCheckbox(final String label, final boolean initialValue) {
		super(label, initialValue);
		initializeComponent();
	}

	/** Disables automatic persistence for this Checkbox.
	 * <p>
	 * After calling this method, the Checkbox will no longer automatically save or restore its value.
	 * </p>
	 * @see #enablePersistence(String) */
	public void disablePersistence() {
		persistenceEnabled = false;
		LOGGER.info("[CCheckbox] Persistence disabled for key: {}", persistenceKey);
	}

	/** Enables automatic persistence for this Checkbox.
	 * <p>
	 * Once enabled, the Checkbox will automatically:
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
	 * @see #disablePersistence() */
	@SuppressWarnings ("unused")
	public void enablePersistence(final String storageKey) {
		if (storageKey == null || storageKey.isBlank()) {
			throw new IllegalArgumentException("Storage key cannot be null or blank");
		}
		persistenceKey = storageKey;
		persistenceEnabled = true;
		// Get session service
		if (sessionService == null) {
			sessionService = CSpringContext.getBean(ISessionService.class);
		}
		LOGGER.info("[CCheckbox] Persistence enabled for key: {}", storageKey);
		// Add value change listener to save on every change
		addValueChangeListener(event -> {
			if (!event.isFromClient()) {
				LOGGER.debug("[CCheckbox] Value change not from client, skipping save for key: {}", persistenceKey);
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

	/** Common initialization for all CCheckbox instances. */
	private final void initializeComponent() {
		CAuxillaries.setId(this);
	}

	/** Checks if persistence is enabled for this Checkbox.
	 * @return true if persistence is enabled, false otherwise */
	public boolean isPersistenceEnabled() { return persistenceEnabled; }

	/** Restores the value from session storage.
	 * <p>
	 * This method is called automatically when persistence is enabled and the component attaches. It can also be called manually if needed.
	 * </p>
	 */
	private void restoreValue() {
		if (!persistenceEnabled || sessionService == null) {
			return;
		}
		try {
			final Optional<String> storedValue = sessionService.getSessionValue(persistenceKey);
			if (storedValue.isPresent()) {
				final String value = storedValue.get();
				LOGGER.debug("[CCheckbox] Restoring value '{}' for key: {}", value, persistenceKey);
				setValue(Boolean.parseBoolean(value));
				LOGGER.info("[CCheckbox] Restored value for key: {}", persistenceKey);
			}
		} catch (final Exception e) {
			LOGGER.error("[CCheckbox] Error restoring value for key: {}", persistenceKey, e);
		}
	}

	/** Saves the current value to session storage.
	 * <p>
	 * This method is called automatically when persistence is enabled and the value changes. It can also be called manually if needed.
	 * </p>
	 */
	private void saveValue() {
		LOGGER.debug("[CCheckbox] Saving value for key: {}", persistenceKey);
		if (!persistenceEnabled || sessionService == null) {
			return;
		}
		try {
			final Boolean value = getValue();
			final String serialized = value.toString();
			sessionService.setSessionValue(persistenceKey, serialized);
			LOGGER.debug("[CCheckbox] Saved value '{}' for key: {}", serialized, persistenceKey);
		} catch (final Exception e) {
			LOGGER.error("[CCheckbox] Error saving value for key: {}", persistenceKey, e);
		}
	}
}
