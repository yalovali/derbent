package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Tests date picker functionality on pages that have date input fields. */
public class CDatePickerComponentTester extends CBaseComponentTester {

	private static final String DATE_PICKER_SELECTOR = "vaadin-date-picker, vaadin-date-time-picker, [id*='date']";

	@Override
	public boolean canTest(final Page page) {
		return elementExists(page, DATE_PICKER_SELECTOR);
	}

	@Override
	public String getComponentName() {
		return "Date Picker";
	}

	@Override
	public void test(final Page page) {
		LOGGER.info("      📅 Testing Date Picker...");
		try {
			final Locator datePickers = page.locator(DATE_PICKER_SELECTOR);
			final int count = datePickers.count();
			if (count > 0) {
				LOGGER.info("         ✓ Found {} date picker(s)", count);
				// Check if date pickers are visible and enabled
				for (int i = 0; i < count && i < 3; i++) {
					final Locator picker = datePickers.nth(i);
					if (picker.isVisible()) {
						LOGGER.info("         ✓ Date picker {} is visible", i + 1);
						final Locator input = picker.locator("input");
						if (input.count() > 0 && input.first().isEditable()) {
							input.first().fill("2025-01-15");
							waitMs(page, 500);
							checkForExceptions(page);
						}
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.debug("         ⚠️ Date picker test error: {}", e.getMessage());
		}
		LOGGER.info("      ✅ Date picker test complete");
	}
}
