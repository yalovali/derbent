package tech.derbent.app.roles.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.services.pageservice.implementations.CPageServiceUserProjectRole;
import tech.derbent.app.roles.domain.CUserProjectRole;
import tech.derbent.base.session.service.ISessionService;

/** CUserProjectRoleService - Service layer for CUserProjectRole entity. Layer: Service (MVC) Handles business logic for project-aware user project
 * role operations. Uses super class methods where available to maintain simplicity. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CUserProjectRoleService extends CEntityOfProjectService<CUserProjectRole> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserProjectRoleService.class);

	/** Constructor for CUserProjectRoleService.
	 * @param repository     the CUserProjectRoleRepository to use for data access
	 * @param clock          the Clock instance for time-related operations
	 * @param sessionService the session service for user context */
	public CUserProjectRoleService(final IUserProjectRoleRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Create a default admin role for a project.
	 * @param projectName the project name
	 * @return the created admin role */
	@Transactional
	public CUserProjectRole createAdminRole(String projectName) {
		CUserProjectRole adminRole = new CUserProjectRole("Project Admin",
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project in session")));
		adminRole.setIsAdmin(true);
		adminRole.setIsUser(true);
		adminRole.setIsGuest(false);
		return save(adminRole);
	}

	/** Create a default guest role for a project.
	 * @param projectName the project name
	 * @return the created guest role */
	@Transactional
	public CUserProjectRole createGuestRole(String projectName) {
		CUserProjectRole guestRole = new CUserProjectRole("Project Guest",
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project in session")));
		guestRole.setIsAdmin(false);
		guestRole.setIsUser(false);
		guestRole.setIsGuest(true);
		return save(guestRole);
	}

	/** Create a default user role for a project.
	 * @param projectName the project name
	 * @return the created user role */
	@Transactional
	public CUserProjectRole createUserRole(String projectName) {
		CUserProjectRole userRole = new CUserProjectRole("Project User",
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project in session")));
		userRole.setIsAdmin(false);
		userRole.setIsUser(true);
		userRole.setIsGuest(false);
		return save(userRole);
	}

	@Override
	public Class<CUserProjectRole> getEntityClass() { return CUserProjectRole.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CUserProjectRoleInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceUserProjectRole.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public String checkDeleteAllowed(final CUserProjectRole entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public void initializeNewEntity(final CUserProjectRole entity) {
		super.initializeNewEntity(entity);
		// Additional entity-specific initialization can be added here if needed
	}

	/** Initialize default roles for a project if they don't exist. */
	@Transactional
	public void initializeDefaultRoles() {
		if (count() == 0) {
			createAdminRole("Default Admin");
			createUserRole("Default User");
			createGuestRole("Default Guest");
			LOGGER.info("Initialized default project roles");
		}
	}
}
