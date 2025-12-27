package tech.derbent.app.roles.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.utils.Check;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.companies.service.CPageServiceUserCompanyRole;
import tech.derbent.app.roles.domain.CUserCompanyRole;
import tech.derbent.base.session.service.ISessionService;

/** CUserCompanyRoleService - Service layer for CUserCompanyRole entity. Layer: Service (MVC) Handles business logic for company-aware user company
 * role operations. Uses super class methods where available to maintain simplicity. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CUserCompanyRoleService extends CNonProjectTypeService<CUserCompanyRole> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserCompanyRoleService.class);

	public CUserCompanyRoleService(final IUserCompanyRoleRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CUserCompanyRole entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Transactional
	public CUserCompanyRole createAdminRole(CCompany company) {
		final CUserCompanyRole adminRole = new CUserCompanyRole("Company Admin", company);
		adminRole.setIsAdmin(true);
		adminRole.setIsUser(true);
		adminRole.setIsGuest(false);
		return save(adminRole);
	}

	@Transactional
	public CUserCompanyRole createGuestRole(CCompany company) {
		final CUserCompanyRole guestRole = new CUserCompanyRole("Company Guest", company);
		guestRole.setIsAdmin(false);
		guestRole.setIsUser(false);
		guestRole.setIsGuest(true);
		return save(guestRole);
	}

	@Transactional
	public CUserCompanyRole createUserRole(CCompany company) {
		final CUserCompanyRole userRole = new CUserCompanyRole("Company User", company);
		userRole.setIsAdmin(false);
		userRole.setIsUser(true);
		userRole.setIsGuest(false);
		return save(userRole);
	}

	@Transactional (readOnly = true)
	public List<CUserCompanyRole> findByCompany(CCompany selectedCompany) {
		Check.notNull(selectedCompany, "Company cannot be null");
		return ((IUserCompanyRoleRepository) repository).findByCompany(selectedCompany);
	}

	@Override
	public Class<CUserCompanyRole> getEntityClass() { return CUserCompanyRole.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceUserCompanyRole.class; }

	@Override
	public CUserCompanyRole getRandom() {
		Check.fail("Use getRandom(CCompany) instead to ensure company context");
		return null;
	}

	public CUserCompanyRole getRandom(CCompany company) {
		final List<CUserCompanyRole> roles = ((IUserCompanyRoleRepository) repository).findByCompany(company);
		if (roles.isEmpty()) {
			throw new IllegalStateException("No roles found for company: " + company.getName());
		}
		final int randomIndex = (int) (Math.random() * roles.size());
		return roles.get(randomIndex);
	}

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Transactional
	public void initializeDefaultRoles(CCompany company) {
		// Check if roles already exist for this company
		final List<CUserCompanyRole> existingRoles = ((IUserCompanyRoleRepository) repository).findByCompany(company);
		if (existingRoles.isEmpty()) {
			createAdminRole(company);
			createUserRole(company);
			createGuestRole(company);
			LOGGER.info("Initialized default company roles for: {}", company.getName());
		}
	}

	@Override
	public void initializeNewEntity(final CUserCompanyRole entity) {
		super.initializeNewEntity(entity);
		// Additional entity-specific initialization can be added here if needed
	}

	@Override
	@Transactional (readOnly = true)
	public Page<CUserCompanyRole> list(final Pageable pageable) {
		final CCompany company = sessionService.getActiveCompany()
				.orElseThrow(() -> new IllegalStateException("No active company selected, cannot list entities without company context"));
		return ((IUserCompanyRoleRepository) repository).listByCompany(company, pageable);
	}
}
