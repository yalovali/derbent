package tech.derbent.api.authentication.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.authentication.service.CLdapAuthenticator;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.setup.domain.CSystemSettings;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.notifications.CNotificationService;

/**
 * CComponentLdapTest - Component for testing LDAP authentication configuration.
 * 
 * Provides comprehensive LDAP testing functionality including:
 * - Connection testing
 * - User search/listing
 * - Authentication testing
 * 
 * Tests use the current system settings LDAP configuration.
 */
public class CComponentLdapTest extends CComponentBase<CSystemSettings<?>> {

	// Component IDs for testing
	public static final String ID_ROOT = "custom-ldap-test-component";
	public static final String ID_TEST_CONNECTION = "custom-ldap-test-connection";
	public static final String ID_FETCH_USERS = "custom-ldap-fetch-users";
	public static final String ID_TEST_AUTH = "custom-ldap-test-auth";
	public static final String ID_USERNAME = "custom-ldap-username";
	public static final String ID_PASSWORD = "custom-ldap-password";
	public static final String ID_RESULTS = "custom-ldap-results";

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentLdapTest.class);
	private static final long serialVersionUID = 1L;

	// Dependencies
	private final CLdapAuthenticator ldapAuthenticator;

	// UI Components
	private CButton buttonTestConnection;
	private CButton buttonFetchUsers;
	private CButton buttonTestAuth;
	private TextField textFieldUsername;
	private PasswordField passwordFieldPassword;
	private Pre preResults;
	private Div divStatus;

	/**
	 * Constructor for LDAP test component.
	 * @param ldapAuthenticator the LDAP authenticator service
	 */
	public CComponentLdapTest(final CLdapAuthenticator ldapAuthenticator) {
		this.ldapAuthenticator = ldapAuthenticator;
		initializeComponents();
	}

	protected void initializeComponents() {
		LOGGER.debug("Initializing LDAP test component");

		// Configure component
		setId(ID_ROOT);
		setSpacing(false);
		setPadding(false);
		getStyle().set("gap", "12px");

		// Create header
		final H4 header = new H4("LDAP Authentication Test");
		header.getStyle().set("margin", "0");
		add(header);

		// Create connection test section
		createConnectionTestSection();

		// Create user fetch section
		createUserFetchSection();

		// Create authentication test section
		createAuthTestSection();

		// Create results section
		createResultsSection();

		LOGGER.debug("LDAP test component initialized");
	}

	private void createConnectionTestSection() {
		final Div sectionHeader = new Div();
		sectionHeader.setText("1. Test LDAP Connection");
		sectionHeader.getStyle().set("font-weight", "bold");
		sectionHeader.getStyle().set("margin-top", "16px");

		buttonTestConnection = new CButton("Test Connection", VaadinIcon.CONNECT.create());
		buttonTestConnection.setId(ID_TEST_CONNECTION);
		buttonTestConnection.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonTestConnection.addClickListener(e -> onTestConnectionClicked());

		final Div description = new Div();
		description.setText("Tests basic connectivity to the LDAP server using current settings");
		description.getStyle().set("font-size", "0.875rem");
		description.getStyle().set("color", "var(--lumo-secondary-text-color)");

		add(sectionHeader, description, buttonTestConnection);
	}

	private void createUserFetchSection() {
		final Div sectionHeader = new Div();
		sectionHeader.setText("2. Fetch User List");
		sectionHeader.getStyle().set("font-weight", "bold");
		sectionHeader.getStyle().set("margin-top", "16px");

		buttonFetchUsers = new CButton("Fetch Users", VaadinIcon.USERS.create());
		buttonFetchUsers.setId(ID_FETCH_USERS);
		buttonFetchUsers.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		buttonFetchUsers.addClickListener(e -> onFetchUsersClicked());

		final Div description = new Div();
		description.setText("Retrieves list of users from LDAP directory for verification");
		description.getStyle().set("font-size", "0.875rem");
		description.getStyle().set("color", "var(--lumo-secondary-text-color)");

		add(sectionHeader, description, buttonFetchUsers);
	}

	private void createAuthTestSection() {
		final Div sectionHeader = new Div();
		sectionHeader.setText("3. Test User Authentication");
		sectionHeader.getStyle().set("font-weight", "bold");
		sectionHeader.getStyle().set("margin-top", "16px");

		final Div description = new Div();
		description.setText("Tests authentication with specific user credentials");
		description.getStyle().set("font-size", "0.875rem");
		description.getStyle().set("color", "var(--lumo-secondary-text-color)");

		textFieldUsername = new TextField("Username");
		textFieldUsername.setId(ID_USERNAME);
		textFieldUsername.setPlaceholder("Enter LDAP username");
		textFieldUsername.setWidthFull();

		passwordFieldPassword = new PasswordField("Password");
		passwordFieldPassword.setId(ID_PASSWORD);
		passwordFieldPassword.setPlaceholder("Enter password");
		passwordFieldPassword.setWidthFull();

		buttonTestAuth = new CButton("Test Authentication", VaadinIcon.KEY.create());
		buttonTestAuth.setId(ID_TEST_AUTH);
		buttonTestAuth.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
		buttonTestAuth.addClickListener(e -> onTestAuthClicked());

		final HorizontalLayout credentialsLayout = new HorizontalLayout();
		credentialsLayout.setWidthFull();
		credentialsLayout.add(textFieldUsername, passwordFieldPassword);

		add(sectionHeader, description, credentialsLayout, buttonTestAuth);
	}

	private void createResultsSection() {
		final Div sectionHeader = new Div();
		sectionHeader.setText("Test Results");
		sectionHeader.getStyle().set("font-weight", "bold");
		sectionHeader.getStyle().set("margin-top", "16px");

		divStatus = new Div();
		divStatus.getStyle().set("padding", "8px 12px");
		divStatus.getStyle().set("border-radius", "4px");
		divStatus.getStyle().set("margin-bottom", "8px");
		updateStatus("Ready for testing", false);

		preResults = new Pre();
		preResults.setId(ID_RESULTS);
		preResults.getStyle().set("background", "var(--lumo-contrast-5pct)");
		preResults.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
		preResults.getStyle().set("border-radius", "4px");
		preResults.getStyle().set("padding", "12px");
		preResults.getStyle().set("font-family", "monospace");
		preResults.getStyle().set("font-size", "0.875rem");
		preResults.getStyle().set("white-space", "pre-wrap");
		preResults.getStyle().set("max-height", "400px");
		preResults.getStyle().set("overflow-y", "auto");
		preResults.setText("No tests run yet. Click a button above to start testing.");

		add(sectionHeader, divStatus, preResults);
	}

	private void updateStatus(final String message, final boolean isError) {
		divStatus.removeAll();
		final Span statusSpan = new Span(isError ? "❌ " + message : "✅ " + message);
		statusSpan.getStyle().set("font-weight", "500");
		divStatus.add(statusSpan);

		if (isError) {
			divStatus.getStyle().set("background", "var(--lumo-error-color-10pct)");
			divStatus.getStyle().set("border", "1px solid var(--lumo-error-color-50pct)");
			divStatus.getStyle().set("color", "var(--lumo-error-text-color)");
		} else {
			divStatus.getStyle().set("background", "var(--lumo-success-color-10pct)");
			divStatus.getStyle().set("border", "1px solid var(--lumo-success-color-50pct)");
			divStatus.getStyle().set("color", "var(--lumo-success-text-color)");
		}
	}

	private void updateResults(final String results) {
		preResults.setText(results);
	}

	private void onTestConnectionClicked() {
		LOGGER.info("Testing LDAP connection...");
		try {
			buttonTestConnection.setEnabled(false);
			updateStatus("Testing connection...", false);

			final CSystemSettings<?> settings = getValue();
			if (settings == null) {
				updateStatus("No system settings available", true);
				updateResults("ERROR: System settings not available for testing");
				return;
			}

			final CLdapAuthenticator.CLdapTestResult result = ldapAuthenticator.testConnection(settings);

			// Update UI with results
			updateStatus(result.getMessage(), !result.isSuccess());

			final StringBuilder resultsText = new StringBuilder();
			resultsText.append("=== LDAP Connection Test ===\n");
			resultsText.append("Result: ").append(result.isSuccess() ? "SUCCESS" : "FAILURE").append("\n");
			resultsText.append("Message: ").append(result.getMessage()).append("\n");
			resultsText.append("Duration: ").append(result.getDurationMs()).append("ms\n");
			if (result.getDetails() != null) {
				resultsText.append("Details: ").append(result.getDetails()).append("\n");
			}
			resultsText.append("\n--- Configuration Used ---\n");
			resultsText.append("Server: ").append(settings.getLdapServerUrl()).append("\n");
			resultsText.append("Search Base: ").append(settings.getLdapSearchBase()).append("\n");
			resultsText.append("Bind DN: ").append(settings.getLdapBindDn()).append("\n");
			resultsText.append("User Filter: ").append(settings.getLdapUserFilter()).append("\n");
			resultsText.append("LDAP Version: ").append(settings.getLdapVersion()).append("\n");
			resultsText.append("Use SSL/TLS: ").append(settings.getLdapUseSslTls()).append("\n");

			updateResults(resultsText.toString());

			// Show notification
			if (result.isSuccess()) {
				CNotificationService.showSuccess("LDAP connection test successful");
			} else {
				CNotificationService.showError("LDAP connection test failed: " + result.getMessage());
			}

		} catch (final Exception e) {
			LOGGER.error("Error during LDAP connection test", e);
			updateStatus("Test failed with error: " + e.getMessage(), true);
			updateResults("ERROR: " + e.getMessage());
			CNotificationService.showException("LDAP connection test failed", e);
		} finally {
			buttonTestConnection.setEnabled(true);
		}
	}

	private void onFetchUsersClicked() {
		LOGGER.info("Fetching LDAP users...");
		try {
			buttonFetchUsers.setEnabled(false);
			updateStatus("Fetching users...", false);

			final CSystemSettings<?> settings = getValue();
			if (settings == null) {
				updateStatus("No system settings available", true);
				updateResults("ERROR: System settings not available for testing");
				return;
			}

			final CLdapAuthenticator.CLdapTestResult result = ldapAuthenticator.fetchAllUsers(settings);

			// Update UI with results
			updateStatus(result.getMessage(), !result.isSuccess());

			final StringBuilder resultsText = new StringBuilder();
			resultsText.append("=== LDAP User Fetch Test ===\n");
			resultsText.append("Result: ").append(result.isSuccess() ? "SUCCESS" : "FAILURE").append("\n");
			resultsText.append("Message: ").append(result.getMessage()).append("\n");
			resultsText.append("Duration: ").append(result.getDurationMs()).append("ms\n");
			if (result.getDetails() != null) {
				resultsText.append("Details: ").append(result.getDetails()).append("\n");
			}

			if (result.isSuccess() && !result.getUserData().isEmpty()) {
				resultsText.append("\n--- Found Users (").append(result.getUserData().size()).append(") ---\n");
				for (int i = 0; i < result.getUserData().size(); i++) {
					resultsText.append(String.format("%3d. %s\n", i + 1, result.getUserData().get(i)));
				}
			}

			resultsText.append("\n--- Configuration Used ---\n");
			resultsText.append("Server: ").append(settings.getLdapServerUrl()).append("\n");
			resultsText.append("Search Base: ").append(settings.getLdapSearchBase()).append("\n");
			resultsText.append("Bind DN: ").append(settings.getLdapBindDn()).append("\n");

			updateResults(resultsText.toString());

			// Show notification
			if (result.isSuccess()) {
				CNotificationService.showSuccess("Found " + result.getUserData().size() + " LDAP users");
			} else {
				CNotificationService.showError("LDAP user fetch failed: " + result.getMessage());
			}

		} catch (final Exception e) {
			LOGGER.error("Error during LDAP user fetch", e);
			updateStatus("Fetch failed with error: " + e.getMessage(), true);
			updateResults("ERROR: " + e.getMessage());
			CNotificationService.showException("LDAP user fetch failed", e);
		} finally {
			buttonFetchUsers.setEnabled(true);
		}
	}

	private void onTestAuthClicked() {
		final String username = textFieldUsername.getValue();
		final String password = passwordFieldPassword.getValue();

		if (username == null || username.trim().isEmpty()) {
			CNotificationService.showError("Please enter a username");
			textFieldUsername.focus();
			return;
		}

		if (password == null || password.trim().isEmpty()) {
			CNotificationService.showError("Please enter a password");
			passwordFieldPassword.focus();
			return;
		}

		LOGGER.info("Testing LDAP authentication for user: {}", username);
		try {
			buttonTestAuth.setEnabled(false);
			updateStatus("Testing authentication...", false);

			final CSystemSettings<?> settings = getValue();
			if (settings == null) {
				updateStatus("No system settings available", true);
				updateResults("ERROR: System settings not available for testing");
				return;
			}

			final CLdapAuthenticator.CLdapTestResult result = ldapAuthenticator.testUserAuthentication(
				username.trim(), password, settings);

			// Update UI with results
			updateStatus(result.getMessage(), !result.isSuccess());

			final StringBuilder resultsText = new StringBuilder();
			resultsText.append("=== LDAP User Authentication Test ===\n");
			resultsText.append("Username: ").append(username).append("\n");
			resultsText.append("Result: ").append(result.isSuccess() ? "SUCCESS" : "FAILURE").append("\n");
			resultsText.append("Message: ").append(result.getMessage()).append("\n");
			resultsText.append("Duration: ").append(result.getDurationMs()).append("ms\n");
			if (result.getDetails() != null) {
				resultsText.append("Details: ").append(result.getDetails()).append("\n");
			}

			resultsText.append("\n--- Configuration Used ---\n");
			resultsText.append("Server: ").append(settings.getLdapServerUrl()).append("\n");
			resultsText.append("Search Base: ").append(settings.getLdapSearchBase()).append("\n");
			resultsText.append("User Filter: ").append(settings.getLdapUserFilter()).append("\n");

			updateResults(resultsText.toString());

			// Show notification
			if (result.isSuccess()) {
				CNotificationService.showSuccess("Authentication successful for user: " + username);
			} else {
				CNotificationService.showError("Authentication failed for user: " + username);
			}

		} catch (final Exception e) {
			LOGGER.error("Error during LDAP authentication test", e);
			updateStatus("Authentication test failed: " + e.getMessage(), true);
			updateResults("ERROR: " + e.getMessage());
			CNotificationService.showException("LDAP authentication test failed", e);
		} finally {
			buttonTestAuth.setEnabled(true);
			passwordFieldPassword.clear(); // Clear password for security
		}
	}

	@Override
	protected void onValueChanged(final CSystemSettings<?> oldValue, final CSystemSettings<?> newValue, final boolean fromClient) {
		// Component doesn't modify the settings, just uses them for testing
		// Value changes don't require UI updates
	}

	@Override
	protected void refreshComponent() {
		// Refresh functionality not needed for test component
		// Component operates independently using current settings
	}
}