package tech.derbent.app.page.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.server.menu.MenuEntry;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.app.page.view.CDynamicPageRouter;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;

/** Service for integrating database-defined pages with the Vaadin menu system. This service bridges CPageEntity data with MenuEntry objects for the
 * hierarchical menu. */
@Service
public class CPageMenuIntegrationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageMenuIntegrationService.class);

	/** Create a MenuEntry from a CPageEntity. */
	private static MenuEntry createMenuEntryFromPage(CPageEntity page) {
		Check.notNull(page, "Page entity cannot be null");
		// Get icon with fallback
		String icon = page.getIconString();
		if (icon == null || icon.trim().isEmpty()) {
			icon = "vaadin:file-text-o";
		}
		// Create menu title with tooltip-friendly formatting
		String menuTitle = page.getMenuTitle();
		if (menuTitle == null || menuTitle.trim().isEmpty()) {
			menuTitle = "dynamic/" + page.getPageTitle();
		} else {
			menuTitle = "dynamic/" + menuTitle;
		}
		// Parse menu order - keep the full hierarchical order value
		// For example, "4.1" becomes 4.1, which CHierarchicalSideMenu will parse
		// to extract parent order (4) and child order (1)
		final Double order = parseMenuOrderToDouble(page.getMenuOrder());
		// Create the menu entry with enhanced metadata
		return new MenuEntry("dynamic." + page.getId(), menuTitle, order, icon, CDynamicPageRouter.class);
	}

	/** Parse menu order string to a Double value. For hierarchical menus, menuOrder is in format like "4.1" where: - "4" is the order of the parent
	 * level - "1" is the order of the child level The string is converted to a Double (e.g., "4.1" → 4.1), which preserves the hierarchical
	 * information. CHierarchicalSideMenu will parse this to extract orders for each level. Examples: - "5" → 5.0 - "4.1" → 4.1 - "4.1.2" → 4.12
	 * (concatenated as decimal) - "" or null → 999.0 (default high order)
	 * @param menuOrderStr The menu order string from CPageEntity
	 * @return The parsed order value */
	private static Double parseMenuOrderToDouble(String menuOrderStr) {
		// Default order for missing or invalid menuOrder
		final Double DEFAULT_ORDER = 999.0;
		// Check for null or empty menuOrder
		if (menuOrderStr == null || menuOrderStr.trim().isEmpty()) {
			return DEFAULT_ORDER;
		}
		try {
			// Try to parse as a simple Double first
			return Double.parseDouble(menuOrderStr.trim());
		} catch (final NumberFormatException e) {
			LOGGER.warn("Invalid menu order format: '{}'. Using default order {}. {}", menuOrderStr, DEFAULT_ORDER, e.getMessage());
			return DEFAULT_ORDER;
		}
	}

	private final CPageEntityService pageEntityService;
	private final ISessionService sessionService;

	public CPageMenuIntegrationService(CPageEntityService pageEntityService, ISessionService sessionService) {
		Check.notNull(pageEntityService, "CPageEntityService cannot be null");
		Check.notNull(sessionService, "CSessionService cannot be null");
		this.pageEntityService = pageEntityService;
		this.sessionService = sessionService;
		// LOGGER.info("CPageMenuIntegrationService initialized");
	}

	/** Get menu entries for database-defined pages for the current project. These entries can be added to the existing menu system. */
	public List<MenuEntry> getDynamicMenuEntries() {
		try {
			Check.notNull(sessionService, "Session service cannot be null");
			final CProject activeProject = sessionService.getActiveProject()
					.orElseThrow(() -> new IllegalStateException("No active project found for dynamic menu entries"));
			final List<CPageEntity> pages = pageEntityService.findActivePagesByProject(activeProject);
			final List<MenuEntry> menuEntries = new ArrayList<>();
			for (final CPageEntity page : pages) {
				try {
					final MenuEntry entry = createMenuEntryFromPage(page);
					menuEntries.add(entry);
				} catch (final Exception e) {
					LOGGER.error("Failed to create menu entry for page: {}", page.getPageTitle(), e);
					throw new RuntimeException("Failed to create menu entry for page: " + page.getPageTitle(), e);
				}
			}
			return menuEntries;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving dynamic menu entries: {}", e.getMessage());
			throw e;
		}
	}

	/** Get a page entity by ID for icon color retrieval. */
	public CPageEntity getPageEntityById(Long pageId) {
		return pageEntityService.getById(pageId).orElse(null);
	}

	/** Get page hierarchy structure for building nested menus. */
	public List<CPageEntity> getPageHierarchyForCurrentProject() {
		final CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for page hierarchy"));
		return pageEntityService.getPageHierarchyForProject(activeProject);
	}

	/** Get pages that should be shown in the quick access toolbar for the current project. */
	public List<CPageEntity> getQuickToolbarPages() {
		final CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for quick toolbar pages"));
		final List<CPageEntity> allPages = pageEntityService.listQuickAccess(activeProject);
		return allPages;
	}

	/** Get root pages (no parent) for the current project. */
	public List<CPageEntity> getRootPagesForCurrentProject() {
		final CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for root pages"));
		return pageEntityService.findRootPagesByProject(activeProject);
	}

	/** Get status information for debugging. */
	public String getStatusInfo() {
		final Optional<CProject> activeProjectOpt = sessionService.getActiveProject();
		if (activeProjectOpt.isEmpty()) {
			return "No active project";
		}
		final CProject activeProject = activeProjectOpt.get();
		final List<CPageEntity> pages = pageEntityService.findActivePagesByProject(activeProject);
		return String.format("Project: %s, Pages: %d", activeProject.getName(), pages.size());
	}

	/** Check if the service is ready (has an active project). */
	public boolean isReady() { return sessionService.getActiveProject().isPresent(); }
}
