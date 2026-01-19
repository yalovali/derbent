package tech.derbent.plm.storage.storage.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.storage.storage.domain.CStorage;
import tech.derbent.plm.storage.storagetype.service.CStorageTypeService;

@Service
@PreAuthorize("isAuthenticated()")
@Menu(icon = "vaadin:storage", title = "PLM.Storage")
@PermitAll
public class CStorageService extends CProjectItemService<CStorage> implements IEntityRegistrable, IEntityWithView {

    private static final Logger LOGGER = LoggerFactory.getLogger(CStorageService.class);
    private final CStorageTypeService storageTypeService;
    private final tech.derbent.plm.storage.storageitem.service.IStorageItemRepository storageItemRepository;

    CStorageService(
            final IStorageRepository repository,
            final Clock clock,
            final ISessionService sessionService,
            final CStorageTypeService storageTypeService,
            final CProjectItemStatusService projectItemStatusService,
            final tech.derbent.plm.storage.storageitem.service.IStorageItemRepository storageItemRepository) {
        super(repository, clock, sessionService, projectItemStatusService);
        this.storageTypeService = storageTypeService;
        this.storageItemRepository = storageItemRepository;
    }

    @Override
    public String checkDeleteAllowed(final CStorage entity) {
        final String superCheck = super.checkDeleteAllowed(entity);
        if (superCheck != null) {
            return superCheck;
        }

        // Check if storage has child locations
        final IStorageRepository storageRepo = (IStorageRepository) repository;
        final long childCount = storageRepo.findByParent(entity).size();
        if (childCount > 0) {
            return String.format("Cannot delete. This storage location has %d child location%s.", childCount,
                    childCount == 1 ? "" : "s");
        }

        // Check if storage has storage items
        final long itemCount = storageItemRepository.findByStorage(entity).size();
        if (itemCount > 0) {
            return String.format("Cannot delete. This storage location contains %d storage item%s.", itemCount,
                    itemCount == 1 ? "" : "s");
        }

        return null;
    }

    @Override
    public Class<CStorage> getEntityClass() {
        return CStorage.class;
    }

    @Override
    public Class<?> getInitializerServiceClass() {
        return CStorageInitializerService.class;
    }

    @Override
    public Class<?> getPageServiceClass() {
        return CPageServiceStorage.class;
    }

    @Override
    public Class<?> getServiceClass() {
        return this.getClass();
    }

    @SuppressWarnings("null")
    @Override
    public void initializeNewEntity(final CStorage entity) {
        super.initializeNewEntity(entity);
        LOGGER.debug("Initializing new storage location entity");
        final CProject<?> currentProject = sessionService.getActiveProject()
                .orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize storage"));
        IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, storageTypeService,
                projectItemStatusService);
        LOGGER.debug("Storage location initialization complete");
    }

    @Override
    @Transactional
    protected void validateEntity(final CStorage entity) throws CValidationException {
        super.validateEntity(entity);

        // Validate name uniqueness within project
        final Optional<CStorage> existing = ((IStorageRepository) repository).findByNameAndProject(entity.getName(),
                entity.getProject());
        if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
            throw new CValidationException("A storage location with this name already exists in the project");
        }

        // Validate capacity
        if (entity.getCapacity() != null && entity.getCapacity().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new CValidationException("Capacity cannot be negative");
        }

        // Validate current utilization
        if (entity.getCurrentUtilization() != null
                && entity.getCurrentUtilization().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new CValidationException("Current utilization cannot be negative");
        }

        // Validate utilization doesn't exceed capacity
        if (entity.getCapacity() != null && entity.getCurrentUtilization() != null) {
            if (entity.getCurrentUtilization().compareTo(entity.getCapacity()) > 0) {
                throw new CValidationException("Current utilization cannot exceed capacity");
            }
        }

        // Prevent circular parent relationships
        if (entity.getParentStorage() != null) {
            CStorage parent = entity.getParentStorage();
            while (parent != null) {
                if (parent.getId() != null && parent.getId().equals(entity.getId())) {
                    throw new CValidationException("Circular parent relationship detected");
                }
                parent = parent.getParentStorage();
            }
        }
    }
}
