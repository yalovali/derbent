package automated_tests.tech.derbent.ui.automation.tests;

import automated_tests.tech.derbent.ui.automation.CBaseUITest;

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
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.WaitForSelectorState;

import tech.derbent.Application;

/** Regression test for grid-search toolbar layout + combobox-driven grid updates.
 * 
 * Validates that:
 * 1) the search toolbar does not have large empty space above its fields (alignment issue)
 * 2) selecting an entity type in the toolbar combobox updates the grid content
 * 3) text filter updates grid results (filter wiring)
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
@DisplayName ("🧪 Backlog Filter Toolbar")
public class CBacklogFilterToolbarTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBacklogFilterToolbarTest.class);

	private void selectComboBoxOptionByText(final Locator comboHost, final String optionText) {
		Locator combo = comboHost;
		final Locator embeddedCombo = comboHost.locator("vaadin-combo-box, c-navigable-combo-box, c-combo-box");
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
		Locator options = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item");
		if (options.count() == 0) {
			options = page.locator("vaadin-combo-box-item");
		}
		final Locator match = options.filter(new Locator.FilterOptions().setHasText(optionText));
		assertTrue(match.count() > 0, "ComboBox option not found: " + optionText);
		match.first().click();
		wait_500();
	}

	private void navigateToSprintPlanningBoard() {
		page.navigate("http://localhost:" + port + "/cpagetestauxillary");
		page.waitForSelector("#test-auxillary-metadata",
				new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(20000).setState(WaitForSelectorState.ATTACHED));

		Locator planningButton = page.locator("vaadin-button").filter(new Locator.FilterOptions().setHasText("Sprint Planning (v2)")).first();
		if (planningButton.count() == 0) {
			planningButton = page.locator("vaadin-button").filter(new Locator.FilterOptions().setHasText("Sprint Planning")).first();
		}
		assertTrue(planningButton.count() > 0, "Sprint Planning button not found on Test Support Page");

		final String route = planningButton.getAttribute("data-route");
		assertTrue(route != null && !route.isBlank(), "Sprint Planning button missing data-route");

		page.navigate("http://localhost:" + port + "/" + route);
		waitForDynamicPageLoad();

		// Select first planning view entity row so the Open Planning Board button has context.
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

		final Locator toolbar = page.locator("#custom-sprint-planning-filter-toolbar");
		toolbar.waitFor(new Locator.WaitForOptions().setTimeout(20000));
	}

	@Test
	@DisplayName ("✅ Toolbar alignment + combobox filter updates grid")
	void testBacklogToolbarAlignmentAndFiltering() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}
		loginToApplication();
		navigateToSprintPlanningBoard();

		final Locator toolbar = page.locator("#custom-sprint-planning-filter-toolbar").first();
		assertTrue(toolbar.count() > 0, "Sprint planning filter toolbar not found");
		toolbar.waitFor(new Locator.WaitForOptions().setTimeout(20000));

		final BoundingBox toolbarBox = toolbar.boundingBox();
		assertTrue(toolbarBox != null, "Toolbar bounding box not available");

		final Locator firstField = toolbar.locator("vaadin-text-field").first();
		assertTrue(firstField.count() > 0, "Expected at least one text field in toolbar");
		final BoundingBox fieldBox = firstField.boundingBox();
		assertTrue(fieldBox != null, "First field bounding box not available");

		final double topGap = fieldBox.y - toolbarBox.y;
		assertTrue(topGap < 25, "Unexpected empty space above toolbar fields (gap=" + topGap + "px)");

		// Entity type combobox should update backlog tree content
		Locator comboType = toolbar.locator("vaadin-combo-box[label='Type']");
		if (comboType.count() == 0) {
			comboType = toolbar.locator("vaadin-combo-box").nth(2);
		}
		assertTrue(comboType.count() > 0, "Type ComboBox not found in toolbar");
		try {
			selectComboBoxOptionByText(comboType, "Meetings");
		} catch (final AssertionError e) {
			selectComboBoxOptionByText(comboType, "Meeting");
		}

		final Locator grid = page.locator("#custom-sprint-planning-backlog-tree-grid");
		assertTrue(grid.count() > 0, "Backlog tree grid not found");
		waitForGridCellText(grid, "Q1 Planning Session");

		// Text filter should narrow results
		final Locator searchInput = toolbar.locator("vaadin-text-field[placeholder='Search...'] input");
		assertTrue(searchInput.count() > 0, "Search input not found");
		searchInput.first().fill("Q1");
		wait_1000();
		waitForGridCellText(grid, "Q1 Planning Session");
		performFailFastCheck("Sprint planning backlog filtering");
	}
}
