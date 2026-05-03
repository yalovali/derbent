package tech.derbent.plm.milestones.milestone.service;

import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.service.CProjectItemImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.milestones.milestone.domain.CMilestone;
import tech.derbent.plm.milestones.milestonetype.domain.CMilestoneType;
import tech.derbent.plm.milestones.milestonetype.service.CMilestoneTypeService;

/** Imports {@link CMilestone} rows from Excel into the active project. */
@Service
@Profile({"derbent", "bab", "default"})
public class CMilestoneImportHandler extends CProjectItemImportHandler<CMilestone, CMilestoneType> {

    private final CMilestoneService milestoneService;
    private final CMilestoneTypeService typeService;

    public CMilestoneImportHandler(final CMilestoneService milestoneService, final CMilestoneTypeService typeService,
            final CProjectItemStatusService statusService, final IUserRepository userRepository) {
        super(statusService, userRepository);
        this.milestoneService = milestoneService;
        this.typeService = typeService;
    }

    @Override
    public Class<CMilestone> getEntityClass() { return CMilestone.class; }

    @Override
    protected Class<CMilestoneType> getTypeClass() { return CMilestoneType.class; }

    @Override
    public Set<String> getRequiredColumns() {
        return Set.of("name", "entitytype");
    }

    @Override
    protected boolean isTypeRequired() {
        return true;
    }

    @Override
    protected Optional<CMilestone> findByNameAndProject(final String name, final CProject<?> project) {
        return milestoneService.findByNameAndProject(name, project);
    }

    @Override
    protected CMilestone createNew(final String name, final CProject<?> project) {
        return new CMilestone(name, project);
    }

    @Override
    protected void save(final CMilestone entity) {
        milestoneService.save(entity);
    }

    @Override
    protected Optional<CMilestoneType> findTypeByNameAndCompany(final String name, final CCompany company) {
        return typeService.findByNameAndCompany(name, company);
    }
}
