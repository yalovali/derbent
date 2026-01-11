package tech.derbent.app.budgets.budgettype.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.budgets.budgettype.domain.CBudgetType;
import tech.derbent.app.companies.domain.CCompany;

public interface IBudgetTypeRepository extends IEntityOfCompanyRepository<CBudgetType> {

	@Override
	@Query ("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.company
			LEFT JOIN FETCH t.workflow
			WHERE t.company = :company
			ORDER BY t.name ASC
			""")
	List<CBudgetType> listByCompanyForPageView(@Param ("company") CCompany company);
}
