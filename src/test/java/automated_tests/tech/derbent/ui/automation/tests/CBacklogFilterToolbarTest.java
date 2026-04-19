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

	private void navigateToSprintEditingPage() {
		page.navigate("http://localhost:" + port + "/cpagetestauxillary");
		page.waitForSelector("#test-auxillary-metadata",
				new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(20000).setState(WaitForSelectorState.ATTACHED));

		final Locator sprintEditingButton = page.locator("vaadin-button").filter(new Locator.FilterOptions().setHasText("Sprints_2")).first();
		assertTrue(sprintEditingButton.count() > 0, "Sprints_2 button not found on Test Support Page");

		final String route = sprintEditingButton.getAttribute("data-route");
		assertTrue(route != null && !route.isBlank(), "Sprints_2 button missing data-route");

		page.navigate("http://localhost:" + port + "/" + route);
		waitForDynamicPageLoad();

		// Select first sprint row to ensure view value is set.
		final Locator cells = page.locator("vaadin-grid vaadin-grid-cell-content");
		final int maxCellScan = Math.min(20, cells.count());
		for (int c = 0; c < maxCellScan; c++) {
			if (cells.nth(c).isVisible()) {
				cells.nth(c).click();
				break;
			}
		}
		wait_1000();

		final Locator backlogNameFilter = page.locator("vaadin-text-field[placeholder='Filter by name...']").first();
		backlogNameFilter.waitFor(new Locator.WaitForOptions().setTimeout(20000));
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
		navigateToSprintEditingPage();

		final Locator toolbar = page.locator(".grid-search-toolbar").filter(
				new Locator.FilterOptions().setHas(page.locator("vaadin-text-field[placeholder='Filter by name...']"))).first();
		assertTrue(toolbar.count() > 0, "Backlog grid-search toolbar not found");
		toolbar.waitFor(new Locator.WaitForOptions().setTimeout(20000));

		final BoundingBox toolbarBox = toolbar.boundingBox();
		assertTrue(toolbarBox != null, "Toolbar bounding box not available");

		final Locator firstField = toolbar.locator("vaadin-text-field").first();
		assertTrue(firstField.count() > 0, "Expected at least one filter text field in toolbar");
		final BoundingBox fieldBox = firstField.boundingBox();
		assertTrue(fieldBox != null, "First field bounding box not available");

		final double topGap = fieldBox.y - toolbarBox.y;
		assertTrue(topGap < 25, "Unexpected empty space above toolbar fields (gap=" + topGap + "px)");

		// Entity type combobox should update grid content
		final Locator comboType = toolbar.locator("vaadin-combo-box").first();
		assertTrue(comboType.count() > 0, "Entity type ComboBox not found in toolbar");
		try {
			selectComboBoxOptionByText(comboType, "Meetings");
		} catch (final AssertionError e) {
			// Some installations use singular labels
			selectComboBoxOptionByText(comboType, "Meeting");
		}

		final Locator grid = toolbar.locator("xpath=following::vaadin-grid[1]");
		assertTrue(grid.count() > 0, "Backlog grid not found next to toolbar");
		waitForGridCellText(grid, "Q1 Planning Session");

		// Text filter should narrow results
		final Locator nameFilterInput = toolbar.locator("vaadin-text-field[placeholder='Filter by name...'] input");
		assertTrue(nameFilterInput.count() > 0, "Name filter input not found");
		nameFilterInput.first().fill("Q1");
		nameFilterInput.first().press("Enter");
		wait_1000();
		waitForGridCellText(grid, "Q1 Planning Session");
		performFailFastCheck("Backlog filtering");
	}
}
