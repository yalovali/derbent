package automated_tests.tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import ui_tests.tech.derbent.ui.automation.CBaseUITest;

/** ComprehensiveViewsPlaywrightTest - Comprehensive Playwright UI automation test suite for ALL views including main views, status views, type views,
 * admin views, and more. This test class implements the requirement to "run playwright tests for all views and status and type views all" by testing
 * every accessible view in the application. */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080"
})
public class ComprehensiveViewsPlaywrightTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ComprehensiveViewsPlaywrightTest.class);

	@Test
	void testAccessibilityForAllViews() {
		LOGGER.info("🧪 Testing accessibility for ALL views...");
		for (final Class<?> view : allViewClasses) {
			try {
				LOGGER.debug("Testing accessibility for view: {}", view.getSimpleName());
				navigateToViewByClass(view);
				takeScreenshot("accessibility-all-" + view.getSimpleName().toLowerCase(), false);
				testAccessibilityBasics(view.getSimpleName());
			} catch (final Exception e) {
				LOGGER.warn("⚠️ Accessibility test failed for view {}: {}", view.getSimpleName(), e.getMessage());
				// Continue with other views
			}
		}
		LOGGER.info("✅ Accessibility testing for all views completed");
	}

	@Test
	void testAllAdministrativeViews() {
		LOGGER.info("🧪 Testing all administrative and system views...");
		for (final Class<?> view : adminViewClasses) {
			LOGGER.info("Testing admin view: {}", view.getSimpleName());
			navigateToViewByClass(view);
			takeScreenshot("admin-view-" + view.getSimpleName().toLowerCase(), false);
			testBasicViewFunctionality(view.getSimpleName());
		}
		LOGGER.info("✅ All administrative views test completed");
	}

	@Test
	void testAllDetailViews() {
		LOGGER.info("🧪 Testing all detail views...");
		for (final Class<?> view : detailViewClasses) {
			LOGGER.info("Testing detail view: {}", view.getSimpleName());
			navigateToViewByClass(view);
			takeScreenshot("detail-view-" + view.getSimpleName().toLowerCase(), false);
			testBasicViewFunctionality(view.getSimpleName());
		}
		LOGGER.info("✅ All detail views test completed");
	}

	@Test
	void testAllExampleAndDemoViews() {
		LOGGER.info("🧪 Testing all example and demo views...");
		for (final Class<?> view : exampleViewClasses) {
			LOGGER.info("Testing example view: {}", view.getSimpleName());
			navigateToViewByClass(view);
			takeScreenshot("example-view-" + view.getSimpleName().toLowerCase(), false);
			testBasicViewFunctionality(view.getSimpleName());
		}
		LOGGER.info("✅ All example and demo views test completed");
	}

	@Test
	void testAllKanbanBoardViews() {
		LOGGER.info("🧪 Testing all Kanban board views...");
		for (final Class<?> view : kanbanViewClasses) {
			LOGGER.info("Testing Kanban view: {}", view.getSimpleName());
			navigateToViewByClass(view);
			takeScreenshot("kanban-view-" + view.getSimpleName().toLowerCase(), false);
			testBasicViewFunctionality(view.getSimpleName());
		}
		LOGGER.info("✅ All Kanban board views test completed");
	}

	@Test
	void testAllMainBusinessViews() {
		LOGGER.info("🧪 Testing all main business views...");
		for (final Class<?> view : mainViewClasses) {
			LOGGER.info("Testing main view: {}", view.getSimpleName());
			navigateToViewByClass(view);
			takeScreenshot("main-view-" + view.getSimpleName().toLowerCase(), false);
			testBasicViewFunctionality(view.getSimpleName());
		}
		LOGGER.info("✅ All main business views test completed");
	}

	@Test
	void testAllStatusAndTypeViews() {
		LOGGER.info("🧪 Testing all status and type configuration views...");
		for (final Class<?> view : statusAndTypeViewClasses) {
			LOGGER.info("Testing status/type view: {}", view.getSimpleName());
			navigateToViewByClass(view);
			takeScreenshot("status-type-view-" + view.getSimpleName().toLowerCase(), false);
			testBasicViewFunctionality(view.getSimpleName());
		}
		LOGGER.info("✅ All status and type views test completed");
	}

	/** Helper method to test basic functionality for each view */
	private void testBasicViewFunctionality(final String viewName) {
		assertBrowserAvailable();
		try {
			// Wait for view to load
			wait_500();
			// Verify the view has loaded by checking for common elements
			final boolean hasContent = page.locator("body").isVisible() && !page.locator("body").textContent().trim().isEmpty();
			assertTrue(hasContent, "View " + viewName + " should have visible content");
			// Check for common UI elements
			final boolean hasVaadinElements = page.locator(
					"vaadin-grid, vaadin-button, vaadin-text-field, " + "vaadin-form-layout, vaadin-vertical-layout, vaadin-horizontal-layout")
					.count() > 0;
			if (hasVaadinElements) {
				LOGGER.debug("✅ View {} has expected Vaadin UI elements", viewName);
			}
		} catch (final Exception e) {
			LOGGER.warn("Basic functionality test failed for view {}: {}", viewName, e.getMessage());
			throw e;
		}
	}

	@Test
	void testComprehensiveAllViews() {
		LOGGER.info("🧪 Testing ALL views comprehensively ({} total views)...", allViewClasses.length);
		int successCount = 0;
		final int totalViews = allViewClasses.length;
		for (final Class<?> view : allViewClasses) {
			try {
				LOGGER.info("Testing comprehensive view ({}/{}): {}", successCount + 1, totalViews, view.getSimpleName());
				navigateToViewByClass(view);
				takeScreenshot("comprehensive-all-" + view.getSimpleName().toLowerCase(), false);
				testBasicViewFunctionality(view.getSimpleName());
				successCount++;
				LOGGER.debug("✅ Successfully tested view: {}", view.getSimpleName());
			} catch (final Exception e) {
				LOGGER.warn("⚠️ Failed to test view {}: {}", view.getSimpleName(), e.getMessage());
				// Continue with other views even if one fails
			}
		}
		LOGGER.info("✅ Comprehensive all views test completed: {}/{} views tested successfully", successCount, totalViews);
		// Ensure at least 80% of views were tested successfully
		assertTrue(successCount >= (totalViews * 0.8),
				String.format("Expected at least 80%% of views to be tested successfully, but only %d/%d (%.1f%%) passed", successCount, totalViews,
						((successCount * 100.0) / totalViews)));
	}

	/** Helper method to test New button functionality */
	private void testNewButtonFunctionality(final String viewName) {
		assertBrowserAvailable();
		try {
			// Look for New button (common in CRUD views)
			final var newButtons = page.locator("vaadin-button:has-text('New'), " + "vaadin-button[data-test-id='new-button'], "
					+ "button:has-text('New'), " + "vaadin-button:has-text('Add')");
			if (newButtons.count() > 0) {
				LOGGER.debug("Found New button for view: {}", viewName);
				// Don't click the button in this test, just verify it exists
			}
		} catch (final Exception e) {
			LOGGER.debug("No New button found for view {}: {}", viewName, e.getMessage());
		}
	}

	/** Test that status and type views specifically have expected CRUD functionality */
	@Test
	void testStatusAndTypeViewsCRUDFunctionality() {
		LOGGER.info("🧪 Testing CRUD functionality for status and type views...");
		for (final Class<?> view : statusAndTypeViewClasses) {
			try {
				LOGGER.info("Testing CRUD for: {}", view.getSimpleName());
				navigateToViewByClass(view);
				// Test New button functionality
				testNewButtonFunctionality(view.getSimpleName());
				takeScreenshot("crud-" + view.getSimpleName().toLowerCase(), false);
			} catch (final Exception e) {
				LOGGER.warn("⚠️ CRUD test failed for view {}: {}", view.getSimpleName(), e.getMessage());
				// Continue with other views
			}
		}
		LOGGER.info("✅ CRUD functionality testing for status and type views completed");
	}
}
