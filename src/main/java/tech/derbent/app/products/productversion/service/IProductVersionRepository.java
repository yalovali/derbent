package tech.derbent.app.products.productversion.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.products.productversion.domain.CProductVersion;
import tech.derbent.app.products.productversiontype.domain.CProductVersionType;
import tech.derbent.api.projects.domain.CProject;

public interface IProductVersionRepository extends IEntityOfProjectRepository<CProductVersion> {

@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
long countByType(@Param ("entityType") CProductVersionType type);

@Override
@Query ("""
		SELECT r FROM CProductVersion r
		LEFT JOIN FETCH r.project
		LEFT JOIN FETCH r.assignedTo
		LEFT JOIN FETCH r.createdBy
		LEFT JOIN FETCH r.status
		LEFT JOIN FETCH r.entityType et
		LEFT JOIN FETCH et.workflow
		LEFT JOIN FETCH r.product
		LEFT JOIN FETCH r.attachments
		LEFT JOIN FETCH r.comments
		WHERE r.id = :id
		""")
Optional<CProductVersion> findById(@Param ("id") Long id);

@Override
@Query ("""
		SELECT r FROM CProductVersion r
		LEFT JOIN FETCH r.project
		LEFT JOIN FETCH r.assignedTo
		LEFT JOIN FETCH r.createdBy
		LEFT JOIN FETCH r.status
		LEFT JOIN FETCH r.entityType et
		LEFT JOIN FETCH et.workflow
		LEFT JOIN FETCH r.product
		LEFT JOIN FETCH r.attachments
		LEFT JOIN FETCH r.comments
		WHERE r.project = :project
		ORDER BY r.name ASC
		""")
List<CProductVersion> listByProjectForPageView(@Param ("project") CProject project);
}
