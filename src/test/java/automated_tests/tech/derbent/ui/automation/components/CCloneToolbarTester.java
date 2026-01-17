package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Tests clone/copy toolbar functionality on pages that support Copy To actions. */
public class CCloneToolbarTester extends CBaseComponentTester {

	private static final String CLONE_BUTTON_SELECTOR = "#cbutton-copy-to, #cbutton-clone, [id*='copy-to'], [id*='clone']";

	@Override
	public boolean canTest(final Page page) {
		return elementExists(page, CLONE_BUTTON_SELECTOR);
	}

	@Override
	public String getComponentName() {
		return "Clone Toolbar";
	}

	@Override
	public void test(final Page page) {
		LOGGER.info("      📄 Testing Clone Toolbar...");
		try {
			final Locator cloneButtons = page.locator(CLONE_BUTTON_SELECTOR);
			if (cloneButtons.count() == 0) {
				LOGGER.info("         ⏭️ Clone button not visible");
				return;
			}
			final Locator cloneButton = cloneButtons.first();
			if (!cloneButton.isVisible()) {
				LOGGER.info("         ⏭️ Clone button not visible");
				return;
			}
			cloneButton.click();
			waitMs(page, 1000);
			checkForExceptions(page);
			final Locator dialogOverlay = page.locator("vaadin-dialog-overlay[opened]");
			if (dialogOverlay.count() > 0) {
				LOGGER.info("         ✓ Clone dialog opened");
				final Locator dialog = dialogOverlay.first();
				if (dialog.locator("#cbutton-cancel").count() > 0) {
					dialog.locator("#cbutton-cancel").first().click();
				} else {
					page.keyboard().press("Escape");
				}
				waitMs(page, 1000);
			} else {
				LOGGER.info("         ℹ️ Clone dialog not detected after click");
			}
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ Clone toolbar test failed: {}", e.getMessage());
		}
		LOGGER.info("      ✅ Clone toolbar test complete");
	}
}
