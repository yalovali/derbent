package tech.derbent.app.teams.team.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractNamedRepository;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.app.teams.team.domain.CTeam;
import tech.derbent.base.users.domain.CUser;

public interface ITeamRepository extends IAbstractNamedRepository<CTeam> {

	/** Find all teams for a specific company */
	@Query ("SELECT t FROM CTeam t WHERE t.company = :company ORDER BY t.name")
	List<CTeam> findByCompany(@Param ("company") CCompany company);
	/** Page view query with fetch joins for full entity graph */
	@Query ("SELECT DISTINCT t FROM CTeam t LEFT JOIN FETCH t.company LEFT JOIN FETCH t.teamManager LEFT JOIN FETCH t.members WHERE t.company = :company ORDER BY t.name")
	List<CTeam> listByCompanyForPageView(@Param ("company") CCompany company);
	@Override
	@Query ("SELECT t FROM CTeam t LEFT JOIN FETCH t.company LEFT JOIN FETCH t.teamManager LEFT JOIN FETCH t.members WHERE t.id = :id")
	Optional<CTeam> findById(@Param ("id") Long id);
	/** Find teams managed by a specific user */
	@Query ("SELECT t FROM CTeam t WHERE t.teamManager = :manager ORDER BY t.name")
	List<CTeam> findByManager(@Param ("manager") CUser manager);
	/** Find teams that include a specific user as a member */
	@Query ("SELECT t FROM CTeam t JOIN t.members m WHERE m = :user ORDER BY t.name")
	List<CTeam> findByMember(@Param ("user") CUser user);
}
