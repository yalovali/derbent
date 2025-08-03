package tech.derbent.orders.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.orders.domain.COrder;
import tech.derbent.projects.domain.CProject;
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

    private final COrderRepository orderRepository;

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
        this.orderRepository = repository;
    }

    /**
     * Finds an order by ID with eagerly loaded relationships.
     * 
     * @param id
     *            the order ID
     * @return optional COrder with loaded relationships
     */
    public Optional<COrder> findByIdWithRelationships(final Long id) {
        LOGGER.info("findByIdWithRelationships called with id: {}", id);

        if (id == null) {
            return Optional.empty();
        }
        return orderRepository.findByIdWithAllRelationships(id);
    }

    /**
     * Finds orders by project with eagerly loaded relationships.
     * 
     * @param project
     *            the project
     * @return list of orders with loaded relationships
     */
    public List<COrder> findByProjectWithRelationships(final CProject project) {
        LOGGER.info("findByProjectWithRelationships called with project: {}",
                project != null ? project.getName() : "null");

        if (project == null) {
            return List.of();
        }
        return orderRepository.findByProjectWithAllRelationships(project);
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
        return orderRepository.findByRequestor(requestor);
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
        return orderRepository.findByResponsible(responsible);
    }

    /**
     * Override get() method to eagerly load relationships and prevent LazyInitializationException. Following the
     * comprehensive lazy loading fix pattern from the guidelines.
     * 
     * @param id
     *            the order ID
     * @return optional COrder with all relationships loaded
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<COrder> getById(final Long id) {
        LOGGER.info("get called with id: {} (overridden to eagerly load relationships)", id);

        if (id == null) {
            return Optional.empty();
        }
        final Optional<COrder> entity = orderRepository.findByIdWithAllRelationships(id);
        entity.ifPresent(this::initializeLazyFields);
        return entity;
    }

    @Override
    protected Class<COrder> getEntityClass() {
        return COrder.class;
    }

    /**
     * Enhanced initialization of lazy-loaded fields specific to Order entities. Based on CActivityService
     * implementation style.
     * 
     * @param entity
     *            the order entity to initialize
     */
    @Override
    protected void initializeLazyFields(final COrder entity) {

        if (entity == null) {
            return;
        }
        LOGGER.debug("Initializing lazy fields for Order with ID: {} entity: {}", entity.getId(), entity.getName());

        try {
            // First call the parent implementation to handle common fields
            super.initializeLazyFields(entity);
            // Initialize Order-specific relationships
            initializeLazyRelationship(entity.getOrderType());
            initializeLazyRelationship(entity.getStatus());

            if ((entity.getApprovals() != null) && !entity.getApprovals().isEmpty()) {
                entity.getApprovals().forEach(this::initializeLazyRelationship);
            }
        } catch (final Exception e) {
            LOGGER.warn("Error initializing lazy fields for Order with ID: {}", entity.getId(), e);
        }
    }
}