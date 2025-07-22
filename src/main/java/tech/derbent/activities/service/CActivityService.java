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
     * Gets paginated activities by project.
     */
    public Page<CActivity> listByProject(final CProject project, final Pageable pageable) {
        return ((CActivityRepository) repository).findByProject(project, pageable);
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
     * Overrides the base get method to eagerly load CActivityType relationship. This prevents
     * LazyInitializationException when the entity is used in UI contexts.
     * 
     * @param id
     *            the activity ID
     * @return optional CActivity with loaded activityType
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CActivity> get(final Long id) {
        LOGGER.debug("Getting CActivity with ID {} (overridden to eagerly load activityType)", id);
        final Optional<CActivity> entity = ((CActivityRepository) repository).findByIdWithActivityType(id);
        // Initialize lazy fields if entity is present (for any other potential lazy relationships)
        entity.ifPresent(this::initializeLazyFields);
        return entity;
    }

    /**
     * Initializes lazy fields for CActivity entity to prevent LazyInitializationException. Specifically handles the
     * lazy-loaded CActivityType relationship.
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
        } catch (final Exception e) {
            LOGGER.warn("Error initializing lazy fields for CActivity with ID: {}", entity.getId(), e);
        }
    }
}
