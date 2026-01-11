package tech.derbent.app.milestones.milestonetype.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.milestones.milestonetype.domain.CMilestoneType;
import tech.derbent.app.companies.domain.CCompany;

public interface IMilestoneTypeRepository extends IEntityOfCompanyRepository<CMilestoneType> {

	@Override
	@Query ("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.company
			LEFT JOIN FETCH t.workflow
			WHERE t.company = :company
			ORDER BY t.name ASC
			""")
	List<CMilestoneType> listByCompanyForPageView(@Param ("company") CCompany company);
}
