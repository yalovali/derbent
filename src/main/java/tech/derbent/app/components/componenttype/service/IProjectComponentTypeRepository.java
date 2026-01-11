package tech.derbent.app.components.componenttype.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.components.componenttype.domain.CProjectComponentType;
import tech.derbent.app.companies.domain.CCompany;

public interface IProjectComponentTypeRepository extends IEntityOfCompanyRepository<CProjectComponentType> {

	@Override
	@Query ("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.company
			LEFT JOIN FETCH t.workflow
			WHERE t.company = :company
			ORDER BY t.name ASC
			""")
	List<CProjectComponentType> listByCompanyForPageView(@Param ("company") CCompany company);
}
