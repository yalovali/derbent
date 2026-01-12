package tech.derbent.app.orders.type.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.orders.type.domain.COrderType;
import tech.derbent.api.companies.domain.CCompany;

/** COrderTypeRepository - Repository interface for COrderType entities. Layer: Service (MVC) Provides data access operations for project-aware order
 * types, extending the standard CEntityOfProjectRepository to inherit common CRUD and query operations. */
public interface IOrderTypeRepository extends IEntityOfCompanyRepository<COrderType> {
	// Inherits standard operations from IEntityOfCompanyRepository
	// Additional custom query methods can be added here if needed

	@Override
	@Query ("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.company
			LEFT JOIN FETCH t.workflow
			WHERE t.company = :company
			ORDER BY t.name ASC
			""")
	List<COrderType> listByCompanyForPageView(@Param ("company") CCompany company);
}
