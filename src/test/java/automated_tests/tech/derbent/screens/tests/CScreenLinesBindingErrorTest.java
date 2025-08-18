package automated_tests.tech.derbent.screens.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.screens.view.CScreenView;
import ui_tests.tech.derbent.ui.automation.CBaseUITest;

/**
 * Specific test to reproduce and verify fix for the CEnhancedBinder incomplete bindings error that occurs when clicking
 * "Add Screen Field Description" in CScreenLinesEditDialog.
 * 
 * Error: com.vaadin.flow.data.binder.BindingException: An exception has been thrown inside binding logic for the field
 * element [label='Entity Field Name']
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource(properties = { "spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa",
        "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080" })
public class CScreenLinesBindingErrorTest extends CBaseUITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CScreenLinesBindingErrorTest.class);

    @Test
    void testAddScreenFieldBindingError() {
        LOGGER.info("🧪 Testing CScreenLinesEditDialog binding error reproduction...");

        // Login and navigate to screen view
        loginToApplication();
        navigateToViewByClass(CScreenView.class);

        // Wait for page to load
        waitForElementById("main-content", 5);
        takeScreenshot("screen-view-loaded", false);

        // Select first screen if available, or create one
        if (getGridRowCount() > 0) {
            // Click first row to select a screen
            clickGrid(0);
            page.waitForTimeout(1000);

            takeScreenshot("screen-selected", false);

            // Look for the "Add Screen Field Description" button
            boolean addButtonFound = false;

            // Try multiple approaches to find the add field button
            if (elementExistsById("add-screen-field-button")) {
                LOGGER.info("✅ Found add screen field button by ID");
                clickById("add-screen-field-button");
                addButtonFound = true;
            } else if (page.getByText("Add Screen Field Description").count() > 0) {
                LOGGER.info("✅ Found add screen field button by text");
                page.getByText("Add Screen Field Description").first().click();
                addButtonFound = true;
            } else {
                // Try simple text search as fallback
                var textButtons = page.locator("vaadin-button:has-text('Add')");
                if (textButtons.count() > 0) {
                    LOGGER.info("✅ Found add button by text");
                    textButtons.first().click();
                    addButtonFound = true;
                }
            }

            if (addButtonFound) {
                // Wait for dialog to appear (this is where the error should occur)
                page.waitForTimeout(2000);

                takeScreenshot("add-field-dialog-opened", false);

                // Check if dialog opened successfully (no binding error)
                boolean dialogOpened = page.locator("vaadin-dialog-overlay").isVisible() || elementExistsById("dialog")
                        || page.getByText("Add Screen Field").isVisible();

                if (dialogOpened) {
                    LOGGER.info("✅ Dialog opened successfully - no binding error detected");

                    // Test that form fields are accessible (Entity Field Name ComboBox specifically)
                    boolean entityFieldNameExists = false;

                    // Look for Entity Field Name ComboBox
                    if (page.getByLabel("Entity Field Name").count() > 0) {
                        entityFieldNameExists = true;
                        LOGGER.info("✅ Entity Field Name ComboBox found by label");
                    } else if (page.getByText("Entity Field Name").count() > 0) {
                        entityFieldNameExists = true;
                        LOGGER.info("✅ Entity Field Name field found by text");
                    }

                    assertTrue(entityFieldNameExists,
                            "Entity Field Name ComboBox should be present and accessible (the field that was causing binding error)");

                    // Test Entity Line Type ComboBox too
                    boolean entityLineTypeExists = page.getByLabel("Entity Line Type").count() > 0
                            || page.getByText("Entity Line Type").count() > 0;

                    if (entityLineTypeExists) {
                        LOGGER.info("✅ Entity Line Type ComboBox also found");
                    }

                    // Close dialog
                    if (elementExistsById("cancel-button")) {
                        clickById("cancel-button");
                    } else if (page.getByText("Cancel").count() > 0) {
                        page.getByText("Cancel").first().click();
                    } else {
                        page.keyboard().press("Escape");
                    }

                    page.waitForTimeout(500);
                    takeScreenshot("dialog-closed", false);

                } else {
                    LOGGER.error("❌ Dialog failed to open - likely binding error occurred");
                    takeScreenshot("dialog-failed-to-open", false);

                    // Check for error notifications
                    if (page.locator("vaadin-notification").isVisible()) {
                        String errorText = page.locator("vaadin-notification").textContent();
                        LOGGER.error("Error notification: " + errorText);
                    }

                    // This should fail the test if the dialog doesn't open due to binding errors
                    assertTrue(false, "Add Screen Field dialog should open without binding errors");
                }

            } else {
                LOGGER.warn("⚠️ Add Screen Field button not found - test cannot reproduce the error scenario");
                takeScreenshot("add-button-not-found", false);
            }

        } else {
            LOGGER.warn("⚠️ No screens found in grid - cannot test field addition without a selected screen");
            takeScreenshot("no-screens-in-grid", false);
        }

        LOGGER.info("✅ CScreenLinesEditDialog binding error test completed");
    }
}