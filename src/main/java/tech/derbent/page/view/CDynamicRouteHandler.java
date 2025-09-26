package tech.derbent.page.view;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import tech.derbent.api.utils.Check;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/** Route handler for dynamic pages defined in the database. This component manages the routing for CPageEntity instances. */
@SpringComponent
@UIScope
public class CDynamicRouteHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicRouteHandler.class);
	private final CPageEntityService pageEntityService;
	private final CSessionService sessionService;

	@Autowired
	public CDynamicRouteHandler(CPageEntityService pageEntityService, CSessionService sessionService) {
		Check.notNull(pageEntityService, "CPageEntityService cannot be null");
		Check.notNull(sessionService, "CSessionService cannot be null");
		this.pageEntityService = pageEntityService;
		this.sessionService = sessionService;
		LOGGER.info("CDynamicRouteHandler initialized");
	}

	/** Get cache information for monitoring. */
	public String getCacheInfo() { return "Dynamic pages handled directly by CPageEntityService"; }

	/** Get component for the given route if it's a dynamic page. */
	public Optional<Component> getComponentForRoute(String route) {
		Check.notBlank(route, "Route cannot be blank");
		LOGGER.debug("Checking for dynamic page component for route: {}", route);
		Optional<CPageEntity> pageEntity = pageEntityService.findByRoute(route);
		if (pageEntity.isEmpty()) {
			return Optional.empty();
		}
		// Create page instance
		CDynamicPageView pageView = new CDynamicPageView(pageEntity.get(), sessionService);
		return Optional.of(pageView);
	}

	/** Get all dynamic pages for the current project. */
	public List<CPageEntity> getDynamicPagesForCurrentProject() {
		CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for dynamic pages"));
		return pageEntityService.findActivePagesByProject(activeProject);
	}

	/** Check if a route is handled by a dynamic page. */
	public boolean isHandledByDynamicPage(String route) {
		Check.notBlank(route, "Route cannot be blank");
		return pageEntityService.findByRoute(route).isPresent();
	}

	/** Register a single dynamic route. */
	private void registerDynamicRoute(CPageEntity page) {
		Check.notNull(page, "Page entity cannot be null");
		String route = page.getRoute();
		Check.notBlank(route, "Page route cannot be blank");
		LOGGER.debug("Registering dynamic route: {} for page: {}", route, page.getPageTitle());
		// Note: In a production system, you would need to handle dynamic route registration
		// more carefully. For this implementation, routes are handled at the view level.
		// The actual route registration happens when users navigate to the routes.
	}

	/** Register all dynamic routes for the current project. This is called during application initialization. */
	public void registerDynamicRoutes() {
		CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for route registration"));
		List<CPageEntity> pages = pageEntityService.findActivePagesByProject(activeProject);
		LOGGER.info("Registering {} dynamic routes for project: {}", pages.size(), activeProject.getName());
		for (CPageEntity page : pages) {
			Check.notNull(page, "Page entity cannot be null");
			try {
				registerDynamicRoute(page);
			} catch (Exception e) {
				LOGGER.error("Failed to register dynamic route for page: {}", page.getPageTitle(), e);
			}
		}
	}
}
