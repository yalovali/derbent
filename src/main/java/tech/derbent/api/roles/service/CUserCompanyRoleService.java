package tech.derbent.api.roles.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.services.CAbstractNamedEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.session.service.CSessionService;

/** CUserCompanyRoleService - Service layer for CUserCompanyRole entity. Layer: Service (MVC) Handles business logic for company-aware user company
 * role operations. Uses super class methods where available to maintain simplicity. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CUserCompanyRoleService extends CAbstractNamedEntityService<CUserCompanyRole> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserCompanyRoleService.class);

	public CUserCompanyRoleService(final IUserCompanyRoleRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Transactional
	public CUserCompanyRole createAdminRole(CCompany company) {
		CUserCompanyRole adminRole = new CUserCompanyRole("Company Admin", company);
		adminRole.setIsAdmin(true);
		adminRole.setIsUser(true);
		adminRole.setIsGuest(false);
		// Add common admin pages
		adminRole.addWriteAccess("CompanySettings");
		adminRole.addWriteAccess("UserManagement");
		adminRole.addWriteAccess("CompanyReports");
		adminRole.addReadAccess("Dashboard");
		return save(adminRole);
	}

	@Transactional
	public CUserCompanyRole createGuestRole(CCompany company) {
		CUserCompanyRole guestRole = new CUserCompanyRole("Company Guest", company);
		guestRole.setIsAdmin(false);
		guestRole.setIsUser(false);
		guestRole.setIsGuest(true);
		// Add basic guest pages
		guestRole.addReadAccess("Dashboard");
		guestRole.addReadAccess("PublicInfo");
		return save(guestRole);
	}

	@Transactional
	public CUserCompanyRole createUserRole(CCompany company) {
		CUserCompanyRole userRole = new CUserCompanyRole("Company User", company);
		userRole.setIsAdmin(false);
		userRole.setIsUser(true);
		userRole.setIsGuest(false);
		// Add common user pages
		userRole.addReadAccess("Dashboard");
		userRole.addReadAccess("Tasks");
		userRole.addWriteAccess("Profile");
		return save(userRole);
	}

	@Override
	protected Class<CUserCompanyRole> getEntityClass() { return CUserCompanyRole.class; }

	@Transactional
	public void initializeDefaultRoles(CCompany company) {
		// Check if roles already exist for this company
		List<CUserCompanyRole> existingRoles = ((IUserCompanyRoleRepository) repository).findByCompany(company);
		if (existingRoles.isEmpty()) {
			createAdminRole(company);
			createUserRole(company);
			createGuestRole(company);
			LOGGER.info("Initialized default company roles for: {}", company.getName());
		}
	}

	@Transactional (readOnly = true)
	public List<CUserCompanyRole> findByCompany(CCompany selectedCompany) {
		Check.notNull(selectedCompany, "Company cannot be null");
		return ((IUserCompanyRoleRepository) repository).findByCompany(selectedCompany);
	}
}
