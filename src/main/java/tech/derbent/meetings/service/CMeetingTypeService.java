package tech.derbent.meetings.service;

import java.time.Clock;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.meetings.domain.CMeetingType;

/**
 * CMeetingTypeService - Service layer for CMeetingType entity. Layer: Service (MVC) Handles business logic for
 * project-aware meeting type operations.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CMeetingTypeService extends CEntityOfProjectService<CMeetingType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingTypeService.class);

    /**
     * Constructor for CMeetingTypeService.
     * 
     * @param repository
     *            the CMeetingTypeRepository to use for data access
     * @param clock
     *            the Clock instance for time-related operations
     */
    CMeetingTypeService(final CMeetingTypeRepository repository, final Clock clock) {
        super(repository, clock);
    }

    /**
     * Gets a meeting type by ID with eagerly loaded relationships. Overrides parent get() method to prevent
     * LazyInitializationException.
     * 
     * @param id
     *            the meeting type ID
     * @return Optional containing the meeting type with loaded relationships
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CMeetingType> get(final Long id) {

        if (id == null) {
            LOGGER.debug("Getting CMeetingType with null ID - returning empty");
            return Optional.empty();
        }
        LOGGER.debug("Getting CMeetingType with ID {} (with eager loading)", id);
        final Optional<CMeetingType> entity = ((CMeetingTypeRepository) repository).findByIdWithRelationships(id);
        entity.ifPresent(this::initializeLazyFields);
        return entity;
    }

    @Override
    protected Class<CMeetingType> getEntityClass() {
        return CMeetingType.class;
    }
}