package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Focused user UI tester that validates user-specific fields and project assignment panel. */
public class CUserComponentTester extends CBaseComponentTester {

	@Override
	public boolean canTest(final Page page) {
		return elementExists(page, "#field-login")
				|| page.locator("label:has-text('Login')").count() > 0
				|| page.locator("text=User Management").count() > 0;
	}

	@Override
	public String getComponentName() {
		return "User View";
	}

	@Override
	public void test(final Page page) {
		LOGGER.info("      👤 Testing User View specifics...");
		try {
			testUserFields(page);
			testCompanyRoleSelection(page);
			testUserProjectSettingsPanel(page);
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ User view test failed: {}", e.getMessage());
		} finally {
			checkForExceptions(page);
		}
		LOGGER.info("      ✅ User view test complete");
	}

	private void testUserFields(final Page page) {
		final Locator loginField = page.locator("#field-login input");
		if (loginField.count() > 0 && loginField.first().isEditable()) {
			loginField.first().fill("autotest-" + System.currentTimeMillis());
			wait_500(page);
			LOGGER.info("         ✓ Login field editable");
		}
		final Locator emailField = page.locator("#field-email input");
		if (emailField.count() > 0 && emailField.first().isEditable()) {
			emailField.first().fill("autotest-" + System.currentTimeMillis() + "@example.com");
			wait_500(page);
			LOGGER.info("         ✓ Email field editable");
		}
	}

	private void testCompanyRoleSelection(final Page page) {
		final Locator roleCombo = page.locator("#field-companyRole");
		if (roleCombo.count() > 0) {
			roleCombo.first().click();
			wait_500(page);
			final Locator items = page.locator("vaadin-combo-box-item");
			if (items.count() > 0) {
				items.first().click();
				wait_500(page);
				LOGGER.info("         ✓ Company role selection applied");
			}
		}
	}

	private void testUserProjectSettingsPanel(final Page page) {
		final Locator grid = page.locator("vaadin-grid").filter(new Locator.FilterOptions().setHasText("Project"));
		if (grid.count() == 0) {
			return;
		}
		final Locator container = grid.first().locator("xpath=ancestor::*[self::vaadin-vertical-layout or self::div][1]");
		final Locator addButton = container.locator("vaadin-button:has-text('Add')");
		if (addButton.count() == 0) {
			return;
		}
		addButton.first().click();
		wait_500(page);
		final Locator dialog = waitForDialogWithText(page, "Add Project to User");
		if (dialog.count() > 0) {
			LOGGER.info("         ✓ User project assignment dialog opened");
			if (dialog.locator("#cbutton-cancel").count() > 0) {
				dialog.locator("#cbutton-cancel").first().click();
			} else {
				page.keyboard().press("Escape");
			}
			waitForDialogToClose(page);
		}
	}
}
