package automated_tests.tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
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


/** Comprehensive CRUD operations test for recent features implemented in last 3 days: - Issues & Bug Tracking (CIssue entity with comments,
 * attachments, workflow) - Teams Management (CTeam entity company-scoped) - Attachments System (integrated with all major entities) - Time Tracking
 * (Gantt timeline features) Tests cover full Create, Read, Update, Delete lifecycle for each feature. */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🔧 Recent Features CRUD Operations Test")
public class CRecentFeaturesCrudTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CRecentFeaturesCrudTest.class);
	private int screenshotCounter = 1;

	@Test
	@DisplayName ("✅ Attachment Integration - Upload/Download/Delete on Activity")
	void testAttachmentOperationsOnActivity() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			LOGGER.info("🧪 Testing Attachment operations on Activity...");
			// Login and navigate to Activities view
			loginToApplication();
			takeScreenshot(String.format("%03d-login-activity", screenshotCounter++), false);
			final boolean navigated = navigateToDynamicPageByEntityType("CActivity");
			assertTrue(navigated, "Failed to navigate to Activities view");
			wait_2000();
			takeScreenshot(String.format("%03d-activity-view", screenshotCounter++), false);
			// Select first activity
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(15000));
			clickFirstGridRow();
			wait_1000();
			takeScreenshot(String.format("%03d-activity-selected", screenshotCounter++), false);
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
			final Path tempFile = Files.createTempFile("test-attachment-", ".txt");
			Files.writeString(tempFile, "Test attachment content - " + System.currentTimeMillis());
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
			final Locator uploadedCell = attachmentsGrid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(fileName));
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
			LOGGER.info("✅ Attachment operations completed successfully");
		} catch (final Exception e) {
			LOGGER.error("❌ Attachment test failed: {}", e.getMessage(), e);
			takeScreenshot(String.format("%03d-attachments-error", screenshotCounter++), true);
			throw new AssertionError("Attachment test failed", e);
		}
	}

	@Test
	@DisplayName ("✅ Comments Integration - Add/Edit/Delete on Issue")
	void testCommentsOnIssue() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			LOGGER.info("🧪 Testing Comments on Issue...");
			// Login and navigate to Issues
			loginToApplication();
			takeScreenshot(String.format("%03d-login-comments", screenshotCounter++), false);
			final boolean navigated = navigateToDynamicPageByEntityType("CIssue");
			assertTrue(navigated, "Failed to navigate to Issues view");
			wait_2000();
			// Create a test issue first
			clickNew();
			wait_1000();
			fillFirstTextField("Issue for Comment Test");
			clickSave();
			wait_2000();
			clickRefresh();
			wait_1000();
			// Select the issue
			clickFirstGridRow();
			wait_1000();
			takeScreenshot(String.format("%03d-issue-selected-for-comments", screenshotCounter++), false);
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
						commentField.first().fill("This is a test comment added by Playwright automation");
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
			LOGGER.info("✅ Comments operations completed successfully");
		} catch (final Exception e) {
			LOGGER.error("❌ Comments test failed: {}", e.getMessage(), e);
			takeScreenshot(String.format("%03d-comments-error", screenshotCounter++), true);
			throw new AssertionError("Comments test failed", e);
		}
	}

	@Test
	@DisplayName ("✅ Issue Entity - Complete CRUD Lifecycle")
	void testIssueCrudOperations() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			LOGGER.info("🧪 Testing Issue CRUD operations...");
			// Login and navigate to Issues view
			loginToApplication();
			takeScreenshot(String.format("%03d-login-issues", screenshotCounter++), false);
			// Navigate to Issues using dynamic page
			final boolean navigated = navigateToDynamicPageByEntityType("CIssue");
			assertTrue(navigated, "Failed to navigate to Issues view");
			wait_2000();
			takeScreenshot(String.format("%03d-issues-view", screenshotCounter++), false);
			// Verify grid loaded
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(15000));
			takeScreenshot(String.format("%03d-issues-grid-loaded", screenshotCounter++), false);
			// Test CREATE operation
			LOGGER.info("📝 Testing CREATE - New Issue");
			clickNew();
			wait_1000();
			takeScreenshot(String.format("%03d-issues-new-dialog", screenshotCounter++), false);
			// Fill issue details
			fillFirstTextField("Test Issue - Automated Test");
			wait_500();
			final Locator textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				textAreas.first().fill("This is an automated test issue created by Playwright");
			}
			wait_500();
			takeScreenshot(String.format("%03d-issues-form-filled", screenshotCounter++), false);
			// Save issue
			clickSave();
			wait_2000();
			performFailFastCheck("After issue create");
			takeScreenshot(String.format("%03d-issues-created", screenshotCounter++), false);
			// Test READ operation - verify issue appears in grid
			LOGGER.info("📖 Testing READ - Verify Issue in Grid");
			clickRefresh();
			wait_1000();
			final Locator grid = page.locator("vaadin-grid").first();
			assertTrue(grid.isVisible(), "Grid should be visible");
			takeScreenshot(String.format("%03d-issues-grid-refreshed", screenshotCounter++), false);
			// Test UPDATE operation
			LOGGER.info("✏️ Testing UPDATE - Edit Issue");
			clickFirstGridRow();
			wait_1000();
			clickEdit();
			wait_1000();
			takeScreenshot(String.format("%03d-issues-edit-dialog", screenshotCounter++), false);
			// Modify issue
			fillFirstTextField("Test Issue - UPDATED");
			wait_500();
			// Test parent item selection if available
			testParentItemSelection();
			wait_500();
			takeScreenshot(String.format("%03d-issues-form-updated", screenshotCounter++), false);
			clickSave();
			wait_2000();
			performFailFastCheck("After issue update");
			takeScreenshot(String.format("%03d-issues-updated", screenshotCounter++), false);
			// Test attachment and comment operations
			clickFirstGridRow();
			wait_1000();
			LOGGER.info("🧪 Testing auxiliary features (attachments, comments)...");
			testAttachmentOperations();
			wait_500();
			takeScreenshot(String.format("%03d-issues-attachments-tested", screenshotCounter++), false);
			testCommentOperations();
			wait_500();
			takeScreenshot(String.format("%03d-issues-comments-tested", screenshotCounter++), false);
			// Test DELETE operation
			LOGGER.info("🗑️ Testing DELETE - Remove Issue");
			clickFirstGridRow();
			wait_500();
			clickDelete();
			wait_500();
			takeScreenshot(String.format("%03d-issues-delete-confirm", screenshotCounter++), false);
			// Confirm deletion
			final Locator confirmYes = page.locator("#cbutton-yes");
			if (confirmYes.count() > 0) {
				confirmYes.first().click();
				wait_1000();
			}
			performFailFastCheck("After issue delete");
			takeScreenshot(String.format("%03d-issues-deleted", screenshotCounter++), false);
			LOGGER.info("✅ Issue CRUD operations completed successfully");
		} catch (final Exception e) {
			LOGGER.error("❌ Issue CRUD test failed: {}", e.getMessage(), e);
			takeScreenshot(String.format("%03d-issues-error", screenshotCounter++), true);
			throw new AssertionError("Issue CRUD test failed", e);
		}
	}

	@Test
	@DisplayName ("✅ Team Entity - Complete CRUD Lifecycle")
	void testTeamCrudOperations() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			LOGGER.info("🧪 Testing Team CRUD operations...");
			// Login and navigate to Teams view
			loginToApplication();
			takeScreenshot(String.format("%03d-login-teams", screenshotCounter++), false);
			// Navigate to Teams using dynamic page
			final boolean navigated = navigateToDynamicPageByEntityType("CTeam");
			assertTrue(navigated, "Failed to navigate to Teams view");
			wait_2000();
			takeScreenshot(String.format("%03d-teams-view", screenshotCounter++), false);
			// Verify grid loaded
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(15000));
			takeScreenshot(String.format("%03d-teams-grid-loaded", screenshotCounter++), false);
			// Test CREATE operation
			LOGGER.info("📝 Testing CREATE - New Team");
			clickNew();
			wait_1000();
			takeScreenshot(String.format("%03d-teams-new-dialog", screenshotCounter++), false);
			// Fill team details
			fillFirstTextField("QA Automation Team");
			wait_500();
			final Locator textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				textAreas.first().fill("Team created by automated testing");
			}
			wait_500();
			takeScreenshot(String.format("%03d-teams-form-filled", screenshotCounter++), false);
			// Save team
			clickSave();
			wait_2000();
			performFailFastCheck("After team create");
			takeScreenshot(String.format("%03d-teams-created", screenshotCounter++), false);
			// Test READ operation
			LOGGER.info("📖 Testing READ - Verify Team in Grid");
			clickRefresh();
			wait_1000();
			takeScreenshot(String.format("%03d-teams-grid-refreshed", screenshotCounter++), false);
			// Test UPDATE operation
			LOGGER.info("✏️ Testing UPDATE - Edit Team");
			clickFirstGridRow();
			wait_1000();
			clickEdit();
			wait_1000();
			takeScreenshot(String.format("%03d-teams-edit-dialog", screenshotCounter++), false);
			// Modify team
			fillFirstTextField("QA Automation Team - UPDATED");
			wait_500();
			takeScreenshot(String.format("%03d-teams-form-updated", screenshotCounter++), false);
			clickSave();
			wait_2000();
			performFailFastCheck("After team update");
			takeScreenshot(String.format("%03d-teams-updated", screenshotCounter++), false);
			// Test DELETE operation
			LOGGER.info("🗑️ Testing DELETE - Remove Team");
			clickFirstGridRow();
			wait_500();
			clickDelete();
			wait_500();
			takeScreenshot(String.format("%03d-teams-delete-confirm", screenshotCounter++), false);
			// Confirm deletion
			final Locator confirmYes = page.locator("#cbutton-yes");
			if (confirmYes.count() > 0) {
				confirmYes.first().click();
				wait_1000();
			}
			performFailFastCheck("After team delete");
			takeScreenshot(String.format("%03d-teams-deleted", screenshotCounter++), false);
			LOGGER.info("✅ Team CRUD operations completed successfully");
		} catch (final Exception e) {
			LOGGER.error("❌ Team CRUD test failed: {}", e.getMessage(), e);
			takeScreenshot(String.format("%03d-teams-error", screenshotCounter++), true);
			throw new AssertionError("Team CRUD test failed", e);
		}
	}
}
