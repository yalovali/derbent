package tech.derbent.activities.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;

@Service
@PreAuthorize("isAuthenticated()")
public class CActivityService extends CAbstractService<CActivity> {

    CActivityService(final CActivityRepository repository, final Clock clock) {
        super(repository, clock);
    }

    @Transactional
    public void createEntity(final String name) {
        if ("fail".equals(name)) {
            throw new RuntimeException("This is for testing the error handler");
        }
        final var entity = new CActivity();
        entity.setName(name);
        repository.saveAndFlush(entity);
    }

    /**
     * Finds activities by project.
     */
    public List<CActivity> findByProject(final CProject project) {
        return ((CActivityRepository) repository).findByProject(project);
    }

    /**
     * Gets paginated activities by project with eagerly loaded relationships.
     */
    public Page<CActivity> listByProject(final CProject project, final Pageable pageable) {
        LOGGER.debug("Getting paginated activities for project {} with eager loading", project.getName());
        return ((CActivityRepository) repository).findByProjectWithTypeAndStatus(project, pageable);
    }

    /**
     * Gets an activity by ID with eagerly loaded CActivityType relationship. This method should be used in UI contexts
     * to prevent LazyInitializationException.
     * 
     * @param id
     *            the activity ID
     * @return optional CActivity with loaded activityType
     */
    @Transactional(readOnly = true)
    public Optional<CActivity> getWithActivityType(final Long id) {
        LOGGER.debug("Getting CActivity with ID {} and eagerly loading CActivityType", id);
        return ((CActivityRepository) repository).findByIdWithActivityType(id);
    }

    /**
     * Gets an activity by ID with eagerly loaded CActivityType and CActivityStatus relationships. 
     * This method should be used in UI contexts to prevent LazyInitializationException.
     * 
     * @param id
     *            the activity ID
     * @return optional CActivity with loaded activityType and activityStatus
     */
    @Transactional(readOnly = true)
    public Optional<CActivity> getWithActivityTypeAndStatus(final Long id) {
        LOGGER.debug("Getting CActivity with ID {} and eagerly loading CActivityType and CActivityStatus", id);
        return ((CActivityRepository) repository).findByIdWithActivityTypeAndStatus(id);
    }

    /**
     * Overrides the base get method to eagerly load CActivityType and CActivityStatus relationships. 
     * This prevents LazyInitializationException when the entity is used in UI contexts.
     * 
     * @param id
     *            the activity ID
     * @return optional CActivity with loaded activityType and activityStatus
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CActivity> get(final Long id) {
        LOGGER.debug("Getting CActivity with ID {} (overridden to eagerly load activityType and activityStatus)", id);
        final Optional<CActivity> entity = ((CActivityRepository) repository).findByIdWithActivityTypeAndStatus(id);
        // Initialize lazy fields if entity is present (for any other potential lazy relationships)
        entity.ifPresent(this::initializeLazyFields);
        return entity;
    }

    /**
     * Initializes lazy fields for CActivity entity to prevent LazyInitializationException. 
     * Specifically handles the lazy-loaded CActivityType and CActivityStatus relationships.
     * 
     * @param entity
     *            the CActivity entity to initialize
     */
    @Override
    protected void initializeLazyFields(final CActivity entity) {
        if (entity == null) {
            return;
        }

        LOGGER.debug("Initializing lazy fields for CActivity with ID: {}", entity.getId());

        try {
            // Initialize the entity itself first
            super.initializeLazyFields(entity);

            // Initialize the lazy-loaded CActivityType relationship
            initializeLazyRelationship(entity.getActivityType(), "CActivityType");
            
            // Initialize the lazy-loaded CActivityStatus relationship
            initializeLazyRelationship(entity.getActivityStatus(), "CActivityStatus");
        } catch (final Exception e) {
            LOGGER.warn("Error initializing lazy fields for CActivity with ID: {}", entity.getId(), e);
        }
    }

    /**
     * Counts the number of activities for a specific project.
     * 
     * @param project the project
     * @return count of activities for the project
     */
    public long countByProject(final CProject project) {
        LOGGER.info("Counting activities for project: {}", project.getName());
        return ((CActivityRepository) repository).countByProject(project);
    }
}
