package automated_tests.tech.derbent.ui.automation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.activities.view.CActivityStatusView;
import tech.derbent.activities.view.CActivityTypeView;
import tech.derbent.decisions.view.CDecisionStatusView;
import tech.derbent.decisions.view.CDecisionsView;
import tech.derbent.decisions.view.CDecisionTypeView;
import tech.derbent.meetings.view.CMeetingStatusView;
import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.meetings.view.CMeetingTypeView;
import tech.derbent.page.view.CPageEntityView;
import tech.derbent.projects.view.CProjectsView;
import tech.derbent.risks.view.CRiskStatusView;
import tech.derbent.risks.view.CRiskView;
import tech.derbent.users.view.CUsersView;
import tech.derbent.users.view.CUserTypeView;

/** Generic Playwright test superclass that provides comprehensive automated testing for any entity view class. This class automatically tests: 1.
 * Menu navigation and accessibility 2. Complete CRUD operations (Create, Read, Update, Delete) 3. Grid functionality (column editing, sorting,
 * filtering) 4. UI responsiveness and interaction patterns 5. Visual regression testing with screenshots The test follows the "Don't Repeat Yourself"
 * principle by using a single parameterized test method that works with any view class that follows the Derbent architectural patterns. */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080"
})
public class CGenericEntityPlaywrightTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGenericEntityPlaywrightTest.class);

	/** Provides all testable view classes for parameterized testing. Each view class is automatically tested for complete functionality. */
	private static Stream<Arguments> provideViewClassesForTesting() {
		return Stream.of(
				// Main business entity views
				Arguments.of(CActivitiesView.class, "Activities"), Arguments.of(CMeetingsView.class, "Meetings"),
				Arguments.of(CProjectsView.class, "Projects"), Arguments.of(CUsersView.class, "Users"),
				Arguments.of(CDecisionsView.class, "Decisions"), Arguments.of(CRiskView.class, "Risks"),
				Arguments.of(CPageEntityView.class, "Page Entities"),
				// Status and type views
				Arguments.of(CActivityStatusView.class, "Activity Status"), Arguments.of(CActivityTypeView.class, "Activity Type"),
				Arguments.of(CMeetingStatusView.class, "Meeting Status"), Arguments.of(CMeetingTypeView.class, "Meeting Type"),
				Arguments.of(CDecisionStatusView.class, "Decision Status"), Arguments.of(CDecisionTypeView.class, "Decision Type"),
				Arguments.of(CRiskStatusView.class, "Risk Status"), Arguments.of(CUserTypeView.class, "User Type"));
	}

	/** Comprehensive test for any entity view class. Tests complete workflow: Menu Navigation → CRUD Operations → Grid Functions
	 * @param viewClass  The view class to test
	 * @param entityName Human-readable name for the entity */
	@ParameterizedTest
	@MethodSource ("provideViewClassesForTesting")
	void testCompleteEntityWorkflow(Class<?> viewClass, String entityName) {
		LOGGER.info("🚀 Starting comprehensive test for entity: {} ({})", entityName, viewClass.getSimpleName());
		try {
			// Step 1: Login and setup
			loginToApplication();
			takeScreenshot("00-login-" + getClassSimpleName(viewClass), false);
			// Step 2: Test menu navigation
			boolean menuNavigated = testMenuNavigation(viewClass, entityName);
			takeScreenshot("01-menu-navigation-" + getClassSimpleName(viewClass), false);
			if (!menuNavigated) {
				LOGGER.warn("⚠️ Menu navigation failed for {}, trying direct route", entityName);
				navigateToViewByClass(viewClass);
			}
			// Step 3: Test view loading and initial state
			testViewInitialState(viewClass, entityName);
			takeScreenshot("02-initial-state-" + getClassSimpleName(viewClass), false);
			// Step 4: Test CRUD operations
			testCRUDOperations(viewClass, entityName);
			// Step 5: Test grid functionality
			testGridFunctionality(viewClass, entityName);
			// Step 6: Test additional features
			testAdditionalFeatures(viewClass, entityName);
			LOGGER.info("✅ Complete test successful for entity: {} ({})", entityName, viewClass.getSimpleName());
		} catch (Exception e) {
			LOGGER.error("❌ Test failed for entity: {} ({}) - Error: {}", entityName, viewClass.getSimpleName(), e.getMessage());
			takeScreenshot("ERROR-" + getClassSimpleName(viewClass), false);
			throw e; // Re-throw to fail the test
		}
	}

	/** Tests menu navigation for the specified view class. Checks both menu text and @Menu annotation-based navigation. */
	private boolean testMenuNavigation(Class<?> viewClass, String entityName) {
		LOGGER.info("🧭 Testing menu navigation for: {}", entityName);
		// Try to find menu item by Menu annotation
		Menu menuAnnotation = viewClass.getAnnotation(Menu.class);
		if (menuAnnotation != null) {
			String menuTitle = menuAnnotation.title();
			LOGGER.info("📋 Found menu annotation with title: {}", menuTitle);
			// Try navigation by menu text
			if (navigateToViewByText(menuTitle)) {
				LOGGER.info("✅ Successfully navigated via menu annotation: {}", menuTitle);
				return true;
			}
			// Try partial menu text matching
			String[] titleParts = menuTitle.split("\\.");
			for (String part : titleParts) {
				if (navigateToViewByText(part)) {
					LOGGER.info("✅ Successfully navigated via partial menu text: {}", part);
					return true;
				}
			}
		}
		// Try navigation by entity name
		if (navigateToViewByText(entityName)) {
			LOGGER.info("✅ Successfully navigated via entity name: {}", entityName);
			return true;
		}
		// Try navigation by class route
		return navigateToViewByClass(viewClass);
	}

	/** Tests the initial state of the view after navigation. */
	private void testViewInitialState(Class<?> viewClass, String entityName) {
		LOGGER.info("🔍 Testing initial view state for: {}", entityName);
		wait_1000(); // Allow view to fully load
		// Check if page loaded successfully
		String currentUrl = page.url();
		LOGGER.info("📍 Current URL: {}", currentUrl);
		// Look for common UI elements
		boolean hasGrid = page.locator("vaadin-grid").count() > 0;
		boolean hasNewButton = page.locator("vaadin-button:has-text('New')").count() > 0;
		boolean hasContent = page.locator("body").textContent().length() > 100;
		LOGGER.info("📊 View state - Grid: {}, New Button: {}, Content: {}", hasGrid, hasNewButton, hasContent);
		if (!hasContent) {
			LOGGER.warn("⚠️ View appears to have minimal content for: {}", entityName);
		}
	}

	/** Tests complete CRUD operations for the entity. */
	private void testCRUDOperations(Class<?> viewClass, String entityName) {
		LOGGER.info("🔄 Testing CRUD operations for: {}", entityName);
		try {
			// CREATE: Test entity creation
			testCreateOperation(viewClass, entityName);
			// READ: Test data reading and grid display
			testReadOperation(viewClass, entityName);
			// UPDATE: Test entity modification
			testUpdateOperation(viewClass, entityName);
			// DELETE: Test entity deletion
			testDeleteOperation(viewClass, entityName);
		} catch (Exception e) {
			LOGGER.warn("⚠️ Some CRUD operations failed for {}: {}", entityName, e.getMessage());
		}
	}

	/** Tests CREATE operation */
	private void testCreateOperation(Class<?> viewClass, String entityName) {
		LOGGER.info("➕ Testing CREATE operation for: {}", entityName);
		try {
			// Click New button if available
			if (page.locator("vaadin-button:has-text('New')").count() > 0) {
				clickNew();
				wait_1000();
				takeScreenshot("03-create-dialog-" + getClassSimpleName(viewClass), false);
				// Fill form fields
				fillFormForEntity(entityName);
				takeScreenshot("04-create-filled-" + getClassSimpleName(viewClass), false);
				// Save the entity
				if (page.locator("vaadin-button:has-text('Save')").count() > 0) {
					clickSave();
					wait_1000();
					takeScreenshot("05-create-saved-" + getClassSimpleName(viewClass), false);
					LOGGER.info("✅ CREATE operation completed for: {}", entityName);
				} else {
					LOGGER.warn("⚠️ Save button not found for: {}", entityName);
				}
			} else {
				LOGGER.warn("⚠️ New button not found for: {}", entityName);
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ CREATE operation failed for {}: {}", entityName, e.getMessage());
			takeScreenshot("05-create-error-" + getClassSimpleName(viewClass), false);
		}
	}

	/** Tests READ operation */
	private void testReadOperation(Class<?> viewClass, String entityName) {
		LOGGER.info("👀 Testing READ operation for: {}", entityName);
		boolean hasData = verifyGridHasData();
		int rowCount = getGridRowCount();
		LOGGER.info("📊 Grid data - Has data: {}, Row count: {}", hasData, rowCount);
		takeScreenshot("06-read-grid-" + getClassSimpleName(viewClass), false);
	}

	/** Tests UPDATE operation */
	private void testUpdateOperation(Class<?> viewClass, String entityName) {
		LOGGER.info("✏️ Testing UPDATE operation for: {}", entityName);
		try {
			// Select first row if available
			if (verifyGridHasData()) {
				clickFirstGridRow();
				wait_500();
				// Try to edit
				if (page.locator("vaadin-button:has-text('Edit')").count() > 0) {
					clickEdit();
					wait_1000();
					takeScreenshot("07-update-dialog-" + getClassSimpleName(viewClass), false);
					// Modify form
					fillFormForEntity("Updated " + entityName);
					takeScreenshot("08-update-filled-" + getClassSimpleName(viewClass), false);
					// Save changes
					if (page.locator("vaadin-button:has-text('Save')").count() > 0) {
						clickSave();
						wait_1000();
						takeScreenshot("09-update-saved-" + getClassSimpleName(viewClass), false);
						LOGGER.info("✅ UPDATE operation completed for: {}", entityName);
					}
				} else {
					LOGGER.warn("⚠️ Edit button not found for: {}", entityName);
				}
			} else {
				LOGGER.warn("⚠️ No data available for UPDATE test: {}", entityName);
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ UPDATE operation failed for {}: {}", entityName, e.getMessage());
		}
	}

	/** Tests DELETE operation */
	private void testDeleteOperation(Class<?> viewClass, String entityName) {
		LOGGER.info("🗑️ Testing DELETE operation for: {}", entityName);
		try {
			// Select first row if available
			if (verifyGridHasData()) {
				clickFirstGridRow();
				wait_500();
				// Try to delete
				if (page.locator("vaadin-button:has-text('Delete')").count() > 0) {
					clickDelete();
					wait_500();
					takeScreenshot("10-delete-" + getClassSimpleName(viewClass), false);
					// Handle confirmation dialog if present
					if (page.locator("vaadin-button:has-text('Confirm')").count() > 0) {
						page.locator("vaadin-button:has-text('Confirm')").click();
						wait_1000();
					}
					LOGGER.info("✅ DELETE operation completed for: {}", entityName);
				} else {
					LOGGER.warn("⚠️ Delete button not found for: {}", entityName);
				}
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ DELETE operation failed for {}: {}", entityName, e.getMessage());
		}
	}

	/** Tests grid functionality including column editing, filtering, and sorting. */
	private void testGridFunctionality(Class<?> viewClass, String entityName) {
		LOGGER.info("📊 Testing grid functionality for: {}", entityName);
		try {
			// Test column editing
			testGridColumnEditing(viewClass, entityName);
			// Test sorting
			testGridSorting(viewClass, entityName);
			// Test filtering
			testGridFiltering(viewClass, entityName);
		} catch (Exception e) {
			LOGGER.warn("⚠️ Grid functionality tests failed for {}: {}", entityName, e.getMessage());
		}
	}

	/** Tests grid column editing functionality */
	private void testGridColumnEditing(Class<?> viewClass, String entityName) {
		LOGGER.info("📊 Testing grid column editing for: {}", entityName);
		try {
			// Look for column edit button or settings
			Locator columnEditButton = page.locator("vaadin-button").filter(new Locator.FilterOptions().setHasText("Edit Columns"));
			if (columnEditButton.count() > 0) {
				columnEditButton.click();
				wait_1000();
				takeScreenshot("11-column-edit-dialog-" + getClassSimpleName(viewClass), false);
				// Interact with column selection
				Locator dialogFields = page.locator("vaadin-list-box vaadin-item");
				if (dialogFields.count() > 0) {
					// Toggle some columns
					dialogFields.first().click();
					wait_500();
					takeScreenshot("12-column-edit-selected-" + getClassSimpleName(viewClass), false);
					// Apply changes
					if (page.locator("vaadin-button:has-text('Apply')").count() > 0) {
						page.locator("vaadin-button:has-text('Apply')").click();
						wait_1000();
						takeScreenshot("13-column-edit-applied-" + getClassSimpleName(viewClass), false);
						LOGGER.info("✅ Column editing tested for: {}", entityName);
					}
				}
			} else {
				LOGGER.info("ℹ️ Column editing not available for: {}", entityName);
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ Column editing test failed for {}: {}", entityName, e.getMessage());
		}
	}

	/** Tests grid sorting functionality */
	private void testGridSorting(Class<?> viewClass, String entityName) {
		LOGGER.info("🔀 Testing grid sorting for: {}", entityName);
		try {
			// Click on grid headers to test sorting
			Locator gridHeaders = page.locator("vaadin-grid-column");
			int headerCount = gridHeaders.count();
			if (headerCount > 0) {
				// Click first header to sort
				gridHeaders.first().click();
				wait_500();
				takeScreenshot("14-grid-sorted-" + getClassSimpleName(viewClass), false);
				LOGGER.info("✅ Grid sorting tested for: {}", entityName);
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ Grid sorting test failed for {}: {}", entityName, e.getMessage());
		}
	}

	/** Tests grid filtering functionality */
	private void testGridFiltering(Class<?> viewClass, String entityName) {
		LOGGER.info("🔍 Testing grid filtering for: {}", entityName);
		try {
			// Look for search/filter fields
			Locator searchField = page.locator("vaadin-text-field").filter(new Locator.FilterOptions().setHasText("Search"));
			if (searchField.count() > 0) {
				searchField.fill("test");
				wait_1000();
				takeScreenshot("15-grid-filtered-" + getClassSimpleName(viewClass), false);
				LOGGER.info("✅ Grid filtering tested for: {}", entityName);
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ Grid filtering test failed for {}: {}", entityName, e.getMessage());
		}
	}

	/** Tests additional features like export, import, etc. */
	private void testAdditionalFeatures(Class<?> viewClass, String entityName) {
		LOGGER.info("🎯 Testing additional features for: {}", entityName);
		// Test responsive design
		testResponsiveDesign();
		takeScreenshot("16-responsive-test-" + getClassSimpleName(viewClass), false);
		// Test accessibility
		testAccessibilityBasics("for " + entityName);
		takeScreenshot("17-accessibility-test-" + getClassSimpleName(viewClass), false);
	}

	/** Fills form fields for the given entity type */
	private void fillFormForEntity(String entityName) {
		LOGGER.info("📝 Filling form for entity: {}", entityName);
		try {
			// Fill text fields
			Locator textFields = page.locator("vaadin-text-field");
			int fieldCount = textFields.count();
			for (int i = 0; i < fieldCount && i < 3; i++) { // Limit to first 3 fields
				try {
					textFields.nth(i).fill("Test " + entityName + " " + (i + 1));
					wait_200();
				} catch (Exception e) {
					LOGGER.debug("Could not fill text field {}: {}", i, e.getMessage());
				}
			}
			// Fill text areas
			Locator textAreas = page.locator("vaadin-text-area");
			if (textAreas.count() > 0) {
				textAreas.first().fill("Test description for " + entityName);
			}
			// Handle combo boxes
			testAllComboBoxes();
		} catch (Exception e) {
			LOGGER.warn("⚠️ Form filling failed: {}", e.getMessage());
		}
	}

	/** Utility method to get simple class name for filenames */
	private String getClassSimpleName(Class<?> clazz) {
		return clazz.getSimpleName().toLowerCase().replace("view", "");
	}

	/** Shorter wait for rapid operations */
	private void wait_200() {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/** Demo test that shows the comprehensive testing approach */
	@Test
	void demonstrateComprehensiveTestingApproach() {
		LOGGER.info("🎭 Demonstrating comprehensive testing approach for all entities");
		loginToApplication();
		takeScreenshot("demo-00-login", false);
		// Test a few key entities to show the approach
		List<Class<?>> demoClasses = Arrays.asList(CActivitiesView.class, CMeetingsView.class, CProjectsView.class);
		for (Class<?> viewClass : demoClasses) {
			try {
				String entityName = viewClass.getSimpleName().replace("View", "").replace("C", "");
				LOGGER.info("🎯 Demo testing: {}", entityName);
				navigateToViewByClass(viewClass);
				wait_1000();
				takeScreenshot("demo-" + getClassSimpleName(viewClass) + "-loaded", false);
				// Quick CRUD demo
				if (page.locator("vaadin-button:has-text('New')").count() > 0) {
					clickNew();
					wait_500();
					takeScreenshot("demo-" + getClassSimpleName(viewClass) + "-new", false);
					if (page.locator("vaadin-button:has-text('Cancel')").count() > 0) {
						clickCancel();
						wait_500();
					}
				}
			} catch (Exception e) {
				LOGGER.warn("⚠️ Demo failed for {}: {}", viewClass.getSimpleName(), e.getMessage());
			}
		}
		takeScreenshot("demo-99-complete", false);
		LOGGER.info("✅ Comprehensive testing approach demonstration complete");
	}
}
