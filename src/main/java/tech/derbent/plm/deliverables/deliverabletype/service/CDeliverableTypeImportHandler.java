package tech.derbent.plm.deliverables.deliverabletype.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.imports.service.CAbstractWorkflowTypeImportHandler;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.deliverables.deliverabletype.domain.CDeliverableType;

/** Imports {@link CDeliverableType} rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "bab", "default"})
public class CDeliverableTypeImportHandler extends CAbstractWorkflowTypeImportHandler<CDeliverableType> {

    private final CDeliverableTypeService deliverableTypeService;

    public CDeliverableTypeImportHandler(final CDeliverableTypeService deliverableTypeService,
            final CWorkflowEntityService workflowEntityService) {
        super(workflowEntityService);
        this.deliverableTypeService = deliverableTypeService;
    }

    @Override
    public Class<CDeliverableType> getEntityClass() { return CDeliverableType.class; }

    @Override
    protected Map<String, String> getAdditionalColumnAliases() {
        return Map.of(
                "Non Deletable", "attributenondeletable",
                "Attribute Non Deletable", "attributenondeletable");
    }

    @Override
    protected Optional<CDeliverableType> findByNameAndCompany(final String name, final CCompany company) {
        return deliverableTypeService.findByNameAndCompany(name, company);
    }

    @Override
    protected CDeliverableType createNew(final String name, final CCompany company) {
        return new CDeliverableType(name, company);
    }

    @Override
    protected void save(final CDeliverableType entity) {
        deliverableTypeService.save(entity);
    }
}
