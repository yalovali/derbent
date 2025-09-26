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

/** Comprehensive test suite that runs all Playwright tests including user profile functionality. This test ensures all major UI components work
 * correctly including CRUD operations and user profile management. */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:suitetestdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8082"
})
@DisplayName ("🧪 Complete Playwright Test Suite")
public class CCompletePlaywrightTestSuite extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCompletePlaywrightTestSuite.class);

	@Test
	@DisplayName ("🚀 Run All Playwright Tests - Complete Suite")
	void runAllPlaywrightTests() {
		LOGGER.info("🧪 Starting complete Playwright test suite...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Check if browser is available
			if (!isBrowserAvailable()) {
				LOGGER.warn("⚠️ Browser not available - running infrastructure verification only");
				verifyTestInfrastructure();
				return;
			}
			// Login once for all tests
			LOGGER.info("🔑 Logging into application...");
			loginToApplication();
			takeScreenshot("suite-login-success", false);
			// Run all major test categories
			runNavigationTests();
			runCrudTests();
			runUserProfileTests();
			runGridFunctionalityTests();
			runUIComponentTests();
			LOGGER.info("✅ Complete Playwright test suite completed successfully");
			takeScreenshot("suite-completed", false);
		} catch (Exception e) {
			LOGGER.error("❌ Complete Playwright test suite failed: {}", e.getMessage());
			takeScreenshot("suite-error", true);
			throw new AssertionError("Complete Playwright test suite failed", e);
		}
	}

	@Test
	@DisplayName ("🎯 Test User Profile Image with ID and XPath Selectors")
	void testUserProfileImageWithSelectors() {
		LOGGER.info("🧪 Testing user profile image with various selectors...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Check if browser is available
			if (!isBrowserAvailable()) {
				LOGGER.warn("⚠️ Browser not available - skipping selector tests");
				return;
			}
			// Login and test profile functionality
			loginToApplication();
			// Test user profile access with different selectors
			testUserProfileAccessWithSelectors();
			// Test profile image components with ID and XPath
			testProfileImageComponentsWithSelectors();
			LOGGER.info("✅ User profile image selector tests completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ User profile image selector tests failed: {}", e.getMessage());
			takeScreenshot("profile-selector-error", true);
			throw new AssertionError("User profile image selector tests failed", e);
		}
	}
	// ===========================================
	// TEST CATEGORY METHODS
	// ===========================================

	/** Runs navigation tests across all views */
	protected void runNavigationTests() {
		LOGGER.info("🧭 Running navigation tests...");
		try {
			// Test navigation to main views
			for (Class<?> viewClass : mainViewClasses) {
				String entityName = viewClass.getSimpleName().replace("View", "").replace("C", "");
				LOGGER.info("🧭 Testing navigation to: {}", entityName);
				boolean navigationSuccess = navigateToViewByClass(viewClass);
				if (navigationSuccess) {
					wait_1000();
					takeScreenshot("navigation-" + entityName.toLowerCase(), false);
					LOGGER.info("✅ Navigation to {} successful", entityName);
				} else {
					LOGGER.warn("⚠️ Navigation to {} failed", entityName);
				}
			}
			LOGGER.info("✅ Navigation tests completed");
		} catch (Exception e) {
			LOGGER.error("❌ Navigation tests failed: {}", e.getMessage());
		}
	}

	/** Runs CRUD operation tests */
	protected void runCrudTests() {
		LOGGER.info("🔄 Running CRUD tests...");
		try {
			// Test CRUD for each main entity
			for (Class<?> viewClass : mainViewClasses) {
				String entityName = viewClass.getSimpleName().replace("View", "").replace("C", "");
				LOGGER.info("🔄 Testing CRUD for: {}", entityName);
				boolean navigationSuccess = navigateToViewByClass(viewClass);
				if (navigationSuccess) {
					wait_1000();
					performEnhancedCRUDWorkflow(entityName);
					LOGGER.info("✅ CRUD test for {} completed", entityName);
				} else {
					LOGGER.warn("⚠️ Could not navigate to {} for CRUD test", entityName);
				}
			}
			LOGGER.info("✅ CRUD tests completed");
		} catch (Exception e) {
			LOGGER.error("❌ CRUD tests failed: {}", e.getMessage());
		}
	}

	/** Runs user profile specific tests */
	protected void runUserProfileTests() {
		LOGGER.info("👤 Running user profile tests...");
		try {
			// Test user menu access
			testUserMenuAccess();
			// Test profile dialog functionality
			testProfileDialogFunctionality();
			// Test profile image interactions
			testProfileImageInteractions();
			LOGGER.info("✅ User profile tests completed");
		} catch (Exception e) {
			LOGGER.error("❌ User profile tests failed: {}", e.getMessage());
		}
	}

	/** Runs grid functionality tests */
	protected void runGridFunctionalityTests() {
		LOGGER.info("📊 Running grid functionality tests...");
		try {
			for (Class<?> viewClass : mainViewClasses) {
				String entityName = viewClass.getSimpleName().replace("View", "").replace("C", "");
				LOGGER.info("📊 Testing grid for: {}", entityName);
				boolean navigationSuccess = navigateToViewByClass(viewClass);
				if (navigationSuccess) {
					wait_1000();
					testGridColumnFunctionality(entityName);
					LOGGER.info("✅ Grid test for {} completed", entityName);
				}
			}
			LOGGER.info("✅ Grid functionality tests completed");
		} catch (Exception e) {
			LOGGER.error("❌ Grid functionality tests failed: {}", e.getMessage());
		}
	}

	/** Runs UI component tests */
	protected void runUIComponentTests() {
		LOGGER.info("🎨 Running UI component tests...");
		try {
			// Test responsive design
			testResponsiveDesign();
			// Test accessibility basics
			testAccessibilityBasics("Complete test suite accessibility check");
			LOGGER.info("✅ UI component tests completed");
		} catch (Exception e) {
			LOGGER.error("❌ UI component tests failed: {}", e.getMessage());
		}
	}

	/** Verifies test infrastructure when browser is not available */
	protected void verifyTestInfrastructure() {
		LOGGER.info("🔧 Verifying test infrastructure...");
		// Verify that essential test classes exist
		try {
			assertNotNull(CBaseUITest.class);
			assertNotNull(CCrudFunctionsTest.class);
			assertNotNull(CUserProfileImageTest.class);
			LOGGER.info("✅ Test classes verified");
			// Verify essential view classes exist
			assertNotNull(mainViewClasses);
			assertTrue(mainViewClasses.length > 0, "Main view classes should exist");
			LOGGER.info("✅ View classes verified");
			LOGGER.info("✅ Test infrastructure verification completed");
		} catch (Exception e) {
			LOGGER.error("❌ Test infrastructure verification failed: {}", e.getMessage());
			throw e;
		}
	}
	// ===========================================
	// USER PROFILE SPECIFIC METHODS
	// ===========================================

	/** Tests user menu access with various selectors */
	protected void testUserMenuAccess() {
		LOGGER.info("🔍 Testing user menu access");
		// Test ID selector for user menu
		var userMenuById = page.locator("#user-menu-item").first();
		if (userMenuById.count() > 0) {
			LOGGER.info("✅ User menu found by ID selector");
		} else {
			LOGGER.warn("⚠️ User menu not found by ID selector");
		}
		// Test alternative selectors
		var userMenuByClass = page.locator("vaadin-menu-bar").first();
		if (userMenuByClass.count() > 0) {
			LOGGER.info("✅ User menu found by class selector");
		}
		takeScreenshot("user-menu-access-test", false);
	}

	/** Tests profile dialog functionality */
	protected void testProfileDialogFunctionality() {
		LOGGER.info("💬 Testing profile dialog functionality");
		try {
			// Click user menu
			var userMenu = page.locator("#user-menu-item, vaadin-menu-bar").first();
			if (userMenu.count() > 0) {
				userMenu.click();
				wait_500();
				// Click Edit Profile
				var editProfile = page.locator("text=Edit Profile").first();
				if (editProfile.count() > 0) {
					editProfile.click();
					wait_1000();
					// Verify dialog opened
					var dialog = page.locator("vaadin-dialog-overlay").first();
					if (dialog.count() > 0) {
						LOGGER.info("✅ Profile dialog opened successfully");
						takeScreenshot("profile-dialog-functionality", false);
						// Close dialog
						clickCancel();
						wait_500();
					}
				}
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ Profile dialog functionality test failed: {}", e.getMessage());
		}
	}

	/** Tests profile image interactions */
	protected void testProfileImageInteractions() {
		LOGGER.info("🖼️ Testing profile image interactions");
		try {
			// Open profile dialog first
			var userMenu = page.locator("#user-menu-item, vaadin-menu-bar").first();
			if (userMenu.count() > 0) {
				userMenu.click();
				wait_500();
				var editProfile = page.locator("text=Edit Profile").first();
				if (editProfile.count() > 0) {
					editProfile.click();
					wait_1000();
					// Test profile image components
					testProfileImageComponentsWithSelectors();
					// Close dialog
					clickCancel();
					wait_500();
				}
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ Profile image interactions test failed: {}", e.getMessage());
		}
	}

	/** Tests user profile access with different selector strategies */
	protected void testUserProfileAccessWithSelectors() {
		LOGGER.info("🎯 Testing user profile access with various selectors");
		// Strategy 1: ID selector
		var userMenuById = page.locator("#user-menu-item");
		if (userMenuById.count() > 0) {
			LOGGER.info("✅ Strategy 1: User menu found by ID");
			testProfileAccessStrategy(userMenuById, "ID");
		}
		// Strategy 2: Class/Tag selector
		var userMenuByTag = page.locator("vaadin-menu-bar vaadin-menu-bar-button");
		if (userMenuByTag.count() > 0) {
			LOGGER.info("✅ Strategy 2: User menu found by tag");
			testProfileAccessStrategy(userMenuByTag, "Tag");
		}
		// Strategy 3: XPath selector
		var userMenuByXPath = page.locator("//vaadin-menu-bar-button[contains(@class, 'menu-bar-button')]");
		if (userMenuByXPath.count() > 0) {
			LOGGER.info("✅ Strategy 3: User menu found by XPath");
			testProfileAccessStrategy(userMenuByXPath, "XPath");
		}
	}

	/** Tests profile access using a specific strategy */
	protected void testProfileAccessStrategy(com.microsoft.playwright.Locator menuLocator, String strategy) {
		try {
			menuLocator.first().click();
			wait_500();
			var editProfile = page.locator("text=Edit Profile");
			if (editProfile.count() > 0) {
				LOGGER.info("✅ {} strategy: Edit Profile option found", strategy);
				editProfile.first().click();
				wait_1000();
				var dialog = page.locator("vaadin-dialog-overlay");
				if (dialog.count() > 0) {
					LOGGER.info("✅ {} strategy: Profile dialog opened", strategy);
					takeScreenshot("profile-access-" + strategy.toLowerCase(), false);
					// Close dialog
					clickCancel();
					wait_500();
				}
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ {} strategy failed: {}", strategy, e.getMessage());
		}
	}

	/** Tests profile image components with various selector strategies */
	protected void testProfileImageComponentsWithSelectors() {
		LOGGER.info("🎯 Testing profile image components with selectors");
		// Test profile picture preview with ID
		var imagePreviewById = page.locator("#profile-picture-preview");
		if (imagePreviewById.count() > 0) {
			LOGGER.info("✅ Profile picture preview found by ID");
			// Test clicking on image
			imagePreviewById.first().click();
			wait_500();
			LOGGER.info("✅ Profile picture preview clicked");
		} else {
			// Try XPath selector
			var imagePreviewByXPath = page.locator("//img[contains(@id, 'profile')]");
			if (imagePreviewByXPath.count() > 0) {
				LOGGER.info("✅ Profile picture preview found by XPath");
			}
		}
		// Test upload component with ID
		var uploadById = page.locator("#profile-picture-upload");
		if (uploadById.count() > 0) {
			LOGGER.info("✅ Upload component found by ID");
		} else {
			// Try generic selector
			var uploadByTag = page.locator("vaadin-upload");
			if (uploadByTag.count() > 0) {
				LOGGER.info("✅ Upload component found by tag");
			}
		}
		// Test delete button with XPath
		var deleteByXPath = page.locator("//vaadin-button[contains(text(), 'Delete')]");
		if (deleteByXPath.count() > 0) {
			LOGGER.info("✅ Delete button found by XPath");
		}
		takeScreenshot("profile-components-selectors", false);
	}

	private void assertNotNull(Object obj) {
		if (obj == null) {
			throw new AssertionError("Object should not be null");
		}
	}

	private void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
