package tech.derbent.projects.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.IAbstractNamedRepository;
import tech.derbent.projects.domain.CProject;

public interface IProjectRepository extends IAbstractNamedRepository<CProject> {

	/** Finds a project by ID and eagerly loads its user settings to prevent LazyInitializationException.
	 * @param projectId the ID of the project to fetch
	 * @return the project with eagerly loaded user settings, or empty if not found */
	@Query ("SELECT p FROM CProject p LEFT JOIN FETCH p.userSettings WHERE p.id = :projectId")
	Optional<CProject> findByIdWithUserSettings(@Param ("projectId") Long projectId);
	/** Finds all projects that are not assigned to a specific user.
	 * @param userId the ID of the user
	 * @return list of projects not assigned to the user */
	@Query ("SELECT p FROM CProject p WHERE p.id NOT IN (SELECT ups.project.id FROM CUserProjectSettings ups WHERE ups.user.id = :userId)")
	List<CProject> findProjectsNotAssignedToUser(@Param ("userId") Long userId);
}
