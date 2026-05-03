package tech.derbent.plm.decisions.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.imports.service.CAbstractWorkflowTypeImportHandler;
import tech.derbent.api.imports.service.CExcelRow;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.decisions.domain.CDecisionType;

/** Imports {@link CDecisionType} rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "default"})
public class CDecisionTypeImportHandler extends CAbstractWorkflowTypeImportHandler<CDecisionType> {

	private final CDecisionTypeService decisionTypeService;

	public CDecisionTypeImportHandler(final CDecisionTypeService decisionTypeService,
			final CWorkflowEntityService workflowEntityService) {
		super(workflowEntityService);
		this.decisionTypeService = decisionTypeService;
	}

	@Override
	public Class<CDecisionType> getEntityClass() { return CDecisionType.class; }

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		// WHY: header normalization turns "Non Deletable" into "nondeletable"; the underlying field is attributeNonDeletable.
		return Map.of(
				"Non Deletable", "attributenondeletable",
				"Attribute Non Deletable", "attributenondeletable");
	}

	@Override
	protected Optional<CDecisionType> findByNameAndCompany(final String name, final CCompany company) {
		return decisionTypeService.findByNameAndCompany(name, company);
	}

	@Override
	protected CDecisionType createNew(final String name, final CCompany company) {
		return new CDecisionType(name, company);
	}

	@Override
	protected void save(final CDecisionType entity) {
		decisionTypeService.save(entity);
	}

	@Override
	protected void applyExtraFields(final CDecisionType entity, final CExcelRow row, final CProject<?> project, final int rowNumber) {
		row.optionalBoolean("requiresapproval").ifPresent(entity::setRequiresApproval);
	}
}

