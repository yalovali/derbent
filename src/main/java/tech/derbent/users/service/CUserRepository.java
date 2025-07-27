package tech.derbent.users.service;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.users.domain.CUser;

public interface CUserRepository extends CAbstractRepository<CUser> {

    /**
     * Finds all enabled login users. Useful for administration and user management.
     * 
     * @param enabled
     *            true to find enabled users, false for disabled users
     * @return List of CUser entities matching the enabled status
     */
    @Query("SELECT u FROM CUser u WHERE u.enabled = :enabled")
    java.util.List<CUser> findByEnabled(@Param("enabled") boolean enabled);

    @Query("SELECT u FROM CUser u LEFT JOIN FETCH u.projectSettings WHERE u.id = :id")
    Optional<CUser> findByIdWithProjects(@Param("id") Long id);

    /**
     * Finds a login user by their CUser (login field). This method is crucial for the authentication flow.
     * Authentication Flow Usage: 1. User enters username in login form 2. Spring Security calls
     * UserDetailsService.loadUserByUsername() 3. CUser uses this method to find the user 4. If found, password is
     * verified and user is authenticated
     * 
     * @param username
     *            the username to search for (corresponds to login field in CUser)
     * @return Optional containing the CUser if found, empty otherwise
     */
    @Query("SELECT u FROM CUser u WHERE u.login = :username")
    Optional<CUser> findByUsername(@Param("username") String username);

    /**
     * Finds a login user by their username and loads associated projects. Useful for getting complete user data
     * including project assignments.
     * 
     * @param username
     *            the username to search for
     * @return Optional containing the CUser with projects if found, empty otherwise
     */
    @Query("SELECT u FROM CUser u LEFT JOIN FETCH u.projectSettings WHERE u.login = :username")
    Optional<CUser> findByUsernameWithProjects(@Param("username") String username);

    /**
     * Finds a user by ID with eagerly loaded CUserType to prevent LazyInitializationException.
     * 
     * @param id
     *            the user ID
     * @return optional CUser with loaded userType
     */
    @Query("SELECT u FROM CUser u LEFT JOIN FETCH u.userType WHERE u.id = :id")
    Optional<CUser> findByIdWithUserType(@Param("id") Long id);

    /**
     * Finds a user by ID with eagerly loaded CUserType and project settings.
     * 
     * @param id
     *            the user ID
     * @return optional CUser with loaded userType and project settings
     */
    @Query("SELECT u FROM CUser u LEFT JOIN FETCH u.userType LEFT JOIN FETCH u.projectSettings WHERE u.id = :id")
    Optional<CUser> findByIdWithUserTypeAndProjects(@Param("id") Long id);

    /**
     * Counts the number of users for a specific project.
     * 
     * @param projectId
     *            the project ID
     * @return count of users assigned to the project
     */
    @Query("SELECT COUNT(DISTINCT u) FROM CUser u JOIN u.projectSettings ps WHERE ps.projectId = :projectId")
    long countUsersByProjectId(@Param("projectId") Long projectId);
}
