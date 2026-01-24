package tech.derbent.api.page.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CPageEntityService extends CEntityOfProjectService<CPageEntity> implements IEntityRegistrable, IEntityWithView {

	public CPageEntityService(final IPageEntityRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
		Check.notNull(repository, "CPageEntityRepository cannot be null");
		Check.notNull(clock, "Clock cannot be null");
		Check.notNull(sessionService, "CSessionService cannot be null");
	}

	@Override
	public String checkDeleteAllowed(final CPageEntity entity) {
		return super.checkDeleteAllowed(entity);
	}

	/** Find active pages by project. */
	public List<CPageEntity> findActivePagesByProject(CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return listByProject(project);
	}

	/** Find page by entity class name. This is used to navigate to entity-specific pages from generic contexts like Gantt charts.
	 * @param entityClassName the simple class name of the entity (e.g., "CActivity", "CMeeting")
	 * @return Optional containing the page entity if found */
	public Optional<CPageEntity> findByEntityClass(String entityClassName) {
		Check.notBlank(entityClassName, "Entity class name cannot be blank");
		// Map entity class name to service bean name (e.g., CActivity -> CActivityService)
		final String serviceBeanName = entityClassName + "Service";
		return findAll().stream().filter(page -> {
			if (page.getGridEntity() != null && page.getGridEntity().getDataServiceBeanName() != null) {
				return page.getGridEntity().getDataServiceBeanName().equals(serviceBeanName);
			}
			return false;
		}).findFirst();
	}

	/** Find page by route. */
	public Optional<CPageEntity> findByRoute(String route) {
		Check.notBlank(route, "Route cannot be blank");
		return findAll().stream().filter(page -> route.equals(page.getRoute())).findFirst();
	}

	/** Find root pages by project (pages with no parent). */
	public List<CPageEntity> findRootPagesByProject(CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return listByProject(project);
	}

	@Override
	public Class<CPageEntity> getEntityClass() { return CPageEntity.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CPageEntityInitializerService.class; }

	/** Get page hierarchy for project. */
	public List<CPageEntity> getPageHierarchyForProject(CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return listByProject(project);
	}

	@Override
	public Class<?> getPageServiceClass() { return CPageServicePageEntity.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	public List<CPageEntity> listQuickAccess(CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IPageEntityRepository) getRepository()).listQuickAccess(project);
	}
}
