package tech.derbent.app.sprints.service;

import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.sprints.domain.CSprintStatus;

/**
 * ISprintStatusRepository - Repository interface for CSprintStatus entity.
 * Provides data access methods for sprint status management.
 */
public interface ISprintStatusRepository extends IEntityOfProjectRepository<CSprintStatus> {
	// Inherits standard CRUD operations from IEntityOfProjectRepository
}
