package tech.derbent.plm.projectincomes.projectincometype.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.plm.projectincomes.projectincometype.domain.CProjectIncomeType;
import tech.derbent.api.companies.domain.CCompany;

public interface IProjectIncomeTypeRepository extends IEntityOfCompanyRepository<CProjectIncomeType> {

	@Override
	@Query ("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.company
			LEFT JOIN FETCH t.workflow
			WHERE t.company = :company
			ORDER BY t.name ASC
			""")
	List<CProjectIncomeType> listByCompanyForPageView(@Param ("company") CCompany company);
}
