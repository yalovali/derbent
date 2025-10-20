package tech.derbent.app.gannt.service;

import org.springframework.stereotype.Repository;
import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.app.gannt.domain.CGanntViewEntity;

@Repository
public interface IGanntViewEntityRepository extends IEntityOfProjectRepository<CGanntViewEntity> {
}
