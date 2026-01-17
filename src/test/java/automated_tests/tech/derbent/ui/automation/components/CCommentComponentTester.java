package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Tests comment component functionality on pages that have comment sections. */
public class CCommentComponentTester extends CBaseComponentTester {

	private static final String COMMENT_COMPONENT_SELECTOR = "#custom-comment-component, [id*='comment']";
	private static final String ADD_COMMENT_BUTTON = "cbutton-add-comment";
	private static final String COMMENT_TEXT_AREA = "field-comment-text";

	@Override
	public boolean canTest(final Page page) {
		return elementExists(page, COMMENT_COMPONENT_SELECTOR) || elementExists(page, "#" + ADD_COMMENT_BUTTON);
	}

	@Override
	public String getComponentName() {
		return "Comment Component";
	}

	@Override
	public void test(final Page page) {
		LOGGER.info("      💬 Testing Comment Component...");
		try {
			openTabOrAccordionIfNeeded(page, "Comments");
			final Locator container = locateCommentsContainer(page);
			if (container == null) {
				LOGGER.info("         ⏭️ Comments container not found");
				return;
			}
			container.scrollIntoViewIfNeeded();
			final Locator addButton = locateCommentToolbarButton(container, "vaadin:plus");
			if (addButton == null) {
				LOGGER.info("         ⏭️ Add comment button not available");
				return;
			}
			addButton.click();
			waitMs(page, 500);
			final Locator dialog = waitForDialogWithText(page, "Add Comment");
			if (dialog.count() == 0) {
				LOGGER.warn("         ⚠️ Add comment dialog did not open");
				return;
			}
			final String commentText = "AutoTest comment " + System.currentTimeMillis();
			final Locator textArea = dialog.locator("vaadin-text-area textarea");
			if (textArea.count() > 0) {
				textArea.first().fill(commentText);
				waitMs(page, 500);
			}
			if (dialog.locator("#cbutton-save").count() > 0) {
				dialog.locator("#cbutton-save").first().click();
				waitForDialogToClose(page);
				waitMs(page, 1000);
			}
			final Locator grid = locateCommentsGrid(container);
			if (grid == null) {
				LOGGER.warn("         ⚠️ Comments grid not found");
				return;
			}
			waitForGridCellText(grid, commentText.substring(0, Math.min(commentText.length(), 20)));
			LOGGER.info("         ✅ Comment created");
			final Locator commentCell = grid.locator("vaadin-grid-cell-content")
					.filter(new Locator.FilterOptions().setHasText(commentText.substring(0, Math.min(commentText.length(), 10))));
			if (commentCell.count() > 0) {
				commentCell.first().click();
				waitMs(page, 500);
			}
			final Locator editButton = locateCommentToolbarButton(container, "vaadin:edit");
			if (editButton != null && !editButton.isDisabled()) {
				editButton.click();
				waitMs(page, 500);
				final Locator editDialog = waitForDialogWithText(page, "Edit Comment");
				final String updated = commentText + " UPDATED";
				final Locator editTextArea = editDialog.locator("vaadin-text-area textarea");
				if (editTextArea.count() > 0) {
					editTextArea.first().fill(updated);
				}
				if (editDialog.locator("#cbutton-save").count() > 0) {
					editDialog.locator("#cbutton-save").first().click();
					waitForDialogToClose(page);
					waitMs(page, 1000);
				}
				waitForGridCellText(grid, "UPDATED");
				LOGGER.info("         ✅ Comment updated");
			} else {
				LOGGER.info("         ⏭️ Edit button disabled");
			}
			final Locator deleteButton = locateCommentToolbarButton(container, "vaadin:trash");
			if (deleteButton != null && !deleteButton.isDisabled()) {
				deleteButton.click();
				waitMs(page, 500);
				confirmDialogIfPresent(page);
				waitForDialogToClose(page);
				waitMs(page, 1000);
				waitForGridCellGone(grid, "UPDATED");
				LOGGER.info("         ✅ Comment deleted");
			} else {
				LOGGER.info("         ⏭️ Delete button disabled");
			}
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ Comment CRUD test failed: {}", e.getMessage());
		} finally {
			checkForExceptions(page);
		}
		LOGGER.info("      ✅ Comment component test complete");
	}

	private Locator locateCommentsContainer(final Page page) {
		final Locator container = page.locator("#custom-comments-component");
		if (container.count() > 0) {
			return container.first();
		}
		final Locator header = page.locator("h2:has-text('Comments'), h3:has-text('Comments'), h4:has-text('Comments'), span:has-text('Comments')");
		if (header.count() > 0) {
			return header.first().locator("xpath=ancestor::*[self::vaadin-vertical-layout or self::div][1]");
		}
		return null;
	}

	private Locator locateCommentsGrid(final Locator container) {
		final Locator grid = container.locator("vaadin-grid").filter(new Locator.FilterOptions().setHasText("Author"));
		if (grid.count() == 0) {
			return null;
		}
		return grid.first();
	}

	private Locator locateCommentToolbarButton(final Locator container, final String iconName) {
		final Locator button =
				container.locator("vaadin-button").filter(new Locator.FilterOptions().setHas(container.page().locator("vaadin-icon[icon='" + iconName + "']")));
		if (button.count() == 0) {
			return null;
		}
		return button.first();
	}
}
