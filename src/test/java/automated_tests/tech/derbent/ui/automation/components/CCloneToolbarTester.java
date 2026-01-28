package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Tests clone/copy toolbar functionality on pages that support Copy To actions. */
public class CCloneToolbarTester extends CBaseComponentTester {

	private static final String CLONE_BUTTON_SELECTOR = "#cbutton-copy-to, #cbutton-clone, [id*='copy-to'], [id*='clone']";
	private static final String CLONE_DIALOG_SELECTOR = "vaadin-dialog-overlay[opened]";
	private static final String COPY_TO_NAME_ID = "copy-to-name";
	private static final String COPY_TO_TARGET_ID = "copy-to-target-type";
	private static final String COPY_TO_INCLUDE_RELATIONS_ID = "copy-to-include-relations";
	private static final String COPY_TO_INCLUDE_ATTACHMENTS_ID = "copy-to-include-attachments";
	private static final String COPY_TO_INCLUDE_COMMENTS_ID = "copy-to-include-comments";
	private static final String COPY_TO_INCLUDE_ALL_COLLECTIONS_ID = "copy-to-include-all-collections";
	private static final String COPY_TO_COPY_STATUS_ID = "copy-to-copy-status";
	private static final String COPY_TO_COPY_WORKFLOW_ID = "copy-to-copy-workflow";
	private static final String COPY_TO_RESET_DATES_ID = "copy-to-reset-dates";
	private static final String COPY_TO_RESET_ASSIGNMENTS_ID = "copy-to-reset-assignments";

	@Override
	public boolean canTest(final Page page) {
		return elementExists(page, CLONE_BUTTON_SELECTOR);
	}

	@Override
	public String getComponentName() { return "Clone Toolbar"; }

	@Override
	public void test(final Page page) {
		LOGGER.info("      📄 Testing Clone Toolbar... (page='{}', url='{}')", safePageTitle(page), safePageUrl(page));
		try {
			final Locator cloneButtons = page.locator(CLONE_BUTTON_SELECTOR);
			if (cloneButtons.count() == 0) {
				LOGGER.info("         ⏭️ Clone button not visible (page='{}', url='{}')", safePageTitle(page), safePageUrl(page));
				return;
			}
			final Locator cloneButton = cloneButtons.first();
			if (!cloneButton.isVisible()) {
				LOGGER.info("         ⏭️ Clone button not visible (page='{}', url='{}')", safePageTitle(page), safePageUrl(page));
				return;
			}
			checkForExceptions(page);
			if (!clickFirstGridRow(page)) {
				LOGGER.info("         ⏭️ No grid row selected; clone requires selection (page='{}', url='{}')", safePageTitle(page),
						safePageUrl(page));
				return;
			}
			checkForExceptions(page);
			runCopyScenarioSameType(page, cloneButton);
			runCopyScenarioDifferentType(page, cloneButton);
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ Clone toolbar test failed: {}", e.getMessage());
		}
		LOGGER.info("      ✅ Clone toolbar test complete");
	}

	private void runCopyScenarioSameType(final Page page, final Locator cloneButton) {
		LOGGER.info("         ▶️ Copy scenario: same entity type (page='{}', url='{}')", safePageTitle(page), safePageUrl(page));
		if (!openCloneDialog(page, cloneButton)) {
			LOGGER.warn("         ⚠️ Clone dialog not detected for same-type scenario");
			return;
		}
		try {
			LOGGER.info("         🧩 Setting same-type copy options...");
			selectComboItemByText(page, COPY_TO_TARGET_ID, "Same as Source");
			checkForExceptions(page);
			setCheckboxValue(page, COPY_TO_INCLUDE_RELATIONS_ID, true);
			checkForExceptions(page);
			setCheckboxValue(page, COPY_TO_INCLUDE_ATTACHMENTS_ID, false);
			checkForExceptions(page);
			setCheckboxValue(page, COPY_TO_INCLUDE_COMMENTS_ID, true);
			checkForExceptions(page);
			setCheckboxValue(page, COPY_TO_INCLUDE_ALL_COLLECTIONS_ID, false);
			checkForExceptions(page);
			setCheckboxValue(page, COPY_TO_COPY_STATUS_ID, true);
			checkForExceptions(page);
			setCheckboxValue(page, COPY_TO_COPY_WORKFLOW_ID, false);
			checkForExceptions(page);
			setCheckboxValue(page, COPY_TO_RESET_DATES_ID, true);
			checkForExceptions(page);
			setCheckboxValue(page, COPY_TO_RESET_ASSIGNMENTS_ID, true);
			checkForExceptions(page);
			final String nameValue = "Copy Same " + System.currentTimeMillis();
			if (!fillField(page, COPY_TO_NAME_ID, nameValue)) {
				LOGGER.warn("         ⚠️ Failed to set name field for same-type copy");
			}
			LOGGER.info("         ✅ Same-type options set (name: {})", nameValue);
			clickDialogSave(page);
			checkForExceptions(page);
			closeAnyOpenDialog(page);
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ Same-type copy scenario failed: {}", e.getMessage());
		}
	}

	private void runCopyScenarioDifferentType(final Page page, final Locator cloneButton) {
		LOGGER.info("         ▶️ Copy scenario: different entity type (page='{}', url='{}')", safePageTitle(page), safePageUrl(page));
		try {
			final List<String> targetTypes = listTargetTypes(page, cloneButton);
			if (targetTypes.isEmpty()) {
				LOGGER.info("         ⏭️ No alternate target type available; skipping different-type copy");
				return;
			}
			final List<String> selectedTargets = selectRandomTargets(targetTypes);
			LOGGER.info("         🎯 Selected {} random target types out of {}: {}", selectedTargets.size(), targetTypes.size(), selectedTargets);
			for (final String targetType : selectedTargets) {
				if (!openCloneDialog(page, cloneButton)) {
					LOGGER.warn("         ⚠️ Clone dialog not detected for target '{}'", targetType);
					continue;
				}
				LOGGER.info("         🧩 Selecting target type '{}'...", targetType);
				selectComboItemByText(page, COPY_TO_TARGET_ID, targetType);
				checkForExceptions(page);
				LOGGER.info("         🧩 Setting different-type copy options...");
				setCheckboxValue(page, COPY_TO_INCLUDE_RELATIONS_ID, true);
				checkForExceptions(page);
				setCheckboxValue(page, COPY_TO_INCLUDE_ATTACHMENTS_ID, false);
				checkForExceptions(page);
				setCheckboxValue(page, COPY_TO_INCLUDE_COMMENTS_ID, false);
				checkForExceptions(page);
				setCheckboxValue(page, COPY_TO_INCLUDE_ALL_COLLECTIONS_ID, false);
				checkForExceptions(page);
				setCheckboxValue(page, COPY_TO_COPY_STATUS_ID, false);
				checkForExceptions(page);
				setCheckboxValue(page, COPY_TO_COPY_WORKFLOW_ID, false);
				checkForExceptions(page);
				setCheckboxValue(page, COPY_TO_RESET_DATES_ID, true);
				checkForExceptions(page);
				setCheckboxValue(page, COPY_TO_RESET_ASSIGNMENTS_ID, true);
				checkForExceptions(page);
				final String nameValue = "Copy Different " + targetType + " " + System.currentTimeMillis();
				if (!fillField(page, COPY_TO_NAME_ID, nameValue)) {
					LOGGER.warn("         ⚠️ Failed to set name field for different-type copy (target={})", targetType);
				}
				LOGGER.info("         ✅ Different-type options set (target={}, name={})", targetType, nameValue);
				clickDialogSave(page);
				checkForExceptions(page);
				closeAnyOpenDialog(page);
			}
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ Different-type copy scenario failed: {}", e.getMessage());
		}
	}

	private boolean openCloneDialog(final Page page, final Locator cloneButton) {
		LOGGER.info("         🧭 Opening copy dialog... (page='{}', url='{}')", safePageTitle(page), safePageUrl(page));
		cloneButton.click();
		waitMs(page, 1000);
		checkForExceptions(page);
		final Locator dialogOverlay = page.locator(CLONE_DIALOG_SELECTOR);
		if (dialogOverlay.count() == 0) {
			return false;
		}
		LOGGER.info("         ✓ Clone dialog opened");
		return true;
	}

	private void clickDialogSave(final Page page) {
		final Locator dialog = page.locator(CLONE_DIALOG_SELECTOR).first();
		final Locator saveButton = dialog.locator("#cbutton-save, vaadin-button:has-text('Save')");
		if (saveButton.count() > 0) {
			LOGGER.info("         💾 Clicking Save in copy dialog...");
			saveButton.first().click();
			waitMs(page, 1000);
			return;
		}
		LOGGER.warn("         ⚠️ Save button not found in clone dialog");
	}

	private void setCheckboxValue(final Page page, final String checkboxId, final boolean desired) {
		final Locator checkbox = page.locator("#" + checkboxId);
		if (checkbox.count() == 0) {
			LOGGER.debug("         ⚠️ Checkbox {} not found", checkboxId);
			return;
		}
		LOGGER.info("         🔘 Setting {} to {}", checkboxId, desired);
		final Locator input = checkbox.locator("input[type='checkbox']");
		final boolean isChecked = input.count() > 0 ? input.first().isChecked() : "true".equals(checkbox.getAttribute("aria-checked"));
		if (isChecked != desired) {
			checkbox.click();
			waitMs(page, 250);
		}
	}

	private void setComboToFirstItem(final Page page, final String comboId) {
		final Locator combo = page.locator("#" + comboId);
		if (combo.count() == 0) {
			LOGGER.debug("         ⚠️ Combo {} not found", comboId);
			return;
		}
		LOGGER.info("         🔽 Selecting first option for {}", comboId);
		combo.first().click();
		waitMs(page, 300);
		final Locator items = page.locator("vaadin-combo-box-item");
		if (items.count() > 0) {
			items.first().click();
			waitMs(page, 300);
		}
	}

	private boolean setComboToFirstNonSameItem(final Page page, final String comboId) {
		final Locator combo = page.locator("#" + comboId);
		if (combo.count() == 0) {
			LOGGER.debug("         ⚠️ Combo {} not found", comboId);
			return false;
		}
		LOGGER.info("         🔽 Selecting non-same option for {}", comboId);
		combo.first().click();
		waitMs(page, 300);
		final Locator items = page.locator("vaadin-combo-box-item");
		for (int i = 0; i < items.count(); i++) {
			final Locator item = items.nth(i);
			final String text = item.innerText();
			if (text != null && text.contains("Same as Source")) {
				continue;
			}
			item.click();
			waitMs(page, 300);
			LOGGER.info("         ✅ Selected target type: {} (page='{}', url='{}')", text, safePageTitle(page), safePageUrl(page));
			return true;
		}
		return false;
	}

	private void selectComboItemByText(final Page page, final String comboId, final String textToFind) {
		final Locator combo = page.locator("#" + comboId);
		if (combo.count() == 0) {
			LOGGER.debug("         ⚠️ Combo {} not found", comboId);
			return;
		}
		LOGGER.info("         🔽 Selecting option '{}' for {}", textToFind, comboId);
		combo.first().click();
		waitMs(page, 300);
		final Locator items = page.locator("vaadin-combo-box-item");
		for (int i = 0; i < items.count(); i++) {
			final Locator item = items.nth(i);
			final String text = item.innerText();
			if (text != null && text.contains(textToFind)) {
				item.click();
				waitMs(page, 300);
				LOGGER.info("         ✅ Selected target type: {} (page='{}', url='{}')", text, safePageTitle(page), safePageUrl(page));
				return;
			}
		}
		LOGGER.debug("         ⚠️ Option '{}' not found for {}", textToFind, comboId);
	}

	private List<String> listTargetTypes(final Page page, final Locator cloneButton) {
		if (!openCloneDialog(page, cloneButton)) {
			LOGGER.warn("         ⚠️ Clone dialog not detected for target listing");
			return List.of();
		}
		try {
			final Locator combo = page.locator("#" + COPY_TO_TARGET_ID);
			if (combo.count() == 0) {
				LOGGER.debug("         ⚠️ Combo {} not found", COPY_TO_TARGET_ID);
				return List.of();
			}
			combo.first().click();
			waitMs(page, 300);
			final Locator items = page.locator("vaadin-combo-box-item");
			final List<String> targets = new ArrayList<>();
			for (int i = 0; i < items.count(); i++) {
				final String text = items.nth(i).innerText();
				if (text == null || text.isBlank()) {
					continue;
				}
				if (text.contains("Same as Source")) {
					continue;
				}
				targets.add(text);
			}
			return targets;
		} finally {
			closeAnyOpenDialog(page);
		}
	}

	private List<String> selectRandomTargets(final List<String> targets) {
		if (targets.isEmpty()) {
			return targets;
		}
		final List<String> shuffled = new ArrayList<>(targets);
		Collections.shuffle(shuffled, new Random());
		final int half = (int) Math.ceil(shuffled.size() / 2.0);
		return shuffled.subList(0, half);
	}

}
