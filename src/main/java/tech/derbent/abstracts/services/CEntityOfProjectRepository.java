package tech.derbent.abstracts.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;

/**
 * CEntityOfProjectRepository - Base repository interface for entities that extend
 * CEntityOfProject. Layer: Service (MVC) - Repository interface Provides common query
 * methods for project-aware entities with proper lazy loading to prevent
 * LazyInitializationException.
 */
@NoRepositoryBean
public interface CEntityOfProjectRepository<
	EntityClass extends CEntityOfProject<EntityClass>>
	extends CAbstractNamedRepository<EntityClass> {

	/**
	 * Counts the number of entities for a specific project.
	 * @param project the project
	 * @return count of entities for the project
	 */
	long countByProject(CProject project);
	/**
	 * Finds an entity by ID with eagerly loaded project relationship to prevent
	 * LazyInitializationException. This base implementation loads common CEntityOfProject
	 * relationships.
	 * @param id the entity ID
	 * @return optional entity with loaded relationships
	 */
	@Query (
		"SELECT e FROM #{#entityName} e " + "LEFT JOIN FETCH e.project "
			+ "LEFT JOIN FETCH e.assignedTo " + "LEFT JOIN FETCH e.createdBy "
			+ "WHERE e.id = :id"
	)
	Optional<EntityClass> findByIdWithProjectRelationships(@Param ("id") Long id);
	/**
	 * Finds entities by project with eagerly loaded project relationship to prevent
	 * LazyInitializationException. This is the base implementation that all
	 * CEntityOfProject repositories should use unless they need additional relationships
	 * loaded.
	 * @param project the project
	 * @return list of entities with loaded project relationship
	 */
	@Query (
		"SELECT e FROM #{#entityName} e " + "LEFT JOIN FETCH e.project "
			+ "LEFT JOIN FETCH e.assignedTo " + "LEFT JOIN FETCH e.createdBy "
			+ "WHERE e.project = :project"
	)
	List<EntityClass> findByProject(@Param ("project") CProject project);
	/**
	 * Finds entities by project with eagerly loaded project relationship and pagination.
	 * @param project  the project
	 * @param pageable pagination information
	 * @return page of entities with loaded project relationship
	 */
	@Query (
		"SELECT e FROM #{#entityName} e " + "LEFT JOIN FETCH e.project "
			+ "LEFT JOIN FETCH e.assignedTo " + "LEFT JOIN FETCH e.createdBy "
			+ "WHERE e.project = :project"
	)
	List<EntityClass> findByProject(@Param ("project") CProject project,
		Pageable pageable);
}