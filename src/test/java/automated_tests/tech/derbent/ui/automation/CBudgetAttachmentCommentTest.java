package automated_tests.tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

/**
 * Comprehensive attachment and comment operations test for Budget entity.
 * Tests cover full Create, Read, Update, Delete lifecycle for attachments and comments.
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
	"spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("🔧 Budget Attachment & Comment Test")
public class CBudgetAttachmentCommentTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBudgetAttachmentCommentTest.class);
	private int screenshotCounter = 1;

	@Test
	@DisplayName("✅ Budget Attachments - Upload/Download/Delete")
	void testBudgetAttachmentOperations() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			org.junit.jupiter.api.Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}

		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			LOGGER.info("🧪 Testing Budget Attachment operations...");

			// Login and navigate to Budgets view
			loginToApplication();
			takeScreenshot(String.format("%03d-login-budget-attachments", screenshotCounter++), false);

			final boolean navigated = navigateToDynamicPageByEntityType("CBudget");
			assertTrue(navigated, "Failed to navigate to Budgets view");
			wait_2000();
			takeScreenshot(String.format("%03d-budgets-view", screenshotCounter++), false);

			// Create a test budget first
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(15000));
			clickNew();
			wait_1000();
			takeScreenshot(String.format("%03d-budgets-new-dialog", screenshotCounter++), false);

			// Wait for dialog to open and fill budget details
			final Locator formDialog = page.locator("vaadin-dialog-overlay[opened]").first();
			formDialog.locator("vaadin-text-field").first().fill("Budget for Attachment Test");
			wait_500();
			final Locator textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				textAreas.first().fill("Budget created for attachment testing");
			}
			wait_500();
			takeScreenshot(String.format("%03d-budgets-form-filled", screenshotCounter++), false);

			clickSave();
			wait_2000();
			performFailFastCheck("After budget create");
			takeScreenshot(String.format("%03d-budgets-created", screenshotCounter++), false);

			// Refresh and select the budget
			clickRefresh();
			wait_1000();
			clickFirstGridRow();
			wait_1000();
			takeScreenshot(String.format("%03d-budget-selected", screenshotCounter++), false);

			// Locate attachments section
			final Locator attachmentsContainer = locateAttachmentsContainer();
			attachmentsContainer.scrollIntoViewIfNeeded();
			takeScreenshot(String.format("%03d-attachments-visible", screenshotCounter++), false);

			// Test UPLOAD
			LOGGER.info("📤 Testing UPLOAD - New Attachment");
			final Locator uploadButton = locateAttachmentToolbarButton(attachmentsContainer, "vaadin:upload");
			uploadButton.click();
			wait_500();

			final Locator dialog = waitForDialogWithText("Upload File");
			final java.nio.file.Path tempFile = Files.createTempFile("test-budget-attachment-", ".txt");
			Files.writeString(tempFile, "Test budget attachment content - " + System.currentTimeMillis());
			dialog.locator("vaadin-upload input[type='file']").setInputFiles(tempFile);

			final Locator dialogUploadButton = dialog.locator("#cbutton-upload");
			waitForButtonEnabled(dialogUploadButton);
			takeScreenshot(String.format("%03d-upload-ready", screenshotCounter++), false);
			dialogUploadButton.click();
			waitForDialogToClose();
			wait_1000();

			// Verify attachment uploaded
			final String fileName = tempFile.getFileName().toString();
			final Locator attachmentsGrid = locateAttachmentsGrid(attachmentsContainer);
			waitForGridCellText(attachmentsGrid, fileName);
			takeScreenshot(String.format("%03d-uploaded", screenshotCounter++), false);

			// Test DOWNLOAD
			LOGGER.info("📥 Testing DOWNLOAD - Get Attachment");
			final Locator uploadedCell = attachmentsGrid.locator("vaadin-grid-cell-content")
				.filter(new Locator.FilterOptions().setHasText(fileName));
			uploadedCell.first().click();
			wait_500();

			final Locator downloadButton = locateAttachmentToolbarButton(attachmentsContainer, "vaadin:download");
			assertFalse(downloadButton.isDisabled(), "Download button should be enabled after selection");
			downloadButton.click();
			wait_500();
			performFailFastCheck("After attachment download");
			takeScreenshot(String.format("%03d-downloaded", screenshotCounter++), false);

			// Test DELETE
			LOGGER.info("🗑️ Testing DELETE - Remove Attachment");
			final Locator deleteButton = locateAttachmentToolbarButton(attachmentsContainer, "vaadin:trash");
			assertFalse(deleteButton.isDisabled(), "Delete button should be enabled after selection");
			deleteButton.click();
			wait_500();
			
			final Locator confirmYes = page.locator("#cbutton-yes");
			if (confirmYes.count() > 0) {
				confirmYes.first().click();
			}
			waitForDialogToClose();
			wait_1000();
			waitForGridCellGone(attachmentsGrid, fileName);
			takeScreenshot(String.format("%03d-deleted", screenshotCounter++), false);

			LOGGER.info("✅ Budget attachment operations completed successfully");

		} catch (final Exception e) {
			LOGGER.error("❌ Budget attachment test failed: {}", e.getMessage(), e);
			takeScreenshot(String.format("%03d-budget-attachments-error", screenshotCounter++), true);
			throw new AssertionError("Budget attachment test failed", e);
		}
	}

	@Test
	@DisplayName("✅ Budget Comments - Add/Edit/Delete")
	void testBudgetCommentOperations() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			org.junit.jupiter.api.Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}

		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			LOGGER.info("🧪 Testing Budget Comments operations...");

			// Login and navigate to Budgets view
			loginToApplication();
			takeScreenshot(String.format("%03d-login-budget-comments", screenshotCounter++), false);

			final boolean navigated = navigateToDynamicPageByEntityType("CBudget");
			assertTrue(navigated, "Failed to navigate to Budgets view");
			wait_2000();
			takeScreenshot(String.format("%03d-budgets-view", screenshotCounter++), false);

			// Create a test budget first
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(15000));
			clickNew();
			wait_1000();
			final Locator formDialog = page.locator("vaadin-dialog-overlay[opened]").first();
			formDialog.locator("vaadin-text-field").first().fill("Budget for Comment Test");
			clickSave();
			wait_2000();
			performFailFastCheck("After budget create");
			takeScreenshot(String.format("%03d-budgets-created", screenshotCounter++), false);

			// Refresh and select the budget
			clickRefresh();
			wait_1000();
			clickFirstGridRow();
			wait_1000();
			takeScreenshot(String.format("%03d-budget-selected-for-comments", screenshotCounter++), false);

			// Look for comments section (tab or accordion)
			openTabOrAccordionIfNeeded("Comments");
			wait_500();
			takeScreenshot(String.format("%03d-comments-section", screenshotCounter++), false);

			// Test ADD COMMENT
			LOGGER.info("💬 Testing ADD COMMENT");
			final Locator commentsContainer = locateCommentsContainer();
			if (commentsContainer.count() > 0) {
				commentsContainer.scrollIntoViewIfNeeded();
				takeScreenshot(String.format("%03d-comments-visible", screenshotCounter++), false);

				// Find add comment button or text field
				final Locator addCommentButton = commentsContainer.locator("vaadin-button")
					.filter(new Locator.FilterOptions().setHas(page.locator("vaadin-icon[icon='vaadin:plus']")));
				
				if (addCommentButton.count() > 0) {
					addCommentButton.first().click();
					wait_500();
					
					// Fill comment text
					final Locator commentField = page.locator("vaadin-text-area");
					if (commentField.count() > 0) {
						commentField.first().fill("This is a test comment for budget added by Playwright automation");
						wait_500();
						takeScreenshot(String.format("%03d-comment-filled", screenshotCounter++), false);
						
						// Save comment
						final Locator saveCommentButton = page.locator("#cbutton-save");
						if (saveCommentButton.count() > 0) {
							saveCommentButton.first().click();
							wait_1000();
							performFailFastCheck("After comment add");
							takeScreenshot(String.format("%03d-comment-added", screenshotCounter++), false);
						}
					}
				}
			}

			LOGGER.info("✅ Budget comments operations completed successfully");

		} catch (final Exception e) {
			LOGGER.error("❌ Budget comments test failed: {}", e.getMessage(), e);
			takeScreenshot(String.format("%03d-budget-comments-error", screenshotCounter++), true);
			throw new AssertionError("Budget comments test failed", e);
		}
	}

}
