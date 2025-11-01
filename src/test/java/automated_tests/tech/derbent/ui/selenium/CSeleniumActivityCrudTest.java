package automated_tests.tech.derbent.ui.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

/**
 * Selenium test for Activity CRUD operations.
 * Demonstrates Selenium testing for Activities entity following the same pattern as Projects.
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
@DisplayName("🎯 Selenium CRUD Test - Activities")
public class CSeleniumActivityCrudTest extends CSeleniumBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSeleniumActivityCrudTest.class);

	@Test
	@DisplayName("✅ Complete CRUD workflow for Activities using Selenium")
	void testActivityCrudOperations() {
		LOGGER.info("🎯 Starting Selenium CRUD test for Activities");

		// Login
		loginToApplication();
		takeScreenshot("selenium-activity-01-logged-in");

		// Navigate to Activities view
		LOGGER.info("🧭 Navigating to Activities view");
		navigateToViewByText("Activities");
		wait_2000();
		takeScreenshot("selenium-activity-02-activities-view");

		// Perform CRUD workflow
		performCRUDWorkflow("Activity");

		LOGGER.info("✅ Activity CRUD test completed successfully!");
	}
}
