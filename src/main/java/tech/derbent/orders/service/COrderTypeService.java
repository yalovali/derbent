package tech.derbent.orders.service;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.orders.domain.COrderType;
import tech.derbent.projects.domain.CProject;

/**
 * COrderTypeService - Service layer for COrderType entity.
 * Layer: Service (MVC)
 * 
 * Handles business logic for project-aware order type operations including creation,
 * validation, and management of order type entities.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class COrderTypeService extends CEntityOfProjectService<COrderType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(COrderTypeService.class);

    /**
     * Constructor for COrderTypeService.
     * 
     * @param repository the COrderTypeRepository to use for data access
     * @param clock the Clock instance for time-related operations
     */
    COrderTypeService(final COrderTypeRepository repository, final Clock clock) {
        super(repository, clock);
    }

    /**
     * Creates a new order type entity with name, description and project.
     * 
     * @param name the name of the order type
     * @param description the description of the order type
     * @param project the project this type belongs to
     */
    @Transactional
    public void createEntity(final String name, final String description, final CProject project) {
        LOGGER.info("createEntity called with name: {}, description: {} for project: {}", 
            name, description, project.getName());

        // Standard test failure logic for error handler testing
        if ("fail".equals(name)) {
            LOGGER.warn("Test failure requested for name: {}", name);
            throw new RuntimeException("This is for testing the error handler");
        }

        // Validate name using parent validation
        validateEntityName(name);
        final COrderType entity = new COrderType(name, description, project);
        repository.saveAndFlush(entity);
        LOGGER.info("Order type created successfully with name: {} for project: {}", 
            name, project.getName());
    }

    @Override
    protected COrderType createNewEntityInstance() {
        return new COrderType();
    }
}