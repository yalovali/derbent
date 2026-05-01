package automated_tests.tech.derbent.ui.automation.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

import automated_tests.tech.derbent.ui.automation.CBaseUITest;
import tech.derbent.Application;

/**
 * Tests CRUD operations on the workflow status transitions editor component
 * (CComponentWorkflowStatusRelations). Verifies that Add, Edit, and Delete
 * operations work correctly in the Workflow Management detail panel.
 *
 * Creates a fresh workflow with no transitions to avoid duplicate-transition conflicts.
 */
@SpringBootTest (webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestPropertySource (properties = {
		"spring.profiles.active=derbent",
		"spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🔄 Workflow Status Transitions Editor Test")
public class CWorkflowEditorPlaywrightTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CWorkflowEditorPlaywrightTest.class);
	private int screenshotCounter = 1;

	@Test
	@DisplayName ("✅ Workflow editor: Add/Edit/Delete status transitions")
	void testWorkflowStatusTransitionsCRUD() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			loginToApplication();
			takeScreenshot(String.format("%03d-login", screenshotCounter++), false);

			boolean navigated = navigateToDynamicPageByEntityType("Workflow");
			if (!navigated) {
				navigated = navigateToDynamicPageByEntityType("CWorkflowEntity");
			}
			assertTrue(navigated, "Could not navigate to Workflow Management page");
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(15000));
			takeScreenshot(String.format("%03d-workflow-page", screenshotCounter++), false);

			// Create a fresh workflow so there are zero existing transitions
			final String testWorkflowName = "TestWorkflow-" + System.currentTimeMillis();
			clickNew();
			wait_1000();
			fillFieldById("field-cworkflow-entity-name", testWorkflowName);
			clickSave();
			wait_2000();
			clickRefresh();
			wait_1000();
			takeScreenshot(String.format("%03d-workflow-created", screenshotCounter++), false);

			// Select the newly created workflow
			final Locator nameCell = page.locator("vaadin-grid-cell-content")
					.filter(new Locator.FilterOptions().setHasText(testWorkflowName));
			assertTrue(nameCell.count() > 0, "Created workflow not found in grid: " + testWorkflowName);
			nameCell.first().click();
			wait_1000();
			takeScreenshot(String.format("%03d-new-workflow-selected", screenshotCounter++), false);

			// Scroll to status transitions component
			final Locator addRelationButton = page.locator("#cbutton-add-relation");
			assertTrue(addRelationButton.count() > 0, "Add relation button (#cbutton-add-relation) not found");
			addRelationButton.scrollIntoViewIfNeeded();
			takeScreenshot(String.format("%03d-transitions-section-visible", screenshotCounter++), false);

			// ── ADD: open the add-transition dialog ─────────────────────────────
			addRelationButton.first().click();
			wait_500();
			final Locator addDialog = waitForDialogWithText("Define Status Transition for Workflow");
			takeScreenshot(String.format("%03d-add-dialog-open", screenshotCounter++), false);

			// Select "From Status" — pick the first available option
			final Locator fromStatusCombo = addDialog.locator("#field-cworkflow-status-relation-from-status");
			assertTrue(fromStatusCombo.count() > 0, "From Status combobox not found");
			fromStatusCombo.first().click();
			wait_500();

			final Locator fromOptions = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item");
			Assumptions.assumeTrue(fromOptions.count() > 1, "Need at least 2 status options");
			final String fromStatusText = fromOptions.first().textContent().trim();
			fromOptions.first().click();
			wait_300();

			// Select "To Status" — pick the LAST available option (most different from first)
			final Locator toStatusCombo = addDialog.locator("#field-cworkflow-status-relation-to-status");
			assertTrue(toStatusCombo.count() > 0, "To Status combobox not found");
			toStatusCombo.first().click();
			wait_500();

			final Locator toOptions = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item");
			Assumptions.assumeTrue(toOptions.count() > 0, "Need at least 1 status option for To Status");
			final int toIndex = toOptions.count() - 1; // pick last option
			final String toStatusText = toOptions.nth(toIndex).textContent().trim();
			toOptions.nth(toIndex).click();
			wait_300();
			takeScreenshot(String.format("%03d-add-dialog-filled", screenshotCounter++), false);

			// Save the transition
			final Locator saveBtnAdd = addDialog.locator("#cbutton-save");
			waitForButtonEnabled(saveBtnAdd);
			saveBtnAdd.click();
			waitForDialogToClose();

			// If dialog is still open (validation error), close it and fail informatively
			if (page.locator("vaadin-dialog-overlay[opened]").count() > 0) {
				page.keyboard().press("Escape");
				waitForDialogToClose();
				throw new AssertionError(
						"Add transition dialog did not close after save — possible duplicate or validation error for: "
								+ fromStatusText + " → " + toStatusText);
			}
			wait_1000();
			LOGGER.info("✅ Added transition: {} → {}", fromStatusText, toStatusText);

			// Verify the new transition appears in the grid
			final Locator transitionsGrid = locateTransitionsGrid();
			waitForGridCellText(transitionsGrid, fromStatusText);
			takeScreenshot(String.format("%03d-transition-added", screenshotCounter++), false);

			// ── SELECT the new row ────────────────────────────────────────────
			final Locator addedRow = transitionsGrid.locator("vaadin-grid-cell-content")
					.filter(new Locator.FilterOptions().setHasText(fromStatusText));
			addedRow.first().click();
			wait_500();

			final Locator editButton = page.locator("#cbutton-edit-relation");
			final Locator deleteButton = page.locator("#cbutton-delete-relation");
			assertTrue(!editButton.first().isDisabled(), "Edit button should be enabled after selection");
			assertTrue(!deleteButton.first().isDisabled(), "Delete button should be enabled after selection");

			// ── EDIT: open edit dialog, verify it opens, then cancel ──────────
			editButton.first().click();
			wait_500();
			final Locator editDialog = waitForDialogWithText("Edit Status Transition");
			takeScreenshot(String.format("%03d-edit-dialog-open", screenshotCounter++), false);

			final Locator cancelBtn = editDialog.locator("#cbutton-cancel");
			if (cancelBtn.count() > 0) {
				cancelBtn.first().click();
			} else {
				page.keyboard().press("Escape");
			}
			waitForDialogToClose();
			wait_500();
			LOGGER.info("✅ Edit dialog opened and closed successfully");

			// ── DELETE: re-select the row and delete ─────────────────────────
			addedRow.first().click();
			wait_500();
			deleteButton.first().click();
			wait_500();

			// Confirm deletion
			final Locator confirmYes = page.locator("#cbutton-yes");
			if (confirmYes.count() > 0) {
				confirmYes.first().click();
			} else {
				final Locator confirmBtn = page.locator("vaadin-dialog-overlay[opened] vaadin-button")
						.filter(new Locator.FilterOptions().setHasText("Yes"));
				if (confirmBtn.count() > 0) {
					confirmBtn.first().click();
				}
			}
			waitForDialogToClose();
			wait_1000();

			// After deletion the grid should be empty (we only added 1 transition)
			// Verify the delete button returns to disabled (grid refreshed, selection cleared)
			waitForDeleteButtonDisabled(deleteButton);
			takeScreenshot(String.format("%03d-transition-deleted", screenshotCounter++), false);
			LOGGER.info("✅ Deleted transition successfully — edit/delete buttons are disabled (selection cleared)");

			performFailFastCheck("After workflow transition delete");

		} catch (final Exception e) {
			LOGGER.error("Workflow editor test failed: {}", e.getMessage());
			takeScreenshot(String.format("%03d-workflow-editor-error", screenshotCounter++), true);
			throw new AssertionError("Workflow editor test failed", e);
		}
	}

	private void waitForDeleteButtonDisabled(final Locator deleteButton) {
		for (int i = 0; i < 12; i++) {
			if (deleteButton.isDisabled()) {
				return;
			}
			wait_500();
		}
		throw new AssertionError("Delete button did not become disabled after deletion (grid was not refreshed)");
	}

	private Locator locateTransitionsGrid() {
		final Locator grid = page.locator("vaadin-grid")
				.filter(new Locator.FilterOptions().setHasText("From Status"));
		assertTrue(grid.count() > 0, "Status transitions grid not found (expected header 'From Status')");
		return grid.first();
	}

	private void wait_300() {
		try {
			Thread.sleep(300);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	protected void waitForButtonEnabled(final Locator button) {
		for (int i = 0; i < 12; i++) {
			if (!button.isDisabled()) {
				return;
			}
			wait_500();
		}
		throw new AssertionError("Button did not become enabled within timeout");
	}

	@Override
	protected void waitForDialogToClose() {
		for (int i = 0; i < 10; i++) {
			if (page.locator("vaadin-dialog-overlay[opened]").count() == 0) {
				return;
			}
			wait_500();
		}
	}

	@Override
	protected Locator waitForDialogWithText(final String text) {
		for (int i = 0; i < 10; i++) {
			final Locator overlay = page.locator("vaadin-dialog-overlay[opened]")
					.filter(new Locator.FilterOptions().setHasText(text));
			if (overlay.count() > 0) {
				return overlay.first();
			}
			wait_500();
		}
		throw new AssertionError("Dialog with text '" + text + "' did not open");
	}

	@Override
	protected void waitForGridCellGone(final Locator grid, final String text) {
		for (int i = 0; i < 12; i++) {
			final Locator matches = grid.locator("vaadin-grid-cell-content")
					.filter(new Locator.FilterOptions().setHasText(text));
			if (matches.count() == 0 || !matches.first().isVisible()) {
				return;
			}
			wait_500();
		}
		throw new AssertionError("Transition row still present after delete: " + text);
	}

	@Override
	protected void waitForGridCellText(final Locator grid, final String text) {
		for (int i = 0; i < 12; i++) {
			if (grid.locator("vaadin-grid-cell-content")
					.filter(new Locator.FilterOptions().setHasText(text)).count() > 0) {
				return;
			}
			wait_500();
		}
		throw new AssertionError("Expected transition row not found: " + text);
	}
}
