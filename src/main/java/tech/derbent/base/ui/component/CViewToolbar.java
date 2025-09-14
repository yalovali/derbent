package tech.derbent.base.ui.component;

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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import tech.derbent.abstracts.interfaces.CProjectListChangeListener;
import tech.derbent.abstracts.utils.CColorUtils;
import tech.derbent.abstracts.views.CAbstractNamedEntityPage;
import tech.derbent.abstracts.views.components.CButton;
import tech.derbent.abstracts.views.components.CDiv;
import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.base.service.CRouteDiscoveryService;
import tech.derbent.gannt.view.CProjectGanntView;
import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.view.CProjectsView;
import tech.derbent.screens.view.CDetailSectionView;
import tech.derbent.session.service.CLayoutService;
import tech.derbent.session.service.CSessionService;
import tech.derbent.setup.service.CSystemSettingsService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.view.CUsersView;

/* CViewToolbar.java This class defines a toolbar for views in the application, providing a consistent header with a title and optional action
 * components. It extends Composite to allow for easy composition of the toolbar's content. */
public final class CViewToolbar<EntityClass extends CAbstractNamedEntityPage<?>> extends Composite<Header> implements CProjectListChangeListener {

	private static final long serialVersionUID = 1L;

	/* just used to create a group of components with nice styling. Not related to a toolbar */
	public static Component group(final Component... components) {
		final var group = new Div(components);
		group.addClassNames(Display.FLEX, FlexDirection.COLUMN, AlignItems.STRETCH, Gap.SMALL, FlexDirection.Breakpoint.Medium.ROW,
				AlignItems.Breakpoint.Medium.CENTER);
		return group;
	}

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private final H1 title;
	private final CSessionService sessionService;
	private final CLayoutService layoutService;
	private final AuthenticationContext authenticationContext;
	private final CSystemSettingsService systemSettingsService;
	private final CRouteDiscoveryService routeDiscoveryService;
	private ComboBox<CProject> projectComboBox;
	private CButton layoutToggleButton;
	private Avatar userAvatar;
	private Span usernameSpan;

	/** Constructs a CViewToolbar with a title, services, and optional components.
	 * @param viewTitle             The title of the view to be displayed in the toolbar.
	 * @param sessionService        The session service for managing project selection.
	 * @param layoutService         The layout service for managing layout mode (optional).
	 * @param authenticationContext The authentication context for user information (optional).
	 * @param components            Optional components to be added to the toolbar.
	 * @throws Exception */
	public CViewToolbar(final String viewTitle, final CSessionService sessionService, final CLayoutService layoutService,
			final AuthenticationContext authenticationContext, final Component... components) throws Exception {
		this(viewTitle, sessionService, layoutService, authenticationContext, null, null, components);
	}

	/** Constructs a CViewToolbar with a title, services, and optional components.
	 * @param viewTitle             The title of the view to be displayed in the toolbar.
	 * @param sessionService        The session service for managing project selection.
	 * @param layoutService         The layout service for managing layout mode (optional).
	 * @param authenticationContext The authentication context for user information (optional).
	 * @param systemSettingsService The system settings service for last visited functionality (optional).
	 * @param routeDiscoveryService The route discovery service for dynamic routes (optional).
	 * @param components            Optional components to be added to the toolbar.
	 * @throws Exception */
	public CViewToolbar(final String viewTitle, final CSessionService sessionService, final CLayoutService layoutService,
			final AuthenticationContext authenticationContext, final CSystemSettingsService systemSettingsService,
			final CRouteDiscoveryService routeDiscoveryService, final Component... components) throws Exception {
		this.sessionService = sessionService;
		this.layoutService = layoutService;
		this.authenticationContext = authenticationContext;
		this.systemSettingsService = systemSettingsService;
		this.routeDiscoveryService = routeDiscoveryService;
		addClassNames(Display.FLEX, FlexDirection.ROW, JustifyContent.BETWEEN, AlignItems.CENTER, Gap.MEDIUM);
		// Add separation line below the toolbar
		getContent().getStyle().set("border-bottom", "1px solid var(--lumo-contrast-20pct)");
		// this is a button that toggles the drawer in the app layout
		final var drawerToggle = new DrawerToggle();
		drawerToggle.addClassNames(Margin.NONE);
		// Add Home button to navigate to dashboard
		final var homeButton = createHomeButton();
		title = new H1(viewTitle);
		title.addClassNames(FontSize.XLARGE, Margin.NONE, FontWeight.LIGHT);
		title.setMinWidth("300px"); // Ensure title has a minimum width
		// Create project selection combobox
		createProjectComboBox();
		// Create quick access toolbar with colorful icons
		final var quickAccessToolbar = createQuickAccessToolbar();
		// Left side: toggle, home button, title, quick access, and project selector
		final var projectSelector = new Div(new Span("Active Project:"), projectComboBox);
		projectSelector.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL);
		final var leftSide = new Div(drawerToggle, homeButton, title, quickAccessToolbar, projectSelector);
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
	public CViewToolbar(final String viewTitle, final CSessionService sessionService, final Component... components) throws Exception {
		this(viewTitle, sessionService, null, null, components);
	}

	/** Creates a colorful icon button for the quick access toolbar. */
	private CButton createColorfulIconButton(final Icon icon, final String tooltip, final String iconColor, final String route) {
		icon.addClassNames(IconSize.MEDIUM); // Use same size as menu icons
		icon.getStyle().set("color", iconColor);
		final CButton button = new CButton("", icon, null);
		button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
		button.getElement().setAttribute("title", tooltip);
		button.addClassNames(Margin.NONE);
		// Add hover effect
		final String originalColor = iconColor;
		final String hoverColor = iconColor + "99"; // Add transparency for hover
		button.getElement().addEventListener("mouseenter", e -> icon.getStyle().set("color", hoverColor));
		button.getElement().addEventListener("mouseleave", e -> icon.getStyle().set("color", originalColor));
		// Navigate to route
		button.addClickListener(event -> {
			LOGGER.info("{} button clicked, navigating to {}", tooltip, route);
			UI.getCurrent().navigate(route);
		});
		return button;
	}

	/** Creates the home button that navigates to the dashboard.
	 * @return the home button */
	private CButton createHomeButton() {
		final Icon homeIcon = VaadinIcon.HOME.create();
		homeIcon.addClassNames(IconSize.MEDIUM);
		// Add colorful styling to the home icon
		homeIcon.getStyle().set("color", "var(--lumo-primary-color)");
		final CButton homeButton = new CButton(null, homeIcon, null);
		homeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
		homeButton.getElement().setAttribute("title", "Go to Dashboard");
		homeButton.addClassNames(Margin.NONE);
		// Add hover effect for better UX
		homeButton.getElement().addEventListener("mouseenter", e -> homeIcon.getStyle().set("color", "var(--lumo-primary-color-50pct)"));
		homeButton.getElement().addEventListener("mouseleave", e -> homeIcon.getStyle().set("color", "var(--lumo-primary-color)"));
		// Handle home button click - navigate to dashboard
		homeButton.addClickListener(event -> {
			LOGGER.info("Home button clicked, navigating to dashboard");
			com.vaadin.flow.component.UI.getCurrent().navigate("home");
		});
		return homeButton;
	}

	/** Creates the layout toggle button. */
	private void createLayoutToggleButton() {
		if (layoutService != null) {
			layoutToggleButton = new CButton();
			layoutToggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
			layoutToggleButton.getElement().setAttribute("title", "Toggle Layout Mode");
			// Set initial icon based on current layout mode
			updateLayoutToggleIcon();
			// Handle layout toggle
			layoutToggleButton.addClickListener(event -> {
				LOGGER.info("Layout toggle button clicked");
				if (layoutService != null) {
					final CLayoutService.LayoutMode oldMode = layoutService.getCurrentLayoutMode();
					layoutService.toggleLayoutMode();
					final CLayoutService.LayoutMode newMode = layoutService.getCurrentLayoutMode();
					LOGGER.info("Layout toggled from {} to {}", oldMode, newMode);
					updateLayoutToggleIcon();
					// Try to force UI update, but handle case where push is not enabled
					final var ui = getUI();
					if (ui.isPresent()) {
						ui.get().access(() -> {
							LOGGER.debug("Forcing UI update after layout toggle");
							try {
								ui.get().push();
								LOGGER.debug("UI push successful after layout toggle");
							} catch (final IllegalStateException e) {
								if (e.getMessage() != null && e.getMessage().contains("Push not enabled")) {
									LOGGER.debug("Push not enabled, layout change will be reflected on next user interaction");
								} else {
									LOGGER.warn("Error during UI push in toolbar: {}", e.getMessage());
								}
							} catch (final Exception e) {
								LOGGER.warn("Error during UI push in toolbar: {}", e.getMessage());
							}
						});
					}
				} else {
					LOGGER.warn("LayoutService is null, cannot toggle layout mode");
				}
			});
		} else {
			LOGGER.debug("LayoutService is null, layout toggle button will not be created");
		}
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
		if (sessionService == null) {
			LOGGER.error("SessionService is null, cannot create project ComboBox");
			return;
		}
		projectComboBox = new ComboBox<>();
		projectComboBox.setItemLabelGenerator(CProject::getName);
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

	/** Creates a quick access toolbar with colorful icons for commonly used features.
	 * @return the quick access toolbar
	 * @throws Exception */
	private Div createQuickAccessToolbar() throws Exception {
		final CButton ganntButton = createNavigateButtonForView(CProjectGanntView.class);
		final CButton projectsButton = createNavigateButtonForView(CProjectsView.class);
		final CButton meetingsButton = createNavigateButtonForView(CMeetingsView.class);
		final CButton activitiesButton = createNavigateButtonForView(CActivitiesView.class);
		final CButton usersButton = createNavigateButtonForView(CUsersView.class);
		final CButton screensButton = createNavigateButtonForView(CDetailSectionView.class);
		// Create last visited button
		final CButton lastVisitedButton = createLastVisitedButton();
		return new CDiv(lastVisitedButton, ganntButton, projectsButton, usersButton, activitiesButton, meetingsButton, screensButton);
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
		if ((userAvatar != null) && (usernameSpan != null)) {
			rightSide.add(userAvatar, usernameSpan);
		}
		if (layoutToggleButton != null) {
			rightSide.add(layoutToggleButton);
		}
		return rightSide;
	}

	/** Creates user avatar and username components. */
	private void createUserComponents(final String username) {
		if ((username == null) || username.trim().isEmpty()) {
			LOGGER.warn("Cannot create user components - username is null or empty");
			return;
		}
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
		if (authenticationContext != null) {
			// Try to get user from session service first
			if (sessionService != null) {
				final var activeUser = sessionService.getActiveUser();
				if (activeUser.isPresent()) {
					final CUser user = activeUser.get();
					final String username = (user.getName() != null) && !user.getName().trim().isEmpty() ? user.getName() : user.getLogin();
					if ((username != null) && !username.trim().isEmpty()) {
						createUserComponents(username);
						return;
					}
				}
			}
			// Fallback to authentication context
			final var authenticatedUser = authenticationContext.getAuthenticatedUser(User.class);
			if (authenticatedUser.isPresent()) {
				final String username = authenticatedUser.get().getUsername();
				if ((username != null) && !username.trim().isEmpty()) {
					createUserComponents(username);
				} else {
					LOGGER.warn("Authenticated user has null or empty username");
				}
			} else {
				LOGGER.debug("No authenticated user found in authentication context");
			}
		} else {
			LOGGER.debug("AuthenticationContext is null, user info components will not be created");
		}
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
		if ((sessionService != null) && (projectComboBox != null)) {
			try {
				final List<CProject> projects = sessionService.getAvailableProjects();
				if (projects == null) {
					LOGGER.warn("SessionService returned null project list");
					return;
				}
				projectComboBox.setItems(projects);
				// If no project is selected but projects are available, select the first
				// one
				if ((projectComboBox.getValue() == null) && !projects.isEmpty()) {
					projectComboBox.setValue(projects.get(0));
				}
			} catch (final Exception e) {
				LOGGER.error("Error refreshing project list", e);
			}
		}
	}

	public void setPageTitle(final String title) {
		if ((title == null) || title.trim().isEmpty()) {
			LOGGER.warn("Cannot set page title - title is null or empty");
			return;
		}
		this.title.setText(title);
	}

	/** Updates the layout toggle button icon based on current layout mode. */
	private void updateLayoutToggleIcon() {
		if ((layoutToggleButton != null) && (layoutService != null)) {
			final CLayoutService.LayoutMode currentMode = layoutService.getCurrentLayoutMode();
			if (currentMode == null) {
				LOGGER.warn("Current layout mode is null, using default VERTICAL");
				return;
			}
			final Icon icon = currentMode == CLayoutService.LayoutMode.HORIZONTAL ? VaadinIcon.GRID_H.create() : VaadinIcon.GRID_V.create();
			icon.addClassNames(IconSize.MEDIUM); // Use same size as menu icons
			layoutToggleButton.setIcon(icon);
			layoutToggleButton.getElement().setAttribute("title", "Current: " + currentMode + " - Click to toggle");
		}
	}

	/** Creates the last visited button for quick access to the last visited page. */
	private CButton createLastVisitedButton() {
		final Icon icon = VaadinIcon.CLOCK.create();
		icon.addClassNames(IconSize.MEDIUM);
		icon.getStyle().set("color", "#e67e22"); // Orange color for last visited
		final CButton lastVisitedButton = new CButton("", icon, null);
		lastVisitedButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
		lastVisitedButton.getElement().setAttribute("title", "Go to Last Visited Page");
		lastVisitedButton.addClassNames(Margin.NONE);
		// Add hover effect
		lastVisitedButton.getElement().addEventListener("mouseenter", e -> icon.getStyle().set("color", "#e67e2299"));
		lastVisitedButton.getElement().addEventListener("mouseleave", e -> icon.getStyle().set("color", "#e67e22"));
		// Handle click - navigate to last visited page
		lastVisitedButton.addClickListener(event -> {
			try {
				String lastVisitedRoute = getLastVisitedRoute();
				if (lastVisitedRoute != null && !lastVisitedRoute.trim().isEmpty()) {
					LOGGER.info("Last visited button clicked, navigating to: {}", lastVisitedRoute);
					UI.getCurrent().navigate(lastVisitedRoute);
				} else {
					LOGGER.info("No last visited route found, navigating to home");
					UI.getCurrent().navigate("home");
				}
			} catch (Exception e) {
				LOGGER.warn("Error navigating to last visited page: {}", e.getMessage());
				UI.getCurrent().navigate("home");
			}
		});
		return lastVisitedButton;
	}

	/** Gets the last visited route from system settings. */
	private String getLastVisitedRoute() {
		try {
			if (systemSettingsService != null) {
				return systemSettingsService.getLastVisitedView();
			}
		} catch (Exception e) {
			LOGGER.warn("Error getting last visited route: {}", e.getMessage());
		}
		return "home"; // Default fallback
	}
}
