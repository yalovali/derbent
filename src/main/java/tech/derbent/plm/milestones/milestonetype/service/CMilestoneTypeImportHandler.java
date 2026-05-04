package tech.derbent.plm.milestones.milestonetype.service;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.imports.service.CAbstractWorkflowTypeImportHandler;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.milestones.milestonetype.domain.CMilestoneType;

/** Imports {@link CMilestoneType} rows from Excel (company-scoped reference data). */
@Service
@Profile ({"derbent", "bab", "default"})
public class CMilestoneTypeImportHandler extends CAbstractWorkflowTypeImportHandler<CMilestoneType> {

	private final CMilestoneTypeService milestoneTypeService;

	public CMilestoneTypeImportHandler(final CMilestoneTypeService milestoneTypeService,
			final CWorkflowEntityService workflowEntityService) {
		super(workflowEntityService);
		this.milestoneTypeService = milestoneTypeService;
	}

	@Override
	public Class<CMilestoneType> getEntityClass() { return CMilestoneType.class; }

	@Override
	protected Optional<CMilestoneType> findByNameAndCompany(final String name, final CCompany company) {
		return milestoneTypeService.findByNameAndCompany(name, company);
	}

	@Override
	protected CMilestoneType createNew(final String name, final CCompany company) {
		return new CMilestoneType(name, company);
	}

	@Override
	protected void save(final CMilestoneType entity) {
		milestoneTypeService.save(entity);
	}
}
