package tech.derbent.api.setup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.authentication.service.CLdapAuthenticator;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.setup.dialogs.CEmailTestDialog;
import tech.derbent.api.setup.dialogs.CLdapTestDialog;
import tech.derbent.api.setup.domain.CSystemSettings;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

@SuppressWarnings ("rawtypes")
public abstract class CPageServiceSystemSettings<SettingsClass extends CSystemSettings<SettingsClass>>
		extends CPageServiceDynamicPage<SettingsClass> {

	private final CLdapAuthenticator ldapAuthenticator;
	Logger LOGGER = LoggerFactory.getLogger(CPageServiceSystemSettings.class);
	Long serialVersionUID = 1L;

	@SuppressWarnings ("unchecked")
	public CPageServiceSystemSettings(IPageServiceImplementer view) {
		super(view);
		ldapAuthenticator = CSpringContext.getBean(CLdapAuthenticator.class);
		Check.notNull(ldapAuthenticator, "LDAP Authenticator must be available in Spring context");
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CSystemSettings");
		if (!(getView() instanceof CGridViewBaseDBEntity)) {
			return;
		}
		final CGridViewBaseDBEntity gridView = (CGridViewBaseDBEntity) getView();
		gridView.generateGridReport();
	}

	/** Create email test component for testing email configuration. Creates a button that opens an enhanced email test dialog. Called by CFormBuilder
	 * when building form from @AMetaData.
	 * @return Component for email configuration testing */
	public Component createComponentEmailTest() {
		try {
			LOGGER.debug("Creating email test component");
			// Get current settings entity
			final CSystemSettings<?> settings = getSystemSettings();
			if (settings == null) {
				LOGGER.warn("No system settings available for email test");
				return createErrorDiv("Settings not loaded");
			}
			// Create button that opens email test dialog (no emoji - VaadinIcon only)
			final Button buttonTestEmail = new Button("Test Email", VaadinIcon.ENVELOPE.create());
			buttonTestEmail.setId("custom-email-test-button");
			buttonTestEmail.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			buttonTestEmail.addClickListener(e -> showEmailTestDialog(settings));
			final HorizontalLayout layout = new HorizontalLayout(buttonTestEmail);
			layout.setSpacing(true);
			layout.setPadding(false);
			LOGGER.debug("Created email test component successfully");
			return layout;
		} catch (final Exception e) {
			LOGGER.error("Error creating email test component", e);
			CNotificationService.showException("Failed to create email test component", e);
			return createErrorDiv("Failed to create email test component: " + e.getMessage());
		}
	}

	/** Create LDAP test component for testing LDAP configuration. Creates a button that opens an enhanced LDAP test dialog. Called by CFormBuilder
	 * when building form from @AMetaData.
	 * @return Component for LDAP authentication testing */
	public Component createComponentLdapTest() {
		try {
			LOGGER.debug("Creating LDAP test component");
			final Button buttonTestLdap = new Button("Test LDAP", VaadinIcon.COG.create());
			buttonTestLdap.setId("custom-ldap-test-button");
			buttonTestLdap.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			buttonTestLdap.addClickListener(e -> showLdapTestDialog());
			final HorizontalLayout layout = new HorizontalLayout(buttonTestLdap);
			layout.setSpacing(true);
			layout.setPadding(false);
			LOGGER.debug("Created LDAP test component successfully");
			return layout;
		} catch (final Exception e) {
			LOGGER.error("Error creating LDAP test component", e);
			CNotificationService.showException("Failed to create LDAP test component", e);
			return createErrorDiv("Failed to create LDAP test component: " + e.getMessage());
		}
	}

	/** Create error div component for fallback display. */
	private Div createErrorDiv(final String message) {
		final Div errorDiv = new Div();
		errorDiv.setText(message);
		errorDiv.getStyle().set("color", "var(--lumo-error-color)");
		errorDiv.getStyle().set("background-color", "#ffebee");
		errorDiv.getStyle().set("border", "1px solid #f44336");
		errorDiv.getStyle().set("border-radius", "4px");
		errorDiv.getStyle().set("padding", "6px");
		return errorDiv;
	}

	/** Get system settings instance for the specific profile implementation. Each subclass (BAB/Derbent) implements this to return their specific
	 * settings. */
	protected abstract CSystemSettings<?> getSystemSettings();

	/** Creates and shows email test dialog with enhanced UI using CDialog base class. */
	private void showEmailTestDialog(final CSystemSettings<?> settings) {
		try {
			final CEmailTestDialog dialog = new CEmailTestDialog(settings);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Error creating email test dialog", e);
			CNotificationService.showException("Failed to create email test dialog", e);
		}
	}

	/** Creates and shows LDAP test dialog with enhanced UI using CDialog base class. */
	private void showLdapTestDialog() {
		try {
			final CSystemSettings<?> settings = getSystemSettings();
			final CLdapTestDialog dialog = new CLdapTestDialog(settings, ldapAuthenticator);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Error creating LDAP test dialog", e);
			CNotificationService.showException("Failed to create LDAP test dialog", e);
		}
	}
}
