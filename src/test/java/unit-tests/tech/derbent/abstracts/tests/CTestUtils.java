package tech.derbent.abstracts.tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.Page;
import com.vaadin.flow.router.Route;

/**
 * CTestUtils - Common utility functions for UI testing Provides reusable functions with annotation-based approach and
 * reduced logging. Uses class metadata instead of magic strings where possible.
 */
public final class CTestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CTestUtils.class);

    private CTestUtils() {
        // Utility class - no instantiation
    }

    /**
     * Extracts route path from view class annotation
     * 
     * @param viewClass
     *            the view class with @Route annotation
     * @return the route path
     */
    public static String getRouteFromClass(final Class<?> viewClass) {
        final Route routeAnnotation = viewClass.getAnnotation(Route.class);
        if (routeAnnotation == null) {
            throw new IllegalArgumentException("Class " + viewClass.getSimpleName() + " has no @Route annotation");
        }
        return routeAnnotation.value();
    }

    /**
     * Gets entity display name by removing 'C' prefix from class name
     * 
     * @param entityClass
     *            the entity class
     * @return the display name
     */
    public static String getEntityDisplayName(final Class<?> entityClass) {
        final String className = entityClass.getSimpleName();
        return className.startsWith("C") ? className.substring(1) : className;
    }

    /**
     * Common function to click Cancel/Close buttons using selectors
     * 
     * @param page
     *            the Playwright page
     * @return true if cancel button was found and clicked
     */
    public static boolean clickCancel(final Page page) {
        try {
            final var cancelButtons = page.locator("vaadin-button:has-text('Cancel'), vaadin-button:has-text('Close')");
            if (cancelButtons.count() > 0) {
                cancelButtons.first().click();
                page.waitForTimeout(1000);
                LOGGER.debug("Successfully clicked Cancel button");
                return true;
            } else {
                LOGGER.debug("Cancel button not found");
                return false;
            }
        } catch (final Exception e) {
            LOGGER.error("Failed to click Cancel button: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Common function to click New/Add buttons using selectors
     * 
     * @param page
     *            the Playwright page
     * @return true if new button was found and clicked
     */
    public static boolean clickNew(final Page page) {
        try {
            final var newButtons = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
            if (newButtons.count() > 0) {
                newButtons.first().click();
                page.waitForTimeout(1000);
                LOGGER.debug("Successfully clicked New button");
                return true;
            } else {
                LOGGER.debug("New/Add button not found");
                return false;
            }
        } catch (final Exception e) {
            LOGGER.error("Failed to click New button: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Common function to click Save/Create buttons using selectors
     * 
     * @param page
     *            the Playwright page
     * @return true if save button was found and clicked
     */
    public static boolean clickSave(final Page page) {
        try {
            final var saveButtons = page.locator("vaadin-button:has-text('Save'), vaadin-button:has-text('Create')");
            if (saveButtons.count() > 0) {
                saveButtons.first().click();
                page.waitForTimeout(2000); // Save operations may take longer
                LOGGER.debug("Successfully clicked Save button");
                return true;
            } else {
                LOGGER.debug("Save/Create button not found");
                return false;
            }
        } catch (final Exception e) {
            LOGGER.error("Failed to click Save button: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Common function to click on grid at specific row index
     * 
     * @param page
     *            the Playwright page
     * @param rowIndex
     *            the row index to click (0-based)
     * @return true if grid row was found and clicked
     */
    public static boolean clickGrid(final Page page, final int rowIndex) {
        try {
            final String selector = "vaadin-grid-cell-content";
            final var gridCells = page.locator(selector);

            if (gridCells.count() > rowIndex) {
                gridCells.nth(rowIndex).click();
                page.waitForTimeout(500);
                LOGGER.debug("Successfully clicked grid row at index: {}", rowIndex);
                return true;
            } else {
                LOGGER.debug("Grid row at index {} not found (total rows: {})", rowIndex, gridCells.count());
                return false;
            }
        } catch (final Exception e) {
            LOGGER.error("Failed to click grid at index {}: {}", rowIndex, e.getMessage());
            return false;
        }
    }

    /**
     * Common function to click on first grid row
     * 
     * @param page
     *            the Playwright page
     * @return true if grid row was found and clicked
     */
    public static boolean clickGrid(final Page page) {
        return clickGrid(page, 0);
    }

    /**
     * Gets the count of grid rows
     * 
     * @param page
     *            the Playwright page
     * @return the number of grid rows
     */
    public static int getGridRowCount(final Page page) {
        try {
            final var gridRows = page.locator("vaadin-grid-cell-content");
            final int rowCount = gridRows.count();
            LOGGER.debug("Grid has {} rows", rowCount);
            return rowCount;
        } catch (final Exception e) {
            LOGGER.error("Failed to get grid row count: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Checks if a form appeared (indicates successful New button click)
     * 
     * @param page
     *            the Playwright page
     * @return true if form elements are present
     */
    public static boolean isFormVisible(final Page page) {
        final int formElements = page.locator("vaadin-form-layout, vaadin-text-field, vaadin-text-area").count();
        final boolean visible = formElements > 0;
        LOGGER.debug("Form visibility check: {} elements found", formElements);
        return visible;
    }

    /**
     * Counts ComboBox components on the page
     * 
     * @param page
     *            the Playwright page
     * @return the number of ComboBox components
     */
    public static int getComboBoxCount(final Page page) {
        try {
            final var comboBoxes = page.locator("vaadin-combo-box");
            final int count = comboBoxes.count();
            LOGGER.debug("Found {} ComboBox components", count);
            return count;
        } catch (final Exception e) {
            LOGGER.error("Failed to count ComboBox components: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Tests a specific ComboBox by index
     * 
     * @param page
     *            the Playwright page
     * @param index
     *            the ComboBox index (0-based)
     * @return true if ComboBox was successfully tested
     */
    public static boolean testComboBoxByIndex(final Page page, final int index) {
        try {
            final var comboBoxes = page.locator("vaadin-combo-box");
            if (comboBoxes.count() <= index) {
                LOGGER.debug("ComboBox at index {} not found", index);
                return false;
            }

            comboBoxes.nth(index).click();
            page.waitForTimeout(500);

            final var options = page.locator("vaadin-combo-box-item");
            final int optionCount = options.count();
            LOGGER.debug("ComboBox {} has {} options", index, optionCount);

            // Close ComboBox
            page.click("body");
            page.waitForTimeout(500);

            return optionCount > 0;
        } catch (final Exception e) {
            LOGGER.error("Error testing ComboBox at index {}: {}", index, e.getMessage());
            return false;
        }
    }

    /**
     * Checks basic accessibility features
     * 
     * @param page
     *            the Playwright page
     * @param viewName
     *            the view name for logging
     * @return true if basic accessibility features are present
     */
    public static boolean checkAccessibility(final Page page, final String viewName) {
        try {
            // Check for proper heading structure
            final var headings = page.locator("h1, h2, h3, h4, h5, h6");
            final int headingCount = headings.count();

            // Check for aria labels
            final var ariaLabeled = page.locator("[aria-label], [aria-labelledby]");
            final int ariaCount = ariaLabeled.count();

            // Check for proper button roles
            final var buttons = page.locator("button, [role='button'], vaadin-button");
            final int buttonCount = buttons.count();

            LOGGER.debug("Accessibility check for {}: {} headings, {} aria-labeled elements, {} buttons", viewName,
                    headingCount, ariaCount, buttonCount);

            return headingCount > 0 && buttonCount > 0;
        } catch (final Exception e) {
            LOGGER.error("Accessibility check failed for {}: {}", viewName, e.getMessage());
            return false;
        }
    }
}