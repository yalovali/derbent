package tech.derbent.api.ui.component.basic;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.textfield.TextArea;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.base.session.service.ISessionService;

/** CTextArea - Enhanced base class for text areas in the application with automatic persistence support.
 * <p>
 * This component extends Vaadin's TextArea with automatic value persistence capabilities, following the same pattern as CTextField and CComboBox.
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
 * CTextArea textArea = new CTextArea("Description");
 * textArea.setPlaceholder("Enter description...");
 * </pre>
 * </p>
 * <p>
 * <b>With Persistence:</b>
 *
 * <pre>
 * CTextArea textArea = new CTextArea("Notes");
 * textArea.enablePersistence("myView_notesField");
 * // Value will automatically persist across page refreshes
 * </pre>
 * </p>
 */
public class CTextArea extends TextArea {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTextArea.class);
	private static final long serialVersionUID = 1L;
	private boolean persistenceEnabled = false;
	private String persistenceKey;
	private ISessionService sessionService;

	public CTextArea() {
		super();
		initializeComponent();
	}

	public CTextArea(final String label) {
		super(label);
		initializeComponent();
	}

	public CTextArea(final String label, final String placeholder) {
		super(label, placeholder);
		initializeComponent();
	}

	/** Disables automatic persistence for this TextArea.
	 * <p>
	 * After calling this method, the TextArea will no longer automatically save or restore its value.
	 * </p>
	 * @see #enablePersistence(String) */
	public void disablePersistence() {
		persistenceEnabled = false;
		LOGGER.info("[CTextArea] Persistence disabled for key: {}", persistenceKey);
	}

	/** Enables automatic persistence for this TextArea.
	 * <p>
	 * Once enabled, the TextArea will automatically:
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
		LOGGER.info("[CTextArea] Persistence enabled for key: {}", storageKey);
		// Add value change listener to save on every change
		addValueChangeListener(event -> {
			if (!event.isFromClient()) {
				LOGGER.debug("[CTextArea] Value change not from client, skipping save for key: {}", persistenceKey);
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

	/** Common initialization for all CTextArea instances. */
	private final void initializeComponent() {
		CAuxillaries.setId(this);
	}

	/** Checks if persistence is enabled for this TextArea.
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
				LOGGER.debug("[CTextArea] Restoring value for key: {}", persistenceKey);
				setValue(value);
				LOGGER.info("[CTextArea] Restored value for key: {}", persistenceKey);
			}
		} catch (final Exception e) {
			LOGGER.error("[CTextArea] Error restoring value for key: {}", persistenceKey, e);
		}
	}

	/** Saves the current value to session storage.
	 * <p>
	 * This method is called automatically when persistence is enabled and the value changes. It can also be called manually if needed.
	 * </p>
	 */
	private void saveValue() {
		LOGGER.debug("[CTextArea] Saving value for key: {}", persistenceKey);
		if (!persistenceEnabled || sessionService == null) {
			return;
		}
		try {
			final String value = getValue();
			if (value != null && !value.isBlank()) {
				sessionService.setSessionValue(persistenceKey, value);
				LOGGER.debug("[CTextArea] Saved value for key: {}", persistenceKey);
			} else {
				sessionService.removeSessionValue(persistenceKey);
				LOGGER.debug("[CTextArea] Cleared value for key: {}", persistenceKey);
			}
		} catch (final Exception e) {
			LOGGER.error("[CTextArea] Error saving value for key: {}", persistenceKey, e);
		}
	}
}
