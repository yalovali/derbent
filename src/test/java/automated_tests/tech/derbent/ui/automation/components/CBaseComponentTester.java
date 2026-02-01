package automated_tests.tech.derbent.ui.automation.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Base class for ALL component testers.
 * Component testers are NOT tests - they are utilities called BY test classes.
 * 
 * MANDATORY:
 * - Extend this class for all component testers
 * - Implement: canTest(), test(), getComponentName()
 * - NO @SpringBootTest annotation
 * - NO @Test methods
 * 
 * Pattern:
 * - Test classes extend CBaseUITest (have @Test methods)
 * - Component testers extend CBaseComponentTester (NO @Test methods)
 */
public abstract class CBaseComponentTester implements IComponentTester {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(CBaseComponentTester.class);
	
	/**
	 * Check if element exists on page.
	 * 
	 * @param page page to check
	 * @param selector CSS selector
	 * @return true if element exists
	 */
	protected boolean elementExists(final Page page, final String selector) {
		try {
			return page.locator(selector).count() > 0;
		} catch (final Exception e) {
			LOGGER.debug("Element not found: {}", selector);
			return false;
		}
	}
	
	/**
	 * Wait for element to be visible.
	 * 
	 * @param page page
	 * @param selector CSS selector
	 * @param timeoutMs timeout in milliseconds
	 * @return locator for element
	 */
	protected Locator waitForElement(final Page page, final String selector, final int timeoutMs) {
		final Locator locator = page.locator(selector);
		locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeoutMs));
		return locator;
	}
	
	/**
	 * Open tab or accordion panel if needed.
	 * 
	 * @param page page
	 * @param label tab/accordion label
	 */
	protected void openTabOrAccordionIfNeeded(final Page page, final String label) {
		try {
			// Try tabs first
			final Locator tab = page.locator("vaadin-tab").filter(new Locator.FilterOptions().setHasText(label));
			if (tab.count() > 0 && tab.isVisible()) {
				tab.click();
				page.waitForTimeout(500);
				return;
			}
			
			// Try accordion
			final Locator accordion = page.locator("vaadin-accordion-panel").filter(new Locator.FilterOptions().setHasText(label));
			if (accordion.count() > 0) {
				final Locator summary = accordion.locator("summary, [slot='summary']");
				if (summary.count() > 0 && !accordion.getAttribute("opened").equals("true")) {
					summary.first().click();
					page.waitForTimeout(500);
				}
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not open tab/accordion for '{}': {}", label, e.getMessage());
		}
	}
	
	/**
	 * Check if component is visible (not in collapsed accordion/closed tab).
	 * 
	 * @param page page
	 * @param selector component selector
	 * @return true if component is visible
	 */
	protected boolean isComponentVisible(final Page page, final String selector) {
		try {
			final Locator locator = page.locator(selector);
			return locator.count() > 0 && locator.isVisible();
		} catch (final Exception e) {
			return false;
		}
	}
	
	/**
	 * Wait for specified milliseconds.
	 * 
	 * @param page page
	 * @param ms milliseconds to wait
	 */
	protected void waitMs(final Page page, final int ms) {
		page.waitForTimeout(ms);
	}
	
	/**
	 * Close any open dialog overlays.
	 * 
	 * @param page page
	 */
	protected void closeAnyOpenDialog(final Page page) {
		try {
			final Locator overlay = page.locator("vaadin-dialog-overlay[opened]");
			if (overlay.count() > 0) {
				// Try ESC key first
				page.keyboard().press("Escape");
				page.waitForTimeout(500);
				
				// If still open, try close button
				if (overlay.count() > 0) {
					final Locator closeButton = overlay.locator("vaadin-button:has-text('Close'), vaadin-button:has-text('Cancel')");
					if (closeButton.count() > 0) {
						closeButton.first().click();
						page.waitForTimeout(500);
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not close dialog: {}", e.getMessage());
		}
	}
	
	/**
	 * Get page title safely.
	 * 
	 * @param page page
	 * @return page title or "<unknown>"
	 */
	protected String safePageTitle(final Page page) {
		try {
			return page.title();
		} catch (final PlaywrightException e) {
			return "<unknown>";
		}
	}
	
	/**
	 * Get page URL safely.
	 * 
	 * @param page page
	 * @return page URL or "<unknown>"
	 */
	protected String safePageUrl(final Page page) {
		try {
			return page.url();
		} catch (final PlaywrightException e) {
			return "<unknown>";
		}
	}
	
	/**
	 * Check for exceptions on page (logs warning if found).
	 * 
	 * @param page page to check
	 */
	protected void checkForExceptions(final Page page) {
		try {
			// Check for exception dialog
			final Locator exceptionDialog = page.locator("vaadin-dialog-overlay:has-text('Exception')");
			if (exceptionDialog.count() > 0 && exceptionDialog.isVisible()) {
				LOGGER.warn("Exception dialog detected on page: {} - {}", safePageTitle(page), safePageUrl(page));
			}
			
			// Check for error messages
			final Locator errorMessage = page.locator(".error-message, vaadin-notification[theme*='error']");
			if (errorMessage.count() > 0) {
				LOGGER.warn("Error message detected on page");
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not check for exceptions: {}", e.getMessage());
		}
	}
	
	/**
	 * Get grid row count.
	 * 
	 * @param page page
	 * @return row count
	 */
	protected int getGridRowCount(final Page page) {
		try {
			final Locator grid = page.locator("vaadin-grid");
			if (grid.count() == 0) {
				return 0;
			}
			return grid.first().locator("vaadin-grid-cell-content").count();
		} catch (final Exception e) {
			LOGGER.debug("Could not get grid row count: {}", e.getMessage());
			return 0;
		}
	}
	
	/**
	 * Click first grid row.
	 * 
	 * @param page page
	 */
	protected void clickFirstGridRow(final Page page) {
		try {
			final Locator grid = page.locator("vaadin-grid");
			if (grid.count() > 0) {
				final Locator firstRow = grid.first().locator("vaadin-grid-cell-content").first();
				if (firstRow.count() > 0) {
					firstRow.click();
					page.waitForTimeout(500);
				}
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not click first grid row: {}", e.getMessage());
		}
	}
	
	/**
	 * Confirm dialog if present (clicks Yes button).
	 * 
	 * @param page page
	 */
	protected void confirmDialogIfPresent(final Page page) {
		try {
			final Locator yesButton = page.locator("#cbutton-yes, vaadin-button:has-text('Yes')");
			if (yesButton.count() > 0 && yesButton.isVisible()) {
				yesButton.first().click();
				page.waitForTimeout(500);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not confirm dialog: {}", e.getMessage());
		}
	}
	
	/**
	 * Fill first editable field with value.
	 * 
	 * @param page page
	 * @param value value to fill
	 */
	protected void fillFirstEditableField(final Page page, final String value) {
		try {
			final Locator textField = page.locator("vaadin-text-field:not([readonly]), input[type='text']:not([readonly])");
			if (textField.count() > 0) {
				textField.first().fill(value);
				page.waitForTimeout(300);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not fill first editable field: {}", e.getMessage());
		}
	}
	
	/**
	 * Wait 500ms.
	 * 
	 * @param page page
	 */
	protected void wait_500(final Page page) {
		page.waitForTimeout(500);
	}
	
	/**
	 * Wait 1000ms.
	 * 
	 * @param page page
	 */
	protected void wait_1000(final Page page) {
		page.waitForTimeout(1000);
	}
	
	/**
	 * Wait for grid cell with text to appear.
	 * 
	 * @param gridLocator grid locator
	 * @param text expected text
	 */
	protected void waitForGridCellText(final Locator gridLocator, final String text) {
		try {
			final Locator cell = gridLocator.locator("vaadin-grid-cell-content:has-text('" + text + "')");
			cell.waitFor(new Locator.WaitForOptions().setTimeout(5000));
		} catch (final Exception e) {
			LOGGER.debug("Could not wait for grid cell text '{}': {}", text, e.getMessage());
		}
	}
	
	/**
	 * Wait for dialog to close.
	 * 
	 * @param page page
	 * @param maxWaitMs max wait time in milliseconds
	 * @param checkIntervalMs check interval in milliseconds
	 */
	protected void waitForDialogToClose(final Page page, final int maxWaitMs, final int checkIntervalMs) {
		final long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - startTime < maxWaitMs) {
			final Locator overlay = page.locator("vaadin-dialog-overlay[opened]");
			if (overlay.count() == 0) {
				return;
			}
			page.waitForTimeout(checkIntervalMs);
		}
		LOGGER.debug("Dialog did not close within {}ms", maxWaitMs);
	}
	
	/**
	 * Wait for grid cell with text to disappear.
	 * 
	 * @param gridLocator grid locator
	 * @param text expected text to disappear
	 */
	protected void waitForGridCellGone(final Locator gridLocator, final String text) {
		try {
			final Locator cell = gridLocator.locator("vaadin-grid-cell-content:has-text('" + text + "')");
			cell.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
		} catch (final Exception e) {
			LOGGER.debug("Could not wait for grid cell gone '{}': {}", text, e.getMessage());
		}
	}
	
	/**
	 * Wait for dialog with specific text to appear.
	 * 
	 * @param page page
	 * @param text expected text in dialog
	 * @return dialog overlay locator
	 */
	protected Locator waitForDialogWithText(final Page page, final String text) {
		try {
			final Locator dialog = page.locator("vaadin-dialog-overlay:has-text('" + text + "')");
			dialog.waitFor(new Locator.WaitForOptions().setTimeout(5000));
			return dialog;
		} catch (final Exception e) {
			LOGGER.debug("Could not wait for dialog with text '{}': {}", text, e.getMessage());
			return page.locator("vaadin-dialog-overlay");
		}
	}
	
	/**
	 * Wait 2000ms.
	 * 
	 * @param page page
	 */
	protected void wait_2000(final Page page) {
		page.waitForTimeout(2000);
	}
	
	/**
	 * Wait for button to be enabled.
	 * 
	 * @param button button locator
	 */
	protected void waitForButtonEnabled(final Locator button) {
		try {
			button.waitFor(new Locator.WaitForOptions().setTimeout(5000));
			// Check if button is not disabled
			final String disabled = button.getAttribute("disabled");
			if (disabled != null) {
				LOGGER.debug("Button is disabled, waiting for it to be enabled");
				button.page().waitForTimeout(500);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not wait for button enabled: {}", e.getMessage());
		}
	}
	
	/**
	 * Fill field with value.
	 * 
	 * @param locator field locator
	 * @param value value to fill
	 */
	protected void fillField(final Locator locator, final String value) {
		try {
			locator.fill(value);
			locator.page().waitForTimeout(300);
		} catch (final Exception e) {
			LOGGER.debug("Could not fill field: {}", e.getMessage());
		}
	}
	
	/**
	 * Fill required fields in form.
	 * 
	 * @param page page
	 * @param testValue value to use for required fields
	 */
	protected void fillRequiredFields(final Page page, final String testValue) {
		try {
			final Locator requiredFields = page.locator("vaadin-text-field[required], vaadin-text-area[required]");
			final int count = requiredFields.count();
			for (int i = 0; i < count; i++) {
				final Locator field = requiredFields.nth(i);
				if (field.isVisible()) {
					field.fill(testValue);
				}
			}
			page.waitForTimeout(300);
		} catch (final Exception e) {
			LOGGER.debug("Could not fill required fields: {}", e.getMessage());
		}
	}
	
	/**
	 * Check if dialog is currently open.
	 * 
	 * @param page page
	 * @return true if dialog is open
	 */
	protected boolean isDialogOpen(final Page page) {
		try {
			return page.locator("vaadin-dialog-overlay[opened]").count() > 0;
		} catch (final Exception e) {
			return false;
		}
	}
	
	/**
	 * Select first option in first combo box.
	 * 
	 * @param page page
	 */
	protected void selectFirstComboBoxOption(final Page page) {
		try {
			final Locator comboBox = page.locator("vaadin-combo-box").first();
			if (comboBox.count() > 0 && comboBox.isVisible()) {
				comboBox.click();
				page.waitForTimeout(500);
				
				// Try to select first option from overlay
				final Locator overlay = page.locator("vaadin-combo-box-overlay");
				if (overlay.count() > 0) {
					final Locator firstItem = overlay.locator("vaadin-combo-box-item").first();
					if (firstItem.count() > 0) {
						firstItem.click();
						page.waitForTimeout(300);
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not select first combo box option: {}", e.getMessage());
		}
	}
}
