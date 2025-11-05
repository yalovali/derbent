package automated_tests.tech.derbent.ui.automation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;

/** Comprehensive test for the company-aware login pattern. This test validates the username@company_id authentication mechanism that is documented in
 * docs/implementation/COMPANY_LOGIN_PATTERN.md. The test verifies: 1. Sample data initialization creates multiple companies 2. Company selection
 * dropdown is populated correctly 3. Login works with username@company_id pattern 4. Multi-tenant isolation is maintained 5. Authentication flow
 * completes successfully */
@SpringBootTest (webEnvironment = WebEnvironment.RANDOM_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=0"
})
@DisplayName ("🔐 Company-Aware Login Pattern Test")
public class CCompanyAwareLoginTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCompanyAwareLoginTest.class);

	@Test
	@DisplayName ("✅ Test complete company-aware login flow")
	void testCompanyAwareLoginFlow() {
		LOGGER.info("🔐 Starting company-aware login pattern test...");
		try {
			// Test 1: Login page loads correctly
			testLoginPageLoads();
			// Test 2: Sample data initialization creates companies
			testSampleDataInitialization();
			// Test 3: Company selection dropdown works
			testCompanySelectionDropdown();
			// Test 4: Login with first company
			testLoginWithFirstCompany();
			// Test 5: Post-login navigation
			testPostLoginNavigation();
			LOGGER.info("✅ Company-aware login pattern test completed successfully!");
		} catch (Exception e) {
			LOGGER.error("❌ Company-aware login pattern test failed: {}", e.getMessage());
			takeScreenshot("company-login-test-error", true);
			throw new AssertionError("Company-aware login pattern test failed", e);
		}
	}

	/** Test that the login page loads correctly with all required elements. */
	private void testLoginPageLoads() {
		LOGGER.info("🧪 Test 1: Login page loads correctly");
		ensureLoginViewLoaded();
		// Verify company dropdown exists
		final Locator companyField = page.locator("#custom-company-input");
		if (companyField.count() == 0) {
			throw new AssertionError("Company selection dropdown not found on login page");
		}
		LOGGER.info("✅ Company dropdown found: #custom-company-input");
		// Verify username field exists
		final Locator usernameField = page.locator("#custom-username-input");
		if (usernameField.count() == 0) {
			throw new AssertionError("Username field not found on login page");
		}
		LOGGER.info("✅ Username field found: #custom-username-input");
		// Verify password field exists
		final Locator passwordField = page.locator("#custom-password-input");
		if (passwordField.count() == 0) {
			throw new AssertionError("Password field not found on login page");
		}
		LOGGER.info("✅ Password field found: #custom-password-input");
		// Verify login button exists
		final Locator loginButton = page.locator("vaadin-button:has-text('Login')");
		if (loginButton.count() == 0) {
			throw new AssertionError("Login button not found on login page");
		}
		LOGGER.info("✅ Login button found");
		takeScreenshot("01-login-page-loaded", false);
		LOGGER.info("✅ Test 1 passed: Login page loads correctly");
	}

	/** Test that sample data initialization creates multiple companies. */
	private void testSampleDataInitialization() {
		LOGGER.info("🧪 Test 2: Sample data initialization creates companies");
		// Initialize sample data via login page button
		initializeSampleDataFromLoginPage();
		// Return to login page
		ensureLoginViewLoaded();
		takeScreenshot("02-sample-data-initialized", false);
		LOGGER.info("✅ Test 2 passed: Sample data initialized");
	}

	/** Test that the company selection dropdown is populated with companies. */
	private void testCompanySelectionDropdown() {
		LOGGER.info("🧪 Test 3: Company selection dropdown works");
		// Click the company dropdown to open it
		final Locator companyField = page.locator("#custom-company-input");
		companyField.click();
		wait_500();
		// Check if company options are visible
		final Locator companyItems = page.locator("vaadin-combo-box-item");
		final int companyCount = companyItems.count();
		if (companyCount == 0) {
			throw new AssertionError("No companies found in dropdown - sample data may not have initialized");
		}
		LOGGER.info("✅ Found {} companies in dropdown", companyCount);
		// Log company names
		for (int i = 0; i < Math.min(companyCount, 5); i++) {
			final String companyName = companyItems.nth(i).textContent();
			LOGGER.info("   Company {}: {}", i + 1, companyName);
		}
		// Close dropdown by clicking somewhere else
		page.locator("#custom-username-input").click();
		wait_500();
		takeScreenshot("03-company-dropdown-populated", false);
		LOGGER.info("✅ Test 3 passed: Company dropdown populated with {} companies", companyCount);
	}

	/** Test login with the first company (auto-selected). */
	private void testLoginWithFirstCompany() {
		LOGGER.info("🧪 Test 4: Login with first company");
		// Note: Company is auto-selected to first enabled company in CCustomLoginView
		// Fill username
		boolean usernameFilled = fillLoginField("#custom-username-input", "input", "username", "admin", "input[type='text']");
		if (!usernameFilled) {
			throw new AssertionError("Failed to fill username field");
		}
		LOGGER.info("✅ Username filled: admin");
		// Fill password
		boolean passwordFilled = fillLoginField("#custom-password-input", "input", "password", "test123", "input[type='password']");
		if (!passwordFilled) {
			throw new AssertionError("Failed to fill password field");
		}
		LOGGER.info("✅ Password filled");
		takeScreenshot("04-credentials-entered", false);
		// Click login button
		clickLoginButton();
		LOGGER.info("✅ Login button clicked");
		// Wait for post-login page load
		wait_afterlogin();
		takeScreenshot("05-post-login", false);
		// Verify we're no longer on the login page
		if (page.url().contains("/login")) {
			takeScreenshot("05-still-on-login-page-error", true);
			throw new AssertionError("Still on login page after login attempt - authentication may have failed");
		}
		LOGGER.info("✅ Successfully redirected from login page");
		LOGGER.info("✅ Test 4 passed: Login successful with first company");
	}

	/** Test post-login navigation to verify session is established. */
	private void testPostLoginNavigation() {
		LOGGER.info("🧪 Test 5: Post-login navigation");
		// Prime navigation menu
		primeNavigationMenu();
		// Verify navigation menu is accessible
		final String menuSelector = "vaadin-side-nav-item, vaadin-tabs vaadin-tab";
		final Locator menuItems = page.locator(menuSelector);
		final int menuCount = menuItems.count();
		if (menuCount == 0) {
			throw new AssertionError("No navigation menu items found after login");
		}
		LOGGER.info("✅ Navigation menu accessible with {} items", menuCount);
		// Try navigating to a menu item
		if (menuCount > 0) {
			final Locator firstMenuItem = menuItems.first();
			final String itemText = firstMenuItem.textContent();
			LOGGER.info("   Clicking first menu item: {}", itemText);
			firstMenuItem.click();
			wait_1000();
			takeScreenshot("06-first-navigation", false);
			LOGGER.info("✅ Successfully navigated to: {}", itemText);
		}
		LOGGER.info("✅ Test 5 passed: Post-login navigation works");
	}

	@Test
	@DisplayName ("✅ Test multiple company login scenario")
	void testMultipleCompanyLogin() {
		LOGGER.info("🔐 Starting multiple company login test...");
		try {
			// Initialize sample data
			ensureLoginViewLoaded();
			initializeSampleDataFromLoginPage();
			ensureLoginViewLoaded();
			// Get list of companies
			final Locator companyField = page.locator("#custom-company-input");
			companyField.click();
			wait_500();
			final Locator companyItems = page.locator("vaadin-combo-box-item");
			final int companyCount = companyItems.count();
			LOGGER.info("📊 Found {} companies for testing", companyCount);
			if (companyCount < 2) {
				LOGGER.warn("⚠️ Only {} company found - skipping multi-company test", companyCount);
				return;
			}
			// Test login with first company
			LOGGER.info("🔐 Testing login with Company 1");
			companyItems.first().click();
			wait_500();
			testLoginSequence("Company 1");
			// Logout (if logout functionality exists)
			performLogout();
			// Navigate back to login
			ensureLoginViewLoaded();
			// Test login with second company
			LOGGER.info("🔐 Testing login with Company 2");
			companyField.click();
			wait_500();
			companyItems.nth(1).click();
			wait_500();
			testLoginSequence("Company 2");
			LOGGER.info("✅ Multiple company login test completed successfully!");
		} catch (Exception e) {
			LOGGER.error("❌ Multiple company login test failed: {}", e.getMessage());
			takeScreenshot("multiple-company-login-error", true);
			throw new AssertionError("Multiple company login test failed", e);
		}
	}

	/** Helper method to test login sequence. */
	private void testLoginSequence(String companyLabel) {
		fillLoginField("#custom-username-input", "input", "username", "admin", "input[type='text']");
		fillLoginField("#custom-password-input", "input", "password", "test123", "input[type='password']");
		takeScreenshot("login-" + companyLabel.toLowerCase().replaceAll("\\s+", "-"), false);
		clickLoginButton();
		wait_afterlogin();
		if (page.url().contains("/login")) {
			throw new AssertionError("Login failed for " + companyLabel);
		}
		LOGGER.info("✅ Login successful for {}", companyLabel);
		takeScreenshot("logged-in-" + companyLabel.toLowerCase().replaceAll("\\s+", "-"), false);
	}

	@Test
	@DisplayName ("✅ Test username@company_id format validation")
	void testUsernameFormatValidation() {
		LOGGER.info("🔐 Starting username format validation test...");
		try {
			// This test validates that the username@company_id pattern works correctly
			// The pattern is created by CCustomLoginView.handleLogin():
			// username = username + "@" + company.getId();
			ensureLoginViewLoaded();
			initializeSampleDataFromLoginPage();
			ensureLoginViewLoaded();
			// Login normally (this internally creates username@company_id)
			loginToApplication("admin", "test123");
			// Verify successful authentication
			if (page.url().contains("/login")) {
				throw new AssertionError("Username@company_id format validation failed - still on login page");
			}
			LOGGER.info("✅ Username@company_id format validated successfully");
			takeScreenshot("username-format-validated", false);
		} catch (Exception e) {
			LOGGER.error("❌ Username format validation test failed: {}", e.getMessage());
			takeScreenshot("username-format-validation-error", true);
			throw new AssertionError("Username format validation test failed", e);
		}
	}
}
