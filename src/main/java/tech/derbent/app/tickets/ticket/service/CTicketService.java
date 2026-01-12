package tech.derbent.app.tickets.ticket.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.tickets.ticket.domain.CTicket;
import tech.derbent.app.tickets.tickettype.service.CTicketTypeService;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.Tickets")
@PermitAll
public class CTicketService extends CProjectItemService<CTicket> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTicketService.class);
	private final CTicketTypeService ticketTypeService;

	CTicketService(final ITicketRepository repository, final Clock clock, final ISessionService sessionService,
			final CTicketTypeService ticketTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.ticketTypeService = ticketTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CTicket entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CTicket> getEntityClass() { return CTicket.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CTicketInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceTicket.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CTicket entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new ticket entity");
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize ticket"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, ticketTypeService, projectItemStatusService);
		LOGGER.debug("Ticket initialization complete");
	}
}
