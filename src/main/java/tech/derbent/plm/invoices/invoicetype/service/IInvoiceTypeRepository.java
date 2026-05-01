package tech.derbent.plm.invoices.invoicetype.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.plm.invoices.invoicetype.domain.CInvoiceType;

public interface IInvoiceTypeRepository extends IEntityOfCompanyRepository<CInvoiceType> {

	@Query ("SELECT it FROM #{#entityName} it LEFT JOIN FETCH it.company WHERE it.id = :id")
	Optional<CInvoiceType> findByIdWithRelationships(@Param ("id") Long id);

	@Override
	@Query ("""
			SELECT it FROM #{#entityName} it
			LEFT JOIN FETCH it.company
			LEFT JOIN FETCH it.workflow
			WHERE it.company = :company
			ORDER BY it.name ASC
			""")
	List<CInvoiceType> listByCompanyForPageView(@Param ("company") CCompany company);
}
