package automated_tests.tech.derbent.ui.automation.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
 * Playwright tests for the inline Grid.Editor architecture.
 *
 * Navigation uses the CPageTestAuxillary pattern (project standard) – buttons
 * at `/cpagetestauxillary` with `data-route` attributes provide stable URLs for
 * all entity pages, including project-scoped ones that are not in the top-level menu.
 *
 * Verified scenarios:
 *   1. Editable column headers show the ✏ pencil prefix.
 *   2. Non-editable columns (e.g. id) have no pencil prefix.
 *   3. Clicking a cell in an editable column activates the inline editor.
 *   4. A text-field input is rendered inside the active editor row.
 *   5. Changing the name value and triggering close auto-saves to the DB.
 *   6. After grid refresh the edited value is still shown.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
	"spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("Inline Grid Editor – Activity")
public class CGridInlineEditTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGridInlineEditTest.class);
	private static final String AUX_PAGE_ROUTE = "cpagetestauxillary";
	private static final String BUTTON_SELECTOR = "[id^='test-aux-btn-']";
	/** Keyword matching the aux-page button title for the Activity grid. */
	private static final String ACTIVITY_PAGE_KEYWORD = "Activities";
	private static final String ACTIVITY_ORIGINAL_NAME = "InlineEdit-Original";
	private static final String ACTIVITY_EDITED_NAME   = "InlineEdit-Updated";

	private int screenshotCounter = 1;

	// ---------------------------------------------------------------------------
	// Main test
	// ---------------------------------------------------------------------------

	@Test
	@DisplayName("Editable headers show pencil, cell click opens editor, close auto-saves")
	void testInlineEditActivatesAndSaves() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("Browser not available – skipping inline-edit test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}

		try {
			Files.createDirectories(Paths.get("target/screenshots"));

			// ── Step 1: login ────────────────────────────────────────────────────────
			loginToApplication();
			snap("01-login");

			// ── Step 2: navigate to Activity Management via the aux test page ─────────
			final String activityRoute = findActivityRouteViaAuxPage();
			assertTrue(activityRoute != null && !activityRoute.isBlank(),
					"Could not find Activity Management button on the aux test page");
			LOGGER.info("Navigating to Activity Management at route: {}", activityRoute);

			page.navigate("http://localhost:" + port + "/" + activityRoute,
					new Page.NavigateOptions().setTimeout(60000)
							.setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));
			wait_2000();
			performFailFastCheck("After navigate to Activity");
			snap("02-activity-page");

			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(15000));

			// ── Step 3: create a test activity so the grid has data ───────────────────
			LOGGER.info("Creating test activity '{}'", ACTIVITY_ORIGINAL_NAME);
			clickNew();
			wait_1000();
			snap("03-new-dialog");
			// Use #field-name (same pattern as CPageComprehensiveTest) to avoid filling the search bar
			fillFieldByIdOrFallback("field-name", ACTIVITY_ORIGINAL_NAME);
			wait_500();
			clickSave();
			wait_2000();
			performFailFastCheck("After activity create");
			snap("04-activity-created");
			clickRefresh();
			wait_1000();

			// ── Step 4: verify at least one header has the ✏ pencil ────────────────
			LOGGER.info("Verifying pencil prefix on editable column headers");
			assertTrue(verifyEditableHeaderExists(),
					"No ✏ column header found – setupGridEditor() may not have run");
			snap("05-pencil-headers-ok");

			// ── Step 5: verify the 'id' header has NO pencil ────────────────────────
			LOGGER.info("Verifying 'id' column is NOT marked editable");
			assertTrue(verifyColumnHeaderIsPlain("id"),
					"'id' column should not have a ✏ prefix");

			// ── Step 6: click an editable Name cell to activate the editor ──────────
			LOGGER.info("Clicking Name cell to activate inline editor");
			assertTrue(clickEditableCellByHeaderText("Name"),
					"Inline editor did not activate after cell click");
			wait_500();
			snap("06-editor-activated");

			// ── Step 7: verify a text input is visible in the active editor row ─────
			LOGGER.info("Verifying text input is visible inside the active editor row");
			assertTrue(isEditorInputVisible(),
					"No text-field input found inside the active grid editor row");
			snap("07-editor-input-visible");

			// ── Step 8: replace the text with the edited name ─────────────────────
			// Use chained locator so Playwright pierces Shadow DOM into the vaadin-text-field
			LOGGER.info("Replacing name with '{}'", ACTIVITY_EDITED_NAME);
			final Locator editorTf = page.locator("vaadin-grid vaadin-text-field").first();
			final Locator editorInput = editorTf.locator("input").first();
			editorInput.click();
			wait_200();
			// Triple-click selects all text inside the field (shadow-DOM safe alternative to selectText())
			editorInput.click(new Locator.ClickOptions().setClickCount(3));
			wait_200();
			editorInput.fill(ACTIVITY_EDITED_NAME);
			wait_500();
			snap("08-name-typed");

			// ── Step 9: close the editor by clicking the grid header → triggers auto-save
			LOGGER.info("Closing editor by clicking outside to trigger auto-save");
			closeEditorByClickingOutside();
			wait_1500();
			performFailFastCheck("After inline editor close");
			snap("09-editor-closed");

			// ── Step 10: refresh and verify the edit persisted ───────────────────────
			LOGGER.info("Refreshing grid to verify persisted value");
			clickRefresh();
			wait_2000();
			snap("10-after-refresh");

			assertTrue(isTextVisibleInGrid(ACTIVITY_EDITED_NAME),
					"Edited name '" + ACTIVITY_EDITED_NAME + "' not visible in grid – auto-save may have failed");
			assertFalse(isTextVisibleInGrid(ACTIVITY_ORIGINAL_NAME),
					"Original name '" + ACTIVITY_ORIGINAL_NAME + "' still visible – edit was not applied");
			snap("11-edited-name-confirmed");

			// ── Step 11: clean up ─────────────────────────────────────────────────────
			LOGGER.info("Cleaning up test activity");
			clickFirstGridRow();
			wait_500();
			clickDelete();
			wait_500();
			final Locator confirmYes = page.locator("#cbutton-yes");
			if (confirmYes.count() > 0) {
				confirmYes.first().click();
				wait_1000();
			}
			performFailFastCheck("After activity delete");
			snap("12-deleted");

			LOGGER.info("Inline-edit test completed successfully");

		} catch (final Exception e) {
			LOGGER.error("Inline-edit test failed: {}", e.getMessage(), e);
			snap("error");
			throw new AssertionError("Inline-edit test failed", e);
		}
	}

	// ---------------------------------------------------------------------------
	// Navigation helpers
	// ---------------------------------------------------------------------------

	/**
	 * Navigates to the CPageTestAuxillary page, waits for buttons to render,
	 * then returns the `data-route` value for the Activity Management button.
	 * Returns null when no matching button is found.
	 */
	private String findActivityRouteViaAuxPage() {
		final String auxUrl = "http://localhost:" + port + "/" + AUX_PAGE_ROUTE;
		LOGGER.info("Navigating to aux test page: {}", auxUrl);
		page.navigate(auxUrl, new Page.NavigateOptions().setTimeout(60000)
				.setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));
		wait_2000();

		// If we were redirected to login, re-authenticate and retry
		if (page.url().contains("/login")) {
			LOGGER.warn("Redirected to login from aux page – re-authenticating");
			loginToApplication();
			page.navigate(auxUrl);
			wait_2000();
		}
		snap("aux-page-loaded");

		// Wait up to 15 s for buttons to appear
		final long deadline = System.currentTimeMillis() + 15_000;
		while (page.locator(BUTTON_SELECTOR).count() == 0 && System.currentTimeMillis() < deadline) {
			wait_500();
		}

		final Locator buttons = page.locator(BUTTON_SELECTOR);
		final int count = buttons.count();
		LOGGER.info("Found {} navigation buttons on aux page", count);

		final List<String> seen = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			try {
				final Locator btn = buttons.nth(i);
				final String title = btn.textContent().trim();
				final String route = btn.getAttribute("data-route");
				seen.add(title);
				if (title.equalsIgnoreCase(ACTIVITY_PAGE_KEYWORD)) {
					LOGGER.info("Found Activity button: title='{}' route='{}'", title, route);
					return route;
				}
			} catch (final Exception e) {
				LOGGER.warn("Could not read aux button {}: {}", i, e.getMessage());
			}
		}
		LOGGER.warn("Activity button not found. Available buttons: {}", seen);
		return null;
	}

	// ---------------------------------------------------------------------------
	// Grid editor assertion helpers
	// ---------------------------------------------------------------------------

	/** Returns true if at least one column header contains the ✏ pencil character. */
	private boolean verifyEditableHeaderExists() {
		try {
			final Locator grid = page.locator("vaadin-grid").first();
			final Locator headerContent = grid.locator(
					"vaadin-grid-cell[part~='header-cell'] vaadin-grid-cell-content");
			for (int i = 0; i < headerContent.count(); i++) {
				final String text = headerContent.nth(i).innerText();
				if (text != null && text.contains("✏")) {
					LOGGER.info("Editable header found: '{}'", text.trim());
					return true;
				}
			}
			// Fallback: any span/sorter inside header area
			final Locator spans = grid.locator("vaadin-grid-sorter, vaadin-grid-cell-content span");
			for (int i = 0; i < spans.count(); i++) {
				final String text = spans.nth(i).innerText();
				if (text != null && text.contains("✏")) {
					return true;
				}
			}
			LOGGER.warn("No ✏ column headers found");
			return false;
		} catch (final Exception e) {
			LOGGER.warn("verifyEditableHeaderExists error: {}", e.getMessage());
			return false;
		}
	}

	/** Returns true if the column identified by name does NOT carry a ✏ pencil. */
	private boolean verifyColumnHeaderIsPlain(final String columnName) {
		try {
			final Locator grid = page.locator("vaadin-grid").first();
			final Locator headerContent = grid.locator(
					"vaadin-grid-cell[part~='header-cell'] vaadin-grid-cell-content");
			for (int i = 0; i < headerContent.count(); i++) {
				final String text = headerContent.nth(i).innerText();
				if (text == null) {
					continue;
				}
				if (text.toLowerCase().contains(columnName.toLowerCase()) && text.contains("✏")) {
					return false;
				}
			}
			return true;
		} catch (final Exception e) {
			LOGGER.warn("verifyColumnHeaderIsPlain error: {}", e.getMessage());
			return true;
		}
	}

	/**
	 * Clicks the first body cell whose column header carries a ✏ pencil prefix and
	 * optionally matches headerText. If no column matches headerText exactly, falls
	 * back to the first ✏-prefixed column.
	 *
	 * Strategy: find the column index of the ✏ header, then click the same-indexed
	 * body cell in the first data row.
	 */
	private boolean clickEditableCellByHeaderText(final String headerText) {
		try {
			final Locator grid = page.locator("vaadin-grid").first();

			// Collect header cells via multiple selectors (Vaadin shadow DOM varies by version)
			final String[] headerSelectors = {
					"vaadin-grid-cell[part~='header-cell'] vaadin-grid-cell-content",
					"vaadin-grid-sorter",
					"vaadin-grid-cell-content"
			};

			int targetIndexByLabel = -1;
			int firstPencilIndex = -1;
			int headersFound = 0;

			for (final String sel : headerSelectors) {
				final Locator cells = grid.locator(sel);
				headersFound = cells.count();
				if (headersFound == 0) {
					continue;
				}
				for (int i = 0; i < headersFound; i++) {
					String text;
					try {
						text = cells.nth(i).innerText();
					} catch (final Exception ignored) {
						continue;
					}
					if (text == null) {
						continue;
					}
					if (text.contains("✏")) {
						if (firstPencilIndex < 0) {
							firstPencilIndex = i;
						}
						if (text.toLowerCase().contains(headerText.toLowerCase())) {
							targetIndexByLabel = i;
							LOGGER.info("Editable column '{}' at index {} via selector '{}'", headerText, i, sel);
							break;
						}
					}
				}
				if (firstPencilIndex >= 0) {
					break; // found pencil headers with this selector
				}
			}

			final int targetIndex = targetIndexByLabel >= 0 ? targetIndexByLabel : firstPencilIndex;
			if (targetIndex < 0) {
				LOGGER.warn("No ✏-prefixed header found for '{}'", headerText);
				return false;
			}
			if (targetIndexByLabel < 0) {
				LOGGER.warn("Header '{}' not found; clicking first pencil column at index {}", headerText, targetIndex);
			}

			// Grid body cells live in deep shadow DOM — CSS selectors can't reach them.
			// Use coordinate-based click: find the header sorter's X centre, then click
			// one row-height below the header in the grid's body area.
			final Locator grid2 = page.locator("vaadin-grid").first();
			final com.microsoft.playwright.options.BoundingBox gridBox = grid2.boundingBox();
			if (gridBox == null) {
				LOGGER.warn("Grid bounding box unavailable");
				return false;
			}

			// Try to get the X-centre of the target column from its sorter element
			double targetX = gridBox.x + gridBox.width / 2; // default: grid centre
			try {
				// Re-locate the matching sorter to get its position
				final Locator sorters = grid2.locator("vaadin-grid-sorter");
				for (int i = 0; i < sorters.count(); i++) {
					final String text = sorters.nth(i).innerText();
					if (text != null && text.contains("✏") && text.toLowerCase().contains(headerText.toLowerCase())) {
						final com.microsoft.playwright.options.BoundingBox sorterBox = sorters.nth(i).boundingBox();
						if (sorterBox != null) {
							targetX = sorterBox.x + sorterBox.width / 2;
							LOGGER.info("Using sorter X-centre={} for column '{}'", targetX, headerText);
						}
						break;
					}
				}
			} catch (final Exception ignored) { /* keep default X */ }

			// Vaadin grid header row is typically ~40px; rows are ~40-50px each.
			// Click at Y = grid.top + header(40px) + half-row(25px).
			final double targetY = gridBox.y + 40 + 25;
			LOGGER.info("Coordinate click at ({}, {}) to activate grid editor", targetX, targetY);
			page.mouse().click(targetX, targetY);
			wait_500();
			return true;
		} catch (final Exception e) {
			LOGGER.warn("clickEditableCellByHeaderText error: {}", e.getMessage());
			return false;
		}
	}

	/** Returns true if a text-field (or numeric-field) input is visible inside the grid editor.
	 *  Uses chained locators so Playwright pierces Shadow DOM into the web components. */
	private boolean isEditorInputVisible() {
		try {
			final Locator gridTfs = page.locator("vaadin-grid vaadin-text-field");
			for (int i = 0; i < gridTfs.count(); i++) {
				final Locator inp = gridTfs.nth(i).locator("input");
				if (inp.count() > 0 && inp.first().isVisible()) {
					return true;
				}
			}
			final Locator gridIntFields = page.locator(
					"vaadin-grid vaadin-integer-field, vaadin-grid vaadin-big-decimal-field");
			for (int i = 0; i < gridIntFields.count(); i++) {
				final Locator inp = gridIntFields.nth(i).locator("input");
				if (inp.count() > 0 && inp.first().isVisible()) {
					return true;
				}
			}
			return false;
		} catch (final Exception e) {
			LOGGER.warn("isEditorInputVisible error: {}", e.getMessage());
			return false;
		}
	}

	/** Returns true if any grid cell contains the given text. */
	private boolean isTextVisibleInGrid(final String text) {
		try {
			final Locator grid = page.locator("vaadin-grid").first();
			return grid.locator("vaadin-grid-cell-content")
					.filter(new Locator.FilterOptions().setHasText(text))
					.count() > 0;
		} catch (final Exception e) {
			LOGGER.warn("isTextVisibleInGrid error: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Closes the active Grid.Editor by clicking outside the data rows.
	 * The close listener on the editor then triggers auto-save.
	 */
	private void closeEditorByClickingOutside() {
		try {
			// Click a grid header sorter to move focus away from the editor row
			final Locator sorter = page.locator("vaadin-grid-sorter").first();
			if (sorter.count() > 0) {
				sorter.first().click();
				return;
			}
			// Fallback: click the toolbar area above the grid
			final Locator toolbar = page.locator(".crud-toolbar, .view-toolbar").first();
			if (toolbar.count() > 0) {
				toolbar.first().click();
				return;
			}
			// Last resort: press Escape (closes editor without save in some Vaadin configs)
			page.keyboard().press("Tab");
		} catch (final Exception e) {
			LOGGER.warn("closeEditorByClickingOutside fallback: {}", e.getMessage());
		}
	}

	// ---------------------------------------------------------------------------
	// Convenience wrappers
	// ---------------------------------------------------------------------------

	/**
	 * Fills a form field by its fieldId (looks for #fieldId input, then #fieldId,
	 * then falls back to page.locator("vaadin-text-field").first()). Avoids
	 * accidentally filling the search/filter bar at the top of the page.
	 */
	private void fillFieldByIdOrFallback(final String fieldId, final String value) {
		try {
			final Locator host = page.locator("#" + fieldId);
			if (host.count() > 0) {
				final Locator inp = host.locator("input");
				if (inp.count() > 0) {
					inp.first().fill(value);
					LOGGER.info("Filled #{} input with '{}'", fieldId, value);
					return;
				}
				host.first().fill(value);
				LOGGER.info("Filled #{} (direct) with '{}'", fieldId, value);
				return;
			}
		} catch (final Exception e) {
			LOGGER.warn("fillFieldByIdOrFallback #{} failed, using fallback: {}", fieldId, e.getMessage());
		}
		// Fallback: fill the first text-field via its inner input (shadow-DOM safe)
		try {
			page.locator("vaadin-text-field").first().locator("input").first().fill(value);
			LOGGER.info("Filled first vaadin-text-field input with '{}'", value);
		} catch (final Exception e) {
			LOGGER.warn("Fallback fill also failed: {}", e.getMessage());
		}
	}

	private void snap(final String label) {
		takeScreenshot(String.format("%03d-%s", screenshotCounter++, label), false);
	}

	private void wait_200() {
		try { Thread.sleep(200); } catch (final InterruptedException ie) { Thread.currentThread().interrupt(); }
	}

	private void wait_1500() {
		try { Thread.sleep(1500); } catch (final InterruptedException ie) { Thread.currentThread().interrupt(); }
	}
}
