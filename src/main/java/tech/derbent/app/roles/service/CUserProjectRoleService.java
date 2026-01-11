package tech.derbent.app.roles.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.utils.Check;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.roles.domain.CUserProjectRole;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.service.CPageServiceUserProjectRole;

/** CUserProjectRoleService - Service layer for CUserProjectRole entity. Layer: Service (MVC) Handles business logic for company-aware and
 * project-aware user project role operations. Uses super class methods where available to maintain simplicity. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CUserProjectRoleService extends CEntityOfCompanyService<CUserProjectRole> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserProjectRoleService.class);

	/** Constructor for CUserProjectRoleService.
	 * @param repository     the IUserProjectRoleRepository to use for data access
	 * @param clock          the Clock instance for time-related operations
	 * @param sessionService the session service for user context */
	public CUserProjectRoleService(final IUserProjectRoleRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CUserProjectRole entity) {
		return super.checkDeleteAllowed(entity);
	}

	/** Create a default admin role for a project.
	 * @param project the project
	 * @return the created admin role */
	@Transactional
	public CUserProjectRole createAdminRole(CProject project) {
		Check.notNull(project, "Project cannot be null");
		final CUserProjectRole adminRole = new CUserProjectRole("Project Admin", project);
		adminRole.setIsAdmin(true);
		adminRole.setIsUser(true);
		adminRole.setIsGuest(false);
		return save(adminRole);
	}

	/** Create a default guest role for a project.
	 * @param project the project
	 * @return the created guest role */
	@Transactional
	public CUserProjectRole createGuestRole(CProject project) {
		Check.notNull(project, "Project cannot be null");
		final CUserProjectRole guestRole = new CUserProjectRole("Project Guest", project);
		guestRole.setIsAdmin(false);
		guestRole.setIsUser(false);
		guestRole.setIsGuest(true);
		return save(guestRole);
	}

	/** Create a default user role for a project.
	 * @param project the project
	 * @return the created user role */
	@Transactional
	public CUserProjectRole createUserRole(CProject project) {
		Check.notNull(project, "Project cannot be null");
		final CUserProjectRole userRole = new CUserProjectRole("Project User", project);
		userRole.setIsAdmin(false);
		userRole.setIsUser(true);
		userRole.setIsGuest(false);
		return save(userRole);
	}
	
	/** Find all roles for a specific project.
	 * @param project the project
	 * @return list of roles for the project */
	@Transactional (readOnly = true)
	public List<CUserProjectRole> findByProject(CProject project) {
		Check.notNull(project, "Project cannot be null");
		return ((IUserProjectRoleRepository) repository).listByProjectForPageView(project);
	}

	@Override
	public Class<CUserProjectRole> getEntityClass() { return CUserProjectRole.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceUserProjectRole.class; }
	
	@Override
	public CUserProjectRole getRandom() {
		Check.fail("Use getRandom(CCompany) instead to ensure company context");
		return null;
	}
	
	public CUserProjectRole getRandom(CCompany company) {
		Check.notNull(company, "Company cannot be null");
		final List<CUserProjectRole> roles = listByCompany(company);
		if (roles.isEmpty()) {
			throw new IllegalStateException("No roles found for company: " + company.getName());
		}
		final int randomIndex = (int) (Math.random() * roles.size());
		return roles.get(randomIndex);
	}

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Initialize default roles for a project if they don't exist.
	 * @param project the project to initialize roles for */
	@Transactional
	public void initializeDefaultRoles(CProject project) {
		Check.notNull(project, "Project cannot be null");
		final List<CUserProjectRole> existingRoles = findByProject(project);
		if (existingRoles.isEmpty()) {
			createAdminRole(project);
			createUserRole(project);
			createGuestRole(project);
			LOGGER.info("Initialized default project roles for: {}", project.getName());
		}
	}

	@Override
	public void initializeNewEntity(final CUserProjectRole entity) {
		super.initializeNewEntity(entity);
		// Additional entity-specific initialization can be added here if needed
	}
	
	@Override
	@Transactional
	public CUserProjectRole newEntity(final String name) {
		final CCompany company = sessionService.getActiveCompany()
				.orElseThrow(() -> new IllegalStateException("No active company selected, cannot create role without company context"));
		final CProject project = sessionService.getActiveProject()
				.orElseThrow(() -> new IllegalStateException("No active project selected, cannot create project role without project context"));
		return new CUserProjectRole(name, project);
	}
}
