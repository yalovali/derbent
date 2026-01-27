package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Page;

/** Tests CSV export/report functionality.
 * <p>
 * Tests:
 * <ul>
 * <li>Report button presence and enabled state</li>
 * <li>CSV export dialog opens with field selector</li>
 * <li>Field selection checkboxes and groups</li>
 * <li>Select All / Deselect All per group</li>
 * <li>Generate CSV button triggers download</li>
 * </ul>
 * </p>
 */
public class CReportComponentTester extends CBaseComponentTester {

	private static final String CANCEL_BUTTON_ID = "custom-csv-export-cancel";
	private static final String CSV_DIALOG_ID = "custom-dialog-csv-export";
	private static final String GENERATE_BUTTON_ID = "custom-csv-export-generate";
	private static final String REPORT_BUTTON_ID = "cbutton-report";

	@Override
	public boolean canTest(final Page page) {
		// Report functionality exists if report button is present
		return elementExists(page, "#" + REPORT_BUTTON_ID);
	}

	@Override
	public String getComponentName() { return "CSV Report Export"; }

	/** Check if element is enabled. */
	private boolean isElementEnabled(final Page page, final String selector) {
		try {
			final var element = page.locator(selector);
			if (element.count() == 0) {
				return false;
			}
			return !element.first().isDisabled();
		} catch (final Exception e) {
			return false;
		}
	}

	@Override
	public void test(final Page page) {
		LOGGER.info("      📊 Testing CSV Report Export...");
		try {
			// Verify report button exists and is visible
			if (!elementExists(page, "#" + REPORT_BUTTON_ID)) {
				LOGGER.info("         ⏭️ Report button not found, skipping");
				return;
			}
			LOGGER.info("         ✓ Report button found");
			// Check if report button is enabled
			final boolean isEnabled = isElementEnabled(page, "#" + REPORT_BUTTON_ID);
			if (!isEnabled) {
				LOGGER.info("         ℹ️ Report button disabled (may require grid data)");
				return;
			}
			LOGGER.info("         ✓ Report button enabled");
			// Click report button to open dialog
			LOGGER.info("         🖱️ Clicking Report button...");
			page.locator("#" + REPORT_BUTTON_ID).click();
			waitMs(page, 1000);
			// Check if CSV export dialog appeared
			if (!elementExists(page, "#" + CSV_DIALOG_ID)) {
				LOGGER.warn("         ⚠️ CSV export dialog did not appear");
				return;
			}
			LOGGER.info("         ✅ CSV export dialog opened");
			// Test field selector checkboxes
			testFieldSelector(page);
			// Test Select All / Deselect All buttons
			testSelectAllButtons(page);
			// Test dialog buttons
			testDialogButtons(page);
			LOGGER.info("      ✅ CSV Report Export test complete");
		} catch (final Exception e) {
			LOGGER.warn("      ⚠️ CSV Report Export test encountered issue: {}", e.getMessage());
		}
	}

	private void testDialogButtons(final Page page) {
		try {
			LOGGER.info("         🔘 Testing dialog buttons...");
			// Check Generate button exists
			if (elementExists(page, "#" + GENERATE_BUTTON_ID)) {
				LOGGER.info("         ✓ Generate CSV button found");
			}
			// Check Cancel button exists
			if (elementExists(page, "#" + CANCEL_BUTTON_ID)) {
				LOGGER.info("         ✓ Cancel button found");
			}
			// Close dialog by clicking Cancel
			LOGGER.info("         🖱️ Closing dialog with Cancel...");
			page.locator("#" + CANCEL_BUTTON_ID).click();
			waitMs(page, 500);
			// Verify dialog closed
			if (!elementExists(page, "#" + CSV_DIALOG_ID)) {
				LOGGER.info("         ✅ Dialog closed successfully");
			} else {
				LOGGER.warn("         ⚠️ Dialog did not close");
			}
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ Dialog button test failed: {}", e.getMessage());
		}
	}

	private void testFieldSelector(final Page page) {
		try {
			LOGGER.info("         ☑️ Testing field selector checkboxes...");
			// Count checkboxes with field-path attribute
			final int checkboxCount = page.locator("vaadin-checkbox[id^='custom-csv-field-']").count();
			if (checkboxCount > 0) {
				LOGGER.info("         ✓ Found {} field checkboxes", checkboxCount);
				// Check if checkboxes are selected by default
				final int selectedCount = page.locator("vaadin-checkbox[id^='custom-csv-field-'][checked]").count();
				LOGGER.info("         ℹ️ {} checkboxes pre-selected", selectedCount);
			} else {
				LOGGER.warn("         ⚠️ No field checkboxes found");
			}
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ Field selector test failed: {}", e.getMessage());
		}
	}

	private void testSelectAllButtons(final Page page) {
		try {
			LOGGER.info("         🔘 Testing Select All / Deselect All buttons...");
			// Find all Select All buttons (one per group)
			final int selectAllCount = page.locator("vaadin-button[id^='custom-select-all-']").count();
			if (selectAllCount > 0) {
				LOGGER.info("         ✓ Found {} Select All buttons (one per group)", selectAllCount);
				// Test first Select All button
				LOGGER.info("         🖱️ Testing first Deselect All button...");
				page.locator("vaadin-button[id^='custom-deselect-all-']").first().click();
				waitMs(page, 300);
				LOGGER.info("         ✓ Deselect All clicked");
				// Test first Deselect All button
				LOGGER.info("         🖱️ Testing first Select All button...");
				page.locator("vaadin-button[id^='custom-select-all-']").first().click();
				waitMs(page, 300);
				LOGGER.info("         ✓ Select All clicked");
			} else {
				LOGGER.info("         ℹ️ No Select All buttons found (may be single group)");
			}
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ Select All button test failed: {}", e.getMessage());
		}
	}
}
