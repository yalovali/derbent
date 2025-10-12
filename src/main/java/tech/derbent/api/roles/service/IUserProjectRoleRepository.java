package tech.derbent.api.roles.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.services.IEntityOfProjectRepository;

/** CUserProjectRoleRepository - Repository interface for CUserProjectRole entity. Layer: Service (MVC) Provides data access operations for
 * project-aware user project roles with eager loading support. Uses super interface methods where available to maintain simplicity. */
public interface IUserProjectRoleRepository extends IEntityOfProjectRepository<CUserProjectRole> {

	@Query (
		"SELECT upr FROM CUserProjectRole upr " + "LEFT JOIN FETCH upr.project " + "LEFT JOIN FETCH upr.assignedTo "
				+ "LEFT JOIN FETCH upr.createdBy " + "WHERE upr.id = :id"
	)
	Optional<CUserProjectRole> findByIdWithRelationships(@Param ("id") Long id);
	@Query (
		"SELECT upr FROM CUserProjectRole upr " + "WHERE (:isAdmin IS NULL OR upr.isAdmin = :isAdmin) "
				+ "AND (:isUser IS NULL OR upr.isUser = :isUser) " + "AND (:isGuest IS NULL OR upr.isGuest = :isGuest)"
	)
	List<CUserProjectRole> findByRoleType(@Param ("isAdmin") Boolean isAdmin, @Param ("isUser") Boolean isUser, @Param ("isGuest") Boolean isGuest);
	@Query ("SELECT upr FROM CUserProjectRole upr " + "WHERE upr.isAdmin = true")
	List<CUserProjectRole> findAdminRoles();
	@Query ("SELECT upr FROM CUserProjectRole upr " + "WHERE upr.isUser = true")
	List<CUserProjectRole> findUserRoles();
	@Query ("SELECT upr FROM CUserProjectRole upr " + "WHERE upr.isGuest = true")
	List<CUserProjectRole> findGuestRoles();
}
