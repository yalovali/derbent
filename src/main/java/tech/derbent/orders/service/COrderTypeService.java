package tech.derbent.orders.service;

import java.time.Clock;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.orders.domain.COrderType;

/**
 * COrderTypeService - Service layer for COrderType entity. Layer: Service (MVC) Handles business logic for
 * project-aware order type operations including creation, validation, and management of order type entities.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class COrderTypeService extends CEntityOfProjectService<COrderType> {

    /**
     * Constructor for COrderTypeService.
     * 
     * @param repository
     *            the COrderTypeRepository to use for data access
     * @param clock
     *            the Clock instance for time-related operations
     */
    COrderTypeService(final COrderTypeRepository repository, final Clock clock) {
        super(repository, clock);
    }

    @Override
    protected Class<COrderType> getEntityClass() {
        return COrderType.class;
    }
}