package tech.derbent.users.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.derbent.companies.domain.CCompany;

/** Unit tests for user-company relationship management and enhanced access control. */
public class CUserCompanyRelationshipTest {

	private CUserCompanySetting settings;
	private CUser user;

	@BeforeEach
	void setUp() {
		user = new CUser("Test User");
		user.setLogin("testuser");
		user.setEmail("test@example.com");
		new CCompany("Test Company");
		settings = new CUserCompanySetting();
		settings.setOwnershipLevel("MEMBER");
		settings.setRole("DEVELOPER");
		settings.setPrivileges("READ,WRITE");
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
}
