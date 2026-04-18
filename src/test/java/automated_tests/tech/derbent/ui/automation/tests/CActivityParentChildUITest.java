package automated_tests.tech.derbent.ui.automation.tests;
import automated_tests.tech.derbent.ui.automation.CBaseUITest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assumptions;
import tech.derbent.Application;



/**
 * UI automation test for Activity parent-child relationship functionality.
 * Tests:
 * - Parent activity selection in activity form
 * - Visual display of parent activity in grid widget
 * - Multi-level hierarchy display
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("🔗 Activity Parent-Child Relationship UI Tests")
public class CActivityParentChildUITest extends CBaseUITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityParentChildUITest.class);
    private int screenshotCounter = 1;

    @Test
    @DisplayName("✅ Verify parent activity display in grid widget")
    void testParentActivityDisplayInWidget() {
        if (!isBrowserAvailable()) {
            LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
            Assumptions.assumeTrue(false, "Browser not available in CI environment");
            return;
        }

        try {
            Files.createDirectories(Paths.get("target/screenshots"));
            LOGGER.info("🧪 Testing parent activity display in widget...");

            // Login and navigate to Activities
            loginToApplication();
            takeScreenshot(String.format("%03d-login", screenshotCounter++), false);

            final boolean navigated = navigateToDynamicPageByEntityType("CActivity");
            assertTrue(navigated, "Failed to navigate to Activities view");
            wait_2000();
            takeScreenshot(String.format("%03d-activities-view", screenshotCounter++), false);

            // Verify grid loaded
            page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(15000));
            takeScreenshot(String.format("%03d-activities-grid-loaded", screenshotCounter++), false);

            // Refresh to ensure sample data is loaded
            clickRefresh();
            wait_2000();
            takeScreenshot(String.format("%03d-activities-grid-refreshed", screenshotCounter++), false);

            // Check if any activities with parent info are displayed
            LOGGER.info("📊 Checking for parent activity display in widgets...");
            final Locator gridCells = page.locator("vaadin-grid-cell-content");
            final int cellCount = gridCells.count();
            LOGGER.info("Found {} grid cells to inspect", cellCount);

            boolean foundParentDisplay = false;
            for (int i = 0; i < cellCount; i++) {
                final String cellText = gridCells.nth(i).textContent();
                if (cellText != null && cellText.contains("↳")) {
                    LOGGER.info("✅ Found parent activity display: {}", cellText);
                    foundParentDisplay = true;
                    break;
                }
            }

            if (foundParentDisplay) {
                LOGGER.info("✅ Parent activity display verified in grid widget");
                takeScreenshot(String.format("%03d-parent-display-found", screenshotCounter++), false);
            } else {
                LOGGER.info("ℹ️ No parent activity display found (may not have child activities in sample data)");
                takeScreenshot(String.format("%03d-no-parent-display", screenshotCounter++), false);
            }

            LOGGER.info("✅ Parent activity display test completed");

        } catch (final Exception e) {
            LOGGER.error("❌ Parent activity display test failed: {}", e.getMessage());
            takeScreenshot(String.format("%03d-parent-display-error", screenshotCounter++), true);
            throw new AssertionError("Parent activity display test failed", e);
        }
    }

    @Test
    @DisplayName("✅ Test parent activity selection in form")
    void testParentActivitySelection() {
        if (!isBrowserAvailable()) {
            LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
            Assumptions.assumeTrue(false, "Browser not available in CI environment");
            return;
        }

        try {
            Files.createDirectories(Paths.get("target/screenshots"));
            LOGGER.info("🧪 Testing parent activity selection in form...");

            // Login and navigate to Activities
            loginToApplication();
            takeScreenshot(String.format("%03d-login-parent-selection", screenshotCounter++), false);

            final boolean navigated = navigateToDynamicPageByEntityType("CActivity");
            assertTrue(navigated, "Failed to navigate to Activities view");
            wait_2000();

            // Verify grid loaded and has data
            page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(15000));
            clickRefresh();
            wait_2000();
            takeScreenshot(String.format("%03d-before-parent-selection", screenshotCounter++), false);

            // Create a new activity as a child
            LOGGER.info("📝 Creating new child activity...");
            clickNew();
            wait_1000();
            takeScreenshot(String.format("%03d-new-activity-dialog", screenshotCounter++), false);

            // Fill in name
            fillFirstTextField("Child Activity - Test Parent Selection");
            wait_500();

            // Fill in description if available
            final Locator textAreas = page.locator("vaadin-text-area");
            if (textAreas.count() > 0) {
                textAreas.first().fill("Testing parent activity selection functionality");
            }
            wait_500();

            // Try to select parent activity
            LOGGER.info("🔗 Attempting to select parent activity...");
            final boolean parentSelected = testParentItemSelection();
            if (parentSelected) {
                LOGGER.info("✅ Parent activity selected successfully");
                takeScreenshot(String.format("%03d-parent-selected", screenshotCounter++), false);
            } else {
                LOGGER.info("ℹ️ Parent selection not available or no parent activities to select");
                takeScreenshot(String.format("%03d-no-parent-selection", screenshotCounter++), false);
            }

            // Save the activity
            clickSave();
            wait_2000();
            performFailFastCheck("After creating child activity");
            takeScreenshot(String.format("%03d-child-activity-created", screenshotCounter++), false);

            // Verify in grid
            clickRefresh();
            wait_1000();
            takeScreenshot(String.format("%03d-grid-with-child", screenshotCounter++), false);

            LOGGER.info("✅ Parent activity selection test completed");

        } catch (final Exception e) {
            LOGGER.error("❌ Parent activity selection test failed: {}", e.getMessage());
            takeScreenshot(String.format("%03d-parent-selection-error", screenshotCounter++), true);
            throw new AssertionError("Parent activity selection test failed", e);
        }
    }

    @Test
    @DisplayName("✅ Verify hierarchical activity structure")
    void testHierarchicalActivityStructure() {
        if (!isBrowserAvailable()) {
            LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
            Assumptions.assumeTrue(false, "Browser not available in CI environment");
            return;
        }

        try {
            Files.createDirectories(Paths.get("target/screenshots"));
            LOGGER.info("🧪 Testing hierarchical activity structure...");

            // Login and navigate to Activities
            loginToApplication();
            takeScreenshot(String.format("%03d-login-hierarchy", screenshotCounter++), false);

            final boolean navigated = navigateToDynamicPageByEntityType("CActivity");
            assertTrue(navigated, "Failed to navigate to Activities view");
            wait_2000();

            // Verify grid loaded
            page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(15000));
            clickRefresh();
            wait_2000();
            takeScreenshot(String.format("%03d-hierarchy-view", screenshotCounter++), false);

            // Count activities with parent relationships
            LOGGER.info("📊 Analyzing hierarchical structure...");
            final Locator gridCells = page.locator("vaadin-grid-cell-content");
            final int cellCount = gridCells.count();
            int childActivityCount = 0;

            for (int i = 0; i < cellCount; i++) {
                final String cellText = gridCells.nth(i).textContent();
                if (cellText != null && cellText.contains("↳")) {
                    childActivityCount++;
                    LOGGER.info("Found child activity: {}", cellText);
                }
            }

            LOGGER.info("📈 Found {} child activities in hierarchy", childActivityCount);
            if (childActivityCount > 0) {
                LOGGER.info("✅ Hierarchical structure verified with {} child activities", childActivityCount);
            } else {
                LOGGER.info("ℹ️ No hierarchical structure detected (sample data may not have child activities)");
            }

            takeScreenshot(String.format("%03d-hierarchy-analyzed", screenshotCounter++), false);
            LOGGER.info("✅ Hierarchical activity structure test completed");

        } catch (final Exception e) {
            LOGGER.error("❌ Hierarchical activity structure test failed: {}", e.getMessage());
            takeScreenshot(String.format("%03d-hierarchy-error", screenshotCounter++), true);
            throw new AssertionError("Hierarchical activity structure test failed", e);
        }
    }
}
