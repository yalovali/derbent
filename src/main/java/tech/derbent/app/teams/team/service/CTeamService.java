package tech.derbent.app.teams.team.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.service.CEntityNamedService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.teams.team.domain.CTeam;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/**
 * CTeamService - Service layer for CTeam entity
 * Handles business logic for team management operations
 */
@Service
@PreAuthorize ("isAuthenticated()")
@Menu (order = 0, icon = "vaadin:group", title = "Settings.Teams")
@PermitAll
@Transactional
public class CTeamService extends CEntityNamedService<CTeam> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTeamService.class);

	public CTeamService(final ITeamRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CTeam entity) {
		return super.checkDeleteAllowed(entity);
	}

	/**
	 * Find all teams for a specific company
	 */
	@Transactional (readOnly = true)
	public List<CTeam> findByCompany(final CCompany company) {
		LOGGER.debug("Finding teams for company: {}", company != null ? company.getName() : "null");
		return ((ITeamRepository) repository).findByCompany(company);
	}

	/**
	 * Find teams managed by a specific user
	 */
	@Transactional (readOnly = true)
	public List<CTeam> findByManager(final CUser manager) {
		LOGGER.debug("Finding teams managed by: {}", manager != null ? manager.getLogin() : "null");
		return ((ITeamRepository) repository).findByManager(manager);
	}

	/**
	 * Find teams that include a specific user as a member
	 */
	@Transactional (readOnly = true)
	public List<CTeam> findByMember(final CUser user) {
		LOGGER.debug("Finding teams for member: {}", user != null ? user.getLogin() : "null");
		return ((ITeamRepository) repository).findByMember(user);
	}

	@Override
	public Class<CTeam> getEntityClass() { return CTeam.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CTeamInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceTeam.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CTeam entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new team entity");
		// Set default company from session if available
		sessionService.getActiveCompany().ifPresent(entity::setCompany);
	}
}
