package tech.derbent.plm.agile.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.imports.service.CAbstractWorkflowTypeImportHandler;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.agile.domain.CUserStoryType;

/** Imports {@link CUserStoryType} rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "default"})
public class CUserStoryTypeImportHandler extends CAbstractWorkflowTypeImportHandler<CUserStoryType> {

	private final CUserStoryTypeService userStoryTypeService;

	public CUserStoryTypeImportHandler(final CUserStoryTypeService userStoryTypeService,
			final CWorkflowEntityService workflowEntityService) {
		super(workflowEntityService);
		this.userStoryTypeService = userStoryTypeService;
	}

	@Override
	public Class<CUserStoryType> getEntityClass() { return CUserStoryType.class; }

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		// WHY: header normalization turns "Non Deletable" into "nondeletable"; the underlying field is attributeNonDeletable.
		return Map.of(
				"Non Deletable", "attributenondeletable",
				"Attribute Non Deletable", "attributenondeletable");
	}

	@Override
	protected Optional<CUserStoryType> findByNameAndCompany(final String name, final CCompany company) {
		return userStoryTypeService.findByNameAndCompany(name, company);
	}

	@Override
	protected CUserStoryType createNew(final String name, final CCompany company) {
		return new CUserStoryType(name, company);
	}

	@Override
	protected void save(final CUserStoryType entity) {
		userStoryTypeService.save(entity);
	}
}
