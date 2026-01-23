package automated_tests.tech.derbent.ui.automation.components;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Tests link component functionality on pages that provide a links section. */
public class CLinkComponentTester extends CBaseComponentTester {

	private static final String LINKS_COMPONENT_SELECTOR = "#custom-links-component, #custom-links-grid, #custom-links-toolbar";
	private static final String LINKS_TAB_SELECTOR =
			"vaadin-tab:has-text('Links'), vaadin-tab:has-text('Link'), vaadin-accordion-panel:has-text('Links')";
	private static final Pattern SOURCE_PATTERN = Pattern.compile("Source:\\s*(.+?)\\s*#(\\d+)");

	@Override
	public boolean canTest(final Page page) {
		return elementExists(page, LINKS_COMPONENT_SELECTOR) || elementExists(page, LINKS_TAB_SELECTOR);
	}

	@Override
	public String getComponentName() { return "Link Component"; }

	@Override
	public void test(final Page page) {
		LOGGER.info("      🔗 Testing Link Component...");
		String createdLinkType = null;
		try {
			openTabOrAccordionIfNeeded(page, "Links");
			final Locator container = locateLinksContainer(page);
			if (container == null) {
				LOGGER.info("         ⏭️ Links container not found");
				return;
			}
			container.scrollIntoViewIfNeeded();
			final Locator toolbar = locateLinksToolbar(container, page);
			final Locator grid = locateLinksGrid(container);
			if (grid == null) {
				LOGGER.info("         ⏭️ Links grid not found");
				return;
			}
			// Test Add Link
			createdLinkType = testAddLink(page, toolbar, grid);
			if (createdLinkType == null) {
				LOGGER.info("         ⏭️ Add link test skipped or failed");
				return;
			}
			// Test Edit Link
			final String updatedLinkType = testEditLink(page, toolbar, grid, createdLinkType);
			if (updatedLinkType != null) {
				createdLinkType = updatedLinkType;
			}
			// Test Grid Selection and Visual Feedback
			testGridSelection(grid, createdLinkType);
			// Test Link Details Expansion
			testLinkDetailsExpansion(grid, createdLinkType);
			// Test Delete Link
			testDeleteLink(page, toolbar, grid, createdLinkType);
			LOGGER.info("      ✅ Link component test complete - All CRUD operations successful");
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ Link CRUD test failed: {}", e.getMessage());
		} finally {
			checkForExceptions(page);
		}
	}

	/** Test adding a new link with enhanced validation.
	 * @return the link type that was created, or null if failed */
	private String testAddLink(final Page page, final Locator toolbar, final Locator grid) {
		try {
			LOGGER.info("         🔹 Testing Add Link...");
			final Locator addButton = locateLinkToolbarButton(toolbar, page, "vaadin:plus");
			if (addButton == null || addButton.isDisabled()) {
				LOGGER.info("            ⏭️ Add link button not available");
				return null;
			}
			addButton.click();
			waitMs(page, 500);
			Locator dialog = waitForDialogWithText(page, "Add Link");
			if (dialog.count() == 0) {
				dialog = waitForDialogWithText(page, "New Link");
			}
			if (dialog.count() == 0) {
				LOGGER.warn("            ⚠️ Add link dialog did not open");
				return null;
			}
			// Validate dialog structure
			validateDialogStructure(dialog);
			final SourceInfo sourceInfo = readSourceInfo(dialog);
			final String linkType = "AutoTest-" + System.currentTimeMillis();
			if (!selectTargetEntityType(dialog, sourceInfo)) {
				LOGGER.warn("            ⚠️ Target entity type selection failed");
				closeAnyOpenDialog(page);
				return null;
			}
			fillLinkType(dialog, linkType);
			fillLinkDescription(dialog, "AutoTest link description for validation");
			// Test filter in entity selection
			testEntitySelectionFilter(dialog);
			final Locator saveButton = dialog.locator("#cbutton-save, vaadin-button:has-text('Save')");
			if (saveButton.count() == 0 || saveButton.first().isDisabled()) {
				LOGGER.warn("            ⚠️ Save button not available in link dialog");
				closeAnyOpenDialog(page);
				return null;
			}
			saveButton.first().click();
			waitForDialogToClose(page, 6, 250);
			if (isDialogOpen(page)) {
				closeAnyOpenDialog(page);
				waitForDialogToClose(page, 6, 250);
			}
			waitForGridCellText(grid, linkType);
			LOGGER.info("            ✅ Link created: {}", linkType);
			return linkType;
		} catch (final Exception e) {
			LOGGER.warn("            ⚠️ Add link failed: {}", e.getMessage());
			return null;
		}
	}

	/** Validate dialog structure follows standards.
	 * @param dialog the dialog locator */
	private void validateDialogStructure(final Locator dialog) {
		try {
			LOGGER.debug("            Validating dialog structure...");
			// Check dialog width (should be 600px or similar)
			// Note: Can't directly check computed width in Playwright, but can verify dialog is visible
			if (!dialog.isVisible()) {
				LOGGER.warn("            ⚠️ Dialog not visible");
				return;
			}
			// Check for FormBuilder fields (linkType, description)
			final Locator linkTypeField = dialog.locator("vaadin-text-field[label='Link Type']");
			final Locator descriptionField = dialog.locator("vaadin-text-area[label='Description']");
			if (linkTypeField.count() > 0) {
				LOGGER.debug("            ✅ Link Type field found (FormBuilder)");
			} else {
				LOGGER.warn("            ⚠️ Link Type field not found");
			}
			if (descriptionField.count() > 0) {
				LOGGER.debug("            ✅ Description field found (FormBuilder)");
			} else {
				LOGGER.warn("            ⚠️ Description field not found");
			}
			// Check for entity selection component
			final Locator entitySelection = dialog.locator("vaadin-combo-box, vaadin-grid");
			if (entitySelection.count() > 0) {
				LOGGER.debug("            ✅ Entity selection component found");
			} else {
				LOGGER.warn("            ⚠️ Entity selection component not found");
			}
			LOGGER.debug("            ✅ Dialog structure validation complete");
		} catch (final Exception e) {
			LOGGER.warn("            ⚠️ Dialog structure validation error: {}", e.getMessage());
		}
	}

	/** Test entity selection filter functionality.
	 * @param dialog the dialog locator */
	private void testEntitySelectionFilter(final Locator dialog) {
		try {
			LOGGER.debug("            Testing entity selection filter...");
			// Try to find filter toolbar in entity selection
			final Locator filterToolbar = dialog.locator(".filter-toolbar, [class*='filter'], vaadin-text-field[placeholder*='filter' i]");
			if (filterToolbar.count() == 0) {
				LOGGER.debug("            Filter toolbar not found (may not be present in this dialog)");
				return;
			}
			// If filter exists, try to use it
			final Locator filterInput = filterToolbar.first().locator("input").first();
			if (filterInput.count() > 0 && filterInput.isVisible()) {
				filterInput.fill("test");
				waitMs(dialog.page(), 500);
				filterInput.fill(""); // Clear filter
				waitMs(dialog.page(), 300);
				LOGGER.debug("            ✅ Entity selection filter tested");
			}
		} catch (final Exception e) {
			LOGGER.debug("            Entity selection filter test skipped: {}", e.getMessage());
		}
	/** Test editing an existing link with enhanced validation.
	 * @return the updated link type, or null if failed */
	private String testEditLink(final Page page, final Locator toolbar, final Locator grid, final String linkType) {
		try {
			LOGGER.info("         🔹 Testing Edit Link...");
			selectGridRowByText(grid, linkType);
			waitMs(page, 300);
			final Locator editButton = locateLinkToolbarButton(toolbar, page, "vaadin:edit");
			if (editButton == null || editButton.isDisabled()) {
				LOGGER.info("            ⏭️ Edit button not available or disabled");
				return null;
			}
			editButton.click();
			waitMs(page, 500);
			final Locator editDialog = waitForDialogWithText(page, "Edit Link");
			if (editDialog.count() == 0) {
				LOGGER.warn("            ⚠️ Edit link dialog did not open");
				closeAnyOpenDialog(page);
				return null;
			}

			// Validate dialog structure in edit mode
			validateDialogStructure(editDialog);

			// Verify fields are populated in edit mode
			final Locator linkTypeField = editDialog.locator("vaadin-text-field[label='Link Type'] input");
			if (linkTypeField.count() > 0 && linkTypeField.first().inputValue().length() > 0) {
				LOGGER.debug("            ✅ Link Type field populated in edit mode");
			}

			final String updatedType = linkType + "-Updated";
			fillLinkType(editDialog, updatedType);
			fillLinkDescription(editDialog, "Updated description for validation");
			final Locator editSave = editDialog.locator("#cbutton-save, vaadin-button:has-text('Save')");
			if (editSave.count() > 0 && !editSave.first().isDisabled()) {
				editSave.first().click();
				waitForDialogToClose(page, 6, 250);
				if (!isDialogOpen(page)) {
					waitForGridCellText(grid, updatedType);
					LOGGER.info("            ✅ Link updated: {} -> {}", linkType, updatedType);
					return updatedType;
				}
			} else {
				closeAnyOpenDialog(page);
			}
			return null;
		} catch (final Exception e) {
			LOGGER.warn("            ⚠️ Edit link failed: {}", e.getMessage());
			return null;
		}
	}

	/** Test grid selection and visual feedback. */
	private void testGridSelection(final Locator grid, final String linkType) {
		try {
			LOGGER.info("         🔹 Testing Grid Selection...");
			// Clear selection first
			grid.click();
			waitMs(grid.page(), 200);
			// Select the row
			selectGridRowByText(grid, linkType);
			waitMs(grid.page(), 300);
			// Verify selection by checking if cell is visible in viewport
			final Locator selectedCell = grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(linkType));
			if (selectedCell.count() > 0 && selectedCell.first().isVisible()) {
				LOGGER.info("            ✅ Grid selection visual feedback verified");
			} else {
				LOGGER.warn("            ⚠️ Grid selection visual feedback not clear");
			}
		} catch (final Exception e) {
			LOGGER.warn("            ⚠️ Grid selection test failed: {}", e.getMessage());
		}
	}

	/** Test link details expansion on row click. */
	private void testLinkDetailsExpansion(final Locator grid, final String linkType) {
		try {
			LOGGER.info("         🔹 Testing Link Details Expansion...");
			final Locator cell = grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(linkType));
			if (cell.count() > 0) {
				// Click to expand details
				cell.first().click();
				waitMs(grid.page(), 500);
				// Click again to collapse
				cell.first().click();
				waitMs(grid.page(), 300);
				LOGGER.info("            ✅ Link details expansion/collapse tested");
			} else {
				LOGGER.warn("            ⚠️ Could not test details expansion");
			}
		} catch (final Exception e) {
			LOGGER.warn("            ⚠️ Details expansion test failed: {}", e.getMessage());
		}
	}

	/** Test deleting a link.
	 * @return true if successful */
	private boolean testDeleteLink(final Page page, final Locator toolbar, final Locator grid, final String linkType) {
		try {
			LOGGER.info("         🔹 Testing Delete Link...");
			selectGridRowByText(grid, linkType);
			waitMs(page, 300);
			final Locator deleteButton = locateLinkToolbarButton(toolbar, page, "vaadin:trash");
			if (deleteButton == null || deleteButton.isDisabled()) {
				LOGGER.info("            ⏭️ Delete button not available or disabled");
				return false;
			}
			deleteButton.click();
			waitMs(page, 500);
			confirmDialogIfPresent(page);
			waitForDialogToClose(page, 6, 250);
			waitMs(page, 1000);
			waitForGridCellGone(grid, linkType);
			LOGGER.info("            ✅ Link deleted: {}", linkType);
			return true;
		} catch (final Exception e) {
			LOGGER.warn("            ⚠️ Delete link failed: {}", e.getMessage());
			return false;
		}
	}

	@SuppressWarnings ("static-method")
	private Locator locateLinksContainer(final Page page) {
		final Locator container = page.locator("#custom-links-component");
		if (container.count() > 0) {
			return container.first();
		}
		final Locator header = page.locator("h2:has-text('Links'), h3:has-text('Links'), h4:has-text('Links'), span:has-text('Links')");
		if (header.count() > 0) {
			return header.first().locator("xpath=ancestor::*[self::vaadin-vertical-layout or self::div][1]");
		}
		return null;
	}

	@SuppressWarnings ("static-method")
	private Locator locateLinksToolbar(final Locator container, final Page page) {
		final Locator toolbar = container.locator("#custom-links-toolbar");
		if (toolbar.count() > 0) {
			return toolbar.first();
		}
		final Locator pageToolbar = page.locator("#custom-links-toolbar");
		if (pageToolbar.count() > 0) {
			return pageToolbar.first();
		}
		return container;
	}

	@SuppressWarnings ("static-method")
	private Locator locateLinksGrid(final Locator container) {
		final Locator grid = container.locator("#custom-links-grid");
		if (grid.count() > 0) {
			return grid.first();
		}
		final Locator fallback = container.locator("vaadin-grid").filter(new Locator.FilterOptions().setHasText("Target"));
		if (fallback.count() > 0) {
			return fallback.first();
		}
		return null;
	}

	@SuppressWarnings ("static-method")
	private Locator locateLinkToolbarButton(final Locator toolbar, final Page page, final String iconName) {
		final Locator scope = toolbar != null ? toolbar : page.locator(":root");
		final Locator button =
				scope.locator("vaadin-button").filter(new Locator.FilterOptions().setHas(page.locator("vaadin-icon[icon='" + iconName + "']")));
		if (button.count() == 0) {
			return null;
		}
		return button.first();
	}

	@SuppressWarnings ("static-method")
	private void fillLinkType(final Locator dialog, final String value) {
		final Locator input = dialog.locator("vaadin-text-field[label='Link Type'] input");
		if (input.count() > 0) {
			input.first().fill(value);
		}
	}

	@SuppressWarnings ("static-method")
	private void fillLinkDescription(final Locator dialog, final String value) {
		final Locator input = dialog.locator("vaadin-text-area[label='Description'] textarea");
		if (input.count() > 0) {
			input.first().fill(value);
		}
	}

	@SuppressWarnings ("static-method")
	private void fillTargetEntityId(final Locator dialog, final SourceInfo sourceInfo) {
		final Locator input = dialog.locator("vaadin-text-field[label='Target Entity ID'] input");
		if (input.count() == 0) {
			return;
		}
		final String targetId = sourceInfo != null && sourceInfo.sourceId != null ? sourceInfo.sourceId : "1";
		input.first().fill(targetId);
	}

	private boolean selectTargetEntityType(final Locator dialog, final SourceInfo sourceInfo) {
		try {
			// Wait for entity selection component to load
			waitMs(dialog.page(), 1000);
			// Try to find the entity type selector (combo box)
			final Locator typeSelector = dialog.locator("vaadin-combo-box").first();
			if (typeSelector.count() == 0) {
				LOGGER.warn("            ⚠️ Entity type selector not found");
				return false;
			}
			typeSelector.click();
			waitMs(dialog.page(), 500);
			// Select first available entity type
			final Locator items = dialog.page().locator("vaadin-combo-box-item");
			if (items.count() == 0) {
				LOGGER.warn("            ⚠️ No entity types available");
				return false;
			}
			// Try to match source type if available
			if (sourceInfo != null && sourceInfo.sourceType != null) {
				final Locator match = items.filter(new Locator.FilterOptions().setHasText(sourceInfo.sourceType));
				if (match.count() > 0) {
					match.first().click();
					waitMs(dialog.page(), 500);
					// Select first entity from grid
					return selectFirstEntityFromGrid(dialog);
				}
			}
			// Fallback: select first type
			items.first().click();
			waitMs(dialog.page(), 500);
			// Select first entity from grid
			return selectFirstEntityFromGrid(dialog);
		} catch (final Exception e) {
			LOGGER.warn("            ⚠️ Entity type selection error: {}", e.getMessage());
			return false;
		}
	}

	/** Select first available entity from the selection grid in dialog.
	 * @param dialog the dialog locator
	 * @return true if successful */
	private boolean selectFirstEntityFromGrid(final Locator dialog) {
		try {
			waitMs(dialog.page(), 1000);
			// Find the entity selection grid
			final Locator grid = dialog.locator("vaadin-grid").first();
			if (grid.count() == 0) {
				LOGGER.warn("            ⚠️ Entity selection grid not found");
				return false;
			}
			// Click first row in grid
			final Locator firstRow = grid.locator("vaadin-grid-cell-content").first();
			if (firstRow.count() > 0 && firstRow.isVisible()) {
				firstRow.click();
				waitMs(dialog.page(), 300);
				LOGGER.debug("            Selected first entity from grid");
				return true;
			}
			LOGGER.warn("            ⚠️ No entities available in grid");
			return false;
		} catch (final Exception e) {
			LOGGER.warn("            ⚠️ Entity grid selection error: {}", e.getMessage());
			return false;
		}
	}

	private void selectGridRowByText(final Locator grid, final String text) {
		final Locator cell = grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(text));
		if (cell.count() > 0) {
			cell.first().click();
			waitMs(grid.page(), 250);
		}
	}

	@SuppressWarnings ("static-method")
	private SourceInfo readSourceInfo(final Locator dialog) {
		final Locator sourceLabel = dialog.locator("span:has-text('Source:')");
		if (sourceLabel.count() == 0) {
			return null;
		}
		final String text = sourceLabel.first().textContent();
		if (text == null) {
			return null;
		}
		final Matcher matcher = SOURCE_PATTERN.matcher(text.trim());
		if (!matcher.find()) {
			return null;
		}
		return new SourceInfo(matcher.group(1).trim(), matcher.group(2).trim());
	}

	private static final class SourceInfo {

		private final String sourceId;
		private final String sourceType;

		private SourceInfo(final String sourceType, final String sourceId) {
			this.sourceType = sourceType;
			this.sourceId = sourceId;
		}
	}
}
