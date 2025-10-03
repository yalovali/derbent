package tech.derbent.api.roles.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.services.IAbstractNamedRepository;
import tech.derbent.companies.domain.CCompany;

/** IUserCompanyRoleRepository - Repository interface for CUserCompanyRole entity. Layer: Service (MVC) Provides data access operations for
 * company-aware user company roles with eager loading support. Uses super interface methods where available to maintain simplicity. */
public interface IUserCompanyRoleRepository extends IAbstractNamedRepository<CUserCompanyRole> {

	/** Find admin roles for the company.
	 * @return list of admin roles */
	@Query ("SELECT ucr FROM CUserCompanyRole ucr " + "WHERE ucr.isAdmin = true")
	List<CUserCompanyRole> findAdminRoles();
	/** Find roles by company.
	 * @param company the company
	 * @return list of roles for the company */
	@Query ("SELECT ucr FROM CUserCompanyRole ucr WHERE ucr.company = :company")
	List<CUserCompanyRole> findByCompany(@Param ("company") CCompany company);
	/** Find role by ID with all relationships eagerly loaded.
	 * @param id the role ID
	 * @return optional role with relationships */
	@Query ("SELECT ucr FROM CUserCompanyRole ucr " + "LEFT JOIN FETCH ucr.company " + "WHERE ucr.id = :id")
	Optional<CUserCompanyRole> findByIdWithRelationships(@Param ("id") Long id);
	/** Find roles that have read access to a specific page.
	 * @param pageName the page name
	 * @return list of roles with read access */
	@Query ("SELECT ucr FROM CUserCompanyRole ucr " + "JOIN ucr.readAccessPages rap " + "WHERE rap = :pageName")
	List<CUserCompanyRole> findByReadAccessPage(@Param ("pageName") String pageName);
	/** Find roles by role type boolean flags.
	 * @param isAdmin whether to find admin roles
	 * @param isUser  whether to find user roles
	 * @param isGuest whether to find guest roles
	 * @return list of matching roles */
	@Query (
		"SELECT ucr FROM CUserCompanyRole ucr " + "WHERE (:isAdmin IS NULL OR ucr.isAdmin = :isAdmin) "
				+ "AND (:isUser IS NULL OR ucr.isUser = :isUser) " + "AND (:isGuest IS NULL OR ucr.isGuest = :isGuest)"
	)
	List<CUserCompanyRole> findByRoleType(@Param ("isAdmin") Boolean isAdmin, @Param ("isUser") Boolean isUser, @Param ("isGuest") Boolean isGuest);
	/** Find roles that have write access to a specific page.
	 * @param pageName the page name
	 * @return list of roles with write access */
	@Query ("SELECT ucr FROM CUserCompanyRole ucr " + "JOIN ucr.writeAccessPages wap " + "WHERE wap = :pageName")
	List<CUserCompanyRole> findByWriteAccessPage(@Param ("pageName") String pageName);
	/** Find guest roles for the company.
	 * @return list of guest roles */
	@Query ("SELECT ucr FROM CUserCompanyRole ucr " + "WHERE ucr.isGuest = true")
	List<CUserCompanyRole> findGuestRoles();
	/** Find user roles for the company.
	 * @return list of user roles */
	@Query ("SELECT ucr FROM CUserCompanyRole ucr " + "WHERE ucr.isUser = true")
	List<CUserCompanyRole> findUserRoles();
}
