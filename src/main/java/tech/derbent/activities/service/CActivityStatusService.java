package tech.derbent.activities.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.activities.domain.CActivityStatus;

/**
 * CActivityStatusService - Service class for managing CActivityStatus entities. Layer: Service (MVC) Provides business
 * logic for activity status management including CRUD operations, validation, and workflow management.
 */
@Service
@Transactional
public class CActivityStatusService extends CEntityOfProjectService<CActivityStatus> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityStatusService.class);

    private final CActivityStatusRepository activityStatusRepository;

    @Autowired
    public CActivityStatusService(final CActivityStatusRepository activityStatusRepository, final Clock clock) {
        super(activityStatusRepository, clock);

        if (activityStatusRepository == null) {
            LOGGER.error("CActivityStatusService constructor - Repository parameter is null");
            throw new IllegalArgumentException("Activity status repository cannot be null");
        }
        this.activityStatusRepository = activityStatusRepository;
    }

    /**
     * Create default activity statuses if they don't exist. This method should be called during application startup.
     */
    public void createDefaultStatusesIfNotExist() {
        LOGGER.debug("createDefaultStatusesIfNotExist() - Creating default activity statuses");
        // TODO implement default statuses creation logic
    }

    /**
     * Delete an activity status by ID.
     * 
     * @param id
     *            the status ID - must not be null
     * @throws IllegalArgumentException
     *             if the ID is null
     */
    public void deleteById(final Long id) {
        LOGGER.debug("deleteById(id={}) - Deleting activity status", id);

        if (id == null) {
            LOGGER.error("deleteById(id=null) - ID parameter is null");
            throw new IllegalArgumentException("Activity status ID cannot be null");
        }
        final Optional<CActivityStatus> existing = activityStatusRepository.findById(id);

        if (!existing.isPresent()) {
            LOGGER.warn("deleteById(id={}) - Activity status not found", id);
            return;
        }

        try {
            activityStatusRepository.deleteById(id);
            LOGGER.debug("deleteById(id={}) - Successfully deleted activity status", id);
        } catch (final Exception e) {
            LOGGER.error("deleteById(id={}) - Error deleting activity status: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete activity status", e);
        }
    }

    /**
     * Check if an activity status name exists (case-insensitive).
     * 
     * @param name
     *            the name to check - must not be null
     * @return true if the name exists, false otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(final String name) {
        LOGGER.debug("existsByName(name={}) - Checking if activity status name exists", name);

        if ((name == null) || name.trim().isEmpty()) {
            LOGGER.warn("existsByName(name={}) - Name parameter is null or empty", name);
            return false;
        }
        final boolean exists = activityStatusRepository.existsByNameIgnoreCase(name.trim());
        LOGGER.debug("existsByName(name={}) - Name exists: {}", name, exists);
        return exists;
    }

    /**
     * Find all activity statuses ordered by sort order.
     * 
     * @return List of all activity statuses
     */
    @Transactional(readOnly = true)
    public List<CActivityStatus> findAll() {
        LOGGER.debug("findAll() - Finding all activity statuses");
        final List<CActivityStatus> statuses = activityStatusRepository.findAllOrderedBySortOrder();
        LOGGER.debug("findAll() - Found {} activity statuses", statuses.size());
        return statuses;
    }

    /**
     * Find all active (non-final) statuses.
     * 
     * @return List of active statuses
     */
    @Transactional(readOnly = true)
    public List<CActivityStatus> findAllActiveStatuses() {
        LOGGER.debug("findAllActiveStatuses() - Finding all active activity statuses");
        final List<CActivityStatus> statuses = activityStatusRepository.findAllActiveStatuses();
        LOGGER.debug("findAllActiveStatuses() - Found {} active statuses", statuses.size());
        return statuses;
    }

    /**
     * Find all final statuses (completed/cancelled states).
     * 
     * @return List of final statuses
     */
    @Transactional(readOnly = true)
    public List<CActivityStatus> findAllFinalStatuses() {
        LOGGER.debug("findAllFinalStatuses() - Finding all final activity statuses");
        final List<CActivityStatus> statuses = activityStatusRepository.findAllFinalStatuses();
        LOGGER.debug("findAllFinalStatuses() - Found {} final statuses", statuses.size());
        return statuses;
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
        final Optional<CActivityStatus> status = activityStatusRepository.findById(id);
        LOGGER.debug("findById(id={}) - Found status: {}", id, status.isPresent());
        return status;
    }

    /**
     * Find activity status by name (case-insensitive).
     * 
     * @param name
     *            the status name - must not be null or empty
     * @return Optional containing the status if found, empty otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CActivityStatus> findByName(final String name) {
        LOGGER.debug("findByName(name={}) - Finding activity status by name", name);

        if ((name == null) || name.trim().isEmpty()) {
            LOGGER.warn("findByName(name={}) - Name parameter is null or empty", name);
            return Optional.empty();
        }
        final Optional<CActivityStatus> status = activityStatusRepository.findByNameIgnoreCase(name.trim());
        LOGGER.debug("findByName(name={}) - Found status: {}", name, status.isPresent());
        return status;
    }

    /**
     * Find the default status for new activities.
     * 
     * @return Optional containing the default status if found
     */
    @Transactional(readOnly = true)
    public Optional<CActivityStatus> findDefaultStatus() {
        LOGGER.debug("findDefaultStatus() - Finding default activity status");
        final Optional<CActivityStatus> status = activityStatusRepository.findDefaultStatus();
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
        final Optional<CActivityStatus> entity = activityStatusRepository.findByIdWithProject(id);
        entity.ifPresent(this::initializeLazyFields);
        return entity;
    }

    @Override
    protected Class<CActivityStatus> getEntityClass() {
        return CActivityStatus.class;
    }

    /**
     * Save or update an activity status.
     * 
     * @param status
     *            the status to save - must not be null
     * @return the saved status
     * @throws IllegalArgumentException
     *             if the status is null or invalid
     */
    @Override
    public CActivityStatus save(final CActivityStatus status) {
        LOGGER.debug("save(status={}) - Saving activity status", status != null ? status.getName() : "null");

        if (status == null) {
            LOGGER.error("save(activityStatus=null) - Activity status parameter is null");
            throw new IllegalArgumentException("Activity status cannot be null");
        }

        if ((status.getName() == null) || status.getName().trim().isEmpty()) {
            LOGGER.error("save() - Activity status name is null or empty for status id={}", status.getId());
            throw new IllegalArgumentException("Activity status name cannot be null or empty");
        }
        // Check for duplicate names (excluding self for updates)
        final String trimmedName = status.getName().trim();
        // search with same name and same project
        final Optional<CActivityStatus> existing = activityStatusRepository.findByNameAndProject(trimmedName,
                status.getProject());

        if (existing.isPresent()) {
            LOGGER.error("save() - Activity status name '{}' already exists", trimmedName);
            throw new IllegalArgumentException("Activity status name '" + trimmedName + "' already exists");
        }

        try {
            final CActivityStatus savedStatus = activityStatusRepository.save(status);
            return savedStatus;
        } catch (final Exception e) {
            LOGGER.error("save() - Error saving activity status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save activity status", e);
        }
    }
}