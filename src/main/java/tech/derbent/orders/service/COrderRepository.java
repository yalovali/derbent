package tech.derbent.orders.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.CEntityOfProjectRepository;
import tech.derbent.orders.domain.COrder;

public interface COrderRepository extends CEntityOfProjectRepository<COrder> {

	@Override
	@Query (
		"SELECT o FROM COrder o " + "LEFT JOIN FETCH o.project " + "LEFT JOIN FETCH o.assignedTo " + "LEFT JOIN FETCH o.createdBy "
				+ "LEFT JOIN FETCH o.orderType " + "LEFT JOIN FETCH o.status " + "WHERE o.id = :id"
	)
	Optional<COrder> findById(@Param ("id") Long id);
}
