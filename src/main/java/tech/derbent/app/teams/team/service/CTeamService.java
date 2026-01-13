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
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.app.teams.team.domain.CTeam;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:group", title = "Settings.Teams")
@PermitAll
@Transactional
public class CTeamService extends CEntityOfCompanyService<CTeam> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTeamService.class);

	public CTeamService(final IEntityOfCompanyRepository<CTeam> repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CTeam entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		// Add team-specific delete checks if needed
		return null;
	}

	@Transactional (readOnly = true)
	public List<CTeam> findByCompany(final CCompany company) {
		LOGGER.debug("Finding teams for company: {}", company != null ? company.getName() : "null");
		return ((ITeamRepository) repository).findByCompany(company);
	}

	@Transactional (readOnly = true)
	public List<CTeam> findByManager(final CUser manager) {
		LOGGER.debug("Finding teams managed by: {}", manager != null ? manager.getLogin() : "null");
		return ((ITeamRepository) repository).findByManager(manager);
	}

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
	}
}
