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

/** Regression test for sprint planning board header/tooling layout.
 * 
 * Validates that:
 * 1) the main toolbar does not have large empty space above its fields (alignment issue)
 * 2) backlog text search updates the leaf grid results
 * 3) add-to-sprint dialog opens for a selected backlog item
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
	@DisplayName ("✅ Toolbar alignment + backlog search + add-to-sprint dialog")
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

		final Locator firstField = toolbar.locator("vaadin-combo-box, vaadin-text-field").first();
		assertTrue(firstField.count() > 0, "Expected at least one field in toolbar");
		final BoundingBox fieldBox = firstField.boundingBox();
		assertTrue(fieldBox != null, "First field bounding box not available");

		final double topGap = fieldBox.y - toolbarBox.y;
		assertTrue(topGap < 25, "Unexpected empty space above toolbar fields (gap=" + topGap + "px)");

		final Locator gridLeaves = page.locator("#custom-sprint-planning-backlog-grid");
		assertTrue(gridLeaves.count() > 0, "Backlog leaf grid not found");

		// Text search is hosted in the backlog parent browser header quick-access panel.
		final Locator searchInput = page.locator("#custom-sprint-planning-backlog-search-field input");
		assertTrue(searchInput.count() > 0, "Backlog search input not found");
		searchInput.first().fill("Q1");
		wait_1000();
		waitForGridCellText(gridLeaves, "Q1 Planning Session");

		// Add-to-sprint dialog should be available for backlog items (DnD alternative).
		final Locator addToSprintButton = page.locator("#custom-sprint-planning-add-to-sprint-button");
		assertTrue(addToSprintButton.count() > 0, "Add to sprint button not found");
		addToSprintButton.first().click();
		page.waitForSelector("#custom-sprint-planning-add-to-sprint-sprint-combobox",
				new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(20000));
		page.locator("#custom-sprint-planning-add-to-sprint-cancel").first().click();

		performFailFastCheck("Sprint planning backlog search + add-to-sprint");
	}
}
