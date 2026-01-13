package tech.derbent.app.teams.team.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.app.teams.team.domain.CTeam;
import tech.derbent.base.users.domain.CUser;

public interface ITeamRepository extends IEntityOfCompanyRepository<CTeam> {

	@Override
	@Query ("SELECT t FROM #{#entityName} t WHERE t.company = :company ORDER BY t.name ASC")
	List<CTeam> findByCompany(@Param ("company") CCompany company);

	@Override
	@Query ("SELECT DISTINCT t FROM #{#entityName} t " +
		"LEFT JOIN FETCH t.company " +
		"LEFT JOIN FETCH t.teamManager " +
		"WHERE t.company = :company " +
		"ORDER BY t.name ASC")
	List<CTeam> listByCompanyForPageView(@Param ("company") CCompany company);

	@Override
	@Query ("SELECT t FROM #{#entityName} t " +
		"LEFT JOIN FETCH t.company " +
		"LEFT JOIN FETCH t.teamManager " +
		"WHERE t.id = :id")
	Optional<CTeam> findById(@Param ("id") Long id);

	@Query ("SELECT t FROM #{#entityName} t WHERE t.teamManager = :manager ORDER BY t.name ASC")
	List<CTeam> findByManager(@Param ("manager") CUser manager);

	@Query ("SELECT DISTINCT t FROM #{#entityName} t JOIN t.members m WHERE m = :user ORDER BY t.name ASC")
	List<CTeam> findByMember(@Param ("user") CUser user);
}
