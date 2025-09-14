package tech.derbent.page.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.abstracts.services.CProjectItemRespository;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.projects.domain.CProject;

public interface CPageEntityRepository extends CProjectItemRespository<CPageEntity> {

	/** Find all active pages for a project, ordered by menu order */
	@Query ("SELECT p FROM CPageEntity p WHERE p.project = :project AND p.isActive = true ORDER BY p.menuOrder, p.pageTitle")
	List<CPageEntity> findActivePagesByProjectOrderByMenuOrder(@Param ("project") CProject project);

	/** Find page by route */
	@Query ("SELECT p FROM CPageEntity p WHERE p.route = :route AND p.isActive = true")
	Optional<CPageEntity> findByRoute(@Param ("route") String route);

	/** Find pages by parent page */
	@Query ("SELECT p FROM CPageEntity p WHERE p.parentPage = :parentPage AND p.isActive = true ORDER BY p.menuOrder, p.pageTitle")
	List<CPageEntity> findByParentPageOrderByMenuOrder(@Param ("parentPage") CPageEntity parentPage);

	/** Find root pages (no parent) for a project */
	@Query ("SELECT p FROM CPageEntity p WHERE p.project = :project AND p.parentPage IS NULL AND p.isActive = true ORDER BY p.menuOrder, p.pageTitle")
	List<CPageEntity> findRootPagesByProjectOrderByMenuOrder(@Param ("project") CProject project);

	/** Check if route exists for project (excluding specific page) */
	@Query ("SELECT COUNT(p) > 0 FROM CPageEntity p WHERE p.project = :project AND p.route = :route AND p.id != :excludeId")
	boolean existsByProjectAndRouteExcludingId(@Param ("project") CProject project, @Param ("route") String route, @Param ("excludeId") Long excludeId);
}
