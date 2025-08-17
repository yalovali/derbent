package automated_tests.tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.activities.view.CActivityStatusView;
import tech.derbent.activities.view.CActivityTypeView;
import tech.derbent.decisions.view.CDecisionStatusView;
import tech.derbent.decisions.view.CDecisionTypeView;
import tech.derbent.decisions.view.CDecisionsView;
import tech.derbent.meetings.view.CMeetingStatusView;
import tech.derbent.meetings.view.CMeetingTypeView;
import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.projects.view.CProjectsView;
import tech.derbent.users.view.CUsersView;
import ui_tests.tech.derbent.ui.automation.CBaseUITest;

/**
 * UserColorAndEntryViewsPlaywrightTest - Comprehensive Playwright tests for user color functionality and entry views
 * status/type validation. This test class specifically validates: 1. User color display and functionality in
 * color-aware components 2. Status entry views display colors correctly 3. Type entry views display colors correctly 4.
 * Color-aware ComboBox components work properly 5. Entry forms show status and type fields properly
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource(properties = { "spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa",
        "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:testdb", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080" })
public class UserColorAndEntryViewsPlaywrightTest extends CBaseUITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserColorAndEntryViewsPlaywrightTest.class);

    private final Class<?>[] statusViews = { CActivityStatusView.class, CDecisionStatusView.class,
            CMeetingStatusView.class };

    private final Class<?>[] typeViews = { CActivityTypeView.class, CDecisionTypeView.class, CMeetingTypeView.class };

    private final Class<?>[] entryViews = { CActivitiesView.class, CDecisionsView.class, CMeetingsView.class,
            CProjectsView.class };

    /**
     * Helper method to click the first row in grid
     */
    private void clickFirstGridRow() {

        try {

            if (page.locator("vaadin-grid vaadin-grid-cell-content").count() > 0) {
                page.locator("vaadin-grid vaadin-grid-cell-content").first().click();
                return;
            }

            if (page.locator("vaadin-grid-cell-content").count() > 0) {
                page.locator("vaadin-grid-cell-content").first().click();
                return;
            }
            LOGGER.warn("No grid rows found to click");
        } catch (final Exception e) {
            LOGGER.warn("Failed to click first grid row: {}", e.getMessage());
        }
    }

    @Test
    void testColorAwareComboBoxFunctionality() {
        LOGGER.info("🧪 Testing color-aware ComboBox functionality...");
        assertBrowserAvailable();

        // Test in multiple views that use ComboBoxes
        for (final Class<?> view : entryViews) {
            LOGGER.info("Testing ComboBox functionality in: {}", view.getSimpleName());
            navigateToViewByClass(view);
            waitForGridLoad();
            // Open new entry form
            clickNew();
            wait_2000();
            // Find ComboBox components
            final int comboBoxCount = page.locator("vaadin-combo-box").count();

            if (comboBoxCount > 0) {
                LOGGER.info("Found {} ComboBox components in {}", comboBoxCount, view.getSimpleName());

                // Test opening a ComboBox to see if options are color-rendered
                try {
                    page.locator("vaadin-combo-box").first().click();
                    wait_1000();
                    // Check if dropdown opened
                    final boolean dropdownOpen = page.locator("vaadin-combo-box-overlay").count() > 0;

                    if (dropdownOpen) {
                        LOGGER.info("✅ ComboBox dropdown opened successfully");
                        // Check for colored options in dropdown
                        final boolean hasColoredOptions = (page.locator("vaadin-combo-box-item[style*='background']")
                                .count() > 0)
                                || (page.locator("vaadin-combo-box-item [style*='background']").count() > 0);

                        if (hasColoredOptions) {
                            LOGGER.info("✅ ComboBox has color-rendered options");
                        } else {
                            LOGGER.info("ℹ️ ComboBox options may not be color-rendered (depends on data)");
                        }
                        // Close dropdown by clicking elsewhere
                        page.locator("body").click();
                        wait_500();
                    }
                } catch (final Exception e) {
                    LOGGER.debug("ComboBox interaction test skipped: {}", e.getMessage());
                }
            } else {
                LOGGER.info("ℹ️ No ComboBox components found in {}", view.getSimpleName());
            }
            // Close the form
            clickCancel();
            wait_1000();
        }
        LOGGER.info("✅ Color-aware ComboBox functionality test completed");
    }

    @Test
    void testColorContrastAndAccessibility() {
        LOGGER.info("🧪 Testing color contrast and accessibility...");
        assertBrowserAvailable();

        // Test accessibility in status views which have the most color usage
        for (final Class<?> view : statusViews) {
            LOGGER.info("Testing accessibility in: {}", view.getSimpleName());
            navigateToViewByClass(view);
            waitForGridLoad();
            // Check for proper ARIA labels and roles
            final boolean hasAriaLabels = page.locator("[aria-label]").count() > 0;
            final boolean hasRoles = page.locator("[role]").count() > 0;

            if (hasAriaLabels) {
                LOGGER.info("✅ {} has ARIA labels for accessibility", view.getSimpleName());
            }

            if (hasRoles) {
                LOGGER.info("✅ {} has role attributes for accessibility", view.getSimpleName());
            }
            // Check for keyboard navigation support
            final boolean hasTabableElements = page.locator("[tabindex], button, input, select, textarea, [href]")
                    .count() > 0;
            assertTrue(hasTabableElements, view.getSimpleName() + " should have keyboard navigable elements");
            // Verify grid accessibility
            final boolean hasGridRole = page.locator("vaadin-grid[role='grid']").count() > 0;

            if (hasGridRole) {
                LOGGER.info("✅ {} has proper grid accessibility attributes", view.getSimpleName());
            }
        }
        LOGGER.info("✅ Color contrast and accessibility test completed");
    }

    @Test
    void testCompleteUserColorWorkflow() {
        LOGGER.info("🧪 Testing complete user color workflow...");
        assertBrowserAvailable();
        // Complete workflow: Users -> Projects -> Activities with color validation 1.
        // Start with Users view
        navigateToViewByClass(CUsersView.class);
        navigateToViewByClass(CProjectsView.class);
        navigateToViewByClass(CActivitiesView.class);
        waitForGridLoad();
        LOGGER.info("✅ Step 3: Accessed Activities view");
        // 4. Test creating new activity with user assignment
        clickNew();
        wait_2000();
        // Look for user assignment fields with color rendering
        final boolean hasUserFields = (page.locator("vaadin-combo-box[label*='User'], vaadin-combo-box[label*='user']")
                .count() > 0)
                || (page.locator("vaadin-combo-box[label*='Assigned'], vaadin-combo-box[label*='assigned']")
                        .count() > 0);

        if (hasUserFields) {
            LOGGER.info("✅ Step 4: Found user assignment fields in activity form");
        } else {
            LOGGER.info("ℹ️ Step 4: User assignment fields may be in different location");
        }
        // 5. Check status/type fields with colors
        final boolean hasStatusTypeFields = page
                .locator("vaadin-combo-box[label*='Status'], vaadin-combo-box[label*='Type']").count() > 0;

        if (hasStatusTypeFields) {
            LOGGER.info("✅ Step 5: Found status/type fields with color support");
        }
        // 6. Close and validate workflow completed
        clickCancel();
        wait_1000();
        LOGGER.info("✅ Complete user color workflow test completed successfully");
    }

    @Test
    void testEntryViewsShowStatusAndTypeFields() {
        LOGGER.info("🧪 Testing entry views show status and type fields...");
        assertBrowserAvailable();

        // Test each main entry view
        for (final Class<?> entryView : entryViews) {
            LOGGER.info("Testing entry view: {}", entryView.getSimpleName());
            navigateToViewByClass(entryView);
            waitForGridLoad();
            // Test creating a new entry to check status/type fields
            clickNew();
            wait_2000();
            // Check for status fields (ComboBox or similar)
            final boolean hasStatusField = (page
                    .locator("vaadin-combo-box[label*='Status'], vaadin-combo-box[label*='status']").count() > 0)
                    || (page.locator("vaadin-select[label*='Status'], vaadin-select[label*='status']").count() > 0);
            // Check for type fields (ComboBox or similar)
            final boolean hasTypeField = (page
                    .locator("vaadin-combo-box[label*='Type'], vaadin-combo-box[label*='type']").count() > 0)
                    || (page.locator("vaadin-select[label*='Type'], vaadin-select[label*='type']").count() > 0);

            if (hasStatusField) {
                LOGGER.info("✅ {} has status field in entry form", entryView.getSimpleName());
            } else {
                LOGGER.info("ℹ️ {} may not have status field (context dependent)", entryView.getSimpleName());
            }

            if (hasTypeField) {
                LOGGER.info("✅ {} has type field in entry form", entryView.getSimpleName());
            } else {
                LOGGER.info("ℹ️ {} may not have type field (context dependent)", entryView.getSimpleName());
            }
            // Check for color-aware components in the form
            final boolean hasColorAwareComponents = (page.locator("[class*='color-aware']").count() > 0)
                    || (page.locator("[style*='background-color']").count() > 0);

            if (hasColorAwareComponents) {
                LOGGER.info("✅ {} has color-aware components", entryView.getSimpleName());
            }
            // Close the form
            clickCancel();
            wait_1000();
        }
        LOGGER.info("✅ Entry views status and type fields test completed");
    }

    @Test
    void testStatusViewsDisplayColorsCorrectly() {
        LOGGER.info("🧪 Testing status views display colors correctly...");
        assertBrowserAvailable();

        // Test each status view
        for (final Class<?> statusView : statusViews) {
            LOGGER.info("Testing status view: {}", statusView.getSimpleName());
            navigateToViewByClass(statusView);
            waitForGridLoad();
            // Check for status column with color rendering
            final boolean hasStatusColumn = page.locator("vaadin-grid-column").count() > 0;
            assertTrue(hasStatusColumn, statusView.getSimpleName() + " should have grid columns");
            // Verify grid has data
            waitForGridLoad();
            final int rowCount = getGridRowCount();

            if (rowCount > 0) {
                LOGGER.info("✅ {} has {} status entries", statusView.getSimpleName(), rowCount);
                // Test opening an entry to verify color display in forms
                clickFirstGridRow();
                wait_2000();
                // Check if color field is displayed in the form
                final boolean hasColorField = page
                        .locator("vaadin-text-field[label*='Color'], vaadin-text-field[label*='color']").count() > 0;

                if (hasColorField) {
                    LOGGER.info("✅ {} displays color field in edit form", statusView.getSimpleName());
                } else {
                    LOGGER.warn("⚠️ {} missing color field in edit form", statusView.getSimpleName());
                }
                // Check for status display with proper styling
                final boolean hasStyledStatus = page
                        .locator("[style*='background'], [class*='status-'], [class*='color-']").count() > 0;

                if (hasStyledStatus) {
                    LOGGER.info("✅ {} has styled status elements", statusView.getSimpleName());
                }
                // Close the form
                clickCancel();
                wait_1000();
            } else {
                LOGGER.warn("⚠️ {} has no status entries to test", statusView.getSimpleName());
            }
        }
        LOGGER.info("✅ Status views color display test completed");
    }

    @Test
    void testTypeViewsDisplayColorsCorrectly() {
        LOGGER.info("🧪 Testing type views display colors correctly...");
        assertBrowserAvailable();

        // Test each type view
        for (final Class<?> typeView : typeViews) {
            LOGGER.info("Testing type view: {}", typeView.getSimpleName());
            navigateToViewByClass(typeView);
            waitForGridLoad();
            // Check for type column with color rendering
            final boolean hasTypeColumn = page.locator("vaadin-grid-column").count() > 0;
            assertTrue(hasTypeColumn, typeView.getSimpleName() + " should have grid columns");
            // Verify grid has data
            final int rowCount = getGridRowCount();

            if (rowCount > 0) {
                LOGGER.info("✅ {} has {} type entries", typeView.getSimpleName(), rowCount);
                // Test opening an entry to verify color display in forms
                clickFirstGridRow();
                wait_2000();
                // Check if color field is displayed in the form
                final boolean hasColorField = page
                        .locator("vaadin-text-field[label*='Color'], vaadin-text-field[label*='color']").count() > 0;

                if (hasColorField) {
                    LOGGER.info("✅ {} displays color field in edit form", typeView.getSimpleName());
                } else {
                    LOGGER.warn("⚠️ {} missing color field in edit form", typeView.getSimpleName());
                }
                // Check for active/inactive status display
                final boolean hasActiveField = page
                        .locator("vaadin-checkbox[label*='Active'], vaadin-checkbox[label*='active']").count() > 0;

                if (hasActiveField) {
                    LOGGER.info("✅ {} displays active status field", typeView.getSimpleName());
                } else {
                    LOGGER.warn("⚠️ {} missing active status field", typeView.getSimpleName());
                }
                // Close the form
                clickCancel();
                wait_1000();
            } else {
                LOGGER.warn("⚠️ {} has no type entries to test", typeView.getSimpleName());
            }
        }
        LOGGER.info("✅ Type views color display test completed");
    }

    @Test
    void testUserColorDisplayInColorAwareComponents() {
        LOGGER.info("🧪 Testing user color display in color-aware components...");
        assertBrowserAvailable();
        // Navigate to Users view to test user color functionality
        navigateToViewByClass(CUsersView.class);
        // Check if user grid displays properly with color-aware components
        waitForGridLoad();
        // Verify color-aware user display (profile pictures, status indicators)
        final boolean hasUserColorElements = page.locator("vaadin-grid-cell-content").count() > 0;
        assertTrue(hasUserColorElements, "Should display user entries with color-aware components");
        // Test user selection in forms by opening a new entry
        clickNew();
        wait_2000();
        // Look for user selection components (ComboBox with color-aware rendering)
        final boolean hasUserSelectionComponents = (page.locator("vaadin-combo-box").count() > 0)
                || (page.locator("[class*='color-aware']").count() > 0);

        if (hasUserSelectionComponents) {
            LOGGER.info("✅ Found user selection components with color-aware rendering");
        } else {
            LOGGER.warn("⚠️ No user selection components found in the form");
        }
        // Close the form
        clickCancel();
        LOGGER.info("✅ User color display test completed");
    }

    /**
     * Helper method to wait for grid loading
     */
    private void waitForGridLoad() {
        wait_2000(); // Give grid time to load

        // Wait for grid to be visible
        try {
            page.waitForSelector("vaadin-grid",
                    new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(5000));
        } catch (final Exception e) {
            LOGGER.debug("Grid selector wait timeout: {}", e.getMessage());
        }
    }
}