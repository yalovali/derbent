package tech.derbent.plm.agile.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.imports.service.CAbstractWorkflowTypeImportHandler;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.agile.domain.CEpicType;

/** Imports {@link CEpicType} rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "default"})
public class CEpicTypeImportHandler extends CAbstractWorkflowTypeImportHandler<CEpicType> {

    private final CEpicTypeService epicTypeService;

    public CEpicTypeImportHandler(final CEpicTypeService epicTypeService,
            final CWorkflowEntityService workflowEntityService) {
        super(workflowEntityService);
        this.epicTypeService = epicTypeService;
    }

    @Override
    public Class<CEpicType> getEntityClass() { return CEpicType.class; }

    @Override
    protected Map<String, String> getAdditionalColumnAliases() {
        return Map.of(
                "Non Deletable", "attributenondeletable",
                "Attribute Non Deletable", "attributenondeletable");
    }

    @Override
    protected Optional<CEpicType> findByNameAndCompany(final String name, final CCompany company) {
        return epicTypeService.findByNameAndCompany(name, company);
    }

    @Override
    protected CEpicType createNew(final String name, final CCompany company) {
        return new CEpicType(name, company);
    }

    @Override
    protected void save(final CEpicType entity) {
        epicTypeService.save(entity);
    }
}
