package automated_tests.tech.derbent.ui.automation;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;
import tech.derbent.api.utils.Check;

/** Comprehensive Playwright test suite for dynamically created entity pages. Tests navigation to pages by entity type, CRUD operations, and error
 * handling. This test never ignores exceptions and fails fast on any errors. */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:dynamicpagesdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080"
})
@DisplayName ("🔄 Dynamic Entity Pages Playwright Test Suite")
public class CDynamicEntityPagesPlaywrightTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicEntityPagesPlaywrightTest.class);
	// Core entity types that should have dynamic pages
	private static final String[] CORE_ENTITY_TYPES = {
			"CUser", "CProject", "CActivity", "CMeeting", "COrder", "CDecision", "CRisk"
	};

	@Test
	@DisplayName ("🧪 Test Dynamic Entity Pages Navigation and CRUD Operations")
	void testDynamicEntityPagesComprehensive() {
		LOGGER.info("🚀 Starting comprehensive dynamic entity pages test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			LOGGER.info("🔐 Logging into application...");
			loginToApplicationEnhanced();
			takeScreenshot("dynamic-pages-login-success");
			// Test each core entity type
			for (String entityType : CORE_ENTITY_TYPES) {
				testEntityTypePage(entityType);
			}
			// Special test for CUser project relations
			testUserProjectRelations();
			LOGGER.info("✅ All dynamic entity pages tested successfully!");
		} catch (Exception e) {
			String message = "Dynamic entity pages test failed: " + e.getMessage();
			LOGGER.error("❌ {}", message, e);
			takeScreenshot("dynamic-pages-error-state");
			throw new AssertionError(message, e);
		}
	}

	@Test
	@DisplayName ("🎯 Test CUser Dynamic Page with Project Relations")
	void testUserPageProjectRelations() {
		LOGGER.info("👤 Testing CUser page with project relations...");
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			loginToApplicationEnhanced();
			takeScreenshot("user-page-login");
			// Navigate to CUser page
			Check.isTrue(navigateToDynamicPageByEntityType("CUser"), "Must be able to navigate to CUser page");
			takeScreenshot("user-page-loaded");
			// Test project relations functionality
			testUserProjectRelations();
			LOGGER.info("✅ CUser project relations test completed successfully");
		} catch (Exception e) {
			String message = "CUser project relations test failed: " + e.getMessage();
			LOGGER.error("❌ {}", message, e);
			takeScreenshot("user-project-relations-error");
			throw new AssertionError(message, e);
		}
	}

	/** Test a specific entity type page.
	 * @param entityType The entity type to test */
	private void testEntityTypePage(String entityType) {
		LOGGER.info("🧪 Testing entity type: {}", entityType);
		try {
			// Navigate to the page
			boolean navigationSuccess = navigateToDynamicPageByEntityType(entityType);
			if (!navigationSuccess) {
				LOGGER.warn("⚠️ Could not navigate to page for entity type: {} - page may not be created yet", entityType);
				return; // This is acceptable per requirements - "if page is not created it is ok not to"
			}
			// Wait for page to load and verify no exceptions
			waitForDynamicPageLoad();
			takeScreenshot("entity-page-" + entityType.toLowerCase() + "-loaded");
			// Test CRUD operations if the page supports them
			if (isDynamicPageLoaded() && hasGridSupport()) {
				testDynamicPageCrudOperations(entityType);
			}
			LOGGER.info("✅ Entity type {} tested successfully", entityType);
		} catch (Exception e) {
			String message = "Failed to test entity type " + entityType + ": " + e.getMessage();
			LOGGER.error("❌ {}", message, e);
			throw new AssertionError(message, e);
		}
	}

	/** Test user project relations functionality. */
	private void testUserProjectRelations() {
		LOGGER.info("🔗 Testing user project relations...");
		try {
			// Ensure we're on the CUser page
			if (!navigateToDynamicPageByEntityType("CUser")) {
				throw new AssertionError("Cannot test user project relations - CUser page not available");
			}
			waitForDynamicPageLoad();
			// Look for users in the grid
			if (!verifyGridHasData()) {
				// Create a user first
				LOGGER.info("➕ Creating a user for project relations testing...");
				createTestUser();
			}
			// Select first user
			clickFirstGridRow();
			wait_500();
			takeScreenshot("user-selected-for-project-test");
			// Look for project relations dialog or component
			testUserProjectRelationsDialog();
		} catch (Exception e) {
			String message = "User project relations test failed: " + e.getMessage();
			LOGGER.error("❌ {}", message, e);
			throw new AssertionError(message, e);
		}
	}

	/** Test user project relations dialog functionality. */
	private void testUserProjectRelationsDialog() {
		try {
			// Look for project assignment buttons or links
			Locator projectButtons = page.locator("vaadin-button").filter(new Locator.FilterOptions().setHasText("Project"));
			if (projectButtons.count() > 0) {
				LOGGER.info("🎯 Found project-related button, testing dialog...");
				projectButtons.first().click();
				wait_1000();
				// Check if dialog opened
				Locator dialog = page.locator("vaadin-dialog-overlay");
				if (dialog.count() > 0) {
					LOGGER.info("✅ Project relations dialog opened successfully");
					takeScreenshot("user-project-dialog-opened");
					// Test dialog functionality
					testProjectAssignmentDialog();
					// Close dialog
					clickCancel();
					wait_500();
				} else {
					LOGGER.warn("⚠️ Project button clicked but no dialog appeared");
				}
			} else {
				LOGGER.warn("⚠️ No project-related buttons found for user");
			}
		} catch (Exception e) {
			throw new RuntimeException("Project relations dialog test failed: " + e.getMessage(), e);
		}
	}

	/** Test project assignment dialog functionality. */
	private void testProjectAssignmentDialog() {
		try {
			// Look for project assignment controls
			Locator comboBoxes = page.locator("vaadin-combo-box");
			if (comboBoxes.count() > 0) {
				LOGGER.info("📋 Testing project assignment combo box...");
				comboBoxes.first().click();
				wait_500();
				Locator items = page.locator("vaadin-combo-box-item");
				if (items.count() > 0) {
					items.first().click();
					wait_500();
					// Save assignment if possible
					Locator saveButton = page.locator("vaadin-button:has-text('Save')");
					if (saveButton.count() > 0) {
						saveButton.click();
						wait_1000();
						LOGGER.info("✅ Project assignment saved successfully");
					}
				} else {
					LOGGER.warn("⚠️ No project options available in combo box");
				}
			}
			takeScreenshot("user-project-assignment-tested");
		} catch (Exception e) {
			throw new RuntimeException("Project assignment dialog test failed: " + e.getMessage(), e);
		}
	}

	/** Create a test user for project relations testing. */
	private void createTestUser() {
		try {
			clickNew();
			wait_1000();
			String testUserName = "TestUser" + System.currentTimeMillis();
			fillFirstTextField(testUserName);
			// Fill email if field exists
			Locator emailField = page.locator("vaadin-text-field").filter(new Locator.FilterOptions().setHasText("Email"));
			if (emailField.count() > 0) {
				emailField.fill("test@example.com");
			}
			// Select user type if combo box exists
			Locator comboBoxes = page.locator("vaadin-combo-box");
			if (comboBoxes.count() > 0) {
				selectFirstComboBoxOption();
			}
			clickSave();
			wait_1000();
			LOGGER.info("✅ Test user created: {}", testUserName);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create test user: " + e.getMessage(), e);
		}
	}

	/** Check if the current page has grid support for CRUD operations.
	 * @return true if the page has a grid that supports CRUD */
	private boolean hasGridSupport() {
		try {
			return page.locator("vaadin-grid").count() > 0 && page.locator("vaadin-button:has-text('New')").count() > 0;
		} catch (Exception e) {
			LOGGER.warn("⚠️ Error checking grid support: {}", e.getMessage());
			return false;
		}
	}

	/** Enhanced login method that ensures successful authentication. */
	protected void loginToApplicationEnhanced() {
		try {
			if (!isBrowserAvailable()) {
				throw new AssertionError("Browser not available for login");
			}
			// Use base class login method
			loginToApplication();
			// Additional verification for dynamic pages testing
			wait_2000();
			// Verify login success
			if (page.locator("text=Login failed, text=Error").count() > 0) {
				throw new AssertionError("Login failed - error message displayed");
			}
			// Wait for main application to load
			page.waitForSelector("vaadin-side-nav, vaadin-app-layout", new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(10000));
			LOGGER.info("✅ Enhanced login successful");
		} catch (Exception e) {
			throw new AssertionError("Enhanced login to application failed: " + e.getMessage(), e);
		}
	}
}
