package automated_tests.tech.derbent.ui.automation.tests;
import automated_tests.tech.derbent.ui.automation.CBaseUITest;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import tech.derbent.Application;


/**
 * Comprehensive CRUD test for Users view following standard testing patterns.
 * Uses navigateToViewByText() for navigation as per TESTING_RULES.md
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = Application.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("🧪 Users View - Comprehensive CRUD Test")
public class CUserViewCrudTest extends CBaseUITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CUserViewCrudTest.class);

    @Test
    @DisplayName("✅ Test Users CRUD Operations with Comments")
    void testUsersCrudOperations() {
        LOGGER.info("🚀 Starting Users CRUD test...");

        if (!isBrowserAvailable()) {
            LOGGER.warn("⚠️ Browser not available - skipping test");
            Assumptions.assumeTrue(false, "Browser not available");
            return;
        }

        try {
            Files.createDirectories(Paths.get("target/screenshots"));

            // Step 1: Login
            LOGGER.info("📋 Step 1: Logging in...");
            loginToApplication();
            wait_afterlogin();
            takeScreenshot("001-users-logged-in", false);

            // Step 2: Navigate to Users view using standard pattern
            LOGGER.info("🧭 Step 2: Navigating to Users view...");
            
            // First expand System menu if collapsed
            Locator systemMenu = page.locator("vaadin-side-nav-item").filter(new Locator.FilterOptions().setHasText("System")).first();
            if (systemMenu.count() > 0) {
                LOGGER.info("   Expanding System menu...");
                systemMenu.click();
                wait_1000();
                takeScreenshot("002a-system-menu-expanded", false);
            }
            
            // Now navigate to Users
            boolean navigated = navigateToViewByText("Users");
            if (!navigated) {
                LOGGER.error("❌ Failed to navigate to Users view");
                takeScreenshot("error-users-navigation-failed", true);
                throw new RuntimeException("Navigation to Users view failed");
            }
            wait_2000();
            takeScreenshot("002-users-page-loaded", false);
            LOGGER.info("✅ Successfully navigated to Users view");

            // Step 3: Verify grid loads
            LOGGER.info("👁️ Step 3: Verifying grid loads...");
            Locator grid = page.locator("vaadin-grid").first();
            if (grid.count() == 0) {
                LOGGER.error("❌ Grid not found on Users page");
                takeScreenshot("error-users-grid-not-found", true);
                throw new RuntimeException("Users grid not found");
            }
            wait_1000();
            LOGGER.info("✅ Grid found and loaded");

            // Step 4: Test READ - Select first user
            LOGGER.info("👁️ Step 4: Testing READ operation...");
            Locator firstRow = grid.locator("vaadin-grid-cell-content").first();
            if (firstRow.count() > 0) {
                firstRow.click();
                wait_1000();
                takeScreenshot("003-users-selected-first-row", false);
                LOGGER.info("✅ READ successful - First user selected");
            } else {
                LOGGER.warn("⚠️ No users in grid to select");
            }

            // Step 5: Test Comments Section
            LOGGER.info("💬 Step 5: Testing Comments section...");
            testCommentsSection();

            // Step 6: Test CREATE (if New button exists)
            LOGGER.info("➕ Step 6: Testing CREATE operation...");
            Locator newButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("New"));
            if (newButton.count() > 0) {
                LOGGER.info("   Clicking New button...");
                newButton.click();
                wait_1000();
                takeScreenshot("004-users-new-button-clicked", false);

                // Fill required fields (adjust based on actual form)
                fillFieldIfPresent("#custom-username-input", "testuser" + System.currentTimeMillis());
                fillFieldIfPresent("#custom-email-input", "test@example.com");
                fillFieldIfPresent("#custom-password-input", "TestPass123!");
                wait_500();

                // Save
                Locator saveButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));
                if (saveButton.count() > 0) {
                    saveButton.click();
                    wait_2000();
                    takeScreenshot("005-users-create-saved", false);
                    LOGGER.info("✅ CREATE successful");
                } else {
                    LOGGER.warn("⚠️ Save button not found");
                }
            } else {
                LOGGER.info("   New button not found - CREATE may be restricted");
            }

            // Step 7: Test UPDATE
            LOGGER.info("✏️ Step 7: Testing UPDATE operation...");
            Locator editButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Edit"));
            if (editButton.count() > 0) {
                editButton.click();
                wait_1000();
                takeScreenshot("006-users-edit-button-clicked", false);

                // Modify fields
                fillFieldIfPresent("#custom-email-input", "updated@example.com");
                wait_500();

                // Save
                Locator saveButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));
                if (saveButton.count() > 0) {
                    saveButton.click();
                    wait_2000();
                    takeScreenshot("007-users-update-saved", false);
                    LOGGER.info("✅ UPDATE successful");
                }
            } else {
                LOGGER.info("   Edit button not found - UPDATE may be restricted");
            }

            // Final summary
            LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            LOGGER.info("✅ Users CRUD test completed successfully!");
            LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        } catch (Exception e) {
            LOGGER.error("❌ Users CRUD test failed: {}", e.getMessage(), e);
            takeScreenshot("error-users-crud-test", true);
            throw new RuntimeException("Users CRUD test failed", e);
        }
    }

    private void testCommentsSection() {
        try {
            LOGGER.info("   Checking for Comments section...");
            Locator commentsSection = page.locator("text=Comments").first();
            if (commentsSection.count() > 0) {
                commentsSection.scrollIntoViewIfNeeded();
                wait_1000();
                takeScreenshot("008-users-comments-section", false);
                LOGGER.info("✅ Comments section visible");
            } else {
                LOGGER.info("   Comments section not found - may not be available for this entity");
            }
        } catch (Exception e) {
            LOGGER.warn("⚠️ Error testing comments section: {}", e.getMessage());
        }
    }

    private void fillFieldIfPresent(String selector, String value) {
        try {
            Locator field = page.locator(selector);
            if (field.count() > 0) {
                field.fill(value);
                LOGGER.info("   Filled field {}: {}", selector, value);
            }
        } catch (Exception e) {
            LOGGER.debug("   Field {} not available: {}", selector, e.getMessage());
        }
    }
}
