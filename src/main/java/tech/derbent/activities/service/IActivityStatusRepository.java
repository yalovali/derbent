package tech.derbent.activities.service;

import org.springframework.stereotype.Repository;
import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.activities.domain.CActivityStatus;

/** CActivityStatusRepository - Repository interface for CActivityStatus entities. Layer: Data Access (MVC) Provides data access operations for
 * activity status management. */
@Repository
public interface IActivityStatusRepository extends IEntityOfProjectRepository<CActivityStatus> {
}
