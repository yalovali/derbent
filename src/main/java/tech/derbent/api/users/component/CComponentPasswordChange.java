package tech.derbent.api.users.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.interfaces.IComponentTransientPlaceHolder;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.constants.CUIConstants;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.users.dialogs.CDialogPasswordChange;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;

/** CComponentPasswordChange - Component for changing user passwords. This component follows the standard CComponentBase<CUser> pattern and provides:
 * - Button to open password change dialog - Context-aware display (shows user info and LDAP status) - LDAP user awareness (disabled button with
 * helpful message) - Integration with CFormBuilder via @AMetaData and setValue() binding */
public class CComponentPasswordChange extends CComponentBase<CUser> implements IPageServiceAutoRegistrable, IComponentTransientPlaceHolder<CUser> {

	public static final String ID_BUTTON = "custom-password-change-button";
	public static final String ID_ROOT = "custom-password-change-component";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentPasswordChange.class);
	private static final long serialVersionUID = 1L;
	// UI Components
	private CButton buttonChangePassword;
	private ISessionService sessionService;
	private Span statusSpan;

	/** Constructor for password change component. Following standard CComponentBase pattern - user is set via setValue() from form binder. */
	public CComponentPasswordChange() {
		initializeComponents();
	}

	private void ensureDependencies() {
		if (sessionService == null) {
			sessionService = CSpringContext.getBean(ISessionService.class);
		}
	}

	@Override
	public String getComponentName() { // TODO Auto-generated method stub
		return "componentPasswordChange";
	}

	private void initializeComponents() {
		LOGGER.debug("Initializing password change component");
		setId(ID_ROOT);
		setSpacing(false);
		setPadding(false);
		getStyle().set("gap", CUIConstants.GAP_TINY);
		// Header
		final H4 header = new H4("Password Management");
		header.getStyle().set("margin", "0");
		// Status information
		statusSpan = new Span();
		statusSpan.getStyle().set("font-size", "var(--lumo-font-size-s)");
		statusSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
		// Change password button
		buttonChangePassword = new CButton("Change Password", VaadinIcon.KEY.create());
		buttonChangePassword.setId(ID_BUTTON);
		buttonChangePassword.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonChangePassword.addClickListener(e -> on_buttonChangePassword_clicked());
		add(header, statusSpan, buttonChangePassword);
		LOGGER.debug("Password change component initialized successfully");
	}

	private void on_buttonChangePassword_clicked() {
		LOGGER.debug("Change password button clicked");
		try {
			ensureDependencies();
			if (getValue() == null) {
				CNotificationService.showError("No user context available");
				return;
			}
			if (getValue().isLDAPUser()) {
				CNotificationService.showWarning(
						"Password management is disabled for LDAP users. Please use your domain authentication system to change passwords.");
				return;
			}
			// Create and open password change dialog
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CDialogPasswordChange dialog = new CDialogPasswordChange(getValue(), userService);
			// Open dialog
			dialog.open();
			LOGGER.debug("Password change dialog opened for user: {}", getValue().getLogin());
		} catch (final Exception e) {
			LOGGER.error("Failed to open password change dialog", e);
			CNotificationService.showException("Failed to open password change dialog", e);
		}
	}

	@Override
	protected void refreshComponent() {
		if (getValue() == null) {
			updateUIWithError("No user available");
			return;
		}
		// Update status information based on user type
		final StringBuilder statusText = new StringBuilder();
		statusText.append("User: ").append(getValue().getName());
		if (getValue().isLDAPUser()) {
			statusText.append(" (LDAP User - Set password from domain systems)");
			statusSpan.getStyle().set("color", "var(--lumo-warning-text-color)");
			// Disable button for LDAP users
			buttonChangePassword.setEnabled(false);
			buttonChangePassword.getElement().setAttribute("title",
					"Password management disabled for LDAP users. Use your domain authentication system to change passwords.");
		} else {
			statusText.append(" (Local User - Password managed locally)");
			statusSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
			// Enable button for local users
			buttonChangePassword.setEnabled(true);
			buttonChangePassword.getElement().removeAttribute("title");
		}
		statusSpan.setText(statusText.toString());
		LOGGER.debug("UI updated for user: {} (LDAP: {})", getValue().getLogin(), getValue().isLDAPUser());
	}

	@Override
	public void setThis(CUser value) {
		setValue(value);
	}

	private void updateUIWithError(final String errorMessage) {
		statusSpan.setText(errorMessage);
		statusSpan.getStyle().set("color", "var(--lumo-error-text-color)");
		buttonChangePassword.setEnabled(false);
		buttonChangePassword.getElement().setAttribute("title", errorMessage);
	}
}
