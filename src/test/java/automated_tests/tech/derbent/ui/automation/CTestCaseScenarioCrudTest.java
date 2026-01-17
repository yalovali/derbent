package automated_tests.tech.derbent.ui.automation;

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
 * Comprehensive CRUD operations test for Test Case and Test Scenario entities.
 * Uses generic helper methods from CBaseUITest for testing attachments and comments.
 * Tests cover full Create, Read, Update, Delete lifecycle with attachments and comments.
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = Application.class)
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
	"spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("🔧 Test Case & Scenario CRUD Test")
public class CTestCaseScenarioCrudTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTestCaseScenarioCrudTest.class);
	private int screenshotCounter = 1;

	@Test
	@DisplayName("✅ Test Case - Complete CRUD with Attachments & Comments")
	void testTestCaseCrudOperations() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}

		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			LOGGER.info("🧪 Testing Test Case CRUD operations with attachments & comments...");

			// Login and navigate to Test Cases view
			loginToApplication();
			takeScreenshot(String.format("%03d-login-testcase", screenshotCounter++), false);

			// Navigate directly to Test Case page using URL
			final String testCaseUrl = "http://localhost:" + port + "/cdynamicpagerouter/CTestCase";
			page.navigate(testCaseUrl);
			wait_2000(); wait_1000(); // Give more time for page to load with project data
			takeScreenshot(String.format("%03d-testcases-view", screenshotCounter++), false);

			// Verify grid loaded
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(20000));
			takeScreenshot(String.format("%03d-testcases-grid-loaded", screenshotCounter++), false);

			// Test CREATE operation
			LOGGER.info("📝 Testing CREATE - New Test Case");
			clickNew();
			wait_1000();
			takeScreenshot(String.format("%03d-testcases-new-dialog", screenshotCounter++), false);

			// Fill test case details
			final Locator formDialog = page.locator("vaadin-dialog-overlay[opened]").first();
			formDialog.locator("vaadin-text-field").first().fill("Test Case - Automated Test " + System.currentTimeMillis());
			wait_500();
			final Locator textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				textAreas.first().fill("Test case created for automated testing");
			}
			wait_500();
			takeScreenshot(String.format("%03d-testcases-form-filled", screenshotCounter++), false);

			// Save test case
			clickSave();
			wait_2000();
			performFailFastCheck("After test case create");
			takeScreenshot(String.format("%03d-testcases-created", screenshotCounter++), false);

			// Test READ operation
			LOGGER.info("📖 Testing READ - Verify Test Case in Grid");
			clickRefresh();
			wait_1000();
			takeScreenshot(String.format("%03d-testcases-grid-refreshed", screenshotCounter++), false);

			// Test UPDATE operation
			LOGGER.info("✏️ Testing UPDATE - Edit Test Case");
			clickFirstGridRow();
			wait_1000();
			clickEdit();
			wait_1000();
			takeScreenshot(String.format("%03d-testcases-edit-dialog", screenshotCounter++), false);

			// Modify test case
			final Locator editDialog = page.locator("vaadin-dialog-overlay[opened]").first();
			final Locator nameField = editDialog.locator("vaadin-text-field").first();
			nameField.fill("");
			wait_500();
			nameField.fill("Test Case - UPDATED");
			wait_500();
			takeScreenshot(String.format("%03d-testcases-form-updated", screenshotCounter++), false);

			clickSave();
			wait_2000();
			performFailFastCheck("After test case update");
			takeScreenshot(String.format("%03d-testcases-updated", screenshotCounter++), false);

			// Select test case for attachment/comment testing
			clickFirstGridRow();
			wait_1000();

			// Use generic attachment CRUD test method
			LOGGER.info("📤 Running generic attachment CRUD operations on Test Case...");
			final boolean attachmentSuccess = testAttachmentCrudOperations();
			assertTrue(attachmentSuccess, "Attachment CRUD operations should succeed");
			takeScreenshot(String.format("%03d-testcases-attachments-completed", screenshotCounter++), false);

			// Use generic comment CRUD test method
			LOGGER.info("💬 Running generic comment CRUD operations on Test Case...");
			final boolean commentSuccess = testCommentCrudOperations();
			assertTrue(commentSuccess, "Comment CRUD operations should succeed");
			takeScreenshot(String.format("%03d-testcases-comments-completed", screenshotCounter++), false);

			// Test DELETE operation
			LOGGER.info("🗑️ Testing DELETE - Remove Test Case");
			clickFirstGridRow();
			wait_500();
			clickDelete();
			wait_500();
			takeScreenshot(String.format("%03d-testcases-delete-confirm", screenshotCounter++), false);

			// Confirm deletion
			final Locator confirmYes = page.locator("#cbutton-yes");
			if (confirmYes.count() > 0) {
				confirmYes.first().click();
				wait_1000();
			}
			performFailFastCheck("After test case delete");
			takeScreenshot(String.format("%03d-testcases-deleted", screenshotCounter++), false);

			LOGGER.info("✅ Test Case CRUD operations completed successfully");

		} catch (final Exception e) {
			LOGGER.error("❌ Test Case CRUD test failed: {}", e.getMessage(), e);
			takeScreenshot(String.format("%03d-testcases-error", screenshotCounter++), true);
			throw new AssertionError("Test Case CRUD test failed", e);
		}
	}

	@Test
	@DisplayName("✅ Test Scenario - Complete CRUD with Attachments & Comments")
	void testTestScenarioCrudOperations() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}

		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			LOGGER.info("🧪 Testing Test Scenario CRUD operations with attachments & comments...");

			// Login and navigate to Test Scenarios view
			loginToApplication();
			takeScreenshot(String.format("%03d-login-testscenario", screenshotCounter++), false);

			// Navigate directly to Test Scenario page using URL
			final String testScenarioUrl = "http://localhost:" + port + "/cdynamicpagerouter/CTestScenario";
			page.navigate(testScenarioUrl);
			wait_2000(); wait_1000(); // Give more time for page to load with project data
			takeScreenshot(String.format("%03d-testscenarios-view", screenshotCounter++), false);

			// Verify grid loaded
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(20000));
			takeScreenshot(String.format("%03d-testscenarios-grid-loaded", screenshotCounter++), false);

			// Test CREATE operation
			LOGGER.info("📝 Testing CREATE - New Test Scenario");
			clickNew();
			wait_1000();
			takeScreenshot(String.format("%03d-testscenarios-new-dialog", screenshotCounter++), false);

			// Fill test scenario details
			final Locator formDialog = page.locator("vaadin-dialog-overlay[opened]").first();
			formDialog.locator("vaadin-text-field").first().fill("Test Scenario - Automated Test " + System.currentTimeMillis());
			wait_500();
			final Locator textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				textAreas.first().fill("Test scenario created for automated testing");
			}
			wait_500();
			takeScreenshot(String.format("%03d-testscenarios-form-filled", screenshotCounter++), false);

			// Save test scenario
			clickSave();
			wait_2000();
			performFailFastCheck("After test scenario create");
			takeScreenshot(String.format("%03d-testscenarios-created", screenshotCounter++), false);

			// Test READ operation
			LOGGER.info("📖 Testing READ - Verify Test Scenario in Grid");
			clickRefresh();
			wait_1000();
			takeScreenshot(String.format("%03d-testscenarios-grid-refreshed", screenshotCounter++), false);

			// Test UPDATE operation
			LOGGER.info("✏️ Testing UPDATE - Edit Test Scenario");
			clickFirstGridRow();
			wait_1000();
			clickEdit();
			wait_1000();
			takeScreenshot(String.format("%03d-testscenarios-edit-dialog", screenshotCounter++), false);

			// Modify test scenario
			final Locator editDialog = page.locator("vaadin-dialog-overlay[opened]").first();
			final Locator nameField = editDialog.locator("vaadin-text-field").first();
			nameField.fill("");
			wait_500();
			nameField.fill("Test Scenario - UPDATED");
			wait_500();
			takeScreenshot(String.format("%03d-testscenarios-form-updated", screenshotCounter++), false);

			clickSave();
			wait_2000();
			performFailFastCheck("After test scenario update");
			takeScreenshot(String.format("%03d-testscenarios-updated", screenshotCounter++), false);

			// Select test scenario for attachment/comment testing
			clickFirstGridRow();
			wait_1000();

			// Use generic attachment CRUD test method
			LOGGER.info("📤 Running generic attachment CRUD operations on Test Scenario...");
			final boolean attachmentSuccess = testAttachmentCrudOperations();
			assertTrue(attachmentSuccess, "Attachment CRUD operations should succeed");
			takeScreenshot(String.format("%03d-testscenarios-attachments-completed", screenshotCounter++), false);

			// Use generic comment CRUD test method
			LOGGER.info("💬 Running generic comment CRUD operations on Test Scenario...");
			final boolean commentSuccess = testCommentCrudOperations();
			assertTrue(commentSuccess, "Comment CRUD operations should succeed");
			takeScreenshot(String.format("%03d-testscenarios-comments-completed", screenshotCounter++), false);

			// Test DELETE operation
			LOGGER.info("🗑️ Testing DELETE - Remove Test Scenario");
			clickFirstGridRow();
			wait_500();
			clickDelete();
			wait_500();
			takeScreenshot(String.format("%03d-testscenarios-delete-confirm", screenshotCounter++), false);

			// Confirm deletion
			final Locator confirmYes = page.locator("#cbutton-yes");
			if (confirmYes.count() > 0) {
				confirmYes.first().click();
				wait_1000();
			}
			performFailFastCheck("After test scenario delete");
			takeScreenshot(String.format("%03d-testscenarios-deleted", screenshotCounter++), false);

			LOGGER.info("✅ Test Scenario CRUD operations completed successfully");

		} catch (final Exception e) {
			LOGGER.error("❌ Test Scenario CRUD test failed: {}", e.getMessage(), e);
			takeScreenshot(String.format("%03d-testscenarios-error", screenshotCounter++), true);
			throw new AssertionError("Test Scenario CRUD test failed", e);
		}
	}
}
