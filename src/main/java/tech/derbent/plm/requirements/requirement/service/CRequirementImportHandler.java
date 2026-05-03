package tech.derbent.plm.requirements.requirement.service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.service.CProjectItemImportHandler;
import tech.derbent.api.imports.service.CExcelRow;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.requirements.requirement.domain.CRequirement;
import tech.derbent.plm.requirements.requirementtype.domain.CRequirementType;
import tech.derbent.plm.requirements.requirementtype.service.CRequirementTypeService;

/** Imports {@link CRequirement} rows from Excel into the active project. */
@Service
@Profile({"derbent", "bab", "default"})
public class CRequirementImportHandler extends CProjectItemImportHandler<CRequirement, CRequirementType> {

    private final CRequirementService requirementService;
    private final CRequirementTypeService typeService;

    public CRequirementImportHandler(final CRequirementService requirementService, final CRequirementTypeService typeService,
            final CProjectItemStatusService statusService, final IUserRepository userRepository) {
        super(statusService, userRepository);
        this.requirementService = requirementService;
        this.typeService = typeService;
    }

    @Override
    public Class<CRequirement> getEntityClass() { return CRequirement.class; }

    @Override
    protected Class<CRequirementType> getTypeClass() { return CRequirementType.class; }

    @Override
    public Set<String> getRequiredColumns() {
        // Requirement validation requires a type.
        return Set.of("name", "entitytype");
    }

    @Override
    protected boolean isTypeRequired() {
        return true;
    }

    @Override
    protected Optional<CRequirement> findByNameAndProject(final String name, final CProject<?> project) {
        return requirementService.findByNameAndProject(name, project);
    }

    @Override
    protected CRequirement createNew(final String name, final CProject<?> project) {
        return new CRequirement(name, project);
    }

    @Override
    protected void save(final CRequirement entity) {
        requirementService.save(entity);
    }

    @Override
    protected Optional<CRequirementType> findTypeByNameAndCompany(final String name, final CCompany company) {
        return typeService.findByNameAndCompany(name, company);
    }

    @Override
    protected void applyExtraFields(final CRequirement entity, final CExcelRow row, final CProject<?> project, final int rowNumber,
            final Map<String, String> rowData) {
        applyMetaFieldsDeclaredOn(entity, row, CRequirement.class);
    }
}
