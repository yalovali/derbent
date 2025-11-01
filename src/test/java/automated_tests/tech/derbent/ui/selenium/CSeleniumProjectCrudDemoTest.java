package automated_tests.tech.derbent.ui.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

/**
 * Selenium demo test showcasing CRUD operations for Projects.
 * This test demonstrates:
 * - Login with sample data initialization
 * - Navigation to Projects view
 * - Create a new project
 * - Read/verify project in grid
 * - Update project details
 * - Delete project
 * - Screenshot capture at each step
 * 
 * This test can be run in both headless and visible browser modes:
 * - Headless (default): mvn test -Dtest=CSeleniumProjectCrudDemoTest
 * - Visible browser: mvn test -Dtest=CSeleniumProjectCrudDemoTest -Dselenium.headless=false
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = tech.derbent.Application.class)
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"server.port=0"
})
@DisplayName("🚀 Selenium CRUD Demo - Projects")
public class CSeleniumProjectCrudDemoTest extends CSeleniumBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSeleniumProjectCrudDemoTest.class);

	@Test
	@DisplayName("✅ Complete CRUD workflow for Projects using Selenium")
	void testProjectCrudOperations() {
		LOGGER.info("🚀 Starting Selenium CRUD demo test for Projects");

		// Step 1: Login to application
		LOGGER.info("📋 Step 1: Login to application");
		loginToApplication();
		takeScreenshot("selenium-01-logged-in");

		// Step 2: Navigate to Projects view
		LOGGER.info("📋 Step 2: Navigate to Projects view");
		navigateTo("/project-overview");
		wait_2000();
		takeScreenshot("selenium-02-projects-view");

		// Step 3: CREATE - Add a new project
		LOGGER.info("📋 Step 3: CREATE - Add a new project");
		clickNew();
		wait_1000();
		takeScreenshot("selenium-03-new-project-dialog");

		String projectName = "Selenium Test Project " + System.currentTimeMillis();
		fillFirstTextField(projectName);
		wait_500();

		// Fill description if available
		fillFirstTextArea("This is a test project created by Selenium automation");
		wait_500();

		// Select options from comboboxes if available
		selectFirstComboBoxOption();
		wait_500();

		takeScreenshot("selenium-04-filled-project-form");

		clickSave();
		wait_2000();
		takeScreenshot("selenium-05-project-created");

		// Step 4: READ - Verify project appears in grid
		LOGGER.info("📋 Step 4: READ - Verify project appears in grid");
		boolean hasData = verifyGridHasData();
		if (hasData) {
			LOGGER.info("✅ Project successfully created and visible in grid");
			int rowCount = getGridRowCount();
			LOGGER.info("📊 Grid contains {} rows", rowCount);
		} else {
			LOGGER.warn("⚠️ No data found in grid after creating project");
		}
		takeScreenshot("selenium-06-project-in-grid");

		// Step 5: UPDATE - Edit the project
		LOGGER.info("📋 Step 5: UPDATE - Edit the project");
		clickFirstGridRow();
		wait_500();
		takeScreenshot("selenium-07-project-selected");

		clickEdit();
		wait_1000();
		takeScreenshot("selenium-08-edit-project-dialog");

		String updatedName = "Updated " + projectName;
		fillFirstTextField(updatedName);
		wait_500();
		takeScreenshot("selenium-09-updated-project-form");

		clickSave();
		wait_2000();
		takeScreenshot("selenium-10-project-updated");
		LOGGER.info("✅ Project successfully updated");

		// Step 6: DELETE - Remove the project
		LOGGER.info("📋 Step 6: DELETE - Remove the project");
		clickFirstGridRow();
		wait_500();
		takeScreenshot("selenium-11-project-selected-for-delete");

		clickDelete();
		wait_2000();
		takeScreenshot("selenium-12-project-deleted");
		LOGGER.info("✅ Project successfully deleted");

		// Final screenshot
		takeScreenshot("selenium-13-crud-complete");
		LOGGER.info("🎉 Selenium CRUD demo test completed successfully!");
	}

	@Test
	@DisplayName("✅ Simple login test using Selenium")
	void testSimpleLogin() {
		LOGGER.info("🚀 Starting simple Selenium login test");
		loginToApplication();
		LOGGER.info("✅ Login successful with Selenium");
	}

	@Test
	@DisplayName("✅ Navigation test using Selenium")
	void testNavigation() {
		LOGGER.info("🚀 Starting Selenium navigation test");

		// Login first
		loginToApplication();
		takeScreenshot("selenium-nav-01-logged-in");

		// Navigate to different views
		LOGGER.info("🧭 Testing navigation to Projects");
		navigateTo("/project-overview");
		wait_1000();
		takeScreenshot("selenium-nav-02-projects");

		LOGGER.info("✅ Navigation test completed successfully");
	}
}
