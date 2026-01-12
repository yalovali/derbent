package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.IconSize;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import tech.derbent.api.entity.view.CAbstractNamedEntityPage;
import tech.derbent.api.interfaces.IProjectListChangeListener;
import tech.derbent.api.screens.view.CDetailSectionView;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CColorAwareComboBox;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.CRouteDiscoveryService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.page.service.CPageMenuIntegrationService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.base.session.service.CLayoutService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.setup.service.CSystemSettingsService;
import tech.derbent.base.users.domain.CUser;

/* CViewToolbar.java This class defines a toolbar for views in the application, providing a consistent header with a title and optional action
 * components. It extends Composite to allow for easy composition of the toolbar's content. */
public final class CViewToolbar extends Composite<Header> implements IProjectListChangeListener {

	private static final long serialVersionUID = 1L;

	/* just used to create a group of components with nice styling. Not related to a toolbar */
	public static Component group(final Component... components) {
		final var group = new Div(components);
		group.addClassNames(Display.FLEX, FlexDirection.COLUMN, AlignItems.STRETCH, Gap.SMALL, FlexDirection.Breakpoint.Medium.ROW,
				AlignItems.Breakpoint.Medium.CENTER);
		return group;
	}

	private final AuthenticationContext authenticationContext;
	private final CLayoutService layoutService;
	private CButton layoutToggleButton;
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private final CPageMenuIntegrationService pageMenuIntegrationService;
	private final H1 pageTitle;
	private CColorAwareComboBox<CProject> projectComboBox;
	private final ISessionService sessionService;
	private final CSystemSettingsService systemSettingsService;
	private Avatar userAvatar;
	private Span usernameSpan;

	/** Constructs a CViewToolbar with a title, services, and optional components.
	 * @param viewTitle             The title of the view to be displayed in the toolbar.
	 * @param sessionService        The session service for managing project selection.
	 * @param layoutService         The layout service for managing layout mode (optional).
	 * @param authenticationContext The authentication context for user information (optional).
	 * @param components            Optional components to be added to the toolbar.
	 * @throws Exception */
	public CViewToolbar(final String viewTitle, final ISessionService sessionService, final CLayoutService layoutService,
			final AuthenticationContext authenticationContext, final Component... components) throws Exception {
		this(viewTitle, sessionService, layoutService, authenticationContext, null, null, null, components);
	}

	/** Constructs a CViewToolbar with a title, services, and optional components.
	 * @param viewTitle                  The title of the view to be displayed in the toolbar.
	 * @param sessionService             The session service for managing project selection.
	 * @param layoutService              The layout service for managing layout mode (optional).
	 * @param authenticationContext      The authentication context for user information (optional).
	 * @param systemSettingsService      The system settings service for last visited functionality (optional).
	 * @param routeDiscoveryService      The route discovery service for dynamic routes (optional).
	 * @param pageMenuIntegrationService The page menu integration service for dynamic quick toolbar buttons (optional).
	 * @param components                 Optional components to be added to the toolbar.
	 * @throws Exception */
	public CViewToolbar(final String viewTitle, final ISessionService sessionService, final CLayoutService layoutService,
			final AuthenticationContext authenticationContext, final CSystemSettingsService systemSettingsService,
			final CRouteDiscoveryService routeDiscoveryService, final CPageMenuIntegrationService pageMenuIntegrationService,
			final Component... components) throws Exception {
		this.sessionService = sessionService;
		this.layoutService = layoutService;
		this.authenticationContext = authenticationContext;
		this.systemSettingsService = systemSettingsService;
		this.pageMenuIntegrationService = pageMenuIntegrationService;
		addClassNames(Display.FLEX, FlexDirection.ROW, JustifyContent.BETWEEN, AlignItems.CENTER, Gap.MEDIUM);
		// Add separation line below the toolbar
		getContent().getStyle().set("border-bottom", "1px solid var(--lumo-contrast-20pct)");
		// this is a button that toggles the drawer in the app layout
		final var drawerToggle = new DrawerToggle();
		drawerToggle.addClassNames(Margin.NONE);
		// Add Home button to navigate to dashboard
		final var homeButton = createHomeButton();
		pageTitle = new H1(viewTitle);
		pageTitle.addClassNames(FontSize.XLARGE, Margin.NONE, FontWeight.LIGHT);
		pageTitle.setMinWidth("300px"); // Ensure title has a minimum width
		// Create project selection combobox
		createProjectComboBox();
		// Create quick access toolbar with colorful icons
		final var quickAccessToolbar = createQuickAccessToolbar();
		// Left side: toggle, home button, title, quick access, and project selector
		final var projectSelector = new Div(new Span("Active Project:"), projectComboBox);
		projectSelector.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL);
		final var leftSide = new Div(drawerToggle, homeButton, pageTitle, quickAccessToolbar, projectSelector);
		leftSide.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.MEDIUM);
		// Spacer to push user info and layout toggle to the right
		final var spacer = new Div();
		spacer.getStyle().set("flex-grow", "1");
		// Right side: user info and layout toggle
		final var rightSide = createRightSideComponents();
		// Add all components to the toolbar
		getContent().add(leftSide, spacer, rightSide);
		// add additional components if passed as a parameter
		if (components.length > 0) {
			// If there are additional components, add them before the spacer
			final var actions = new Div(components);
			actions.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL);
			// Re-add components in the correct order
			getContent().removeAll();
			getContent().add(leftSide, actions, spacer, rightSide);
		}
		// Register for project list change notifications
		sessionService.addProjectListChangeListener(this);
	}

	/** Constructs a CViewToolbar with a title and optional components.
	 * @param viewTitle      The title of the view to be displayed in the toolbar.
	 * @param sessionService The session service for managing project selection.
	 * @param components     Optional components to be added to the toolbar.
	 * @throws Exception */
	public CViewToolbar(final String viewTitle, final ISessionService sessionService, final Component... components) throws Exception {
		this(viewTitle, sessionService, null, null, components);
	}

	/** Creates a colorful icon button for the quick access toolbar. */
	private CButton createColorfulIconButton(final Icon icon, final String tooltip, final String iconColor, final String route) {
		Check.notNull(icon, "Icon must not be null");
		Check.notNull(tooltip, "Tooltip must not be null");
		Check.notNull(iconColor, "Icon color must not be null");
		Check.notNull(route, "Route must not be null");
		// Style the icon
		CColorUtils.setIconClassSize(icon, IconSize.MEDIUM); // Use same size as menu icons
		icon.getStyle().set("color", iconColor);
		final CButton button = new CButton("", icon, null);
		button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
		button.getElement().setAttribute("title", tooltip);
		button.addClassNames(Margin.NONE);
		// Add hover effect
		final String originalColor = iconColor;
		final String hoverColor = iconColor + "99"; // Add transparency for hover
		button.getElement().addEventListener("mouseenter", e -> {
			icon.getStyle().set("color", hoverColor);
		});
		button.getElement().addEventListener("mouseleave", e -> {
			icon.getStyle().set("color", originalColor);
		});
		// Navigate to route
		button.addClickListener(e -> {
			LOGGER.info("{} button clicked, navigating to {}", tooltip, route);
			UI.getCurrent().navigate(route);
		});
		return button;
	}

	/** Creates a button for a dynamic page to be shown in the quick access toolbar. */
	private CButton createDynamicPageButton(final CPageEntity page) {
		try {
			Check.notNull(page, "Page must not be null");
			// Get icon for the page
			Icon icon;
			try {
				if (page.getIconString() != null && page.getIconString().startsWith("vaadin:")) {
					icon = CColorUtils.createStyledIcon(page.getIconString());
				} else {
					icon = CColorUtils.createStyledIcon("vaadin:file-text-o"); // Default fallback
				}
				icon.setSize("32px");
			} catch (final Exception e) {
				LOGGER.warn("Could not parse icon '{}' for page '{}', using default {}: {}", page.getIconString(), page.getPageTitle(),
						e.getMessage());
				icon = CColorUtils.createStyledIcon("vaadin:file-text-o");
			}
			// Generate a color for the button based on page ID (for consistency)
			final String[] colors = {
					"#2196F3", "#4CAF50", "#FF9800", "#9C27B0", "#607D8B", "#E91E63", "#795548", "#009688"
			};
			final String color = colors[Math.abs(page.getId().hashCode()) % colors.length];
			// Create the route for the dynamic page
			final String route = page.getRoute(); // This returns "cdynamicpagerouter." + getId()
			return createColorfulIconButton(icon, page.getPageTitle(), color, route);
		} catch (final Exception e) {
			LOGGER.error("Error creating dynamic page button for page '{}': {}", page.getPageTitle(), e.getMessage());
			throw new RuntimeException(e);
		}
	}

	/** Creates the home button that navigates to the dashboard.
	 * @return the home button */
	private CButton createHomeButton() {
		final Icon homeIcon = CColorUtils.createStyledIcon(CColorUtils.CRUD_HOME_ICON, "var(--lumo-primary-color)");
		final CButton homeButton = new CButton(null, homeIcon, null);
		homeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
		homeButton.getElement().setAttribute("title", "Go to Dashboard");
		homeButton.addClassNames(Margin.NONE);
		// Add hover effect for better UX
		homeButton.getElement().addEventListener("mouseenter", e -> homeIcon.getStyle().set("color", "var(--lumo-primary-color-50pct)"));
		homeButton.getElement().addEventListener("mouseleave", e -> homeIcon.getStyle().set("color", "var(--lumo-primary-color)"));
		// Handle home button click - navigate to dashboard
		homeButton.addClickListener(e -> {
			LOGGER.info("Home button clicked, navigating to dashboard");
			com.vaadin.flow.component.UI.getCurrent().navigate("home");
		});
		return homeButton;
	}

	/** Creates the last visited button for quick access to the last visited page. */
	private CButton createLastVisitedButton() {
		final Icon icon = CColorUtils.createStyledIcon("vaadin:clock", "#e67e22"); // Orange color for last visited
		final CButton lastVisitedButton = new CButton("", icon, null);
		lastVisitedButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
		lastVisitedButton.getElement().setAttribute("title", "Go to Last Visited Page");
		lastVisitedButton.addClassNames(Margin.NONE);
		// Add hover effect
		lastVisitedButton.getElement().addEventListener("mouseenter", e -> {
			icon.getStyle().set("color", "#e67e2299");
		});
		lastVisitedButton.getElement().addEventListener("mouseleave", e -> {
			icon.getStyle().set("color", "#e67e22");
		});
		// Handle click - navigate to last visited page
		lastVisitedButton.addClickListener(e -> {
			try {
				final String lastVisitedRoute = getLastVisitedRoute();
				if (lastVisitedRoute != null && !lastVisitedRoute.trim().isEmpty()) {
					LOGGER.info("Last visited button clicked, navigating to: {}", lastVisitedRoute);
					UI.getCurrent().navigate(lastVisitedRoute);
				} else {
					LOGGER.info("No last visited route found, navigating to home");
					UI.getCurrent().navigate("home");
				}
			} catch (final Exception ex) {
				LOGGER.error("Error navigating to last visited page: {}", ex.getMessage());
				UI.getCurrent().navigate("home");
			}
		});
		return lastVisitedButton;
	}

	/** Creates the layout toggle button. */
	private void createLayoutToggleButton() {
		Check.notNull(layoutService, "LayoutService must not be null to create layout toggle button");
		layoutToggleButton = new CButton();
		layoutToggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
		layoutToggleButton.getElement().setAttribute("title", "Toggle Layout Mode");
		// Set initial icon based on current layout mode
		updateLayoutToggleIcon();
		// Handle layout toggle
		layoutToggleButton.addClickListener(e -> {
			LOGGER.info("Layout toggle button clicked");
			Check.notNull(layoutService, "LayoutService is null, cannot toggle layout mode");
			final CLayoutService.LayoutMode oldMode = CLayoutService.getCurrentLayoutMode();
			CLayoutService.toggleLayoutMode();
			final CLayoutService.LayoutMode newMode = CLayoutService.getCurrentLayoutMode();
			LOGGER.info("Layout toggled from {} to {}", oldMode, newMode);
			updateLayoutToggleIcon();
			// Try to force UI update, but handle case where push is not enabled
			final var ui = getUI();
			Check.isTrue(ui.isPresent(), "UI is not present, cannot push layout change");
			ui.get().access(() -> {
				LOGGER.debug("Forcing UI update after layout toggle");
				try {
					ui.get().push();
					LOGGER.debug("UI push successful after layout toggle");
				} catch (final IllegalStateException ex) {
					if (ex.getMessage() != null && ex.getMessage().contains("Push not enabled")) {
						LOGGER.debug("Push not enabled, layout change will be reflected on next user interaction");
					} else {
						LOGGER.warn("Error during UI push in toolbar: {}", ex.getMessage());
					}
				} catch (final Exception ex) {
					LOGGER.warn("Error during UI push in toolbar: {}", ex.getMessage());
				}
			});
		});
	}

	private CButton createNavigateButtonForView(final Class<? extends CAbstractNamedEntityPage<?>> clazz) throws Exception {
		final String title = CColorUtils.getTitleForView(clazz);
		final String route = CColorUtils.getRouteForView(clazz);
		final Icon icon = CColorUtils.getIconForViewClass(clazz);
		final String color = CColorUtils.getStaticIconColorCode(clazz);
		return createColorfulIconButton(icon, title, color, route);
	}

	/** Creates the project selection ComboBox. */
	private void createProjectComboBox() {
		Check.notNull(sessionService, "SessionService must not be null to create project ComboBox");
		projectComboBox = new CColorAwareComboBox<>(CProject.class);
		projectComboBox.setPlaceholder("Select Project");
		projectComboBox.setWidth("200px");
		// Load available projects
		refreshProjectList();
		// Set current active project
		sessionService.getActiveProject().ifPresent(projectComboBox::setValue);
		// Handle project selection change
		projectComboBox.addValueChangeListener(event -> {
			final CProject selectedProject = event.getValue();
			if (selectedProject != null) {
				LOGGER.info("Project changed to: {}", selectedProject.getName());
				sessionService.setActiveProject(selectedProject);
			}
		});
	}

	/** Creates a quick access toolbar with colorful icons for commonly used features. Includes both static page views and dynamic pages marked for
	 * quick toolbar display.
	 * @return the quick access toolbar
	 * @throws Exception */
	private Div createQuickAccessToolbar() throws Exception {
		final List<CButton> buttons = new ArrayList<>();
		// Add last visited button first
		buttons.add(createLastVisitedButton());
		// Add static buttons (these will be deprecated in favor of dynamic pages)
		buttons.add(createNavigateButtonForView(CDetailSectionView.class));
		// Add dynamic page buttons if pageMenuIntegrationService is available
		if (pageMenuIntegrationService != null && pageMenuIntegrationService.isReady()) {
			try {
				final List<CPageEntity> quickToolbarPages = pageMenuIntegrationService.getQuickToolbarPages();
				for (final CPageEntity page : quickToolbarPages) {
					final CButton pageButton = createDynamicPageButton(page);
					buttons.add(pageButton);
					// LOGGER.debug("Added dynamic page button for: {}", page.getPageTitle());
				}
			} catch (final Exception e) {
				LOGGER.warn("Could not load dynamic quick toolbar pages: {}", e.getMessage());
			}
		} else {
			LOGGER.debug("PageMenuIntegrationService not available or not ready for quick toolbar");
		}
		return new CDiv(buttons.toArray(new Component[0]));
	}

	/** Creates the right side components containing user info and layout toggle. */
	private HorizontalLayout createRightSideComponents() {
		final var rightSide = new HorizontalLayout();
		rightSide.setSpacing(true);
		rightSide.setAlignItems(HorizontalLayout.Alignment.CENTER);
		// Create user info components
		createUserInfoComponents();
		// Create layout toggle button
		createLayoutToggleButton();
		// Add components to right side if they are available
		if (userAvatar != null && usernameSpan != null) {
			rightSide.add(userAvatar, usernameSpan);
		}
		if (layoutToggleButton != null) {
			rightSide.add(layoutToggleButton);
		}
		return rightSide;
	}

	/** Creates user avatar and username components. */
	private void createUserComponents(final String username) {
		Check.notBlank(username, "Username must not be null or empty to create user components");
		// Create simple avatar without icon
		userAvatar = new Avatar();
		userAvatar.setName(username);
		userAvatar.setAbbreviation(username.length() > 0 ? username.substring(0, 1).toUpperCase() : "U");
		userAvatar.addThemeVariants(AvatarVariant.LUMO_XSMALL);
		userAvatar.setColorIndex(3);
		userAvatar.getElement().setAttribute("title", "User: " + username);
		// Create username span without icon
		usernameSpan = new Span(username);
		usernameSpan.addClassNames(FontWeight.MEDIUM);
		usernameSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
	}

	/** Creates user info components (avatar and username). */
	private void createUserInfoComponents() {
		Check.notNull(sessionService, "SessionService must not be null to create user info components");
		Check.notNull(authenticationContext, "AuthenticationContext must not be null to create user info components");
		// Try to get user from session service first
		final var activeUser = sessionService.getActiveUser();
		if (activeUser.isPresent()) {
			final CUser user = activeUser.get();
			final String username = user.getName() != null && !user.getName().trim().isEmpty() ? user.getName() : user.getLogin();
			if (username != null && !username.trim().isEmpty()) {
				createUserComponents(username);
				return;
			}
		}
		// Fallback to authentication context
		final var authenticatedUser = authenticationContext.getAuthenticatedUser(User.class);
		if (authenticatedUser.isPresent()) {
			final String username = authenticatedUser.get().getUsername();
			Check.notBlank(username, "Authenticated user's username must not be null or empty");
			createUserComponents(username);
		} else {
			LOGGER.debug("No authenticated user found in authentication context");
		}
	}

	/** Gets the last visited route from system settings. */
	private String getLastVisitedRoute() {
		try {
			if (systemSettingsService != null) {
				return systemSettingsService.getLastVisitedView();
			}
		} catch (final Exception e) {
			LOGGER.warn("Error getting last visited route: {}", e.getMessage());
		}
		return "home"; // Default fallback
	}

	/** Override onAttach to ensure listener registration on component attach. */
	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		// Re-register in case it was missed during construction
		sessionService.addProjectListChangeListener(this);
	}

	/** Override onDetach to clean up listener registration when component is detached. */
	@Override
	protected void onDetach(final DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		// Unregister to prevent memory leaks
		sessionService.removeProjectListChangeListener(this);
	}

	/** Called when the project list changes. Refreshes the ComboBox items. */
	@Override
	public void onProjectListChanged() {
		refreshProjectList();
	}

	/** Refreshes the project list in the ComboBox. */
	public void refreshProjectList() {
		// LOGGER.debug("Refreshing project list in toolbar ComboBox");
		Check.notNull(sessionService, "SessionService must not be null to refresh project list");
		Check.notNull(projectComboBox, "Project ComboBox must not be null to refresh project list");
		try {
			final List<CProject> projects = sessionService.getAvailableProjects();
			if (projects == null) {
				LOGGER.warn("SessionService returned null project list");
				return;
			}
			projectComboBox.setItems(projects);
			// If no project is selected but projects are available, select the first
			// one
			if (projectComboBox.getValue() == null && !projects.isEmpty()) {
				projectComboBox.setValue(projects.get(0));
			}
		} catch (final Exception e) {
			LOGGER.error("Error refreshing project list", e);
		}
	}

	public void setPageTitle(final String title) {
		Check.notBlank(title, "Title must not be null or empty to set page title");
		// Get company name and append to title if available
		// final String displayTitle = title;
		// try {
		// if (sessionService != null) {
		// final var company = sessionService.getCurrentCompany();
		// if (company != null) {
		// displayTitle = title + " - " + company.getName();
		// }
		// }
		// } catch (final Exception e) {
		// LOGGER.debug("Could not get company name for title: {}", e.getMessage());
		// }
		pageTitle.setText(title);
	}

	/** Updates the layout toggle button icon based on current layout mode. */
	private void updateLayoutToggleIcon() {
		Check.notNull(layoutService, "LayoutService must not be null to update layout toggle icon");
		Check.notNull(layoutToggleButton, "Layout toggle button must not be null to update its icon");
		final CLayoutService.LayoutMode currentMode = CLayoutService.getCurrentLayoutMode();
		Check.notNull(currentMode, "Current layout mode must not be null to update layout toggle icon");
		final String iconString =
				currentMode == CLayoutService.LayoutMode.HORIZONTAL ? CColorUtils.CRUD_LAYOUT_HORIZONTAL_ICON : CColorUtils.CRUD_LAYOUT_VERTICAL_ICON;
		final Icon icon = CColorUtils.createStyledIcon(iconString);
		layoutToggleButton.setIcon(icon);
		layoutToggleButton.getElement().setAttribute("title", "Current: " + currentMode + " - Click to toggle");
	}
}
