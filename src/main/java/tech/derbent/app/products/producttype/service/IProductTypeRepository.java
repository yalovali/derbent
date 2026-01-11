package tech.derbent.app.products.producttype.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.products.producttype.domain.CProductType;
import tech.derbent.app.companies.domain.CCompany;

public interface IProductTypeRepository extends IEntityOfCompanyRepository<CProductType> {

	@Override
	@Query ("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.company
			LEFT JOIN FETCH t.workflow
			WHERE t.company = :company
			ORDER BY t.name ASC
			""")
	List<CProductType> listByCompanyForPageView(@Param ("company") CCompany company);
}
