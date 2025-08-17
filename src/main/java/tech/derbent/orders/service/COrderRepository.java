package tech.derbent.orders.service;

import java.util.List;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.orders.domain.COrder;
import tech.derbent.users.domain.CUser;

/**
 * COrderRepository - Repository interface for COrder entities. Layer: Service (MVC) Provides data access operations for
 * orders, extending the CEntityOfProjectRepository to inherit common CRUD and query operations with proper lazy
 * loading. Includes order-specific query methods for finding orders by requestor, responsible user, and status.
 */
public interface COrderRepository extends CEntityOfProjectRepository<COrder> {

    List<COrder> findByRequestor(CUser requestor);

    List<COrder> findByResponsible(CUser responsible);
}