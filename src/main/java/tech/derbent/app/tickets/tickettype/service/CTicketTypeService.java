package tech.derbent.app.tickets.tickettype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.tickets.ticket.service.ITicketRepository;
import tech.derbent.app.tickets.tickettype.domain.CTicketType;
import tech.derbent.base.session.service.ISessionService;

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
	public void initializeNewEntity(final CTicketType entity) {
		super.initializeNewEntity(entity);
		final CProject activeProject = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project in session"));
		final long typeCount = ((ITicketTypeRepository) repository).countByProject(activeProject);
		final String autoName = String.format("TicketType %02d", typeCount + 1);
		entity.setName(autoName);
	}
}
