package tech.derbent.api.setup.dialogs;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.PasswordField;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.authentication.service.CLdapAuthenticator;
import tech.derbent.api.setup.domain.CSystemSettings;
import tech.derbent.api.setup.service.CPageServiceSystemSettings;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CTabSheet;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.ui.dialogs.CDialog;
import tech.derbent.api.ui.notifications.CNotificationService;

/**
 * Enhanced LDAP test dialog using CDialog base class and proper Derbent styling.
 * Features three tabs: Connection Test, User Authentication, and User Search.
 * 
 * Following AGENTS.md patterns:
 * - CDialog base class with proper styling
 * - CTabSheet for organized content
 * - Responsive layout with fixed dialog size
 * - Color-coded results with proper feedback
 */
public final class CLdapTestDialog extends CDialog {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CLdapTestDialog.class);
	private static final long serialVersionUID = 1L;
	
	// Dependencies
	private final CLdapAuthenticator ldapAuthenticator;
	
	// UI Components
	private CTabSheet tabSheet;
	private CButton buttonClose;
	
	// Tab content references for auto-trigger
	private CButton buttonTestConnection;
	private VerticalLayout connectionTab;
	private VerticalLayout authTab;
	private VerticalLayout userSearchTab;
	
	/**
	 * Constructor for LDAP test dialog.
	 * @param ldapAuthenticator the LDAP authenticator service
	 */
	public CLdapTestDialog(final CLdapAuthenticator ldapAuthenticator) throws Exception {
		super();
		this.ldapAuthenticator = ldapAuthenticator;
		setupDialog();
		
		// Auto-trigger connection test after dialog is rendered
		autoTriggerConnectionTest();
	}
	
	@Override
	public String getDialogTitleString() {
		return "LDAP Configuration Test";
	}
	
	@Override
	protected Icon getFormIcon() throws Exception {
		return VaadinIcon.COG.create();
	}
	
	@Override
	protected String getFormTitleString() {
		return "Test LDAP Configuration";
	}
	
	@Override
	protected void setupContent() throws Exception {
		// Set dialog dimensions for optimal UX
		setWidth("800px");
		setHeight("650px");
		setResizable(true);
		setDraggable(true);
		
		// Create tab sheet with enhanced styling
		tabSheet = new CTabSheet();
		tabSheet.setSizeFull();
		
		// Apply custom styling to make tabs more appealing
		tabSheet.getElement().getStyle()
			.set("--lumo-primary-color", "#1976D2")
			.set("--lumo-primary-text-color", "#1976D2")
			.set("border-radius", "8px")
			.set("background", "linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%)");
		
		// Create tabs with improved icons and styling
		connectionTab = createConnectionTestTab();
		final Tab connectionTabComponent = tabSheet.add("🔌 Connection Health", connectionTab);
		styleTab(connectionTabComponent);
		
		authTab = createAuthenticationTestTab();
		final Tab authTabComponent = tabSheet.add("🔐 User Authentication", authTab);
		styleTab(authTabComponent);
		
		userSearchTab = createUserSearchTab();
		final Tab userTabComponent = tabSheet.add("👥 User Search", userSearchTab);
		styleTab(userTabComponent);
		
		mainLayout.add(tabSheet);
	}
	
	@Override
	protected void setupButtons() {
		buttonClose = new CButton("Close", VaadinIcon.CLOSE.create());
		buttonClose.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		buttonClose.addClickListener(e -> close());
		buttonLayout.add(buttonClose);
	}
	
	/**
	 * Apply custom styling to tab components for better visual appeal.
	 */
	private void styleTab(final Tab tab) {
		tab.getElement().getStyle()
			.set("font-weight", "600")
			.set("padding", "8px 16px")
			.set("border-radius", "6px 6px 0 0");
	}
	
	/**
	 * Create connection test tab with current configuration display.
	 */
	private VerticalLayout createConnectionTestTab() {
		final VerticalLayout tab = new VerticalLayout();
		tab.setSizeFull();
		tab.setSpacing(true);
		tab.setPadding(false);
		
		// Current configuration section
		final CDiv configSection = createConfigurationDisplaySection();
		
		// Test button section
		final HorizontalLayout buttonSection = new HorizontalLayout();
		buttonSection.setSpacing(true);
		buttonSection.setAlignItems(HorizontalLayout.Alignment.CENTER);
		
		buttonTestConnection = new CButton("🔌 Test Connection", VaadinIcon.CONNECT.create());
		buttonTestConnection.setId("ldap-test-connection-btn");
		buttonTestConnection.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		
		final CButton buttonRefreshConfig = new CButton("🔄 Refresh Config", VaadinIcon.REFRESH.create());
		buttonRefreshConfig.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		buttonRefreshConfig.addClickListener(e -> refreshConfigurationDisplay(configSection));
		
		buttonSection.add(buttonTestConnection, buttonRefreshConfig);
		
		// Result area (initially hidden)
		final CDiv resultArea = new CDiv();
		resultArea.setId("ldap-connection-result");
		resultArea.setVisible(false);
		styleResultArea(resultArea);
		
		// Add click listener for connection test
		buttonTestConnection.addClickListener(e -> performConnectionTest(resultArea));
		
		tab.add(configSection, buttonSection, resultArea);
		return tab;
	}
	
	/**
	 * Create authentication test tab with username/password fields.
	 */
	private VerticalLayout createAuthenticationTestTab() {
		final VerticalLayout tab = new VerticalLayout();
		tab.setSizeFull();
		tab.setSpacing(true);
		tab.setPadding(false);
		
		// Info section
		final CDiv infoSection = new CDiv();
		infoSection.getStyle()
			.set("background", "linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%)")
			.set("border-radius", "8px")
			.set("padding", "16px")
			.set("border-left", "4px solid #1976d2");
		
		final H4 infoTitle = new H4("🔐 Test User Authentication");
		infoTitle.getStyle().set("margin", "0 0 8px 0").set("color", "#1565c0");
		
		final Span infoText = new Span("Enter user credentials to test authentication against LDAP server. " +
			"This verifies that user login works with current LDAP configuration.");
		infoText.getStyle().set("font-size", "0.9em").set("color", "#1976d2");
		
		infoSection.add(infoTitle, infoText);
		
		// Input fields
		final VerticalLayout formSection = new VerticalLayout();
		formSection.setSpacing(false);
		formSection.setPadding(false);
		formSection.getStyle().set("gap", "12px");
		
		final CTextField usernameField = new CTextField("Username");
		usernameField.setPlaceholder("Enter username (e.g. john.doe)");
		usernameField.setWidthFull();
		
		final PasswordField passwordField = new PasswordField("Password");
		passwordField.setPlaceholder("Enter password");
		passwordField.setWidthFull();
		
		formSection.add(usernameField, passwordField);
		
		// Test button
		final HorizontalLayout buttonSection = new HorizontalLayout();
		final CButton buttonTestAuth = new CButton("🔐 Test Authentication", VaadinIcon.SHIELD.create());
		buttonTestAuth.setId("ldap-test-auth-btn");
		buttonTestAuth.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonSection.add(buttonTestAuth);
		
		// Result area
		final CDiv resultArea = new CDiv();
		resultArea.setId("ldap-auth-result");
		resultArea.setVisible(false);
		styleResultArea(resultArea);
		
		// Add click listener
		buttonTestAuth.addClickListener(e -> performAuthenticationTest(
			usernameField.getValue(), passwordField.getValue(), resultArea));
		
		tab.add(infoSection, formSection, buttonSection, resultArea);
		return tab;
	}
	
	/**
	 * Create user search tab for fetching LDAP users.
	 */
	private VerticalLayout createUserSearchTab() {
		final VerticalLayout tab = new VerticalLayout();
		tab.setSizeFull();
		tab.setSpacing(true);
		tab.setPadding(false);
		
		// Info section
		final CDiv infoSection = new CDiv();
		infoSection.getStyle()
			.set("background", "linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%)")
			.set("border-radius", "8px")
			.set("padding", "16px")
			.set("border-left", "4px solid #7b1fa2");
		
		final H4 infoTitle = new H4("👥 Search LDAP Users");
		infoTitle.getStyle().set("margin", "0 0 8px 0").set("color", "#6a1b9a");
		
		final Span infoText = new Span("Fetch user list from LDAP directory. This helps verify " +
			"that user search and directory browsing works correctly.");
		infoText.getStyle().set("font-size", "0.9em").set("color", "#7b1fa2");
		
		infoSection.add(infoTitle, infoText);
		
		// Search controls
		final HorizontalLayout searchSection = new HorizontalLayout();
		searchSection.setSpacing(true);
		searchSection.setAlignItems(HorizontalLayout.Alignment.END);
		
		final CTextField searchField = new CTextField("Search Filter (optional)");
		searchField.setPlaceholder("e.g. (sAMAccountName=john*)");
		searchField.setWidth("300px");
		
		final CButton buttonSearchUsers = new CButton("👥 Fetch Users", VaadinIcon.SEARCH.create());
		buttonSearchUsers.setId("ldap-search-users-btn");
		buttonSearchUsers.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		
		final CButton buttonClearResults = new CButton("🗑️ Clear", VaadinIcon.TRASH.create());
		buttonClearResults.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		
		searchSection.add(searchField, buttonSearchUsers, buttonClearResults);
		
		// Result area with user stats
		final CDiv resultArea = new CDiv();
		resultArea.setId("ldap-users-result");
		resultArea.setVisible(false);
		styleResultArea(resultArea);
		resultArea.getStyle().set("max-height", "250px").set("overflow-y", "auto");
		
		// Add click listeners
		buttonSearchUsers.addClickListener(e -> performUserSearch(searchField.getValue(), resultArea));
		buttonClearResults.addClickListener(e -> {
			resultArea.removeAll();
			resultArea.setVisible(false);
		});
		
		tab.add(infoSection, searchSection, resultArea);
		return tab;
	}
	
	/**
	 * Create configuration display section showing current LDAP settings.
	 */
	private CDiv createConfigurationDisplaySection() {
		final CDiv section = new CDiv();
		section.setId("ldap-config-display");
		
		section.getStyle()
			.set("background", "linear-gradient(135deg, #e8f5e8 0%, #c8e6c9 100%)")
			.set("border-radius", "8px")
			.set("padding", "16px")
			.set("border-left", "4px solid #4caf50");
		
		refreshConfigurationDisplay(section);
		return section;
	}
	
	/**
	 * Refresh configuration display with current system settings.
	 */
	private void refreshConfigurationDisplay(final CDiv section) {
		section.removeAll();
		
		try {
			final CSystemSettings<?> settings = getCurrentSystemSettings();
			
			final H4 title = new H4("📋 Current LDAP Configuration");
			title.getStyle().set("margin", "0 0 12px 0").set("color", "#388e3c");
			
			final VerticalLayout configLayout = new VerticalLayout();
			configLayout.setSpacing(false);
			configLayout.setPadding(false);
			configLayout.getStyle().set("gap", "4px");
			
			// Configuration items with improved styling
			addConfigItem(configLayout, "🌐 Server", settings.getLdapServerUrl(), "Not configured");
			addConfigItem(configLayout, "🏢 Base DN", settings.getLdapSearchBase(), "Not configured");
			addConfigItem(configLayout, "👤 Bind User", settings.getLdapBindDn(), "Anonymous");
			addConfigItem(configLayout, "📝 Version", settings.getLdapVersion() != null ? settings.getLdapVersion().toString() : null, "3 (default)");
			addConfigItem(configLayout, "🔒 SSL/TLS", Boolean.TRUE.equals(settings.getLdapUseSslTls()) ? "Enabled" : "Disabled", "Disabled");
			addConfigItem(configLayout, "🔍 User Filter", settings.getLdapUserFilter(), "sAMAccountName=%USERNAME%");
			
			section.add(title, configLayout);
			
		} catch (final Exception e) {
			LOGGER.error("Error refreshing LDAP configuration display", e);
			section.add(new Span("❌ Error loading configuration: " + e.getMessage()));
		}
	}
	
	/**
	 * Add a configuration item to the display layout.
	 */
	private void addConfigItem(final VerticalLayout layout, final String label, final String value, final String defaultValue) {
		final HorizontalLayout item = new HorizontalLayout();
		item.setSpacing(true);
		item.setAlignItems(HorizontalLayout.Alignment.CENTER);
		
		final Span labelSpan = new Span(label);
		labelSpan.getStyle().set("font-weight", "600").set("min-width", "120px").set("color", "#2e7d32");
		
		final Span valueSpan = new Span(value != null && !value.trim().isEmpty() ? value : defaultValue);
		valueSpan.getStyle()
			.set("font-family", "monospace")
			.set("font-size", "0.9em")
			.set("background", value != null && !value.trim().isEmpty() ? "#ffffff" : "#fff3e0")
			.set("padding", "2px 6px")
			.set("border-radius", "4px")
			.set("color", value != null && !value.trim().isEmpty() ? "#333" : "#f57c00");
		
		item.add(labelSpan, valueSpan);
		layout.add(item);
	}
	
	/**
	 * Style result areas with consistent appearance.
	 */
	private void styleResultArea(final CDiv area) {
		area.getStyle()
			.set("margin-top", "16px")
			.set("padding", "16px")
			.set("border-radius", "8px")
			.set("border", "1px solid #e0e0e0")
			.set("background", "#fafafa")
			.set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
	}
	
	/**
	 * Perform connection test and display results.
	 */
	private void performConnectionTest(final CDiv resultArea) {
		buttonTestConnection.setEnabled(false);
		buttonTestConnection.setText("🔄 Testing...");
		resultArea.removeAll();
		resultArea.setVisible(true);
		
		try {
			final CSystemSettings<?> settings = getCurrentSystemSettings();
			if (settings == null) {
				displayErrorResult(resultArea, "System settings not found");
				return;
			}
			
			final CLdapAuthenticator.CLdapTestResult result = ldapAuthenticator.testConnection(settings);
			displayTestResult(resultArea, result);
			
		} catch (final Exception e) {
			LOGGER.error("Connection test failed", e);
			displayErrorResult(resultArea, "Connection test failed: " + e.getMessage());
		} finally {
			buttonTestConnection.setEnabled(true);
			buttonTestConnection.setText("🔌 Test Connection");
		}
	}
	
	/**
	 * Perform authentication test and display results.
	 */
	private void performAuthenticationTest(final String username, final String password, final CDiv resultArea) {
		if (username == null || username.trim().isEmpty()) {
			CNotificationService.showError("Please enter a username");
			return;
		}
		if (password == null || password.trim().isEmpty()) {
			CNotificationService.showError("Please enter a password");
			return;
		}
		
		resultArea.removeAll();
		resultArea.setVisible(true);
		
		try {
			final CSystemSettings<?> settings = getCurrentSystemSettings();
			if (settings == null) {
				displayErrorResult(resultArea, "System settings not found");
				return;
			}
			
			final CLdapAuthenticator.CLdapTestResult result = ldapAuthenticator.testUserAuthentication(username, password, settings);
			displayTestResult(resultArea, result);
			
		} catch (final Exception e) {
			LOGGER.error("Authentication test failed", e);
			displayErrorResult(resultArea, "Authentication test failed: " + e.getMessage());
		}
	}
	
	/**
	 * Perform user search and display results.
	 */
	private void performUserSearch(final String searchFilter, final CDiv resultArea) {
		resultArea.removeAll();
		resultArea.setVisible(true);
		
		try {
			final CSystemSettings<?> settings = getCurrentSystemSettings();
			if (settings == null) {
				displayErrorResult(resultArea, "System settings not found");
				return;
			}
			
			final CLdapAuthenticator.CLdapTestResult result = ldapAuthenticator.fetchAllUsers(settings);
			displayTestResult(resultArea, result);
			
			// Display user list with stats
			if (result.isSuccess() && !result.getUserData().isEmpty()) {
				final List<String> users = result.getUserData();
				
				// Add user count info
				final Div statsDiv = new Div();
				statsDiv.getStyle()
					.set("background", "#e8f5e8")
					.set("padding", "8px")
					.set("border-radius", "4px")
					.set("margin", "8px 0")
					.set("font-weight", "600")
					.set("color", "#2e7d32");
				statsDiv.setText(String.format("📊 Found %d users in LDAP directory", users.size()));
				
				resultArea.add(statsDiv);
				
				// Add user list
				final VerticalLayout userList = new VerticalLayout();
				userList.setSpacing(true);
				userList.setPadding(false);
				userList.getStyle().set("gap", "4px");
				
				users.forEach(user -> {
					final Span userSpan = new Span("👤 " + user);
					userSpan.getStyle()
						.set("font-family", "monospace")
						.set("font-size", "0.9em")
						.set("background", "#ffffff")
						.set("padding", "4px 8px")
						.set("border-radius", "4px")
						.set("border", "1px solid #e0e0e0");
					userList.add(userSpan);
				});
				
				resultArea.add(userList);
			}
			
		} catch (final Exception e) {
			LOGGER.error("User search failed", e);
			displayErrorResult(resultArea, "User search failed: " + e.getMessage());
		}
	}
	
	/**
	 * Display test results with proper styling.
	 */
	private void displayTestResult(final CDiv resultArea, final CLdapAuthenticator.CLdapTestResult result) {
		if (result.isSuccess()) {
			// Success result
			final Div successDiv = new Div();
			successDiv.getStyle()
				.set("background", "linear-gradient(135deg, #e8f5e8 0%, #c8e6c9 100%)")
				.set("padding", "12px")
				.set("border-radius", "6px")
				.set("border-left", "4px solid #4caf50")
				.set("color", "#2e7d32")
				.set("font-weight", "600");
			successDiv.setText("✅ " + result.getMessage());
			
			resultArea.add(successDiv);
			
			// Add additional info if available
			if (result.getDetails() != null && !result.getDetails().isEmpty()) {
				final Div detailsDiv = new Div();
				detailsDiv.getStyle()
					.set("background", "#f9f9f9")
					.set("padding", "8px")
					.set("border-radius", "4px")
					.set("margin-top", "8px")
					.set("font-family", "monospace")
					.set("font-size", "0.9em");
				detailsDiv.setText(result.getDetails());
				resultArea.add(detailsDiv);
			}
		} else {
			displayErrorResult(resultArea, result.getMessage());
		}
	}
	
	/**
	 * Display error results with proper styling.
	 */
	private void displayErrorResult(final CDiv resultArea, final String errorMessage) {
		final Div errorDiv = new Div();
		errorDiv.getStyle()
			.set("background", "linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%)")
			.set("padding", "12px")
			.set("border-radius", "6px")
			.set("border-left", "4px solid #f44336")
			.set("color", "#c62828")
			.set("font-weight", "600");
		errorDiv.setText("❌ " + errorMessage);
		
		resultArea.add(errorDiv);
	}
	
	/**
	 * Get system settings from the current active page service.
	 * This method dynamically finds the appropriate system settings service based on active profile.
	 */
	private CSystemSettings<?> getCurrentSystemSettings() {
		try {
			// Try to get BAB settings first (if in BAB profile)
			try {
				final Object babService = CSpringContext.getBean("CPageServiceSystemSettings_Bab");
				if (babService instanceof CPageServiceSystemSettings) {
					// Use reflection to call getSystemSettings since it's protected
					final java.lang.reflect.Method method = babService.getClass().getDeclaredMethod("getSystemSettings");
					method.setAccessible(true);
					return (CSystemSettings<?>) method.invoke(babService);
				}
			} catch (final Exception e) {
				// BAB service not available, continue to try Derbent
			}
			
			// Try to get Derbent settings (if in Derbent profile)
			try {
				final Object derbentService = CSpringContext.getBean("CPageServiceSystemSettings_Derbent");
				if (derbentService instanceof CPageServiceSystemSettings) {
					// Use reflection to call getSystemSettings since it's protected
					final java.lang.reflect.Method method = derbentService.getClass().getDeclaredMethod("getSystemSettings");
					method.setAccessible(true);
					return (CSystemSettings<?>) method.invoke(derbentService);
				}
			} catch (final Exception e) {
				// Derbent service not available
			}
			
			LOGGER.warn("No system settings service found - using default values");
			return null;
			
		} catch (final Exception e) {
			LOGGER.error("Error getting system settings", e);
			return null;
		}
	}
	
	/**
	 * Auto-trigger connection test when dialog opens.
	 */
	private void autoTriggerConnectionTest() {
		UI.getCurrent().getPage().executeJs(
			"setTimeout(() => { " +
			"const testBtn = document.getElementById('ldap-test-connection-btn'); " +
			"if (testBtn) testBtn.click(); " +
			"}, 300);"
		);
	}
}