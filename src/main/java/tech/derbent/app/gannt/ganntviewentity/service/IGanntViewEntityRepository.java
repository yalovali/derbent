package tech.derbent.app.gannt.ganntviewentity.service;

import org.springframework.stereotype.Repository;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.gannt.ganntviewentity.domain.CGanntViewEntity;

@Repository
public interface IGanntViewEntityRepository extends IEntityOfProjectRepository<CGanntViewEntity> {
}
