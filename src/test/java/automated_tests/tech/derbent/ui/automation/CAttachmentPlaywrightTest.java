package automated_tests.tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
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

@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("📎 Attachment Upload/Download/Delete Test")
public class CAttachmentPlaywrightTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CAttachmentPlaywrightTest.class);
	private int screenshotCounter = 1;

	@ParameterizedTest (name = "✅ Attachment lifecycle on {0}")
	@MethodSource ("attachmentEntityTypes")
	void testAttachmentLifecycleOnEntity(final String entityType) {
		// Check if browser is available
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			org.junit.jupiter.api.Assumptions.assumeTrue(false, "Browser not available in CI environment");
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
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(15000));
			clickFirstGridRow();
			wait_1000();
			takeScreenshot(String.format("%03d-%s-selected", screenshotCounter++, entityType.toLowerCase()), false);

			final Locator attachmentsContainer = locateAttachmentsContainer();
			attachmentsContainer.scrollIntoViewIfNeeded();
			takeScreenshot(String.format("%03d-attachments-visible", screenshotCounter++), false);

			final Locator uploadButton = locateAttachmentToolbarButton(attachmentsContainer, "vaadin:upload");
			uploadButton.click();
			wait_500();

			final Locator dialog = waitForDialogWithText("Upload File");
			final Path tempFile = Files.createTempFile("attachment-upload-", ".txt");
			Files.writeString(tempFile, "Attachment upload test content " + System.currentTimeMillis());
			dialog.locator("vaadin-upload input[type='file']").setInputFiles(tempFile);

			final Locator dialogUploadButton = dialog.locator("#cbutton-upload");
			waitForButtonEnabled(dialogUploadButton);
			takeScreenshot(String.format("%03d-upload-ready", screenshotCounter++), false);
			dialogUploadButton.click();
			waitForDialogToClose();
			wait_1000();

			final String fileName = tempFile.getFileName().toString();
			final Locator attachmentsGrid = locateAttachmentsGrid(attachmentsContainer);
			waitForGridCellText(attachmentsGrid, fileName);
			takeScreenshot(String.format("%03d-uploaded", screenshotCounter++), false);

			final Locator uploadedCell = attachmentsGrid.locator("vaadin-grid-cell-content")
					.filter(new Locator.FilterOptions().setHasText(fileName));
			uploadedCell.first().click();
			wait_500();

			final Locator downloadButton = locateAttachmentToolbarButton(attachmentsContainer, "vaadin:download");
			final Locator deleteButton = locateAttachmentToolbarButton(attachmentsContainer, "vaadin:trash");
			assertTrue(!downloadButton.isDisabled(), "Download button should be enabled after selection");
			assertTrue(!deleteButton.isDisabled(), "Delete button should be enabled after selection");

			downloadButton.click();
			wait_500();
			performFailFastCheck("After attachment download click");

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
		} catch (final Exception e) {
			LOGGER.error("Attachment lifecycle test failed: {}", e.getMessage(), e);
			takeScreenshot(String.format("%03d-attachment-error", screenshotCounter++), true);
			throw new AssertionError("Attachment lifecycle test failed", e);
		}
	}

	private static Stream<String> attachmentEntityTypes() {
		return Stream.of("CActivity", "CRisk", "CMeeting", "CDecision", "CSprint", "COrder", "CProject", "CUser");
	}

	private Locator locateAttachmentsGrid(final Locator container) {
		final Locator grid = container.locator("vaadin-grid").filter(new Locator.FilterOptions().setHasText("File Name"));
		assertTrue(grid.count() > 0, "Attachments grid not found");
		return grid.first();
	}

	private Locator locateAttachmentToolbarButton(final Locator container, final String iconName) {
		final Locator button = container.locator("vaadin-button")
				.filter(new Locator.FilterOptions().setHas(page.locator("vaadin-icon[icon='" + iconName + "']")));
		assertTrue(button.count() > 0, "Toolbar button not found for icon " + iconName);
		return button.first();
	}

	private Locator waitForDialogWithText(final String text) {
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

	private void navigateToActivitiesFallback() {
		final String url = "http://localhost:" + port + "/cdynamicpagerouter/page:3";
		page.navigate(url);
		wait_2000();
	}

	private void waitForDialogToClose() {
		final int maxAttempts = 10;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			if (page.locator("vaadin-dialog-overlay[opened]").count() == 0) {
				return;
			}
			wait_500();
		}
	}

	private void waitForButtonEnabled(final Locator button) {
		final int maxAttempts = 12;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			if (!button.isDisabled()) {
				return;
			}
			wait_500();
		}
		throw new AssertionError("Upload button did not become enabled");
	}

	private void waitForGridCellText(final Locator grid, final String text) {
		final int maxAttempts = 12;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			if (grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(text)).count() > 0) {
				return;
			}
			wait_500();
		}
		throw new AssertionError("Expected attachment row not found: " + text);
	}

	private void waitForGridCellGone(final Locator grid, final String text) {
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
		throw new AssertionError("Attachment row still present after delete: " + text);
	}
}
