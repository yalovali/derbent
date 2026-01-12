package tech.derbent.app.providers.providertype.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.providers.providertype.domain.CProviderType;
import tech.derbent.api.companies.domain.CCompany;

public interface IProviderTypeRepository extends IEntityOfCompanyRepository<CProviderType> {

	@Override
	@Query ("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.company
			LEFT JOIN FETCH t.workflow
			WHERE t.company = :company
			ORDER BY t.name ASC
			""")
	List<CProviderType> listByCompanyForPageView(@Param ("company") CCompany company);
}
