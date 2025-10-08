package tech.derbent.projects.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.IAbstractNamedRepository;
import tech.derbent.projects.domain.CProject;

public interface IProjectRepository extends IAbstractNamedRepository<CProject> {

	@Query (
		"SELECT p FROM CProject p WHERE p.id NOT IN (SELECT ups.project.id FROM CUserProjectSettings ups WHERE ups.user.id = :userId) and "
				+ " p.company.id = (SELECT u.company.id FROM CUser u WHERE u.id = :userId)"
	)
	List<CProject> findProjectsNotAssignedToUser(@Param ("userId") Long userId);
	@Query ("SELECT p FROM CProject p WHERE p.company.id = :companyId")
	List<CProject> findByCompanyId(@Param ("companyId") Long companyId);
	@Query ("SELECT p FROM CProject p WHERE p.company.id = :companyId")
	Page<CProject> findByCompanyId(@Param ("companyId") Long companyId, Pageable pageable);
}
