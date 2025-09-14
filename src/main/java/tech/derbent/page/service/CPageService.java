package tech.derbent.page.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinService;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.view.CDynamicPageView;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/**
 * Service for managing dynamic pages and their integration with the menu system.
 * Handles both lazy and eager loading strategies for database-defined pages.
 */
@Service
public class CPageService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageService.class);
	
	private final CPageEntityService pageEntityService;
	private final CSessionService sessionService;
	
	// Cache for created page instances (used in eager loading mode)
	private final ConcurrentMap<String, Component> pageInstanceCache = new ConcurrentHashMap<>();
	
	// Configuration for loading strategy
	private final boolean eagerLoadingEnabled = Boolean.parseBoolean(
			System.getProperty("derbent.pages.eager-loading", "false"));

	public CPageService(final CPageEntityService pageEntityService, final CSessionService sessionService) {
		this.pageEntityService = pageEntityService;
		this.sessionService = sessionService;
		LOGGER.info("CPageService initialized with {} loading strategy", 
				eagerLoadingEnabled ? "eager" : "lazy");
	}

	/**
	 * Get all database-defined pages for the current project to populate the menu.
	 * This method is used by MainLayout to generate dynamic menu items.
	 */
	@Cacheable(value = "menu-pages", key = "#project.id")
	public List<CPageEntity> getMenuPagesForProject(final CProject project) {
		LOGGER.debug("Getting menu pages for project: {}", project.getName());
		
		List<CPageEntity> pages = pageEntityService.findActivePagesByProject(project);
		
		if (eagerLoadingEnabled) {
			// Pre-create page instances for better performance
			preCreatePageInstances(pages);
		}
		
		// Register routes dynamically if not already registered
		registerDynamicRoutes(pages);
		
		return pages;
	}

	/**
	 * Create or retrieve a page instance for the given route.
	 * Used when a user navigates to a database-defined page.
	 */
	public Optional<Component> getPageInstance(final String route) {
		LOGGER.debug("Getting page instance for route: {}", route);
		
		// Check cache first (for eager loading)
		if (pageInstanceCache.containsKey(route)) {
			LOGGER.debug("Returning cached page instance for route: {}", route);
			return Optional.of(pageInstanceCache.get(route));
		}
		
		// Find page entity by route
		Optional<CPageEntity> pageEntity = pageEntityService.findByRoute(route);
		if (pageEntity.isEmpty()) {
			LOGGER.warn("No page entity found for route: {}", route);
			return Optional.empty();
		}
		
		// Create page instance (lazy loading)
		Component pageInstance = createPageInstance(pageEntity.get());
		
		// Cache the instance if eager loading is enabled
		if (eagerLoadingEnabled) {
			pageInstanceCache.put(route, pageInstance);
		}
		
		return Optional.of(pageInstance);
	}

	/**
	 * Get hierarchical page structure for a project.
	 * Used for building nested menu structures.
	 */
	public List<CPageEntity> getPageHierarchy(final CProject project) {
		return pageEntityService.getPageHierarchyForProject(project);
	}

	/**
	 * Get pages that have no parent (root level pages).
	 */
	public List<CPageEntity> getRootPages(final CProject project) {
		return pageEntityService.findRootPagesByProject(project);
	}

	/**
	 * Get child pages for a given parent page.
	 */
	public List<CPageEntity> getChildPages(final CPageEntity parentPage) {
		return pageEntityService.findByParentPage(parentPage);
	}

	/**
	 * Check if eager loading is enabled.
	 */
	public boolean isEagerLoadingEnabled() {
		return eagerLoadingEnabled;
	}

	/**
	 * Get current cache size (for monitoring).
	 */
	public int getCacheSize() {
		return pageInstanceCache.size();
	}

	/**
	 * Clear the page instance cache.
	 */
	public void clearCache() {
		LOGGER.info("Clearing page instance cache, size: {}", pageInstanceCache.size());
		pageInstanceCache.clear();
	}

	/**
	 * Pre-create page instances for eager loading.
	 */
	private void preCreatePageInstances(final List<CPageEntity> pages) {
		LOGGER.debug("Pre-creating {} page instances for eager loading", pages.size());
		
		for (CPageEntity page : pages) {
			if (!pageInstanceCache.containsKey(page.getRoute())) {
				try {
					Component pageInstance = createPageInstance(page);
					pageInstanceCache.put(page.getRoute(), pageInstance);
					LOGGER.debug("Pre-created page instance for route: {}", page.getRoute());
				} catch (Exception e) {
					LOGGER.error("Failed to pre-create page instance for route: {}", page.getRoute(), e);
				}
			}
		}
	}

	/**
	 * Create a page instance for the given page entity.
	 */
	private Component createPageInstance(final CPageEntity pageEntity) {
		LOGGER.debug("Creating page instance for: {}", pageEntity.getPageTitle());
		
		// Create a dynamic page view with the page entity
		CDynamicPageView pageView = new CDynamicPageView(pageEntity, sessionService);
		
		return pageView;
	}

	/**
	 * Register dynamic routes for database-defined pages.
	 */
	private void registerDynamicRoutes(final List<CPageEntity> pages) {
		// Note: In a production system, route registration should be done carefully
		// to avoid conflicts. For this implementation, we'll handle it in the view layer.
		LOGGER.debug("Dynamic route registration delegated to view layer for {} pages", pages.size());
	}
}