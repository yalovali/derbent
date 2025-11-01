package automated_tests.tech.derbent.ui.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

/**
 * Selenium test for User management CRUD operations.
 * Demonstrates Selenium testing for Users entity.
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
@DisplayName("👥 Selenium CRUD Test - Users")
public class CSeleniumUserCrudTest extends CSeleniumBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSeleniumUserCrudTest.class);

	@Test
	@DisplayName("✅ Complete CRUD workflow for Users using Selenium")
	void testUserCrudOperations() {
		LOGGER.info("👥 Starting Selenium CRUD test for Users");

		// Login
		loginToApplication();
		takeScreenshot("selenium-user-01-logged-in");

		// Navigate to Users view
		LOGGER.info("🧭 Navigating to Users view");
		navigateToViewByText("Users");
		wait_2000();
		takeScreenshot("selenium-user-02-users-view");

		// Perform CRUD workflow
		performCRUDWorkflow("User");

		LOGGER.info("✅ User CRUD test completed successfully!");
	}
}
