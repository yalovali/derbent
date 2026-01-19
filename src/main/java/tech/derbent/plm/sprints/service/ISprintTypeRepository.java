package tech.derbent.plm.sprints.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.plm.sprints.domain.CSprintType;

public interface ISprintTypeRepository extends IEntityOfCompanyRepository<CSprintType> {

	@Override
	@Query ("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.company
			LEFT JOIN FETCH t.workflow
			WHERE t.company = :company
			ORDER BY t.name ASC
			""")
	List<CSprintType> listByCompanyForPageView(@Param ("company") CCompany company);
}
