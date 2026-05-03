package tech.derbent.plm.requirements.requirementtype.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.imports.service.CAbstractWorkflowTypeImportHandler;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.requirements.requirementtype.domain.CRequirementType;

/** Imports {@link CRequirementType} rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "bab", "default"})
public class CRequirementTypeImportHandler extends CAbstractWorkflowTypeImportHandler<CRequirementType> {

    private final CRequirementTypeService requirementTypeService;

    public CRequirementTypeImportHandler(final CRequirementTypeService requirementTypeService,
            final CWorkflowEntityService workflowEntityService) {
        super(workflowEntityService);
        this.requirementTypeService = requirementTypeService;
    }

    @Override
    public Class<CRequirementType> getEntityClass() { return CRequirementType.class; }

    @Override
    protected Map<String, String> getAdditionalColumnAliases() {
        return Map.of(
                "Non Deletable", "attributenondeletable",
                "Attribute Non Deletable", "attributenondeletable");
    }

    @Override
    protected Optional<CRequirementType> findByNameAndCompany(final String name, final CCompany company) {
        return requirementTypeService.findByNameAndCompany(name, company);
    }

    @Override
    protected CRequirementType createNew(final String name, final CCompany company) {
        return new CRequirementType(name, company);
    }

    @Override
    protected void save(final CRequirementType entity) {
        requirementTypeService.save(entity);
    }
}
