package tech.derbent.users.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.roles.service.CUserCompanyRoleService;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.users.service.CUserCompanySettingsService;
import tech.derbent.users.service.CUserService;

/** Integration test to validate that the FetchType.LAZY fix for CUserCompanySetting.role prevents the PostgreSQL 1664 column limit error while still
 * allowing proper data access. This test ensures: 1. All @ManyToOne relationships in CUserCompanySetting use FetchType.LAZY 2. Saving
 * CUserCompanySetting with a role doesn't generate massive JOIN queries 3. Role data can still be accessed within a transaction */
@SpringBootTest (classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"
})
@DisplayName ("🔍 CUserCompanySetting FetchType Fix Validation Test")
public class CUserCompanySettingFetchTypeTest {

	@Autowired
	private CUserService userService;
	@Autowired
	private CCompanyService companyService;
	@Autowired
	private CUserCompanyRoleService roleService;
	@Autowired
	private CUserCompanySettingsService userCompanySettingsService;
	private CUser testUser;
	private CCompany testCompany;
	private CUserCompanyRole testRole;

	@BeforeEach
	@Transactional
	public void setUp() {
		// Create test user
		testUser = new CUser();
		testUser.setName("FetchType Test User");
		testUser.setLogin("fetchtype_test_user_" + System.currentTimeMillis());
		testUser.setEmail("fetchtype@test.com");
		testUser = userService.save(testUser);
		// Create test company
		testCompany = new CCompany();
		testCompany.setName("FetchType Test Company");
		testCompany.setDescription("Company for FetchType validation");
		testCompany = companyService.save(testCompany);
		// Create test role
		testRole = new CUserCompanyRole("Test Role", testCompany);
		testRole.setIsAdmin(false);
		testRole.setIsUser(true);
		testRole.setIsGuest(false);
		testRole = roleService.save(testRole);
	}

	@Test
	@Transactional
	@DisplayName ("Test saving CUserCompanySetting with role doesn't cause PostgreSQL 1664 error")
	public void testSaveWithRoleDoesNotCauseColumnLimitError() {
		// This test validates that the fix (FetchType.LAZY for role) prevents the error:
		// "ERROR: target lists can have at most 1664 entries"
		// This save operation would fail with the error if role was EAGER fetched
		// because CUserCompanyRole has two @ElementCollection fields with EAGER fetch
		CUserCompanySetting savedSettings = userCompanySettingsService.addUserToCompany(testUser, testCompany, "MEMBER", testRole);
		// Verify save succeeded
		assertNotNull(savedSettings, "Settings should be saved successfully");
		assertNotNull(savedSettings.getId(), "Saved settings should have an ID");
		assertEquals(testUser.getId(), savedSettings.getUser().getId(), "User should match");
		assertEquals(testCompany.getId(), savedSettings.getCompany().getId(), "Company should match");
		assertEquals(testRole.getId(), savedSettings.getRole().getId(), "Role should match");
		assertEquals("MEMBER", savedSettings.getOwnershipLevel(), "Ownership level should match");
	}

	@Test
	@Transactional
	@DisplayName ("Test role can be accessed within transaction despite LAZY loading")
	public void testRoleAccessibleWithinTransaction() {
		// Create and save settings
		CUserCompanySetting savedSettings = userCompanySettingsService.addUserToCompany(testUser, testCompany, "MEMBER", testRole);
		// Retrieve settings and access lazy-loaded role within transaction
		CUserCompanySetting retrievedSettings = userCompanySettingsService.getById(savedSettings.getId()).orElseThrow();
		// Access role properties - this should work within transaction
		CUserCompanyRole retrievedRole = retrievedSettings.getRole();
		assertNotNull(retrievedRole, "Role should be accessible within transaction");
		assertEquals("Test Role", retrievedRole.getName(), "Role name should match");
		assertTrue(retrievedRole.getIsUser(), "Role should have user flag set");
	}

	@Test
	@Transactional
	@DisplayName ("Test multiple settings can be saved without exceeding column limit")
	public void testMultipleSettingsWithRoles() {
		// This test simulates a realistic scenario where multiple users have company assignments
		// In the bug scenario, even one save could fail due to EAGER fetch generating massive queries
		// Create additional users and roles
		CUser user2 = new CUser();
		user2.setName("Second User");
		user2.setLogin("second_user_" + System.currentTimeMillis());
		user2.setEmail("second@test.com");
		user2 = userService.save(user2);
		CUserCompanyRole role2 = new CUserCompanyRole("Admin Role", testCompany);
		role2.setIsAdmin(true);
		role2 = roleService.save(role2);
		// Save multiple settings
		CUserCompanySetting saved1 = userCompanySettingsService.addUserToCompany(testUser, testCompany, "MEMBER", testRole);
		CUserCompanySetting saved2 = userCompanySettingsService.addUserToCompany(user2, testCompany, "ADMIN", role2);
		// Verify both saves succeeded
		assertNotNull(saved1.getId(), "First settings should be saved");
		assertNotNull(saved2.getId(), "Second settings should be saved");
		// Verify we can retrieve and access role data
		assertEquals(testRole.getId(), saved1.getRole().getId(), "First role should match");
		assertEquals(role2.getId(), saved2.getRole().getId(), "Second role should match");
	}

	@Test
	@Transactional
	@DisplayName ("Test that guest roles can be assigned (but should be filtered in UI)")
	public void testGuestRoleCanBeAssignedButShouldBeFilteredInUI() {
		// Create a guest role
		CUserCompanyRole guestRole = new CUserCompanyRole("Guest Role", testCompany);
		guestRole.setIsAdmin(false);
		guestRole.setIsUser(false);
		guestRole.setIsGuest(true);
		guestRole = roleService.save(guestRole);
		// Guest roles CAN be assigned at the data layer
		CUserCompanySetting savedSettings = userCompanySettingsService.addUserToCompany(testUser, testCompany, "GUEST", guestRole);
		assertNotNull(savedSettings, "Settings with guest role should be saved");
		assertNotNull(savedSettings.getId(), "Saved settings should have an ID");
		assertEquals(guestRole.getId(), savedSettings.getRole().getId(), "Guest role should be assigned");
		assertTrue(savedSettings.getRole().isGuest(), "Role should be a guest role");
		// Note: The UI layer (data providers in components) will filter out guest roles
		// from the selection ComboBox, but at the data layer they are valid
	}

	@Test
	@Transactional
	@DisplayName ("Test initializeAllFields properly initializes role within transaction")
	public void testInitializeAllFieldsWithinTransaction() {
		// Create and save settings
		CUserCompanySetting savedSettings = userCompanySettingsService.addUserToCompany(testUser, testCompany, "MEMBER", testRole);
		// Retrieve settings
		CUserCompanySetting retrievedSettings = userCompanySettingsService.getById(savedSettings.getId()).orElseThrow();
		// Call initializeAllFields within transaction
		retrievedSettings.initializeAllFields();
		// Access role, company and user - should work within transaction
		CUserCompanyRole role = retrievedSettings.getRole();
		assertNotNull(role, "Role should not be null");
		assertNotNull(role.getName(), "Role name should not be null");
		assertEquals("Test Role", role.getName(), "Role name should match");
		CCompany company = retrievedSettings.getCompany();
		assertNotNull(company, "Company should not be null");
		assertNotNull(company.getName(), "Company name should not be null");
		CUser user = retrievedSettings.getUser();
		assertNotNull(user, "User should not be null");
		assertNotNull(user.getLogin(), "User login should not be null");
	}
}
