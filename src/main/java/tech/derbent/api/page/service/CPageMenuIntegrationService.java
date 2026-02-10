package tech.derbent.api.page.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.server.menu.MenuEntry;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.menu.MyMenuConfiguration;
import tech.derbent.api.menu.MyMenuEntry;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.page.view.CDynamicPageRouter;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;

/** Service for integrating database-defined pages with the Vaadin menu system. This service bridges CPageEntity data with MenuEntry objects for the
 * hierarchical menu. */
@Service
public class CPageMenuIntegrationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageMenuIntegrationService.class);

	/** Create a MenuEntry from a CPageEntity. 🔴 TODO FUTURE FIX: Replace MenuEntry with custom annotation to preserve hierarchical ordering Current
	 * limitation: Vaadin MenuEntry.order() is Double - cannot preserve 3+ level hierarchies! SOLUTION APPROACH: Create custom @MyMenu annotation with
	 * String orderString field Example:
	 *
	 * <pre>
	 * {@literal @}MyMenu(
	 *     title = "Project.Activities.Type1",
	 *     orderString = "5.4.3",  // ← Preserved as String!
	 *     icon = "vaadin:tasks"
	 * )
	 * public class CActivityTypeView extends CAbstractPage { }
	 * </pre>
	 *
	 * Benefits of custom annotation: 1. ✅ No data loss - orderString preserved exactly as written 2. ✅ Supports unlimited hierarchy levels 3. ✅
	 * Supports multi-digit positions (e.g., "10.20.30") 4. ✅ No ambiguity - "5.4.3" stays as ["5", "4", "3"] 5. ✅ Backward compatible - can coexist
	 * with MenuEntry Implementation steps: 1. Create @MyMenu annotation with orderString field 2. Create MyMenuEntry class to hold parsed data 3.
	 * Scan for @MyMenu annotations at startup (like @Menu) 4. Store orderString in MyMenuEntry (don't convert to Double!) 5. Update
	 * CHierarchicalSideMenu to use orderString directly 6. Parse orderString only when needed (split by ".") Migration path: - Phase 1: Add @MyMenu
	 * support alongside existing @Menu - Phase 2: Migrate dynamic pages to use MyMenuEntry - Phase 3: Migrate static pages to @MyMenu - Phase 4:
	 * Deprecate Double-based ordering Reference: See HIERARCHICAL_MENU_ORDERING_BUG_ANALYSIS.md for complete analysis */
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
		// 🔴 BUG: This loses information for 3+ levels! See parseMenuOrderToDouble() for details.
		final Double order = parseMenuOrderToDouble(page.getMenuOrder());
		// Create the menu entry with enhanced metadata
		// 🔴 TODO FUTURE: Replace with MyMenuEntry that preserves orderString
		return new MenuEntry("dynamic." + page.getId(), menuTitle, order, icon, CDynamicPageRouter.class);
	}

	private static boolean isNumeric(final String value) {
		if (value == null || value.isEmpty()) {
			return false;
		}
		for (int i = 0; i < value.length(); i++) {
			if (!Character.isDigit(value.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/** Parse menu order string to a Double value. For hierarchical menus, menuOrder is in format like "4.1" where: - "4" is the order of the parent
	 * level - "1" is the order of the child level The string is converted to a Double (e.g., "4.1" → 4.1), which preserves the hierarchical
	 * information. CHierarchicalSideMenu will parse this to extract orders for each level. Examples: - "5" → 5.0 - "4.1" → 4.1 - "4.1.2" → 4.12
	 * (concatenated as decimal) - "" or null → 999.0 (default high order) 🔴 CRITICAL BUG: This method DESTROYS hierarchical structure for 3+ level
	 * menus! Current behavior (WRONG): - Input: "5.4.3" (parent=5, child1=4, child2=3) - parts[] = ["5", "4", "3"] - decimals = "4" + "3" = "43" ←
	 * WRONG! Should keep separate: [4, 3] - result = Double.parseDouble("5.43") = 5.43 - CHierarchicalSideMenu receives 5.43 and CANNOT distinguish:
	 * * Was it "5.4.3" (parent=5, child1=4, child2=3)? * Or was it "5.43" (parent=5, child=43)? * Or even "5.4.30" (parent=5, child1=4, child2=30)?
	 * Impact: Menu items with 3+ levels are ordered INCORRECTLY! Example: "1.2.3" and "1.23" both become 1.23 - COLLISION! 🔧 TODO FIX OPTIONS: 1.
	 * Keep original string in MenuEntry metadata (requires Vaadin API extension) 2. Use encoding scheme: "5.4.3" → 5004003 (fixed-width decimal
	 * parts) 3. Store as array: ["5", "4", "3"] in custom MenuEntry subclass 4. Use hierarchical object instead of Double for ordering
	 * @param menuOrderStr The menu order string from CPageEntity
	 * @return The parsed order value (LOSSY for 3+ levels!) */
	private static Double parseMenuOrderToDouble(String menuOrderStr) {
		// Default order for missing or invalid menuOrder
		final Double DEFAULT_ORDER = 999.0;
		// Check for null or empty menuOrder
		if (menuOrderStr == null || menuOrderStr.trim().isEmpty()) {
			return DEFAULT_ORDER;
		}
		final String trimmed = menuOrderStr.trim();
		try {
			// Try to parse as a simple Double first
			return Double.parseDouble(trimmed);
		} catch (final NumberFormatException e) {
			// 🔴 BUG: Concatenating parts destroys hierarchy for 3+ levels!
			// "5.4.3" becomes "5.43" - information loss!
			final String[] parts = trimmed.split("\\.");
			if (parts.length > 1 && isNumeric(parts[0])) {
				final StringBuilder decimals = new StringBuilder();
				for (int i = 1; i < parts.length; i++) {
					if (!isNumeric(parts[i])) {
						LOGGER.warn("Invalid menu order format: '{}'. Using default order {}. {}", menuOrderStr, DEFAULT_ORDER, e.getMessage());
						return DEFAULT_ORDER;
					}
					decimals.append(parts[i]); // ← BUG: Concatenates "4" + "3" = "43" instead of [4, 3]
				}
				final String composed = parts[0] + "." + decimals; // ← Result: "5.43" (hierarchy lost!)
				try {
					return Double.parseDouble(composed);
				} catch (final NumberFormatException composedError) {
					LOGGER.warn("Invalid menu order format: '{}'. Using default order {}. {}", menuOrderStr, DEFAULT_ORDER,
							composedError.getMessage());
					return DEFAULT_ORDER;
				}
			}
			LOGGER.warn("Invalid menu order format: '{}'. Using default order {}. {}", menuOrderStr, DEFAULT_ORDER, e.getMessage());
			return DEFAULT_ORDER;
		}
	}

	private final CPageEntityService pageEntityService;
	private final ISessionService sessionService;
	private final MyMenuConfiguration myMenuConfiguration;

	public CPageMenuIntegrationService(CPageEntityService pageEntityService, ISessionService sessionService,
			MyMenuConfiguration myMenuConfiguration) {
		Check.notNull(pageEntityService, "CPageEntityService cannot be null");
		Check.notNull(sessionService, "CSessionService cannot be null");
		Check.notNull(myMenuConfiguration, "MyMenuConfiguration cannot be null");
		this.pageEntityService = pageEntityService;
		this.sessionService = sessionService;
		this.myMenuConfiguration = myMenuConfiguration;
		
		// Scan @MyMenu annotations at startup
		myMenuConfiguration.scanMyMenuAnnotations();
		LOGGER.info("Initialized CPageMenuIntegrationService with {} @MyMenu entries", 
			myMenuConfiguration.getMyMenuEntries().size());
	}

	/** @deprecated Use getDynamicMyMenuEntries() instead. This method uses Double-based ordering which loses hierarchy data. */
	@Deprecated
	public List<MenuEntry> getDynamicMenuEntries() {
		try {
			Check.notNull(sessionService, "Session service cannot be null");
			final Optional<CProject<?>> activeProject = sessionService.getActiveProject();
			if (activeProject.isEmpty()) {
				if (CSpringContext.isBabProfile()) {
					LOGGER.info("Skipping dynamic menu entries for BAB profile (no active project).");
					return List.of();
				}
				throw new IllegalStateException("No active project found for dynamic menu entries");
			}
			final List<CPageEntity> pages = pageEntityService.findActivePagesByProject(activeProject.get());
			final List<MenuEntry> menuEntries = new ArrayList<>();
			pages.forEach((final CPageEntity page) -> {
				try {
					final MenuEntry entry = createMenuEntryFromPage(page);
					menuEntries.add(entry);
				} catch (final Exception e) {
					LOGGER.error("Failed to create menu entry for page: {}", page.getPageTitle(), e);
					throw new RuntimeException("Failed to create menu entry for page: " + page.getPageTitle(), e);
				}
			});
			return menuEntries;
		} catch (final Exception e) {
			LOGGER.error("Error retrieving dynamic menu entries: {}", e.getMessage());
			throw e;
		}
	}

	/** ✅ NEW: Get MyMenuEntry objects for database-defined pages (String-based ordering, no data loss!). This replaces getDynamicMenuEntries() which
	 * used Double-based ordering. Key difference: - OLD: menuOrder "5.4.3" → Double 5.43 (HIERARCHY LOST!) - NEW: menuOrder "5.4.3" → MyMenuEntry
	 * with orderComponents [5, 4, 3] (EXACT!)
	 * @return List of MyMenuEntry objects for active project's pages */
	public List<MyMenuEntry> getDynamicMyMenuEntries() {
		try {
			Check.notNull(sessionService, "Session service cannot be null");
			final Optional<CProject<?>> activeProject = sessionService.getActiveProject();
			if (activeProject.isEmpty()) {
				if (CSpringContext.isBabProfile()) {
					LOGGER.info("Skipping dynamic menu entries for BAB profile (no active project).");
					return List.of();
				}
				throw new IllegalStateException("No active project found for dynamic menu entries");
			}
			final List<CPageEntity> pages = pageEntityService.findActivePagesByProject(activeProject.get());
			final List<MyMenuEntry> myMenuEntries = new ArrayList<>();
			pages.forEach((final CPageEntity page) -> {
				try {
					// Get icon with fallback
					String icon = page.getIconString();
					if (icon == null || icon.trim().isEmpty()) {
						icon = "vaadin:file-text-o";
					}
					// Create menu title
					String menuTitle = page.getMenuTitle();
					if (menuTitle == null || menuTitle.trim().isEmpty()) {
						menuTitle = page.getPageTitle();
					}
					// ✅ Keep menuOrder as STRING - no conversion to Double!
					// This preserves EXACT hierarchy: "10.20.30" stays as [10, 20, 30]
					String menuOrderString = page.getMenuOrder();
					if (menuOrderString == null || menuOrderString.trim().isEmpty()) {
						menuOrderString = "999"; // Default order
					}
					// Create path for dynamic page
					final String path = "dynamic." + page.getId();
					// ✅ Create MyMenuEntry with String-based ordering (NO DATA LOSS!)
					final MyMenuEntry myEntry = new MyMenuEntry(path, menuTitle, menuOrderString, // ← STRING! Preserved exactly!
							icon, CDynamicPageRouter.class, false, // showInQuickToolbar - dynamic pages not in quick toolbar by default
							new String[0] // no profile restriction for dynamic pages
					);
					myMenuEntries.add(myEntry);
					LOGGER.debug("Created MyMenuEntry for page '{}': orderString='{}', orderComponents={}", page.getPageTitle(), menuOrderString,
							Arrays.toString(myEntry.orderComponents()));
				} catch (final Exception e) {
					LOGGER.error("Failed to create MyMenuEntry for page: {}", page.getPageTitle(), e);
				}
			});
			LOGGER.info("Created {} dynamic MyMenuEntries for project '{}'", myMenuEntries.size(), activeProject.get().getName());
			return myMenuEntries;
		} catch (final Exception e) {
			LOGGER.error("Error getting dynamic MyMenuEntries: {}", e.getMessage(), e);
			return List.of();
		}
	}

	/** Get a page entity by ID for icon color retrieval. */
	public CPageEntity getPageEntityById(Long pageId) {
		return pageEntityService.getById(pageId).orElse(null);
	}

	/** Get page hierarchy structure for building nested menus. */
	public List<CPageEntity> getPageHierarchyForCurrentProject() {
		final Optional<CProject<?>> activeProject = sessionService.getActiveProject();
		if (activeProject.isEmpty()) {
			if (CSpringContext.isBabProfile()) {
				return List.of();
			}
			throw new IllegalStateException("No active project found for page hierarchy");
		}
		return pageEntityService.getPageHierarchyForProject(activeProject.get());
	}

	/** Get pages that should be shown in the quick access toolbar for the current project. */
	public List<CPageEntity> getQuickToolbarPages() {
		final Optional<CProject<?>> activeProject = sessionService.getActiveProject();
		if (activeProject.isEmpty()) {
			if (CSpringContext.isBabProfile()) {
				return List.of();
			}
			throw new IllegalStateException("No active project found for quick toolbar pages");
		}
		return pageEntityService.listQuickAccess(activeProject.get());
	}
	
	/**
	 * Get @MyMenu annotated entries marked for quick toolbar.
	 * 
	 * @return list of MyMenuEntry objects where showInQuickToolbar is true
	 */
	public List<MyMenuEntry> getMyMenuEntriesForQuickToolbar() {
		return myMenuConfiguration.getMyMenuEntriesForQuickToolbar();
	}
	
	/**
	 * Get all static @MyMenu annotated entries.
	 * 
	 * @return list of all MyMenuEntry objects from @MyMenu annotations
	 */
	public List<MyMenuEntry> getStaticMyMenuEntries() {
		return myMenuConfiguration.getMyMenuEntries();
	}

	/** Get root pages (no parent) for the current project. */
	public List<CPageEntity> getRootPagesForCurrentProject() {
		final Optional<CProject<?>> activeProject = sessionService.getActiveProject();
		if (activeProject.isEmpty()) {
			if (CSpringContext.isBabProfile()) {
				return List.of();
			}
			throw new IllegalStateException("No active project found for root pages");
		}
		return pageEntityService.findRootPagesByProject(activeProject.get());
	}

	/** Get status information for debugging. */
	public String getStatusInfo() {
		final Optional<CProject<?>> activeProjectOpt = sessionService.getActiveProject();
		if (activeProjectOpt.isEmpty()) {
			return "No active project";
		}
		final CProject<?> activeProject = activeProjectOpt.get();
		final List<CPageEntity> pages = pageEntityService.findActivePagesByProject(activeProject);
		return "Project: %s, Pages: %d".formatted(activeProject.getName(), pages.size());
	}

	/** Check if the service is ready (has an active project). */
	public boolean isReady() {
		return sessionService.getActiveProject().isPresent() || CSpringContext.isBabProfile();
	}
}
