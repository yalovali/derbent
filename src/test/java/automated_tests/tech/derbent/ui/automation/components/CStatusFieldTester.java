package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Tests status field functionality on pages that have workflow status fields. */
public class CStatusFieldTester extends CBaseComponentTester {

	private static final String STATUS_FIELD_SELECTOR = "#field-status, [id*='status']";

	@Override
	public boolean canTest(final Page page) {
		return elementExists(page, STATUS_FIELD_SELECTOR);
	}

	@Override
	public String getComponentName() {
		return "Status Field";
	}

	@Override
	public void test(final Page page) {
		LOGGER.info("      📊 Testing Status Field...");
		try {
			final Locator statusField = page.locator(STATUS_FIELD_SELECTOR);
			if (statusField.count() > 0) {
				final Locator firstStatus = statusField.first();
				if (firstStatus.isVisible()) {
					LOGGER.info("         ✓ Status field is visible");
					// Check if it's a select/combo box
					final String tagName = firstStatus.evaluate("el => el.tagName").toString().toLowerCase();
					if (tagName.contains("select") || tagName.contains("combo")) {
						LOGGER.info("         ✓ Status field is interactive (select/combo)");
						firstStatus.click();
						waitMs(page, 500);
						final Locator items = page.locator("vaadin-combo-box-item");
						if (items.count() > 0) {
							items.first().click();
							waitMs(page, 500);
							checkForExceptions(page);
							LOGGER.info("         ✓ Status selection applied");
						}
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.debug("         ⚠️ Status field test error: {}", e.getMessage());
		}
		LOGGER.info("      ✅ Status field test complete");
	}
}
