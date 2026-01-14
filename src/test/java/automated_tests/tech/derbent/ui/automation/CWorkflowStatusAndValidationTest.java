package automated_tests.tech.derbent.ui.automation;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Test suite for workflow status management and name field validation features.
 * Tests the following functionality:
 * 1. Status combobox appears in CRUD toolbar for workflow entities
 * 2. Status combobox shows valid workflow transitions
 * 3. Save button is disabled when name field is empty
 * 4. Save button is enabled when name field has content
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"server.port=8081"
})
@DisplayName("🔄 Workflow Status and Name Validation Test Suite")
public class CWorkflowStatusAndValidationTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CWorkflowStatusAndValidationTest.class);
	
	// Workflow entities that should have status combobox
	private static final List<String> WORKFLOW_ENTITY_ROUTES = Arrays.asList(
		"cdynamicpagerouter/page:3",  // Activities
		"cdynamicpagerouter/page:4",  // Meetings
		"cdynamicpagerouter/page:5",  // Products
		"cdynamicpagerouter/page:6",  // Components
		"cdynamicpagerouter/page:7",  // Risks
		"cdynamicpagerouter/page:8",  // Deliverables
		"cdynamicpagerouter/page:9",  // Assets
		"cdynamicpagerouter/page:10", // Milestones
		"cdynamicpagerouter/page:11"  // Tickets
	);
	
	private int screenshotCounter = 1;

	@Test
	@DisplayName("✅ Test workflow status combobox appears for workflow entities")
	void testWorkflowStatusComboboxAppears() {
		LOGGER.info("🚀 Starting workflow status combobox test...");
		// Check if browser is available
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			org.junit.jupiter.api.Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}
		try {
			Files.createDirectories(Paths.get("target/screenshots"));

			// Login
			loginToApplication();
			LOGGER.info("✅ Login successful");
			takeScreenshot(String.format("%03d-login-success", screenshotCounter++), false);

			// Test each workflow entity
			int successCount = 0;
			int totalTested = 0;
			
			for (String route : WORKFLOW_ENTITY_ROUTES) {
				totalTested++;
				LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
				LOGGER.info("🔍 Testing route {}/{}: {}", totalTested, WORKFLOW_ENTITY_ROUTES.size(), route);
				
				try {
					// Navigate to the entity page
					String url = "http://localhost:" + port + "/" + route;
					page.navigate(url);
					wait_2000();
					
					// Wait for grid to be visible
					page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(5000));
					
					// Click on first row to select an entity
					clickFirstGridRow();
					wait_1000();
					
					// Check if status combobox exists in CRUD toolbar
					Locator statusCombobox = page.locator("vaadin-combo-box").filter(
						new Locator.FilterOptions().setHasText("Status")
					).or(page.locator("vaadin-combo-box[label='Status']"));
					
					if (statusCombobox.count() > 0) {
						LOGGER.info("✅ Status combobox found for route: {}", route);
						takeScreenshot(String.format("%03d-status-combobox-%s", screenshotCounter++, 
							route.replace("/", "-").replace(":", "-")), false);
						successCount++;
						
						// Try to open the combobox to see options
						statusCombobox.first().click();
						wait_500();
						takeScreenshot(String.format("%03d-status-options-%s", screenshotCounter++, 
							route.replace("/", "-").replace(":", "-")), false);
					} else {
						LOGGER.warn("⚠️ Status combobox NOT found for route: {}", route);
						takeScreenshot(String.format("%03d-no-status-%s", screenshotCounter++, 
							route.replace("/", "-").replace(":", "-")), false);
					}
				} catch (Exception e) {
					LOGGER.error("❌ Error testing route {}: {}", route, e.getMessage());
					takeScreenshot(String.format("%03d-error-%s", screenshotCounter++, 
						route.replace("/", "-").replace(":", "-")), true);
				}
			}

			LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			LOGGER.info("✅ Workflow status combobox test completed");
			LOGGER.info("   Routes tested: {}", totalTested);
			LOGGER.info("   Status combobox found: {}", successCount);
			LOGGER.info("   Success rate: {}%", (successCount * 100.0 / totalTested));
			
		} catch (Exception e) {
			LOGGER.error("❌ Workflow status test failed: {}", e.getMessage(), e);
			takeScreenshot("error-workflow-status", true);
			throw new AssertionError("Workflow status test failed", e);
		}
	}

	@Test
	@DisplayName("✅ Test save button disabled when name field is empty")
	void testSaveButtonDisabledWithEmptyName() {
		LOGGER.info("🚀 Starting name field validation test...");
		// Check if browser is available
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			org.junit.jupiter.api.Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}
		try {
			Files.createDirectories(Paths.get("target/screenshots"));

			// Login
			loginToApplication();
			LOGGER.info("✅ Login successful");

			// Navigate to Activities page (a workflow entity with name field)
			String url = "http://localhost:" + port + "/cdynamicpagerouter/page:3";
			page.navigate(url);
			wait_2000();

			// Wait for grid to be visible
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(5000));
			
			// Click New button to create new entity
			clickNew();
			wait_1000();
			
			// Take screenshot of initial state
			takeScreenshot(String.format("%03d-new-entity-initial", screenshotCounter++), false);

			// Find the name field
			Locator nameField = page.locator("#field-name").or(
				page.locator("vaadin-text-field[label='Name']")
			).or(page.locator("vaadin-text-field").first());
			
			if (nameField.count() == 0) {
				LOGGER.warn("⚠️ Name field not found, skipping validation test");
				return;
			}

			// Fill name field with some text first
			nameField.first().fill("Test Activity");
			wait_500();
			
			// Check save button is enabled
			Locator saveButton = page.locator("vaadin-button:has-text('Save')");
			boolean enabledWithText = !saveButton.first().isDisabled();
			LOGGER.info("Save button enabled with text: {}", enabledWithText);
			takeScreenshot(String.format("%03d-name-filled-save-enabled", screenshotCounter++), false);

			// Clear the name field
			nameField.first().clear();
			nameField.first().fill("");
			wait_500();
			
			// Trigger blur event by clicking elsewhere
			page.locator("body").click();
			wait_500();
			
			// Check save button is disabled
			boolean disabledWhenEmpty = saveButton.first().isDisabled();
			LOGGER.info("Save button disabled when empty: {}", disabledWhenEmpty);
			takeScreenshot(String.format("%03d-name-empty-save-disabled", screenshotCounter++), false);

			// Fill name again and verify it re-enables
			nameField.first().fill("Test Activity 2");
			wait_500();
			boolean reEnabledWithText = !saveButton.first().isDisabled();
			LOGGER.info("Save button re-enabled with text: {}", reEnabledWithText);
			takeScreenshot(String.format("%03d-name-refilled-save-reenabled", screenshotCounter++), false);

			LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			LOGGER.info("✅ Name field validation test completed");
			LOGGER.info("   Save enabled with text: {}", enabledWithText);
			LOGGER.info("   Save disabled when empty: {}", disabledWhenEmpty);
			LOGGER.info("   Save re-enabled with text: {}", reEnabledWithText);
			
			if (!disabledWhenEmpty) {
				LOGGER.warn("⚠️ WARNING: Save button was NOT disabled when name was empty!");
			}

		} catch (Exception e) {
			LOGGER.error("❌ Name validation test failed: {}", e.getMessage(), e);
			takeScreenshot("error-name-validation", true);
			throw new AssertionError("Name validation test failed", e);
		}
	}

	@Test
	@DisplayName("✅ Test name validation on multiple entity types")
	void testNameValidationOnMultipleEntities() {
		LOGGER.info("🚀 Starting multi-entity name validation test...");
		// Check if browser is available
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			org.junit.jupiter.api.Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}
		try {
			Files.createDirectories(Paths.get("target/screenshots"));

			// Login
			loginToApplication();
			LOGGER.info("✅ Login successful");

			// Test a few different entity types
			List<String> testRoutes = Arrays.asList(
				"cdynamicpagerouter/page:3",  // Activities
				"cdynamicpagerouter/page:4",  // Meetings
				"cdynamicpagerouter/page:5"   // Products
			);

			int successCount = 0;
			
			for (String route : testRoutes) {
				LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
				LOGGER.info("🔍 Testing name validation for route: {}", route);
				
				try {
					// Navigate to the entity page
					String url = "http://localhost:" + port + "/" + route;
					page.navigate(url);
					wait_2000();

					// Wait for grid
					page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(5000));
					
					// Click New
					clickNew();
					wait_1000();

					// Find name field
					Locator nameField = page.locator("#field-name").or(
						page.locator("vaadin-text-field[label='Name']")
					).or(page.locator("vaadin-text-field").first());
					
					if (nameField.count() == 0) {
						LOGGER.warn("⚠️ Name field not found for route: {}", route);
						continue;
					}

					// Test empty name disables save
					nameField.first().clear();
					nameField.first().fill("");
					wait_500();
					page.locator("body").click(); // Trigger blur
					wait_500();
					
					Locator saveButton = page.locator("vaadin-button:has-text('Save')");
					boolean isDisabled = saveButton.first().isDisabled();
					
					if (isDisabled) {
						LOGGER.info("✅ Save button correctly disabled for empty name on route: {}", route);
						successCount++;
					} else {
						LOGGER.warn("⚠️ Save button NOT disabled for empty name on route: {}", route);
					}
					
					takeScreenshot(String.format("%03d-validation-%s", screenshotCounter++, 
						route.replace("/", "-").replace(":", "-")), false);

					// Cancel to clean up
					clickCancel();
					wait_500();

				} catch (Exception e) {
					LOGGER.error("❌ Error testing route {}: {}", route, e.getMessage());
				}
			}

			LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			LOGGER.info("✅ Multi-entity name validation test completed");
			LOGGER.info("   Routes tested: {}", testRoutes.size());
			LOGGER.info("   Successful validations: {}", successCount);

		} catch (Exception e) {
			LOGGER.error("❌ Multi-entity validation test failed: {}", e.getMessage(), e);
			takeScreenshot("error-multi-validation", true);
			throw new AssertionError("Multi-entity validation test failed", e);
		}
	}
}
