package tech.derbent.api.projects.service;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject_Bab;

@Profile ("bab")
public interface IProject_BabRepository extends IProjectRepository<CProject_Bab> {

	@Override
	@Query ("""
			SELECT p FROM CProject_Bab p
			LEFT JOIN FETCH p.company
			WHERE p.company.id = :company_id
			ORDER BY p.name
			""")
	java.util.List<CProject_Bab> findByCompanyId(@Param ("company_id") Long companyId);

	@Override
	@Query ("""
			SELECT p FROM CProject_Bab p
			LEFT JOIN FETCH p.company
			WHERE p.id = :id
			""")
	java.util.Optional<CProject_Bab> findByIdForPageView(@Param ("id") Long id);

	@Override
	@Query ("""
			SELECT p FROM CProject_Bab p
			LEFT JOIN FETCH p.company
			WHERE p.company.id = :company_id
			ORDER BY p.name
			""")
	java.util.List<CProject_Bab> listByCompanyForPageView(@Param ("company_id") Long companyId);
}
