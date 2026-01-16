package tech.derbent.app.components.componentversion.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.components.componentversion.domain.CProjectComponentVersion;
import tech.derbent.app.components.componentversiontype.domain.CProjectComponentVersionType;
import tech.derbent.api.projects.domain.CProject;

public interface IProjectComponentVersionRepository extends IEntityOfProjectRepository<CProjectComponentVersion> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
	long countByType(@Param ("entityType") CProjectComponentVersionType type);
	@Override
	@Query ("""
			SELECT r FROM CProjectComponentVersion r
			LEFT JOIN FETCH r.project
			LEFT JOIN FETCH r.assignedTo
			LEFT JOIN FETCH r.createdBy
			LEFT JOIN FETCH r.status
			LEFT JOIN FETCH r.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH r.projectComponent
			LEFT JOIN FETCH r.attachments
			LEFT JOIN FETCH r.comments
			WHERE r.id = :id
			""")
	Optional<CProjectComponentVersion> findById(@Param ("id") Long id);

	@Override
	@Query ("""
			SELECT r FROM CProjectComponentVersion r
			LEFT JOIN FETCH r.project
			LEFT JOIN FETCH r.assignedTo
			LEFT JOIN FETCH r.createdBy
			LEFT JOIN FETCH r.status
			LEFT JOIN FETCH r.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH r.projectComponent
			LEFT JOIN FETCH r.attachments
			LEFT JOIN FETCH r.comments
			WHERE r.project = :project
			ORDER BY r.name ASC
			""")
	List<CProjectComponentVersion> listByProjectForPageView(@Param ("project") CProject project);
}
