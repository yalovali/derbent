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
 * Test to reproduce and fix the CUsersView binding issue: "All bindings created with
 * forField must be completed before calling readBean" This test specifically addresses
 * the navigation error when trying to access /users
 */
@SpringBootTest (
	webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class
)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa",
	"spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080",
	"spring.sql.init.mode=never" }
)
public class CUsersViewBindingTest {

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
	private CScreenService screenService;

	@Autowired
	private CUserProjectSettingsService userProjectSettingsService;

	/**
	 * Test that CUsersView can be instantiated without throwing the binding error. This
	 * reproduces the exact issue: "All bindings created with forField must be completed
	 * before calling readBean"
	 */
	@Test
	void testCUsersViewCanBeInstantiated() {
		// This should not throw java.lang.IllegalStateException: All bindings created
		// with forField must be completed before calling readBean
		assertDoesNotThrow(() -> {
			final CUsersView view = new CUsersView(screenService, userService,
				projectService, userTypeService, companyService, sessionService,
				userProjectSettingsService);
			assertNotNull(view, "CUsersView should be instantiated successfully");
		}, "CUsersView should be instantiated without binding errors");
	}

	/**
	 * Test that CUsersView can navigate to a user (which triggers populateForm) without
	 * binding errors.
	 */
	@Test
	void testCUsersViewNavigateToUser() {
		assertDoesNotThrow(() -> {
			final CUsersView view = new CUsersView(screenService, userService,
				projectService, userTypeService, companyService, sessionService,
				userProjectSettingsService);
			// Create a test user
			tech.derbent.users.domain.CUser testUser = userService.createEntity();
			testUser.setName("Test User");
			testUser.setLastname("Test Last");
			testUser.setLogin("testuser");
			testUser.setEmail("test@example.com");
			testUser = userService.save(testUser);
			// This should trigger populateForm and might cause the binding error
			view.testPopulateForm(testUser);
			assertNotNull(view, "CUsersView should handle user navigation successfully");
		}, "CUsersView should navigate to user without binding errors");
	}
}