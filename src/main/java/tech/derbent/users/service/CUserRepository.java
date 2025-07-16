package tech.derbent.users.service;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.users.domain.CUser;

public interface CUserRepository extends CAbstractRepository<CUser> {

	@Query("SELECT u FROM CUser u LEFT JOIN FETCH u.projects WHERE u.id = :id")
	Optional<CUser> findByIdWithProjects(@Param("id") Long id);
}
