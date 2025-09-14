package tech.derbent.page.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.server.menu.MenuEntry;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.view.CDynamicPageView;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/**
 * Service for integrating database-defined pages with the Vaadin menu system.
 * This service bridges CPageEntity data with MenuEntry objects for the hierarchical menu.
 */
@Service
public class CPageMenuIntegrationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageMenuIntegrationService.class);
	
	private final CPageService pageService;
	private final CSessionService sessionService;

	public CPageMenuIntegrationService(CPageService pageService, CSessionService sessionService) {
		this.pageService = pageService;
		this.sessionService = sessionService;
		LOGGER.info("CPageMenuIntegrationService initialized");
	}

	/**
	 * Get menu entries for database-defined pages for the current project.
	 * These entries can be added to the existing menu system.
	 */
	public List<MenuEntry> getDynamicMenuEntries() {
		Optional<CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isEmpty()) {
			LOGGER.debug("No active project, returning empty menu entries");
			return List.of();
		}

		List<CPageEntity> pages = pageService.getMenuPagesForProject(activeProject.get());
		List<MenuEntry> menuEntries = new ArrayList<>();

		for (CPageEntity page : pages) {
			try {
				MenuEntry entry = createMenuEntryFromPage(page);
				menuEntries.add(entry);
				LOGGER.debug("Created menu entry for page: {}", page.getPageTitle());
			} catch (Exception e) {
				LOGGER.error("Failed to create menu entry for page: {}", page.getPageTitle(), e);
			}
		}

		LOGGER.info("Created {} dynamic menu entries for project: {}", menuEntries.size(), activeProject.get().getName());
		return menuEntries;
	}

	/**
	 * Create a MenuEntry from a CPageEntity.
	 */
	private MenuEntry createMenuEntryFromPage(CPageEntity page) {
		String title = page.getTitle(); // e.g., "Project.Overview"
		String path = page.getRoute(); // e.g., "project-overview-1"
		String icon = page.getIcon() != null ? page.getIcon() : "vaadin:file-text";
		
		Double order;
		try {
			order = Double.parseDouble(page.getMenuOrder());
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid menu order for page {}: {}", page.getPageTitle(), page.getMenuOrder());
			order = 999.0; // Default to end of menu
		}
		
		// Create MenuEntry using the record constructor
		return new MenuEntry(title, path, order, icon, CDynamicPageView.class);
	}

	/**
	 * Get page hierarchy structure for building nested menus.
	 */
	public List<CPageEntity> getPageHierarchyForCurrentProject() {
		Optional<CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isEmpty()) {
			return List.of();
		}

		return pageService.getPageHierarchy(activeProject.get());
	}

	/**
	 * Get root pages (no parent) for the current project.
	 */
	public List<CPageEntity> getRootPagesForCurrentProject() {
		Optional<CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isEmpty()) {
			return List.of();
		}

		return pageService.getRootPages(activeProject.get());
	}

	/**
	 * Check if the service is ready (has an active project).
	 */
	public boolean isReady() {
		return sessionService.getActiveProject().isPresent();
	}

	/**
	 * Get status information for debugging.
	 */
	public String getStatusInfo() {
		Optional<CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isEmpty()) {
			return "No active project";
		}

		List<CPageEntity> pages = pageService.getMenuPagesForProject(activeProject.get());
		return String.format("Project: %s, Pages: %d, Cache: %s", 
				activeProject.get().getName(), 
				pages.size(),
				pageService.getCacheSize());
	}
}