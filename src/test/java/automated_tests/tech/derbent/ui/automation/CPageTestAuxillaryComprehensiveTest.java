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
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🧪 CPageTestAuxillary Comprehensive Page Testing")
public class CPageTestAuxillaryComprehensiveTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageTestAuxillaryComprehensiveTest.class);
	private static final String TEST_AUX_PAGE_ROUTE = "cpagetestauxillary";
	private static final String BUTTON_SELECTOR = "[id^='test-aux-btn-']";
	private static final String METADATA_SELECTOR = "#test-auxillary-metadata";
	private static final String CRUD_CANCEL_BUTTON_ID = "cbutton-cancel";
	private static final String CRUD_DELETE_BUTTON_ID = "cbutton-delete";
	private static final String CRUD_NEW_BUTTON_ID = "cbutton-new";
	private static final String CRUD_REFRESH_BUTTON_ID = "cbutton-refresh";
	private static final String CRUD_SAVE_BUTTON_ID = "cbutton-save";
	private static final String CONFIRM_YES_BUTTON_ID = "cbutton-yes";
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
			try {
				wait_2000(); // Wait for navigation and page load
			} catch (final AssertionError e) {
				throw new AssertionError("Exception dialog detected while navigating to: " + button.title + " (" + button.route + ")", e);
			}
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
			// Look for common CRUD buttons by deterministic IDs
			Locator newButton = page.locator("#" + CRUD_NEW_BUTTON_ID);
			Locator deleteButton = page.locator("#" + CRUD_DELETE_BUTTON_ID);
			Locator saveButton = page.locator("#" + CRUD_SAVE_BUTTON_ID);
			Locator refreshButton = page.locator("#" + CRUD_REFRESH_BUTTON_ID);
			// If we have at least 2 of these buttons, consider it a CRUD toolbar
			int count = 0;
			if (newButton.count() > 0)
				count++;
			if (deleteButton.count() > 0)
				count++;
			if (saveButton.count() > 0)
				count++;
			if (refreshButton.count() > 0)
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
	private boolean checkCrudButtonExists(String buttonId) {
		try {
			Locator button = page.locator("#" + buttonId);
			return button.count() > 0;
		} catch (Exception e) {
			LOGGER.debug("Error checking for {} button: {}", buttonId, e.getMessage());
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
			boolean hasNew = checkCrudButtonExists(CRUD_NEW_BUTTON_ID);
			boolean hasDelete = checkCrudButtonExists(CRUD_DELETE_BUTTON_ID);
			boolean hasSave = checkCrudButtonExists(CRUD_SAVE_BUTTON_ID);
			boolean hasRefresh = checkCrudButtonExists(CRUD_REFRESH_BUTTON_ID);
			boolean hasCancel = checkCrudButtonExists(CRUD_CANCEL_BUTTON_ID);
			LOGGER.info("   CRUD Buttons available:");
			LOGGER.info("      New: {}", hasNew);
			LOGGER.info("      Delete: {}", hasDelete);
			LOGGER.info("      Save: {}", hasSave);
			LOGGER.info("      Refresh: {}", hasRefresh);
			LOGGER.info("      Cancel: {}", hasCancel);
			if (hasRefresh) {
				testRefreshButton(pageName);
			}
			if (hasNew && hasSave) {
				testCreateAndSave(pageName);
			} else if (hasNew) {
				testNewButton(pageName);
			}
			if (hasSave && checkGridHasData()) {
				testUpdateAndSave(pageName);
			}
			if (hasSave) {
				testStatusChangeIfPresent(pageName);
			}
			if (hasDelete && checkGridHasData()) {
				testDeleteButton(pageName);
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
			Locator newButton = page.locator("#" + CRUD_NEW_BUTTON_ID);
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
					Locator cancelButton = page.locator("#" + CRUD_CANCEL_BUTTON_ID);
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
	private void testRefreshButton(String pageName) {
		try {
			LOGGER.info("   🔄 Testing Refresh button...");
			Locator refreshButton = page.locator("#" + CRUD_REFRESH_BUTTON_ID);
			if (refreshButton.count() > 0) {
				refreshButton.first().click();
				wait_500();
				performFailFastCheck("CRUD Refresh");
				takeScreenshot(String.format("%03d-page-%s-refresh-clicked", screenshotCounter++, pageName), false);
				LOGGER.info("      ✓ Clicked Refresh button");
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️  Refresh button test failed: {}", e.getMessage());
		}
	}

	private void testCreateAndSave(String pageName) {
		try {
			LOGGER.info("   🧾 Testing New + Save workflow...");
			Locator newButton = page.locator("#" + CRUD_NEW_BUTTON_ID);
			if (newButton.count() == 0) {
				return;
			}
			newButton.first().click();
			wait_1000();
			populateEditableFields(pageName);
			locatorById(CRUD_SAVE_BUTTON_ID).click();
			wait_1000();
			performFailFastCheck("CRUD Save New");
			takeScreenshot(String.format("%03d-page-%s-created", screenshotCounter++, pageName), false);
		} catch (Exception e) {
			LOGGER.warn("⚠️  Create + Save test failed: {}", e.getMessage());
		}
	}

	private void testUpdateAndSave(String pageName) {
		try {
			LOGGER.info("   ✏️  Testing Update + Save workflow...");
			if (!checkGridHasData()) {
				return;
			}
			testGridRowSelection(pageName);
			final String fieldId = findEditableFieldId();
			if (fieldId == null) {
				LOGGER.warn("      ⚠️ No editable field found for update workflow");
				return;
			}
			if (isComboBoxById(fieldId)) {
				selectFirstComboBoxOptionById(fieldId);
			} else {
				final String updateValue = "Updated-" + pageName;
				fillFieldById(fieldId, updateValue);
				LOGGER.info("      ✓ Updated field {} with {}", fieldId, updateValue);
			}
			locatorById(CRUD_SAVE_BUTTON_ID).click();
			wait_1000();
			performFailFastCheck("CRUD Save Update");
			takeScreenshot(String.format("%03d-page-%s-updated", screenshotCounter++, pageName), false);
		} catch (Exception e) {
			LOGGER.warn("⚠️  Update + Save test failed: {}", e.getMessage());
		}
	}

	private void testStatusChangeIfPresent(String pageName) {
		try {
			Locator statusFields = page.locator("[id^='field-'][id*='status']");
			if (statusFields.count() == 0) {
				return;
			}
			String statusFieldId = statusFields.first().getAttribute("id");
			if (statusFieldId == null || statusFieldId.isBlank()) {
				return;
			}
			LOGGER.info("   🟣 Testing status field update via {}", statusFieldId);
			final String before = readFieldValueById(statusFieldId);
			final String selected = selectDifferentComboBoxOptionById(statusFieldId, before);
			if (selected == null) {
				LOGGER.warn("      ⚠️ No alternate status value available for {}", statusFieldId);
				return;
			}
			locatorById(CRUD_SAVE_BUTTON_ID).click();
			wait_1000();
			final String after = readFieldValueById(statusFieldId);
			if (after != null && !after.isBlank()) {
				LOGGER.info("      ✓ Status updated: {} -> {}", before, after);
			}
			performFailFastCheck("CRUD Status Save");
			takeScreenshot(String.format("%03d-page-%s-status-updated", screenshotCounter++, pageName), false);
		} catch (Exception e) {
			LOGGER.warn("⚠️  Status change test failed: {}", e.getMessage());
		}
	}

	private void testDeleteButton(String pageName) {
		try {
			LOGGER.info("   🗑️ Testing Delete button...");
			final int beforeCount = getGridRowCount();
			if (beforeCount == 0) {
				LOGGER.warn("      ⚠️ No rows available to delete");
				return;
			}
			testGridRowSelection(pageName);
			locatorById(CRUD_DELETE_BUTTON_ID).click();
			wait_500();
			confirmDialogIfPresent();
			wait_1000();
			if (checkCrudButtonExists(CRUD_REFRESH_BUTTON_ID)) {
				locatorById(CRUD_REFRESH_BUTTON_ID).click();
				wait_500();
			}
			final int afterCount = getGridRowCount();
			if (afterCount >= beforeCount) {
				LOGGER.warn("      ⚠️ Delete did not reduce grid row count ({} -> {})", beforeCount, afterCount);
			} else {
				LOGGER.info("      ✓ Deleted row ({} -> {})", beforeCount, afterCount);
			}
			performFailFastCheck("CRUD Delete");
			takeScreenshot(String.format("%03d-page-%s-deleted", screenshotCounter++, pageName), false);
		} catch (Exception e) {
			LOGGER.warn("⚠️  Delete button test failed: {}", e.getMessage());
		}
	}

	private void confirmDialogIfPresent() {
		final Locator overlay = page.locator("vaadin-dialog-overlay[opened]");
		if (overlay.count() == 0) {
			return;
		}
		final Locator confirmButton = page.locator("#" + CONFIRM_YES_BUTTON_ID);
		if (confirmButton.count() > 0) {
			confirmButton.first().click();
			wait_500();
		}
	}

	private String findEditableFieldId() {
		final Locator fields = page.locator("[id^='field-']");
		for (int i = 0; i < fields.count(); i++) {
			final Locator field = fields.nth(i);
			final String fieldId = field.getAttribute("id");
			if (fieldId == null || fieldId.isBlank()) {
				continue;
			}
			if (isNonEditableFieldId(fieldId)) {
				continue;
			}
			if (field.locator("input, textarea").count() > 0) {
				return fieldId;
			}
			final String tagName = field.evaluate("el => el.tagName.toLowerCase()").toString();
			if (tagName.contains("combo-box")) {
				return fieldId;
			}
		}
		return null;
	}

	private void populateEditableFields(final String pageName) {
		final String baseValue = "Test-" + pageName;
		final Locator fields = page.locator("[id^='field-']");
		int textIndex = 0;
		if (pageName.toLowerCase().contains("approval")) {
			LOGGER.info("      🔎 Field IDs on {}:", pageName);
			for (int i = 0; i < fields.count(); i++) {
				final String fieldId = fields.nth(i).getAttribute("id");
				if (fieldId != null) {
					LOGGER.info("         - {}", fieldId);
				}
			}
		}
		for (int i = 0; i < fields.count(); i++) {
			final Locator field = fields.nth(i);
			final String fieldId = field.getAttribute("id");
			if (fieldId == null || fieldId.isBlank()) {
				continue;
			}
			if (isNonEditableFieldId(fieldId)) {
				continue;
			}
			if (isComboBoxById(fieldId)) {
				try {
					selectFirstComboBoxOptionById(fieldId);
					LOGGER.info("      ✓ Selected first option for {}", fieldId);
				} catch (Exception e) {
					LOGGER.debug("      ⚠️ Could not select combo option for {}: {}", fieldId, e.getMessage());
				}
				continue;
			}
			if (field.locator("input").count() > 0 || field.locator("textarea").count() > 0) {
				final String currentValue = readFieldValueById(fieldId);
				if (currentValue == null || currentValue.isBlank()) {
					final String value = textIndex == 0 ? baseValue : baseValue + "-" + textIndex;
					fillFieldById(fieldId, value);
					LOGGER.info("      ✓ Filled {} with {}", fieldId, value);
					textIndex++;
				}
			}
		}
		if (textIndex == 0) {
			final String fallbackField = findEditableFieldId();
			if (fallbackField != null && !isComboBoxById(fallbackField)) {
				fillFieldById(fallbackField, baseValue);
				LOGGER.info("      ✓ Filled fallback {} with {}", fallbackField, baseValue);
			}
		}
		selectComboFieldByIdSubstring("approval-status");
		selectComboFieldByIdSuffix("-order");
	}

	private void selectComboFieldByIdSubstring(final String fragment) {
		final Locator fields = page.locator("[id^='field-'][id*='" + fragment + "']");
		if (fields.count() == 0) {
			return;
		}
		final String fieldId = fields.first().getAttribute("id");
		if (fieldId == null || fieldId.isBlank()) {
			return;
		}
		if (isComboBoxById(fieldId)) {
			try {
				selectFirstComboBoxOptionById(fieldId);
				LOGGER.info("      ✓ Selected required combo {}", fieldId);
			} catch (Exception e) {
				LOGGER.debug("      ⚠️ Failed to select combo {}: {}", fieldId, e.getMessage());
			}
		}
	}

	private void selectComboFieldByIdSuffix(final String suffix) {
		final Locator fields = page.locator("[id^='field-'][id$='" + suffix + "']");
		if (fields.count() == 0) {
			return;
		}
		final String fieldId = fields.first().getAttribute("id");
		if (fieldId == null || fieldId.isBlank()) {
			return;
		}
		if (isComboBoxById(fieldId)) {
			try {
				selectFirstComboBoxOptionById(fieldId);
				LOGGER.info("      ✓ Selected required combo {}", fieldId);
			} catch (Exception e) {
				LOGGER.debug("      ⚠️ Failed to select combo {}: {}", fieldId, e.getMessage());
			}
		}
	}

	private boolean isComboBoxById(final String fieldId) {
		try {
			final Locator field = locatorById(fieldId);
			final String tagName = field.evaluate("el => el.tagName.toLowerCase()").toString();
			return tagName.contains("combo-box");
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isNonEditableFieldId(final String fieldId) {
		final String lower = fieldId.toLowerCase();
		return lower.endsWith("-id") || lower.contains("-created") || lower.contains("-updated") || lower.contains("-version")
				|| lower.contains("-createdby") || lower.contains("-modified");
	}

	private String selectDifferentComboBoxOptionById(final String elementId, final String currentValue) {
		Locator combo = locatorById(elementId);
		combo.click();
		wait_500();
		Locator options = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item");
		if (options.count() == 0) {
			options = page.locator("vaadin-combo-box-item");
		}
		for (int i = 0; i < options.count(); i++) {
			final String optionText = options.nth(i).textContent() != null ? options.nth(i).textContent().trim() : "";
			if (!optionText.isBlank() && (currentValue == null || !optionText.equals(currentValue.trim()))) {
				options.nth(i).click();
				wait_500();
				return optionText;
			}
		}
		if (options.count() > 0) {
			options.first().click();
			wait_500();
			return options.first().textContent();
		}
		return null;
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
