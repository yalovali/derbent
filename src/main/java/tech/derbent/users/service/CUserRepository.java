package tech.derbent.users.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.CAbstractNamedRepository;
import tech.derbent.users.domain.CUser;

public interface CUserRepository extends CAbstractNamedRepository<CUser> {

	/** Count distinct users by project ID using generic pattern */
	@Query ("SELECT COUNT(DISTINCT u) FROM #{#entityName} u LEFT JOIN u.projectSettings ps LEFT JOIN u.userType ut WHERE ps.project.id = :projectId")
	long countByProjectId(@Param ("projectId") Long projectId);
	/** Find user by ID with eager loading using generic pattern */
	@Override
	@Query (
		"SELECT u FROM #{#entityName} u LEFT JOIN FETCH u.userType LEFT JOIN FETCH u.company LEFT JOIN FETCH u.projectSettings ps LEFT JOIN FETCH ps.project WHERE u.id = :userId"
	)
	Optional<CUser> findById(@Param ("userId") Long id);
	/** Find user by username with eager loading using generic pattern */
	@Query (
		"SELECT u FROM #{#entityName} u LEFT JOIN FETCH u.userType LEFT JOIN FETCH u.company LEFT JOIN FETCH u.projectSettings ps LEFT JOIN FETCH ps.project WHERE u.login = :username"
	)
	Optional<CUser> findByUsername(@Param ("username") String username);
	/** Find all users that are not assigned to a specific project using generic pattern */
	@Query ("SELECT u FROM #{#entityName} u WHERE u.id NOT IN (SELECT ups.user.id FROM CUserProjectSettings ups WHERE ups.project.id = :projectId)")
	List<CUser> findUsersNotAssignedToProject(@Param ("projectId") Long projectId);
}
