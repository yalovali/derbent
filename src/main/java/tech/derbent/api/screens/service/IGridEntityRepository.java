package tech.derbent.api.screens.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.screens.domain.CGridEntity;

public interface IGridEntityRepository extends IEntityOfProjectRepository<CGridEntity> {

	@Query ("SELECT g FROM CGridEntity g WHERE g.project = :project AND g.name = :name")
	Optional<CGridEntity> findByNameAndProject(@Param ("project") CProject project, @Param ("name") String name);
}
