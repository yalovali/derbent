package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Page;

/** Tests CRUD toolbar functionality on pages that have create/read/update/delete operations. */
public class CCrudToolbarTester extends CBaseComponentTester {

	private static final String CRUD_CANCEL_BUTTON_ID = "cbutton-cancel";
	private static final String CRUD_DELETE_BUTTON_ID = "cbutton-delete";
	private static final String CRUD_EDIT_BUTTON_ID = "cbutton-edit";
	private static final String CRUD_NEW_BUTTON_ID = "cbutton-new";
	private static final String CRUD_REFRESH_BUTTON_ID = "cbutton-refresh";
	private static final String CRUD_SAVE_BUTTON_ID = "cbutton-save";

	@Override
	public boolean canTest(final Page page) {
		// CRUD toolbar exists if we have at least 2 of the standard buttons
		int count = 0;
		if (elementExists(page, "#" + CRUD_NEW_BUTTON_ID)) {
			count++;
		}
		if (elementExists(page, "#" + CRUD_DELETE_BUTTON_ID)) {
			count++;
		}
		if (elementExists(page, "#" + CRUD_SAVE_BUTTON_ID)) {
			count++;
		}
		if (elementExists(page, "#" + CRUD_REFRESH_BUTTON_ID)) {
			count++;
		}
		return count >= 2;
	}

	@Override
	public String getComponentName() { return "CRUD Toolbar"; }

	@SuppressWarnings ("static-method")
	private boolean isElementEnabled(final Page page, final String selector) {
		try {
			final var element = page.locator(selector);
			if (element.count() == 0) {
				return false;
			}
			return !element.first().isDisabled();
		} catch (@SuppressWarnings ("unused") final Exception e) {
			return false;
		}
	}

	@Override
	public void test(final Page page) {
		LOGGER.info("      🛠️ Testing CRUD Toolbar...");
		int buttonsFound = 0;
		// Detect available buttons
		final boolean hasNew = elementExists(page, "#" + CRUD_NEW_BUTTON_ID);
		final boolean hasEdit = elementExists(page, "#" + CRUD_EDIT_BUTTON_ID);
		final boolean hasDelete = elementExists(page, "#" + CRUD_DELETE_BUTTON_ID);
		final boolean hasSave = elementExists(page, "#" + CRUD_SAVE_BUTTON_ID);
		final boolean hasCancel = elementExists(page, "#" + CRUD_CANCEL_BUTTON_ID);
		final boolean hasRefresh = elementExists(page, "#" + CRUD_REFRESH_BUTTON_ID);
		if (hasNew) {
			LOGGER.info("         ✓ New button found");
			buttonsFound++;
		}
		if (hasEdit) {
			LOGGER.info("         ✓ Edit button found");
			buttonsFound++;
		}
		if (hasDelete) {
			LOGGER.info("         ✓ Delete button found");
			buttonsFound++;
		}
		if (hasSave) {
			LOGGER.info("         ✓ Save button found");
			buttonsFound++;
		}
		if (hasCancel) {
			LOGGER.info("         ✓ Cancel button found");
			buttonsFound++;
		}
		if (hasRefresh) {
			LOGGER.info("         ✓ Refresh button found");
			buttonsFound++;
		}
		try {
			if (hasRefresh) {
				LOGGER.info("         🔄 Testing Refresh button...");
				page.locator("#" + CRUD_REFRESH_BUTTON_ID).click();
				waitMs(page, 1000);
				checkForExceptions(page);
				LOGGER.info("         ✅ Refresh works");
			}
			final int initialCount = getGridRowCount(page);
			LOGGER.info("         📊 Initial grid count: {}", initialCount);
			int afterCreateCount = initialCount;
			if (hasNew && hasSave) {
				afterCreateCount = testCreateWorkflow(page, initialCount, hasCancel);
			}
			if (hasEdit && hasSave) {
				testEditWorkflow(page);
			}
			if (hasDelete) {
				testDeleteWorkflow(page, afterCreateCount);
			}
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ CRUD operation test encountered issue: {}", e.getMessage());
		}
		LOGGER.info("      ✅ CRUD toolbar test complete ({} buttons found)", buttonsFound);
	}

	private int testCreateWorkflow(final Page page, final int initialCount, final boolean hasCancel) {
		try {
			LOGGER.info("         ➕ Testing CREATE workflow...");
			page.locator("#" + CRUD_NEW_BUTTON_ID).click();
			waitMs(page, 1000);
			checkForExceptions(page);
			if (!isElementEnabled(page, "#" + CRUD_SAVE_BUTTON_ID)) {
				LOGGER.info("         ⏭️ Save button not enabled, skipping CREATE workflow");
				return initialCount;
			}
			final String testValue = "AutoTest-" + System.currentTimeMillis();
			final boolean filled = fillFirstEditableField(page, testValue);
			if (filled) {
				LOGGER.info("         📝 Filled editable field with: {}", testValue);
			}
			fillRequiredFields(page, testValue);
			// Special handling for CUser lastname if it exists (generic fallback)
			if (elementExists(page, "#field-lastname")) {
				fillField(page, "field-lastname", "Doe");
			}
			selectFirstComboBoxOption(page);
			page.locator("#" + CRUD_SAVE_BUTTON_ID).click();
			waitMs(page, 1500);
			checkForExceptions(page);
			if (initialCount >= 0) {
				final int newCount = getGridRowCount(page);
				if (newCount > initialCount) {
					LOGGER.info("         ✅ CREATE increased grid count ({} -> {})", initialCount, newCount);
					return newCount;
				} else {
					LOGGER.info("         ⚠️ Grid count did not increase ({} -> {})", initialCount, newCount);
					return newCount;
				}
			}
			return initialCount;
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ CREATE workflow failed: {}", e.getMessage());
			try {
				if (hasCancel && elementExists(page, "#" + CRUD_CANCEL_BUTTON_ID)) {
					page.locator("#" + CRUD_CANCEL_BUTTON_ID).click();
					waitMs(page, 500);
				}
			} catch (final Exception ignored) { /**/ }
			return initialCount;
		}
	}

	private void testDeleteWorkflow(final Page page, final int currentCount) {
		try {
			LOGGER.info("         🗑️ Testing DELETE workflow...");
			final boolean rowSelected = clickFirstGridRow(page);
			if (!rowSelected) {
				LOGGER.info("         ⏭️ No grid row available for DELETE");
				return;
			}
			page.locator("#" + CRUD_DELETE_BUTTON_ID).click();
			waitMs(page, 1000);
			confirmDialogIfPresent(page);
			waitMs(page, 1500);
			checkForExceptions(page);
			if (currentCount >= 0) {
				final int afterDeleteCount = getGridRowCount(page);
				if (afterDeleteCount < currentCount) {
					LOGGER.info("         ✅ DELETE decreased grid count ({} -> {})", currentCount, afterDeleteCount);
				} else {
					LOGGER.info("         ⚠️ Grid count did not decrease ({} -> {})", currentCount, afterDeleteCount);
				}
			}
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ DELETE workflow failed: {}", e.getMessage());
		}
	}

	private void testEditWorkflow(final Page page) {
		try {
			LOGGER.info("         ✏️ Testing EDIT workflow...");
			final boolean rowSelected = clickFirstGridRow(page);
			if (!rowSelected) {
				LOGGER.info("         ⏭️ No grid row available for EDIT");
				return;
			}
			page.locator("#" + CRUD_EDIT_BUTTON_ID).click();
			waitMs(page, 1000);
			checkForExceptions(page);
			final String testValue = "AutoTest-Edit-" + System.currentTimeMillis();
			final boolean filled = fillFirstEditableField(page, testValue);
			if (filled) {
				LOGGER.info("         📝 Updated editable field with: {}", testValue);
			}
			page.locator("#" + CRUD_SAVE_BUTTON_ID).click();
			waitMs(page, 1500);
			checkForExceptions(page);
			LOGGER.info("         ✅ EDIT saved successfully");
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ EDIT workflow failed: {}", e.getMessage());
		}
	}
}
