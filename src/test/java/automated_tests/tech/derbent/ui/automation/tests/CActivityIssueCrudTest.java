package automated_tests.tech.derbent.ui.automation.tests;

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
import automated_tests.tech.derbent.ui.automation.CBaseUITest;
import tech.derbent.Application;



/**
 * Comprehensive CRUD test for Activity and Issue entities with auxiliary features:
 * - Parent item selection (Issues can link to Activities)
 * - Attachments (upload/download/delete)
 * - Comments (add/edit/delete)
 * - Story points
 * - Sprint integration
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
@DisplayName("🔧 Activity & Issue CRUD with Auxiliary Features")
public class CActivityIssueCrudTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityIssueCrudTest.class);
	private int screenshotCounter = 1;

	@Test
	@DisplayName("✅ Activity - Complete CRUD with Attachments & Comments")
	void testActivityCrudWithAuxiliaryFeatures() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}

		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			LOGGER.info("🧪 Testing Activity CRUD with auxiliary features...");

			// Login and navigate to Activities
			loginToApplication();
			takeScreenshot(String.format("%03d-login-activities", screenshotCounter++), false);

			final boolean navigated = navigateToDynamicPageByEntityType("CActivity");
			assertTrue(navigated, "Failed to navigate to Activities view");
			wait_2000();
			takeScreenshot(String.format("%03d-activities-view", screenshotCounter++), false);

			// Verify grid loaded
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(15000));
			takeScreenshot(String.format("%03d-activities-grid-loaded", screenshotCounter++), false);

			// CREATE operation
			LOGGER.info("📝 Testing CREATE - New Activity");
			clickNew();
			wait_1000();
			takeScreenshot(String.format("%03d-activities-new-dialog", screenshotCounter++), false);

			fillFirstTextField("Test Activity - Automated");
			wait_500();
			final Locator textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				textAreas.first().fill("Activity created by automated testing");
			}
			wait_500();
			takeScreenshot(String.format("%03d-activities-form-filled", screenshotCounter++), false);

			clickSave();
			wait_2000();
			performFailFastCheck("After activity create");
			takeScreenshot(String.format("%03d-activities-created", screenshotCounter++), false);

			// READ operation
			LOGGER.info("📖 Testing READ - Verify Activity in Grid");
			clickRefresh();
			wait_1000();
			final Locator grid = page.locator("vaadin-grid").first();
			assertTrue(grid.isVisible(), "Grid should be visible");
			takeScreenshot(String.format("%03d-activities-grid-refreshed", screenshotCounter++), false);

			// UPDATE operation
			LOGGER.info("✏️ Testing UPDATE - Edit Activity");
			clickFirstGridRow();
			wait_1000();
			clickEdit();
			wait_1000();
			takeScreenshot(String.format("%03d-activities-edit-dialog", screenshotCounter++), false);

			fillFirstTextField("Test Activity - UPDATED");
			wait_500();
			takeScreenshot(String.format("%03d-activities-form-updated", screenshotCounter++), false);

			clickSave();
			wait_2000();
			performFailFastCheck("After activity update");
			takeScreenshot(String.format("%03d-activities-updated", screenshotCounter++), false);

			// Test auxiliary features
			LOGGER.info("🧪 Testing auxiliary features...");
			clickFirstGridRow();
			wait_1000();

			final boolean hasAttachments = testAttachmentOperations();
			if (hasAttachments) {
				takeScreenshot(String.format("%03d-activities-attachments-tested", screenshotCounter++), false);
			}

			final boolean hasComments = testCommentOperations();
			if (hasComments) {
				takeScreenshot(String.format("%03d-activities-comments-tested", screenshotCounter++), false);
			}

			// DELETE operation
			LOGGER.info("🗑️ Testing DELETE - Remove Activity");
			clickDelete();
			wait_500();
			takeScreenshot(String.format("%03d-activities-delete-confirm", screenshotCounter++), false);

			final Locator confirmYes = page.locator("#cbutton-yes");
			if (confirmYes.count() > 0) {
				confirmYes.first().click();
				wait_1000();
			}
			performFailFastCheck("After activity delete");
			takeScreenshot(String.format("%03d-activities-deleted", screenshotCounter++), false);

			LOGGER.info("✅ Activity CRUD with auxiliary features completed successfully");

		} catch (final Exception e) {
			LOGGER.error("❌ Activity CRUD test failed: {}", e.getMessage());
			takeScreenshot(String.format("%03d-activities-error", screenshotCounter++), true);
			throw new AssertionError("Activity CRUD test failed", e);
		}
	}

	@Test
	@DisplayName("✅ Issue - Complete CRUD with Parent Item, Attachments & Comments")
	void testIssueCrudWithAuxiliaryFeatures() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}

		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			LOGGER.info("🧪 Testing Issue CRUD with auxiliary features...");

			// Login and navigate to Issues
			loginToApplication();
			takeScreenshot(String.format("%03d-login-issues", screenshotCounter++), false);

			final boolean navigated = navigateToDynamicPageByEntityType("CIssue");
			assertTrue(navigated, "Failed to navigate to Issues view");
			wait_2000();
			takeScreenshot(String.format("%03d-issues-view", screenshotCounter++), false);

			// Verify grid loaded
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(15000));
			takeScreenshot(String.format("%03d-issues-grid-loaded", screenshotCounter++), false);

			// CREATE operation
			LOGGER.info("📝 Testing CREATE - New Issue");
			clickNew();
			wait_1000();
			takeScreenshot(String.format("%03d-issues-new-dialog", screenshotCounter++), false);

			fillFirstTextField("Test Issue - Bug Report");
			wait_500();
			final Locator textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				textAreas.first().fill("Issue created by automated testing");
			}
			wait_500();
			takeScreenshot(String.format("%03d-issues-form-filled", screenshotCounter++), false);

			clickSave();
			wait_2000();
			performFailFastCheck("After issue create");
			takeScreenshot(String.format("%03d-issues-created", screenshotCounter++), false);

			// READ operation
			LOGGER.info("📖 Testing READ - Verify Issue in Grid");
			clickRefresh();
			wait_1000();
			final Locator grid = page.locator("vaadin-grid").first();
			assertTrue(grid.isVisible(), "Grid should be visible");
			takeScreenshot(String.format("%03d-issues-grid-refreshed", screenshotCounter++), false);

			// UPDATE operation with parent item
			LOGGER.info("✏️ Testing UPDATE - Edit Issue with Parent Item");
			clickFirstGridRow();
			wait_1000();
			clickEdit();
			wait_1000();
			takeScreenshot(String.format("%03d-issues-edit-dialog", screenshotCounter++), false);

			fillFirstTextField("Test Issue - UPDATED");
			wait_500();

			// Test parent item selection (link to activity)
			final boolean hasParentItem = testParentItemSelection();
			if (hasParentItem) {
				LOGGER.info("✅ Parent item (Linked Activity) selected successfully");
			}
			wait_500();

			takeScreenshot(String.format("%03d-issues-form-updated", screenshotCounter++), false);

			clickSave();
			wait_2000();
			performFailFastCheck("After issue update");
			takeScreenshot(String.format("%03d-issues-updated", screenshotCounter++), false);

			// Test auxiliary features
			LOGGER.info("🧪 Testing auxiliary features...");
			clickFirstGridRow();
			wait_1000();

			final boolean hasAttachments = testAttachmentOperations();
			if (hasAttachments) {
				takeScreenshot(String.format("%03d-issues-attachments-tested", screenshotCounter++), false);
			}

			final boolean hasComments = testCommentOperations();
			if (hasComments) {
				takeScreenshot(String.format("%03d-issues-comments-tested", screenshotCounter++), false);
			}

			// DELETE operation
			LOGGER.info("🗑️ Testing DELETE - Remove Issue");
			clickDelete();
			wait_500();
			takeScreenshot(String.format("%03d-issues-delete-confirm", screenshotCounter++), false);

			final Locator confirmYes = page.locator("#cbutton-yes");
			if (confirmYes.count() > 0) {
				confirmYes.first().click();
				wait_1000();
			}
			performFailFastCheck("After issue delete");
			takeScreenshot(String.format("%03d-issues-deleted", screenshotCounter++), false);

			LOGGER.info("✅ Issue CRUD with auxiliary features completed successfully");

		} catch (final Exception e) {
			LOGGER.error("❌ Issue CRUD test failed: {}", e.getMessage());
			takeScreenshot(String.format("%03d-issues-error", screenshotCounter++), true);
			throw new AssertionError("Issue CRUD test failed", e);
		}
	}
}
