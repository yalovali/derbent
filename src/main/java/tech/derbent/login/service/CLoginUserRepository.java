package tech.derbent.login.service;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.login.domain.CLoginUser;

/**
 * Repository interface for CLoginUser entities.
 * Extends CAbstractRepository to inherit basic CRUD operations.
 * 
 * This repository is used by the authentication system to:
 * 1. Find users by username for login validation
 * 2. Load user details for Spring Security authentication
 * 3. Manage user account data in the database
 */
public interface CLoginUserRepository extends CAbstractRepository<CLoginUser> {

	/**
	 * Finds a login user by their username (login field).
	 * This method is crucial for the authentication flow.
	 * 
	 * Authentication Flow Usage:
	 * 1. User enters username in login form
	 * 2. Spring Security calls UserDetailsService.loadUserByUsername()
	 * 3. CLoginUserService uses this method to find the user
	 * 4. If found, password is verified and user is authenticated
	 * 
	 * @param username the username to search for (corresponds to login field in CUser)
	 * @return Optional containing the CLoginUser if found, empty otherwise
	 */
	@Query("SELECT u FROM CLoginUser u WHERE u.login = :username")
	Optional<CLoginUser> findByUsername(@Param("username") String username);

	/**
	 * Finds a login user by their username and loads associated projects.
	 * Useful for getting complete user data including project assignments.
	 * 
	 * @param username the username to search for
	 * @return Optional containing the CLoginUser with projects if found, empty otherwise
	 */
	@Query("SELECT u FROM CLoginUser u LEFT JOIN FETCH u.projects WHERE u.login = :username")
	Optional<CLoginUser> findByUsernameWithProjects(@Param("username") String username);

	/**
	 * Finds all enabled login users.
	 * Useful for administration and user management.
	 * 
	 * @param enabled true to find enabled users, false for disabled users
	 * @return List of CLoginUser entities matching the enabled status
	 */
	@Query("SELECT u FROM CLoginUser u WHERE u.enabled = :enabled")
	java.util.List<CLoginUser> findByEnabled(@Param("enabled") boolean enabled);
}