package tech.derbent.api.entityOfCompany.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.companies.domain.CCompany;

/** CProjectItemStatusRepository - Repository interface for CProjectItemStatus entities. Layer: Data Access (MVC) Provides data access operations for
 * activity status management. */
@Repository
public interface IProjectItemStatusRepository extends IEntityOfCompanyRepository<CProjectItemStatus> {

	@Override
	@Query ("""
			SELECT s FROM #{#entityName} s
			LEFT JOIN FETCH s.company
			WHERE s.company = :company
			ORDER BY s.name ASC
			""")
	List<CProjectItemStatus> listByCompanyForPageView(@Param ("company") CCompany company);
}
