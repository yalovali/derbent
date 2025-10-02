package tech.derbent.api.roles.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.services.CAbstractNamedEntityService;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.session.service.CSessionService;

/** CUserCompanyRoleService - Service layer for CUserCompanyRole entity. Layer: Service (MVC) Handles business logic for company-aware user company
 * role operations. Uses super class methods where available to maintain simplicity. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CUserCompanyRoleService extends CAbstractNamedEntityService<CUserCompanyRole> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserCompanyRoleService.class);

	/** Constructor for CUserCompanyRoleService.
	 * @param repository     the IUserCompanyRoleRepository to use for data access
	 * @param clock          the Clock instance for time-related operations
	 * @param sessionService the session service for user context */
	public CUserCompanyRoleService(final IUserCompanyRoleRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Create a default admin role for a company.
	 * @param company the company
	 * @return the created admin role */
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

	/** Create a default guest role for a company.
	 * @param company the company
	 * @return the created guest role */
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

	/** Create a default user role for a company.
	 * @param company the company
	 * @return the created user role */
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

	/** Initialize default roles for a company if they don't exist.
	 * @param company the company to initialize roles for */
	@Transactional
	public void initializeDefaultRoles(CCompany company) {
		// Check if roles already exist for this company
		java.util.List<CUserCompanyRole> existingRoles = ((IUserCompanyRoleRepository) repository).findByCompany(company);
		if (existingRoles.isEmpty()) {
			createAdminRole(company);
			createUserRole(company);
			createGuestRole(company);
			LOGGER.info("Initialized default company roles for: {}", company.getName());
		}
	}
}
