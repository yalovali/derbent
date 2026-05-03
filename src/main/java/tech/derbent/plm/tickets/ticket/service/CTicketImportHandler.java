package tech.derbent.plm.tickets.ticket.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.service.CExcelRow;
import tech.derbent.api.imports.service.CProjectItemImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.tickets.ticket.domain.CTicket;
import tech.derbent.plm.tickets.ticketpriority.domain.CTicketPriority;
import tech.derbent.plm.tickets.ticketpriority.service.CTicketPriorityService;
import tech.derbent.plm.tickets.tickettype.domain.CTicketType;
import tech.derbent.plm.tickets.tickettype.service.CTicketTypeService;

/** Imports {@link CTicket} rows from Excel into the active project. */
@Service
@Profile({"derbent", "default"})
public class CTicketImportHandler extends CProjectItemImportHandler<CTicket, CTicketType> {

	private final CTicketService ticketService;
	private final CTicketTypeService typeService;
	private final CTicketPriorityService priorityService;

	public CTicketImportHandler(final CTicketService ticketService, final CTicketTypeService typeService,
			final CTicketPriorityService priorityService, final CProjectItemStatusService statusService,
			final IUserRepository userRepository) {
		super(statusService, userRepository);
		this.ticketService = ticketService;
		this.typeService = typeService;
		this.priorityService = priorityService;
	}

	@Override
	public Class<CTicket> getEntityClass() { return CTicket.class; }

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.of("Type", "entitytype");
	}

	@Override
	protected Class<CTicketType> getTypeClass() { return CTicketType.class; }

	@Override
	protected Optional<CTicket> findByNameAndProject(final String name, final CProject<?> project) {
		return ticketService.findByNameAndProject(name, project);
	}

	@Override
	protected CTicket createNew(final String name, final CProject<?> project) {
		return new CTicket(name, project);
	}

	@Override
	protected void save(final CTicket entity) {
		ticketService.save(entity);
	}

	@Override
	protected Optional<CTicketType> findTypeByNameAndCompany(final String name, final CCompany company) {
		return typeService.findByNameAndCompany(name, company);
	}

	@Override
	protected void applyExtraFields(final CTicket entity, final CExcelRow row, final CProject<?> project, final int rowNumber,
			final Map<String, String> rowData) {
		applyMetaFieldsDeclaredOn(entity, row, CTicket.class);

		final String priorityName = row.string("priority");
		if (priorityName.isBlank()) {
			return;
		}
		final CTicketPriority priority = priorityService.findByNameAndCompany(priorityName, project.getCompany()).orElse(null);
		if (priority == null) {
			throw new IllegalArgumentException("Ticket Priority '" + priorityName + "' not found");
		}
		entity.setPriority(priority);
	}
}
