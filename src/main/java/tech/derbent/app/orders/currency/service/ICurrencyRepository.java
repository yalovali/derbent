package tech.derbent.app.orders.currency.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.orders.currency.domain.CCurrency;
import tech.derbent.api.projects.domain.CProject;

public interface ICurrencyRepository extends IEntityOfProjectRepository<CCurrency> {
	// Inherits standard operations from CAbstractNamedRepository
	// Additional custom query methods can be added here if needed

	@Override
	@Query ("""
			SELECT c FROM #{#entityName} c
			LEFT JOIN FETCH c.project
			LEFT JOIN FETCH c.assignedTo
			LEFT JOIN FETCH c.createdBy
			WHERE c.project = :project
			ORDER BY c.name ASC
			""")
	List<CCurrency> listByProjectForPageView(@Param ("project") CProject<?> project);
}
