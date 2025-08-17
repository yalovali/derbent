package automated_tests.tech.derbent.risks.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.risks.view.CRiskStatusView;
import ui_tests.tech.derbent.ui.automation.CApplicationGeneric_UITest;

/**
 * CRiskStatusViewPlaywrightTest - Tests for risk status view focusing on navigation
 * behavior and CRUD operations.
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
public class CRiskStatusViewPlaywrightTest extends CApplicationGeneric_UITest {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CRiskStatusViewPlaywrightTest.class);

	@Test
	void testRiskStatusNavigationAndFormCreation() {
		LOGGER.info("🧪 Testing Risk Status view navigation and form creation...");
		
		// Navigate to the risk status view
		navigateToViewByClass(CRiskStatusView.class);
		wait_1000();
		takeScreenshot("risk-status-view-loaded");
		
		// Click new to create a new risk status
		clickNew();
		wait_1000();
		takeScreenshot("risk-status-new-form");
		
		// Fill basic fields
		final String statusName = "Test Risk Status " + System.currentTimeMillis();
		fillFirstTextField(statusName);
		wait_500();
		
		// Try to fill description if available
		final var textAreas = page.locator("vaadin-text-area");
		if (textAreas.count() > 0) {
			textAreas.first().fill("Test description for risk status");
			wait_500();
		}
		
		// Test final status checkbox if available
		final var checkBoxes = page.locator("vaadin-checkbox");
		if (checkBoxes.count() > 0) {
			LOGGER.debug("Testing is final checkbox");
			for (int i = 0; i < checkBoxes.count(); i++) {
				final var checkbox = checkBoxes.nth(i);
				final String text = checkbox.textContent().toLowerCase();
				if (text.contains("final")) {
					checkbox.click();
					wait_500();
					takeScreenshot("risk-status-final-checkbox");
					break;
				}
			}
		}
		
		// Save the form
		clickSave();
		wait_2000();
		takeScreenshot("risk-status-saved");
		
		LOGGER.info("✅ Risk Status navigation and form creation test completed");
	}

	@Test
	void testRiskStatusGridAndSelection() {
		LOGGER.info("🧪 Testing Risk Status grid display and selection...");
		
		// Navigate to the risk status view
		navigateToViewByClass(CRiskStatusView.class);
		wait_1000();
		
		// Check if grid is present and has data
		final var gridRows = page.locator("vaadin-grid-cell-content");
		if (gridRows.count() > 0) {
			LOGGER.debug("Grid has {} rows", gridRows.count());
			
			// Click on first row to select it
			gridRows.first().click();
			wait_1000();
			takeScreenshot("risk-status-grid-selected");
		}
		
		LOGGER.info("✅ Risk Status grid and selection test completed");
	}

	@Test
	void testRiskStatusViewAccessibility() {
		LOGGER.info("🧪 Testing Risk Status view accessibility...");
		
		// Navigate to the risk status view
		navigateToViewByClass(CRiskStatusView.class);
		wait_1000();
		
		// Check that the page title and main elements are present
		assertTrue(page.locator("h1, h2").count() > 0 || 
			page.locator("[role='heading']").count() > 0,
			"Page should have proper heading structure");
		
		// Check for grid presence
		assertTrue(page.locator("vaadin-grid").count() > 0,
			"Grid component should be present");
		
		// Check for toolbar/action buttons
		assertTrue(page.locator("vaadin-button").count() > 0,
			"Action buttons should be present");
		
		takeScreenshot("risk-status-accessibility-check");
		LOGGER.info("✅ Risk Status view accessibility test completed");
	}
}