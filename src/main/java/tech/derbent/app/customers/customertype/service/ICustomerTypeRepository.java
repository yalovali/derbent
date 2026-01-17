package tech.derbent.app.customers.customertype.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.domains.ITypeEntityRepository;
import tech.derbent.app.customers.customertype.domain.CCustomerType;
import tech.derbent.api.companies.domain.CCompany;

public interface ICustomerTypeRepository extends ITypeEntityRepository<CCustomerType> {

	@Override
	@Query("""
			SELECT ct FROM CCustomerType ct
			LEFT JOIN FETCH ct.workflow
			WHERE ct.id = :id
			""")
	Optional<CCustomerType> findById(@Param("id") Long id);

	@Override
	@Query("""
			SELECT ct FROM CCustomerType ct
			LEFT JOIN FETCH ct.workflow
			WHERE ct.company = :company
			ORDER BY ct.name ASC
			""")
	List<CCustomerType> listByCompanyForPageView(@Param("company") CCompany company);
}
