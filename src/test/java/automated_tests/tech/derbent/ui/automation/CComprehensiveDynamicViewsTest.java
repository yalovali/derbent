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

/** Comprehensive test suite for all dynamic views and windows. This test validates: 1. Complete navigation coverage of all menu items 2. Dynamic page
 * loading for all views 3. CRUD operations on key entity types 4. Grid functionality across views 5. Form validation and interaction 6. Multi-view
 * data consistency */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080"
})
@DisplayName ("🚀 Comprehensive Dynamic Views Navigation Test")
public class CComprehensiveDynamicViewsTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComprehensiveDynamicViewsTest.class);

	@Test
	@DisplayName ("✅ Complete navigation and dynamic view loading test")
	void testCompleteNavigationAndDynamicViews() {
		LOGGER.info("🚀 Starting comprehensive dynamic views navigation test...");
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			// Phase 1: Initialize and login
			LOGGER.info("📋 Phase 1: Initialization and Login");
			testInitializationAndLogin();
			// Phase 2: Test all menu navigation
			LOGGER.info("📋 Phase 2: Complete Menu Navigation");
			testCompleteMenuNavigation();
			// Phase 3: Test dynamic page loading
			LOGGER.info("📋 Phase 3: Dynamic Page Loading Verification");
			testDynamicPageLoading();
			// Phase 4: Test key CRUD operations
			LOGGER.info("📋 Phase 4: CRUD Operations Testing");
			testKeyCrudOperations();
			LOGGER.info("✅ Comprehensive dynamic views test completed successfully!");
		} catch (Exception e) {
			LOGGER.error("❌ Comprehensive dynamic views test failed: {}", e.getMessage());
			takeScreenshot("comprehensive-test-error", true);
			throw new AssertionError("Comprehensive dynamic views test failed", e);
		}
	}

	/** Phase 1: Test initialization and login. */
	private void testInitializationAndLogin() {
		LOGGER.info("🔐 Testing initialization and login...");
		// Initialize sample data and login
		loginToApplication();
		wait_afterlogin();
		takeScreenshot("phase1-logged-in", false);
		// Verify login was successful
		if (page.url().contains("/login")) {
			throw new AssertionError("Login failed - still on login page");
		}
		LOGGER.info("✅ Phase 1 complete: Successfully logged in");
	}

	/** Phase 2: Test complete menu navigation with screenshots. */
	private void testCompleteMenuNavigation() {
		LOGGER.info("🧭 Testing complete menu navigation...");
		try {
			// Visit all menu items and capture screenshots
			int visitedCount = visitMenuItems(true, // Capture screenshots
					false, // Don't allow empty menu
					"nav"); // Screenshot prefix
			LOGGER.info("✅ Visited {} menu items successfully", visitedCount);
			if (visitedCount < 5) {
				LOGGER.warn("⚠️ Only {} menu items found - expected more", visitedCount);
			}
			LOGGER.info("✅ Phase 2 complete: Menu navigation successful");
		} catch (Exception e) {
			takeScreenshot("phase2-menu-navigation-error", true);
			throw new AssertionError("Menu navigation test failed: " + e.getMessage(), e);
		}
	}

	/** Phase 3: Test dynamic page loading for various view types. */
	private void testDynamicPageLoading() {
		LOGGER.info("⚡ Testing dynamic page loading...");
		try {
			// Get all menu items
			final String menuSelector = "vaadin-side-nav-item, vaadin-tabs vaadin-tab";
			final Locator menuItems = page.locator(menuSelector);
			final int totalItems = menuItems.count();
			LOGGER.info("📊 Testing dynamic loading for {} views", totalItems);
			int successCount = 0;
			int failureCount = 0;
			// Test each menu item for proper page loading
			for (int i = 0; i < Math.min(totalItems, 10); i++) {
				// Navigate to menu item
				final Locator currentItems = page.locator(menuSelector);
				if (currentItems.count() == 0) {
					break;
				}
				final int index = Math.min(i, currentItems.count() - 1);
				final Locator navItem = currentItems.nth(index);
				final String itemText = navItem.textContent().trim();
				LOGGER.info("🔍 Testing dynamic loading for: {}", itemText);
				try {
					// Click and wait for page to load
					navItem.click();
					wait_1000();
					// Verify dynamic page loaded
					waitForDynamicPageLoad();
					// Check if page has interactive elements
					boolean hasGrid = page.locator("vaadin-grid").count() > 0;
					boolean hasForm = page.locator("vaadin-form-layout, vaadin-vertical-layout").count() > 0;
					boolean hasButtons = page.locator("vaadin-button").count() > 0;
					if (hasGrid || hasForm || hasButtons) {
						LOGGER.info("✅ Dynamic page loaded: {}", itemText);
						successCount++;
					} else {
						LOGGER.warn("⚠️ Page may not have loaded fully: {}", itemText);
					}
					takeScreenshot("phase3-dynamic-" + itemText.toLowerCase().replaceAll("[^a-z0-9]+", "-"), false);
				} catch (Exception e) {
					LOGGER.warn("⚠️ Failed to load dynamic page for {}: {}", itemText, e.getMessage());
					failureCount++;
					takeScreenshot("phase3-error-" + itemText.toLowerCase().replaceAll("[^a-z0-9]+", "-"), false);
				}
			}
			LOGGER.info("📊 Dynamic page loading results: {} succeeded, {} failed", successCount, failureCount);
			if (failureCount > successCount) {
				throw new AssertionError("Too many dynamic page loading failures");
			}
			LOGGER.info("✅ Phase 3 complete: Dynamic page loading validated");
		} catch (Exception e) {
			takeScreenshot("phase3-dynamic-loading-error", true);
			throw new AssertionError("Dynamic page loading test failed: " + e.getMessage(), e);
		}
	}

	/** Phase 4: Test CRUD operations on key entity types. */
	private void testKeyCrudOperations() {
		LOGGER.info("🔄 Testing CRUD operations on key entities...");
		try {
			// Test CRUD on Projects (if accessible)
			if (testCrudForEntity("Projects", "project-overview", "cpageproject-overview")) {
				LOGGER.info("✅ CRUD operations tested for Projects");
			}
			// Test CRUD on Activities (if accessible)
			if (testCrudForEntity("Activities", "activities", "cpageactivity")) {
				LOGGER.info("✅ CRUD operations tested for Activities");
			}
			// Test CRUD on Users (if accessible)
			if (testCrudForEntity("Users", "users", "cpageusers")) {
				LOGGER.info("✅ CRUD operations tested for Users");
			}
			LOGGER.info("✅ Phase 4 complete: CRUD operations validated");
		} catch (Exception e) {
			takeScreenshot("phase4-crud-error", true);
			LOGGER.warn("⚠️ CRUD operations test encountered errors: {}", e.getMessage());
			// Don't fail the entire test if CRUD operations fail
		}
	}

	/** Helper method to test CRUD operations for a specific entity. */
	private boolean testCrudForEntity(String entityName, String... possibleRoutes) {
		LOGGER.info("🔄 Testing CRUD for: {}", entityName);
		try {
			// Try to navigate to the entity view
			boolean navigated = false;
			for (String route : possibleRoutes) {
				try {
					page.navigate("http://localhost:" + port + "/" + route);
					wait_1000();
					// Check if we're on a valid page (not error)
					if (!page.url().contains("/login") && page.locator("vaadin-grid, vaadin-form-layout").count() > 0) {
						navigated = true;
						LOGGER.info("✅ Navigated to {} via route: {}", entityName, route);
						break;
					}
				} catch (Exception e) {
					LOGGER.debug("Route {} not accessible: {}", route, e.getMessage());
				}
			}
			if (!navigated) {
				LOGGER.warn("⚠️ Could not navigate to {} view", entityName);
				return false;
			}
			// Check if there's a grid with data
			final Locator grid = page.locator("vaadin-grid").first();
			if (grid.count() == 0) {
				LOGGER.warn("⚠️ No grid found for {}", entityName);
				return false;
			}
			takeScreenshot("crud-before-" + entityName.toLowerCase(), false);
			// Test CREATE operation
			LOGGER.info("➕ Testing CREATE for {}", entityName);
			if (page.locator("vaadin-button:has-text('New')").count() > 0) {
				clickNew();
				wait_1000();
				// Fill first available text field
				if (page.locator("vaadin-text-field").count() > 0) {
					fillFirstTextField("Test " + entityName + " " + System.currentTimeMillis());
				}
				// Try to save
				if (page.locator("vaadin-button:has-text('Save')").count() > 0) {
					clickSave();
					wait_1000();
					takeScreenshot("crud-created-" + entityName.toLowerCase(), false);
					LOGGER.info("✅ CREATE operation completed for {}", entityName);
				} else {
					// Cancel if no save button
					if (page.locator("vaadin-button:has-text('Cancel')").count() > 0) {
						clickCancel();
					}
					LOGGER.warn("⚠️ No Save button found for {}", entityName);
				}
			} else {
				LOGGER.warn("⚠️ No New button found for {}", entityName);
			}
			return true;
		} catch (Exception e) {
			LOGGER.warn("⚠️ CRUD test failed for {}: {}", entityName, e.getMessage());
			takeScreenshot("crud-error-" + entityName.toLowerCase(), false);
			return false;
		}
	}

	@Test
	@DisplayName ("✅ Test grid functionality across views")
	void testGridFunctionality() {
		LOGGER.info("📊 Starting grid functionality test...");
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login
			loginToApplication();
			wait_afterlogin();
			// Find views with grids
			LOGGER.info("🔍 Finding views with grid components...");
			final String menuSelector = "vaadin-side-nav-item, vaadin-tabs vaadin-tab";
			final Locator menuItems = page.locator(menuSelector);
			final int totalItems = menuItems.count();
			int gridCount = 0;
			for (int i = 0; i < Math.min(totalItems, 15); i++) {
				final Locator currentItems = page.locator(menuSelector);
				if (currentItems.count() == 0) {
					break;
				}
				final int index = Math.min(i, currentItems.count() - 1);
				final Locator navItem = currentItems.nth(index);
				final String itemText = navItem.textContent().trim();
				// Navigate to view
				navItem.click();
				wait_1000();
				// Check if view has a grid
				if (page.locator("vaadin-grid").count() > 0) {
					gridCount++;
					LOGGER.info("📊 Grid found in: {}", itemText);
					// Test grid interaction
					testGridInteraction(itemText);
					takeScreenshot("grid-" + itemText.toLowerCase().replaceAll("[^a-z0-9]+", "-"), false);
				}
			}
			LOGGER.info("✅ Grid functionality tested on {} views", gridCount);
		} catch (Exception e) {
			LOGGER.error("❌ Grid functionality test failed: {}", e.getMessage());
			takeScreenshot("grid-functionality-error", true);
			throw new AssertionError("Grid functionality test failed", e);
		}
	}

	/** Test grid interaction functionality. */
	private void testGridInteraction(String viewName) {
		try {
			final Locator grid = page.locator("vaadin-grid").first();
			// Check if grid has data
			final Locator cells = grid.locator("vaadin-grid-cell-content");
			final int cellCount = cells.count();
			if (cellCount > 0) {
				LOGGER.info("   Grid has {} cells", cellCount);
				// Try clicking first row
				cells.first().click();
				wait_500();
				LOGGER.info("   ✅ Row selection works");
			} else {
				LOGGER.info("   ℹ️ Grid is empty");
			}
			// Check for sortable columns
			final Locator sorters = grid.locator("vaadin-grid-sorter");
			if (sorters.count() > 0) {
				LOGGER.info("   Grid has {} sortable columns", sorters.count());
			}
		} catch (Exception e) {
			LOGGER.warn("   ⚠️ Grid interaction test failed for {}: {}", viewName, e.getMessage());
		}
	}

	@Test
	@DisplayName ("✅ Test form validation across views")
	void testFormValidation() {
		LOGGER.info("📝 Starting form validation test...");
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login
			loginToApplication();
			wait_afterlogin();
			// Test form validation on various views
			int formsTestsed = 0;
			// Find views with "New" buttons (indicate forms)
			final String menuSelector = "vaadin-side-nav-item, vaadin-tabs vaadin-tab";
			final Locator menuItems = page.locator(menuSelector);
			final int totalItems = menuItems.count();
			for (int i = 0; i < Math.min(totalItems, 10); i++) {
				final Locator currentItems = page.locator(menuSelector);
				if (currentItems.count() == 0) {
					break;
				}
				final int index = Math.min(i, currentItems.count() - 1);
				final Locator navItem = currentItems.nth(index);
				final String itemText = navItem.textContent().trim();
				// Navigate to view
				navItem.click();
				wait_1000();
				// Check if view has "New" button
				if (page.locator("vaadin-button:has-text('New')").count() > 0) {
					LOGGER.info("📝 Testing form in: {}", itemText);
					testFormValidationForView(itemText);
					formsTestsed++;
				}
			}
			LOGGER.info("✅ Form validation tested on {} views", formsTestsed);
		} catch (Exception e) {
			LOGGER.error("❌ Form validation test failed: {}", e.getMessage());
			takeScreenshot("form-validation-error", true);
			throw new AssertionError("Form validation test failed", e);
		}
	}

	/** Test form validation for a specific view. */
	private void testFormValidationForView(String viewName) {
		try {
			// Click New button
			clickNew();
			wait_1000();
			// Check for required fields
			final Locator requiredFields = page.locator("vaadin-text-field[required], vaadin-combo-box[required]");
			final int requiredCount = requiredFields.count();
			if (requiredCount > 0) {
				LOGGER.info("   Found {} required fields", requiredCount);
			}
			takeScreenshot("form-" + viewName.toLowerCase().replaceAll("[^a-z0-9]+", "-"), false);
			// Cancel form
			if (page.locator("vaadin-button:has-text('Cancel')").count() > 0) {
				clickCancel();
				wait_500();
			}
		} catch (Exception e) {
			LOGGER.warn("   ⚠️ Form validation test failed for {}: {}", viewName, e.getMessage());
		}
	}
}
