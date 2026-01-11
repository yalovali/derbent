package tech.derbent.app.decisions.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.decisions.domain.CDecisionType;
import tech.derbent.app.companies.domain.CCompany;

/** CDecisionTypeRepository - Repository interface for CDecisionType entities. Layer: Data Access (MVC) Provides data access methods for project-aware
 * decision type entities with eager loading support. */
public interface IDecisionTypeRepository extends IEntityOfCompanyRepository<CDecisionType> {

	@Override
	@Query ("SELECT dt FROM CDecisionType dt " + "LEFT JOIN FETCH dt.company " + "WHERE dt.id = :id")
	Optional<CDecisionType> findById(@Param ("id") Long id);
	@Query ("SELECT dt FROM CDecisionType dt WHERE dt.company = :company AND dt.active = false")
	List<CDecisionType> findByCompanyAndActiveFalse(@Param ("company") CCompany company);
	@Query ("SELECT dt FROM CDecisionType dt WHERE dt.company = :company AND dt.active = true")
	List<CDecisionType> findByCompanyAndActiveTrue(@Param ("company") CCompany company);
	@Query ("SELECT dt FROM CDecisionType dt WHERE dt.company = :company AND dt.requiresApproval = true")
	List<CDecisionType> findByCompanyAndRequiresApprovalTrue(@Param ("company") CCompany company);
	@Query ("SELECT dt FROM CDecisionType dt WHERE dt.company = :company")
	List<CDecisionType> findByCompanyOrderBySortOrderAsc(@Param ("company") CCompany company);

	@Override
	@Query ("""
			SELECT dt FROM CDecisionType dt
			LEFT JOIN FETCH dt.company
			LEFT JOIN FETCH dt.workflow
			WHERE dt.company = :company
			ORDER BY dt.name ASC
			""")
	List<CDecisionType> listByCompanyForPageView(@Param ("company") CCompany company);
}
