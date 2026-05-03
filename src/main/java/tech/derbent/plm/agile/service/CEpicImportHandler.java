package tech.derbent.plm.agile.service;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.service.CAgileEntityImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CEpicType;

/** Imports {@link CEpic} rows from Excel into the active project. */
@Service
@Profile({"derbent", "bab", "default"})
public class CEpicImportHandler extends CAgileEntityImportHandler<CEpic, CEpicType> {

    private final CEpicService epicService;
    private final CEpicTypeService typeService;

    public CEpicImportHandler(final CEpicService epicService, final CEpicTypeService typeService,
            final CProjectItemStatusService statusService, final IUserRepository userRepository) {
        super(statusService, userRepository);
        this.epicService = epicService;
        this.typeService = typeService;
    }

    @Override
    public Class<CEpic> getEntityClass() { return CEpic.class; }

    @Override
    protected Class<CEpicType> getTypeClass() { return CEpicType.class; }

    @Override
    protected Optional<CEpic> findByNameAndProject(final String name, final CProject<?> project) {
        return epicService.findByNameAndProject(name, project);
    }

    @Override
    protected CEpic createNew(final String name, final CProject<?> project) {
        return new CEpic(name, project);
    }

    @Override
    protected void save(final CEpic entity) {
        epicService.save(entity);
    }

    @Override
    protected Optional<CEpicType> findTypeByNameAndCompany(final String name, final CCompany company) {
        return typeService.findByNameAndCompany(name, company);
    }
}
