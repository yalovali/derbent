package tech.derbent.plm.tickets.tickettype.service;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.imports.service.CAbstractWorkflowTypeImportHandler;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.tickets.tickettype.domain.CTicketType;

/** Imports {@link CTicketType} rows from Excel (company-scoped reference data). */
@Service
@Profile ({"derbent", "default"})
public class CTicketTypeImportHandler extends CAbstractWorkflowTypeImportHandler<CTicketType> {

	private final CTicketTypeService ticketTypeService;

	public CTicketTypeImportHandler(final CTicketTypeService ticketTypeService,
			final CWorkflowEntityService workflowEntityService) {
		super(workflowEntityService);
		this.ticketTypeService = ticketTypeService;
	}

	@Override
	public Class<CTicketType> getEntityClass() { return CTicketType.class; }

	@Override
	protected Optional<CTicketType> findByNameAndCompany(final String name, final CCompany company) {
		return ticketTypeService.findByNameAndCompany(name, company);
	}

	@Override
	protected CTicketType createNew(final String name, final CCompany company) {
		return new CTicketType(name, company);
	}

	@Override
	protected void save(final CTicketType entity) {
		ticketTypeService.save(entity);
	}
}
