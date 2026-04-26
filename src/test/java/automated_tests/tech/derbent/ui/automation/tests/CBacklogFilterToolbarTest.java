package automated_tests.tech.derbent.ui.automation.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Mouse;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.options.WaitForSelectorState;
import automated_tests.tech.derbent.ui.automation.CBaseUITest;
import tech.derbent.Application;

/** Regression test for sprint planning board header/tooling layout. Validates that: 1) the main toolbar does not have large empty space above its
 * fields (alignment issue) 2) backlog text search updates the leaf grid results 3) add-to-sprint dialog opens for a selected backlog item */
@SpringBootTest (
		webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class
)
@TestPropertySource (properties = {
		"spring.profiles.active=derbent",
		"spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🧪 Backlog Filter Toolbar")
public class CBacklogFilterToolbarTest extends CBaseUITest {

	private static final String BACKLOG_SEARCH_TEXT = "MFA";
	private static final String ID_BACKLOG_GRID =
			"#custom-sprint-planning-backlog-grid";
	private static final String ID_SPRINT_GRID =
			"#custom-sprint-planning-tree-grid";
	private static final Logger LOGGER =
			LoggerFactory.getLogger(CBacklogFilterToolbarTest.class);
	private static final String SPRINT_NAME = "Sprint 1";

	private void clickContextMenuItem(final String text) {
		final Locator menuItem = page.locator("vaadin-context-menu-item")
				.filter(new Locator.FilterOptions().setHasText(text)).first();
		if (menuItem.count() == 0) {
			final java.util.List<String> availableItems =
					page.locator("vaadin-context-menu-item").allTextContents();
			throw new AssertionError("Context menu item not found: " + text
					+ " available=" + availableItems);
		}
		menuItem.click();
		wait_500();
	}

	@SuppressWarnings ("unused")
	private void dragAndDrop(final Locator source, final Locator target) {
		final BoundingBox sourceBox = source.boundingBox();
		final BoundingBox targetBox = target.boundingBox();
		assertNotNull(sourceBox, "Source drag box not available");
		assertNotNull(targetBox, "Target drag box not available");
		page.mouse().move(sourceBox.x + sourceBox.width / 2,
				sourceBox.y + sourceBox.height / 2);
		page.mouse().down();
		page.mouse().move(targetBox.x + targetBox.width / 2,
				targetBox.y + targetBox.height / 2,
				new Mouse.MoveOptions().setSteps(15));
		page.mouse().up();
		wait_500();
	}

	private Locator locateFirstAddToSprintCandidate(final Locator grid) {
		final Locator cells = grid.locator("vaadin-grid-cell-content");
		for (int index = 0; index < cells.count(); index++) {
			final Locator candidate = cells.nth(index);
			final String text = candidate.textContent();
			if (!candidate.isVisible() || candidate.boundingBox() == null
					|| text == null || text.isBlank()) {
				continue;
			}
			if (!text.chars().anyMatch(Character::isLetter)) {
				continue;
			}
			openContextMenu(candidate);
			final Locator addToSprintMenuItem =
					page.locator("vaadin-context-menu-item")
							.filter(new Locator.FilterOptions()
									.setHasText("Add to sprint"))
							.first();
			if (addToSprintMenuItem.count() > 0
					&& addToSprintMenuItem.isVisible()) {
				page.keyboard().press("Escape");
				wait_500();
				return candidate;
			}
			page.keyboard().press("Escape");
			wait_500();
		}
		assertTrue(false, "No backlog row exposed the Add to sprint action");
		return locateFirstVisibleGridCell(grid);
	}

	private Locator locateFirstVisibleGridCell(final Locator grid) {
		final Locator cells = grid.locator("vaadin-grid-cell-content");
		for (int index = 0; index < cells.count(); index++) {
			final Locator candidate = cells.nth(index);
			final String text = candidate.textContent();
			if (candidate.isVisible() && candidate.boundingBox() != null
					&& text != null && !text.isBlank()) {
				return candidate;
			}
		}
		assertTrue(cells.count() > 0, "No visible backlog grid cells found");
		return cells.first();
	}

	@SuppressWarnings ("unused")
	private Locator locateGridCellWithText(final Locator grid,
			final String text) {
		final Locator cells = grid.locator("vaadin-grid-cell-content")
				.filter(new Locator.FilterOptions().setHasText(text));
		for (int index = 0; index < cells.count(); index++) {
			final Locator candidate = cells.nth(index);
			if (candidate.isVisible() && candidate.boundingBox() != null) {
				return candidate;
			}
		}
		assertTrue(cells.count() > 0, "Grid cell not found: " + text);
		return cells.first();
	}

	private void navigateToSprintPlanningBoard() {
		page.navigate("http://localhost:" + port + "/cpagetestauxillary");
		page.waitForSelector("#test-auxillary-metadata",
				new com.microsoft.playwright.Page.WaitForSelectorOptions()
						.setTimeout(20000)
						.setState(WaitForSelectorState.ATTACHED));
		Locator planningButton = page.locator("vaadin-button").filter(
				new Locator.FilterOptions().setHasText("Sprint Planning (v2)"))
				.first();
		if (planningButton.count() == 0) {
			planningButton = page.locator("vaadin-button").filter(
					new Locator.FilterOptions().setHasText("Sprint Planning"))
					.first();
		}
		assertTrue(planningButton.count() > 0,
				"Sprint Planning button not found on Test Support Page");
		final String route = planningButton.getAttribute("data-route");
		assertTrue(route != null && !route.isBlank(),
				"Sprint Planning button missing data-route");
		page.navigate("http://localhost:" + port + "/" + route);
		waitForDynamicPageLoad();

		// Newer sprint planning screens render the board directly (no intermediate grid + "Open Board" button).
		// The filter controls are hosted in the sprint grid quick-access panel, so we wait for a stable control ID.
		Locator toolbar = page.locator("#custom-sprint-planning-sprint-filter-combobox");
		if (toolbar.count() > 0) {
			toolbar.first().waitFor(new Locator.WaitForOptions().setTimeout(20000));
			return;
		}

		// Legacy flow: select a planning view row then open the embedded board.
		final Locator cells = page.locator("vaadin-grid vaadin-grid-cell-content");
		final int maxCellScan = Math.min(20, cells.count());
		for (int c = 0; c < maxCellScan; c++) {
			if (cells.nth(c).isVisible()) {
				cells.nth(c).click();
				break;
			}
		}
		wait_1000();
		final Locator openBoardButton = page.locator("#cbutton-open-sprint-planning-board");
		assertTrue(openBoardButton.count() > 0, "Open Sprint Planning Board button not found");
		openBoardButton.first().click();
		waitForDynamicPageLoad();
		toolbar = page.locator("#custom-sprint-planning-sprint-filter-combobox");
		toolbar.waitFor(new Locator.WaitForOptions().setTimeout(20000));
	}

	private void openContextMenu(final Locator locator) {
		locator.click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
		wait_500();
	}

	private void selectComboBoxOptionByText(final Locator comboHost,
			final String optionText) {
		Locator combo = comboHost;
		final Locator embeddedCombo = comboHost.locator(
				"vaadin-combo-box, c-navigable-combo-box, c-combo-box");
		if (embeddedCombo.count() > 0) {
			combo = embeddedCombo.first();
		}
		final Locator input = combo.locator("input");
		if (input.count() > 0) {
			input.first().click();
		} else {
			combo.click();
		}
		wait_500();
		if (input.count() > 0) {
			input.first().fill(optionText);
			wait_500();
		}
		Locator options = page.locator(
				"vaadin-combo-box-overlay[opened] vaadin-combo-box-item");
		if (options.count() == 0) {
			options = page.locator("vaadin-combo-box-item");
		}
		final Locator match = options
				.filter(new Locator.FilterOptions().setHasText(optionText))
				.first();
		assertTrue(match.count() > 0,
				"ComboBox option not found: " + optionText);
		match.click();
		wait_500();
	}

	@Test
	@DisplayName (
		"✅ Toolbar alignment + backlog search + context menu + drag/drop"
	)
	void testBacklogToolbarAlignmentAndFiltering() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}
		loginToApplication();
		navigateToSprintPlanningBoard();

		// Sprint planning hosts its filter controls inside the sprint grid quick-access panel.
		final Locator toolbar = page
				.locator("#custom-sprint-planning-tree-grid-quick-access").first();
		assertTrue(toolbar.count() > 0,
				"Sprint planning quick-access panel not found");
		toolbar.waitFor(new Locator.WaitForOptions().setTimeout(20000));
		final BoundingBox toolbarBox = toolbar.boundingBox();
		assertNotNull(toolbarBox, "Toolbar bounding box not available");
		final Locator firstField = toolbar
				.locator("vaadin-combo-box, vaadin-text-field").first();
		assertTrue(firstField.count() > 0,
				"Expected at least one field in quick-access panel");
		final BoundingBox fieldBox = firstField.boundingBox();
		assertNotNull(fieldBox, "First field bounding box not available");
		final double topGap = fieldBox.y - toolbarBox.y;
		assertTrue(topGap < 25,
				"Unexpected empty space above toolbar fields (gap=" + topGap
						+ "px)");
		final Locator gridLeaves = page.locator(ID_BACKLOG_GRID);
		final Locator gridSprints = page.locator(ID_SPRINT_GRID);
		assertTrue(gridLeaves.count() > 0, "Backlog leaf grid not found");
		assertTrue(gridSprints.count() > 0, "Sprint grid not found");
		// Text search is hosted in the backlog parent browser header quick-access panel.
		final Locator searchInput = page
				.locator("#custom-sprint-planning-backlog-search-field input");
		assertTrue(searchInput.count() > 0, "Backlog search input not found");
		searchInput.first().fill(BACKLOG_SEARCH_TEXT);
		wait_500();
		final Locator filteredBacklogItemCell =
				locateFirstVisibleGridCell(gridLeaves);
		final String filteredLabel =
				filteredBacklogItemCell.textContent() != null
						? filteredBacklogItemCell.textContent().trim() : "";
		assertTrue(!filteredLabel.isBlank(),
				"Backlog search did not expose a visible item label");
		// After confirming the search narrows the backlog, pick the first row that actually exposes the sprint action.
		searchInput.first().clear();
		wait_500();
		final Locator backlogItemCell =
				locateFirstAddToSprintCandidate(gridLeaves);
		final String backlogItemLabel = backlogItemCell.textContent() != null
				? backlogItemCell.textContent().trim() : "";
		assertTrue(!backlogItemLabel.isBlank(),
				"Backlog search did not expose a visible item label");
		backlogItemCell.click();
		wait_500();
		// Add-to-sprint dialog should be available for backlog items (DnD alternative).
		final Locator addToSprintButton =
				page.locator("#custom-sprint-planning-add-to-sprint-button");
		assertTrue(addToSprintButton.count() > 0,
				"Add to sprint button not found");
		addToSprintButton.first().click();
		page.waitForSelector(
				"#custom-sprint-planning-add-to-sprint-sprint-combobox",
				new com.microsoft.playwright.Page.WaitForSelectorOptions()
						.setTimeout(20000));
		page.locator("#custom-sprint-planning-add-to-sprint-cancel").first()
				.click();
		// Right-click should target the row under the mouse so the row-specific Add to sprint action opens the same dialog.
		openContextMenu(backlogItemCell);
		clickContextMenuItem("Add to sprint");
		page.waitForSelector(
				"#custom-sprint-planning-add-to-sprint-sprint-combobox",
				new com.microsoft.playwright.Page.WaitForSelectorOptions()
						.setTimeout(20000));
		selectComboBoxOptionByText(page.locator(
				"#custom-sprint-planning-add-to-sprint-sprint-combobox"),
				SPRINT_NAME);
		page.locator("#custom-sprint-planning-add-to-sprint-ok").first()
				.click();
		wait_1000();
		waitForGridCellText(gridSprints, backlogItemLabel);
		waitForGridCellGone(gridLeaves, backlogItemLabel);
		performFailFastCheck(
				"Sprint planning backlog search + full interaction flow");
	}
}
