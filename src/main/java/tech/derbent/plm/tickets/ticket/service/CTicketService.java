package tech.derbent.plm.tickets.ticket.service;

import java.math.BigDecimal;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.tickets.ticket.domain.CTicket;
import tech.derbent.plm.tickets.ticketpriority.domain.CTicketPriority;
import tech.derbent.plm.tickets.ticketpriority.service.CTicketPriorityService;
import tech.derbent.plm.tickets.tickettype.service.CTicketTypeService;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CTicketService extends CProjectItemService<CTicket> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTicketService.class);
	private final CTicketPriorityService ticketPriorityService;
	private final CTicketTypeService typeService;

	CTicketService(final ITicketRepository repository, final Clock clock, final ISessionService sessionService,
			final CTicketTypeService ticketTypeService, final CProjectItemStatusService statusService,
			final CTicketPriorityService ticketPriorityService) {
		super(repository, clock, sessionService, statusService);
		typeService = ticketTypeService;
		this.ticketPriorityService = ticketPriorityService;
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
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
		// Initialize priority (Contextual DB Lookup)
		final java.util.List<CTicketPriority> priorities = ticketPriorityService.listByCompany(sessionService.getActiveCompany().orElseThrow());
		if (!priorities.isEmpty()) {
			((CTicket) entity).setPriority(priorities.get(0));
		} else {
			LOGGER.warn("No ticket priorities found for company {}", sessionService.getActiveCompany().orElseThrow().getName());
		}
	}

	@Override
	protected void validateEntity(final CTicket entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Ticket type is required");
		Check.notNull(entity.getPriority(), "Priority is required");
		
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getExternalReference(), "External Reference", 255);
		validateStringLength(entity.getContextInformation(), "Context Information", 2000);
		validateStringLength(entity.getResult(), "Result", 2000);
		
		// 3. Unique Checks
		validateUniqueNameInProject((ITicketRepository) repository, entity, entity.getName(), entity.getProject());
		
		// 4. Numeric Checks
		validateNumericField(entity.getWorkHoursEstimated(), "Work Hours Estimated", new BigDecimal("9999.99"));
		validateNumericField(entity.getWorkHoursReal(), "Work Hours Real", new BigDecimal("9999.99"));
		validateNumericField(entity.getWorkHoursLeft(), "Work Hours Left", new BigDecimal("9999.99"));
	}
}
