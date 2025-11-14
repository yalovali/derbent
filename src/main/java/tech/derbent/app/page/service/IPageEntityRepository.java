package tech.derbent.app.page.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.app.projects.domain.CProject;

public interface IPageEntityRepository extends IProjectItemRespository<CPageEntity> {

	@Query ("SELECT p FROM CPageEntity p WHERE p.project = ?1 AND p.attributeShowInQuickToolbar = true ORDER BY p.menuOrder ASC")
	List<CPageEntity> listQuickAccess(CProject project);
}
