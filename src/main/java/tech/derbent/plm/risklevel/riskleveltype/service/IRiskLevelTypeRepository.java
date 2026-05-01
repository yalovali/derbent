package tech.derbent.plm.risklevel.riskleveltype.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.plm.risklevel.riskleveltype.domain.CRiskLevelType;

public interface IRiskLevelTypeRepository extends IEntityOfCompanyRepository<CRiskLevelType> {

	@Query ("SELECT rt FROM #{#entityName} rt LEFT JOIN FETCH rt.company WHERE rt.id = :id")
	Optional<CRiskLevelType> findByIdWithRelationships(@Param ("id") Long id);

	@Override
	@Query ("""
			SELECT rt FROM #{#entityName} rt
			LEFT JOIN FETCH rt.company
			LEFT JOIN FETCH rt.workflow
			WHERE rt.company = :company
			ORDER BY rt.name ASC
			""")
	List<CRiskLevelType> listByCompanyForPageView(@Param ("company") CCompany company);
}
