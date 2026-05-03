package tech.derbent.plm.activities.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.imports.service.CAbstractWorkflowTypeImportHandler;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.activities.domain.CActivityType;

/** Imports {@link CActivityType} rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "default"})
public class CActivityTypeImportHandler extends CAbstractWorkflowTypeImportHandler<CActivityType> {

    private final CActivityTypeService activityTypeService;

    public CActivityTypeImportHandler(final CActivityTypeService activityTypeService,
            final CWorkflowEntityService workflowEntityService) {
        super(workflowEntityService);
        this.activityTypeService = activityTypeService;
    }

    @Override
    public Class<CActivityType> getEntityClass() { return CActivityType.class; }

    @Override
    protected Map<String, String> getAdditionalColumnAliases() {
        // WHY: header normalization turns "Non Deletable" into "nondeletable"; the underlying field is attributeNonDeletable.
        return Map.of(
                "Non Deletable", "attributenondeletable",
                "Attribute Non Deletable", "attributenondeletable");
    }

    @Override
    protected Optional<CActivityType> findByNameAndCompany(final String name, final CCompany company) {
        return activityTypeService.findByNameAndCompany(name, company);
    }

    @Override
    protected CActivityType createNew(final String name, final CCompany company) {
        return new CActivityType(name, company);
    }

    @Override
    protected void save(final CActivityType entity) {
        activityTypeService.save(entity);
    }
}

