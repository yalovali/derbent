package tech.derbent.abstracts.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import com.vaadin.flow.router.Route;

import tech.derbent.ui.automation.CBaseUITest;

/**
 * CGenericViewTest - Generic superclass for view testing
 * Uses class annotations and metadata instead of magic strings
 * Provides common test patterns for navigation, CRUD, ComboBox testing
 * 
 * @param <T> The entity class being tested
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop", 
	"server.port=8080" 
})
public abstract class CGenericViewTest<T> extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGenericViewTest.class);

	/**
	 * Get the view class being tested
	 */
	protected abstract Class<?> getViewClass();

	/**
	 * Get the entity class being tested
	 */
	protected abstract Class<T> getEntityClass();

	/**
	 * Get the display name for the entity (used in logging)
	 */
	protected String getEntityDisplayName() {
		return getEntityClass().getSimpleName().substring(1); // Remove 'C' prefix
	}

	/**
	 * Get the route path from view class annotation
	 */
	protected String getViewRoute() {
		final Route routeAnnotation = getViewClass().getAnnotation(Route.class);
		if (routeAnnotation == null) {
			throw new IllegalStateException("View class " + getViewClass().getSimpleName() + " has no @Route annotation");
		}
		return routeAnnotation.value();
	}

	/**
	 * Generic navigation test - tests basic navigation to the view
	 */
	@Test
	public void testViewNavigation() {
		final String entityName = getEntityDisplayName();
		LOGGER.debug("Testing navigation to {} view", entityName);
		
		try {
			assertTrue(navigateToViewByClass(getViewClass()),
				"Should successfully navigate to " + entityName + " view");
			LOGGER.debug("Navigation to {} view completed successfully", entityName);
		} catch (final Exception e) {
			LOGGER.error("Navigation test failed for {} view: {}", entityName, e.getMessage());
			takeScreenshot("navigation-test-failed-" + entityName.toLowerCase(), true);
			fail("Navigation test failed for " + entityName + " view: " + e.getMessage());
		}
	}

	/**
	 * Generic view loading test - tests that view loads properly
	 */
	@Test
	public void testViewLoading() {
		final String entityName = getEntityDisplayName();
		LOGGER.debug("Testing {} view loading", entityName);
		
		try {
			assertTrue(navigateToViewByClass(getViewClass()),
				"Should navigate to " + entityName + " view");
			
			// Check for basic view elements
			final int gridCount = page.locator("vaadin-grid").count();
			LOGGER.debug("{} view has {} grids", entityName, gridCount);
			
			final int buttonCount = page.locator("vaadin-button").count();
			LOGGER.debug("{} view has {} buttons", entityName, buttonCount);
			
			if (gridCount == 0 && buttonCount == 0) {
				takeScreenshot("view-loading-no-elements-" + entityName.toLowerCase(), true);
				fail(entityName + " view appears to have no interactive elements");
			}
			
			LOGGER.debug("{} view loading test completed", entityName);
		} catch (final Exception e) {
			LOGGER.error("View loading test failed for {}: {}", entityName, e.getMessage());
			takeScreenshot("view-loading-failed-" + entityName.toLowerCase(), true);
			fail("View loading test failed for " + entityName + ": " + e.getMessage());
		}
	}

	/**
	 * Generic new item test - tests creating new items
	 */
	@Test
	public void testNewItemCreation() {
		final String entityName = getEntityDisplayName();
		LOGGER.debug("Testing new {} creation", entityName);
		
		try {
			assertTrue(navigateToViewByClass(getViewClass()),
				"Should navigate to " + entityName + " view");
			
			// Try to click New button
			clickNew();
			wait_1000();
			
			// Check if a form appeared
			final int formElements = page.locator("vaadin-form-layout, vaadin-text-field, vaadin-text-area").count();
			if (formElements == 0) {
				LOGGER.warn("No form elements found after clicking New for {}", entityName);
				takeScreenshot("new-item-no-form-" + entityName.toLowerCase(), true);
			} else {
				LOGGER.debug("Form appeared with {} elements for new {} creation", formElements, entityName);
				
				// Try to cancel to clean up
				clickCancel();
			}
			
			LOGGER.debug("New {} creation test completed", entityName);
		} catch (final Exception e) {
			LOGGER.error("New item creation test failed for {}: {}", entityName, e.getMessage());
			takeScreenshot("new-item-test-failed-" + entityName.toLowerCase(), true);
			fail("New item creation test failed for " + entityName + ": " + e.getMessage());
		}
	}

	/**
	 * Generic grid interaction test - tests basic grid interactions
	 */
	@Test
	public void testGridInteractions() {
		final String entityName = getEntityDisplayName();
		LOGGER.debug("Testing {} grid interactions", entityName);
		
		try {
			assertTrue(navigateToViewByClass(getViewClass()),
				"Should navigate to " + entityName + " view");
			
			final int gridRowCount = getGridRowCount();
			LOGGER.debug("{} grid has {} rows", entityName, gridRowCount);
			
			if (gridRowCount > 0) {
				// Try to click first row
				clickGrid(0);
				wait_500();
				LOGGER.debug("Successfully clicked first row in {} grid", entityName);
			} else {
				LOGGER.debug("No rows in {} grid to test interactions", entityName);
			}
			
			LOGGER.debug("{} grid interactions test completed", entityName);
		} catch (final Exception e) {
			LOGGER.error("Grid interactions test failed for {}: {}", entityName, e.getMessage());
			takeScreenshot("grid-interactions-failed-" + entityName.toLowerCase(), true);
			fail("Grid interactions test failed for " + entityName + ": " + e.getMessage());
		}
	}

	/**
	 * Generic accessibility test - tests basic accessibility features
	 */
	@Test
	public void testViewAccessibility() {
		final String entityName = getEntityDisplayName();
		LOGGER.debug("Testing {} view accessibility", entityName);
		
		try {
			assertTrue(navigateToViewByClass(getViewClass()),
				"Should navigate to " + entityName + " view");
			
			testAccessibilityBasics(entityName);
			LOGGER.debug("{} accessibility test completed", entityName);
		} catch (final Exception e) {
			LOGGER.error("Accessibility test failed for {}: {}", entityName, e.getMessage());
			takeScreenshot("accessibility-test-failed-" + entityName.toLowerCase(), true);
			fail("Accessibility test failed for " + entityName + ": " + e.getMessage());
		}
	}

	/**
	 * Generic ComboBox test - tests ComboBox components in the view
	 */
	@Test
	public void testViewComboBoxes() {
		final String entityName = getEntityDisplayName();
		LOGGER.debug("Testing {} ComboBox components", entityName);
		
		try {
			assertTrue(navigateToViewByClass(getViewClass()),
				"Should navigate to " + entityName + " view");
			
			// Try to open new form to access ComboBoxes
			clickNew();
			wait_1000();
			
			final var comboBoxes = page.locator("vaadin-combo-box");
			final int comboBoxCount = comboBoxes.count();
			LOGGER.debug("Found {} ComboBox components in {} form", comboBoxCount, entityName);
			
			// Test each ComboBox if any exist
			for (int i = 0; i < comboBoxCount && i < 3; i++) { // Limit to first 3 to avoid long test times
				try {
					comboBoxes.nth(i).click();
					wait_500();
					
					final var options = page.locator("vaadin-combo-box-item");
					final int optionCount = options.count();
					LOGGER.debug("ComboBox {} has {} options", i, optionCount);
					
					if (optionCount == 0) {
						LOGGER.warn("ComboBox {} in {} view has no options", i, entityName);
					}
					
					// Close ComboBox
					page.click("body");
					wait_500();
				} catch (final Exception cbException) {
					LOGGER.warn("Error testing ComboBox {} in {}: {}", i, entityName, cbException.getMessage());
				}
			}
			
			// Clean up by canceling
			clickCancel();
			
			LOGGER.debug("{} ComboBox test completed", entityName);
		} catch (final Exception e) {
			LOGGER.error("ComboBox test failed for {}: {}", entityName, e.getMessage());
			takeScreenshot("combobox-test-failed-" + entityName.toLowerCase(), true);
			fail("ComboBox test failed for " + entityName + ": " + e.getMessage());
		}
	}
}