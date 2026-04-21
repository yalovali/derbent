package automated_tests.tech.derbent.ui.automation.tests;

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
import automated_tests.tech.derbent.ui.automation.CBaseUITest;
import tech.derbent.Application;
import tech.derbent.plm.gnnt.gnntviewentity.view.CComponentGnntBoard;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntBoardFilterToolbar;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTreeGrid;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestPropertySource(properties = {
		"spring.profiles.active=derbent",
		"spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("🧪 Gnnt view display")
public class CGnntViewDisplayTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGnntViewDisplayTest.class);

	private void assertVisible(final String selector) {
		final Locator locator = page.locator(selector);
		assertTrue(locator.count() > 0, "Missing element: " + selector);
		assertTrue(locator.first().isVisible(), "Element not visible: " + selector);
	}

	private void navigateToGnntViews() {
		page.locator("vaadin-text-field[placeholder='Search menu...'] input").first().fill("Gnnt Views");
		wait_500();
		final Locator menuItem = page.locator(".hierarchical-menu-item").filter(new Locator.FilterOptions().setHasText("Gnnt Views")).first();
		assertTrue(menuItem.count() > 0, "Gnnt Views menu item not found");
		menuItem.click();
		waitForDynamicPageLoad();
	}

	private void selectFirstGnntViewRow() {
		final Locator firstCell = page.locator("vaadin-grid vaadin-grid-cell-content").first();
		assertTrue(firstCell.count() > 0, "Gnnt Views grid did not render any cells");
		firstCell.click();
		wait_1000();
	}

	private void selectGnntViewRowByName(final String viewName) {
		final Locator rowCell = page.locator("vaadin-grid vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText(viewName)).first();
		assertTrue(rowCell.count() > 0, "Gnnt view row not found for: " + viewName);
		rowCell.click();
		wait_1000();
	}

	@Test
	@DisplayName("✅ Gnnt sample view opens dedicated board and displays tree timeline")
	void testGnntBoardOpensFromManagementPage() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}
		loginToApplication();
		navigateToGnntViews();
		selectFirstGnntViewRow();
		final Locator openBoardButton = locatorById("cbutton-open-gnnt-board");
		assertTrue(openBoardButton.count() > 0, "Open Gnnt button not found");
		openBoardButton.first().click();
		page.waitForSelector("#" + CComponentGnntBoard.ID_BOARD);
		wait_2000();
		assertVisible("#" + CComponentGnntBoard.ID_BOARD);
		assertVisible("#" + CComponentGnntBoard.ID_SUMMARY);
		assertVisible("#" + CGnntBoardFilterToolbar.ID_TOOLBAR);
		assertVisible("#" + CGnntTreeGrid.ID_TREE_GRID);
		assertTrue(page.locator("#" + CGnntTreeGrid.ID_TREE_GRID + " vaadin-grid-cell-content").count() > 0,
				"Gnnt tree grid has no rendered cells");
		performFailFastCheck("Gnnt tree board display");
	}

	@Test
	@DisplayName("✅ Gnnt tree view opens dedicated board and displays collapsible tree grid")
	void testGnntTreeBoardOpensFromManagementPage() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}
		loginToApplication();
		navigateToGnntViews();
		selectGnntViewRowByName("Release Roadmap");
		final Locator openBoardButton = locatorById("cbutton-open-gnnt-board");
		assertTrue(openBoardButton.count() > 0, "Open Gnnt button not found");
		openBoardButton.first().click();
		page.waitForSelector("#" + CComponentGnntBoard.ID_BOARD);
		wait_2000();
		assertVisible("#" + CComponentGnntBoard.ID_BOARD);
		assertVisible("#" + CGnntBoardFilterToolbar.ID_TOOLBAR);
		assertVisible("#" + CGnntTreeGrid.ID_TREE_GRID);
		assertTrue(page.locator("#" + CGnntTreeGrid.ID_TREE_GRID + " vaadin-grid-cell-content").count() > 0, "Gnnt tree grid has no rendered cells");
		performFailFastCheck("Gnnt board display");
	}
}
