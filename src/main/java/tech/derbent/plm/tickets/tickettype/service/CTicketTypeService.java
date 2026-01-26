package tech.derbent.plm.tickets.tickettype.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.tickets.ticket.service.ITicketRepository;
import tech.derbent.plm.tickets.tickettype.domain.CTicketType;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CTicketTypeService extends CTypeEntityService<CTicketType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTicketTypeService.class);
	@Autowired
	private final ITicketRepository ticketRepository;

	public CTicketTypeService(final ITicketTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final ITicketRepository ticketRepository) {
		super(repository, clock, sessionService);
		this.ticketRepository = ticketRepository;
	}

	@Override
	public String checkDeleteAllowed(final CTicketType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = ticketRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d item%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for ticket type: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CTicketType> getEntityClass() { return CTicketType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CTicketTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceTicketType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		if (entity instanceof final CEntityNamed entityCasted && entityCasted.getName() == null) {
			final CCompany activeCompany =
					sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
			final long typeCount = ((ITicketTypeRepository) repository).countByCompany(activeCompany);
			final String autoName = String.format("TicketType %02d", typeCount + 1);
			((CEntityNamed<?>) entity).setName(autoName);
		}
	}

	@Override
	protected void validateEntity(final CTicketType entity) {
		super.validateEntity(entity);
		// Unique Name Check
		final Optional<CTicketType> existing = ((ITicketTypeRepository) repository).findByNameAndCompany(entity.getName(), entity.getCompany());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
		}
	}
}
