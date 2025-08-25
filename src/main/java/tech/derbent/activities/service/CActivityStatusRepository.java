package tech.derbent.activities.service;

import org.springframework.stereotype.Repository;
import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.activities.domain.CActivityStatus;

/** CActivityStatusRepository - Repository interface for CActivityStatus entities. Layer: Data Access (MVC) Provides data access operations for
 * activity status management. */
@Repository
public interface CActivityStatusRepository extends CEntityOfProjectRepository<CActivityStatus> {
}
