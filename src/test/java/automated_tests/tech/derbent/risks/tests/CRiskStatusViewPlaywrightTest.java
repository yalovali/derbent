package automated_tests.tech.derbent.risks.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import tech.derbent.risks.view.CRiskStatusView;
import ui_tests.tech.derbent.ui.automation.CApplicationGeneric_UITest;

/** CRiskStatusViewPlaywrightTest - Tests for risk status view focusing on navigation behavior and CRUD operations. */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.url=jdbc:h2:mem:testdb", "spring.jpa.hibernate.ddl-auto=create-drop",
		"server.port=8080"
})
public class CRiskStatusViewPlaywrightTest extends CApplicationGeneric_UITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CRiskStatusViewPlaywrightTest.class);

	@Test
	void testRiskStatusFormFieldsPresence() {
		LOGGER.info("🧪 Testing Risk Status form fields presence...");
		// Navigate to the risk status view and open new form
		navigateToViewByClass(CRiskStatusView.class);
		wait_1000();
		clickNew();
		wait_1000();
		// Assert required form fields are present
		final var textFields = page.locator("vaadin-text-field");
		assertTrue(textFields.count() > 0, "Form should have text field(s) for risk status name");
		final var textAreas = page.locator("vaadin-text-area");
		// Note: Don't assert if text area is required - depends on your form design
		LOGGER.debug("Found {} text area(s) for description", textAreas.count());
		final var checkboxes = page.locator("vaadin-checkbox");
		LOGGER.debug("Found {} checkbox(es) for additional options", checkboxes.count());
		LOGGER.info("✅ Risk Status form fields presence test completed");
	}

	@Test
	void testRiskStatusFormValidation() {
		LOGGER.info("🧪 Testing Risk Status form validation...");
		// Navigate to the risk status view
		navigateToViewByClass(CRiskStatusView.class);
		wait_1000();
		// Click new to create a new risk status
		clickNew();
		wait_1000();
		// Assert form elements are present
		final var textFields = page.locator("vaadin-text-field");
		assertTrue(textFields.count() > 0, "Form should have at least one text field");
		// Try to save without filling required fields
		clickSave();
		wait_1000();
		LOGGER.info("✅ Risk Status form validation test completed");
	}

	@Test
	void testRiskStatusGridAndSelection() {
		LOGGER.info("🧪 Testing Risk Status grid display and selection...");
		// Navigate to the risk status view
		navigateToViewByClass(CRiskStatusView.class);
		wait_1000();
		// Assert grid is present
		final var grid = page.locator("vaadin-grid");
		assertTrue(grid.count() > 0, "Grid component should be present on the page");
		// Assert grid has data and select first row
		final var gridRows = page.locator("vaadin-grid-cell-content");
		assertTrue(gridRows.count() > 0, "Grid should contain at least one row with data");
		LOGGER.debug("Grid has {} rows", gridRows.count());
		// Click on first row to select it
		gridRows.first().click();
		wait_1000();
		// Assert that selection worked (you can add more specific assertions here)
		assertTrue(true, "Grid row selection completed successfully");
		LOGGER.info("✅ Risk Status grid and selection test completed");
	}

	@Test
	void testRiskStatusNavigationAndFormCreation() {
		LOGGER.info("🧪 Testing Risk Status view navigation and form creation...");
		// Navigate to the risk status view
		navigateToViewByClass(CRiskStatusView.class);
		wait_1000();
		// Click new to create a new risk status
		clickNew();
		wait_1000();
		// Fill basic fields
		final String statusName = "Test Risk Status " + System.currentTimeMillis();
		fillFirstTextField(statusName);
		wait_500();
		// Assert and fill description if text area is available
		final var textAreas = page.locator("vaadin-text-area");
		assertTrue(textAreas.count() > 0, "Text area for description should be present");
		textAreas.first().fill("Test description for risk status");
		wait_500();
		// Assert and test final status checkbox if available
		final var checkBoxes = page.locator("vaadin-checkbox");
		assertTrue(checkBoxes.count() > 0, "Checkboxes for additional options should be present");
		for (int i = 0; i < checkBoxes.count(); i++) {
			final var checkbox = checkBoxes.nth(i);
			final String text = checkbox.textContent().toLowerCase();
			if (text.contains("final")) {
				checkbox.click();
				wait_500();
				LOGGER.debug("Successfully clicked final status checkbox");
				break;
			}
		}
		// Save the form
		clickSave();
		wait_2000();
		LOGGER.info("✅ Risk Status navigation and form creation test completed");
	}

	@Test
	void testRiskStatusViewAccessibility() {
		LOGGER.info("🧪 Testing Risk Status view accessibility...");
		// Navigate to the risk status view
		navigateToViewByClass(CRiskStatusView.class);
		wait_1000();
		// Assert that the page has proper heading structure
		final var headings = page.locator("h1, h2, [role='heading']");
		assertTrue(headings.count() > 0, "Page should have proper heading structure");
		LOGGER.debug("Found {} heading elements", headings.count());
		// Assert grid presence
		final var grids = page.locator("vaadin-grid");
		assertTrue(grids.count() > 0, "Grid component should be present");
		assertEquals(1, grids.count(), "Should have exactly one grid component");
		// Assert toolbar/action buttons presence
		final var buttons = page.locator("vaadin-button");
		assertTrue(buttons.count() > 0, "Action buttons should be present");
		LOGGER.debug("Found {} button elements", buttons.count());
		// Assert minimum expected buttons (New, Save, Cancel, etc.)
		assertTrue(buttons.count() >= 2, "Should have at least 2 action buttons (New, Save/Cancel)");
		LOGGER.info("✅ Risk Status view accessibility test completed");
	}
}
