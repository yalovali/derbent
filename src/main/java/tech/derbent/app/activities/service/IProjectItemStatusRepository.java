package tech.derbent.app.activities.service;

import org.springframework.stereotype.Repository;
import tech.derbent.api.entityOfProject.domain.CProjectItemStatus;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;

/** CProjectItemStatusRepository - Repository interface for CProjectItemStatus entities. Layer: Data Access (MVC) Provides data access operations for
 * activity status management. */
@Repository
public interface IProjectItemStatusRepository extends IEntityOfProjectRepository<CProjectItemStatus> {
}
