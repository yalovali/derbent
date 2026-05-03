package tech.derbent.plm.sprints.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.imports.service.CAbstractWorkflowTypeImportHandler;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.sprints.domain.CSprintType;

/** Imports {@link CSprintType} rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "default"})
public class CSprintTypeImportHandler extends CAbstractWorkflowTypeImportHandler<CSprintType> {

	private final CSprintTypeService sprintTypeService;

	public CSprintTypeImportHandler(final CSprintTypeService sprintTypeService,
			final CWorkflowEntityService workflowEntityService) {
		super(workflowEntityService);
		this.sprintTypeService = sprintTypeService;
	}

	@Override
	public Class<CSprintType> getEntityClass() { return CSprintType.class; }

	@Override
	public Map<String, String> getColumnAliases() {
		// WHY: header normalization turns "Non Deletable" into "nondeletable"; the underlying field is attributeNonDeletable.
		return Map.of(
				"Non Deletable", "attributenondeletable",
				"Attribute Non Deletable", "attributenondeletable");
	}

	@Override
	protected Optional<CSprintType> findByNameAndCompany(final String name, final CCompany company) {
		return sprintTypeService.findByNameAndCompany(name, company);
	}

	@Override
	protected CSprintType createNew(final String name, final CCompany company) {
		return new CSprintType(name, company);
	}

	@Override
	protected void save(final CSprintType entity) {
		sprintTypeService.save(entity);
	}
}
