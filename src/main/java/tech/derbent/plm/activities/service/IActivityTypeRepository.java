package tech.derbent.plm.activities.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.plm.activities.domain.CActivityType;
import tech.derbent.api.companies.domain.CCompany;

public interface IActivityTypeRepository extends IEntityOfCompanyRepository<CActivityType> {

	@Override
	@Query ("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.company
			LEFT JOIN FETCH t.workflow
			WHERE t.company = :company
			ORDER BY t.name ASC
			""")
	List<CActivityType> listByCompanyForPageView(@Param ("company") CCompany company);
}
