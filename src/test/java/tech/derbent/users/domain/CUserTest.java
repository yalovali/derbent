package tech.derbent.users.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tech.derbent.companies.domain.CCompany;

/**
 * Unit tests for CUser domain class focusing on user roles and company relationships.
 * Layer: Test (MVC)
 */
class CUserTest {

    @Test
    void testUserRoleEnumDefaultBehavior() {
        // Given
        CUser user = new CUser();
        
        // When
        CUserRole defaultRole = user.getUserRole();
        
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
    void testUserConstructorWithRoleEnum() {
        // Given
        String username = "testuser";
        String password = "password123";
        String name = "Test User";
        String email = "test@example.com";
        CUserRole role = CUserRole.PROJECT_MANAGER;
        
        // When
        CUser user = new CUser(username, password, name, email, role);
        
        // Then
        assertEquals(username, user.getLogin());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(role, user.getUserRole());
        assertEquals("PROJECT_MANAGER", user.getRoles()); // Legacy roles string should be set
    }

    @Test
    void testUserConstructorWithRoleString() {
        // Given
        String username = "testuser";
        String password = "password123";
        String name = "Test User";
        String email = "test@example.com";
        String roles = "ADMIN";
        
        // When
        CUser user = new CUser(username, password, name, email, roles);
        
        // Then
        assertEquals(username, user.getLogin());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(CUserRole.ADMIN, user.getUserRole()); // Should parse enum from string
        assertEquals(roles, user.getRoles());
    }

    @Test
    void testCompanyRelationship() {
        // Given
        CUser user = new CUser();
        CCompany company = new CCompany("Test Company Inc.");
        
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
        CUser user = new CUser();
        
        // When
        user.setCompany(null);
        
        // Then
        assertNull(user.getCompany());
    }

    @Test
    void testUserRoleSetterSynchronization() {
        // Given
        CUser user = new CUser();
        
        // When
        user.setUserRole(CUserRole.ADMIN);
        
        // Then
        assertEquals(CUserRole.ADMIN, user.getUserRole());
        assertEquals("ADMIN", user.getRoles()); // Should sync with legacy roles string
    }

    @Test
    void testUserRoleSetterWithNull() {
        // Given
        CUser user = new CUser();
        
        // When
        user.setUserRole(null);
        
        // Then
        assertEquals(CUserRole.TEAM_MEMBER, user.getUserRole()); // Should default to TEAM_MEMBER
        assertEquals("TEAM_MEMBER", user.getRoles());
    }

    @Test
    void testAllUserRoleEnumValues() {
        // Test all enum values have proper display names and authorities
        for (CUserRole role : CUserRole.values()) {
            assertNotNull(role.getDisplayName());
            assertNotNull(role.getDescription());
            assertNotNull(role.getAuthority());
            assertTrue(role.getAuthority().startsWith("ROLE_"));
            assertTrue(role.getDisplayName().length() > 0);
            assertTrue(role.getDescription().length() > 0);
        }
    }
}