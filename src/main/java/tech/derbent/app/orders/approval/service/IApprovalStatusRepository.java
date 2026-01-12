package tech.derbent.app.orders.approval.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.app.orders.approval.domain.CApprovalStatus;

/** CApprovalStatusRepository - Repository interface for CApprovalStatus entities. Layer: Service (MVC) Provides data access operations for approval
 * statuses, extending the standard CAbstractNamedRepository to inherit common CRUD and query operations. */
public interface IApprovalStatusRepository extends IEntityOfCompanyRepository<CApprovalStatus> {

	@Override
	@Query ("""
			SELECT s FROM #{#entityName} s
			LEFT JOIN FETCH s.company
			WHERE s.company = :company
			ORDER BY s.name ASC
			""")
	List<CApprovalStatus> listByCompanyForPageView(@Param ("company") CCompany company);

	@Query ("""
			SELECT s FROM #{#entityName} s
			LEFT JOIN FETCH s.company
			WHERE s.id = :id
			""")
	Optional<CApprovalStatus> findByIdWithCompany(@Param ("id") Long id);
}
