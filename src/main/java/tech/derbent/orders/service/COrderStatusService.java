package tech.derbent.orders.service;

import java.time.Clock;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.orders.domain.COrderStatus;

/**
 * COrderStatusService - Service layer for COrderStatus entity. Layer: Service (MVC) Handles business logic for order
 * status operations including creation, validation, and management of order status entities.
 * Since COrderStatus extends CStatus which extends CTypeEntity which extends CEntityOfProject,
 * this service must extend CEntityOfProjectService to enforce project-based queries.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class COrderStatusService extends CEntityOfProjectService<COrderStatus> {

    /**
     * Constructor for COrderStatusService.
     * 
     * @param repository
     *            the COrderStatusRepository to use for data access
     * @param clock
     *            the Clock instance for time-related operations
     */
    COrderStatusService(final COrderStatusRepository repository, final Clock clock) {
        super(repository, clock);
    }

    @Override
    protected Class<COrderStatus> getEntityClass() {
        return COrderStatus.class;
    }
}