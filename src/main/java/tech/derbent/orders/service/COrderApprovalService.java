package tech.derbent.orders.service;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.orders.domain.COrderApproval;

/**
 * COrderApprovalService - Service layer for COrderApproval entity.
 * Layer: Service (MVC)
 * 
 * Handles business logic for order approval operations including creation,
 * validation, and management of order approval entities.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class COrderApprovalService extends CAbstractNamedEntityService<COrderApproval> {

    private static final Logger LOGGER = LoggerFactory.getLogger(COrderApprovalService.class);

    /**
     * Constructor for COrderApprovalService.
     * 
     * @param repository the COrderApprovalRepository to use for data access
     * @param clock the Clock instance for time-related operations
     */
    COrderApprovalService(final COrderApprovalRepository repository, final Clock clock) {
        super(repository, clock);
    }

    /**
     * Creates a new order approval entity with name and description.
     * 
     * @param name the name of the order approval
     * @param description the description of the order approval
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
        final COrderApproval entity = new COrderApproval();
        entity.setName(name);
        entity.setDescription(description);
        repository.saveAndFlush(entity);
        LOGGER.info("Order approval created successfully with name: {}", name);
    }

    @Override
    protected COrderApproval createNewEntityInstance() {
        return new COrderApproval();
    }
}