package tech.derbent.app.roles.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.roles.domain.CUserProjectRole;
import tech.derbent.app.projects.domain.CProject;

/** IUserProjectRoleRepository - Repository interface for CUserProjectRole entity. Layer: Service (MVC) Provides data access operations for
 * company-aware and project-aware user project roles with eager loading support. Uses super interface methods where available to maintain simplicity. */
public interface IUserProjectRoleRepository extends IEntityOfCompanyRepository<CUserProjectRole> {

	@Query (
		"SELECT upr FROM CUserProjectRole upr " + "LEFT JOIN FETCH upr.company " + "LEFT JOIN FETCH upr.project " 
				+ "WHERE upr.id = :id"
	)
	Optional<CUserProjectRole> findByIdWithRelationships(@Param ("id") Long id);
	@Query (
		"SELECT upr FROM CUserProjectRole upr " + "WHERE (:isAdmin IS NULL OR upr.isAdmin = :isAdmin) "
				+ "AND (:isUser IS NULL OR upr.isUser = :isUser) " + "AND (:isGuest IS NULL OR upr.isGuest = :isGuest) ORDER BY upr.name ASC"
	)
	List<CUserProjectRole> findByRoleType(@Param ("isAdmin") Boolean isAdmin, @Param ("isUser") Boolean isUser, @Param ("isGuest") Boolean isGuest);
	@Query ("SELECT upr FROM CUserProjectRole upr " + "WHERE upr.isAdmin = true ORDER BY upr.name ASC")
	List<CUserProjectRole> findAdminRoles();
	@Query ("SELECT upr FROM CUserProjectRole upr " + "WHERE upr.isUser = true ORDER BY upr.name ASC")
	List<CUserProjectRole> findUserRoles();
	@Query ("SELECT upr FROM CUserProjectRole upr " + "WHERE upr.isGuest = true ORDER BY upr.name ASC")
	List<CUserProjectRole> findGuestRoles();

	@Query ("""
			SELECT upr FROM CUserProjectRole upr
			LEFT JOIN FETCH upr.company
			LEFT JOIN FETCH upr.project
			WHERE upr.project = :project
			ORDER BY upr.name ASC
			""")
	List<CUserProjectRole> listByProjectForPageView(@Param ("project") CProject project);
}
