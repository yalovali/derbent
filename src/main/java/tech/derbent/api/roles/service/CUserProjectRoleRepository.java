package tech.derbent.api.roles.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.services.CEntityOfProjectRepository;

/** CUserProjectRoleRepository - Repository interface for CUserProjectRole entity. Layer: Service (MVC) Provides data access operations for
 * project-aware user project roles with eager loading support. Uses super interface methods where available to maintain simplicity. */
public interface CUserProjectRoleRepository extends CEntityOfProjectRepository<CUserProjectRole> {

	/** Find role by ID with all relationships eagerly loaded.
	 * @param id the role ID
	 * @return optional role with relationships */
	@Query (
		"SELECT upr FROM CUserProjectRole upr " + "LEFT JOIN FETCH upr.project " + "LEFT JOIN FETCH upr.assignedTo "
				+ "LEFT JOIN FETCH upr.createdBy " + "WHERE upr.id = :id"
	)
	Optional<CUserProjectRole> findByIdWithRelationships(@Param ("id") Long id);
	/** Find roles by role type boolean flags.
	 * @param isAdmin whether to find admin roles
	 * @param isUser  whether to find user roles
	 * @param isGuest whether to find guest roles
	 * @return list of matching roles */
	@Query (
		"SELECT upr FROM CUserProjectRole upr " + "WHERE (:isAdmin IS NULL OR upr.isAdmin = :isAdmin) "
				+ "AND (:isUser IS NULL OR upr.isUser = :isUser) " + "AND (:isGuest IS NULL OR upr.isGuest = :isGuest)"
	)
	List<CUserProjectRole> findByRoleType(@Param ("isAdmin") Boolean isAdmin, @Param ("isUser") Boolean isUser, @Param ("isGuest") Boolean isGuest);
	/** Find roles that have read access to a specific page.
	 * @param pageName the page name
	 * @return list of roles with read access */
	@Query ("SELECT upr FROM CUserProjectRole upr " + "JOIN upr.readAccessPages rap " + "WHERE rap = :pageName")
	List<CUserProjectRole> findByReadAccessPage(@Param ("pageName") String pageName);
	/** Find roles that have write access to a specific page.
	 * @param pageName the page name
	 * @return list of roles with write access */
	@Query ("SELECT upr FROM CUserProjectRole upr " + "JOIN upr.writeAccessPages wap " + "WHERE wap = :pageName")
	List<CUserProjectRole> findByWriteAccessPage(@Param ("pageName") String pageName);
	/** Find admin roles for the current project.
	 * @return list of admin roles */
	@Query ("SELECT upr FROM CUserProjectRole upr " + "WHERE upr.isAdmin = true")
	List<CUserProjectRole> findAdminRoles();
	/** Find user roles for the current project.
	 * @return list of user roles */
	@Query ("SELECT upr FROM CUserProjectRole upr " + "WHERE upr.isUser = true")
	List<CUserProjectRole> findUserRoles();
	/** Find guest roles for the current project.
	 * @return list of guest roles */
	@Query ("SELECT upr FROM CUserProjectRole upr " + "WHERE upr.isGuest = true")
	List<CUserProjectRole> findGuestRoles();
}
