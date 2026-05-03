package tech.derbent.plm.tickets.ticketpriority.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.imports.service.CAbstractSimpleTypeImportHandler;
import tech.derbent.api.imports.service.CExcelRow;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.tickets.ticketpriority.domain.CTicketPriority;

/** Imports {@link CTicketPriority} rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "default"})
public class CTicketPriorityImportHandler extends CAbstractSimpleTypeImportHandler<CTicketPriority> {

	private final CTicketPriorityService ticketPriorityService;

	public CTicketPriorityImportHandler(final CTicketPriorityService ticketPriorityService) {
		this.ticketPriorityService = ticketPriorityService;
	}

	@Override
	public Class<CTicketPriority> getEntityClass() { return CTicketPriority.class; }

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.of(
				"Priority Level", "prioritylevel",
				"Is Default", "isdefault");
	}

	@Override
	protected Optional<CTicketPriority> findByNameAndCompany(final String name, final CCompany company) {
		return ticketPriorityService.findByNameAndCompany(name, company);
	}

	@Override
	protected CTicketPriority createNew(final String name, final CCompany company) {
		return new CTicketPriority(name, company);
	}

	@Override
	protected void save(final CTicketPriority entity) {
		ticketPriorityService.save(entity);
	}

	@Override
	protected void applyExtraFields(final CTicketPriority entity, final CExcelRow row, final CProject<?> project, final int rowNumber) {
		row.optionalInt("prioritylevel").ifPresent(entity::setPriorityLevel);
		row.optionalBoolean("isdefault").ifPresent(entity::setIsDefault);
	}
}
