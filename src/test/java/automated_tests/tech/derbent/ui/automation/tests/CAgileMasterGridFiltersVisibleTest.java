package automated_tests.tech.derbent.ui.automation.tests;

import automated_tests.tech.derbent.ui.automation.CBaseUITest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.PlaywrightException;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import com.microsoft.playwright.Locator;

import tech.derbent.Application;

/** Verifies that the dynamic master grid toolbar renders Agile filters when applicable.
 * <p>
 * This is a UI-level regression guard: the filters must be visible (DOM exists) on pages like Activities.
 * </p>
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
@DisplayName ("🧪 Agile Master Grid Filters Visible")
public class CAgileMasterGridFiltersVisibleTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CAgileMasterGridFiltersVisibleTest.class);

	private void navigateToActivities() {
		page.locator("vaadin-text-field[placeholder='Search menu...'] input").first().fill("Activities");
		wait_500();
		final Locator activitiesItem = page.locator(".hierarchical-menu-item").filter(new Locator.FilterOptions().setHasText("Activities")).first();
		if (activitiesItem.count() == 0) {
			takeScreenshot("activities-menu-search-missing", false);
			throw new AssertionError("Activities menu item not found via side menu search");
		}
		activitiesItem.click();
		waitForDynamicPageLoad();
	}

	@Test
	@DisplayName ("✅ Activity page shows Agile filter comboboxes")
	void testActivityMasterToolbarShowsFilters() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}

		loginToApplication();
		navigateToActivities();

		assertVisible("#custom-master-toolbar-clear-filters");
		assertMasterToolbarWrapEnabled();
		assertVisible("#custom-master-filter-epic");
		assertVisible("#custom-master-filter-feature");
		assertVisible("#custom-master-filter-user-story");
		assertVisible("#custom-master-filter-responsible");
		assertVisible("#custom-master-filter-sprint");

	}

	@Test
	@DisplayName ("✅ Agile filter comboboxes cascade (Epic → Feature → User Story)")
	void testAgileFilterComboboxesCascade() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}

		loginToApplication();
		navigateToActivities();

		// Sample data seeds:
		// Epics: Customer Portal Platform, Mobile Application Development
		// Features: Real-time Notifications System (Epic#1), Advanced Search and Filtering (Epic#2)
		// UserStories: User Login and Authentication (Feature#1), Profile Management (Feature#2)
		selectComboBoxOptionByText("custom-master-filter-epic", "Customer Portal Platform");

		assertComboBoxOptionsContain("custom-master-filter-feature", "Real-time Notifications System", true);
		assertComboBoxOptionsContain("custom-master-filter-feature", "Advanced Search and Filtering", false);

		selectComboBoxOptionByText("custom-master-filter-feature", "Real-time Notifications System");
		assertComboBoxOptionsContain("custom-master-filter-user-story", "User Login and Authentication", true);
		assertComboBoxOptionsContain("custom-master-filter-user-story", "Profile Management", false);

		// Switching Epic must switch the child Feature list.
		selectComboBoxOptionByText("custom-master-filter-epic", "Mobile Application Development");
		assertComboBoxOptionsContain("custom-master-filter-feature", "Advanced Search and Filtering", true);
		assertComboBoxOptionsContain("custom-master-filter-feature", "Real-time Notifications System", false);
	}

	@Test
	@DisplayName ("✅ Agile User Story filter updates Activity grid")
	void testAgileUserStoryFilterUpdatesGrid() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}

		loginToApplication();
		navigateToActivities();

		final Locator grid = page.locator("vaadin-grid").first();
		waitForGridCellText(grid, "Implement Login Form UI");
		waitForGridCellText(grid, "Create Profile Edit Form");

		selectComboBoxOptionByText("custom-master-filter-user-story", "User Login and Authentication");
		waitForGridCellGone(grid, "Create Profile Edit Form");
		waitForGridCellText(grid, "Implement Authentication API");

		page.locator("#custom-master-toolbar-clear-filters").click();
		wait_500();
		waitForGridCellText(grid, "Implement Login Form UI");
		waitForGridCellText(grid, "Create Profile Edit Form");
	}

	private void assertFiltersForEntity(final String entityType, final boolean expectEpic, final boolean expectFeature,
			final boolean expectUserStory) {
		final boolean navigated = navigateToDynamicPageByEntityType(entityType);
		assertTrue(navigated, "Failed to navigate to view: " + entityType);
		assertVisible("#custom-master-toolbar-clear-filters");

		assertPresence("#custom-master-filter-epic", expectEpic);
		assertPresence("#custom-master-filter-feature", expectFeature);
		assertPresence("#custom-master-filter-user-story", expectUserStory);

		// Responsible + Sprint should remain available for project items on agile pages.
		assertVisible("#custom-master-filter-responsible");
		assertVisible("#custom-master-filter-sprint");
	}

	private void assertPresence(final String cssSelector, final boolean expectedPresent) {
		final Locator locator = page.locator(cssSelector);
		if (expectedPresent) {
			assertTrue(locator.count() > 0, "Missing element: " + cssSelector);
			assertTrue(locator.first().isVisible(), "Element not visible: " + cssSelector);
			return;
		}
		assertTrue(locator.count() == 0 || !locator.first().isVisible(), "Element should not be visible: " + cssSelector);
	}

	private void assertMasterToolbarWrapEnabled() {
		final Object value = page.evalOnSelector("#custom-master-toolbar-search",
				"el => getComputedStyle(el.closest('.crud-toolbar')).flexWrap");
		final String flexWrap = value != null ? value.toString() : null;
		assertTrue("wrap".equals(flexWrap) || "wrap-reverse".equals(flexWrap),
				"Master toolbar must enable flex-wrap to prevent filter overflow (flexWrap=" + flexWrap + ")");
	}

	private void assertVisible(final String cssSelector) {
		final Locator locator = page.locator(cssSelector);
		assertTrue(locator.count() > 0, "Missing element: " + cssSelector);
		assertTrue(locator.first().isVisible(), "Element not visible: " + cssSelector);
	}

	private void selectComboBoxOptionByText(final String elementId, final String optionText) {
		final Locator host = locatorById(elementId);
		Locator combo = host;
		final Locator embeddedCombo = host.locator("vaadin-combo-box, c-navigable-combo-box, c-combo-box");
		if (embeddedCombo.count() > 0) {
			combo = embeddedCombo.first();
		}
		combo.scrollIntoViewIfNeeded();

		final Locator input = combo.locator("input");
		if (input.count() > 0) {
			input.first().click();
			try {
				input.first().fill(optionText);
			} catch (final PlaywrightException e) {
				// Some combo boxes don't allow direct fill; fallback to open-only.
				LOGGER.debug("Unable to fill combobox {}: {}", elementId, e.getMessage());
			}
		} else {
			combo.click();
		}
		wait_500();

		Locator options = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item")
				.filter(new Locator.FilterOptions().setHasText(optionText));
		for (int attempt = 0; attempt < 5 && options.count() == 0; attempt++) {
			wait_500();
			options = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item")
					.filter(new Locator.FilterOptions().setHasText(optionText));
		}
		assertTrue(options.count() > 0, "Missing combobox option '" + optionText + "' for #" + elementId);
		options.first().click();
		wait_500();
	}

	private void assertComboBoxOptionsContain(final String elementId, final String expectedText, final boolean expectedPresent) {
		final Locator host = locatorById(elementId);
		Locator combo = host;
		final Locator embeddedCombo = host.locator("vaadin-combo-box, c-navigable-combo-box, c-combo-box");
		if (embeddedCombo.count() > 0) {
			combo = embeddedCombo.first();
		}
		combo.scrollIntoViewIfNeeded();

		final Locator input = combo.locator("input");
		if (input.count() > 0) {
			input.first().click();
		} else {
			combo.click();
		}
		wait_500();

		Locator options = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item")
				.filter(new Locator.FilterOptions().setHasText(expectedText));
		for (int attempt = 0; attempt < 5 && options.count() == 0; attempt++) {
			wait_500();
			options = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item")
					.filter(new Locator.FilterOptions().setHasText(expectedText));
		}

		if (expectedPresent) {
			assertTrue(options.count() > 0, "Expected option not present: '" + expectedText + "' in #" + elementId);
		} else {
			assertTrue(options.count() == 0, "Unexpected option present: '" + expectedText + "' in #" + elementId);
		}

		try {
			page.keyboard().press("Escape");
		} catch (final PlaywrightException e) {
			LOGGER.debug("Unable to close combobox overlay via ESC: {}", e.getMessage());
		}
		wait_500();
	}
}
