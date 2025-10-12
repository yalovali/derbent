package tech.derbent.page.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.domains.CProjectItemService;
import tech.derbent.api.utils.Check;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CPageEntityService extends CProjectItemService<CPageEntity> {

	public CPageEntityService(final IPageEntityRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
		Check.notNull(repository, "CPageEntityRepository cannot be null");
		Check.notNull(clock, "Clock cannot be null");
		Check.notNull(sessionService, "CSessionService cannot be null");
	}

	/** Find active pages by project. */
	public List<CPageEntity> findActivePagesByProject(CProject project) {
		Check.notNull(project, "Project cannot be null");
		return listByProject(project);
	}

	/** Find child pages by parent page. */
	public List<CPageEntity> findByParentPage(CPageEntity parentPage) {
		Check.notNull(parentPage, "Parent page cannot be null");
		// For now, return empty list as hierarchy is not fully implemented
		return List.of();
	}

	/** Find page by route. */
	public Optional<CPageEntity> findByRoute(String route) {
		Check.notBlank(route, "Route cannot be blank");
		return findAll().stream().filter(page -> route.equals(page.getRoute())).findFirst();
	}

	/** Find root pages by project (pages with no parent). */
	public List<CPageEntity> findRootPagesByProject(CProject project) {
		Check.notNull(project, "Project cannot be null");
		return listByProject(project);
	}

	@Override
	protected Class<CPageEntity> getEntityClass() { return CPageEntity.class; }

	@Override
	public String checkDependencies(final CPageEntity entity) {
		final String superCheck = super.checkDependencies(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final CPageEntity entity) {
		super.initializeNewEntity(entity);
		tech.derbent.api.utils.Check.notNull(entity, "Entity cannot be null");
		// Stub for future implementation
	}

	/** Get page hierarchy for project. */
	public List<CPageEntity> getPageHierarchyForProject(CProject project) {
		Check.notNull(project, "Project cannot be null");
		return listByProject(project);
	}

	public List<CPageEntity> listQuickAccess(CProject project) {
		Check.notNull(project, "Project cannot be null");
		return ((IPageEntityRepository) getRepository()).listQuickAccess(project);
	}
}
