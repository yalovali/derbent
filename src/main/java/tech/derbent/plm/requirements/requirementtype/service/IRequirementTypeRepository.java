package tech.derbent.plm.requirements.requirementtype.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.plm.requirements.requirementtype.domain.CRequirementType;

public interface IRequirementTypeRepository extends IEntityOfCompanyRepository<CRequirementType> {

	@Override
	@Query("""
			SELECT t FROM CRequirementType t
			LEFT JOIN FETCH t.company
			LEFT JOIN FETCH t.workflow
			WHERE t.company = :company
			ORDER BY t.sortOrder ASC, t.name ASC
			""")
	List<CRequirementType> listByCompanyForPageView(@Param("company") CCompany company);
}
