package automated_tests.tech.derbent.ui.automation.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

/** Base class for component testers with common utility methods. */
public abstract class CBaseComponentTester implements IComponentTester {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CBaseComponentTester.class);

	protected static final String GRID_SELECTOR = "vaadin-grid, vaadin-grid-pro, so-grid, c-grid";
	protected static final String EXCEPTION_DIALOG_ID = "custom-exception-dialog";
	protected static final String EXCEPTION_DETAILS_DIALOG_ID = "custom-exception-details-dialog";
	protected static final String CONFIRM_YES_BUTTON_ID = "cbutton-yes";
	protected static final String INFO_OK_BUTTON_ID = "cbutton-ok";

	/** Check if element exists on page.
	 * @param page Page
	 * @param selector CSS selector
	 * @return true if exists */
	protected boolean elementExists(final Page page, final String selector) {
		try {
			return page.locator(selector).count() > 0;
		} catch (final Exception e) {
			return false;
		}
	}

	/** Check for exception dialogs.
	 * @param page Page
	 * @return true if exception detected */
	protected boolean hasException(final Page page) {
		try {
			final Locator exceptionDialog = page.locator("#" + EXCEPTION_DIALOG_ID);
			final Locator exceptionDetailsDialog = page.locator("#" + EXCEPTION_DETAILS_DIALOG_ID);
			return exceptionDialog.count() > 0 || exceptionDetailsDialog.count() > 0;
		} catch (final Exception e) {
			return false;
		}
	}

	/** Wait for short duration.
	 * @param page Page */
	protected void wait_500(final Page page) {
		page.waitForTimeout(500);
	}

	/** Wait for medium duration.
	 * @param page Page */
	protected void wait_1000(final Page page) {
		page.waitForTimeout(1000);
	}

	/** Wait for long duration.
	 * @param page Page */
	protected void wait_2000(final Page page) {
		page.waitForTimeout(2000);
	}

	/** Click button safely.
	 * @param page Page
	 * @param buttonId Button ID
	 * @return true if clicked */
	protected boolean clickButton(final Page page, final String buttonId) {
		try {
			final Locator button = page.locator("#" + buttonId);
			if (button.count() > 0 && button.first().isVisible()) {
				button.first().click();
				wait_500(page);
				return true;
			}
			return false;
		} catch (final PlaywrightException e) {
			LOGGER.debug("Failed to click button {}: {}", buttonId, e.getMessage());
			return false;
		}
	}

	protected void openTabOrAccordionIfNeeded(final Page page, final String text) {
		final Locator tab = page.locator("vaadin-tab").filter(new Locator.FilterOptions().setHasText(text));
		if (tab.count() > 0) {
			tab.first().click();
			wait_500(page);
			return;
		}
		final Locator accordion = page.locator("vaadin-accordion-panel").filter(new Locator.FilterOptions().setHasText(text));
		if (accordion.count() > 0) {
			final Locator heading = accordion.first().locator("vaadin-accordion-heading, [part='summary']");
			if (heading.count() > 0) {
				heading.first().click();
			} else {
				accordion.first().click();
			}
			wait_500(page);
		}
	}

	protected Locator waitForDialogWithText(final Page page, final String text) {
		for (int attempt = 0; attempt < 10; attempt++) {
			final Locator dialog = page.locator("vaadin-dialog-overlay[opened]").filter(new Locator.FilterOptions().setHasText(text));
			if (dialog.count() > 0) {
				return dialog.first();
			}
			wait_500(page);
		}
		return page.locator("vaadin-dialog-overlay[opened]");
	}

	protected void waitForDialogToClose(final Page page) {
		for (int attempt = 0; attempt < 10; attempt++) {
			if (page.locator("vaadin-dialog-overlay[opened]").count() == 0) {
				return;
			}
			wait_500(page);
		}
	}

	protected void waitForButtonEnabled(final Locator button) {
		for (int attempt = 0; attempt < 10; attempt++) {
			if (button.count() > 0 && !button.first().isDisabled()) {
				return;
			}
			if (button.count() > 0) {
				waitMs(button.page(), 250);
			}
		}
	}

	protected void waitForGridCellText(final Locator grid, final String text) {
		for (int attempt = 0; attempt < 10; attempt++) {
			if (grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(text)).count() > 0) {
				return;
			}
			wait_500(grid.page());
		}
	}

	protected void waitForGridCellGone(final Locator grid, final String text) {
		for (int attempt = 0; attempt < 10; attempt++) {
			if (grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(text)).count() == 0) {
				return;
			}
			wait_500(grid.page());
		}
	}

	/** Fill input field safely.
	 * @param page Page
	 * @param fieldId Field ID
	 * @param value Value to fill
	 * @return true if filled */
	protected boolean fillField(final Page page, final String fieldId, final String value) {
		try {
			final Locator field = page.locator("#" + fieldId);
			if (field.count() > 0) {
				field.first().fill(value);
				wait_500(page);
				return true;
			}
			return false;
		} catch (final PlaywrightException e) {
			LOGGER.debug("Failed to fill field {}: {}", fieldId, e.getMessage());
			return false;
		}
	}

	protected boolean fillFirstEditableField(final Page page, final String value) {
		try {
			final Locator inputs = page.locator("vaadin-text-field input, vaadin-text-area textarea, input[type='text'], textarea");
			for (int i = 0; i < inputs.count(); i++) {
				final Locator input = inputs.nth(i);
				if (input.isVisible() && input.isEditable()) {
					input.fill(value);
					wait_500(page);
					return true;
				}
			}
			return false;
		} catch (final Exception e) {
			LOGGER.debug("Failed to fill first editable field: {}", e.getMessage());
			return false;
		}
	}

	protected int getGridRowCount(final Page page) {
		try {
			final Locator grid = page.locator(GRID_SELECTOR);
			if (grid.count() == 0) {
				return -1;
			}
			final Object result = grid.first().evaluate(
					"grid => grid && (grid._dataProviderController && typeof grid._dataProviderController.size === 'number' ? grid._dataProviderController.size : "
							+ "(grid.items && Array.isArray(grid.items) ? grid.items.length : "
							+ "(grid._cache && typeof grid._cache.size === 'number' ? grid._cache.size : null)))");
			if (result instanceof Number) {
				return ((Number) result).intValue();
			}
		} catch (final Exception e) {
			LOGGER.debug("Failed to read grid row count via controller: {}", e.getMessage());
		}
		try {
			final Locator cells = page.locator("vaadin-grid-cell-content");
			final int cellCount = cells.count();
			if (cellCount == 0) {
				return 0;
			}
			final Locator columns = page.locator("vaadin-grid-column");
			final int columnCount = columns.count();
			if (columnCount > 0) {
				return cellCount / columnCount;
			}
			return cellCount;
		} catch (final Exception e) {
			LOGGER.debug("Failed to estimate grid row count: {}", e.getMessage());
			return -1;
		}
	}

	protected boolean clickFirstGridRow(final Page page) {
		try {
			final Locator cells = page.locator("vaadin-grid-cell-content");
			if (cells.count() > 0) {
				cells.first().click();
				wait_500(page);
				return true;
			}
			return false;
		} catch (final Exception e) {
			LOGGER.debug("Failed to click first grid row: {}", e.getMessage());
			return false;
		}
	}

	protected boolean selectFirstComboBoxOption(final Page page) {
		try {
			final Locator comboBoxes = page.locator("vaadin-combo-box");
			if (comboBoxes.count() == 0) {
				return false;
			}
			final Locator comboBox = comboBoxes.first();
			if (!comboBox.isVisible()) {
				return false;
			}
			comboBox.click();
			wait_500(page);
			final Locator items = page.locator("vaadin-combo-box-item");
			if (items.count() > 0) {
				items.first().click();
				wait_500(page);
				return true;
			}
			return false;
		} catch (final Exception e) {
			LOGGER.debug("Failed to select first combo box option: {}", e.getMessage());
			return false;
		}
	}

	protected void confirmDialogIfPresent(final Page page) {
		try {
			final Locator confirmButton = page.locator("#" + CONFIRM_YES_BUTTON_ID);
			if (confirmButton.count() > 0 && confirmButton.first().isVisible()) {
				confirmButton.first().click();
				wait_500(page);
			}
		} catch (final Exception e) {
			LOGGER.debug("Failed to confirm dialog: {}", e.getMessage());
		}
	}

	/** Wait for specified milliseconds.
	 * @param page Page
	 * @param ms Milliseconds to wait */
	protected void waitMs(final Page page, final int ms) {
		page.waitForTimeout(ms);
	}

	/** Check for exception dialogs and throw if found.
	 * @param page Page
	 * @throws AssertionError if exception dialog detected */
	protected void checkForExceptions(final Page page) {
		if (hasException(page)) {
			throw new AssertionError("Exception dialog detected on page");
		}
	}
}
