package tech.derbent.orders.service;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.orders.domain.COrderStatus;

/**
 * COrderStatusService - Service layer for COrderStatus entity.
 * Layer: Service (MVC)
 * 
 * Handles business logic for order status operations including creation,
 * validation, and management of order status entities.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class COrderStatusService extends CAbstractNamedEntityService<COrderStatus> {

    private static final Logger LOGGER = LoggerFactory.getLogger(COrderStatusService.class);

    /**
     * Constructor for COrderStatusService.
     * 
     * @param repository the COrderStatusRepository to use for data access
     * @param clock the Clock instance for time-related operations
     */
    COrderStatusService(final COrderStatusRepository repository, final Clock clock) {
        super(repository, clock);
    }

    /**
     * Creates a new order status entity with name and description.
     * 
     * @param name the name of the order status
     * @param description the description of the order status
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
        final COrderStatus entity = new COrderStatus(name, description);
        repository.saveAndFlush(entity);
        LOGGER.info("Order status created successfully with name: {}", name);
    }

    @Override
    protected COrderStatus createNewEntityInstance() {
        return new COrderStatus();
    }
}