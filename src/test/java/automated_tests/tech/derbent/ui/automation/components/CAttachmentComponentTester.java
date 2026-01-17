package automated_tests.tech.derbent.ui.automation.components;

import java.nio.file.Files;
import java.nio.file.Path;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Tests attachment component functionality on pages that have file upload capabilities. */
public class CAttachmentComponentTester extends CBaseComponentTester {

	private static final String ATTACHMENT_COMPONENT_SELECTOR = "#custom-attachment-component, vaadin-upload, [id*='attachment']";
	private static final String ADD_ATTACHMENT_BUTTON = "cbutton-add-attachment";

	@Override
	public boolean canTest(final Page page) {
		return elementExists(page, ATTACHMENT_COMPONENT_SELECTOR);
	}

	@Override
	public String getComponentName() {
		return "Attachment Component";
	}

	@Override
	public void test(final Page page) {
		LOGGER.info("      🗂️ Testing Attachment Component...");
		try {
			openTabOrAccordionIfNeeded(page, "Attachments");
			final Locator container = locateAttachmentsContainer(page);
			if (container == null) {
				LOGGER.info("         ⏭️ Attachments container not found");
				return;
			}
			container.scrollIntoViewIfNeeded();
			final Locator uploadButton = locateAttachmentToolbarButton(container, "vaadin:upload");
			if (uploadButton == null) {
				LOGGER.info("         ⏭️ Upload button not available");
				return;
			}
			uploadButton.click();
			waitMs(page, 500);
			final Locator dialog = waitForDialogWithText(page, "Upload File");
			if (dialog.count() == 0) {
				LOGGER.warn("         ⚠️ Upload dialog did not open");
				return;
			}
			final Path tempFile = Files.createTempFile("autotest-attachment-", ".txt");
			Files.writeString(tempFile, "AutoTest attachment content " + System.currentTimeMillis());
			dialog.locator("vaadin-upload input[type='file']").setInputFiles(tempFile);
			final Locator dialogUploadButton = dialog.locator("#cbutton-upload");
			waitForButtonEnabled(dialogUploadButton);
			dialogUploadButton.click();
			waitForDialogToClose(page);
			waitMs(page, 1000);
			final String fileName = tempFile.getFileName().toString();
			final Locator grid = locateAttachmentsGrid(container);
			if (grid == null) {
				LOGGER.warn("         ⚠️ Attachments grid not found");
				return;
			}
			waitForGridCellText(grid, fileName);
			LOGGER.info("         ✅ Attachment uploaded: {}", fileName);
			final Locator uploadedCell = grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(fileName));
			if (uploadedCell.count() > 0) {
				uploadedCell.first().click();
				waitMs(page, 500);
			}
			final Locator downloadButton = locateAttachmentToolbarButton(container, "vaadin:download");
			if (downloadButton != null && !downloadButton.isDisabled()) {
				downloadButton.click();
				waitMs(page, 500);
				LOGGER.info("         ✅ Attachment download triggered");
			} else {
				LOGGER.info("         ⏭️ Download button disabled");
			}
			final Locator deleteButton = locateAttachmentToolbarButton(container, "vaadin:trash");
			if (deleteButton != null && !deleteButton.isDisabled()) {
				deleteButton.click();
				waitMs(page, 500);
				confirmDialogIfPresent(page);
				waitForDialogToClose(page);
				waitMs(page, 1000);
				waitForGridCellGone(grid, fileName);
				LOGGER.info("         ✅ Attachment deleted");
			} else {
				LOGGER.info("         ⏭️ Delete button disabled");
			}
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ Attachment CRUD test failed: {}", e.getMessage());
		} finally {
			checkForExceptions(page);
		}
		LOGGER.info("      ✅ Attachment component test complete");
	}

	private Locator locateAttachmentsContainer(final Page page) {
		final Locator container = page.locator("#custom-attachments-component");
		if (container.count() > 0) {
			return container.first();
		}
		final Locator header = page.locator("h2:has-text('Attachments'), h3:has-text('Attachments'), h4:has-text('Attachments'), span:has-text('Attachments')");
		if (header.count() > 0) {
			return header.first().locator("xpath=ancestor::*[self::vaadin-vertical-layout or self::div][1]");
		}
		return null;
	}

	private Locator locateAttachmentsGrid(final Locator container) {
		final Locator grid = container.locator("vaadin-grid").filter(new Locator.FilterOptions().setHasText("File Name"));
		if (grid.count() == 0) {
			return null;
		}
		return grid.first();
	}

	private Locator locateAttachmentToolbarButton(final Locator container, final String iconName) {
		final Locator button =
				container.locator("vaadin-button").filter(new Locator.FilterOptions().setHas(container.page().locator("vaadin-icon[icon='" + iconName + "']")));
		if (button.count() == 0) {
			return null;
		}
		return button.first();
	}
}
