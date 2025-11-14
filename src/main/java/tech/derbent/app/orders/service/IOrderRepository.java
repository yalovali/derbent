package tech.derbent.app.orders.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.orders.domain.COrder;

public interface IOrderRepository extends IEntityOfProjectRepository<COrder> {

	@Override
	@Query (
		"SELECT o FROM COrder o " + "LEFT JOIN FETCH o.project " + "LEFT JOIN FETCH o.assignedTo " + "LEFT JOIN FETCH o.createdBy "
				+ "LEFT JOIN FETCH o.entityType " + "LEFT JOIN FETCH o.status " + "WHERE o.id = :id"
	)
	Optional<COrder> findById(@Param ("id") Long id);
}
