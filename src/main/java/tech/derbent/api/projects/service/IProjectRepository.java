package tech.derbent.api.projects.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.companies.service.ICompanyEntityRepositoryBase;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.domain.CProjectType;

@NoRepositoryBean
public interface IProjectRepository<ProjectClass extends CProject<ProjectClass>>
		extends IEntityOfCompanyRepository<ProjectClass>, ICompanyEntityRepositoryBase<ProjectClass> {

	@Query ("SELECT COUNT(p) FROM #{#entityName} p WHERE p.entityType = :type")
	long countByType(@Param ("type") CProjectType type);
	@Override
	@Query ("""
			SELECT p FROM #{#entityName} p
			LEFT JOIN FETCH p.company
			WHERE p.company.id = :company_id
			ORDER BY p.name
			""")
	List<ProjectClass> findByCompanyId(@Param ("company_id") Long company_id);
	@Override
	@Query ("SELECT p FROM #{#entityName} p LEFT JOIN FETCH p.company WHERE p.company.id = :company_id ORDER BY p.name")
	Page<ProjectClass> findByCompanyId(@Param ("company_id") Long company_id, Pageable pageable);
	@Query ("""
			SELECT p FROM #{#entityName} p
			LEFT JOIN FETCH p.company
			WHERE p.id = :id
			""")
	Optional<ProjectClass> findByIdForPageView(@Param ("id") Long id);
	@Query ("""
				SELECT p FROM #{#entityName} p
				WHERE p.id NOT IN (SELECT ups.project.id FROM CUserProjectSettings ups WHERE ups.user.id = :userId) and
				p.company.id = (SELECT u.company.id FROM CUser u WHERE u.id = :userId) ORDER BY p.name
			""")
	List<ProjectClass> findNotAssignedToUser(@Param ("userId") Long userId);
	@Query ("""
			SELECT p FROM #{#entityName} p
			LEFT JOIN FETCH p.company
			WHERE p.company.id = :company_id
			ORDER BY p.name
			""")
	List<ProjectClass> listByCompanyForPageView(@Param ("company_id") Long company_id);
}
