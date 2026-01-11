package tech.derbent.app.comments.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.comments.domain.CCommentPriority;
import tech.derbent.app.companies.domain.CCompany;

/** CCommentPriorityRepository - Repository interface for CCommentPriority entities. Layer: Service (MVC) - Repository interface Provides data access
 * methods for comment priority entities. */
public interface ICommentPriorityRepository extends IEntityOfCompanyRepository<CCommentPriority> {

	@Override
	@Query ("""
			SELECT p FROM #{#entityName} p
			LEFT JOIN FETCH p.company
			LEFT JOIN FETCH p.workflow
			WHERE p.company = :company
			ORDER BY p.name ASC
			""")
	List<CCommentPriority> listByCompanyForPageView(@Param ("company") CCompany company);
}
