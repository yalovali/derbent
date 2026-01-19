package tech.derbent.api.ui.component.basic;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.base.session.service.ISessionService;

/** CTextField - Enhanced base class for text fields in the application with automatic persistence support.
 * <p>
 * This component extends Vaadin's TextField with automatic value persistence capabilities, following the same pattern as CComboBox.
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
 * CTextField textField = new CTextField("Name");
 * textField.setPlaceholder("Enter name...");
 * </pre>
 * </p>
 * <p>
 * <b>With Persistence:</b>
 *
 * <pre>
 * CTextField textField = new CTextField("Filter");
 * textField.enablePersistence("myView_nameFilter");
 * // Value will automatically persist across page refreshes
 * </pre>
 * </p>
 * <p>
 * <b>How Persistence Works:</b>
 * <ol>
 * <li>On value change: Automatically saves value to session storage</li>
 * <li>On component attach: Automatically retrieves and restores stored value</li>
 * <li>User changes only - programmatic setValue() calls don't trigger save</li>
 * </ol>
 * </p>
 */
public class CTextField extends TextField {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTextField.class);
	private static final long serialVersionUID = 1L;

	public static CTextField createEmail(final String label) {
		final CTextField field = new CTextField(label);
		field.setPlaceholder("user@example.com");
		field.setPrefixComponent(VaadinIcon.ENVELOPE.create());
		field.setPattern("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
		field.setErrorMessage("Please enter a valid email address");
		return field;
	}

	public static CTextField createNumeric(final String label) {
		final CTextField field = new CTextField(label);
		field.setPattern("[0-9]*");
		field.setErrorMessage("Please enter only numbers");
		field.setValueChangeMode(ValueChangeMode.EAGER);
		return field;
	}

	public static CTextField createSearch(final String label) {
		final CTextField field = new CTextField(label);
		field.setPlaceholder("Type to search...");
		field.setValueChangeMode(ValueChangeMode.EAGER);
		field.setClearButtonVisible(true);
		return field;
	}

	private boolean persistenceEnabled = false;
	private String persistenceKey;
	private ISessionService sessionService;

	public CTextField() {
		super();
		initializeComponent();
	}

	public CTextField(final String label) {
		super(label);
		initializeComponent();
	}

	/** Disables automatic persistence for this TextField.
	 * <p>
	 * After calling this method, the TextField will no longer automatically save or restore its value.
	 * </p>
	 * @see #enablePersistence(String) */
	public void disablePersistence() {
		persistenceEnabled = false;
		LOGGER.info("[CTextField] Persistence disabled for key: {}", persistenceKey);
	}

	/** Enables automatic persistence for this TextField.
	 * <p>
	 * Once enabled, the TextField will automatically:
	 * <ul>
	 * <li>Save its value to session storage whenever the user (or code) changes it</li>
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
		LOGGER.info("[CTextField] Persistence enabled for key: {}", storageKey);
		// Add value change listener to save on every change
		addValueChangeListener(event -> {
			if (!event.isFromClient()) {
				LOGGER.debug("[CTextField] Value change not from client, skipping save for key: {}", persistenceKey);
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

	/** Common initialization for all CTextField instances. */
	private final void initializeComponent() {
		CAuxillaries.setId(this);
	}

	/** Checks if persistence is enabled for this TextField.
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
				LOGGER.debug("[CTextField] Restoring value '{}' for key: {}", value, persistenceKey);
				setValue(value);
				LOGGER.info("[CTextField] Restored value for key: {}", persistenceKey);
			}
		} catch (final Exception e) {
			LOGGER.error("[CTextField] Error restoring value for key: {}", persistenceKey, e);
		}
	}

	/** Saves the current value to session storage.
	 * <p>
	 * This method is called automatically when persistence is enabled and the value changes. It can also be called manually if needed.
	 * </p>
	 */
	private void saveValue() {
		LOGGER.debug("[CTextField] Saving value for key: {}", persistenceKey);
		if (!persistenceEnabled || sessionService == null) {
			return;
		}
		try {
			final String value = getValue();
			if (value != null && !value.isBlank()) {
				sessionService.setSessionValue(persistenceKey, value);
				LOGGER.debug("[CTextField] Saved value '{}' for key: {}", value, persistenceKey);
			} else {
				sessionService.removeSessionValue(persistenceKey);
				LOGGER.debug("[CTextField] Cleared value for key: {}", persistenceKey);
			}
		} catch (final Exception e) {
			LOGGER.error("[CTextField] Error saving value for key: {}", persistenceKey, e);
		}
	}
}
