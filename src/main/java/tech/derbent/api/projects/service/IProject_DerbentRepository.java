package tech.derbent.api.projects.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject_Derbent;

@Profile ("!bab")
public interface IProject_DerbentRepository extends IProjectRepository<CProject_Derbent> {

	@Override
	@Query ("""
			SELECT p FROM CProject_Derbent p
			LEFT JOIN FETCH p.company
			LEFT JOIN FETCH p.attachments
			LEFT JOIN FETCH p.comments
			WHERE p.company.id = :company_id
			ORDER BY p.name
			""")
	List<CProject_Derbent> findByCompanyId(@Param ("company_id") Long companyId);

	@Override
	@Query ("""
			SELECT p FROM CProject_Derbent p
			LEFT JOIN FETCH p.company
			LEFT JOIN FETCH p.comments
			LEFT JOIN FETCH p.attachments
			WHERE p.id = :id
			""")
	Optional<CProject_Derbent> findByIdForPageView(@Param ("id") Long id);

	@Override
	@Query ("""
			SELECT p FROM CProject_Derbent p
			LEFT JOIN FETCH p.company
			LEFT JOIN FETCH p.comments
			LEFT JOIN FETCH p.attachments
			WHERE p.company.id = :company_id
			ORDER BY p.name
			""")
	List<CProject_Derbent> listByCompanyForPageView(@Param ("company_id") Long companyId);
}
