package unit_tests.tech.derbent.users.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tech.derbent.companies.domain.CCompany;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserRole;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Unit tests for CUser domain class focusing on user roles and company relationships. Layer: Test (MVC)
 */
class CUserTest extends CTestBase {

    @Override
    protected void setupForTest() {
        // TODO Auto-generated method stub
    }

    @Test
    void testAllUserRoleEnumValues() {

        // Test all enum values have proper display names and authorities
        for (final CUserRole role : CUserRole.values()) {
            assertNotNull(role.getDisplayName());
            assertNotNull(role.getDescription());
            assertNotNull(role.getAuthority());
            assertTrue(role.getAuthority().startsWith("ROLE_"));
            assertTrue(role.getDisplayName().length() > 0);
            assertTrue(role.getDescription().length() > 0);
        }
    }

    @Test
    void testCompanyRelationship() {
        // Given
        final CUser user = new CUser("testuser");
        final CCompany company = new CCompany("Test Company Inc.");
        // When
        user.setCompany(company);
        // Then
        assertEquals(company, user.getCompany());
        assertNotNull(user.getCompany().getName());
        assertEquals("Test Company Inc.", user.getCompany().getName());
    }

    @Test
    void testNullableCompanyRelationship() {
        // Given
        final CUser user = new CUser("testuser");
        // When
        user.setCompany(null);
        // Then
        assertNull(user.getCompany());
    }

    @Test
    void testUserConstructorWithRoleEnum() {
        // Given
        final String username = "testuser";
        final String password = "password123";
        final String name = "Test User";
        final String email = "test@example.com";
        final CUserRole role = CUserRole.PROJECT_MANAGER;
        // When
        final CUser user = new CUser(username, password, name, email, role);
        // Then
        assertEquals(username, user.getLogin());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(role, user.getUserRole());
        assertEquals("PROJECT_MANAGER", user.getRoles()); // Legacy roles string should be
                                                          // set
    }

    @Test
    void testUserConstructorWithRoleString() {
        // Given
        final String username = "testuser";
        final String password = "password123";
        final String name = "Test User";
        final String email = "test@example.com";
        final String roles = "ADMIN";
        // When
        final CUser user = new CUser(username, password, name, email, roles);
        // Then
        assertEquals(username, user.getLogin());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(CUserRole.ADMIN, user.getUserRole()); // Should parse enum from
                                                           // string
        assertEquals(roles, user.getRoles());
    }

    @Test
    void testUserRoleEnumDefaultBehavior() {
        // Given
        final CUser user = new CUser("test");
        // When
        final CUserRole defaultRole = user.getUserRole();
        // Then
        assertEquals(CUserRole.TEAM_MEMBER, defaultRole);
        assertNotNull(defaultRole.getDisplayName());
        assertNotNull(defaultRole.getDescription());
        assertEquals("ROLE_TEAM_MEMBER", defaultRole.getAuthority());
    }

    @Test
    void testUserRoleFromString() {
        // Test various string inputs
        assertEquals(CUserRole.ADMIN, CUserRole.fromString("ADMIN"));
        assertEquals(CUserRole.PROJECT_MANAGER, CUserRole.fromString("PROJECT_MANAGER"));
        assertEquals(CUserRole.TEAM_MEMBER, CUserRole.fromString("TEAM_MEMBER"));
        assertEquals(CUserRole.GUEST, CUserRole.fromString("GUEST"));
        // Test legacy mappings
        assertEquals(CUserRole.TEAM_MEMBER, CUserRole.fromString("USER"));
        assertEquals(CUserRole.PROJECT_MANAGER, CUserRole.fromString("MANAGER"));
        // Test case insensitivity
        assertEquals(CUserRole.ADMIN, CUserRole.fromString("admin"));
        assertEquals(CUserRole.PROJECT_MANAGER, CUserRole.fromString("project manager"));
        // Test default fallback
        assertEquals(CUserRole.TEAM_MEMBER, CUserRole.fromString("INVALID_ROLE"));
        assertEquals(CUserRole.TEAM_MEMBER, CUserRole.fromString(null));
        assertEquals(CUserRole.TEAM_MEMBER, CUserRole.fromString(""));
    }

    @Test
    void testUserRoleSetterSynchronization() {
        // Given
        final CUser user = new CUser("testuser");
        // When
        user.setUserRole(CUserRole.ADMIN);
        // Then
        assertEquals(CUserRole.ADMIN, user.getUserRole());
        assertEquals("ADMIN", user.getRoles()); // Should sync with legacy roles string
    }

    @Test
    void testUserRoleSetterWithNull() {
        // Given
        final CUser user = new CUser("testuser");
        // When
        user.setUserRole(null);
        // Then
        assertEquals(CUserRole.TEAM_MEMBER, user.getUserRole()); // Should default to
                                                                 // TEAM_MEMBER
        assertEquals("TEAM_MEMBER", user.getRoles());
    }
}