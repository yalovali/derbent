package tech.derbent.api.page.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.projects.domain.CProject;

public interface IPageEntityRepository extends IProjectItemRespository<CPageEntity> {

	@Query ("SELECT p FROM CPageEntity p WHERE p.project = ?1 AND p.attributeShowInQuickToolbar = true ORDER BY p.menuOrder ASC")
	List<CPageEntity> listQuickAccess(CProject<?> project);

	@Override
	@Query ("""
			SELECT p FROM CPageEntity p
			LEFT JOIN FETCH p.project
			LEFT JOIN FETCH p.assignedTo
			LEFT JOIN FETCH p.createdBy
			LEFT JOIN FETCH p.status
			LEFT JOIN FETCH p.detailSection
			LEFT JOIN FETCH p.gridEntity
			WHERE p.project = :project
			ORDER BY p.name ASC
			""")
	List<CPageEntity> listByProjectForPageView(@Param ("project") CProject<?> project);
}
