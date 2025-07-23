package tech.derbent.meetings.service;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.meetings.domain.CMeetingType;

/**
 * CMeetingTypeService - Service layer for CMeetingType entity.
 * Layer: Service (MVC)
 * Handles business logic for meeting type operations.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CMeetingTypeService extends CAbstractService<CMeetingType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingTypeService.class);

    /**
     * Constructor for CMeetingTypeService.
     * 
     * @param repository the CMeetingTypeRepository to use for data access
     * @param clock the Clock instance for time-related operations
     */
    CMeetingTypeService(final CMeetingTypeRepository repository, final Clock clock) {
        super(repository, clock);
        LOGGER.info("CMeetingTypeService initialized");
    }

    /**
     * Creates a new meeting type entity.
     * 
     * @param name the name of the meeting type
     * @param description the description of the meeting type
     */
    @Transactional
    public void createEntity(final String name, final String description) {
        LOGGER.info("Creating new meeting type: {}", name);
        final var entity = new CMeetingType(name, description);
        repository.saveAndFlush(entity);
    }
}