package unit_tests.tech.derbent.users.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;
import tech.derbent.users.view.CUsersView;

/**
 * Integration test for CUsersView to verify that navigation works without forField
 * binding errors. This test simulates the exact scenario described in the problem
 * statement: "There was an exception while trying to navigate to 'users'"
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
public class CUsersViewNavigationTest {

	@Autowired
	private CUserService userService;

	@Autowired
	private CProjectService projectService;

	@Autowired
	private CUserTypeService userTypeService;

	@Autowired
	private CCompanyService companyService;

	@Autowired
	private CSessionService sessionService;

	@Autowired
	private CUserProjectSettingsService userProjectSettingsService;

	@Autowired
	private CScreenService screenService;

	/**
	 * Test that form clearing and repopulation works correctly. This simulates the
	 * navigation scenario that could trigger binding errors.
	 */
	@Test
	void testFormClearAndRepopulate() {
		assertDoesNotThrow(() -> {
			final CUsersView view = new CUsersView(screenService, userService,
				projectService, userTypeService, companyService, sessionService,
				userProjectSettingsService);
			// Create test users
			tech.derbent.users.domain.CUser user1 = userService.createEntity();
			user1.setName("User One");
			user1.setLogin("user1");
			user1.setEmail("user1@example.com");
			user1 = userService.save(user1);
			tech.derbent.users.domain.CUser user2 = userService.createEntity();
			user2.setName("User Two");
			user2.setLogin("user2");
			user2.setEmail("user2@example.com");
			user2 = userService.save(user2);
			// Simulate navigation between users (this is where binding errors could
			// occur)
			view.testPopulateForm(user1);
			view.testPopulateForm(null); // Clear form
			view.testPopulateForm(user2); // Populate with different user
			view.testPopulateForm(null); // Clear again
			assertNotNull(view,
				"CUsersView should handle form clearing and repopulation successfully");
		}, "Form clearing and repopulation should work without binding errors");
	}

	/**
	 * Test navigation to the users view without any user ID (list view). This should not
	 * throw: "All bindings created with forField must be completed before calling
	 * readBean"
	 */
	@Test
	void testNavigateToUsersListView() {
		assertDoesNotThrow(() -> {
			final CUsersView view = new CUsersView(screenService, userService,
				projectService, userTypeService, companyService, sessionService,
				userProjectSettingsService);
			// Test that the view can handle null population (simulating navigation to
			// list view)
			view.testPopulateForm(null);
			assertNotNull(view,
				"CUsersView should handle navigation to list view successfully");
		}, "Navigation to /users should work without binding errors");
	}
}