package tech.derbent.app.components.component.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.components.component.domain.CProjectComponent;
import tech.derbent.app.components.componenttype.domain.CProjectComponentType;

public interface IProjectComponentRepository extends IEntityOfProjectRepository<CProjectComponent> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
	long countByType(@Param ("entityType") CProjectComponentType type);
	@Override
	@Query (
		"SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy LEFT JOIN FETCH r.status LEFT JOIN FETCH r.entityType et LEFT JOIN FETCH et.workflow WHERE r.id = :id"
	)
	Optional<CProjectComponent> findById(@Param ("id") Long id);
}
