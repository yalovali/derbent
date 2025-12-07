package tech.derbent.app.roles.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.app.roles.domain.CUserCompanyRole;
import tech.derbent.api.entity.service.IAbstractNamedRepository;
import tech.derbent.app.companies.domain.CCompany;

/** IUserCompanyRoleRepository - Repository interface for CUserCompanyRole entity. Layer: Service (MVC) Provides data access operations for
 * company-aware user company roles with eager loading support. Uses super interface methods where available to maintain simplicity. */
public interface IUserCompanyRoleRepository extends IAbstractNamedRepository<CUserCompanyRole> {

	@Query ("SELECT ucr FROM CUserCompanyRole ucr WHERE ucr.isAdmin = true ORDER BY ucr.name ASC")
	List<CUserCompanyRole> findAdminRoles();
	@Query ("SELECT ucr FROM CUserCompanyRole ucr LEFT JOIN company WHERE ucr.company = :company ORDER BY ucr.name ASC")
	List<CUserCompanyRole> findByCompany(@Param ("company") CCompany company);
	@Query ("SELECT ucr FROM CUserCompanyRole ucr LEFT JOIN company WHERE ucr.company = :company ORDER BY ucr.name ASC")
	Page<CUserCompanyRole> listByCompany(@Param ("company") CCompany company, Pageable pageable);
	@Query ("SELECT ucr FROM CUserCompanyRole ucr LEFT JOIN FETCH ucr.company WHERE ucr.id = :id")
	Optional<CUserCompanyRole> findByIdWithRelationships(@Param ("id") Long id);
	@Query (
		"SELECT ucr FROM CUserCompanyRole ucr WHERE (:isAdmin IS NULL OR ucr.isAdmin = :isAdmin) "
				+ "AND (:isUser IS NULL OR ucr.isUser = :isUser) AND (:isGuest IS NULL OR ucr.isGuest = :isGuest) ORDER BY ucr.name ASC"
	)
	List<CUserCompanyRole> findByRoleType(@Param ("isAdmin") Boolean isAdmin, @Param ("isUser") Boolean isUser, @Param ("isGuest") Boolean isGuest);
	@Query ("SELECT ucr FROM CUserCompanyRole ucr WHERE ucr.isGuest = true ORDER BY ucr.name ASC")
	List<CUserCompanyRole> findGuestRoles();
	@Query ("SELECT ucr FROM CUserCompanyRole ucr WHERE ucr.isUser = true ORDER BY ucr.name ASC")
	List<CUserCompanyRole> findUserRoles();
}
