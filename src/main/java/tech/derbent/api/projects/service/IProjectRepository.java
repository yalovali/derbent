package tech.derbent.api.projects.service;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.companies.service.ICompanyEntityRepositoryBase;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.domain.CProjectType;

public interface IProjectRepository extends IEntityOfCompanyRepository<CProject>, ICompanyEntityRepositoryBase<CProject> {

	@Query ("SELECT COUNT(p) FROM CProject p WHERE p.entityType = :type")
	long countByType(@Param ("type") CProjectType type);
	@Override
	@Query ("""
			SELECT p FROM CProject p
			LEFT JOIN FETCH p.company
			LEFT JOIN FETCH p.attachments
			LEFT JOIN FETCH p.comments
			WHERE p.company.id = :company_id
			ORDER BY p.name
			""")
	List<CProject> findByCompanyId(@Param ("company_id") Long company_id);
	@Override
	@Query ("SELECT p FROM CProject p LEFT JOIN FETCH p.company WHERE p.company.id = :company_id ORDER BY p.name")
	Page<CProject> findByCompanyId(@Param ("company_id") Long company_id, Pageable pageable);
	@Query ("""
			SELECT p FROM CProject p
			LEFT JOIN FETCH p.company
			LEFT JOIN FETCH p.comments
			LEFT JOIN FETCH p.attachments
			LEFT JOIN FETCH p.kanbanLine
			WHERE p.id = :id
			""")
	Optional<CProject> findByIdForPageView(@Param ("id") Long id);
	@Query ("""
				SELECT p FROM CProject p
				LEFT JOIN FETCH p.kanbanLine
				WHERE p.id NOT IN (SELECT ups.project.id FROM CUserProjectSettings ups WHERE ups.user.id = :userId) and
				p.company.id = (SELECT u.company.id FROM CUser u WHERE u.id = :userId) ORDER BY p.name
			""")
	List<CProject> findNotAssignedToUser(@Param ("userId") Long userId);
	@Query ("""
			SELECT p FROM CProject p
			LEFT JOIN FETCH p.company
			LEFT JOIN FETCH p.comments
			LEFT JOIN FETCH p.attachments
			LEFT JOIN FETCH p.kanbanLine
			WHERE p.company.id = :company_id
			ORDER BY p.name
			""")
	List<CProject> listByCompanyForPageView(@Param ("company_id") Long company_id);
}
