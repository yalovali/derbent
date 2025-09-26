package tech.derbent.users.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tech.derbent.companies.domain.CCompany;

/** Unit tests for user-company relationship management and enhanced access control. */
public class CUserCompanyRelationshipTest {

    private CUser user;
    private CCompany company;
    private CUserCompanySettings settings;

    @BeforeEach
    void setUp() {
        user = new CUser("Test User");
        user.setLogin("testuser");
        user.setEmail("test@example.com");

        company = new CCompany("Test Company");

        settings = new CUserCompanySettings();
        settings.setOwnershipLevel("MEMBER");
        settings.setRole("DEVELOPER");
        settings.setPrivileges("READ,WRITE");
    }

    @Test
    void testUserCompanySettingsInitialization() {
        // Test that companySettings is properly initialized
        assertNotNull(user.getCompanySettings(), "User companySettings should be initialized");
        assertTrue(user.getCompanySettings().isEmpty(), "User companySettings should be empty initially");
    }

    @Test
    void testAddCompanySettingsToUser() {
        // Test adding company settings to user
        user.addCompanySettings(settings);

        assertEquals(1, user.getCompanySettings().size(), "User should have one company setting");
        assertTrue(user.getCompanySettings().contains(settings), "User should contain the added settings");
        assertEquals(user, settings.getUser(), "Settings should reference the user");
    }

    @Test
    void testBidirectionalRelationship() {
        // Set up the bidirectional relationship
        settings.setUser(user);
        settings.setCompany(company);
        user.addCompanySettings(settings);

        // Verify both sides of the relationship
        assertEquals(1, user.getCompanySettings().size(), "User should have one company setting");
        assertEquals(user, settings.getUser(), "Settings should reference the user");
        assertEquals(company, settings.getCompany(), "Settings should reference the company");
    }

    @Test
    void testRemoveCompanySettingsFromUser() {
        // Set up the relationship first
        settings.setUser(user);
        user.addCompanySettings(settings);

        // Remove the settings
        user.removeCompanySettings(settings);

        assertTrue(user.getCompanySettings().isEmpty(), "User companySettings should be empty after removal");
        assertNull(settings.getUser(), "Settings should not reference the user after removal");
    }

    @Test
    void testOwnershipLevels() {
        // Test OWNER level
        settings.setOwnershipLevel("OWNER");
        assertTrue(settings.isOwner(), "Settings should be owner level");
        assertTrue(settings.isAdmin(), "Owner should also be admin");
        assertTrue(settings.isMember(), "Owner should also be member");

        // Test ADMIN level
        settings.setOwnershipLevel("ADMIN");
        assertFalse(settings.isOwner(), "Settings should not be owner level");
        assertTrue(settings.isAdmin(), "Settings should be admin level");
        assertTrue(settings.isMember(), "Admin should also be member");

        // Test MEMBER level
        settings.setOwnershipLevel("MEMBER");
        assertFalse(settings.isOwner(), "Settings should not be owner level");
        assertFalse(settings.isAdmin(), "Settings should not be admin level");
        assertTrue(settings.isMember(), "Settings should be member level");
    }

    @Test
    void testPrivilegeManagement() {
        // Test adding privileges
        settings.addPrivilege("MANAGE_USERS");
        assertTrue(settings.hasPrivilege("MANAGE_USERS"), "Should have MANAGE_USERS privilege");

        settings.addPrivilege("DELETE");
        assertTrue(settings.hasPrivilege("DELETE"), "Should have DELETE privilege");
        assertTrue(settings.hasPrivilege("MANAGE_USERS"), "Should still have MANAGE_USERS privilege");

        // Test removing privileges
        settings.removePrivilege("MANAGE_USERS");
        assertFalse(settings.hasPrivilege("MANAGE_USERS"), "Should not have MANAGE_USERS privilege");
        assertTrue(settings.hasPrivilege("DELETE"), "Should still have DELETE privilege");
    }

    @Test
    void testCompanyAdminMethods() {
        // Test company admin detection
        settings.setOwnershipLevel("ADMIN");
        assertTrue(settings.isCompanyAdmin(), "Should be company admin");
        assertTrue(settings.canManageUsers(), "Should be able to manage users");

        // Test with privileges
        settings.setOwnershipLevel("MEMBER");
        assertFalse(settings.isCompanyAdmin(), "Should not be company admin");
        
        settings.addPrivilege("MANAGE_USERS");
        assertTrue(settings.canManageUsers(), "Should be able to manage users with privilege");
    }

    @Test
    void testPrimaryCompanyFunctionality() {
        // Test setting primary company
        settings.setIsPrimaryCompany(true);
        assertTrue(settings.isPrimaryCompany(), "Should be primary company");

        // Test through user method
        settings.setUser(user);
        settings.setCompany(company);
        user.addCompanySettings(settings);

        assertEquals(company, user.getPrimaryCompany(), "Primary company should be set correctly");
    }

    @Test
    void testUserCompanyAdminChecks() {
        // Set up company admin relationship
        settings.setUser(user);
        settings.setCompany(company);
        settings.setOwnershipLevel("ADMIN");
        user.addCompanySettings(settings);

        assertTrue(user.isCompanyAdmin(), "User should be company admin");
        assertTrue(user.isCompanyAdmin(company), "User should be admin of specific company");
    }

    @Test
    void testStaticHelperMethods() {
        // Test the static helper methods in CUserCompanySettings
        CUserCompanySettings.addUserToCompany(company, user, settings);

        // Verify that the relationship is set up
        assertEquals(user, settings.getUser(), "Settings should reference the user");
        assertEquals(company, settings.getCompany(), "Settings should reference the company");
        assertEquals(1, user.getCompanySettings().size(), "User should have one company setting");
    }

    @Test
    void testStaticRemoveMethod() {
        // Set up the relationship first
        settings.setUser(user);
        settings.setCompany(company);
        user.addCompanySettings(settings);

        // Remove using static method
        CUserCompanySettings.removeUserFromCompany(company, user);

        assertTrue(user.getCompanySettings().isEmpty(), "User companySettings should be empty after removal");
    }

    @Test
    void testNullSafetyInMethods() {
        // Test null safety
        user.addCompanySettings(null);
        user.removeCompanySettings(null);

        assertTrue(user.getCompanySettings().isEmpty(), "Adding/removing null should not affect the collection");
    }

    @Test
    void testDuplicateAddPrevention() {
        // Add the same settings twice
        user.addCompanySettings(settings);
        user.addCompanySettings(settings);

        assertEquals(1, user.getCompanySettings().size(), "User should have only one instance of the settings");
    }
}