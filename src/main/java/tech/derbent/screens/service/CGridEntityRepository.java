package tech.derbent.screens.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.CEntityOfProjectRepository;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CGridEntity;

public interface CGridEntityRepository extends CEntityOfProjectRepository<CGridEntity> {

	@Query ("SELECT g FROM CGridEntity g WHERE g.project = :project AND g.name = :name")
	Optional<CGridEntity> findByNameAndProject(@Param ("project") CProject project, @Param ("name") String name);
}
