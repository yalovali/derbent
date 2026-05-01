package tech.derbent.api.page.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.page.domain.CPageEntityType;

public interface IPageEntityTypeRepository extends IEntityOfCompanyRepository<CPageEntityType> {

	@Query ("SELECT pt FROM #{#entityName} pt LEFT JOIN FETCH pt.company WHERE pt.id = :id")
	Optional<CPageEntityType> findByIdWithRelationships(@Param ("id") Long id);

	@Override
	@Query ("""
			SELECT pt FROM #{#entityName} pt
			LEFT JOIN FETCH pt.company
			LEFT JOIN FETCH pt.workflow
			WHERE pt.company = :company
			ORDER BY pt.name ASC
			""")
	List<CPageEntityType> listByCompanyForPageView(@Param ("company") CCompany company);
}
