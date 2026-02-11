package tech.derbent.api.setup.component;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.setup.dialogs.CEmailTestDialog;
import tech.derbent.api.setup.domain.CSystemSettings;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.notifications.CNotificationService;

/**
 * CComponentEmailTest - Component for testing email configuration.
 */
public class CComponentEmailTest extends CComponentBase<CSystemSettings<?>> {

	public static final String ID_ROOT = "custom-email-test-component";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentEmailTest.class);
	private static final long serialVersionUID = 1L;

	private CButton buttonOpenDialog;
	private CSystemSettings<?> currentSettings;

	public CComponentEmailTest(final CSystemSettings<?> settings) {
		this.currentSettings = settings;
		initializeComponents();
	}

	protected void initializeComponents() {
		LOGGER.debug("Initializing email test component");
		setId(ID_ROOT);
		setSpacing(false);
		setPadding(false);
		getStyle().set("gap", "12px");

		final H4 header = new H4("Email Configuration Test");
		header.getStyle().set("margin", "0");

		buttonOpenDialog = new CButton("Open Email Test Dialog", VaadinIcon.ENVELOPE.create());
		buttonOpenDialog.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonOpenDialog.addClickListener(e -> on_buttonOpenDialog_clicked());

		add(header, buttonOpenDialog);
		
		LOGGER.debug("Email test component initialized successfully");
	}

	private void on_buttonOpenDialog_clicked() {
		LOGGER.debug("Opening email test dialog");
		try {
			if (currentSettings == null) {
				CNotificationService.showWarning("No email configuration loaded");
				return;
			}
			final CEmailTestDialog dialog = new CEmailTestDialog(currentSettings);
			dialog.open();
			LOGGER.debug("Email test dialog opened successfully");
		} catch (final Exception e) {
			LOGGER.error("Failed to open email test dialog", e);
			CNotificationService.showException("Failed to open email test dialog", e);
		}
	}

	@Override
	protected void refreshComponent() {
		// Refresh if needed
	}

	@Override
	public void setValue(final CSystemSettings<?> value) {
		this.currentSettings = value;
		refreshComponent();
	}

	@Override
	public CSystemSettings<?> getValue() {
		return currentSettings;
	}
}
