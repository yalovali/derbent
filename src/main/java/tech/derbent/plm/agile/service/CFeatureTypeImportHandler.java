package tech.derbent.plm.agile.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.imports.service.CAbstractWorkflowTypeImportHandler;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.agile.domain.CFeatureType;

/** Imports {@link CFeatureType} rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "default"})
public class CFeatureTypeImportHandler extends CAbstractWorkflowTypeImportHandler<CFeatureType> {

    private final CFeatureTypeService featureTypeService;

    public CFeatureTypeImportHandler(final CFeatureTypeService featureTypeService,
            final CWorkflowEntityService workflowEntityService) {
        super(workflowEntityService);
        this.featureTypeService = featureTypeService;
    }

    @Override
    public Class<CFeatureType> getEntityClass() { return CFeatureType.class; }

    @Override
    protected Map<String, String> getAdditionalColumnAliases() {
        return Map.of(
                "Non Deletable", "attributenondeletable",
                "Attribute Non Deletable", "attributenondeletable");
    }

    @Override
    protected Optional<CFeatureType> findByNameAndCompany(final String name, final CCompany company) {
        return featureTypeService.findByNameAndCompany(name, company);
    }

    @Override
    protected CFeatureType createNew(final String name, final CCompany company) {
        return new CFeatureType(name, company);
    }

    @Override
    protected void save(final CFeatureType entity) {
        featureTypeService.save(entity);
    }
}
