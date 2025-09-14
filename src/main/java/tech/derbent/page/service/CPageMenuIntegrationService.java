package tech.derbent.page.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.server.menu.MenuEntry;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.view.CDynamicPageRouter;
import tech.derbent.page.view.CDynamicPageView;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/** Service for integrating database-defined pages with the Vaadin menu system. This service bridges CPageEntity data with MenuEntry objects for the
 * hierarchical menu. */
@Service
public class CPageMenuIntegrationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageMenuIntegrationService.class);
	private final CPageEntityService pageEntityService;
	private final CSessionService sessionService;

	public CPageMenuIntegrationService(CPageEntityService pageEntityService, CSessionService sessionService) {
		Check.notNull(pageEntityService, "CPageEntityService cannot be null");
		Check.notNull(sessionService, "CSessionService cannot be null");
		this.pageEntityService = pageEntityService;
		this.sessionService = sessionService;
		LOGGER.info("CPageMenuIntegrationService initialized");
	}

	/** Get menu entries for database-defined pages for the current project. These entries can be added to the existing menu system. */
	public List<MenuEntry> getDynamicMenuEntries() {
		CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for dynamic menu entries"));
		List<CPageEntity> pages = pageEntityService.findActivePagesByProject(activeProject);
		List<MenuEntry> menuEntries = new ArrayList<>();
		for (CPageEntity page : pages) {
			Check.notNull(page, "Page entity cannot be null");
			try {
				MenuEntry entry = createMenuEntryFromPage(page);
				menuEntries.add(entry);
				LOGGER.debug("Created menu entry for page: {}", page.getPageTitle());
			} catch (Exception e) {
				LOGGER.error("Failed to create menu entry for page: {}", page.getPageTitle(), e);
			}
		}
		LOGGER.info("Created {} dynamic menu entries for project: {}", menuEntries.size(), activeProject.getName());
		return menuEntries;
	}

	/** Create a MenuEntry from a CPageEntity. */
	private MenuEntry createMenuEntryFromPage(CPageEntity page) {
		Check.notNull(page, "Page entity cannot be null");
		String title = page.getTitle(); // e.g., "pages.Project Overview"
		Check.notBlank(title, "Page title cannot be blank");
		String route = page.getRoute(); // e.g., "route_index_01"
		Check.notBlank(route, "Page route cannot be blank");
		String icon = page.getIcon() != null ? page.getIcon() : "vaadin:file-text";
		Double order;
		try {
			String menuOrderStr = page.getMenuOrder();
			Check.notBlank(menuOrderStr, "Menu order cannot be blank");
			order = Double.parseDouble(menuOrderStr);
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid menu order for page {}: {}", page.getPageTitle(), page.getMenuOrder());
			order = 999.0; // Default to end of menu
		}
		// Create the navigation URL that points to our dynamic page router with the route as parameter
		String navigationPath = "project-pages/" + route;
		// Create MenuEntry using the record constructor
		// Point to CDynamicPageRouter which will handle the actual route resolution
		return new MenuEntry(title, navigationPath, order, icon, CDynamicPageRouter.class);
	}

	/** Get page hierarchy structure for building nested menus. */
	public List<CPageEntity> getPageHierarchyForCurrentProject() {
		CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for page hierarchy"));
		return pageEntityService.getPageHierarchyForProject(activeProject);
	}

	/** Get root pages (no parent) for the current project. */
	public List<CPageEntity> getRootPagesForCurrentProject() {
		CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for root pages"));
		return pageEntityService.findRootPagesByProject(activeProject);
	}

	/** Check if the service is ready (has an active project). */
	public boolean isReady() { return sessionService.getActiveProject().isPresent(); }

	/** Get status information for debugging. */
	public String getStatusInfo() {
		Optional<CProject> activeProjectOpt = sessionService.getActiveProject();
		if (activeProjectOpt.isEmpty()) {
			return "No active project";
		}
		CProject activeProject = activeProjectOpt.get();
		List<CPageEntity> pages = pageEntityService.findActivePagesByProject(activeProject);
		return String.format("Project: %s, Pages: %d", activeProject.getName(), pages.size());
	}
}
