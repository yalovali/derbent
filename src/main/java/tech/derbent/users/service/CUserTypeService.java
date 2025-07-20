package tech.derbent.users.service;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.users.domain.CUserType;

/**
 * CUserTypeService - Service layer for CUserType entity.
 * Layer: Service (MVC)
 * Handles business logic for user type operations.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CUserTypeService extends CAbstractService<CUserType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CUserTypeService.class);

    /**
     * Constructor for CUserTypeService.
     * @param repository the CUserTypeRepository to use for data access
     * @param clock the Clock instance for time-related operations
     */
    CUserTypeService(final CUserTypeRepository repository, final Clock clock) {
        super(repository, clock);
        LOGGER.info("CUserTypeService initialized");
    }

    /**
     * Creates a new user type entity.
     * @param name the name of the user type
     * @param description the description of the user type
     */
    @Transactional
    public void createEntity(final String name, final String description) {
        LOGGER.info("Creating new user type: {}", name);
        final var entity = new CUserType(name, description);
        repository.saveAndFlush(entity);
    }
}