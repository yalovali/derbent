package tech.derbent.plm.projectexpenses.projectexpensetype.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.plm.projectexpenses.projectexpensetype.domain.CProjectExpenseType;
import tech.derbent.api.companies.domain.CCompany;

public interface IProjectExpenseTypeRepository extends IEntityOfCompanyRepository<CProjectExpenseType> {

	@Override
	@Query ("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.company
			LEFT JOIN FETCH t.workflow
			WHERE t.company = :company
			ORDER BY t.name ASC
			""")
	List<CProjectExpenseType> listByCompanyForPageView(@Param ("company") CCompany company);
}
