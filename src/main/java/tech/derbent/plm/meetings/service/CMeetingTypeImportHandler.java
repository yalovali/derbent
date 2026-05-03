package tech.derbent.plm.meetings.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.imports.service.CAbstractWorkflowTypeImportHandler;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.meetings.domain.CMeetingType;

/** Imports {@link CMeetingType} rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "default"})
public class CMeetingTypeImportHandler extends CAbstractWorkflowTypeImportHandler<CMeetingType> {

	private final CMeetingTypeService meetingTypeService;

	public CMeetingTypeImportHandler(final CMeetingTypeService meetingTypeService,
			final CWorkflowEntityService workflowEntityService) {
		super(workflowEntityService);
		this.meetingTypeService = meetingTypeService;
	}

	@Override
	public Class<CMeetingType> getEntityClass() { return CMeetingType.class; }

	@Override
	public Map<String, String> getColumnAliases() {
		// WHY: header normalization turns "Non Deletable" into "nondeletable"; the underlying field is attributeNonDeletable.
		return Map.of(
				"Non Deletable", "attributenondeletable",
				"Attribute Non Deletable", "attributenondeletable");
	}

	@Override
	protected Optional<CMeetingType> findByNameAndCompany(final String name, final CCompany company) {
		return meetingTypeService.findByNameAndCompany(name, company);
	}

	@Override
	protected CMeetingType createNew(final String name, final CCompany company) {
		return new CMeetingType(name, company);
	}

	@Override
	protected void save(final CMeetingType entity) {
		meetingTypeService.save(entity);
	}
}

