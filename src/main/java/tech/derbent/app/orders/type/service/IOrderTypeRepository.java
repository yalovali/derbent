package tech.derbent.app.orders.type.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.orders.type.domain.COrderType;
import tech.derbent.app.projects.domain.CProject;

/** COrderTypeRepository - Repository interface for COrderType entities. Layer: Service (MVC) Provides data access operations for project-aware order
 * types, extending the standard CEntityOfProjectRepository to inherit common CRUD and query operations. */
public interface IOrderTypeRepository extends IEntityOfProjectRepository<COrderType> {
	// Inherits standard operations from CEntityOfProjectRepository
	// Additional custom query methods can be added here if needed

	@Override
	@Query ("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.project
			LEFT JOIN FETCH t.assignedTo
			LEFT JOIN FETCH t.createdBy
			LEFT JOIN FETCH t.workflow
			WHERE t.project = :project
			ORDER BY t.name ASC
			""")
	List<COrderType> listByProjectForPageView(@Param ("project") CProject project);
}
