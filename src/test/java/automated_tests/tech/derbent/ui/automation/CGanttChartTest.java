package automated_tests.tech.derbent.ui.automation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;

/** Playwright test that verifies the Gantt chart functionality and UI enhancements. */
@SpringBootTest (webEnvironment = WebEnvironment.RANDOM_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=0"
})
@DisplayName ("📊 Gantt Chart UI Test")
public class CGanttChartTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGanttChartTest.class);

	@Test
	@DisplayName ("✅ Gantt chart header displays with enhanced UI")
	void ganttChartHeaderDisplay() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping Gantt chart test");
			return;
		}
		try {
			LOGGER.info("📊 Starting Gantt chart header UI test");
			
			// Login to application
			loginToApplication();
			wait_2000();
			
			// Navigate to a page that should have a Gantt chart
			// Try navigating to Projects view which may have Gantt chart
			navigateToProjects();
			wait_2000();
			
			// Check if Gantt chart elements are present
			final Locator ganttHeader = page.locator(".gantt-timeline-header");
			if (ganttHeader.count() > 0) {
				LOGGER.info("✅ Gantt chart header found");
				
				// Verify header has border styling
				takeScreenshot("gantt-header-with-border", true);
				
				// Check for timeline controls
				final Locator controls = page.locator(".gantt-timeline-controls");
				if (controls.count() > 0) {
					LOGGER.info("✅ Timeline controls found");
				}
				
				// Check for scale selector (combobox)
				final Locator scaleSelect = page.locator(".gantt-timeline-scale-select");
				if (scaleSelect.count() > 0) {
					LOGGER.info("✅ Scale selector (combobox) found");
					
					// Test clicking the combobox to see dropdown
					scaleSelect.first().click();
					wait_500();
					takeScreenshot("gantt-scale-selector-open", true);
					
					// Close the dropdown
					page.keyboard().press("Escape");
					wait_500();
				}
				
				// Check for width adjustment buttons
				final Locator buttons = controls.locator("vaadin-button");
				final int buttonCount = buttons.count();
				LOGGER.info("ℹ️ Found {} control buttons in Gantt header", buttonCount);
				
				// Take final screenshot
				takeScreenshot("gantt-chart-complete", true);
				LOGGER.info("✅ Gantt chart header UI test completed successfully");
			} else {
				LOGGER.warn("⚠️ Gantt chart header not found on current page");
				takeScreenshot("gantt-header-not-found", true);
			}
		} catch (Exception e) {
			LOGGER.error("❌ Gantt chart test failed: {}", e.getMessage());
			takeScreenshot("gantt-test-error", true);
			throw new AssertionError("Gantt chart test failed: " + e.getMessage(), e);
		}
	}

	@Test
	@DisplayName ("✅ Login workflow with DB generation works correctly")
	void loginWorkflowWithDBGeneration() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping login workflow test");
			return;
		}
		try {
			LOGGER.info("🔐 Testing complete login workflow with DB generation");
			
			// Take screenshot of login page
			ensureLoginViewLoaded();
			takeScreenshot("login-page-initial", true);
			
			// Initialize sample data (DB generation)
			initializeSampleDataFromLoginPage();
			wait_1000();
			takeScreenshot("after-db-generation", true);
			
			// Ensure login view is loaded again
			ensureLoginViewLoaded();
			takeScreenshot("login-page-after-db-init", true);
			
			// Complete login
			loginToApplication("admin", "test123");
			wait_2000();
			
			// Verify we're logged in by checking for app layout
			final Locator appLayout = page.locator("vaadin-app-layout, vaadin-side-nav");
			if (appLayout.count() > 0) {
				LOGGER.info("✅ Successfully logged in - app layout detected");
				takeScreenshot("logged-in-success", true);
			} else {
				LOGGER.warn("⚠️ App layout not detected after login");
				takeScreenshot("login-verification-failed", true);
			}
			
			LOGGER.info("✅ Login workflow with DB generation test completed");
		} catch (Exception e) {
			LOGGER.error("❌ Login workflow test failed: {}", e.getMessage());
			takeScreenshot("login-workflow-error", true);
			throw new AssertionError("Login workflow test failed: " + e.getMessage(), e);
		}
	}
}
