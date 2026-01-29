package tech.derbent.plm.teams.team.service;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.teams.team.domain.CTeam;

@Service
@PreAuthorize ("isAuthenticated()")
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
		return superCheck != null ? superCheck : null;
	}

	/**
	 * Copy CTeam-specific fields from source to target entity.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CTeam source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);

		if (!(target instanceof CTeam targetTeam)) {
			return;
		}

		// Conditional: relations
		if (options.includesRelations()) {
			targetTeam.setTeamManager(source.getTeamManager());
			if (source.getMembers() != null) {
				targetTeam.setMembers(new HashSet<>(source.getMembers()));
			}
		}

		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	@Transactional (readOnly = true)
	public List<CTeam> findByCompany(final CCompany company) {
		// LOGGER.debug("Finding teams for company: {}", company != null ? company.getName() : "null");
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
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final CTeam entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getCompany(), ValidationMessages.COMPANY_REQUIRED);
		// Unique name check - use base class helper
		validateUniqueNameInCompany((ITeamRepository) repository, entity, entity.getName(), entity.getCompany());
		
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getDescription(), "Description", 2000);
	}
}
