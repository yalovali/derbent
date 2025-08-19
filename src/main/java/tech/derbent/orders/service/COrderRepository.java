package tech.derbent.orders.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.orders.domain.COrder;
import tech.derbent.users.domain.CUser;

/**
 * COrderRepository - Repository interface for COrder entities. Layer: Service (MVC) Provides data access operations for
 * orders, extending the CEntityOfProjectRepository to inherit common CRUD and query operations with proper lazy
 * loading. Includes order-specific query methods for finding orders by requestor, responsible user, and status.
 */
public interface COrderRepository extends CEntityOfProjectRepository<COrder> {

    /**
     * Finds an order by ID with eagerly loaded relationships to prevent N+1 queries.
     * 
     * @param id
     *            the order ID
     * @return Optional containing the order with loaded relationships
     */
    @Query("SELECT o FROM COrder o " + "LEFT JOIN FETCH o.project " + "LEFT JOIN FETCH o.assignedTo "
            + "LEFT JOIN FETCH o.createdBy " + "LEFT JOIN FETCH o.orderType " + "LEFT JOIN FETCH o.status "
            + "WHERE o.id = :id")
    Optional<COrder> findByIdWithEagerLoading(@Param("id") Long id);

    List<COrder> findByRequestor(CUser requestor);

    List<COrder> findByResponsible(CUser responsible);
}