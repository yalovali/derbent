package tech.derbent.app.components.componentversion.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.components.componentversion.domain.CComponentVersion;
import tech.derbent.app.components.componentversiontype.domain.CComponentVersionType;

public interface IComponentVersionRepository extends IEntityOfProjectRepository<CComponentVersion> {

@Query ("SELECT COUNT(a) FROM {#entityName} a WHERE a.entityType = :type")
long countByType(@Param ("type") CComponentVersionType type);

@Override
@Query ("SELECT r FROM CComponentVersion r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy LEFT JOIN FETCH r.status LEFT JOIN FETCH r.entityType LEFT JOIN FETCH r.component WHERE r.id = :id")
Optional<CComponentVersion> findById(@Param ("id") Long id);
}
