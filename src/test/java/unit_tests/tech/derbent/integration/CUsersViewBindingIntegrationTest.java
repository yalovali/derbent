package unit_tests.tech.derbent.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
 * Integration test to verify that the CUsersView binding issue is fixed. This test
 * specifically addresses the problem: "All bindings created with forField must be
 * completed before calling readBean" that occurred when navigating to 'cusersview'.
 */
@SpringBootTest (classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.jpa.hibernate.ddl-auto=create-drop", "spring.sql.init.mode=never",
	"spring.jpa.defer-datasource-initialization=false" }
)
public class CUsersViewBindingIntegrationTest {

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
	 * Test that form population with user data works without binding errors.
	 */
	@Test
	void testCUsersViewFormPopulationWithUserData() {
		assertDoesNotThrow(() -> {
			// Create CUsersView
			final CUsersView usersView = new CUsersView(screenService, userService,
				projectService, userTypeService, companyService, sessionService,
				userProjectSettingsService);
			// Create a test user
			final var testUser = userService.createEntity();
			testUser.setName("Test User");
			testUser.setLastname("Test Lastname");
			testUser.setLogin("testuser");
			testUser.setEmail("test@example.com");
			// This should trigger readBean without throwing binding completion errors
			usersView.testPopulateForm(testUser);
			System.out.println(
				"✅ CUsersView form populated with user data without binding errors");
		}, "CUsersView form population should not throw binding completion errors");
	}

	/**
	 * Test that CUsersView can be instantiated without binding completion errors. This
	 * reproduces the exact scenario from the bug report.
	 */
	@Test
	void testCUsersViewInstantiationDoesNotThrowBindingError() {
		assertDoesNotThrow(() -> {
			// Create CUsersView - this should trigger form creation and binding
			final CUsersView usersView = new CUsersView(screenService, userService,
				projectService, userTypeService, companyService, sessionService,
				userProjectSettingsService);
			assertNotNull(usersView, "CUsersView should be created successfully");
			assertNotNull(usersView.getBinder(), "Binder should be initialized");
			// Try to populate form with null (this was causing the binding issue)
			usersView.testPopulateForm(null);
			System.out.println(
				"✅ CUsersView instantiated and populated without binding errors");
		}, "CUsersView instantiation should not throw 'All bindings created with forField must be completed' error");
	}
}