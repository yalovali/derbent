package tech.derbent.api.setup.dialogs;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.PasswordField;
import tech.derbent.api.authentication.service.CLdapAuthenticator;
import tech.derbent.api.setup.domain.CSystemSettings;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CTabSheet;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.ui.constants.CUIConstants;
import tech.derbent.api.ui.dialogs.CDialog;
import tech.derbent.api.ui.notifications.CNotificationService;

/** Enhanced LDAP test dialog using CDialog base class and proper Derbent styling. Features three tabs: Connection Test, User Authentication, and User
 * Search. Following AGENTS.md patterns: - CDialog base class with proper styling - CTabSheet for organized content - Responsive layout with fixed
 * dialog size - Color-coded results with proper feedback */
public final class CLdapTestDialog extends CDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(CLdapTestDialog.class);
	private static final long serialVersionUID = 1L;
	private CDiv authResultArea;
	private VerticalLayout authTab;
	private CButton buttonClose;
	private CButton buttonTestConnection;
	private VerticalLayout connectionTab;
	private final CLdapAuthenticator ldapAuthenticator;
	private PasswordField passwordField;
	// Dependencies
	private final CSystemSettings<?> settings;
	// UI Components
	private CTabSheet tabSheet;
	// Authentication tab fields
	private CTextField usernameField;
	private VerticalLayout userSearchTab;

	/** Constructor for LDAP test dialog.
	 * @param settings          the system settings with LDAP configuration
	 * @param ldapAuthenticator the LDAP authenticator service */
	public CLdapTestDialog(final CSystemSettings<?> settings, final CLdapAuthenticator ldapAuthenticator) throws Exception {
		this.settings = settings;
		this.ldapAuthenticator = ldapAuthenticator;
		setupDialog();
		// Auto-trigger connection test after dialog is rendered
		autoTriggerConnectionTest();
	}

	/** Add a configuration item to the display layout. */
	private void addConfigItem(final VerticalLayout layout, final String label, final String value, final String defaultValue) {
		final HorizontalLayout item = new HorizontalLayout();
		item.setSpacing(true);
		item.setAlignItems(HorizontalLayout.Alignment.CENTER);
		item.setWidthFull();
		final Span labelSpan = new Span(label + ":");
		labelSpan.getStyle().set("font-weight", CUIConstants.FONT_WEIGHT_SEMIBOLD).set("min-width", CUIConstants.LABEL_WIDTH_FORM)
				.set("color", CUIConstants.COLOR_SUCCESS_TEXT).set("flex-shrink", "0");
		final Span valueSpan = new Span(value != null && !value.trim().isEmpty() ? value : defaultValue);
		valueSpan.getStyle().set("font-family", "monospace").set("font-size", "0.9em")
				.set("background", value != null && !value.trim().isEmpty() ? CUIConstants.COLOR_WHITE : CUIConstants.COLOR_WARNING_BG)
				.set("padding", CUIConstants.PADDING_LABEL).set("border-radius", CUIConstants.BORDER_RADIUS_STANDARD)
				.set("color", value != null && !value.trim().isEmpty() ? CUIConstants.COLOR_GRAY_DARK : CUIConstants.COLOR_WARNING_TEXT)
				.set("word-break", "break-all").set("overflow-wrap", "anywhere").set("max-width", "100%");
		item.add(labelSpan, valueSpan);
		layout.add(item);
	}

	/** Auto-trigger connection test when dialog opens. */
	private void autoTriggerConnectionTest() {
		UI.getCurrent().getPage().executeJs("setTimeout(() => { " + "const testBtn = document.getElementById('ldap-test-connection-btn'); "
				+ "if (testBtn) testBtn.click(); " + "}, 300);");
	}

	/** Create authentication test tab with username/password fields. */
	private VerticalLayout createAuthenticationTestTab() {
		final VerticalLayout tab = new VerticalLayout();
		tab.setSizeFull();
		tab.setSpacing(false);
		tab.setPadding(false);
		tab.getStyle().set("gap", CUIConstants.GAP_EXTRA_TINY);
		// Info section
		final CDiv infoSection = createTextBannerSection("Enter user credentials to test authentication against LDAP server.",
				CUIConstants.COLOR_INFO_TEXT, CUIConstants.GRADIENT_INFO);
		// Input fields and button in single row
		final HorizontalLayout inputRow = new HorizontalLayout();
		inputRow.setWidthFull();
		inputRow.setSpacing(true);
		inputRow.setAlignItems(HorizontalLayout.Alignment.END);
		usernameField = new CTextField("Username");
		usernameField.setPlaceholder("Enter username");
		usernameField.setWidth(CUIConstants.FIELD_WIDTH_NARROW);
		passwordField = new PasswordField("Password");
		passwordField.setPlaceholder("Enter password");
		passwordField.setWidth(CUIConstants.FIELD_WIDTH_NARROW);
		final CButton buttonTestAuth = new CButton("Test Authentication", VaadinIcon.CHECK_CIRCLE.create());
		buttonTestAuth.setId("ldap-test-auth-btn");
		buttonTestAuth.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonTestAuth.addClickListener(e -> performAuthenticationTest());
		inputRow.add(usernameField, passwordField, buttonTestAuth);
		// Result area
		authResultArea = createScrollableResultArea("ldap-auth-result", CUIConstants.RESULT_AREA_MAX_HEIGHT);
		authResultArea.setVisible(false);
		tab.add(infoSection, inputRow, authResultArea);
		return tab;
	}

	/** Create configuration display section showing current LDAP settings. */
	private CDiv createConfigurationDisplaySection() {
		final CDiv section = new CDiv();
		section.setId("ldap-config-display");
		section.getStyle().set("background", CUIConstants.GRADIENT_SUCCESS).set("border-radius", CUIConstants.BORDER_RADIUS_MEDIUM)
				.set("padding", CUIConstants.PADDING_STANDARD)
				.set("border-left",
						CUIConstants.BORDER_WIDTH_ACCENT + " " + CUIConstants.BORDER_STYLE_SOLID + " " + CUIConstants.COLOR_SUCCESS_BORDER)
				.set("max-width", "100%").set("overflow-x", "hidden");
		refreshConfigurationDisplay(section);
		return section;
	}

	/** Create connection test tab with current configuration display. */
	private VerticalLayout createConnectionTestTab() {
		final VerticalLayout tab = new VerticalLayout();
		tab.setSizeFull();
		tab.setSpacing(false);
		tab.setPadding(false);
		tab.getStyle().set("gap", CUIConstants.GAP_EXTRA_TINY);
		// Current configuration section
		final CDiv configSection = createConfigurationDisplaySection();
		// Test button section
		final HorizontalLayout buttonSection = new HorizontalLayout();
		buttonSection.setSpacing(true);
		buttonTestConnection = new CButton("Test Connection", VaadinIcon.CONNECT.create());
		buttonTestConnection.setId("ldap-test-connection-btn");
		buttonTestConnection.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonSection.add(buttonTestConnection);
		// Result area (initially hidden)
		final CDiv resultArea = createScrollableResultArea("ldap-connection-result", CUIConstants.RESULT_AREA_MAX_HEIGHT);
		resultArea.setVisible(false);
		buttonTestConnection.addClickListener(e -> performConnectionTest(resultArea));
		tab.add(configSection, buttonSection, resultArea);
		return tab;
	}

	/** Create user search tab for fetching LDAP users. */
	private VerticalLayout createUserSearchTab() {
		final VerticalLayout tab = new VerticalLayout();
		tab.setSizeFull();
		tab.setSpacing(false);
		tab.setPadding(false);
		tab.getStyle().set("gap", CUIConstants.GAP_EXTRA_TINY);
		// Info section
		final CDiv infoSection = createTextBannerSection("Fetch user list from LDAP directory to verify search functionality.",
				CUIConstants.COLOR_PURPLE_ACCENT, CUIConstants.GRADIENT_PURPLE);
		// Search controls
		final HorizontalLayout searchSection = new HorizontalLayout();
		searchSection.setSpacing(true);
		searchSection.setAlignItems(HorizontalLayout.Alignment.END);
		final CTextField searchField = new CTextField("Search Filter (optional)");
		searchField.setPlaceholder("e.g. (sAMAccountName=john*)");
		searchField.setWidth(CUIConstants.FIELD_WIDTH_STANDARD);
		final CButton buttonSearchUsers = new CButton("Fetch Users", VaadinIcon.SEARCH.create());
		buttonSearchUsers.setId("ldap-search-users-btn");
		buttonSearchUsers.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		final CButton buttonClearResults = new CButton("Clear", VaadinIcon.TRASH.create());
		buttonClearResults.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		searchSection.add(searchField, buttonSearchUsers, buttonClearResults);
		// Result count area (above list)
		final CDiv resultCountArea = new CDiv();
		resultCountArea.setId("ldap-users-count");
		resultCountArea.setVisible(false);
		resultCountArea.getStyle().set("background", CUIConstants.COLOR_SUCCESS_BG).set("padding", CUIConstants.PADDING_SMALL)
				.set("border-radius", CUIConstants.BORDER_RADIUS_STANDARD).set("font-weight", CUIConstants.FONT_WEIGHT_SEMIBOLD)
				.set("color", "#2e7d32");
		// User list area with internal scrolling
		final CDiv userListArea = createScrollableResultArea("ldap-users-list", CUIConstants.GRID_HEIGHT_SHORT);
		userListArea.setVisible(false);
		userListArea.getStyle().set("background", "#fafafa").set("overflow-x", "hidden").set("padding", CUIConstants.PADDING_SMALL);
		// Add click listeners
		buttonSearchUsers.addClickListener(e -> performUserSearch(searchField.getValue(), resultCountArea, userListArea));
		buttonClearResults.addClickListener(e -> {
			resultCountArea.removeAll();
			resultCountArea.setVisible(false);
			userListArea.removeAll();
			userListArea.setVisible(false);
		});
		tab.add(infoSection, searchSection, resultCountArea, userListArea);
		return tab;
	}

	/** Display error results with proper styling. */
	private void displayErrorResult(final CDiv resultArea, final String errorMessage) {
		final CDiv errorDiv = createTextBannerSection("❌ " + errorMessage, CUIConstants.COLOR_ERROR_TEXT, CUIConstants.GRADIENT_ERROR);
		resultArea.add(errorDiv);
	}

	/** Display test results with proper styling. */
	private void displayTestResult(final CDiv resultArea, final CLdapAuthenticator.CLdapTestResult result) {
		if (result.isSuccess()) {
			// Success result
			final CDiv successDiv =
					createTextBannerSection("✅ " + result.getMessage(), CUIConstants.COLOR_SUCCESS_TEXT, CUIConstants.GRADIENT_SUCCESS);
			resultArea.add(successDiv);
			// Add additional info if available
			if (result.getDetails() != null && !result.getDetails().isEmpty()) {
				final CDiv detailsDiv = createTextBannerSection(result.getDetails(), CUIConstants.COLOR_GRAY_DARK, CUIConstants.COLOR_GRAY_LIGHT);
				detailsDiv.getStyle().set("font-family", "monospace").set("font-size", CUIConstants.FONT_SIZE_SMALL);
				resultArea.add(detailsDiv);
			}
		} else {
			displayErrorResult(resultArea, result.getMessage());
		}
	}

	@Override
	public String getDialogTitleString() { return "LDAP Configuration Test"; }

	@Override
	protected Icon getFormIcon() throws Exception { return VaadinIcon.COG.create(); }

	@Override
	protected String getFormTitleString() { return "Test LDAP Configuration"; }

	/** Perform authentication test and display results. */
	private void performAuthenticationTest() {
		try {
			final String username = usernameField.getValue();
			final String password = passwordField.getValue();
			if (username == null || username.trim().isEmpty()) {
				CNotificationService.showError("Please enter a username");
				return;
			}
			if (password == null || password.trim().isEmpty()) {
				CNotificationService.showError("Please enter a password");
				return;
			}
			authResultArea.removeAll();
			authResultArea.setVisible(true);
			// Use settings passed via constructor
			final CLdapAuthenticator.CLdapTestResult result = ldapAuthenticator.testUserAuthentication(username, password, settings);
			displayTestResult(authResultArea, result);
		} catch (final Exception e) {
			LOGGER.error("Authentication test failed", e);
			displayErrorResult(authResultArea, "Authentication test failed: " + e.getMessage());
		}
	}

	/** Perform connection test and display results. */
	private void performConnectionTest(final CDiv resultArea) {
		buttonTestConnection.setEnabled(false);
		buttonTestConnection.setText("Testing...");
		resultArea.removeAll();
		resultArea.setVisible(true);
		try {
			// Use settings passed via constructor
			final CLdapAuthenticator.CLdapTestResult result = ldapAuthenticator.testConnection(settings);
			displayTestResult(resultArea, result);
		} catch (final Exception e) {
			LOGGER.error("Connection test failed", e);
			displayErrorResult(resultArea, "Connection test failed: " + e.getMessage());
		} finally {
			buttonTestConnection.setEnabled(true);
			buttonTestConnection.setText("Test Connection");
		}
	}

	/** Perform user search and display results. */
	private void performUserSearch(final String filter, final CDiv resultCountArea, final CDiv userListArea) {
		resultCountArea.removeAll();
		resultCountArea.setVisible(false);
		userListArea.removeAll();
		userListArea.setVisible(false);
		try {
			// Use settings passed via constructor
			final CLdapAuthenticator.CLdapTestResult result = ldapAuthenticator.fetchAllUsers(settings);
			if (result.isSuccess() && !result.getUserData().isEmpty()) {
				List<String> users = result.getUserData();
				// Apply filter if provided
				if (filter != null && !filter.trim().isEmpty()) {
					final String lowerFilter = filter.toLowerCase();
					users = users.stream().filter(user -> user.toLowerCase().contains(lowerFilter)).toList();
				}
				// Show result count
				resultCountArea.setText("📊 Found %d users in LDAP directory".formatted(users.size()));
				resultCountArea.setVisible(true);
				// Add user list
				final VerticalLayout userList = new VerticalLayout();
				userList.setSpacing(false);
				userList.setPadding(false);
				userList.getStyle().set("gap", CUIConstants.GAP_TINY);
				users.stream().map(user -> new Span("👤 " + user)).forEach(userSpan -> {
					userSpan.getStyle().set("font-family", "monospace").set("font-size", "0.9em").set("background", "#ffffff")
							.set("padding", CUIConstants.PADDING_TINY + " " + CUIConstants.PADDING_SMALL)
							.set("border-radius", CUIConstants.BORDER_RADIUS_STANDARD)
							.set("border",
									CUIConstants.BORDER_WIDTH_STANDARD + " " + CUIConstants.BORDER_STYLE_SOLID + " " + CUIConstants.COLOR_GRAY_MEDIUM)
							.set("display", "block");
					userList.add(userSpan);
				});
				userListArea.add(userList);
				userListArea.setVisible(true);
			} else {
				displayErrorResult(resultCountArea, result.getMessage());
				resultCountArea.setVisible(true);
			}
		} catch (final Exception e) {
			LOGGER.error("User search failed", e);
			displayErrorResult(resultCountArea, "User search failed: " + e.getMessage());
			resultCountArea.setVisible(true);
		}
	}

	/** Refresh configuration display with current system settings. */
	private void refreshConfigurationDisplay(final CDiv section) {
		section.removeAll();
		try {
			// Use settings passed via constructor
			final VerticalLayout configLayout = new VerticalLayout();
			configLayout.setSpacing(false);
			configLayout.setPadding(false);
			configLayout.getStyle().set("gap", CUIConstants.GAP_TINY);
			// Configuration items without fancy icons
			addConfigItem(configLayout, "Server", settings.getLdapServerUrl(), "Not configured");
			addConfigItem(configLayout, "Base DN", settings.getLdapSearchBase(), "Not configured");
			addConfigItem(configLayout, "Bind User", settings.getLdapBindDn(), "Anonymous");
			addConfigItem(configLayout, "Version", settings.getLdapVersion() != null ? settings.getLdapVersion().toString() : null, "3 (default)");
			addConfigItem(configLayout, "SSL/TLS", Boolean.TRUE.equals(settings.getLdapUseSslTls()) ? "Enabled" : "Disabled", "Disabled");
			addConfigItem(configLayout, "User Filter", settings.getLdapUserFilter(), "sAMAccountName=%USERNAME%");
			section.add(configLayout);
		} catch (final Exception e) {
			LOGGER.error("Error refreshing LDAP configuration display", e);
			section.add(new Span("❌ Error loading configuration: " + e.getMessage()));
		}
	}

	@Override
	protected void setupButtons() {
		buttonClose = new CButton("Close", VaadinIcon.CLOSE.create());
		buttonClose.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		buttonClose.addClickListener(e -> close());
		buttonLayout.add(buttonClose);
	}

	@Override
	protected void setupContent() throws Exception {
		// Set dialog dimensions - no vertical scrollbar
		setWidth(CUIConstants.DIALOG_WIDTH_WIDE);
		setHeight(CUIConstants.DIALOG_HEIGHT_MEDIUM);
		setResizable(true);
		setDraggable(true);
		// Configure main layout to prevent scrolling
		mainLayout.setSizeFull();
		mainLayout.getStyle().set("overflow", "hidden");
		// Create tab sheet
		tabSheet = new CTabSheet();
		tabSheet.setSizeFull();
		tabSheet.getElement().getStyle().set("--lumo-primary-color", "#1976D2").set("--lumo-primary-text-color", "#1976D2");
		// Create tabs
		connectionTab = createConnectionTestTab();
		final Tab connectionTabComponent = tabSheet.add("Connection Health", connectionTab);
		styleTab(connectionTabComponent);
		authTab = createAuthenticationTestTab();
		final Tab authTabComponent = tabSheet.add("User Authentication", authTab);
		styleTab(authTabComponent);
		userSearchTab = createUserSearchTab();
		final Tab userTabComponent = tabSheet.add("User Search", userSearchTab);
		styleTab(userTabComponent);
		mainLayout.add(tabSheet);
	}

	/** Apply custom styling to tab components for better visual appeal. */
	private void styleTab(final Tab tab) {
		tab.getElement().getStyle().set("font-weight", CUIConstants.FONT_WEIGHT_SEMIBOLD)
				.set("padding", CUIConstants.PADDING_SMALL + " " + CUIConstants.PADDING_STANDARD)
				.set("border-radius", CUIConstants.GAP_EXTRA_TINY + " " + CUIConstants.GAP_EXTRA_TINY + " 0 0");
	}
}
