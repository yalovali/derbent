package tech.derbent.base.ui.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
import tech.derbent.abstracts.utils.CColorUtils;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.components.CButton;

/** CHierarchicalSideMenu - A hierarchical side menu component with up to 4 levels of navigation. Layer: View (MVC) Features: - Supports up to 4
 * levels of menu hierarchy - Sliding animations between levels - Back button navigation - Parses menu entries from route annotations in format:
 * parentItem2.childItem1.childofchileitem1 - Responsive design with proper styling - Current page highlighting */
public final class CHierarchicalSideMenu extends Div implements AfterNavigationObserver {

	/** Inner class representing a single menu item. */
	private final class CMenuItem {

		private final String name;
		private final String iconName;
		private final String path;
		private final String targetLevelKey;
		private final boolean isNavigation;
		private final String iconColor;

		public CMenuItem(final Class<? extends Component> clazz, final String name, final String iconName, final String path,
				final String targetLevelKey, final boolean isNavigation) throws Exception {
			this.name = name;
			if (iconName.startsWith("class:")) {
				// get icon from class
				try {
					this.iconName = CColorUtils.getStaticIconFilename(iconName.replace("class:", ""));
				} catch (Exception e) {
					LOGGER.warn("Check route information for icon retrieval. Probably missing Class path or function.");
					throw e;
				}
			} else {
				// get icon directly
				this.iconName = iconName;
			}
			this.path = path;
			this.targetLevelKey = targetLevelKey;
			this.isNavigation = isNavigation;
			// get icon with full class name
			this.iconColor = CColorUtils.getStaticIconColorCode(clazz.getName());
		}

		public Component createComponent() {
			final HorizontalLayout itemLayout = new HorizontalLayout();
			itemLayout.addClassNames(Display.FLEX, AlignItems.CENTER, Padding.MEDIUM, Gap.MEDIUM, MENU_ITEM_CLASS);
			itemLayout.setWidthFull();
			// Check if this item represents the current page
			final boolean isCurrentPage = (path != null) && !path.trim().isEmpty() && (currentRoute != null) && currentRoute.equals(path.trim());
			// Add icon with consistent sizing and colorful styling
			Icon icon;
			if ((iconName != null) && !iconName.trim().isEmpty()) {
				icon = new Icon(iconName);
			} else {
				// Use a transparent placeholder icon to maintain consistent spacing
				icon = VaadinIcon.CIRCLE.create();
				icon.getStyle().set("visibility", "hidden");
			}
			// set color from iconColor if available
			if ((iconColor != null) && !iconColor.trim().isEmpty()) {
				icon.getStyle().set("color", iconColor);
			}
			icon.addClassNames(IconSize.MEDIUM);
			icon.getStyle().set("min-width", "24px").set("min-height", "24px");
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
				final Icon navIcon = VaadinIcon.ANGLE_RIGHT.create();
				navIcon.addClassNames(IconSize.MEDIUM);
				navIcon.getStyle().set("color", "var(--lumo-primary-color)");
				navIcon.getStyle().set("min-width", "24px").set("min-height", "24px");
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

		private void handleItemClick(final com.vaadin.flow.component.ClickEvent<HorizontalLayout> event) {
			if (isNavigation && (targetLevelKey != null)) {
				// Navigate to sub-level
				showLevel(targetLevelKey);
			} else if ((path != null) && !path.trim().isEmpty()) {
				// Navigate to actual page
				LOGGER.debug("Navigating to path: {}", path);
				com.vaadin.flow.component.UI.getCurrent().navigate(path);
			}
		}
	}

	/** Inner class representing a single level in the menu hierarchy. */
	private final class CMenuLevel {

		private final String levelKey;
		private final String displayName;
		private final CMenuLevel parent;
		private final List<CMenuItem> items;

		public CMenuLevel(final String levelKey, final String displayName, final CMenuLevel parent) {
			this.levelKey = levelKey;
			this.displayName = displayName;
			this.parent = parent;
			this.items = new ArrayList<>();
		}

		public void addMenuItem(final Class<? extends Component> clazz, final String name, final String iconName, final String path)
				throws Exception {
			final CMenuItem item = new CMenuItem(clazz, name, iconName, path, null, false);
			items.add(item);
		}

		public void addNavigationItem(final Class<? extends Component> clazz, final String name, final String iconName, final String targetLevelKey)
				throws Exception {
			final CMenuItem item = new CMenuItem(clazz, name, iconName, null, targetLevelKey, true);
			items.add(item);
		}

		public Component createLevelComponent() {
			final VerticalLayout levelLayout = new VerticalLayout();
			levelLayout.addClassNames(Padding.NONE, Gap.SMALL);
			levelLayout.setWidthFull();
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

	private static final long serialVersionUID = 1L;
	private static final int MAX_MENU_LEVELS = 4;
	// Styling constants
	private static final String MENU_ITEM_CLASS = "hierarchical-menu-item";
	private static final String BACK_BUTTON_CLASS = "hierarchical-back-button";
	private static final String LEVEL_CONTAINER_CLASS = "hierarchical-level-container";
	private static final String HEADER_CLASS = "hierarchical-header";
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	// Menu structure components
	private final VerticalLayout menuContainer;
	private final HorizontalLayout headerLayout;
	private final Div currentLevelContainer;
	// Navigation state
	private final List<String> navigationPath;
	private final Map<String, CMenuLevel> menuLevels;
	private CMenuLevel currentLevel;
	private String currentRoute; // Track current route for highlighting

	/** Constructor initializes the hierarchical side menu component.
	 * @throws Exception */
	public CHierarchicalSideMenu() throws Exception {
		this.navigationPath = new ArrayList<>();
		this.menuLevels = new HashMap<>();
		// Initialize main container
		menuContainer = new VerticalLayout();
		menuContainer.addClassNames(Padding.NONE, Gap.SMALL);
		menuContainer.setWidthFull();
		// Initialize header with back button area
		headerLayout = new HorizontalLayout();
		headerLayout.addClassNames(Display.FLEX, AlignItems.CENTER, Padding.MEDIUM, HEADER_CLASS);
		headerLayout.setWidthFull();
		// Initialize current level display container
		currentLevelContainer = new Div();
		currentLevelContainer.addClassNames(LEVEL_CONTAINER_CLASS);
		currentLevelContainer.setWidthFull();
		// Build menu structure from annotations
		buildMenuHierarchy();
		// Add components to main container
		menuContainer.add(headerLayout, currentLevelContainer);
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
	 * @throws Exception */
	private void buildMenuHierarchy() throws Exception {
		final var rootLevel = new CMenuLevel("root", "Homepage", null);
		menuLevels.put("root", rootLevel);
		// Get menu entries from MenuConfiguration
		final var menuEntries = MenuConfiguration.getMenuEntries();
		for (final MenuEntry menuEntry : menuEntries) {
			processMenuEntry(menuEntry);
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
		getElement().getStyle().set("overflow", "hidden").set("transition", "all 0.3s ease-in-out");
		addClassNames("hierarchical-side-menu");
	}

	/** Processes a single menu entry and adds it to the appropriate level.
	 * @param menuEntry The menu entry to process
	 * @throws Exception */
	private void processMenuEntry(final MenuEntry menuEntry) throws Exception {
		Check.notNull(menuEntry, "Menu entry must not be null");
		final String title = menuEntry.title();
		final String path = menuEntry.path();
		final String icon = menuEntry.icon();
		// get menu view class color
		// final String iconColor = CIconSetLoadergetStaticIconColorCode(menuEntry.menu);
		Check.notBlank(title, "Menu entry title must not be blank");
		// Split title by dots to get hierarchy levels (up to 4 levels)
		final String[] titleParts = title.split("\\.");
		final int levelCount = Math.min(titleParts.length, MAX_MENU_LEVELS);
		// Ensure all parent levels exist
		String currentLevelKey = "root";
		for (int i = 0; i < (levelCount - 1); i++) {
			final String levelName = titleParts[i].trim();
			final String childLevelKey = currentLevelKey + "." + levelName;
			if (!menuLevels.containsKey(childLevelKey)) {
				final CMenuLevel parentLevel = menuLevels.get(currentLevelKey);
				final CMenuLevel newLevel = new CMenuLevel(childLevelKey, levelName, parentLevel);
				menuLevels.put(childLevelKey, newLevel);
				// Add navigation item to parent level
				parentLevel.addNavigationItem(menuEntry.menuClass(), levelName, icon, childLevelKey);
			}
			currentLevelKey = childLevelKey;
		}
		// Add final menu item (leaf node) to the current level
		if (levelCount > 0) {
			final String itemName = titleParts[levelCount - 1].trim();
			final CMenuLevel targetLevel = menuLevels.get(currentLevelKey);
			if (targetLevel != null) {
				targetLevel.addMenuItem(menuEntry.menuClass(), itemName, icon, path);
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
			levelIcon = VaadinIcon.ARROW_LEFT.create();
			levelIcon.addClassNames(IconSize.MEDIUM);
			levelIcon.getStyle().set("color", "var(--lumo-primary-color)");
			levelIcon.getStyle().set("min-width", "24px").set("min-height", "24px");
			final CButton backButton = new CButton("", levelIcon, this::handleBackButtonClick);
			backButton.addClassNames(BACK_BUTTON_CLASS, Margin.Right.MEDIUM);
			backButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE);
			backButton.getStyle().set("min-width", "40px").set("min-height", "40px");
			headerLayout.add(backButton);
		} else {
			// Add app icon for root level to prevent label jumping
			levelIcon = VaadinIcon.CUBES.create();
			levelIcon.addClassNames(IconSize.MEDIUM, Margin.Right.MEDIUM);
			levelIcon.getStyle().set("color", "var(--lumo-primary-color)");
			levelIcon.getStyle().set("min-width", "24px").set("min-height", "24px");
			headerLayout.add(levelIcon);
		}
		// Add level title with consistent font size
		final Span levelTitle = new Span(level.getDisplayName());
		levelTitle.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);
		headerLayout.add(levelTitle);
		// Add spacer to push content to the left
		final Div spacer = new Div();
		spacer.setWidthFull();
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
