package tech.derbent.app.deliverables.deliverable.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.deliverables.deliverable.domain.CDeliverable;
import tech.derbent.app.deliverables.deliverabletype.domain.CDeliverableType;

public interface IDeliverableRepository extends IEntityOfProjectRepository<CDeliverable> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
	long countByType(@Param ("entityType") CDeliverableType type);
	@Override
	@Query (
		"SELECT r FROM CDeliverable r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy LEFT JOIN FETCH r.status LEFT JOIN FETCH r.entityType et LEFT JOIN FETCH et.workflow " + "WHERE r.id = :id"
	)
	Optional<CDeliverable> findById(@Param ("id") Long id);
}
