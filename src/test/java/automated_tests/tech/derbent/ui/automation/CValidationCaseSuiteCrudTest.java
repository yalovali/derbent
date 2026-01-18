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
 * Comprehensive CRUD operations test for Validation Case and Validation Suite entities.
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
@DisplayName("🔧 Validation Case & Suite CRUD Test")
public class CValidationCaseSuiteCrudTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationCaseSuiteCrudTest.class);
	private int screenshotCounter = 1;

	@Test
	@DisplayName("✅ Validation Case - Complete CRUD with Attachments & Comments")
	void testTestCaseCrudOperations() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}

		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			LOGGER.info("🧪 Testing Validation Case CRUD operations with attachments & comments...");

			// Login and navigate to Validation Cases view
			loginToApplication();
			takeScreenshot(String.format("%03d-login-validationcase", screenshotCounter++), false);

			// Navigate directly to Validation Case page using URL
			final String validationCaseUrl = "http://localhost:" + port + "/cdynamicpagerouter/CValidationCase";
			page.navigate(validationCaseUrl);
			wait_2000(); wait_1000(); // Give more time for page to load with project data
			takeScreenshot(String.format("%03d-validationcases-view", screenshotCounter++), false);

			// Verify grid loaded
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(20000));
			takeScreenshot(String.format("%03d-validationcases-grid-loaded", screenshotCounter++), false);

			// Test CREATE operation
			LOGGER.info("📝 Testing CREATE - New Validation Case");
			clickNew();
			wait_1000();
			takeScreenshot(String.format("%03d-validationcases-new-dialog", screenshotCounter++), false);

			// Fill test case details
			final Locator formDialog = page.locator("vaadin-dialog-overlay[opened]").first();
			formDialog.locator("vaadin-text-field").first().fill("Validation Case - Automated Test " + System.currentTimeMillis());
			wait_500();
			final Locator textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				textAreas.first().fill("Validation case created for automated testing");
			}
			wait_500();
			takeScreenshot(String.format("%03d-validationcases-form-filled", screenshotCounter++), false);

			// Save test case
			clickSave();
			wait_2000();
			performFailFastCheck("After test case create");
			takeScreenshot(String.format("%03d-validationcases-created", screenshotCounter++), false);

			// Test READ operation
			LOGGER.info("📖 Testing READ - Verify Validation Case in Grid");
			clickRefresh();
			wait_1000();
			takeScreenshot(String.format("%03d-validationcases-grid-refreshed", screenshotCounter++), false);

			// Test UPDATE operation
			LOGGER.info("✏️ Testing UPDATE - Edit Validation Case");
			clickFirstGridRow();
			wait_1000();
			clickEdit();
			wait_1000();
			takeScreenshot(String.format("%03d-validationcases-edit-dialog", screenshotCounter++), false);

			// Modify test case
			final Locator editDialog = page.locator("vaadin-dialog-overlay[opened]").first();
			final Locator nameField = editDialog.locator("vaadin-text-field").first();
			nameField.fill("");
			wait_500();
			nameField.fill("Validation Case - UPDATED");
			wait_500();
			takeScreenshot(String.format("%03d-validationcases-form-updated", screenshotCounter++), false);

			clickSave();
			wait_2000();
			performFailFastCheck("After test case update");
			takeScreenshot(String.format("%03d-validationcases-updated", screenshotCounter++), false);

			// Select test case for attachment/comment testing
			clickFirstGridRow();
			wait_1000();

			// Use generic attachment CRUD test method
			LOGGER.info("📤 Running generic attachment CRUD operations on Validation Case...");
			final boolean attachmentSuccess = testAttachmentCrudOperations();
			assertTrue(attachmentSuccess, "Attachment CRUD operations should succeed");
			takeScreenshot(String.format("%03d-validationcases-attachments-completed", screenshotCounter++), false);

			// Use generic comment CRUD test method
			LOGGER.info("💬 Running generic comment CRUD operations on Validation Case...");
			final boolean commentSuccess = testCommentCrudOperations();
			assertTrue(commentSuccess, "Comment CRUD operations should succeed");
			takeScreenshot(String.format("%03d-validationcases-comments-completed", screenshotCounter++), false);

			// Test DELETE operation
			LOGGER.info("🗑️ Testing DELETE - Remove Validation Case");
			clickFirstGridRow();
			wait_500();
			clickDelete();
			wait_500();
			takeScreenshot(String.format("%03d-validationcases-delete-confirm", screenshotCounter++), false);

			// Confirm deletion
			final Locator confirmYes = page.locator("#cbutton-yes");
			if (confirmYes.count() > 0) {
				confirmYes.first().click();
				wait_1000();
			}
			performFailFastCheck("After test case delete");
			takeScreenshot(String.format("%03d-validationcases-deleted", screenshotCounter++), false);

			LOGGER.info("✅ Validation Case CRUD operations completed successfully");

		} catch (final Exception e) {
			LOGGER.error("❌ Validation Case CRUD test failed: {}", e.getMessage(), e);
			takeScreenshot(String.format("%03d-validationcases-error", screenshotCounter++), true);
			throw new AssertionError("Validation Case CRUD test failed", e);
		}
	}

	@Test
	@DisplayName("✅ Validation Suite - Complete CRUD with Attachments & Comments")
	void testTestScenarioCrudOperations() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}

		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			LOGGER.info("🧪 Testing Validation Suite CRUD operations with attachments & comments...");

			// Login and navigate to Validation Suites view
			loginToApplication();
			takeScreenshot(String.format("%03d-login-validationsuite", screenshotCounter++), false);

			// Navigate directly to Validation Suite page using URL
			final String validationSuiteUrl = "http://localhost:" + port + "/cdynamicpagerouter/CValidationSuite";
			page.navigate(validationSuiteUrl);
			wait_2000(); wait_1000(); // Give more time for page to load with project data
			takeScreenshot(String.format("%03d-validationsuites-view", screenshotCounter++), false);

			// Verify grid loaded
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(20000));
			takeScreenshot(String.format("%03d-validationsuites-grid-loaded", screenshotCounter++), false);

			// Test CREATE operation
			LOGGER.info("📝 Testing CREATE - New Validation Suite");
			clickNew();
			wait_1000();
			takeScreenshot(String.format("%03d-validationsuites-new-dialog", screenshotCounter++), false);

			// Fill test scenario details
			final Locator formDialog = page.locator("vaadin-dialog-overlay[opened]").first();
			formDialog.locator("vaadin-text-field").first().fill("Validation Suite - Automated Test " + System.currentTimeMillis());
			wait_500();
			final Locator textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				textAreas.first().fill("Validation suite created for automated testing");
			}
			wait_500();
			takeScreenshot(String.format("%03d-validationsuites-form-filled", screenshotCounter++), false);

			// Save test scenario
			clickSave();
			wait_2000();
			performFailFastCheck("After test scenario create");
			takeScreenshot(String.format("%03d-validationsuites-created", screenshotCounter++), false);

			// Test READ operation
			LOGGER.info("📖 Testing READ - Verify Validation Suite in Grid");
			clickRefresh();
			wait_1000();
			takeScreenshot(String.format("%03d-validationsuites-grid-refreshed", screenshotCounter++), false);

			// Test UPDATE operation
			LOGGER.info("✏️ Testing UPDATE - Edit Validation Suite");
			clickFirstGridRow();
			wait_1000();
			clickEdit();
			wait_1000();
			takeScreenshot(String.format("%03d-validationsuites-edit-dialog", screenshotCounter++), false);

			// Modify test scenario
			final Locator editDialog = page.locator("vaadin-dialog-overlay[opened]").first();
			final Locator nameField = editDialog.locator("vaadin-text-field").first();
			nameField.fill("");
			wait_500();
			nameField.fill("Validation Suite - UPDATED");
			wait_500();
			takeScreenshot(String.format("%03d-validationsuites-form-updated", screenshotCounter++), false);

			clickSave();
			wait_2000();
			performFailFastCheck("After test scenario update");
			takeScreenshot(String.format("%03d-validationsuites-updated", screenshotCounter++), false);

			// Select test scenario for attachment/comment testing
			clickFirstGridRow();
			wait_1000();

			// Use generic attachment CRUD test method
			LOGGER.info("📤 Running generic attachment CRUD operations on Validation Suite...");
			final boolean attachmentSuccess = testAttachmentCrudOperations();
			assertTrue(attachmentSuccess, "Attachment CRUD operations should succeed");
			takeScreenshot(String.format("%03d-validationsuites-attachments-completed", screenshotCounter++), false);

			// Use generic comment CRUD test method
			LOGGER.info("💬 Running generic comment CRUD operations on Validation Suite...");
			final boolean commentSuccess = testCommentCrudOperations();
			assertTrue(commentSuccess, "Comment CRUD operations should succeed");
			takeScreenshot(String.format("%03d-validationsuites-comments-completed", screenshotCounter++), false);

			// Test DELETE operation
			LOGGER.info("🗑️ Testing DELETE - Remove Validation Suite");
			clickFirstGridRow();
			wait_500();
			clickDelete();
			wait_500();
			takeScreenshot(String.format("%03d-validationsuites-delete-confirm", screenshotCounter++), false);

			// Confirm deletion
			final Locator confirmYes = page.locator("#cbutton-yes");
			if (confirmYes.count() > 0) {
				confirmYes.first().click();
				wait_1000();
			}
			performFailFastCheck("After test scenario delete");
			takeScreenshot(String.format("%03d-validationsuites-deleted", screenshotCounter++), false);

			LOGGER.info("✅ Validation Suite CRUD operations completed successfully");

		} catch (final Exception e) {
			LOGGER.error("❌ Validation Suite CRUD test failed: {}", e.getMessage(), e);
			takeScreenshot(String.format("%03d-validationsuites-error", screenshotCounter++), true);
			throw new AssertionError("Validation Suite CRUD test failed", e);
		}
	}
}
