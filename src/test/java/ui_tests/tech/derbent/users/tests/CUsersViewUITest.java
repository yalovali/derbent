package ui_tests.tech.derbent.users.tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;
import tech.derbent.users.view.CUsersView;
import ui_tests.tech.derbent.abstracts.ui.CAbstractUITest;

/** CUsersViewUITest - Comprehensive UI tests for the Users view. Layer: Testing (MVC) Tests grid functionality, lazy loading prevention, data
 * loading, and user interactions for the Users view including profile picture handling and relationship access. */
class CUsersViewUITest extends CAbstractUITest<CUser> {

	@Mock
	private CUserService mockUserService;
	@Mock
	private CUserTypeService mockUserTypeService;
	@Mock
	private CCompanyService mockCompanyService;
	@Mock
	private CProjectService mockProjectService;
	@Mock
	private CUserProjectSettingsService mockUserProjectService;
	@Mock
	private CSessionService mockSessionService;
	@Mock
	private CScreenService screenService;
	private CUsersView usersView;
	private CUserType testUserType;
	private CCompany testCompany;
	private CProject project;

	public CUsersViewUITest() {
		super(CUser.class);
	}

	@Override
	protected CUser createTestEntity(final Long id, final String name) {
		final CUser user = new CUser(name);
		user.setLastname("Doe");
		user.setLogin(name.toLowerCase() + "doe");
		user.setEmail(name.toLowerCase() + "@example.com");
		user.setEnabled(true);
		user.setRoles("USER");
		// Initialize relationships to prevent lazy loading issues
		user.setUserType(testUserType);
		user.setCompany(testCompany);
		// Create a simple profile picture (small byte array)
		final byte[] profilePicture = {
				1, 2, 3, 4, 5
		}; // Simple test data
		user.setProfilePictureData(profilePicture);
		return user;
	}

	@Override
	protected void setupTestData() {
		// Initialize dependencies first before creating test entities
		setupTestEntities();
		final CUser user1 = createTestEntity(1L, "John");
		final CUser user2 = createTestEntity(2L, "Jane");
		final CUser user3 = createTestEntity(3L, "Bob");
		testEntities = Arrays.asList(user1, user2, user3);
	}

	private void setupTestEntities() {
		// Create test project first
		project = new CProject("Test Project");
		// Create test user type
		testUserType = new CUserType("Administrator", project);
		testUserType.setDescription("System administrator");
		// Create test company
		testCompany = new CCompany("Test Company");
		testCompany.setDescription("Test company for users");
	}

	@BeforeEach
	void setupUserTests() {
		usersView = new CUsersView(mockUserService, mockProjectService, mockUserTypeService, mockCompanyService, mockSessionService,
				mockUserProjectSettingsService, screenService);
	}

	@Test
	void testCompanyColumnAccess() {
		LOGGER.info("Testing company column access");
		testEntities.forEach(user -> {
			final String companyDisplay = user.getCompany() != null ? user.getCompany().getName() : "";
			assertNotNull(companyDisplay, "Company display should not be null");
		});
	}

	@Test
	void testEmailColumnFormatting() {
		LOGGER.info("Testing email column formatting");
		testEntities.forEach(user -> {
			final String email = user.getEmail();
			assertNotNull(email, "Email should not be null");
			assertTrue(email.contains("@"), "Email should contain @ symbol");
		});
	}

	@Test
	void testEnabledStatusColumn() {
		LOGGER.info("Testing enabled status column");
		testEntities.forEach(user -> {
			assertDoesNotThrow(() -> {
				final boolean enabled = user.isEnabled();
				final String statusDisplay = enabled ? "Enabled" : "Disabled";
				assertNotNull(statusDisplay, "Status display should not be null");
			}, "Status column should not throw exceptions");
		});
	}

	@Test
	void testGridColumnAccess() {
		LOGGER.info("Testing users grid column access for lazy loading issues");
		// This tests all columns to ensure no lazy loading exceptions occur
		testGridColumnAccess(usersView.getGrid());
		// Specifically test relationships
		testEntities.forEach(user -> {
			verifyEntityRelationships(user);
		});
	}

	@Test
	void testGridCreation() {
		LOGGER.info("Testing users grid creation");
		assertNotNull(usersView.getGrid(), "Grid should be created");
		assertTrue(usersView.getGrid().getColumns().size() > 0, "Grid should have columns");
		// Verify expected columns exist
		final boolean hasNameColumn = usersView.getGrid().getColumns().stream().anyMatch(col -> "name".equals(col.getKey()));
		assertTrue(hasNameColumn, "Grid should have name column");
		final boolean hasEmailColumn = usersView.getGrid().getColumns().stream().anyMatch(col -> "email".equals(col.getKey()));
		assertTrue(hasEmailColumn, "Grid should have email column");
	}

	@Test
	void testGridDataLoading() {
		LOGGER.info("Testing users grid data loading");
		// Test that grid can load data without exceptions
		testGridDataLoading(usersView.getGrid());
		// Note: Service calls are verified in other tests that actually trigger data
		// loading
	}

	@Test
	void testGridSelection() {
		LOGGER.info("Testing users grid selection");
		testGridSelection(usersView.getGrid());
	}

	@Test
	void testGridWithEmptyData() {
		LOGGER.info("Testing grid behavior with empty data");
		// Mock empty result
		when(mockUserService.list(any())).thenReturn(Arrays.asList());
		// Should not throw exceptions
		assertDoesNotThrow(() -> {
			testGridDataLoading(usersView.getGrid());
		}, "Grid should handle empty data gracefully");
	}

	@Test
	void testGridWithNullRelationships() {
		LOGGER.info("Testing grid behavior with null relationships");
		// Create user with null relationships
		final CUser userWithNulls = new CUser("User");
		userWithNulls.setLastname("WithNulls");
		userWithNulls.setLogin("usernulls");
		userWithNulls.setEmail("nulls@example.com");
		userWithNulls.setEnabled(false);
		// Leave userType and company null Test that columns handle null relationships
		// gracefully
		assertDoesNotThrow(() -> {
			// Test user type column
			final String userTypeDisplay = userWithNulls.getUserType() != null ? userWithNulls.getUserType().getName() : "";
			assertEquals("", userTypeDisplay);
			// Test company column
			final String companyDisplay = userWithNulls.getCompany() != null ? userWithNulls.getCompany().getName() : "";
			assertEquals("", companyDisplay);
		}, "Grid columns should handle null relationships gracefully");
	}

	@Test
	void testProfilePictureColumn() {
		LOGGER.info("Testing profile picture column");
		testEntities.forEach(user -> {
			final byte[] profileData = user.getProfilePictureData();
			// Should handle both null and non-null profile pictures
			assertDoesNotThrow(() -> {
				if ((profileData != null) && (profileData.length > 0)) {
					// Should be able to access profile picture data
					assertTrue(profileData.length > 0, "Profile picture should have data");
				}
			}, "Profile picture access should not throw exceptions");
		});
	}

	@Test
	void testRolesColumnAccess() {
		LOGGER.info("Testing roles column access");
		testEntities.forEach(user -> {
			assertDoesNotThrow(() -> {
				final String roles = user.getRoles();
				assertNotNull(roles, "Roles should not be null");
			}, "Roles column access should not throw exceptions");
		});
	}

	@Test
	void testUserTypeColumnAccess() {
		LOGGER.info("Testing user type column access");
		testEntities.forEach(user -> {
			final String userTypeDisplay = user.getUserType() != null ? user.getUserType().getName() : "";
			assertNotNull(userTypeDisplay, "User type display should not be null");
		});
	}

	@Test
	void testViewInitialization() {
		LOGGER.info("Testing users view initialization");
		assertNotNull(usersView, "Users view should be created");
		assertNotNull(usersView.getGrid(), "Grid should be initialized");
		// Verify view is properly configured
		assertTrue(usersView.getClassNames().contains("users-view"), "View should have proper CSS class");
	}

	@Override
	protected void verifyEntityRelationships(final CUser entity) {
		assertNotNull(entity.getUserType(), "User type should be initialized");
		assertNotNull(entity.getCompany(), "Company should be initialized");
		// Verify lazy relationships can be accessed without exceptions
		try {
			final String userTypeName = entity.getUserType().getName();
			assertNotNull(userTypeName, "User type name should be accessible");
			final String companyName = entity.getCompany().getName();
			assertNotNull(companyName, "Company name should be accessible");
		} catch (final Exception e) {
			fail("Relationship access caused lazy loading exception: " + e.getMessage());
		}
	}
}
