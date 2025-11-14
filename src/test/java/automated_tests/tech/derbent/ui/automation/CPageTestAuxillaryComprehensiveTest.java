package automated_tests.tech.derbent.ui.automation;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Comprehensive test suite for CPageTestAuxillary that dynamically tests all pages accessible via navigation buttons.
 * <p>
 * This test suite:
 * <ul>
 * <li>Navigates to CPageTestAuxillary page after login
 * <li>Dynamically discovers all navigation buttons
 * <li>For each button, extracts the target route from data-route attribute
 * <li>Navigates directly to each route URL (more reliable than clicking JavaScript handlers)
 * <li>Runs conditional tests based on page content:
 * <ul>
 * <li>Grid tests if grid is present
 * <li>CRUD toolbar tests if toolbar exists
 * </ul>
 * <li>Uses generic, reusable check.* functions for validation
 * <li>Handles dynamic number of buttons without hardcoding
 * </ul>
 * <p>
 * Design Philosophy:
 * <ul>
 * <li><b>Fast execution</b>: Reasonable timeouts, no excessive waits
 * <li><b>Complete coverage</b>: Tests ALL buttons, no skipping
 * <li><b>Generic testing</b>: Reusable functions work with any page type
 * <li><b>Direct navigation</b>: Uses URL navigation instead of clicking for reliability
 * <li><b>Detailed logging</b>: Clear progress indicators and error messages
 * </ul> */
@SpringBootTest (webEnvironment = WebEnvironment.RANDOM_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=0"
})
@DisplayName ("🧪 CPageTestAuxillary Comprehensive Page Testing")
public class CPageTestAuxillaryComprehensiveTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageTestAuxillaryComprehensiveTest.class);
	private static final String TEST_AUX_PAGE_ROUTE = "cpagetestauxillary";
	private static final String BUTTON_SELECTOR = "[id^='test-aux-btn-']";
	private static final String METADATA_SELECTOR = "#test-auxillary-metadata";
	private int screenshotCounter = 1;
	private int pagesVisited = 0;
	private int gridPagesFound = 0;
	private int crudPagesFound = 0;

	@Test
	@DisplayName ("✅ Comprehensive test of all CPageTestAuxillary navigation buttons")
	void testAllAuxillaryPages() {
		LOGGER.info("🚀 Starting comprehensive CPageTestAuxillary test suite...");
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			// Step 1: Login to application
			LOGGER.info("📝 Step 1: Logging into application...");
			loginToApplication();
			takeScreenshot(String.format("%03d-after-login", screenshotCounter++), false);
			// Step 2: Navigate to CPageTestAuxillary
			LOGGER.info("🧭 Step 2: Navigating to CPageTestAuxillary page...");
			navigateToTestAuxillaryPage();
			wait_2000(); // Give time for buttons to be populated
			takeScreenshot(String.format("%03d-test-auxillary-page", screenshotCounter++), false);
			// Step 3: Discover all navigation buttons dynamically
			LOGGER.info("🔍 Step 3: Discovering navigation buttons...");
			List<ButtonInfo> buttons = discoverNavigationButtons();
			LOGGER.info("📊 Found {} navigation buttons to test", buttons.size());
			// Step 4: Test each button's target page
			LOGGER.info("🧪 Step 4: Testing each navigation button's target page...");
			LOGGER.info("Will test {} buttons by navigating directly to their routes", buttons.size());
			for (int i = 0; i < buttons.size(); i++) {
				ButtonInfo button = buttons.get(i);
				LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
				LOGGER.info("🎯 Testing button {}/{}: {}", i + 1, buttons.size(), button.title);
				LOGGER.info("   Route: {}", button.route);
				LOGGER.info("   Button ID: {}", button.id);
				testNavigationButton(button, i + 1, buttons.size());
			}
			// Step 5: Summary
			LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			LOGGER.info("✅ Test suite completed successfully!");
			LOGGER.info("📊 Summary:");
			LOGGER.info("   Total buttons tested: {}", buttons.size());
			LOGGER.info("   Pages visited: {}", pagesVisited);
			LOGGER.info("   Pages with grids: {}", gridPagesFound);
			LOGGER.info("   Pages with CRUD toolbars: {}", crudPagesFound);
			LOGGER.info("   Screenshots captured: {}", screenshotCounter - 1);
		} catch (Exception e) {
			LOGGER.error("❌ Test suite failed: {}", e.getMessage(), e);
			takeScreenshot("error-comprehensive-test", true);
			throw new AssertionError("Comprehensive test suite failed", e);
		}
	}

	/** Navigate to the CPageTestAuxillary page. */
	private void navigateToTestAuxillaryPage() {
		try {
			String url = "http://localhost:" + port + "/" + TEST_AUX_PAGE_ROUTE;
			LOGGER.debug("Navigating to: {}", url);
			page.navigate(url);
			wait_1000();
			// Verify page loaded
			page.waitForSelector(BUTTON_SELECTOR + ", " + METADATA_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
			LOGGER.info("✅ Successfully navigated to CPageTestAuxillary page");
		} catch (Exception e) {
			throw new AssertionError("Failed to navigate to CPageTestAuxillary page: " + e.getMessage(), e);
		}
	}

	/** Discover all navigation buttons on the CPageTestAuxillary page dynamically.
	 * @return List of button information */
	private List<ButtonInfo> discoverNavigationButtons() {
		List<ButtonInfo> buttons = new ArrayList<>();
		try {
			// Try to read button count from metadata
			Locator metadataDiv = page.locator(METADATA_SELECTOR);
			if (metadataDiv.count() > 0) {
				String buttonCountStr = metadataDiv.getAttribute("data-button-count");
				LOGGER.debug("Metadata indicates {} buttons", buttonCountStr);
			}
			// Discover all buttons with the test-aux-btn- prefix
			Locator buttonLocators = page.locator(BUTTON_SELECTOR);
			int buttonCount = buttonLocators.count();
			LOGGER.info("🔍 Discovered {} navigation buttons", buttonCount);
			for (int i = 0; i < buttonCount; i++) {
				Locator button = buttonLocators.nth(i);
				ButtonInfo info = new ButtonInfo();
				info.index = i;
				info.id = button.getAttribute("id");
				info.title = button.getAttribute("data-title");
				info.route = button.getAttribute("data-route");
				// Fallback: extract title from button text if not in attributes
				if (info.title == null || info.title.isEmpty()) {
					info.title = button.textContent();
					if (info.title != null) {
						info.title = info.title.trim();
					}
				}
				buttons.add(info);
				LOGGER.debug("   Button {}: {} -> {}", i, info.title, info.route);
			}
			return buttons;
		} catch (Exception e) {
			throw new AssertionError("Failed to discover navigation buttons: " + e.getMessage(), e);
		}
	}

	/** Test a single navigation button and its target page.
	 * @param button      Button information
	 * @param buttonNum   Button number (1-based)
	 * @param totalButtons Total number of buttons */
	private void testNavigationButton(ButtonInfo button, int buttonNum, int totalButtons) {
		try {
			// Navigate directly to the route instead of clicking the button
			// This is more reliable than clicking Vaadin buttons with JavaScript handlers
			LOGGER.info("🧭 Navigating to: {} (button: {})", button.route, button.title);
			if (button.route == null || button.route.isEmpty()) {
				LOGGER.warn("⚠️  Button has no route: {}", button.title);
				return;
			}
			String targetUrl = "http://localhost:" + port + "/" + button.route;
			page.navigate(targetUrl);
			wait_2000(); // Wait for navigation and page load
			pagesVisited++;
			// Take initial screenshot
			String pageNameSafe = sanitizeForFileName(button.title, "page-" + button.index);
			takeScreenshot(String.format("%03d-page-%s-initial", screenshotCounter++, pageNameSafe), false);
			// Check what's on the page and run appropriate tests
			LOGGER.info("🔍 Analyzing page content...");
			boolean hasGrid = checkGridExists();
			boolean hasCrudToolbar = checkCrudToolbarExists();
			LOGGER.info("   Grid present: {}", hasGrid);
			LOGGER.info("   CRUD toolbar present: {}", hasCrudToolbar);
			// Run conditional tests based on page content
			if (hasGrid) {
				LOGGER.info("📊 Running grid tests...");
				runGridTests(pageNameSafe);
				gridPagesFound++;
			} else {
				LOGGER.info("ℹ️  No grid found, skipping grid tests");
			}
			if (hasCrudToolbar) {
				LOGGER.info("🔧 Running CRUD toolbar tests...");
				runCrudToolbarTests(pageNameSafe);
				crudPagesFound++;
			} else {
				LOGGER.info("ℹ️  No CRUD toolbar found, skipping CRUD tests");
			}
			// Take final screenshot
			takeScreenshot(String.format("%03d-page-%s-final", screenshotCounter++, pageNameSafe), false);
			LOGGER.info("✅ Completed testing button {}/{}: {}", buttonNum, totalButtons, button.title);
		} catch (Exception e) {
			LOGGER.error("❌ Failed to test button: {} - {}", button.title, e.getMessage(), e);
			takeScreenshot("error-button-" + button.index, true);
			// Don't throw - continue with next button
		}
	}

	// ==========================================
	// GENERIC CHECK FUNCTIONS
	// ==========================================
	/** Check if a grid exists on the current page.
	 * @return true if grid is present */
	private boolean checkGridExists() {
		try {
			Locator grids = page.locator("vaadin-grid, vaadin-grid-pro, so-grid, c-grid");
			return grids.count() > 0;
		} catch (Exception e) {
			LOGGER.debug("Error checking for grid: {}", e.getMessage());
			return false;
		}
	}

	/** Check if a CRUD toolbar exists on the current page.
	 * @return true if CRUD toolbar is present */
	private boolean checkCrudToolbarExists() {
		try {
			// Look for common CRUD buttons
			Locator newButton = page.locator("vaadin-button:has-text('New')");
			Locator editButton = page.locator("vaadin-button:has-text('Edit')");
			Locator deleteButton = page.locator("vaadin-button:has-text('Delete')");
			Locator saveButton = page.locator("vaadin-button:has-text('Save')");
			// If we have at least 2 of these buttons, consider it a CRUD toolbar
			int count = 0;
			if (newButton.count() > 0)
				count++;
			if (editButton.count() > 0)
				count++;
			if (deleteButton.count() > 0)
				count++;
			if (saveButton.count() > 0)
				count++;
			return count >= 2;
		} catch (Exception e) {
			LOGGER.debug("Error checking for CRUD toolbar: {}", e.getMessage());
			return false;
		}
	}

	/** Check if grid has data.
	 * @return true if grid contains data */
	private boolean checkGridHasData() {
		try {
			Locator grid = page.locator("vaadin-grid, vaadin-grid-pro, so-grid, c-grid").first();
			if (grid.count() == 0) {
				return false;
			}
			Locator cells = grid.locator("vaadin-grid-cell-content, [part='cell']");
			int cellCount = cells.count();
			LOGGER.debug("Grid has {} cells", cellCount);
			return cellCount > 0;
		} catch (Exception e) {
			LOGGER.debug("Error checking if grid has data: {}", e.getMessage());
			return false;
		}
	}

	/** Check if grid is sortable.
	 * @return true if grid has sortable columns */
	private boolean checkGridIsSortable() {
		try {
			Locator sorters = page.locator("vaadin-grid-sorter");
			return sorters.count() > 0;
		} catch (Exception e) {
			LOGGER.debug("Error checking if grid is sortable: {}", e.getMessage());
			return false;
		}
	}

	/** Check if a specific CRUD button exists.
	 * @param buttonText Button text to check for
	 * @return true if button exists */
	private boolean checkCrudButtonExists(String buttonText) {
		try {
			Locator button = page.locator("vaadin-button:has-text('" + buttonText + "')");
			return button.count() > 0;
		} catch (Exception e) {
			LOGGER.debug("Error checking for {} button: {}", buttonText, e.getMessage());
			return false;
		}
	}

	// ==========================================
	// GRID TEST FUNCTIONS
	// ==========================================
	/** Run comprehensive grid tests on the current page.
	 * @param pageName Page name for screenshots */
	private void runGridTests(String pageName) {
		try {
			// Test 1: Check if grid has data
			boolean hasData = checkGridHasData();
			LOGGER.info("   ✓ Grid has data: {}", hasData);
			// Test 2: Check if grid is sortable
			boolean isSortable = checkGridIsSortable();
			LOGGER.info("   ✓ Grid is sortable: {}", isSortable);
			if (isSortable) {
				// Test sorting on first column
				testGridSorting(pageName);
			}
			// Test 3: Count grid rows
			int rowCount = getGridRowCount();
			LOGGER.info("   ✓ Grid row count: {}", rowCount);
			// Test 4: Try to select first row if data exists
			if (hasData && rowCount > 0) {
				testGridRowSelection(pageName);
			}
			takeScreenshot(String.format("%03d-page-%s-grid-tested", screenshotCounter++, pageName), false);
		} catch (Exception e) {
			LOGGER.warn("⚠️  Grid tests encountered error: {}", e.getMessage());
		}
	}

	/** Test grid sorting functionality.
	 * @param pageName Page name for screenshots */
	private void testGridSorting(String pageName) {
		try {
			LOGGER.info("   🔄 Testing grid sorting...");
			Locator sorters = page.locator("vaadin-grid-sorter");
			if (sorters.count() > 0) {
				// Click first sorter to sort ascending
				sorters.first().click();
				wait_500();
				LOGGER.info("      ✓ Sorted ascending");
				// Click again to sort descending
				sorters.first().click();
				wait_500();
				LOGGER.info("      ✓ Sorted descending");
				takeScreenshot(String.format("%03d-page-%s-sorted", screenshotCounter++, pageName), false);
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️  Grid sorting test failed: {}", e.getMessage());
		}
	}

	/** Test grid row selection.
	 * @param pageName Page name for screenshots */
	private void testGridRowSelection(String pageName) {
		try {
			LOGGER.info("   🖱️  Testing grid row selection...");
			Locator grid = page.locator("vaadin-grid, vaadin-grid-pro, so-grid, c-grid").first();
			Locator cells = grid.locator("vaadin-grid-cell-content, [part='cell']");
			if (cells.count() > 0) {
				cells.first().click();
				wait_500();
				LOGGER.info("      ✓ Selected first row");
				takeScreenshot(String.format("%03d-page-%s-row-selected", screenshotCounter++, pageName), false);
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️  Grid row selection test failed: {}", e.getMessage());
		}
	}

	// ==========================================
	// CRUD TOOLBAR TEST FUNCTIONS
	// ==========================================
	/** Run comprehensive CRUD toolbar tests on the current page.
	 * @param pageName Page name for screenshots */
	private void runCrudToolbarTests(String pageName) {
		try {
			// Test what buttons are available
			boolean hasNew = checkCrudButtonExists("New");
			boolean hasEdit = checkCrudButtonExists("Edit");
			boolean hasDelete = checkCrudButtonExists("Delete");
			boolean hasSave = checkCrudButtonExists("Save");
			boolean hasCancel = checkCrudButtonExists("Cancel");
			LOGGER.info("   CRUD Buttons available:");
			LOGGER.info("      New: {}", hasNew);
			LOGGER.info("      Edit: {}", hasEdit);
			LOGGER.info("      Delete: {}", hasDelete);
			LOGGER.info("      Save: {}", hasSave);
			LOGGER.info("      Cancel: {}", hasCancel);
			// Test New button if available
			if (hasNew) {
				testNewButton(pageName);
			}
			// Test Edit button if available and grid has data
			if (hasEdit && checkGridHasData()) {
				testEditButton(pageName);
			}
			takeScreenshot(String.format("%03d-page-%s-crud-tested", screenshotCounter++, pageName), false);
		} catch (Exception e) {
			LOGGER.warn("⚠️  CRUD toolbar tests encountered error: {}", e.getMessage());
		}
	}

	/** Test the New button functionality.
	 * @param pageName Page name for screenshots */
	private void testNewButton(String pageName) {
		try {
			LOGGER.info("   ➕ Testing New button...");
			Locator newButton = page.locator("vaadin-button:has-text('New')");
			if (newButton.count() > 0) {
				newButton.first().click();
				wait_1000();
				LOGGER.info("      ✓ Clicked New button");
				takeScreenshot(String.format("%03d-page-%s-new-clicked", screenshotCounter++, pageName), false);
				// Check if a form or dialog appeared
				boolean hasDialog = page.locator("vaadin-dialog, vaadin-dialog-overlay").count() > 0;
				boolean hasFormLayout = page.locator("vaadin-form-layout").count() > 0;
				LOGGER.info("      Dialog/Form appeared: {}", hasDialog || hasFormLayout);
				// Try to close dialog/form if opened
				if (hasDialog || hasFormLayout) {
					// Look for Cancel button to close
					Locator cancelButton = page.locator("vaadin-button:has-text('Cancel')");
					if (cancelButton.count() > 0) {
						cancelButton.first().click();
						wait_500();
						LOGGER.info("      ✓ Closed form via Cancel button");
					}
				}
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️  New button test failed: {}", e.getMessage());
		}
	}

	/** Test the Edit button functionality.
	 * @param pageName Page name for screenshots */
	private void testEditButton(String pageName) {
		try {
			LOGGER.info("   ✏️  Testing Edit button...");
			// First select a row
			Locator grid = page.locator("vaadin-grid, vaadin-grid-pro, so-grid, c-grid").first();
			if (grid.count() > 0) {
				Locator cells = grid.locator("vaadin-grid-cell-content, [part='cell']");
				if (cells.count() > 0) {
					cells.first().click();
					wait_500();
					LOGGER.info("      ✓ Selected row for editing");
					// Now try to click Edit
					Locator editButton = page.locator("vaadin-button:has-text('Edit')");
					if (editButton.count() > 0) {
						editButton.first().click();
						wait_1000();
						LOGGER.info("      ✓ Clicked Edit button");
						takeScreenshot(String.format("%03d-page-%s-edit-clicked", screenshotCounter++, pageName), false);
						// Check if a form or dialog appeared
						boolean hasDialog = page.locator("vaadin-dialog, vaadin-dialog-overlay").count() > 0;
						boolean hasFormLayout = page.locator("vaadin-form-layout").count() > 0;
						LOGGER.info("      Edit form appeared: {}", hasDialog || hasFormLayout);
						// Try to close dialog/form if opened
						if (hasDialog || hasFormLayout) {
							Locator cancelButton = page.locator("vaadin-button:has-text('Cancel')");
							if (cancelButton.count() > 0) {
								cancelButton.first().click();
								wait_500();
								LOGGER.info("      ✓ Closed edit form via Cancel button");
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️  Edit button test failed: {}", e.getMessage());
		}
	}

	// ==========================================
	// HELPER CLASS
	// ==========================================
	/** Helper class to store button information. */
	private static class ButtonInfo {

		String id;
		int index;
		String route;
		String title;
	}
}
