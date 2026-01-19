package tech.derbent.plm.components.componentversiontype.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.plm.components.componentversiontype.domain.CProjectComponentVersionType;
import tech.derbent.api.companies.domain.CCompany;

public interface IProjectComponentVersionTypeRepository extends IEntityOfCompanyRepository<CProjectComponentVersionType> {

	@Override
	@Query ("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.company
			LEFT JOIN FETCH t.workflow
			WHERE t.company = :company
			ORDER BY t.name ASC
			""")
	List<CProjectComponentVersionType> listByCompanyForPageView(@Param ("company") CCompany company);
}
