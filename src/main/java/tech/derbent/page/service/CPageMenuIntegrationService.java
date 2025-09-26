package tech.derbent.page.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.server.menu.MenuEntry;
import tech.derbent.api.utils.Check;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.view.CDynamicPageRouter;
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

	/** Create a MenuEntry from a CPageEntity. */
	private MenuEntry createMenuEntryFromPage(CPageEntity page) {
		Check.notNull(page, "Page entity cannot be null");
		// Get icon with fallback
		String icon = page.getIcon();
		if (icon == null || icon.trim().isEmpty()) {
			icon = "vaadin:file-text-o";
		}
		// Parse menu order with fallback
		Double order;
		try {
			String menuOrderStr = page.getMenuOrder();
			if (menuOrderStr != null && !menuOrderStr.trim().isEmpty()) {
				order = Double.parseDouble(menuOrderStr);
			} else {
				order = 50.0; // Default order
			}
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid menu order for page {}: {}", page.getPageTitle(), page.getMenuOrder());
			order = 50.0;
		}
		// Create menu title with tooltip-friendly formatting
		String menuTitle = page.getMenuTitle();
		if (menuTitle == null || menuTitle.trim().isEmpty()) {
			menuTitle = "dynamic/" + page.getPageTitle();
		} else {
			menuTitle = "dynamic/" + menuTitle;
		}
		// Create the menu entry with enhanced metadata
		return new MenuEntry("dynamic." + page.getId(), menuTitle, order, icon, CDynamicPageRouter.class);
	}

	/** Get menu entries for database-defined pages for the current project. These entries can be added to the existing menu system. */
	public List<MenuEntry> getDynamicMenuEntries() {
		CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for dynamic menu entries"));
		List<CPageEntity> pages = pageEntityService.findActivePagesByProject(activeProject);
		List<MenuEntry> menuEntries = new ArrayList<>();
		for (CPageEntity page : pages) {
			try {
				MenuEntry entry = createMenuEntryFromPage(page);
				menuEntries.add(entry);
			} catch (Exception e) {
				LOGGER.error("Failed to create menu entry for page: {}", page.getPageTitle(), e);
				throw new RuntimeException("Failed to create menu entry for page: " + page.getPageTitle(), e);
			}
		}
		return menuEntries;
	}

	/** Get page hierarchy structure for building nested menus. */
	public List<CPageEntity> getPageHierarchyForCurrentProject() {
		CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for page hierarchy"));
		return pageEntityService.getPageHierarchyForProject(activeProject);
	}

	/** Get pages that should be shown in the quick access toolbar for the current project. */
	public List<CPageEntity> getQuickToolbarPages() {
		CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for quick toolbar pages"));
		List<CPageEntity> allPages = pageEntityService.listQuickAccess(activeProject);
		return allPages;
	}

	/** Get root pages (no parent) for the current project. */
	public List<CPageEntity> getRootPagesForCurrentProject() {
		CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for root pages"));
		return pageEntityService.findRootPagesByProject(activeProject);
	}

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

	/** Check if the service is ready (has an active project). */
	public boolean isReady() { return sessionService.getActiveProject().isPresent(); }

	/** Get a page entity by ID for icon color retrieval. */
	public CPageEntity getPageEntityById(Long pageId) {
		return pageEntityService.getById(pageId).orElse(null);
	}
}
