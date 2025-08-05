package tech.derbent.users.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractNamedRepository;
import tech.derbent.users.domain.CUser;

public interface CUserRepository extends CAbstractNamedRepository<CUser> {

	@Query (
		"SELECT COUNT(DISTINCT u) FROM CUser u JOIN u.projectSettings ps WHERE ps.project.id = :projectId"
	)
	long countUsersByProjectId(@Param ("projectId") Long projectId);
	
	/**
	 * Finds all enabled login users. Useful for administration and user management.
	 * @param enabled true to find enabled users, false for disabled users
	 * @return List of CUser entities matching the enabled status
	 */
	@Query ("SELECT u FROM CUser u WHERE u.enabled = :enabled")
	List<CUser> findByEnabled(@Param ("enabled") boolean enabled);
	
	/**
	 * Finds a login user by their username (login field). This method is crucial for the
	 * authentication flow.
	 * @param username the username to search for (corresponds to login field in CUser)
	 * @return Optional containing the CUser if found, empty otherwise
	 */
	@Query ("SELECT u FROM CUser u WHERE u.login = :username")
	Optional<CUser> findByUsername(@Param ("username") String username);
	
	/**
	 * Find user by ID with eager loading of commonly accessed associations.
	 * This prevents N+1 queries when accessing user details.
	 * @param id the user ID
	 * @return Optional containing the user with eagerly loaded associations
	 */
	@Query ("SELECT u FROM CUser u " +
			"LEFT JOIN FETCH u.userType " +
			"LEFT JOIN FETCH u.company " +
			"WHERE u.id = :id")
	Optional<CUser> findByIdWithEagerLoading(@Param ("id") Long id);
	
	/**
	 * Find all enabled users with eager loading for dropdown/selection purposes.
	 * Optimized for UI components that need user information.
	 * @return List of enabled users with eager loaded associations
	 */
	@Query ("SELECT u FROM CUser u " +
			"LEFT JOIN FETCH u.userType " +
			"LEFT JOIN FETCH u.company " +
			"WHERE u.enabled = true " +
			"ORDER BY u.name")
	List<CUser> findAllEnabledWithEagerLoading();
}
