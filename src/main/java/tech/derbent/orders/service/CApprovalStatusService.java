package tech.derbent.orders.service;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.orders.domain.CApprovalStatus;

/**
 * CApprovalStatusService - Service layer for CApprovalStatus entity.
 * Layer: Service (MVC)
 * 
 * Handles business logic for approval status operations including creation,
 * validation, and management of approval status entities.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CApprovalStatusService extends CAbstractNamedEntityService<CApprovalStatus> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CApprovalStatusService.class);

    /**
     * Constructor for CApprovalStatusService.
     * 
     * @param repository the CApprovalStatusRepository to use for data access
     * @param clock the Clock instance for time-related operations
     */
    CApprovalStatusService(final CApprovalStatusRepository repository, final Clock clock) {
        super(repository, clock);
    }

    /**
     * Creates a new approval status entity with name and description.
     * 
     * @param name the name of the approval status
     * @param description the description of the approval status
     */
    @Transactional
    public void createEntity(final String name, final String description) {
        LOGGER.info("createEntity called with name: {} and description: {}", name, description);

        // Standard test failure logic for error handler testing
        if ("fail".equals(name)) {
            LOGGER.warn("Test failure requested for name: {}", name);
            throw new RuntimeException("This is for testing the error handler");
        }

        // Validate name using parent validation
        validateEntityName(name);
        final CApprovalStatus entity = new CApprovalStatus(name, description);
        repository.saveAndFlush(entity);
        LOGGER.info("Approval status created successfully with name: {}", name);
    }

    @Override
    protected CApprovalStatus createNewEntityInstance() {
        return new CApprovalStatus();
    }
}