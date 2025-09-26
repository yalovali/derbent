package tech.derbent.users.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.projects.domain.CProject;

/** Test class for the new role system functionality. Validates that CUserProjectRole and CUserCompanyRole work as expected. */
@SpringBootTest
@TestPropertySource (locations = "classpath:application-test.properties")
@Transactional
public class CRoleSystemTest {

	@Test
	public void testProjectRoleCreation() {
		// Create a test project
		CProject project = new CProject("Test Project");
		// Create project roles
		CUserProjectRole adminRole = new CUserProjectRole("Admin", project);
		adminRole.setIsAdmin(true);
		adminRole.setIsUser(true);
		adminRole.setIsGuest(false);
		CUserProjectRole userRole = new CUserProjectRole("User", project);
		userRole.setIsAdmin(false);
		userRole.setIsUser(true);
		userRole.setIsGuest(false);
		CUserProjectRole guestRole = new CUserProjectRole("Guest", project);
		guestRole.setIsAdmin(false);
		guestRole.setIsUser(false);
		guestRole.setIsGuest(true);
		// Test boolean attributes
		assertTrue(adminRole.isAdmin());
		assertTrue(adminRole.isUser());
		assertFalse(adminRole.isGuest());
		assertFalse(userRole.isAdmin());
		assertTrue(userRole.isUser());
		assertFalse(userRole.isGuest());
		assertFalse(guestRole.isAdmin());
		assertFalse(guestRole.isUser());
		assertTrue(guestRole.isGuest());
	}

	@Test
	public void testProjectRolePageAccess() {
		CProject project = new CProject("Test Project");
		CUserProjectRole role = new CUserProjectRole("Test Role", project);
		// Test page access
		role.addReadAccess("Dashboard");
		role.addWriteAccess("Settings");
		assertTrue(role.hasReadAccess("Dashboard"));
		assertTrue(role.hasWriteAccess("Settings"));
		assertTrue(role.hasReadAccess("Settings")); // Write implies read
		assertFalse(role.hasWriteAccess("Dashboard"));
		// Test page access collections
		assertEquals(2, role.getReadAccessPages().size());
		assertEquals(1, role.getWriteAccessPages().size());
		// Test removal
		role.removeWriteAccess("Settings");
		assertFalse(role.hasWriteAccess("Settings"));
		assertTrue(role.hasReadAccess("Settings")); // Read access remains
	}

	@Test
	public void testRoleToString() {
		CProject project = new CProject("Test Project");
		CUserProjectRole role = new CUserProjectRole("Test Role", project);
		role.setIsAdmin(true);
		role.addReadAccess("Page1");
		role.addWriteAccess("Page2");
		String roleString = role.toString();
		assertTrue(roleString.contains("Test Role"));
		assertTrue(roleString.contains("isAdmin=true"));
		assertTrue(roleString.contains("readPages=2"));
		assertTrue(roleString.contains("writePages=1"));
	}
}
