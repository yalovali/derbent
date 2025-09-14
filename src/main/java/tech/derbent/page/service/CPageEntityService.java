package tech.derbent.page.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.domains.CProjectItemService;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CPageEntityService extends CProjectItemService<CPageEntity> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageEntityService.class);
	private final CPageEntityRepository pageRepository;

	public CPageEntityService(final CPageEntityRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
		this.pageRepository = repository;
	}

	/** Find all active pages for the current project, ordered by menu order */
	@Cacheable (value = "project-pages", key = "#project.id")
	public List<CPageEntity> findActivePagesByProject(final CProject project) {
		LOGGER.debug("Finding active pages for project: {}", project.getName());
		return pageRepository.findActivePagesByProjectOrderByMenuOrder(project);
	}

	/** Find page by route */
	@Cacheable (value = "page-by-route", key = "#route")
	public Optional<CPageEntity> findByRoute(final String route) {
		LOGGER.debug("Finding page by route: {}", route);
		return pageRepository.findByRoute(route);
	}

	/** Find pages by parent page */
	@Cacheable (value = "child-pages", key = "#parentPage.id")
	public List<CPageEntity> findByParentPage(final CPageEntity parentPage) {
		LOGGER.debug("Finding child pages for parent: {}", parentPage.getPageTitle());
		return pageRepository.findByParentPageOrderByMenuOrder(parentPage);
	}

	/** Find root pages (no parent) for a project */
	@Cacheable (value = "root-pages", key = "#project.id")
	public List<CPageEntity> findRootPagesByProject(final CProject project) {
		LOGGER.debug("Finding root pages for project: {}", project.getName());
		return pageRepository.findRootPagesByProjectOrderByMenuOrder(project);
	}

	/** Get hierarchical page structure for menu generation */
	@Cacheable (value = "page-hierarchy", key = "#project.id")
	public List<CPageEntity> getPageHierarchyForProject(final CProject project) {
		LOGGER.debug("Building page hierarchy for project: {}", project.getName());
		// For menu purposes, we return all active pages for the project
		// The hierarchy is maintained through the title field (e.g., "Project.Overview")
		return findActivePagesByProject(project);
	}

	@Override
	@Transactional
	@CacheEvict (value = { "project-pages", "page-by-route", "child-pages", "root-pages", "page-hierarchy" }, allEntries = true)
	public CPageEntity save(final CPageEntity entity) {
		validatePage(entity);
		LOGGER.info("Saving page: {} for project: {}", entity.getPageTitle(), entity.getProject().getName());
		return super.save(entity);
	}

	/** Validate page entity before saving */
	private void validatePage(final CPageEntity page) {
		if (page.getRoute() == null || page.getRoute().trim().isEmpty()) {
			throw new IllegalArgumentException("Page route cannot be empty");
		}
		if (page.getPageTitle() == null || page.getPageTitle().trim().isEmpty()) {
			throw new IllegalArgumentException("Page title cannot be empty");
		}
		// Check for duplicate routes within the same project
		if (page.getProject() != null) {
			Long excludeId = page.getId() != null ? page.getId() : -1L;
			if (pageRepository.existsByProjectAndRouteExcludingId(page.getProject(), page.getRoute(), excludeId)) {
				throw new IllegalArgumentException("Route '" + page.getRoute() + "' already exists in this project");
			}
		}
	}

	@Override
	protected Class<CPageEntity> getEntityClass() { return CPageEntity.class; }
}
