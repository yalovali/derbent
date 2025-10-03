package tech.derbent.users.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
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

/** Integration test to validate that the lazy loading fix for CUserCompanySetting prevents LazyInitializationException when accessing company data
 * outside of a transaction. This test ensures: 1. The repository query eagerly fetches company data 2. The service method properly initializes lazy
 * fields 3. UI components can safely access company data without LazyInitializationException */
@SpringBootTest (classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"
})
@DisplayName ("🔍 CUserCompanySetting Lazy Loading Fix Validation Test")
public class CUserCompanySettingLazyLoadingTest {

	@Autowired
	private CCompanyService companyService;
	private CCompany testCompany;
	private CUser testUser;
	private CUserCompanyRole testRole;
	@Autowired
	private CUserCompanyRoleService userCompanyRoleService;
	@Autowired
	private CUserCompanySettingsService userCompanySettingsService;
	@Autowired
	private CUserService userService;

	@BeforeEach
	@Transactional
	public void setup() {
		// Create and save test company
		testCompany = new CCompany();
		testCompany.setName("Test Company for Lazy Loading");
		testCompany.setDescription("Company for testing lazy loading fix");
		testCompany = companyService.save(testCompany);
		// Create and save test user with unique login
		testUser = new CUser();
		testUser.setName("Test User");
		testUser.setLogin("testuser_lazy_" + System.currentTimeMillis());
		testUser.setEmail("testuser_lazy_" + System.currentTimeMillis() + "@example.com");
		testUser = userService.save(testUser);
		// Create test role
		testRole = new CUserCompanyRole("Developer", testCompany);
		testRole.setIsAdmin(false);
		testRole.setIsUser(true);
		testRole = userCompanyRoleService.save(testRole);
		// Create and save user company setting
		CUserCompanySetting setting = userCompanySettingsService.addUserToCompany(testUser, testCompany, "MEMBER", testRole);
		testUser.setCompanySettings(setting);
		testUser = userService.save(testUser);
	}

	@Test
	@DisplayName ("Test company can be accessed outside transaction using findByIdWithCompanySetting")
	public void testCompanyAccessibleOutsideTransaction() {
		// Fetch user with company setting eagerly loaded (simulating UI access)
		Optional<CUser> userOpt = userService.findByIdWithCompanySetting(testUser.getId());
		// Verify user was found
		assertTrue(userOpt.isPresent(), "User should be found");
		CUser user = userOpt.get();
		// Verify company setting exists
		assertNotNull(user.getCompanySettings(), "Company setting should not be null");
		CUserCompanySetting setting = user.getCompanySettings();
		// Access company outside transaction - should NOT throw LazyInitializationException
		CCompany company = setting.getCompany();
		assertNotNull(company, "Company should not be null");
		assertNotNull(company.getName(), "Company name should not be null");
		assertEquals("Test Company for Lazy Loading", company.getName(), "Company name should match");
		// Access role outside transaction - should NOT throw LazyInitializationException
		CUserCompanyRole role = setting.getRole();
		assertNotNull(role, "Role should not be null");
		assertNotNull(role.getName(), "Role name should not be null");
		assertEquals("Developer", role.getName(), "Role name should match");
	}

	@Test
	@Transactional
	@DisplayName ("Test initializeAllFields properly initializes company within transaction")
	public void testInitializeAllFieldsWithinTransaction() {
		// Fetch user within transaction
		Optional<CUser> userOpt = userService.getById(testUser.getId());
		assertTrue(userOpt.isPresent(), "User should be found");
		CUser user = userOpt.get();
		// Get company setting
		CUserCompanySetting setting = user.getCompanySettings();
		assertNotNull(setting, "Company setting should not be null");
		// Call initializeAllFields within transaction
		setting.initializeAllFields();
		// Access company and user - should work within transaction
		CCompany company = setting.getCompany();
		assertNotNull(company, "Company should not be null");
		assertNotNull(company.getName(), "Company name should not be null");
		assertEquals("Test Company for Lazy Loading", company.getName(), "Company name should match");
		CUser settingUser = setting.getUser();
		assertNotNull(settingUser, "User should not be null");
		assertNotNull(settingUser.getLogin(), "User login should not be null");
		// Access role - should work within transaction
		CUserCompanyRole role = setting.getRole();
		assertNotNull(role, "Role should not be null");
		assertNotNull(role.getName(), "Role name should not be null");
		assertEquals("Developer", role.getName(), "Role name should match");
	}

	@Test
	@DisplayName ("Test repository query eagerly fetches company data")
	public void testRepositoryEagerlyFetchesCompany() {
		// Fetch user using the new repository method
		Optional<CUser> userOpt = userService.findByIdWithCompanySetting(testUser.getId());
		assertTrue(userOpt.isPresent(), "User should be found");
		CUser user = userOpt.get();
		CUserCompanySetting setting = user.getCompanySettings();
		assertNotNull(setting, "Company setting should not be null");
		// Access company outside transaction (no @Transactional on this test method)
		// This should work because the repository eagerly fetched the company
		CCompany company = setting.getCompany();
		assertNotNull(company, "Company should be eagerly loaded");
		String companyName = company.getName();
		assertNotNull(companyName, "Company name should be accessible");
		assertEquals("Test Company for Lazy Loading", companyName);
		// Access role outside transaction - should work because eagerly fetched
		CUserCompanyRole role = setting.getRole();
		assertNotNull(role, "Role should be eagerly loaded");
		String roleName = role.getName();
		assertNotNull(roleName, "Role name should be accessible");
		assertEquals("Developer", roleName);
	}
}
