package tech.derbent.projects.service;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractNamedRepository;
import tech.derbent.projects.domain.CProject;

public interface CProjectRepository extends CAbstractNamedRepository<CProject> {
    /**
     * Finds a project by ID and eagerly loads its user settings to prevent LazyInitializationException.
     *
     * @param projectId
     *            the ID of the project to fetch
     * @return the project with eagerly loaded user settings, or empty if not found
     */
    @Query("SELECT p FROM CProject p LEFT JOIN FETCH p.userSettings WHERE p.id = :projectId")
    Optional<CProject> findByIdWithUserSettings(@Param("projectId") Long projectId);
}