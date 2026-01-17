package automated_tests.tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assumptions;


@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("💬 Comment CRUD Operations Test")
public class CCommentPlaywrightTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCommentPlaywrightTest.class);

	private static Stream<String> commentEntityTypes() {
		return Stream.of("CActivity", "CRisk", "CMeeting", "CProject", "CIssue", "CProduct", "CDeliverable");
	}

	private int screenshotCounter = 1;

	@Override
	protected Locator locateCommentsContainer() {
		openCommentsSectionIfNeeded();
		final Locator container = page.locator("#custom-comments-component");
		if (container.count() > 0) {
			return container.first();
		}
		final String selector = "h2:has-text('Comments'), h3:has-text('Comments'), h4:has-text('Comments'), span:has-text('Comments')";
		page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(15000));
		final Locator header = page.locator(selector);
		assertTrue(header.count() > 0, "Comments section header not found");
		return header.first().locator("xpath=ancestor::*[self::vaadin-vertical-layout or self::div][1]");
	}

	private Locator locateCommentsGrid(final Locator container) {
		final Locator grid = container.locator("vaadin-grid").filter(new Locator.FilterOptions().setHasText("Author"));
		assertTrue(grid.count() > 0, "Comments grid not found");
		return grid.first();
	}

	private Locator locateCommentToolbarButton(final Locator container, final String iconName) {
		final Locator button =
				container.locator("vaadin-button").filter(new Locator.FilterOptions().setHas(page.locator("vaadin-icon[icon='" + iconName + "']")));
		assertTrue(button.count() > 0, "Toolbar button not found for icon " + iconName);
		return button.first();
	}

	private void navigateToActivitiesFallback() {
		final String url = "http://localhost:" + port + "/cdynamicpagerouter/page:3";
		page.navigate(url);
		wait_2000();
	}

	private void openCommentsSectionIfNeeded() {
		final Locator tab = page.locator("vaadin-tab").filter(new Locator.FilterOptions().setHasText("Comments"));
		if (tab.count() > 0) {
			tab.first().click();
			wait_500();
			return;
		}
		final Locator accordion = page.locator("vaadin-accordion-panel").filter(new Locator.FilterOptions().setHasText("Comments"));
		if (accordion.count() > 0) {
			final Locator heading = accordion.first().locator("vaadin-accordion-heading, [part='summary']");
			if (heading.count() > 0) {
				heading.first().click();
			} else {
				accordion.first().click();
			}
			wait_500();
		}
	}

	@ParameterizedTest (name = "✅ Comment lifecycle on {0}")
	@MethodSource ("commentEntityTypes")
	void testCommentLifecycleOnEntity(final String entityType) {
		// Check if browser is available
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			loginToApplication();
			takeScreenshot(String.format("%03d-login", screenshotCounter++), false);
			final boolean navigated = navigateToDynamicPageByEntityType(entityType);
			if (!navigated) {
				if ("CActivity".equals(entityType)) {
					LOGGER.warn("⚠️ Menu navigation failed for CActivity, trying direct route fallback");
					navigateToActivitiesFallback();
				} else {
					throw new AssertionError("Navigation failed for entity type: " + entityType);
				}
			}
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(15000));
			clickFirstGridRow();
			wait_1000();
			takeScreenshot(String.format("%03d-%s-selected", screenshotCounter++, entityType.toLowerCase()), false);
			final Locator commentsContainer = locateCommentsContainer();
			commentsContainer.scrollIntoViewIfNeeded();
			takeScreenshot(String.format("%03d-comments-visible", screenshotCounter++), false);
			// Test Add Comment
			final Locator addButton = locateCommentToolbarButton(commentsContainer, "vaadin:plus");
			addButton.click();
			wait_500();
			final Locator dialog = waitForDialogWithText("Add Comment");
			final Locator commentTextArea = dialog.locator("vaadin-text-area");
			final String commentText = "Test comment created at " + System.currentTimeMillis();
			commentTextArea.fill(commentText);
			// Mark as important
			final Locator importantCheckbox = dialog.locator("vaadin-checkbox").filter(new Locator.FilterOptions().setHasText("Important"));
			if (importantCheckbox.count() > 0) {
				importantCheckbox.first().click();
				wait_500();
			}
			takeScreenshot(String.format("%03d-comment-ready", screenshotCounter++), false);
			final Locator saveButton = dialog.locator("#cbutton-save");
			waitForButtonEnabled(saveButton);
			saveButton.click();
			waitForDialogToClose();
			wait_1000();
			// Verify comment appears in grid (use getCommentPreview logic)
			final Locator commentsGrid = locateCommentsGrid(commentsContainer);
			final String previewText = commentText.length() > 100 ? commentText.substring(0, 100) : commentText;
			waitForGridCellText(commentsGrid, previewText.substring(0, Math.min(50, previewText.length())));
			takeScreenshot(String.format("%03d-comment-added", screenshotCounter++), false);
			// Test expand details by clicking on the comment row
			final Locator commentCell =
					commentsGrid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(commentText.substring(0, 20)));
			commentCell.first().click();
			wait_500();
			takeScreenshot(String.format("%03d-comment-expanded", screenshotCounter++), false);
			// Verify expanded details are visible (full comment text should be visible)
			final Locator expandedDetails = page.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(commentText));
			assertTrue(expandedDetails.count() > 0, "Expanded comment details should be visible");
			// Collapse by clicking again
			commentCell.first().click();
			wait_500();
			takeScreenshot(String.format("%03d-comment-collapsed", screenshotCounter++), false);
			// Select comment for edit/delete
			commentCell.first().click();
			wait_500();
			final Locator editButton = locateCommentToolbarButton(commentsContainer, "vaadin:edit");
			final Locator deleteButton = locateCommentToolbarButton(commentsContainer, "vaadin:trash");
			assertTrue(!editButton.isDisabled(), "Edit button should be enabled after selection");
			assertTrue(!deleteButton.isDisabled(), "Delete button should be enabled after selection");
			// Test Edit Comment
			editButton.click();
			wait_500();
			final Locator editDialog = waitForDialogWithText("Edit Comment");
			final Locator editTextArea = editDialog.locator("vaadin-text-area");
			final String updatedComment = commentText + " - UPDATED";
			editTextArea.fill(updatedComment);
			takeScreenshot(String.format("%03d-comment-editing", screenshotCounter++), false);
			final Locator editSaveButton = editDialog.locator("#cbutton-save");
			editSaveButton.click();
			waitForDialogToClose();
			wait_1000();
			// Verify updated comment
			waitForGridCellText(commentsGrid, "UPDATED");
			takeScreenshot(String.format("%03d-comment-updated", screenshotCounter++), false);
			// Test Delete Comment
			final Locator updatedCell = commentsGrid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText("UPDATED"));
			updatedCell.first().click();
			wait_500();
			deleteButton.click();
			wait_500();
			// Confirm deletion
			final Locator confirmYes = page.locator("#cbutton-yes");
			if (confirmYes.count() > 0) {
				confirmYes.first().click();
			}
			waitForDialogToClose();
			wait_1000();
			waitForGridCellGone(commentsGrid, "UPDATED");
			takeScreenshot(String.format("%03d-comment-deleted", screenshotCounter++), false);
			performFailFastCheck("After comment delete");
		} catch (final Exception e) {
			LOGGER.error("Comment lifecycle test failed: {}", e.getMessage(), e);
			takeScreenshot(String.format("%03d-comment-error", screenshotCounter++), true);
			throw new AssertionError("Comment lifecycle test failed", e);
		}
	}

	@Override
	protected void waitForButtonEnabled(final Locator button) {
		final int maxAttempts = 12;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			if (!button.isDisabled()) {
				return;
			}
			wait_500();
		}
		throw new AssertionError("Button did not become enabled");
	}

	@Override
	protected void waitForDialogToClose() {
		final int maxAttempts = 10;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			if (page.locator("vaadin-dialog-overlay[opened]").count() == 0) {
				return;
			}
			wait_500();
		}
	}

	@Override
	protected Locator waitForDialogWithText(final String text) {
		final int maxAttempts = 10;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			final Locator overlay = page.locator("vaadin-dialog-overlay[opened]").filter(new Locator.FilterOptions().setHasText(text));
			if (overlay.count() > 0) {
				return overlay.first();
			}
			wait_500();
		}
		throw new AssertionError("Dialog with text '" + text + "' did not open");
	}

	@Override
	protected void waitForGridCellGone(final Locator grid, final String text) {
		final int maxAttempts = 12;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			final Locator matches = grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(text));
			if (matches.count() == 0) {
				return;
			}
			if (!matches.first().isVisible()) {
				return;
			}
			wait_500();
		}
		throw new AssertionError("Comment row still present after delete: " + text);
	}

	@Override
	protected void waitForGridCellText(final Locator grid, final String text) {
		final int maxAttempts = 12;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			if (grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(text)).count() > 0) {
				return;
			}
			wait_500();
		}
		throw new AssertionError("Expected comment row not found: " + text);
	}
}
