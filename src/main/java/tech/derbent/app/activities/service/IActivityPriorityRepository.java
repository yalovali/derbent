package tech.derbent.app.activities.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.activities.domain.CActivityPriority;
import tech.derbent.app.companies.domain.CCompany;

/** CActivityPriorityRepository - Repository interface for CActivityPriority entities. Layer: Data Access (MVC) Provides data access operations for
 * activity priority management. */
@Repository
public interface IActivityPriorityRepository extends IEntityOfCompanyRepository<CActivityPriority> {

	@Query ("SELECT p FROM CActivityPriority p WHERE p.isDefault = true and p.company = :company")
	Optional<CActivityPriority> findByIsDefaultTrue(@Param ("company") CCompany company);

	@Override
	@Query ("""
			SELECT p FROM CActivityPriority p
			LEFT JOIN FETCH p.company
			LEFT JOIN FETCH p.workflow
			WHERE p.company = :company
			ORDER BY p.name ASC
			""")
	List<CActivityPriority> listByCompanyForPageView(@Param ("company") CCompany company);
}
