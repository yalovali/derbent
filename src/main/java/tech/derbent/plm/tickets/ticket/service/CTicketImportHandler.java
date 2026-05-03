package tech.derbent.plm.tickets.ticket.service;

import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.CAbstractExcelImportHandler;
import tech.derbent.api.imports.service.CExcelRow;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.tickets.ticket.domain.CTicket;
import tech.derbent.plm.tickets.ticketpriority.domain.CTicketPriority;
import tech.derbent.plm.tickets.ticketpriority.service.CTicketPriorityService;
import tech.derbent.plm.tickets.tickettype.domain.CTicketType;
import tech.derbent.plm.tickets.tickettype.service.CTicketTypeService;

/** Imports {@link CTicket} rows from Excel into the active project. */
@Service
@Profile({"derbent", "default"})
public class CTicketImportHandler extends CAbstractExcelImportHandler<CTicket> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTicketImportHandler.class);

	private final CTicketService ticketService;
	private final CTicketTypeService typeService;
	private final CTicketPriorityService priorityService;
	private final CProjectItemStatusService statusService;
	private final IUserRepository userRepository;

	public CTicketImportHandler(final CTicketService ticketService, final CTicketTypeService typeService,
			final CTicketPriorityService priorityService, final CProjectItemStatusService statusService,
			final IUserRepository userRepository) {
		this.ticketService = ticketService;
		this.typeService = typeService;
		this.priorityService = priorityService;
		this.statusService = statusService;
		this.userRepository = userRepository;
	}

	@Override
	public Class<CTicket> getEntityClass() { return CTicket.class; }

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.of("Type", "entitytype");
	}

	@Override
	public Set<String> getRequiredColumns() {
		return Set.of("name");
	}

	@Override
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final CExcelRow row = row(rowData);
		final String name = row.string("name");
		if (name.isBlank()) {
			return CImportRowResult.error(rowNumber, "Name is required", rowData);
		}
		if (project.getCompany() == null) {
			return CImportRowResult.error(rowNumber, "Project company is required", rowData);
		}

		// WHY: system_init.xlsx is imported automatically after DB reset and can also be imported manually.
		final CTicket ticket = ticketService.findByNameAndProject(name, project)
				.orElseGet(() -> new CTicket(name, project));

		row.optionalString("description").ifPresent(ticket::setDescription);
		row.optionalString("contextinformation").ifPresent(ticket::setContextInformation);
		row.optionalString("result").ifPresent(ticket::setResult);

		row.optionalLocalDate("duedate").ifPresent(ticket::setDueDate);
		row.optionalLocalDate("initialdate").ifPresent(ticket::setInitialDate);
		row.optionalLocalDate("planneddate").ifPresent(ticket::setPlannedDate);

		final String statusName = row.string("status");
		if (!statusName.isBlank()) {
			final CProjectItemStatus status = statusService.findByNameAndCompany(statusName, project.getCompany()).orElse(null);
			if (status == null) {
				return CImportRowResult.error(rowNumber,
						"Status '" + statusName + "' not found. Create it before importing.", rowData);
			}
			ticket.setStatus(status);
		}

		final String typeName = row.string("entitytype");
		if (!typeName.isBlank()) {
			final CTicketType type = typeService.findByNameAndCompany(typeName, project.getCompany()).orElse(null);
			if (type == null) {
				return CImportRowResult.error(rowNumber,
						"Ticket Type '" + typeName + "' not found. Create it before importing.", rowData);
			}
			ticket.setEntityType(type);
		}

		final String priorityName = row.string("priority");
		if (!priorityName.isBlank()) {
			final CTicketPriority priority = priorityService.findByNameAndCompany(priorityName, project.getCompany()).orElse(null);
			if (priority == null) {
				return CImportRowResult.error(rowNumber,
						"Ticket Priority '" + priorityName + "' not found. Create it before importing.", rowData);
			}
			ticket.setPriority(priority);
		}

		final String assignedToLogin = row.string("assignedto");
		if (!assignedToLogin.isBlank()) {
			final CUser user = userRepository.findByUsernameIgnoreCase(project.getCompany().getId(), assignedToLogin).orElse(null);
			if (user == null) {
				return CImportRowResult.error(rowNumber,
						"Assigned user '" + assignedToLogin + "' not found in company. Create it before importing.", rowData);
			}
			ticket.setAssignedTo(user);
		}

		if (!options.isDryRun()) {
			ticketService.save(ticket);
		}
		LOGGER.debug("Imported ticket '{}' (row {})", name, rowNumber);
		return CImportRowResult.success(rowNumber, name);
	}
}
