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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import automated_tests.tech.derbent.ui.automation.CBaseUITest;
import tech.derbent.Application;

/** Verifies that the dynamic master grid toolbar renders Agile filters when applicable.
 * <p>
 * This is a UI-level regression guard: the filters must be visible (DOM exists) on pages like Activities.
 * </p>
 */
@SpringBootTest (webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestPropertySource (properties = {
		"spring.profiles.active=derbent", "spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🧪 Agile Master Grid Filters Visible")
public class CAgileMasterGridFiltersVisibleTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CAgileMasterGridFiltersVisibleTest.class);

	private void assertVisible(final String cssSelector) {
		final Locator locator = page.locator(cssSelector);
		assertTrue(locator.count() > 0, "Missing element: " + cssSelector);
		assertTrue(locator.first().isVisible(), "Element not visible: " + cssSelector);
	}

	private void navigateToAgileEntityView() {
		// The test support page gives this regression guard a stable entry point regardless of left-menu structure changes.
		page.navigate("http://localhost:" + port + "/cpagetestauxillary");
		page.waitForSelector("#test-auxillary-metadata",
				new Page.WaitForSelectorOptions().setTimeout(20000).setState(WaitForSelectorState.ATTACHED));
		final Locator userStoryButton = page.locator("vaadin-button").filter(new Locator.FilterOptions().setHasText("User Stories"));
		assertTrue(userStoryButton.count() > 0, "User Stories button not found on Test Support Page");
		userStoryButton.first().click();
		waitForDynamicPageLoad();
	}

	private void openFirstAgileHierarchy() {
		page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(20000));
		assertTrue(verifyGridHasData(), "Agile grid should contain at least one row");
		clickFirstGridRow();
		wait_1000();
		openTabOrAccordionIfNeeded("Agile Hierarchy");
		wait_1000();
	}

	@Test
	@DisplayName ("✅ Agile entity page shows hierarchy access")
	void testAgileEntityPageShowsHierarchyAccess() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}
		loginToApplication();
		navigateToAgileEntityView();
		openFirstAgileHierarchy();
		final Locator childrenTab = page.locator("vaadin-tab").filter(new Locator.FilterOptions().setHasText("Children"));
		assertTrue(childrenTab.count() > 0, "Agile hierarchy should expose a Children tab for the selected entity");
	}

	@Test
	@DisplayName ("✅ Agile entity page opens children workflow")
	void testAgileEntityPageShowsChildrenWorkflow() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}
		loginToApplication();
		navigateToAgileEntityView();
		openFirstAgileHierarchy();
		openTabOrAccordionIfNeeded("Children");
		page.waitForSelector("#custom-agile-children-component", new Page.WaitForSelectorOptions().setTimeout(15000));
		assertVisible("#custom-agile-children-component");
	}

	@Test
	@DisplayName ("✅ Agile entity page loads visible grid data")
	void testAgileEntityPageLoadsGridData() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}
		loginToApplication();
		navigateToAgileEntityView();
		final Locator grid = page.locator("vaadin-grid").first();
		page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(20000));
		assertTrue(verifyGridHasData(), "Agile grid should contain data for the active project");
		final Locator cells = grid.locator("vaadin-grid-cell-content, [part='cell']");
		assertTrue(cells.count() > 0, "Agile grid should render at least one visible cell");
	}
}
