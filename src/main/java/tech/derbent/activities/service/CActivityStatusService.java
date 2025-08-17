package tech.derbent.activities.service;

import java.time.Clock;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CActivityStatusService - Service class for managing CActivityStatus entities. Layer: Service (MVC) Provides business
 * logic for activity status management including CRUD operations, validation, and workflow management.
 */
@Service
@Transactional
public class CActivityStatusService extends CEntityOfProjectService<CActivityStatus> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityStatusService.class);

    @Autowired
    public CActivityStatusService(final CActivityStatusRepository activityStatusRepository, final Clock clock) {
        super(activityStatusRepository, clock);

        if (activityStatusRepository == null) {
            LOGGER.error("CActivityStatusService constructor - Repository parameter is null");
            throw new IllegalArgumentException("Activity status repository cannot be null");
        }
    }

    /**
     * Create default activity statuses if they don't exist. This method should be called during application startup.
     */
    public void createDefaultStatusesIfNotExist() {
        LOGGER.debug("createDefaultStatusesIfNotExist() - Creating default activity statuses");
        // TODO implement default statuses creation logic
    }

    /**
     * Find activity status by ID.
     * 
     * @param id
     *            the status ID - must not be null
     * @return Optional containing the status if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<CActivityStatus> findById(final Long id) {
        LOGGER.debug("findById(id={}) - Finding activity status by ID", id);

        if (id == null) {
            LOGGER.warn("findById(id=null) - ID parameter is null");
            return Optional.empty();
        }
        final Optional<CActivityStatus> status = repository.findById(id);
        LOGGER.debug("findById(id={}) - Found status: {}", id, status.isPresent());
        return status;
    }

    /**
     * Find the default status for new activities.
     * 
     * @return Optional containing the default status if found
     */
    @Transactional(readOnly = true)
    public Optional<CActivityStatus> findDefaultStatus(final CProject project) {
        LOGGER.debug("findDefaultStatus() - Finding default activity status");
        final Optional<CActivityStatus> status = ((CActivityStatusService) repository).findDefaultStatus(project);
        LOGGER.debug("findDefaultStatus() - Found default status: {}", status.isPresent());
        return status;
    }

    /**
     * Override get() method to eagerly load project relationship and prevent LazyInitializationException.
     * 
     * @param id
     *            the activity status ID
     * @return optional CActivityStatus with all relationships loaded
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CActivityStatus> getById(final Long id) {
        LOGGER.debug("get called with id: {} (overridden to eagerly load project)", id);

        if (id == null) {
            return Optional.empty();
        }
        final Optional<CActivityStatus> entity = repository.findById(id);
        entity.ifPresent(this::initializeLazyFields);
        return entity;
    }

    @Override
    protected Class<CActivityStatus> getEntityClass() {
        return CActivityStatus.class;
    }
}