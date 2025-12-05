package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.IconSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CPageTestAuxillaryService;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.app.page.service.CPageMenuIntegrationService;
import tech.derbent.app.page.view.CDynamicPageRouter;

/** CHierarchicalSideMenu - A hierarchical side menu component with up to 4 levels of navigation. Layer: View (MVC) Features: - Supports up to 4
 * levels of menu hierarchy - Sliding animations between levels - Back button navigation - Parses menu entries from route annotations in format:
 * parentItem2.childItem1.childofchileitem1 - Responsive design with proper styling - Current page highlighting */
public final class CHierarchicalSideMenu extends Div implements AfterNavigationObserver {

	/** Inner class representing a single menu item. */
	private final class CMenuItem {

		private final String iconColor;
		private final String iconName;
		private final boolean isNavigation;
		private final String name;
		private final Double order;
		private final String path;
		private final String targetLevelKey;

		public CMenuItem(final Class<? extends Component> clazz, final String name, final String iconName, final String path,
				final String targetLevelKey, final boolean isNavigation, final Double order) throws Exception {
			try {
				this.name = name;
				this.order = order != null ? order : 9999.0; // Default order if not specified
				if (iconName.startsWith("class:")) {
					this.iconName = CColorUtils.getStaticIconFilename(iconName.replace("class:", ""));
				} else {
					this.iconName = iconName;
				}
				this.path = path;
				this.targetLevelKey = targetLevelKey;
				this.isNavigation = isNavigation;
				// get icon color - use CPageEntity color for dynamic pages, otherwise use class color
				if (isDynamicPagePath(path)) {
					iconColor = getDynamicPageIconColor(path);
				} else {
					iconColor = CColorUtils.getStaticIconColorCode(clazz.getName());
				}
			} catch (Exception e) {
				LOGGER.warn("Check route information for icon retrieval. Probably missing Class path or function.");
				throw e;
			}
		}

		public Component createComponent() {
			final HorizontalLayout itemLayout = new HorizontalLayout();
			itemLayout.addClassNames(Display.FLEX, AlignItems.CENTER, Padding.MEDIUM, Gap.MEDIUM, MENU_ITEM_CLASS);
			itemLayout.setWidthFull();
			// Check if this item represents the current page
			final boolean isCurrentPage = (path != null) && !path.trim().isEmpty() && (currentRoute != null) && currentRoute.equals(path.trim());
			// Add icon with consistent sizing and colorful styling
			Icon icon = CColorUtils.setIconClassSize(CColorUtils.createStyledIcon(iconName, iconColor), IconSize.MEDIUM);
			if (icon == null) {
				// space holder icon to keep alignment
				icon = VaadinIcon.CIRCLE.create();
				icon.getStyle().set("visibility", "hidden");
			}
			itemLayout.add(icon);
			// Add text with highlighting
			final Span itemText = new Span(name);
			itemText.addClassNames(FontSize.LARGE, isCurrentPage ? FontWeight.BOLD : FontWeight.NORMAL);
			if (isCurrentPage) {
				itemText.addClassNames(TextColor.PRIMARY);
			}
			itemLayout.add(itemText);
			// Add navigation arrow for navigation items
			if (isNavigation) {
				final Div spacer = new Div();
				spacer.setWidthFull();
				itemLayout.add(spacer);
				itemLayout.setFlexGrow(1, spacer);
				final Icon navIcon = CColorUtils.setIconClassSize(VaadinIcon.ANGLE_RIGHT.create(), IconSize.MEDIUM);
				navIcon.getStyle().set("color", "var(--lumo-primary-color)");
				itemLayout.add(navIcon);
			}
			// Apply current page highlighting styles
			if (isCurrentPage) {
				itemLayout.getElement().getStyle().set("background-color", "var(--lumo-primary-color-10pct)").set("border-left",
						"4px solid var(--lumo-primary-color)");
			}
			// Add click listener
			itemLayout.addClickListener(this::handleItemClick);
			// Style as clickable
			itemLayout.getElement().getStyle().set("cursor", "pointer").set("border-radius", "var(--lumo-border-radius-m)").set("transition",
					"all 0.2s ease");
			// Add hover effects (only if not current page to avoid conflicts)
			if (!isCurrentPage) {
				itemLayout.getElement().addEventListener("mouseenter",
						e -> itemLayout.getElement().getStyle().set("background-color", "var(--lumo-contrast-5pct)"));
				itemLayout.getElement().addEventListener("mouseleave", e -> itemLayout.getElement().getStyle().remove("background-color"));
			}
			return itemLayout;
		}

		public Double getOrder() { return order; }

		private void handleItemClick(final ClickEvent<HorizontalLayout> event) {
			if (isNavigation && (targetLevelKey != null)) {
				// Navigate to sub-level
				showLevel(targetLevelKey);
			} else if ((path != null) && !path.trim().isEmpty()) {
				// Navigate to actual page
				LOGGER.debug("Navigating to path: {}", path);
				if (path.startsWith("dynamic.")) {
					// Remove "dynamic." prefix and navigate
					String dynamicPath = path.substring("dynamic.".length());
					// give rest of path as a parameter to dynamicview page
					String dynamicViewPath = "cdynamicpagerouter/page:" + dynamicPath;
					UI.getCurrent().navigate(dynamicViewPath);
					return;
				} else {
					UI.getCurrent().navigate(path);
				}
			}
		}
	}

	/** Inner class representing a single level in the menu hierarchy. */
	private final class CMenuLevel {

		private final String displayName;
		private final List<CMenuItem> items;
		private final String levelKey;
		private final CMenuLevel parent;

		public CMenuLevel(final String levelKey, final String displayName, final CMenuLevel parent) {
			LOGGER.debug("Creating menu level: {} (parent: {})", levelKey, parent != null ? parent.getLevelKey() : "none" + " with display name: {}",
					displayName);
			this.levelKey = levelKey;
			this.displayName = displayName;
			this.parent = parent;
			items = new ArrayList<>();
		}

		public CMenuItem addMenuItem(final Class<? extends Component> clazz, final String name, final String iconName, final String path,
				final Double order) throws Exception {
			final CMenuItem item = new CMenuItem(clazz, name, iconName, path, null, false, order);
			items.add(item);
			// Also add to flat list for search (only non-navigation items with paths)
			if ((path != null) && !path.isBlank()) {
				allMenuItems.add(item);
			}
			return item;
		}

		public void addNavigationItem(final Class<? extends Component> clazz, final String name, final String iconName, final String targetLevelKey,
				final Double order) throws Exception {
			final CMenuItem item = new CMenuItem(clazz, name, iconName, null, targetLevelKey, true, order);
			items.add(item);
		}

		public Component createLevelComponent() {
			final VerticalLayout levelLayout = new VerticalLayout();
			levelLayout.addClassNames(Padding.NONE, Gap.SMALL);
			levelLayout.setWidthFull();
			// Sort items by order before adding to layout
			items.sort((a, b) -> Double.compare(a.getOrder(), b.getOrder()));
			for (final CMenuItem item : items) {
				levelLayout.add(item.createComponent());
			}
			return levelLayout;
		}

		public String getDisplayName() { return displayName; }

		// Getters
		public String getLevelKey() { return levelKey; }

		public CMenuLevel getParent() { return parent; }
	}

	private static final String BACK_BUTTON_CLASS = "hierarchical-back-button";
	private static final String HEADER_CLASS = "hierarchical-header";
	private static final String LEVEL_CONTAINER_CLASS = "hierarchical-level-container";
	private static final int MAX_MENU_LEVELS = 4;
	// Styling constants
	private static final String MENU_ITEM_CLASS = "hierarchical-menu-item";
	private static final long serialVersionUID = 1L;
	// All menu items for search (flat list)
	private final List<CMenuItem> allMenuItems;
	private CMenuLevel currentLevel;
	private final Div currentLevelContainer;
	private String currentRoute; // Track current route for highlighting
	private final HorizontalLayout headerLayout;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	// Menu structure components
	private final VerticalLayout menuContainer;
	private final Map<String, CMenuLevel> menuLevels;
	// Navigation state
	private final List<String> navigationPath;
	// Services for dynamic menu integration
	private final CPageMenuIntegrationService pageMenuService;
	private final CPageTestAuxillaryService pageTestAuxillaryService;
	private final Div searchContainer;
	// Search components
	private final TextField searchField;

	/** Constructor initializes the hierarchical side menu component.
	 * @param pageMenuService Service for dynamic page menu integration
	 * @throws Exception */
	public CHierarchicalSideMenu(CPageMenuIntegrationService pageMenuService, CPageTestAuxillaryService pageTestAuxillaryService) throws Exception {
		LOGGER.info("Initializing CHierarchicalSideMenu");
		this.pageMenuService = pageMenuService;
		this.pageTestAuxillaryService = pageTestAuxillaryService;
		navigationPath = new ArrayList<>();
		menuLevels = new HashMap<>();
		allMenuItems = new ArrayList<>();
		// Initialize main container
		menuContainer = new VerticalLayout();
		menuContainer.addClassNames(Padding.NONE, Gap.SMALL);
		menuContainer.setWidthFull();
		// Initialize header with back button area
		headerLayout = new HorizontalLayout();
		headerLayout.addClassNames(Display.FLEX, AlignItems.CENTER, Padding.MEDIUM, HEADER_CLASS);
		headerLayout.setWidthFull();
		// Initialize search field
		searchField = createSearchField();
		searchField.setWidth("90%");
		searchContainer = new Div(searchField);
		searchContainer.addClassNames(Padding.Horizontal.MEDIUM, Padding.Bottom.SMALL);
		searchContainer.setWidthFull();
		// Initialize current level display container
		currentLevelContainer = new Div();
		currentLevelContainer.addClassNames(LEVEL_CONTAINER_CLASS);
		currentLevelContainer.setWidthFull();
		// Build menu structure from annotations
		buildMenuHierarchy();
		// Add components to main container (header, search, content)
		menuContainer.add(headerLayout, searchContainer, currentLevelContainer);
		add(menuContainer);
		// Apply CSS styling
		initializeStyles();
		// Show root level initially
		showLevel("root");
		LOGGER.info("CHierarchicalSideMenu initialized successfully with {} menu levels", menuLevels.size());
	}

	@Override
	public void afterNavigation(final AfterNavigationEvent event) {
		// Update current route and refresh highlighting
		final String newRoute = event.getLocation().getPath();
		if (!newRoute.equals(currentRoute)) {
			currentRoute = newRoute;
			refreshCurrentLevel();
		}
	}

	/** Builds the menu hierarchy from route annotations. Parses menu entries in format: parentItem2.childItem1.childofchileitem1
	 * @param pageTestAuxillaryService2
	 * @throws Exception */
	private void buildMenuHierarchy() throws Exception {
		LOGGER.debug("Building menu hierarchy from route annotations");
		Check.notNull(pageMenuService, "Page menu service must not be null");
		final var rootLevel = new CMenuLevel("root", "Homepage", null);
		menuLevels.put("root", rootLevel);
		List<MenuEntry> allMenuEntries = new ArrayList<>();
		allMenuEntries.addAll(MenuConfiguration.getMenuEntries());
		allMenuEntries.addAll(pageMenuService.getDynamicMenuEntries());
		// Process all menu entries (both static and dynamic)
		pageTestAuxillaryService.clearRoutes(); // Clear previous routes to avoid duplicates
		for (final MenuEntry menuEntry : allMenuEntries) {
			processMenuEntry(menuEntry);
		}
	}

	Icon createMenuIcon(Icon icon, String iconColor) {
		CColorUtils.setIconClassSize(icon, IconSize.MEDIUM);
		icon.getStyle().set("color", iconColor);
		return icon;
	}

	/** Creates the search field for filtering menu items.
	 * @return TextField configured for menu search */
	private TextField createSearchField() {
		final TextField textFieldSearch = new TextField();
		textFieldSearch.setPlaceholder("Search menu...");
		textFieldSearch.setPrefixComponent(VaadinIcon.SEARCH.create());
		textFieldSearch.setClearButtonVisible(true);
		textFieldSearch.setWidthFull();
		textFieldSearch.setValueChangeMode(ValueChangeMode.EAGER);
		textFieldSearch.addValueChangeListener(event -> on_textFieldSearch_valueChanged(event.getValue()));
		// Style the search field
		textFieldSearch.getStyle().set("--vaadin-input-field-border-radius", "var(--lumo-border-radius-m)");
		return textFieldSearch;
	}

	/** Creates a search result item component for displaying filtered menu items.
	 * @param item The menu item to display
	 * @return Component representing the search result item */
	private Component createSearchResultItem(final CMenuItem item) {
		final HorizontalLayout itemLayout = new HorizontalLayout();
		itemLayout.addClassNames(Display.FLEX, AlignItems.CENTER, Padding.MEDIUM, Gap.MEDIUM, MENU_ITEM_CLASS);
		itemLayout.setWidthFull();
		// Add icon with consistent sizing and colorful styling
		Icon icon = CColorUtils.setIconClassSize(CColorUtils.createStyledIcon(item.iconName, item.iconColor), IconSize.MEDIUM);
		if (icon == null) {
			icon = VaadinIcon.CIRCLE.create();
			icon.getStyle().set("visibility", "hidden");
		}
		itemLayout.add(icon);
		// Add text
		final Span itemText = new Span(item.name);
		itemText.addClassNames(FontSize.LARGE, FontWeight.NORMAL);
		itemLayout.add(itemText);
		// Add click listener to navigate
		itemLayout.addClickListener(e -> {
			if ((item.path != null) && !item.path.isBlank()) {
				LOGGER.debug("Search result clicked - navigating to path: {}", item.path);
				if (item.path.startsWith("dynamic.")) {
					String dynamicPath = item.path.substring("dynamic.".length());
					String dynamicViewPath = "cdynamicpagerouter/page:" + dynamicPath;
					UI.getCurrent().navigate(dynamicViewPath);
				} else {
					UI.getCurrent().navigate(item.path);
				}
				// Clear search after navigation
				searchField.clear();
			}
		});
		// Style as clickable
		itemLayout.getElement().getStyle().set("cursor", "pointer").set("border-radius", "var(--lumo-border-radius-m)").set("transition",
				"all 0.2s ease");
		// Add hover effects
		itemLayout.getElement().addEventListener("mouseenter",
				e -> itemLayout.getElement().getStyle().set("background-color", "var(--lumo-contrast-5pct)"));
		itemLayout.getElement().addEventListener("mouseleave", e -> itemLayout.getElement().getStyle().remove("background-color"));
		return itemLayout;
	}

	/** Displays search results based on the filter text.
	 * @param filterText The text to filter menu items by */
	private void displaySearchResults(final String filterText) {
		currentLevelContainer.removeAll();
		final VerticalLayout resultsLayout = new VerticalLayout();
		resultsLayout.addClassNames(Padding.NONE, Gap.SMALL);
		resultsLayout.setWidthFull();
		// Filter menu items (case-insensitive) using streams for better readability
		final String lowerFilter = filterText.toLowerCase();
		final List<CMenuItem> filteredItems = allMenuItems.stream().filter(item -> item.name.toLowerCase().contains(lowerFilter))
				.sorted((a, b) -> Double.compare(a.getOrder(), b.getOrder())).toList();
		if (filteredItems.isEmpty()) {
			final Span noResults = new Span("No matching items found");
			noResults.addClassNames(Padding.MEDIUM, TextColor.SECONDARY);
			resultsLayout.add(noResults);
		} else {
			for (final CMenuItem item : filteredItems) {
				resultsLayout.add(createSearchResultItem(item));
			}
		}
		currentLevelContainer.add(resultsLayout);
		// Update header to show search mode
		updateHeaderForSearch();
	}

	/** Get icon color for a dynamic page.
	 * @param path The dynamic page path (format: "dynamic.{pageId}")
	 * @return The color code from CPageEntity, or default color if not found */
	private String getDynamicPageIconColor(final String path) {
		try {
			if ((path == null) || !path.startsWith("dynamic.")) {
				return CColorUtils.getStaticIconColorCode(CDynamicPageRouter.class.getName());
			}
			// Extract page ID from path
			String pageIdStr = path.substring("dynamic.".length());
			Long pageId = Long.parseLong(pageIdStr);
			// Get the page entity and its color
			CPageEntity pageEntity = pageMenuService.getPageEntityById(pageId);
			if (pageEntity != null) {
				return pageEntity.getColor();
			} else {
				return CPageEntity.DEFAULT_COLOR;
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to get dynamic page icon color for path {}: {}", path, e.getMessage());
		}
		// Fallback to default dynamic page color
		try {
			return CColorUtils.getStaticIconColorCode(CDynamicPageRouter.class.getName());
		} catch (Exception e) {
			LOGGER.warn("Failed to get fallback icon color: {}", e.getMessage());
			return "#102bff"; // Hard-coded fallback
		}
	}

	/** Handles back button click events.
	 * @param event The click event */
	private void handleBackButtonClick(final ClickEvent<com.vaadin.flow.component.button.Button> event) {
		LOGGER.debug("Back button clicked from level: {}", currentLevel != null ? currentLevel.getLevelKey() : "unknown");
		if ((currentLevel != null) && (currentLevel.getParent() != null)) {
			showLevel(currentLevel.getParent().getLevelKey());
		}
	}

	/** Initializes CSS styles for the hierarchical menu. */
	private void initializeStyles() {
		LOGGER.debug("Applying CSS styles to CHierarchicalSideMenu");
		getElement().getStyle().set("overflow", "hidden").set("transition", "all 0.3s ease-in-out");
		addClassNames("hierarchical-side-menu");
	}

	/** Check if a path represents a dynamic page.
	 * @param path The path to check
	 * @return true if this is a dynamic page path */
	private boolean isDynamicPagePath(final String path) {
		return (path != null) && path.startsWith("dynamic.");
	}

	/** Handles search field value changes.
	 * @param value The new search value */
	private void on_textFieldSearch_valueChanged(final String value) {
		if ((value == null) || value.trim().isEmpty()) {
			// Clear search - show current level
			showLevel(currentLevel != null ? currentLevel.getLevelKey() : "root");
		} else {
			// Display filtered results
			displaySearchResults(value.trim());
		}
	}

	/** Parse hierarchical order from entry order. The order value encodes the hierarchy: - For order 4.1: integer part 4 is parent order, fractional
	 * part 0.1 represents child order 1 - For order 5.0: top-level item with order 5 - For order 4.12: could represent 4.1.2 (three levels) This
	 * method extracts the appropriate order component for each hierarchy level.
	 * @param order      The order from MenuEntry (e.g., 4.1, 5.23)
	 * @param levelCount Number of hierarchy levels
	 * @return Array of order values for each level */
	private Double[] parseHierarchicalOrder(Double order, int levelCount) {
		Double[] orderComponents = new Double[levelCount];
		if (order == null) {
			// Use default order for all levels
			for (int i = 0; i < levelCount; i++) {
				orderComponents[i] = 999.0;
			}
			return orderComponents;
		}
		// Extract integer part (parent order) and fractional part (child orders)
		int integerPart = (int) Math.floor(order);
		double fractionalPart = order - integerPart;
		if (levelCount == 1) {
			// Single level - use the full order
			orderComponents[0] = order;
		} else if (levelCount == 2) {
			// Two levels - integer part for parent, fractional part for child
			orderComponents[0] = (double) integerPart;
			// Fractional part 0.1 represents child order 1, 0.2 represents 2, etc.
			orderComponents[1] = fractionalPart * 10.0;
		} else {
			// Three or more levels - distribute the fractional part
			// For order 4.123: parent=4, child1=1, child2=2, child3=3
			orderComponents[0] = (double) integerPart;
			String fractionalStr = String.format("%.10f", fractionalPart).substring(2); // Remove "0."
			for (int i = 1; i < levelCount; i++) {
				if ((i - 1) < fractionalStr.length()) {
					char digit = fractionalStr.charAt(i - 1);
					orderComponents[i] = (double) Character.getNumericValue(digit);
				} else {
					orderComponents[i] = 999.0; // Default for missing components
				}
			}
		}
		return orderComponents;
	}

	/** Processes a single menu entry and adds it to the appropriate level.
	 * @param menuEntry The menu entry to process
	 * @throws Exception */
	private void processMenuEntry(final MenuEntry menuEntry) throws Exception {
		Check.notNull(menuEntry, "Menu entry must not be null");
		String title = menuEntry.title();
		final String path = menuEntry.path();
		final String iconName = menuEntry.icon();
		final Double entryOrder = menuEntry.order();
		boolean isDynamic = title.startsWith("dynamic/");
		if (isDynamic) {
			title = title.replace("dynamic/", "");
		}
		Check.notBlank(title, "Menu entry title must not be blank");
		// Split title by dots to get hierarchy levels (up to 4 levels)
		final String[] titleParts = title.split("\\.");
		final int levelCount = Math.min(titleParts.length, MAX_MENU_LEVELS);
		// Parse hierarchical order components if order contains dots (e.g., "4.1" means parent order 4, child order 1)
		// Otherwise, use the same order for all levels
		Double[] orderComponents = parseHierarchicalOrder(entryOrder, levelCount);
		// Ensure all parent levels exist
		String currentLevelKey = "root";
		for (int i = 0; i < (levelCount - 1); i++) {
			final String levelName = titleParts[i].trim();
			final String childLevelKey = currentLevelKey + "." + levelName;
			if (!menuLevels.containsKey(childLevelKey)) {
				final CMenuLevel parentLevel = menuLevels.get(currentLevelKey);
				final CMenuLevel newLevel = new CMenuLevel(childLevelKey, levelName, parentLevel);
				menuLevels.put(childLevelKey, newLevel);
				// Add navigation item to parent level with appropriate order component
				parentLevel.addNavigationItem(menuEntry.menuClass(), levelName, iconName, childLevelKey, orderComponents[i]);
			}
			currentLevelKey = childLevelKey;
		}
		// Add final menu item (leaf node) to the current level with its order component
		if (levelCount > 0) {
			final String itemName = titleParts[levelCount - 1].trim();
			final CMenuLevel targetLevel = menuLevels.get(currentLevelKey);
			if (targetLevel != null) {
				CMenuItem menuItem = targetLevel.addMenuItem(menuEntry.menuClass(), itemName, iconName, path, orderComponents[levelCount - 1]);
				pageTestAuxillaryService.addRoute(itemName, menuItem.iconName, menuItem.iconColor, path);
			}
		}
	}

	/** Refreshes the current level display to update highlighting. */
	private void refreshCurrentLevel() {
		if (currentLevel != null) {
			currentLevelContainer.removeAll();
			currentLevelContainer.add(currentLevel.createLevelComponent());
		}
	}

	/** Shows the specified menu level with sliding animation.
	 * @param levelKey The key of the level to show */
	private void showLevel(final String levelKey) {
		final CMenuLevel level = menuLevels.get(levelKey);
		if (level == null) {
			LOGGER.warn("Menu level '{}' not found", levelKey);
			return;
		}
		// Update navigation path
		updateNavigationPath(levelKey);
		// Update header with back button if not at root
		updateHeader(level);
		// Clear current level container and add new level
		currentLevelContainer.removeAll();
		currentLevelContainer.add(level.createLevelComponent());
		// Add sliding animation class
		currentLevelContainer.addClassName("slide-in");
		// Store current level reference
		currentLevel = level;
		// LOGGER.debug("Menu level '{}' displayed successfully", levelKey);
	}

	/** Updates the header with appropriate back button and title.
	 * @param level The current menu level */
	private void updateHeader(final CMenuLevel level) {
		headerLayout.removeAll();
		// Always add an icon area with consistent width
		Icon levelIcon;
		if (level.getParent() != null) {
			// Add back button with consistent sizing
			levelIcon = createMenuIcon(VaadinIcon.ARROW_LEFT.create(), "var(--lumo-primary-color)");
			final CButton backButton = new CButton("", levelIcon, this::handleBackButtonClick);
			backButton.addClassNames(BACK_BUTTON_CLASS);
			backButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE);
			// backButton.getStyle().set("min-width", "40px").set("min-height", "40px");
			headerLayout.add(backButton);
		} else {
			levelIcon = createMenuIcon(VaadinIcon.CUBES.create(), "var(--lumo-primary-color)");
			levelIcon.addClassNames(Margin.Right.MEDIUM, Margin.Left.SMALL);
			headerLayout.add(levelIcon);
		}
		// Add level title with consistent font size
		final Span levelTitle = new Span(level.getDisplayName());
		levelTitle.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);
		headerLayout.add(levelTitle);
		// Add spacer to push content to the left
		final Div spacer = new CDiv();
		headerLayout.add(spacer);
		headerLayout.setFlexGrow(1, spacer);
	}

	/** Updates the header to show search mode. */
	private void updateHeaderForSearch() {
		headerLayout.removeAll();
		final Icon searchIcon = createMenuIcon(VaadinIcon.SEARCH.create(), "var(--lumo-primary-color)");
		searchIcon.addClassNames(Margin.Right.MEDIUM, Margin.Left.SMALL);
		headerLayout.add(searchIcon);
		final Span title = new Span("Search Results");
		title.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);
		headerLayout.add(title);
		final Div spacer = new CDiv();
		headerLayout.add(spacer);
		headerLayout.setFlexGrow(1, spacer);
	}

	/** Updates the navigation path based on the current level.
	 * @param levelKey The current level key */
	private void updateNavigationPath(final String levelKey) {
		navigationPath.clear();
		if ("root".equals(levelKey)) {
			return;
		}
		// Build path from root to current level
		final String[] pathParts = levelKey.split("\\.");
		String currentPath = "";
		for (final String part : pathParts) {
			if ("root".equals(part)) {
				currentPath = "root";
			} else {
				currentPath = currentPath.isEmpty() ? part : currentPath + "." + part;
			}
			navigationPath.add(currentPath);
		}
	}
}
