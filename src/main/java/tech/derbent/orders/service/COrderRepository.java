package tech.derbent.orders.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.orders.domain.COrder;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * COrderRepository - Repository interface for COrder entities. Layer: Service (MVC)
 * 
 * Provides data access operations for orders, extending the CEntityOfProjectRepository to inherit common CRUD and query
 * operations with proper lazy loading. Includes order-specific query methods for finding orders by requestor,
 * responsible user, and status.
 */
public interface COrderRepository extends CEntityOfProjectRepository<COrder> {

    /**
     * Finds orders by requestor.
     * 
     * @param requestor
     *            the user who requested the orders
     * @return list of orders requested by the user
     */
    List<COrder> findByRequestor(CUser requestor);

    /**
     * Finds orders by responsible user.
     * 
     * @param responsible
     *            the user responsible for the orders
     * @return list of orders managed by the user
     */
    List<COrder> findByResponsible(CUser responsible);

    /**
     * Finds an order by ID with eagerly loaded relationships to prevent LazyInitializationException. This extends the
     * base method with order-specific relationships.
     * 
     * @param id
     *            the order ID
     * @return optional COrder with loaded relationships
     */
    @Query("SELECT o FROM COrder o " + "LEFT JOIN FETCH o.project " + "LEFT JOIN FETCH o.assignedTo "
            + "LEFT JOIN FETCH o.createdBy " + "LEFT JOIN FETCH o.orderType " + "LEFT JOIN FETCH o.status "
            + "LEFT JOIN FETCH o.currency " + "LEFT JOIN FETCH o.requestor " + "LEFT JOIN FETCH o.responsible "
            + "WHERE o.id = :id")
    Optional<COrder> findByIdWithAllRelationships(@Param("id") Long id);

    /**
     * Finds orders by project with eagerly loaded relationships. This extends the base implementation with
     * order-specific relationships.
     * 
     * @param project
     *            the project
     * @return list of orders with loaded relationships
     */
    @Query("SELECT o FROM COrder o " + "LEFT JOIN FETCH o.project " + "LEFT JOIN FETCH o.assignedTo "
            + "LEFT JOIN FETCH o.createdBy " + "LEFT JOIN FETCH o.orderType " + "LEFT JOIN FETCH o.status "
            + "LEFT JOIN FETCH o.currency " + "LEFT JOIN FETCH o.requestor " + "LEFT JOIN FETCH o.responsible "
            + "WHERE o.project = :project")
    List<COrder> findByProjectWithAllRelationships(@Param("project") CProject project);
}