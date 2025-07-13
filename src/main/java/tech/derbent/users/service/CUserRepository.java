package tech.derbent.users.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import tech.derbent.users.domain.CUser;

public interface CUserRepository extends JpaRepository<CUser, Long>, JpaSpecificationExecutor<CUser> {

	// If you don't need a total row count, Slice is better than Page as it only
	// performs a select query. Page performs both a select and a count query.
	Slice<CUser> findAllBy(Pageable pageable);
}
