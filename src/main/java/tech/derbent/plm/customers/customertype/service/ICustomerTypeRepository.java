package tech.derbent.plm.customers.customertype.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.plm.customers.customertype.domain.CCustomerType;
import tech.derbent.api.companies.domain.CCompany;

public interface ICustomerTypeRepository extends IEntityOfCompanyRepository<CCustomerType> {

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
