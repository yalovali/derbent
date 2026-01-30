package tech.derbent.plm.tickets.ticketpriority.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.tickets.ticketpriority.domain.CTicketPriority;

@Profile("derbent")
@Service
@Transactional
public class CTicketPriorityService extends CTypeEntityService<CTicketPriority> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CTicketPriorityService.class);

	public CTicketPriorityService(final ITicketPriorityRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks dependencies before allowing ticket priority deletion. Always calls super.checkDeleteAllowed() first to ensure all parent-level checks
	 * (null validation, non-deletable flag) are performed.
	 * @param entity the ticket priority entity to check
	 * @return null if priority can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CTicketPriority entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Transactional (readOnly = true)
	public Optional<CTicketPriority> findDefaultPriority(final CCompany company) {
		return ((ITicketPriorityRepository) repository).findByIsDefaultTrue(company);
	}

	@Override
	public Class<CTicketPriority> getEntityClass() { return CTicketPriority.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CTicketPriorityInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceTicketPriority.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final CTicketPriority entity) throws CValidationException {
		super.validateEntity(entity);
		// Unique Name Check - use base class helper
		validateUniqueNameInCompany((ITicketPriorityRepository) repository, entity, entity.getName(), entity.getCompany());
	}
}
