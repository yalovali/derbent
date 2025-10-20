package tech.derbent.base.users.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.IAbstractNamedRepository;
import tech.derbent.app.companies.service.ICompanyEntityRepositoryBase;
import tech.derbent.base.users.domain.CUser;

public interface IUserRepository extends IAbstractNamedRepository<CUser>, ICompanyEntityRepositoryBase<CUser> {

	/** Count distinct users by project ID using generic pattern */
	@Query ("SELECT COUNT(DISTINCT u) FROM #{#entityName} u LEFT JOIN u.projectSettings ps WHERE ps.project.id = :projectId")
	long countByProjectId(@Param ("projectId") Long projectId);
	/** Find all users by company ID with eager loading */
	@Override
	@Query (
		"SELECT u FROM #{#entityName} u LEFT JOIN FETCH u.company co LEFT JOIN FETCH u.companyRole cr LEFT JOIN FETCH u.activities WHERE u.company.id = :companyId"
	)
	List<CUser> findByCompanyId(@Param ("companyId") Long companyId);
	/** Find users by company ID with pagination */
	@Override
	@Query (
		"SELECT u FROM #{#entityName} u LEFT JOIN FETCH u.company co LEFT JOIN FETCH u.companyRole cr LEFT JOIN FETCH u.activities WHERE u.company.id = :companyId"
	)
	Page<CUser> findByCompanyId(@Param ("companyId") Long companyId, Pageable pageable);
	/** Find user by ID with eager loading using generic pattern */
	@Override
	@Query ("SELECT u FROM #{#entityName} u " + /* */
			"LEFT JOIN FETCH u.company co LEFT JOIN FETCH u.companyRole cr LEFT JOIN FETCH u.activities WHERE u.id = :userId"
	)
	Optional<CUser> findById(@Param ("userId") Long id);
	/** Find all users by project ID with eager loading using generic pattern */
	@Query ("SELECT u FROM #{#entityName} u " + /* */
			"LEFT JOIN FETCH u.company co WHERE u.id IN (SELECT ups.user.id FROM CUserProjectSettings ups WHERE ups.project.id = :projectId)"
	)
	List<CUser> findByProject(Long projectId);
	/** Find user by username with eager loading using generic pattern */
	@Query (
		"SELECT u FROM #{#entityName} u LEFT JOIN FETCH u.projectSettings ps LEFT JOIN FETCH u.company co LEFT JOIN FETCH ps.project LEFT JOIN FETCH u.activities WHERE u.login = :username and u.company.id = :CompanyId"
	)
	Optional<CUser> findByUsername(@Param ("CompanyId") Long companyId, @Param ("username") String username);
	/** Find all users that are not assigned to a specific company using generic pattern */
	@Query ("SELECT u FROM #{#entityName} u WHERE u.company.id != :companyId OR u.company IS NULL")
	List<CUser> findNotAssignedToCompany(@Param ("companyId") Long companyId);
	/** Find all users that are not assigned to a specific project using generic pattern */
	@Query (
		"SELECT u FROM #{#entityName} u WHERE u.id NOT IN (SELECT ups.user.id FROM CUserProjectSettings ups WHERE ups.project.id = :projectId) and (u.company.id = :CompanyId)"
	)
	List<CUser> findNotAssignedToProject(@Param ("projectId") Long projectId, @Param ("CompanyId") Long companyId);
}
