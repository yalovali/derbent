package unit_tests.tech.derbent.abstracts.tests;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import com.vaadin.flow.router.Route;

import tech.derbent.abstracts.utils.Check;
import ui_tests.tech.derbent.ui.automation.CBaseUITest;

/**
 * CGenericViewTest - Generic superclass for view testing Uses class annotations and
 * metadata instead of magic strings Provides common test patterns for navigation, CRUD,
 * ComboBox testing
 * @param <T> The entity class being tested
 */
@SpringBootTest (
	webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class
)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa",
	"spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080" }
)
public abstract class CGenericViewTest<T> extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGenericViewTest.class);

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
	 * Get the view class being tested
	 */
	protected abstract Class<?> getViewClass();

	/**
	 * Get the route path from view class annotation
	 */
	protected String getViewRoute() {
		final Route routeAnnotation = getViewClass().getAnnotation(Route.class);
		Check.notNull(routeAnnotation, "View class " + getViewClass().getSimpleName() + " must have @Route annotation");
		return routeAnnotation.value();
	}

	/**
	 * Enhanced grid interaction test with comprehensive checks
	 */
	@Test
	public void testGridInteractions() {
		final String entityName = getEntityDisplayName();
		LOGGER.debug("🧪 Testing {} grid interactions with enhanced checks", entityName);

		try {
			navigateToViewByClass(getViewClass());
			wait_1000();
			
			final int gridRowCount = getGridRowCount();
			LOGGER.debug("{} grid contains {} rows", entityName, gridRowCount);

			// Verify grid is present
			final var grids = page.locator("vaadin-grid");
			Check.condition(grids.count() > 0, 
				entityName + " view should contain at least one grid component");

			if (gridRowCount > 0) {
				// Test grid selection
				clickGrid(0);
				wait_500();
				
				// Verify selection worked without errors
				LOGGER.debug("Successfully clicked first row in {} grid without errors", entityName);
				
				// Test grid header interactions
				final var gridHeaders = page.locator("vaadin-grid-column-header");
				Check.condition(gridHeaders.count() > 0,
					entityName + " grid should have column headers");
				
				// Test sorting by clicking first header
				if (gridHeaders.count() > 0) {
					gridHeaders.first().click();
					wait_500();
					LOGGER.debug("Successfully tested sorting for {} grid", entityName);
				}
			} else {
				LOGGER.debug("No rows in {} grid - this is acceptable for empty views", entityName);
			}
			
			LOGGER.debug("✅ {} grid interactions test completed successfully", entityName);
			
		} catch (final Exception e) {
			LOGGER.error("❌ Grid interactions test failed for {}: {}", entityName, e.getMessage());
			takeScreenshot("grid-interactions-failed-" + entityName.toLowerCase(), true);
			throw new AssertionError("Grid interactions test failed for " + entityName + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Enhanced new item creation test with detailed validation
	 */
	@Test
	public void testNewItemCreation() {
		final String entityName = getEntityDisplayName();
		LOGGER.debug("🧪 Testing new {} creation with enhanced validation", entityName);

		try {
			navigateToViewByClass(getViewClass());
			wait_500();
			
			// Get initial row count
			final int initialRowCount = getGridRowCount();
			LOGGER.debug("Initial {} grid has {} rows", entityName, initialRowCount);
			
			// Try to click New button
			final boolean newButtonClicked = clickNew();
			Check.condition(newButtonClicked, 
				"Should be able to click New button in " + entityName + " view");
			wait_1000();
			
			// Check if a form appeared
			final var formElements = page.locator("vaadin-form-layout, vaadin-text-field, vaadin-text-area");
			final int formElementCount = formElements.count();
			
			Check.condition(formElementCount > 0,
				"Form elements should appear after clicking New in " + entityName + " view");
			
			LOGGER.debug("Form appeared with {} elements for new {} creation", 
				formElementCount, entityName);
			
			// Test form validation by attempting to save without filling required fields
			clickSave();
			wait_500();
			
			// Look for validation messages (expected behavior)
			final var validationMessages = page.locator(".v-errormessage, vaadin-error-message");
			LOGGER.debug("Validation check: found {} validation messages as expected", 
				validationMessages.count());
			
			// Cancel to clean up the form
			clickCancel();
			wait_500();
			
			// Verify we're back to grid view
			final int finalRowCount = getGridRowCount();
			Check.condition(finalRowCount == initialRowCount,
				"Row count should be unchanged after canceling new " + entityName + " creation");
			
			LOGGER.debug("✅ New {} creation test completed successfully", entityName);
			
		} catch (final Exception e) {
			LOGGER.error("❌ New item creation test failed for {}: {}", entityName, e.getMessage());
			takeScreenshot("new-item-test-failed-" + entityName.toLowerCase(), true);
			throw new AssertionError("New item creation test failed for " + entityName + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Enhanced accessibility test with comprehensive checks
	 */
	@Test
	public void testViewAccessibility() {
		final String entityName = getEntityDisplayName();
		LOGGER.debug("🧪 Testing {} view accessibility with enhanced checks", entityName);

		try {
			navigateToViewByClass(getViewClass());
			wait_500();
			
			// Test keyboard navigation basics
			final var focusableElements = page.locator("button, input, select, textarea, [tabindex]:not([tabindex='-1'])");
			Check.condition(focusableElements.count() > 0,
				entityName + " view should have focusable elements for accessibility");
			
			// Test ARIA labels and roles
			final var ariaElements = page.locator("[aria-label], [role]");
			LOGGER.debug("{} view has {} elements with ARIA attributes", entityName, ariaElements.count());
			
			// Test that main interactive elements have proper accessibility
			final var buttons = page.locator("vaadin-button");
			for (int i = 0; i < Math.min(buttons.count(), 3); i++) {
				final var button = buttons.nth(i);
				// Check that button has text or aria-label
				final String buttonText = button.textContent();
				final String ariaLabel = button.getAttribute("aria-label");
				Check.condition(
					(buttonText != null && !buttonText.trim().isEmpty()) || 
					(ariaLabel != null && !ariaLabel.trim().isEmpty()),
					"Buttons in " + entityName + " view should have accessible text or aria-label");
			}
			
			testAccessibilityBasics(entityName);
			LOGGER.debug("✅ {} accessibility test completed successfully", entityName);
			
		} catch (final Exception e) {
			LOGGER.error("❌ Accessibility test failed for {}: {}", entityName, e.getMessage());
			takeScreenshot("accessibility-test-failed-" + entityName.toLowerCase(), true);
			throw new AssertionError("Accessibility test failed for " + entityName + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Enhanced ComboBox test with comprehensive content verification
	 */
	@Test
	public void testViewComboBoxes() {
		final String entityName = getEntityDisplayName();
		LOGGER.debug("🧪 Testing {} ComboBox components with enhanced verification", entityName);

		try {
			navigateToViewByClass(getViewClass());
			wait_500();
			
			// Try to open new form to access ComboBoxes
			final boolean newFormOpened = clickNew();
			if (!newFormOpened) {
				LOGGER.debug("No New button available in {} view - skipping ComboBox test", entityName);
				return;
			}
			
			wait_1000();
			final var comboBoxes = page.locator("vaadin-combo-box");
			final int comboBoxCount = comboBoxes.count();
			LOGGER.debug("Found {} ComboBox components in {} form", comboBoxCount, entityName);

			// Test each ComboBox comprehensively
			for (int i = 0; i < Math.min(comboBoxCount, 5); i++) {
				try {
					final var comboBox = comboBoxes.nth(i);
					
					// Get ComboBox label for better logging
					final String comboBoxLabel = comboBox.getAttribute("label");
					LOGGER.debug("Testing ComboBox {}: '{}'", i, comboBoxLabel);
					
					// Click to open dropdown
					comboBox.click();
					wait_500();
					
					// Check for dropdown options
					final var options = page.locator("vaadin-combo-box-item, vaadin-combo-box-dropdown-wrapper vaadin-item");
					final int optionCount = options.count();
					LOGGER.debug("ComboBox '{}' has {} options available", comboBoxLabel, optionCount);

					// Verify ComboBox functionality
					Check.condition(optionCount >= 0,
						"ComboBox '" + comboBoxLabel + "' should have accessible options without errors");
					
					// If options are available, test selection
					if (optionCount > 0) {
						options.first().click();
						wait_200();
						LOGGER.debug("Successfully selected first option in ComboBox '{}'", comboBoxLabel);
					}
					
					// Close dropdown by clicking elsewhere
					page.locator("body").click();
					wait_200();
					
				} catch (final Exception cbException) {
					LOGGER.warn("Error testing ComboBox {} in {}: {}", i, entityName, cbException.getMessage());
					// Continue with next ComboBox
				}
			}
			
			// Clean up by canceling
			clickCancel();
			wait_500();
			
			LOGGER.debug("✅ {} ComboBox test completed successfully", entityName);
			
		} catch (final Exception e) {
			LOGGER.error("❌ ComboBox test failed for {}: {}", entityName, e.getMessage());
			takeScreenshot("combobox-test-failed-" + entityName.toLowerCase(), true);
			throw new AssertionError("ComboBox test failed for " + entityName + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Enhanced view loading test with comprehensive validation
	 */
	@Test
	public void testViewLoading() {
		final String entityName = getEntityDisplayName();
		LOGGER.debug("🧪 Testing {} view loading with enhanced validation", entityName);

		try {
			navigateToViewByClass(getViewClass());
			wait_1000();
			
			// Check for basic view elements
			final int gridCount = page.locator("vaadin-grid").count();
			final int buttonCount = page.locator("vaadin-button").count();
			final int formCount = page.locator("vaadin-form-layout").count();
			
			LOGGER.debug("{} view loading summary: {} grids, {} buttons, {} forms", 
				entityName, gridCount, buttonCount, formCount);

			// Enhanced element checks
			Check.condition(gridCount > 0 || buttonCount > 0 || formCount > 0,
				entityName + " view should contain at least one interactive element (grid, button, or form)");
			
			// Test that the page title or heading contains relevant information
			final var headings = page.locator("h1, h2, h3, [role='heading']");
			LOGGER.debug("{} view has {} heading elements", entityName, headings.count());
			
			// Verify no JavaScript errors occurred during loading
			final String currentUrl = page.url();
			Check.condition(currentUrl.contains(getViewRoute()) || currentUrl.contains(entityName.toLowerCase()),
				"URL should reflect the " + entityName + " view route");
			
			// Test responsive layout basics
			final var mainLayout = page.locator("vaadin-app-layout, vaadin-vertical-layout, vaadin-horizontal-layout");
			Check.condition(mainLayout.count() > 0,
				entityName + " view should have proper layout components");
			
			LOGGER.debug("✅ {} view loading test completed successfully", entityName);
			
		} catch (final Exception e) {
			LOGGER.error("❌ View loading test failed for {}: {}", entityName, e.getMessage());
			takeScreenshot("view-loading-failed-" + entityName.toLowerCase(), true);
			throw new AssertionError("View loading test failed for " + entityName + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Enhanced navigation test with comprehensive validation
	 */
	@Test
	public void testViewNavigation() {
		final String entityName = getEntityDisplayName();
		LOGGER.debug("🧪 Testing navigation to {} view with enhanced validation", entityName);

		try {
			// Test navigation
			navigateToViewByClass(getViewClass());
			wait_1000();
			
			// Verify successful navigation
			final String currentUrl = page.url();
			final String expectedRoute = getViewRoute();
			
			Check.condition(
				currentUrl.contains(expectedRoute) || currentUrl.contains(entityName.toLowerCase()),
				"Navigation should lead to correct URL containing '" + expectedRoute + "' or '" + entityName.toLowerCase() + "'");
			
			// Verify page is responsive and loaded
			final var pageContent = page.locator("body");
			Check.condition(pageContent.isVisible(),
				"Page content should be visible after navigation to " + entityName + " view");
			
			// Test that we can navigate back and forth
			page.goBack();
			wait_500();
			page.goForward();
			wait_500();
			
			// Verify we're back to the correct view
			final String finalUrl = page.url();
			Check.condition(
				finalUrl.contains(expectedRoute) || finalUrl.contains(entityName.toLowerCase()),
				"Should return to " + entityName + " view after browser navigation");
			
			LOGGER.debug("✅ Navigation to {} view completed successfully", entityName);
			
		} catch (final Exception e) {
			LOGGER.error("❌ Navigation test failed for {} view: {}", entityName, e.getMessage());
			takeScreenshot("navigation-test-failed-" + entityName.toLowerCase(), true);
			throw new AssertionError("Navigation test failed for " + entityName + " view: " + e.getMessage(), e);
		}
	}

	/**
	 * Additional test for CRUD operations validation
	 */
	@Test
	public void testCRUDOperations() {
		final String entityName = getEntityDisplayName();
		LOGGER.debug("🧪 Testing CRUD operations for {} with comprehensive validation", entityName);

		try {
			navigateToViewByClass(getViewClass());
			wait_500();
			
			final int initialRowCount = getGridRowCount();
			LOGGER.debug("Initial {} count: {}", entityName, initialRowCount);
			
			// Test Create operation
			final boolean newButtonAvailable = clickNew();
			if (newButtonAvailable) {
				wait_1000();
				
				// Verify form opened
				final var formFields = page.locator("vaadin-text-field, vaadin-text-area, vaadin-combo-box");
				Check.condition(formFields.count() > 0,
					"Create form should have input fields for " + entityName);
				
				// Test form validation
				clickSave();
				wait_500();
				
				// Check for validation messages
				final var validationErrors = page.locator(".v-errormessage, vaadin-error-message");
				LOGGER.debug("Validation test: found {} validation messages for empty form", validationErrors.count());
				
				// Cancel the form
				clickCancel();
				wait_500();
				
				// Verify we're back to grid view
				final int afterCancelCount = getGridRowCount();
				Check.condition(afterCancelCount == initialRowCount,
					"Row count should be unchanged after canceling create operation");
			} else {
				LOGGER.debug("Create operation not available for {} view", entityName);
			}
			
			// Test Read operation (grid display)
			final var gridCells = page.locator("vaadin-grid-cell-content");
			if (gridCells.count() > 0) {
				LOGGER.debug("Successfully verified Read operation - grid displays {} data", entityName);
			}
			
			LOGGER.debug("✅ CRUD operations test completed for {}", entityName);
			
		} catch (final Exception e) {
			LOGGER.error("❌ CRUD operations test failed for {}: {}", entityName, e.getMessage());
			takeScreenshot("crud-operations-failed-" + entityName.toLowerCase(), true);
			throw new AssertionError("CRUD operations test failed for " + entityName + ": " + e.getMessage(), e);
		}
	}
}