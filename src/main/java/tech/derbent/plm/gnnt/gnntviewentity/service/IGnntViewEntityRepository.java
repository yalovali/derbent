package tech.derbent.plm.gnnt.gnntviewentity.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntViewEntity;

@Repository
public interface IGnntViewEntityRepository extends IEntityOfProjectRepository<CGnntViewEntity> {

	@Override
	@Query("""
			SELECT g FROM #{#entityName} g
			LEFT JOIN FETCH g.project
			LEFT JOIN FETCH g.assignedTo
			LEFT JOIN FETCH g.createdBy
			WHERE g.project = :project
			ORDER BY g.name ASC
			""")
	List<CGnntViewEntity> listByProjectForPageView(@Param("project") CProject<?> project);
}
