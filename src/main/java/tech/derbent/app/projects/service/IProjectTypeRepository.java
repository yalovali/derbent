package tech.derbent.app.projects.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.projects.domain.CProjectType;

public interface IProjectTypeRepository extends IEntityOfCompanyRepository<CProjectType> {

	@Override
	@Query("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.company
			LEFT JOIN FETCH t.workflow
			WHERE t.company = :company
			ORDER BY t.name ASC
			""")
	List<CProjectType> listByCompanyForPageView(@Param("company") CCompany company);
}
