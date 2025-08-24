package unit_tests.tech.derbent.users.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.EUserRole;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/** Unit tests for CUser domain class focusing on user roles and company relationships. Layer: Test (MVC) */
class CUserTest extends CTestBase {
	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}

	@Test
	void testAllUserRoleEnumValues() {
		// Test all enum values have proper display names and authorities
		for (final EUserRole role : EUserRole.values()) {
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
		assertEquals(EUserRole.ADMIN, user.getUserRole()); // Should parse enum from
															// string
		assertEquals(roles, user.getRoles());
	}

	@Test
	void testUserRoleEnumDefaultBehavior() {
		// Given
		final CUser user = new CUser("test");
		// When
		final EUserRole defaultRole = user.getUserRole();
		// Then
		assertEquals(EUserRole.TEAM_MEMBER, defaultRole);
		assertNotNull(defaultRole.getDisplayName());
		assertNotNull(defaultRole.getDescription());
		assertEquals("ROLE_TEAM_MEMBER", defaultRole.getAuthority());
	}

	@Test
	void testUserRoleFromString() {
		// Test various string inputs
		assertEquals(EUserRole.ADMIN, EUserRole.fromString("ADMIN"));
		assertEquals(EUserRole.PROJECT_MANAGER, EUserRole.fromString("PROJECT_MANAGER"));
		assertEquals(EUserRole.TEAM_MEMBER, EUserRole.fromString("TEAM_MEMBER"));
		assertEquals(EUserRole.GUEST, EUserRole.fromString("GUEST"));
		// Test legacy mappings
		assertEquals(EUserRole.TEAM_MEMBER, EUserRole.fromString("USER"));
		assertEquals(EUserRole.PROJECT_MANAGER, EUserRole.fromString("MANAGER"));
		// Test case insensitivity
		assertEquals(EUserRole.ADMIN, EUserRole.fromString("admin"));
		assertEquals(EUserRole.PROJECT_MANAGER, EUserRole.fromString("project manager"));
		// Test default fallback
		assertEquals(EUserRole.TEAM_MEMBER, EUserRole.fromString("INVALID_ROLE"));
		assertEquals(EUserRole.TEAM_MEMBER, EUserRole.fromString(null));
		assertEquals(EUserRole.TEAM_MEMBER, EUserRole.fromString(""));
	}

	@Test
	void testUserRoleSetterSynchronization() {
		// Given
		final CUser user = new CUser("testuser");
		// When
		user.setUserRole(EUserRole.ADMIN);
		// Then
		assertEquals(EUserRole.ADMIN, user.getUserRole());
		assertEquals("ADMIN", user.getRoles()); // Should sync with legacy roles string
	}

	@Test
	void testUserRoleSetterWithNull() {
		// Given
		final CUser user = new CUser("testuser");
		// When
		user.setUserRole(null);
		// Then
		assertEquals(EUserRole.TEAM_MEMBER, user.getUserRole()); // Should default to
																	// TEAM_MEMBER
		assertEquals("TEAM_MEMBER", user.getRoles());
	}
}
