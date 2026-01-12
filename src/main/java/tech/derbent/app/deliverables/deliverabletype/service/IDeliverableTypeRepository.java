package tech.derbent.app.deliverables.deliverabletype.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.deliverables.deliverabletype.domain.CDeliverableType;
import tech.derbent.api.companies.domain.CCompany;

public interface IDeliverableTypeRepository extends IEntityOfCompanyRepository<CDeliverableType> {

	@Override
	@Query ("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.company
			LEFT JOIN FETCH t.workflow
			WHERE t.company = :company
			ORDER BY t.name ASC
			""")
	List<CDeliverableType> listByCompanyForPageView(@Param ("company") CCompany company);
}
