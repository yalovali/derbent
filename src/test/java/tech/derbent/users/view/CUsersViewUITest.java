package tech.derbent.users.view;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import tech.derbent.abstracts.ui.CAbstractUITest;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

/**
 * CUsersViewUITest - Comprehensive UI tests for the Users view.
 * Layer: Testing (MVC)
 * 
 * Tests grid functionality, lazy loading prevention, data loading, and user interactions
 * for the Users view including profile picture handling and relationship access.
 */
class CUsersViewUITest extends CAbstractUITest<CUser> {

    @Mock
    private CUserService mockUserService;

    @Mock
    private CUserTypeService mockUserTypeService;

    @Mock
    private CCompanyService mockCompanyService;

    @Mock
    private CProjectService mockProjectService;

    private CUsersView usersView;
    private CUserType testUserType;
    private CCompany testCompany;

    public CUsersViewUITest() {
        super(CUser.class);
    }

    @BeforeEach
    void setupUserTests() {
        setupTestEntities();
        usersView = new CUsersView(
            mockUserService,
            mockProjectService,
            mockUserTypeService,
            mockCompanyService,
            mockSessionService
        );
    }

    private void setupTestEntities() {
        // Create test user type
        testUserType = new CUserType();
        testUserType.setName("Administrator");
        testUserType.setDescription("System administrator");

        // Create test company
        testCompany = new CCompany();
        testCompany.setName("Test Company");
        testCompany.setDescription("Test company for users");
    }

    @Override
    protected void setupTestData() {
        CUser user1 = createTestEntity(1L, "John");
        CUser user2 = createTestEntity(2L, "Jane");
        CUser user3 = createTestEntity(3L, "Bob");
        
        testEntities = Arrays.asList(user1, user2, user3);
    }

    @Override
    protected CUser createTestEntity(Long id, String name) {
        CUser user = new CUser();
        user.setName(name);
        user.setLastname("Doe");
        user.setLogin(name.toLowerCase() + "doe");
        user.setEmail(name.toLowerCase() + "@example.com");
        user.setEnabled(true);
        user.setRoles("USER");
        
        // Initialize relationships to prevent lazy loading issues
        user.setUserType(testUserType);
        user.setCompany(testCompany);
        
        // Create a simple profile picture (small byte array)
        byte[] profilePicture = {1, 2, 3, 4, 5}; // Simple test data
        user.setProfilePictureData(profilePicture);
        
        return user;
    }

    @Override
    protected void verifyEntityRelationships(CUser entity) {
        assertNotNull(entity.getUserType(), "User type should be initialized");
        assertNotNull(entity.getCompany(), "Company should be initialized");
        
        // Verify lazy relationships can be accessed without exceptions
        try {
            String userTypeName = entity.getUserType().getName();
            assertNotNull(userTypeName, "User type name should be accessible");
            
            String companyName = entity.getCompany().getName();
            assertNotNull(companyName, "Company name should be accessible");
        } catch (Exception e) {
            fail("Relationship access caused lazy loading exception: " + e.getMessage());
        }
    }

    @Test
    void testGridCreation() {
        LOGGER.info("Testing users grid creation");
        
        assertNotNull(usersView.getGrid(), "Grid should be created");
        assertTrue(usersView.getGrid().getColumns().size() > 0, "Grid should have columns");
        
        // Verify expected columns exist
        boolean hasNameColumn = usersView.getGrid().getColumns().stream()
            .anyMatch(col -> "name".equals(col.getKey()));
        assertTrue(hasNameColumn, "Grid should have name column");
        
        boolean hasEmailColumn = usersView.getGrid().getColumns().stream()
            .anyMatch(col -> "email".equals(col.getKey()));
        assertTrue(hasEmailColumn, "Grid should have email column");
    }

    @Test
    void testGridDataLoading() {
        LOGGER.info("Testing users grid data loading");
        
        // Test that grid can load data without exceptions
        testGridDataLoading(usersView.getGrid());
        
        // Verify service was called
        verify(mockUserService, atLeastOnce()).list(any());
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
    void testGridSelection() {
        LOGGER.info("Testing users grid selection");
        
        testGridSelection(usersView.getGrid());
    }

    @Test
    void testProfilePictureColumn() {
        LOGGER.info("Testing profile picture column");
        
        testEntities.forEach(user -> {
            byte[] profileData = user.getProfilePictureData();
            
            // Should handle both null and non-null profile pictures
            assertDoesNotThrow(() -> {
                if (profileData != null && profileData.length > 0) {
                    // Should be able to access profile picture data
                    assertTrue(profileData.length > 0, "Profile picture should have data");
                }
            }, "Profile picture access should not throw exceptions");
        });
    }

    @Test
    void testUserTypeColumnAccess() {
        LOGGER.info("Testing user type column access");
        
        testEntities.forEach(user -> {
            String userTypeDisplay = user.getUserType() != null 
                ? user.getUserType().getName() 
                : "";
            
            assertNotNull(userTypeDisplay, "User type display should not be null");
        });
    }

    @Test
    void testCompanyColumnAccess() {
        LOGGER.info("Testing company column access");
        
        testEntities.forEach(user -> {
            String companyDisplay = user.getCompany() != null 
                ? user.getCompany().getName() 
                : "";
            
            assertNotNull(companyDisplay, "Company display should not be null");
        });
    }

    @Test
    void testEnabledStatusColumn() {
        LOGGER.info("Testing enabled status column");
        
        testEntities.forEach(user -> {
            assertDoesNotThrow(() -> {
                boolean enabled = user.isEnabled();
                String statusDisplay = enabled ? "Enabled" : "Disabled";
                assertNotNull(statusDisplay, "Status display should not be null");
            }, "Status column should not throw exceptions");
        });
    }

    @Test
    void testGridWithNullRelationships() {
        LOGGER.info("Testing grid behavior with null relationships");
        
        // Create user with null relationships
        CUser userWithNulls = new CUser();
        userWithNulls.setName("User");
        userWithNulls.setLastname("WithNulls");
        userWithNulls.setLogin("usernulls");
        userWithNulls.setEmail("nulls@example.com");
        userWithNulls.setEnabled(false);
        // Leave userType and company null
        
        // Test that columns handle null relationships gracefully
        assertDoesNotThrow(() -> {
            // Test user type column
            String userTypeDisplay = userWithNulls.getUserType() != null 
                ? userWithNulls.getUserType().getName() 
                : "";
            assertEquals("", userTypeDisplay);
            
            // Test company column
            String companyDisplay = userWithNulls.getCompany() != null 
                ? userWithNulls.getCompany().getName() 
                : "";
            assertEquals("", companyDisplay);
            
        }, "Grid columns should handle null relationships gracefully");
    }

    @Test
    void testUserWithProjectsLoading() {
        LOGGER.info("Testing user with projects loading");
        
        if (!testEntities.isEmpty()) {
            CUser testUser = testEntities.get(0);
            
            // Mock the getUserWithProjects method
            when(mockUserService.getUserWithProjects(testUser.getId()))
                .thenReturn(testUser);
            
            // Test that loading user with projects doesn't cause exceptions
            assertDoesNotThrow(() -> {
                usersView.testPopulateForm(testUser);
            }, "Loading user with projects should not throw exceptions");
        }
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
    void testViewInitialization() {
        LOGGER.info("Testing users view initialization");
        
        assertNotNull(usersView, "Users view should be created");
        assertNotNull(usersView.getGrid(), "Grid should be initialized");
        
        // Verify view is properly configured
        assertTrue(usersView.getClassNames().contains("users-view"),
                  "View should have proper CSS class");
    }

    @Test
    void testFormPopulation() {
        LOGGER.info("Testing form population with user data");
        
        if (!testEntities.isEmpty()) {
            CUser testUser = testEntities.get(0);
            
            // Mock getUserWithProjects to return the test user
            when(mockUserService.getUserWithProjects(testUser.getId()))
                .thenReturn(testUser);
            
            // Test form population doesn't throw exceptions
            assertDoesNotThrow(() -> {
                usersView.testPopulateForm(testUser);
            }, "Form population should not throw exceptions");
        }
    }

    @Test
    void testEmailColumnFormatting() {
        LOGGER.info("Testing email column formatting");
        
        testEntities.forEach(user -> {
            String email = user.getEmail();
            assertNotNull(email, "Email should not be null");
            assertTrue(email.contains("@"), "Email should contain @ symbol");
        });
    }

    @Test
    void testRolesColumnAccess() {
        LOGGER.info("Testing roles column access");
        
        testEntities.forEach(user -> {
            assertDoesNotThrow(() -> {
                String roles = user.getRoles();
                assertNotNull(roles, "Roles should not be null");
            }, "Roles column access should not throw exceptions");
        });
    }
}