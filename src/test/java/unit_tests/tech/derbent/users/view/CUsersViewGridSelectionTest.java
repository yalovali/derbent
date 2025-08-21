package unit_tests.tech.derbent.users.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;
import tech.derbent.users.view.CUsersView;

/**
 * Integration test to verify the grid selection binding fix works in the full context.
 * This test reproduces the exact scenario from the problem statement: "when clicked
 * inside cuserview grid, there is a readBean error"
 */
@SpringBootTest (classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.jpa.hibernate.ddl-auto=create-drop", "spring.sql.init.mode=never" }
)
public class CUsersViewGridSelectionTest {

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
	 * Test that simulates the exact grid selection scenario that was causing the error.
	 * This reproduces: "when clicked inside cuserview grid, there is a readBean error"
	 */
	@Test
	void testGridSelectionDoesNotCauseReadBeanError() {
		assertDoesNotThrow(() -> {
			// Create the view (this initializes the grid and forms)
			final CUsersView view = new CUsersView(screenService, userService,
				projectService, userTypeService, companyService, sessionService,
				userProjectSettingsService);
			assertNotNull(view, "CUsersView should be created successfully");
			// Create and save some test users to simulate grid data
			CUser user1 = userService.createEntity();
			user1.setName("John");
			user1.setLastname("Doe");
			user1.setLogin("john.doe");
			user1.setEmail("john.doe@example.com");
			user1 = userService.save(user1);
			CUser user2 = userService.createEntity();
			user2.setName("Jane");
			user2.setLastname("Smith");
			user2.setLogin("jane.smith");
			user2.setEmail("jane.smith@example.com");
			user2 = userService.save(user2);
			// Test that the binder can handle readBean operations without errors This is
			// the core operation that was failing in the grid selection The fix should
			// prevent incomplete bindings from causing readBean to fail Get the binder
			// from the view (this represents the form binder) In the real scenario, this
			// would be triggered by grid selection but we're testing the core binding
			// operation directly The key test: multiple readBean calls should not fail
			// due to incomplete bindings This simulates rapid grid selection changes
			final java.lang.reflect.Method getBinder = view.getClass().getSuperclass()
				.getSuperclass().getDeclaredMethod("getBinder");
			getBinder.setAccessible(true);
			final Object binder = getBinder.invoke(view);

			if (binder instanceof CEnhancedBinder) {
				@SuppressWarnings ("unchecked")
				final CEnhancedBinder<CUser> enhancedBinder =
					(CEnhancedBinder<CUser>) binder;
				// These readBean calls should work without throwing incomplete binding
				// errors
				enhancedBinder.readBean(user1);
				enhancedBinder.readBean(user2);
				enhancedBinder.readBean(user1);
				enhancedBinder.readBean(null);
			}
		}, "Grid selection and form population should work without readBean binding errors");
	}

	/**
	 * Test that multiple rapid grid selections work (stress test for binding issues).
	 */
	@Test
	void testRapidGridSelectionChanges() {
		assertDoesNotThrow(() -> {
			final CUsersView view = new CUsersView(screenService, userService,
				projectService, userTypeService, companyService, sessionService,
				userProjectSettingsService);
			// Create multiple users
			final CUser[] users = new CUser[5];

			for (int i = 0; i < 5; i++) {
				users[i] = userService.createEntity();
				users[i].setName("User" + i);
				users[i].setLastname("Test" + i);
				users[i].setLogin("user" + i);
				users[i].setEmail("user" + i + "@test.com");
				users[i] = userService.save(users[i]);
			}
			// Test rapid binder operations (simulating rapid grid selection)
			final java.lang.reflect.Method getBinder = view.getClass().getSuperclass()
				.getSuperclass().getDeclaredMethod("getBinder");
			getBinder.setAccessible(true);
			final Object binder = getBinder.invoke(view);

			if (binder instanceof CEnhancedBinder) {
				@SuppressWarnings ("unchecked")
				final CEnhancedBinder<CUser> enhancedBinder =
					(CEnhancedBinder<CUser>) binder;

				// Simulate rapid grid selection changes - this should not cause binding
				// errors
				for (int cycle = 0; cycle < 3; cycle++) {

					for (int i = 0; i < 5; i++) {
						enhancedBinder.readBean(users[i]);
					}
				}
			}
		}, "Rapid grid selection changes should not cause binding errors");
	}
}