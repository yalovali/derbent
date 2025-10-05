package tech.derbent.users.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.IAbstractNamedRepository;
import tech.derbent.users.domain.CUser;

public interface IUserRepository extends IAbstractNamedRepository<CUser> {

	/** Count distinct users by project ID using generic pattern */
	@Query ("SELECT COUNT(DISTINCT u) FROM #{#entityName} u LEFT JOIN u.projectSettings ps LEFT JOIN u.userType ut WHERE ps.project.id = :projectId")
	long countByProjectId(@Param ("projectId") Long projectId);
	/** Find user by ID with eager loading using generic pattern */
	@Override
	@Query ("SELECT u " + /**/
			"FROM #{#entityName} u LEFT JOIN FETCH u.userType " + /* */
			"WHERE u.id = :userId"
	)
	Optional<CUser> findById(@Param ("userId") Long id);
	/** Find user by ID with company setting eagerly loaded. This is used when the UI needs to access company data to avoid
	 * LazyInitializationException. */
	@Query ("SELECT u FROM #{#entityName} u " + /* */
			"LEFT JOIN FETCH u.userType " + /* */
			"LEFT JOIN FETCH u.companySetting cs " + /* */
			"LEFT JOIN FETCH cs.company " + /* */
			"LEFT JOIN FETCH cs.role " + /* */
			"WHERE u.id = :userId"
	)
	Optional<CUser> findByIdWithCompanySetting(@Param ("userId") Long userId);
	/** Find all users by project ID with eager loading using generic pattern */
	@Query ("SELECT u FROM #{#entityName} u " + /* */
			"WHERE u.id IN (SELECT ups.user.id FROM CUserProjectSettings ups WHERE ups.project.id = :projectId)"
	)
	List<CUser> findByProject(Long projectId);
	/** Find user by username with eager loading using generic pattern */
	@Query (
		"SELECT u FROM #{#entityName} u LEFT JOIN FETCH u.userType LEFT JOIN FETCH u.projectSettings ps LEFT JOIN FETCH ps.project WHERE u.login = :username"
	)
	Optional<CUser> findByUsername(@Param ("username") String username);
	/** Find all users that are not assigned to a specific company using generic pattern */
	@Query ("SELECT u FROM #{#entityName} u WHERE u.id NOT IN (SELECT ucs.user.id FROM CUserCompanySetting ucs WHERE ucs.company.id = :companyId)")
	List<CUser> findUsersNotAssignedToCompany(@Param ("companyId") Long companyId);
	/** Find all users that are not assigned to a specific project using generic pattern */
	@Query ("SELECT u FROM #{#entityName} u WHERE u.id NOT IN (SELECT ups.user.id FROM CUserProjectSettings ups WHERE ups.project.id = :projectId)")
	List<CUser> findUsersNotAssignedToProject(@Param ("projectId") Long projectId);
	@Query ("SELECT u " + /**/
			"FROM #{#entityName} u LEFT JOIN FETCH u.userType "
	)
	Page<CUser> list(Pageable pageable);
	/** Find all users by company ID with eager loading */
	@Query (
		"SELECT u FROM #{#entityName} u " + "LEFT JOIN FETCH u.userType "
				+ "WHERE u.id IN (SELECT ucs.user.id FROM CUserCompanySetting ucs WHERE ucs.company.id = :companyId)"
	)
	List<CUser> findByCompanyId(@Param ("companyId") Long companyId);
	/** Find users by company ID with pagination */
	@Query (
		"SELECT u FROM #{#entityName} u " + "LEFT JOIN FETCH u.userType "
				+ "WHERE u.id IN (SELECT ucs.user.id FROM CUserCompanySetting ucs WHERE ucs.company.id = :companyId)"
	)
	Page<CUser> findByCompanyId(@Param ("companyId") Long companyId, Pageable pageable);
}
