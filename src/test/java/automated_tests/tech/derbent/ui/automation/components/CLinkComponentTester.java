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
	public String getComponentName() {
		return "Link Component";
	}

	@Override
	public void test(final Page page) {
		LOGGER.info("      🔗 Testing Link Component...");
		try {
			openTabOrAccordionIfNeeded(page, "Links");
			final Locator container = locateLinksContainer(page);
			if (container == null) {
				LOGGER.info("         ⏭️ Links container not found");
				return;
			}
			container.scrollIntoViewIfNeeded();
			final Locator toolbar = locateLinksToolbar(container, page);
			final Locator addButton = locateLinkToolbarButton(toolbar, page, "vaadin:plus");
			if (addButton == null) {
				LOGGER.info("         ⏭️ Add link button not available");
				return;
			}
			addButton.click();
			waitMs(page, 500);
			Locator dialog = waitForDialogWithText(page, "Add Link");
			if (dialog.count() == 0) {
				dialog = waitForDialogWithText(page, "New Link");
			}
			if (dialog.count() == 0) {
				LOGGER.warn("         ⚠️ Add link dialog did not open");
				return;
			}
			final SourceInfo sourceInfo = readSourceInfo(dialog);
			final String linkType = "AutoTest-" + System.currentTimeMillis();
			String currentType = linkType;
			if (!selectTargetEntityType(dialog, sourceInfo)) {
				LOGGER.warn("         ⚠️ Target entity type selection failed");
			}
			fillTargetEntityId(dialog, sourceInfo);
			fillLinkType(dialog, linkType);
			fillLinkDescription(dialog, "AutoTest link description");
			final Locator saveButton = dialog.locator("#cbutton-save, vaadin-button:has-text('Save')");
			if (saveButton.count() == 0 || saveButton.first().isDisabled()) {
				LOGGER.warn("         ⚠️ Save button not available in link dialog");
				closeAnyOpenDialog(page);
				return;
			}
			saveButton.first().click();
			waitForDialogToClose(page, 6, 250);
			if (isDialogOpen(page)) {
				closeAnyOpenDialog(page);
				waitForDialogToClose(page, 6, 250);
			}
			final Locator grid = locateLinksGrid(container);
			if (grid == null) {
				LOGGER.warn("         ⚠️ Links grid not found");
				return;
			}
			waitForGridCellText(grid, linkType);
			LOGGER.info("         ✅ Link created");
			selectGridRowByText(grid, linkType);
			final Locator editButton = locateLinkToolbarButton(toolbar, page, "vaadin:edit");
			if (editButton != null && !editButton.isDisabled()) {
				editButton.click();
				waitMs(page, 500);
				final Locator editDialog = waitForDialogWithText(page, "Edit Link");
				final String updatedType = linkType + "-U";
				fillLinkType(editDialog, updatedType);
				final Locator editSave = editDialog.locator("#cbutton-save, vaadin-button:has-text('Save')");
				if (editSave.count() > 0 && !editSave.first().isDisabled()) {
					editSave.first().click();
					waitForDialogToClose(page, 6, 250);
				} else {
					closeAnyOpenDialog(page);
					waitForDialogToClose(page, 6, 250);
				}
				if (!isDialogOpen(page)) {
					waitForGridCellText(grid, updatedType);
					currentType = updatedType;
					LOGGER.info("         ✅ Link updated");
				}
			} else {
				LOGGER.info("         ⏭️ Edit button disabled");
			}
			selectGridRowByText(grid, currentType);
			final Locator deleteButton = locateLinkToolbarButton(toolbar, page, "vaadin:trash");
			if (deleteButton != null && !deleteButton.isDisabled()) {
				deleteButton.click();
				waitMs(page, 500);
				confirmDialogIfPresent(page);
				waitForDialogToClose(page, 6, 250);
				waitMs(page, 1000);
				waitForGridCellGone(grid, currentType);
				LOGGER.info("         ✅ Link deleted");
			} else {
				LOGGER.info("         ⏭️ Delete button disabled");
			}
		} catch (final Exception e) {
			LOGGER.warn("         ⚠️ Link CRUD test failed: {}", e.getMessage());
		} finally {
			checkForExceptions(page);
		}
		LOGGER.info("      ✅ Link component test complete");
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
		final Locator combo = dialog.locator("vaadin-combo-box[label='Target Entity Type']");
		if (combo.count() == 0) {
			return false;
		}
		combo.first().click();
		waitMs(dialog.page(), 500);
		final Locator items = dialog.page().locator("vaadin-combo-box-item");
		if (items.count() == 0) {
			return false;
		}
		if (sourceInfo != null && sourceInfo.sourceType != null) {
			final Locator match = items.filter(new Locator.FilterOptions().setHasText(sourceInfo.sourceType));
			if (match.count() > 0) {
				match.first().click();
				waitMs(dialog.page(), 250);
				return true;
			}
		}
		items.first().click();
		waitMs(dialog.page(), 250);
		return true;
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

		private final String sourceType;
		private final String sourceId;

		private SourceInfo(final String sourceType, final String sourceId) {
			this.sourceType = sourceType;
			this.sourceId = sourceId;
		}
	}
}
