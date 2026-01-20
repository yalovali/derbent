package tech.derbent.plm.storage.storagetype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.storage.storage.service.IStorageRepository;
import tech.derbent.plm.storage.storagetype.domain.CStorageType;

import java.util.Optional;
import tech.derbent.api.validation.ValidationMessages;

@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CStorageTypeService extends CTypeEntityService<CStorageType> implements IEntityRegistrable, IEntityWithView {

    private static final Logger LOGGER = LoggerFactory.getLogger(CStorageTypeService.class);
    private final IStorageRepository storageRepository;

    public CStorageTypeService(final IStorageTypeRepository repository, final Clock clock, final ISessionService sessionService,
            final IStorageRepository storageRepository) {
        super(repository, clock, sessionService);
        this.storageRepository = storageRepository;
    }

    @Override
    public String checkDeleteAllowed(final CStorageType entity) {
        final String superCheck = super.checkDeleteAllowed(entity);
        if (superCheck != null) {
            return superCheck;
        }
        try {
            final long usageCount = storageRepository.countByType(entity);
            if (usageCount > 0) {
                return String.format("Cannot delete. It is being used by %d item%s.", usageCount, usageCount == 1 ? "" : "s");
            }
            return null;
        } catch (final Exception e) {
            LOGGER.error("Error checking dependencies for storage type: {}", entity.getName(), e);
            return "Error checking dependencies: " + e.getMessage();
        }
    }

    @Override
    protected void validateEntity(final CStorageType entity) {
        super.validateEntity(entity);
        // Unique Name Check
        final Optional<CStorageType> existing = ((IStorageTypeRepository) repository).findByNameAndCompany(entity.getName(), entity.getCompany());
        if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
            throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
        }
    }

    @Override
    public Class<CStorageType> getEntityClass() { return CStorageType.class; }

    @Override
    public Class<?> getServiceClass() { return getClass(); }

    @Override
    public Class<?> getInitializerServiceClass() { return CStorageTypeInitializerService.class; }

    @Override
    public Class<?> getPageServiceClass() { return CPageServiceStorageType.class; }

    @Override
    public void initializeNewEntity(final CStorageType entity) {
        super.initializeNewEntity(entity);
        final CCompany activeCompany = sessionService.getActiveCompany()
                .orElseThrow(() -> new IllegalStateException("No active company in session"));
        final long typeCount = ((IStorageTypeRepository) repository).countByCompany(activeCompany);
        final String autoName = String.format("StorageType %02d", typeCount + 1);
        entity.setName(autoName);
    }
}
