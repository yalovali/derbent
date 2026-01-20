package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Tests project user settings component. */
public class CProjectUserSettingsComponentTester extends CBaseComponentTester {

	private static final String COMPONENT_ADD_BUTTON_ID = "#cbutton-add-relation";
	private static final String COMPONENT_EDIT_BUTTON_ID = "#cbutton-edit-relation";
	private static final String COMPONENT_DELETE_BUTTON_ID = "#cbutton-delete-relation";

	@Override
	public boolean canTest(final Page page) {
		return elementExists(page, COMPONENT_ADD_BUTTON_ID);
	}

	@Override
	public String getComponentName() {
		return "Project User Settings Component";
	}

	@Override
	public void test(final Page page) {
		LOGGER.info("      👥 Testing Project User Settings Component...");
		try {
			final Locator addButton = page.locator(COMPONENT_ADD_BUTTON_ID);
			if (addButton.count() == 0 || !addButton.first().isVisible()) {
				LOGGER.info("         ⏭️ Add button not visible");
				return;
			}

			// Test Add Dialog Opening
			addButton.first().click();
			waitMs(page, 500);
			
			// Dialog title might vary, but it should be a dialog
			Locator dialog = page.locator("vaadin-dialog-overlay[opened]");
			if (dialog.count() > 0) {
				LOGGER.info("         ✅ Add dialog opened");
				// Close dialog
				if (dialog.locator("#cbutton-cancel").count() > 0) {
					dialog.locator("#cbutton-cancel").first().click();
				} else if (dialog.locator("vaadin-button:has-text('Cancel')").count() > 0) {
					dialog.locator("vaadin-button:has-text('Cancel')").first().click();
				} else {
					page.keyboard().press("Escape");
				}
				waitForDialogToClose(page);
			} else {
				LOGGER.warn("         ⚠️ Add dialog did not open");
			}

			// Test Grid Selection (if items exist)
			final Locator grid = page.locator("vaadin-grid").filter(new Locator.FilterOptions().setHasText("User"));
			if (grid.count() > 0) {
				final Locator firstRow = grid.first().locator("vaadin-grid-cell-content").first();
				if (firstRow.count() > 0) {
					firstRow.click();
					waitMs(page, 200);
					
					final Locator editButton = page.locator(COMPONENT_EDIT_BUTTON_ID);
					final Locator deleteButton = page.locator(COMPONENT_DELETE_BUTTON_ID);
					
					if (editButton.count() > 0 && !editButton.first().isDisabled()) {
						LOGGER.info("         ✅ Edit button enabled on selection");
					}
					if (deleteButton.count() > 0 && !deleteButton.first().isDisabled()) {
						LOGGER.info("         ✅ Delete button enabled on selection");
					}
				}
			}

		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ Project User Settings test failed: {}", e.getMessage());
		} finally {
			checkForExceptions(page);
		}
		LOGGER.info("      ✅ Project User Settings component test complete");
	}
}
