package tech.derbent.plm.storage.storage.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.storage.storage.domain.CStorage;
import tech.derbent.plm.storage.storagetype.service.CStorageTypeService;

@Service
@PreAuthorize("isAuthenticated()")
@Menu(icon = "vaadin:warehouse", title = "Storage.Storage")
@PermitAll
public class CStorageService extends CProjectItemService<CStorage> implements IEntityRegistrable, IEntityWithView {

    private static final Logger LOGGER = LoggerFactory.getLogger(CStorageService.class);
    private final CStorageTypeService storageTypeService;

    public CStorageService(final IStorageRepository repository, final Clock clock, final ISessionService sessionService,
            final CStorageTypeService storageTypeService, final CProjectItemStatusService projectItemStatusService) {
        super(repository, clock, sessionService, projectItemStatusService);
        this.storageTypeService = storageTypeService;
    }

    @Override
    public Class<CStorage> getEntityClass() { return CStorage.class; }

    @Override
    public Class<?> getInitializerServiceClass() { return CStorageInitializerService.class; }

    @Override
    public Class<?> getPageServiceClass() { return CPageServiceStorage.class; }

    @Override
    public Class<?> getServiceClass() { return this.getClass(); }

    @Override
    public void initializeNewEntity(final CStorage entity) {
        super.initializeNewEntity(entity);
        LOGGER.debug("Initializing new storage entity");
        final CProject<?> currentProject = sessionService.getActiveProject()
                .orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize storage"));
        IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, storageTypeService, projectItemStatusService);
    }
}
