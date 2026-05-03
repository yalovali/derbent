package tech.derbent.plm.agile.service;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.service.CAbstractAgileItemImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CFeatureType;

/** Imports {@link CFeature} rows from Excel into the active project. */
@Service
@Profile({"derbent", "bab", "default"})
public class CFeatureImportHandler extends CAbstractAgileItemImportHandler<CFeature, CFeatureType> {

    private final CFeatureService featureService;
    private final CFeatureTypeService typeService;

    public CFeatureImportHandler(final CFeatureService featureService, final CFeatureTypeService typeService,
            final CProjectItemStatusService statusService, final IUserRepository userRepository) {
        super(statusService, userRepository);
        this.featureService = featureService;
        this.typeService = typeService;
    }

    @Override
    public Class<CFeature> getEntityClass() { return CFeature.class; }

    @Override
    protected Class<CFeatureType> getTypeClass() { return CFeatureType.class; }

    @Override
    protected Optional<CFeature> findByNameAndProject(final String name, final CProject<?> project) {
        return featureService.findByNameAndProject(name, project);
    }

    @Override
    protected CFeature createNew(final String name, final CProject<?> project) {
        return new CFeature(name, project);
    }

    @Override
    protected void save(final CFeature entity) {
        featureService.save(entity);
    }

    @Override
    protected Optional<CFeatureType> findTypeByNameAndCompany(final String name, final CCompany company) {
        return typeService.findByNameAndCompany(name, company);
    }
}
