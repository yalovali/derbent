package tech.derbent.api.services;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;

/** CEntityOfProjectRepository - Base repository interface for entities that extend CEntityOfProject. Layer: Service (MVC) - Repository interface
 * Provides common query methods for project-aware entities using standard pagination patterns. */
@NoRepositoryBean
public interface IEntityOfProjectRepository<EntityClass extends CEntityOfProject<EntityClass>> extends IAbstractNamedRepository<EntityClass> {

	@Query ("SELECT COUNT(s) FROM #{#entityName} s WHERE s.project = :project")
	long countByProject(@Param ("project") CProject project);
	@Query ("SELECT COUNT(s) > 0 FROM #{#entityName} s WHERE LOWER(s.name) = LOWER(:name) AND s.project = :project")
	boolean existsByNameProject(@Param ("name") String name, @Param ("project") CProject project);
	@Query ("SELECT s FROM #{#entityName} s WHERE LOWER(s.name) = LOWER(:name) AND s.project = :project")
	Optional<EntityClass> findByNameAndProject(@Param ("name") String name, @Param ("project") CProject project);
	@Query ("SELECT e FROM #{#entityName} e WHERE e.project = :project")
	List<EntityClass> listByProject(@Param ("project") CProject project);
	@Query ("SELECT e FROM #{#entityName} e WHERE e.project.id = :pid")
	List<EntityClass> listByProjectId(@Param ("pid") Long pid);
	@Query ("SELECT e FROM #{#entityName} e WHERE e.project = :project")
	Page<EntityClass> listByProject(@Param ("project") CProject project, Pageable pageable);
}
