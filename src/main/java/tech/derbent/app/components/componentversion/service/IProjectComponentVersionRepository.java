package tech.derbent.app.components.componentversion.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.components.componentversion.domain.CProjectComponentVersion;
import tech.derbent.app.components.componentversiontype.domain.CProjectComponentVersionType;

public interface IProjectComponentVersionRepository extends IEntityOfProjectRepository<CProjectComponentVersion> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
	long countByType(@Param ("entityType") CProjectComponentVersionType type);
	@Override
	@Query (
		"SELECT r FROM CProjectComponentVersion r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy LEFT JOIN FETCH r.status LEFT JOIN FETCH r.entityType et LEFT JOIN FETCH et.workflow LEFT JOIN FETCH r.component WHERE r.id = :id"
	)
	Optional<CProjectComponentVersion> findById(@Param ("id") Long id);
}
