package tech.derbent.gannt.service;

import org.springframework.stereotype.Repository;
import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.gannt.domain.CGanntViewEntity;

@Repository
public interface IGanntViewEntityRepository extends IEntityOfProjectRepository<CGanntViewEntity> {
}
