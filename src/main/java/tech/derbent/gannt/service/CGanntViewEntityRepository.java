package tech.derbent.gannt.service;

import org.springframework.stereotype.Repository;
import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.gannt.domain.CGanntViewEntity;

@Repository
public interface CGanntViewEntityRepository extends CEntityOfProjectRepository<CGanntViewEntity> {
}
