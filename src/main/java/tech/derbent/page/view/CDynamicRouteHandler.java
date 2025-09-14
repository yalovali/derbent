package tech.derbent.page.view;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.service.CPageService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/** Route handler for dynamic pages defined in the database. This component manages the routing for CPageEntity instances. */
@SpringComponent
@UIScope
public class CDynamicRouteHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicRouteHandler.class);
	private final CPageService pageService;
	private final CSessionService sessionService;

	@Autowired
	public CDynamicRouteHandler(CPageService pageService, CSessionService sessionService) {
		this.pageService = pageService;
		this.sessionService = sessionService;
		LOGGER.info("CDynamicRouteHandler initialized");
	}

	/** Get component for the given route if it's a dynamic page. */
	public Optional<Component> getComponentForRoute(String route) {
		LOGGER.debug("Checking for dynamic page component for route: {}", route);
		return pageService.getPageInstance(route);
	}

	/** Get all dynamic pages for the current project. */
	public List<CPageEntity> getDynamicPagesForCurrentProject() {
		Optional<CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isEmpty()) {
			LOGGER.debug("No active project, returning empty dynamic pages list");
			return List.of();
		}
		return pageService.getMenuPagesForProject(activeProject.get());
	}

	/** Check if a route is handled by a dynamic page. */
	public boolean isHandledByDynamicPage(String route) {
		return pageService.getPageInstance(route).isPresent();
	}

	/** Register all dynamic routes for the current project. This is called during application initialization. */
	public void registerDynamicRoutes() {
		Optional<CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isEmpty()) {
			LOGGER.debug("No active project, skipping dynamic route registration");
			return;
		}
		List<CPageEntity> pages = pageService.getMenuPagesForProject(activeProject.get());
		LOGGER.info("Registering {} dynamic routes for project: {}", pages.size(), activeProject.get().getName());
		for (CPageEntity page : pages) {
			try {
				registerDynamicRoute(page);
			} catch (Exception e) {
				LOGGER.error("Failed to register dynamic route for page: {}", page.getPageTitle(), e);
			}
		}
	}

	/** Register a single dynamic route. */
	private void registerDynamicRoute(CPageEntity page) {
		String route = page.getRoute();
		LOGGER.debug("Registering dynamic route: {} for page: {}", route, page.getPageTitle());
		// Note: In a production system, you would need to handle dynamic route registration
		// more carefully. For this implementation, routes are handled at the view level.
		// The actual route registration happens when users navigate to the routes.
	}

	/** Get cache information for monitoring. */
	public String getCacheInfo() {
		return String.format("Dynamic pages cache size: %d, Eager loading: %s", pageService.getCacheSize(), pageService.isEagerLoadingEnabled());
	}
}
