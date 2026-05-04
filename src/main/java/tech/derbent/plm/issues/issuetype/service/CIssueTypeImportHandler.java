package tech.derbent.plm.issues.issuetype.service;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.imports.service.CAbstractWorkflowTypeImportHandler;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.issues.issuetype.domain.CIssueType;

/** Imports {@link CIssueType} rows from Excel (company-scoped reference data). */
@Service
@Profile ({"derbent", "default"})
public class CIssueTypeImportHandler extends CAbstractWorkflowTypeImportHandler<CIssueType> {

	private final CIssueTypeService issueTypeService;

	public CIssueTypeImportHandler(final CIssueTypeService issueTypeService,
			final CWorkflowEntityService workflowEntityService) {
		super(workflowEntityService);
		this.issueTypeService = issueTypeService;
	}

	@Override
	public Class<CIssueType> getEntityClass() { return CIssueType.class; }

	@Override
	protected Optional<CIssueType> findByNameAndCompany(final String name, final CCompany company) {
		return issueTypeService.findByNameAndCompany(name, company);
	}

	@Override
	protected CIssueType createNew(final String name, final CCompany company) {
		return new CIssueType(name, company);
	}

	@Override
	protected void save(final CIssueType entity) {
		issueTypeService.save(entity);
	}
}

