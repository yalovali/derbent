package tech.derbent.users.service;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractNamedRepository;
import tech.derbent.users.domain.CUser;

public interface CUserRepository extends CAbstractNamedRepository<CUser> {

	@Query (
		"SELECT COUNT(DISTINCT u) FROM CUser u " + "LEFT JOIN u.projectSettings ps "
			+ "LEFT JOIN u.userType ut " + "LEFT JOIN u.company c "
			+ "WHERE ps.project.id = :projectId"
	)
	long countUsersByProjectId(@Param ("projectId") Long projectId);
	@Query (
		"SELECT u FROM CUser u " + "LEFT JOIN FETCH u.projectSettings ps "
			+ "LEFT JOIN FETCH ps.project " + "LEFT JOIN FETCH u.userType "
			+ "LEFT JOIN FETCH u.company " + "WHERE u.id = :userId"
	)
	Optional<CUser> findByIdWithUserSettings(@Param ("userId") Long id);
	@Query (
		"SELECT u FROM CUser u " + "LEFT JOIN FETCH u.userType "
			+ "LEFT JOIN FETCH u.company " + "LEFT JOIN FETCH u.projectSettings ps "
			+ "LEFT JOIN FETCH ps.project " + "WHERE u.login = :username"
	)
	Optional<CUser> findByUsername(@Param ("username") String username);
}