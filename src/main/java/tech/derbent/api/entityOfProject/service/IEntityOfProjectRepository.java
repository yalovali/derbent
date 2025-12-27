package tech.derbent.api.entityOfProject.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractNamedRepository;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.app.projects.domain.CProject;

@NoRepositoryBean
public interface IEntityOfProjectRepository<EntityClass extends CEntityOfProject<EntityClass>> extends IAbstractNamedRepository<EntityClass> {

	@Query ("SELECT COUNT(s) FROM #{#entityName} s WHERE s.project = :project")
	long countByProject(@Param ("project") CProject project);
	@Query ("SELECT COUNT(s) > 0 FROM #{#entityName} s WHERE LOWER(s.name) = LOWER(:name) AND s.project = :project")
	boolean existsByNameProject(@Param ("name") String name, @Param ("project") CProject project);
	@Query ("SELECT s FROM #{#entityName} s WHERE LOWER(s.name) = LOWER(:name) AND s.project = :project")
	Optional<EntityClass> findByNameAndProject(@Param ("name") String name, @Param ("project") CProject project);
	@Query ("SELECT e FROM #{#entityName} e WHERE e.project = :project ORDER BY e.name ASC")
	List<EntityClass> listByProject(@Param ("project") CProject project);
	@Query ("SELECT e FROM #{#entityName} e WHERE e.project = :project ORDER BY e.name ASC")
	Page<EntityClass> listByProject(@Param ("project") CProject project, Pageable pageable);
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.assignedTo
			LEFT JOIN FETCH e.createdBy
			WHERE e.project = :project
			ORDER BY e.name ASC
			""")
	List<EntityClass> listByProjectForPageView(@Param ("project") CProject project);
	@Query ("SELECT e FROM #{#entityName} e WHERE e.project.id = :pid ORDER BY e.name ASC")
	List<EntityClass> listByProjectId(@Param ("pid") Long pid);
}
