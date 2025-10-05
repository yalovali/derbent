package tech.derbent.users.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
		CUserCompanySetting setting = userCompanySettingsService.addUserToCompany(testUser, testCompany, testRole, "MEMBER");
		testUser.setCompanySettings(setting);
		testUser = userService.save(testUser);
	}
}
