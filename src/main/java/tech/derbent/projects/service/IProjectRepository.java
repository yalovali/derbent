package tech.derbent.projects.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.IAbstractNamedRepository;
import tech.derbent.projects.domain.CProject;

public interface IProjectRepository extends IAbstractNamedRepository<CProject> {

	/** Finds all projects that are not assigned to a specific user.
	 * @param userId the ID of the user
	 * @return list of projects not assigned to the user */
	@Query ("SELECT p FROM CProject p WHERE p.id NOT IN (SELECT ups.project.id FROM CUserProjectSettings ups WHERE ups.user.id = :userId)")
	List<CProject> findProjectsNotAssignedToUser(@Param ("userId") Long userId);
	/** Finds all projects by company ID.
	 * @param companyId the ID of the company
	 * @return list of projects for the company */
	@Query ("SELECT p FROM CProject p WHERE p.company.id = :companyId")
	List<CProject> findByCompanyId(@Param ("companyId") Long companyId);
}
