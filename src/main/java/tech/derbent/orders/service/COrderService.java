package tech.derbent.orders.service;

import java.time.Clock;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.orders.domain.COrder;
import tech.derbent.users.domain.CUser;

/**
 * COrderService - Service layer for COrder entity. Layer: Service (MVC) Handles business logic for order operations
 * including creation, validation, project-based queries, and management of order entities with comprehensive financial
 * and approval workflow support.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class COrderService extends CEntityOfProjectService<COrder> {

    /**
     * Constructor for COrderService.
     * 
     * @param repository
     *            the COrderRepository to use for data access
     * @param clock
     *            the Clock instance for time-related operations
     */
    COrderService(final COrderRepository repository, final Clock clock) {
        super(repository, clock);
    }

    /**
     * Finds orders by requestor.
     * 
     * @param requestor
     *            the user who requested the orders
     * @return list of orders requested by the user
     */
    public List<COrder> findByRequestor(final CUser requestor) {
        LOGGER.info("findByRequestor called with requestor: {}", requestor != null ? requestor.getName() : "null");

        if (requestor == null) {
            return List.of();
        }
        return ((COrderService) repository).findByRequestor(requestor);
    }

    /**
     * Finds orders by responsible user.
     * 
     * @param responsible
     *            the user responsible for the orders
     * @return list of orders managed by the user
     */
    public List<COrder> findByResponsible(final CUser responsible) {
        LOGGER.info("findByResponsible called with responsible: {}",
                responsible != null ? responsible.getName() : "null");

        if (responsible == null) {
            return List.of();
        }
        return ((COrderService) repository).findByResponsible(responsible);
    }

    @Override
    protected Class<COrder> getEntityClass() {
        return COrder.class;
    }

    /**
     * Gets an order by ID with all relationships eagerly loaded. This prevents LazyInitializationException when
     * accessing order details.
     * 
     * @param id
     *            the order ID
     * @return optional order with loaded relationships
     */
    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<COrder> getById(final Long id) {
        if (id == null) {
            return java.util.Optional.empty();
        }

        LOGGER.debug("Getting COrder with ID {} (overridden to eagerly load relationships)", id);
        final java.util.Optional<COrder> entity = ((COrderRepository) repository).findByIdWithEagerLoading(id);
        entity.ifPresent(this::initializeLazyFields);
        return entity;
    }

    /**
     * Enhanced initialization of lazy-loaded fields specific to Order entities. Uses improved null-safe patterns.
     * 
     * @param entity
     *            the order entity to initialize
     */
    @Override
    public void initializeLazyFields(final COrder entity) {

        if (entity == null) {
            LOGGER.debug("Order entity is null, skipping lazy field initialization");
            return;
        }

        try {
            super.initializeLazyFields(entity); // Handles CEntityOfProject relationships automatically
            initializeLazyRelationship(entity.getOrderType(), "orderType");
            initializeLazyRelationship(entity.getStatus(), "status");

            if ((entity.getApprovals() != null) && !entity.getApprovals().isEmpty()) {
                entity.getApprovals().forEach(approval -> initializeLazyRelationship(approval, "approval"));
            }
        } catch (final Exception e) {
            LOGGER.warn("Error initializing lazy fields for Order with ID: {}", entity.getId(), e);
        }
    }
}