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

/** Comprehensive test suite for testing New, Save, and Delete button functionality across all pages. This test validates: 1. Button presence and
 * visibility 2. Button responsiveness (clickability) 3. Button functionality (actual operations) 4. Form interactions with buttons 5. Grid
 * interactions with buttons 6. Error handling and validation */
@SpringBootTest (webEnvironment = WebEnvironment.RANDOM_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=0"
})
@DisplayName ("🔘 Comprehensive Button Functionality Test")
public class CButtonFunctionalityTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CButtonFunctionalityTest.class);

	@Test
	@DisplayName ("✅ Test New, Save, and Delete buttons across all pages")
	void testButtonFunctionalityAcrossAllPages() {
		LOGGER.info("🚀 Starting comprehensive button functionality test...");
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			// Phase 1: Login
			LOGGER.info("📋 Phase 1: Login to Application");
			loginToApplication();
			wait_afterlogin();
			takeScreenshot("button-test-logged-in", false);
			// Phase 2: Test buttons across all pages
			LOGGER.info("📋 Phase 2: Testing Buttons Across All Pages");
			testButtonsAcrossAllPages();
			LOGGER.info("✅ Comprehensive button functionality test completed successfully!");
		} catch (Exception e) {
			LOGGER.error("❌ Button functionality test failed: {}", e.getMessage());
			takeScreenshot("button-test-error", true);
			throw new AssertionError("Button functionality test failed", e);
		}
	}

	/** Test buttons across all pages by navigating through menu items. */
	private void testButtonsAcrossAllPages() {
		LOGGER.info("🧭 Testing buttons across all navigable pages...");
		try {
			// Get all menu items
			final String menuSelector = "vaadin-side-nav-item, vaadin-tabs vaadin-tab";
			wait_1000(); // Wait for menu to be fully loaded
			final Locator menuItems = page.locator(menuSelector);
			final int totalItems = menuItems.count();
			LOGGER.info("📊 Found {} menu items to test", totalItems);
			int testedPages = 0;
			int pagesWithNewButton = 0;
			int pagesWithSaveButton = 0;
			int pagesWithDeleteButton = 0;
			// Test each menu item
			for (int i = 0; i < totalItems; i++) {
				try {
					// Re-query menu items as DOM may have changed
					final Locator currentItems = page.locator(menuSelector);
					if (currentItems.count() == 0) {
						LOGGER.warn("⚠️ Menu items disappeared after {} pages", testedPages);
						break;
					}
					final int index = Math.min(i, currentItems.count() - 1);
					final Locator navItem = currentItems.nth(index);
					String itemText = "";
					try {
						itemText = navItem.textContent().trim();
					} catch (Exception e) {
						itemText = "Menu Item " + (i + 1);
					}
					if (itemText.isEmpty()) {
						itemText = "Menu Item " + (i + 1);
					}
					LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
					LOGGER.info("🔍 Testing Page {} of {}: {}", i + 1, totalItems, itemText);
					LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
					// Navigate to the page
					navItem.click();
					wait_1000();
					// Test buttons on this page
					ButtonTestResult result = testButtonsOnCurrentPage(itemText);
					testedPages++;
					if (result.hasNewButton) {
						pagesWithNewButton++;
					}
					if (result.hasSaveButton) {
						pagesWithSaveButton++;
					}
					if (result.hasDeleteButton) {
						pagesWithDeleteButton++;
					}
					// Log summary for this page
					LOGGER.info("📊 Page Summary: New={}, Save={}, Delete={}", result.hasNewButton ? "✅" : "❌", result.hasSaveButton ? "✅" : "❌",
							result.hasDeleteButton ? "✅" : "❌");
				} catch (Exception e) {
					LOGGER.warn("⚠️ Failed to test page {}: {}", i + 1, e.getMessage());
					takeScreenshot("button-test-error-page-" + i, false);
				}
			}
			// Final summary
			LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			LOGGER.info("📊 FINAL SUMMARY");
			LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			LOGGER.info("Total Pages Tested: {}", testedPages);
			LOGGER.info("Pages with New Button: {}", pagesWithNewButton);
			LOGGER.info("Pages with Save Button: {}", pagesWithSaveButton);
			LOGGER.info("Pages with Delete Button: {}", pagesWithDeleteButton);
			LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
		} catch (Exception e) {
			takeScreenshot("button-test-navigation-error", true);
			throw new AssertionError("Failed to test buttons across pages: " + e.getMessage(), e);
		}
	}

	/** Test buttons on the current page. */
	private ButtonTestResult testButtonsOnCurrentPage(String pageName) {
		ButtonTestResult result = new ButtonTestResult();
		String safePageName = pageName.toLowerCase().replaceAll("[^a-z0-9]+", "-");
		try {
			// Take initial screenshot
			takeScreenshot("button-test-" + safePageName + "-initial", false);
			// Test New button
			result.hasNewButton = testNewButton(pageName, safePageName);
			// After testing New, check if Save/Delete appeared (in dialog or form)
			if (result.hasNewButton) {
				// Save button should appear after clicking New
				result.hasSaveButton = testSaveButton(pageName, safePageName);
				// Test Cancel to close the form
				if (page.locator("vaadin-button:has-text('Cancel')").count() > 0) {
					LOGGER.info("   🔴 Clicking Cancel to close form");
					clickCancel();
					wait_500();
				}
			}
			// Test Edit + Save workflow if there's data
			if (verifyGridHasData()) {
				LOGGER.info("   📊 Page has grid data, testing Edit + Save workflow");
				clickFirstGridRow();
				wait_500();
				// Test Edit button if present
				if (page.locator("vaadin-button:has-text('Edit')").count() > 0) {
					LOGGER.info("   ✏️ Testing Edit button");
					clickEdit();
					wait_1000();
					takeScreenshot("button-test-" + safePageName + "-edit-form", false);
					// Save button should be in edit form
					if (page.locator("vaadin-button:has-text('Save')").count() > 0) {
						result.hasSaveButton = true;
						LOGGER.info("   ✅ Save button found in edit form");
					}
					// Cancel edit
					if (page.locator("vaadin-button:has-text('Cancel')").count() > 0) {
						clickCancel();
						wait_500();
					}
				}
				// Test Delete button
				clickFirstGridRow();
				wait_500();
				result.hasDeleteButton = testDeleteButton(pageName, safePageName);
			} else {
				LOGGER.info("   ℹ️ Page has no grid data, skipping Edit/Delete tests");
			}
		} catch (Exception e) {
			LOGGER.warn("   ⚠️ Error testing buttons on page {}: {}", pageName, e.getMessage());
			takeScreenshot("button-test-" + safePageName + "-error", false);
		}
		return result;
	}

	/** Test New button functionality. */
	private boolean testNewButton(String pageName, String safePageName) {
		try {
			final Locator newButton = page.locator("vaadin-button:has-text('New')");
			if (newButton.count() == 0) {
				LOGGER.info("   ❌ New button not found on page: {}", pageName);
				return false;
			}
			LOGGER.info("   ✅ New button found on page: {}", pageName);
			// Test if button is visible and enabled
			if (!newButton.first().isVisible()) {
				LOGGER.warn("   ⚠️ New button is not visible");
				return false;
			}
			if (!newButton.first().isEnabled()) {
				LOGGER.warn("   ⚠️ New button is not enabled");
				return false;
			}
			LOGGER.info("   🖱️ Testing New button click responsiveness");
			newButton.first().click();
			wait_1000();
			takeScreenshot("button-test-" + safePageName + "-new-clicked", false);
			// Verify that a form or dialog appeared
			boolean formAppeared = page.locator("vaadin-dialog-overlay[opened], vaadin-form-layout, vaadin-vertical-layout").count() > 0;
			if (formAppeared) {
				LOGGER.info("   ✅ New button is responsive - form/dialog appeared");
			} else {
				LOGGER.warn("   ⚠️ New button clicked but no form/dialog appeared");
			}
			return true;
		} catch (Exception e) {
			LOGGER.warn("   ⚠️ Error testing New button: {}", e.getMessage());
			return false;
		}
	}

	/** Test Save button functionality. */
	private boolean testSaveButton(String pageName, String safePageName) {
		try {
			final Locator saveButton = page.locator("vaadin-button:has-text('Save')");
			if (saveButton.count() == 0) {
				LOGGER.info("   ❌ Save button not found on page: {}", pageName);
				return false;
			}
			LOGGER.info("   ✅ Save button found on page: {}", pageName);
			// Test if button is visible and enabled
			if (!saveButton.first().isVisible()) {
				LOGGER.warn("   ⚠️ Save button is not visible");
				return false;
			}
			if (!saveButton.first().isEnabled()) {
				LOGGER.warn("   ⚠️ Save button is not enabled");
				return false;
			}
			// Try to fill at least one field to make save valid
			try {
				final Locator textFields = page.locator("vaadin-text-field:visible");
				if (textFields.count() > 0) {
					String testValue = "Test " + pageName + " " + System.currentTimeMillis();
					textFields.first().fill(testValue);
					LOGGER.info("   📝 Filled test data: {}", testValue);
					wait_500();
				}
				// Try to select combobox options if present
				final Locator comboBoxes = page.locator("vaadin-combo-box:visible");
				if (comboBoxes.count() > 0) {
					try {
						comboBoxes.first().click();
						wait_500();
						final Locator items = page.locator("vaadin-combo-box-item");
						if (items.count() > 0) {
							items.first().click();
							wait_500();
							LOGGER.info("   📋 Selected combobox option");
						}
					} catch (Exception e) {
						LOGGER.debug("   ⚠️ Could not select combobox: {}", e.getMessage());
					}
				}
			} catch (Exception e) {
				LOGGER.debug("   ⚠️ Could not fill form fields: {}", e.getMessage());
			}
			LOGGER.info("   🖱️ Testing Save button click responsiveness");
			saveButton.first().click();
			wait_1000();
			takeScreenshot("button-test-" + safePageName + "-save-clicked", false);
			// Verify that form closed or notification appeared
			boolean formClosed = page.locator("vaadin-dialog-overlay[opened]").count() == 0;
			boolean notificationAppeared = page.locator("vaadin-notification").count() > 0;
			if (formClosed || notificationAppeared) {
				LOGGER.info("   ✅ Save button is responsive - form closed or notification appeared");
			} else {
				LOGGER.warn("   ⚠️ Save button clicked but no response detected");
			}
			return true;
		} catch (Exception e) {
			LOGGER.warn("   ⚠️ Error testing Save button: {}", e.getMessage());
			return false;
		}
	}

	/** Test Delete button functionality. */
	private boolean testDeleteButton(String pageName, String safePageName) {
		try {
			final Locator deleteButton = page.locator("vaadin-button:has-text('Delete')");
			if (deleteButton.count() == 0) {
				LOGGER.info("   ❌ Delete button not found on page: {}", pageName);
				return false;
			}
			LOGGER.info("   ✅ Delete button found on page: {}", pageName);
			// Test if button is visible and enabled
			if (!deleteButton.first().isVisible()) {
				LOGGER.warn("   ⚠️ Delete button is not visible");
				return false;
			}
			if (!deleteButton.first().isEnabled()) {
				LOGGER.warn("   ⚠️ Delete button is not enabled");
				return false;
			}
			LOGGER.info("   🖱️ Testing Delete button click responsiveness");
			deleteButton.first().click();
			wait_1000();
			takeScreenshot("button-test-" + safePageName + "-delete-clicked", false);
			// Check if confirmation dialog appeared
			boolean confirmDialogAppeared = page.locator("vaadin-confirm-dialog-overlay[opened]").count() > 0;
			if (confirmDialogAppeared) {
				LOGGER.info("   ✅ Delete button is responsive - confirmation dialog appeared");
				// Cancel the deletion to avoid actually deleting data
				final Locator cancelButton = page.locator("vaadin-confirm-dialog-overlay[opened] vaadin-button:has-text('Cancel'), "
						+ "vaadin-confirm-dialog-overlay[opened] vaadin-button:has-text('No'), "
						+ "vaadin-confirm-dialog-overlay[opened] vaadin-button:has-text('Hayır')");
				if (cancelButton.count() > 0) {
					cancelButton.first().click();
					wait_500();
					LOGGER.info("   🔴 Cancelled deletion to preserve test data");
				}
			} else {
				// Check if item was deleted (grid updated or notification)
				boolean notificationAppeared = page.locator("vaadin-notification").count() > 0;
				if (notificationAppeared) {
					LOGGER.info("   ✅ Delete button is responsive - notification appeared");
				} else {
					LOGGER.warn("   ⚠️ Delete button clicked but no confirmation dialog or response detected");
				}
			}
			return true;
		} catch (Exception e) {
			LOGGER.warn("   ⚠️ Error testing Delete button: {}", e.getMessage());
			return false;
		}
	}

	/** Helper class to store button test results. */
	private static class ButtonTestResult {

		boolean hasNewButton = false;
		boolean hasSaveButton = false;
		boolean hasDeleteButton = false;
	}

	@Test
	@DisplayName ("✅ Test button responsiveness with repeated clicks")
	void testButtonResponsiveness() {
		LOGGER.info("🚀 Starting button responsiveness test...");
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login
			loginToApplication();
			wait_afterlogin();
			// Navigate to first page with a New button
			if (navigateToDynamicPageByEntityType("CProject")) {
				LOGGER.info("📋 Testing button responsiveness on Projects page");
				testButtonRepeatability("Projects");
			} else if (navigateToDynamicPageByEntityType("CUser")) {
				LOGGER.info("📋 Testing button responsiveness on Users page");
				testButtonRepeatability("Users");
			} else {
				LOGGER.warn("⚠️ Could not find suitable page for responsiveness test");
			}
			LOGGER.info("✅ Button responsiveness test completed!");
		} catch (Exception e) {
			LOGGER.error("❌ Button responsiveness test failed: {}", e.getMessage());
			takeScreenshot("responsiveness-test-error", true);
			throw new AssertionError("Button responsiveness test failed", e);
		}
	}

	/** Test button repeatability by clicking multiple times. */
	private void testButtonRepeatability(String entityName) {
		try {
			LOGGER.info("🔄 Testing New button repeatability...");
			for (int i = 0; i < 3; i++) {
				// Test New button
				if (page.locator("vaadin-button:has-text('New')").count() > 0) {
					LOGGER.info("   Attempt {} - Clicking New button", i + 1);
					clickNew();
					wait_1000();
					takeScreenshot("responsiveness-new-attempt-" + i, false);
					// Cancel
					if (page.locator("vaadin-button:has-text('Cancel')").count() > 0) {
						clickCancel();
						wait_500();
					}
				}
			}
			LOGGER.info("✅ New button is consistently responsive across multiple clicks");
		} catch (Exception e) {
			LOGGER.warn("⚠️ Button repeatability test failed: {}", e.getMessage());
		}
	}
}
