package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Focused project UI tester that validates project-specific fields and user assignment panel. */
public class CProjectComponentTester extends CBaseComponentTester {

	@Override
	public boolean canTest(final Page page) {
		return elementExists(page, "#field-entityType")
				|| page.locator("label:has-text('Project Type')").count() > 0
				|| page.locator("text=Project Management").count() > 0;
	}

	@Override
	public String getComponentName() {
		return "Project View";
	}

	@Override
	public void test(final Page page) {
		LOGGER.info("      🧭 Testing Project View specifics...");
		try {
			testProjectTypeSelection(page);
			testActiveCheckbox(page);
			testProjectUserSettingsPanel(page);
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ Project view test failed: {}", e.getMessage());
		} finally {
			checkForExceptions(page);
		}
		LOGGER.info("      ✅ Project view test complete");
	}

	private void testProjectTypeSelection(final Page page) {
		final Locator typeCombo = page.locator("#field-entityType").first();
		if (typeCombo.count() > 0) {
			typeCombo.click();
			wait_500(page);
			final Locator items = page.locator("vaadin-combo-box-item");
			if (items.count() > 0) {
				items.first().click();
				wait_500(page);
				LOGGER.info("         ✓ Project Type combo selection applied");
			}
			return;
		}
		final Locator typeLabel = page.locator("label:has-text('Project Type')").first();
		if (typeLabel.count() > 0) {
			final Locator combo = typeLabel.locator("xpath=ancestor::*[self::vaadin-form-item or self::div][1]").locator("vaadin-combo-box");
			if (combo.count() > 0) {
				combo.first().click();
				wait_500(page);
				final Locator items = page.locator("vaadin-combo-box-item");
				if (items.count() > 0) {
					items.first().click();
					wait_500(page);
					LOGGER.info("         ✓ Project Type combo selection applied");
				}
			}
		}
	}

	private void testActiveCheckbox(final Page page) {
		final Locator activeCheckbox = page.locator("#field-active");
		if (activeCheckbox.count() > 0 && activeCheckbox.first().isVisible()) {
			activeCheckbox.first().click();
			wait_500(page);
			activeCheckbox.first().click();
			wait_500(page);
			LOGGER.info("         ✓ Active checkbox toggled");
			return;
		}
		final Locator activeLabel = page.locator("label:has-text('Active')").first();
		if (activeLabel.count() > 0) {
			final Locator checkbox = activeLabel.locator("xpath=ancestor::*[self::vaadin-form-item or self::div][1]").locator("vaadin-checkbox");
			if (checkbox.count() > 0) {
				checkbox.first().click();
				wait_500(page);
				checkbox.first().click();
				wait_500(page);
				LOGGER.info("         ✓ Active checkbox toggled");
			}
		}
	}

	private void testProjectUserSettingsPanel(final Page page) {
		final Locator grid = page.locator("vaadin-grid").filter(new Locator.FilterOptions().setHasText("User"));
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
		final Locator dialog = waitForDialogWithText(page, "Add User to Project");
		if (dialog.count() > 0) {
			LOGGER.info("         ✓ Project user assignment dialog opened");
			if (dialog.locator("#cbutton-cancel").count() > 0) {
				dialog.locator("#cbutton-cancel").first().click();
			} else {
				page.keyboard().press("Escape");
			}
			waitForDialogToClose(page);
		}
	}
}
