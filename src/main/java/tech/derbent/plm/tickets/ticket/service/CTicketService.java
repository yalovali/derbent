package tech.derbent.plm.tickets.ticket.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityConstants;
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

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.Tickets")
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
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		if (entity.getExternalReference() != null && entity.getExternalReference().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("External Reference cannot exceed %d characters", 255));
		}
		if (entity.getContextInformation() != null && entity.getContextInformation().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Context Information cannot exceed %d characters", 2000));
		}
		if (entity.getResult() != null && entity.getResult().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Result cannot exceed %d characters", 2000));
		}
		// 3. Unique Checks
		final Optional<CTicket> existingName = ((ITicketRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
		// 4. Numeric Checks
		validateNumericField(entity.getWorkHoursEstimated(), "Work Hours Estimated", new BigDecimal("9999.99"));
		validateNumericField(entity.getWorkHoursReal(), "Work Hours Real", new BigDecimal("9999.99"));
		validateNumericField(entity.getWorkHoursLeft(), "Work Hours Left", new BigDecimal("9999.99"));
	}

	private void validateNumericField(BigDecimal value, String fieldName, BigDecimal max) {
		if (value != null) {
			if (value.compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException(fieldName + " must be positive");
			}
			if (value.compareTo(max) > 0) {
				throw new IllegalArgumentException(fieldName + " cannot exceed " + max);
			}
		}
	}
}
