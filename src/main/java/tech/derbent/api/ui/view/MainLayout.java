package tech.derbent.api.ui.view;

import java.io.ByteArrayInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.IconSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.interfaces.IPageTitleProvider;
import tech.derbent.api.page.service.CPageMenuIntegrationService;
import tech.derbent.api.ui.component.enhanced.CHierarchicalSideMenu;
import tech.derbent.api.ui.component.enhanced.CViewToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.ui.theme.CFontSizeService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.CRouteDiscoveryService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CPageTestAuxillaryService;
import tech.derbent.base.session.service.CLayoutService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.setup.service.CSystemSettingsService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.base.users.view.CDialogUserProfile;

/** The main layout is a top-level placeholder for other views. It provides a side navigation menu and a user menu. */
// vaadin applayout is used to create a layout with a side navigation menu it
// consists of
// a header, a side navigation, and a user menu the side navigation is
// dynamically
// populated with menu entries from `MenuConfiguration`. Each entry is
// represented as a
// `SideNavItem` with With Flow, the root layout can be defined using the
// @Layout
// annotation, which tells the router to render all routes or views inside of
// it. use
// these functions to add content to 3 sections:addToNavBar addToDrawer
// addToHeader added
// afterNavigationObserver to the layout to handle navigation events
@Layout
@PermitAll // When security is enabled, allow all authenticated users
public final class MainLayout extends AppLayout implements AfterNavigationObserver {

	private static final long serialVersionUID = 1L;

	
	private static Div createAppMarker() {
		final var slidingHeader = new Div();
		slidingHeader.addClassNames(Display.FLEX, AlignItems.CENTER, Margin.Horizontal.MEDIUM, Gap.SMALL);
		slidingHeader.getStyle().set("flex-wrap", "nowrap"); // Ensure single line
		// Original header content (logo and app name) - version removed
		final Icon icon = CColorUtils.setIconClassSize(VaadinIcon.CALENDAR_BRIEFCASE.create(), IconSize.LARGE);
		icon.getStyle().set("color", "var(--lumo-primary-color)");
		icon.getStyle().set("min-width", "var(--lumo-icon-size-l)");
		icon.getStyle().set("min-height", "var(--lumo-icon-size-l)");
		slidingHeader.add(icon);
		final var appName = new Span("Derbent");
		appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);
		appName.getStyle().set("white-space", "nowrap"); // Prevent text wrapping
		slidingHeader.add(appName);
		return slidingHeader;
	}

	/** Sets up avatar with user initials when no profile picture is available.
	 * @param avatar The avatar component to configure
	 * @param user   The user whose initials to display */
	private static void setupAvatarInitials(final Avatar avatar, final CUser user) {
		if (user == null) {
			return;
		}
		String initials = "";
		// Get initials from first name
		if (user.getName() != null && !user.getName().trim().isEmpty()) {
			final String[] nameParts = user.getName().trim().split("\\s+");
			for (final String part : nameParts) {
				if (!part.isEmpty()) {
					initials += part.substring(0, 1).toUpperCase();
					if (initials.length() >= 2) {
						break; // Limit to 2 initials
					}
				}
			}
		}
		// Add last name initial if we have less than 2 initials
		if (user.getLastname() != null && !user.getLastname().trim().isEmpty() && initials.length() < 2) {
			initials += user.getLastname().substring(0, 1).toUpperCase();
		}
		// Fall back to username if no name is available
		if (initials.isEmpty() && user.getLogin() != null && !user.getLogin().trim().isEmpty()) {
			initials = user.getLogin().substring(0, 1).toUpperCase();
		}
		// Final fallback
		if (initials.isEmpty()) {
			initials = "U";
		}
		avatar.setAbbreviation(initials);
		// Set tooltip with full name
		String displayName = "";
		if (user.getName() != null && !user.getName().trim().isEmpty()) {
			displayName = user.getName();
			if (user.getLastname() != null && !user.getLastname().trim().isEmpty()) {
				displayName += " " + user.getLastname();
			}
		} else {
			displayName = user.getLogin();
		}
		avatar.getElement().setAttribute("title", displayName);
	}

	private final AuthenticationContext authenticationContext;
	private final User currentUser;
	private final CLayoutService layoutService;
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private CViewToolbar mainToolbar;
	private final CPageMenuIntegrationService pageMenuService;
	private final CPageTestAuxillaryService pageTestAuxillaryService;
	private final PasswordEncoder passwordEncoder;
	private final CRouteDiscoveryService routeDiscoveryService;
	private final ISessionService sessionService;
	private final CSystemSettingsService systemSettingsService;
	private final CUserService userService;

	MainLayout(final AuthenticationContext authenticationContext, final ISessionService sessionService, final CLayoutService layoutService,
			final PasswordEncoder passwordEncoder, final CUserService userService, final CSystemSettingsService systemSettingsService,
			final CRouteDiscoveryService routeDiscoveryService, final CPageMenuIntegrationService pageMenuService,
			CPageTestAuxillaryService pageTestAuxillaryService) throws Exception {
		this.authenticationContext = authenticationContext;
		this.sessionService = sessionService;
		this.layoutService = layoutService;
		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
		this.systemSettingsService = systemSettingsService;
		this.routeDiscoveryService = routeDiscoveryService;
		this.pageMenuService = pageMenuService;
		this.pageTestAuxillaryService = pageTestAuxillaryService;
		currentUser = authenticationContext.getAuthenticatedUser(User.class).orElse(null);
		setSessionUserFromContext();
		setId("main-layout");
		setPrimarySection(Section.DRAWER);
		// Apply font size scale from system settings
		applyFontSizeFromSettings();
		// this is the main layout, so we add the side navigation menu and the user menu
		// to the drawer and the toolbar to the navbar
		addToDrawer(createHeader());
		// ok, lets put it in a scroller, so it can scroll if it is too long????
		addToDrawer(new Scroller(createSlidingHeader()));
		// must be after menu creation
		addToNavbar(true, createNavBar()); // Add the toggle button to the navbar
		// why this is in a scroller? Add the side navigation menu to the drawer,
		// wrapped
		// in a Scroller for better scrolling behavior addToDrawer(new
		// Scroller(createSideNav()));
		addToDrawer(createUserMenu()); // Add the user menu to the navbar
	}

	@Override
	public void afterNavigation(final AfterNavigationEvent event) {
		// Update the view title in the toolbar after navigation
		String pageTitle = null;
		// Check if the current content implements IPageTitleProvider (for dynamic
		// pages)
		final Component content = getContent();
		if (content instanceof IPageTitleProvider) {
			pageTitle = ((IPageTitleProvider) content).getPageTitle();
			// LOGGER.debug("Using page title from IPageTitleProvider: {}", pageTitle);
		} else {
			// Fall back to MenuConfiguration if no custom title is provided
			pageTitle = MenuConfiguration.getPageHeader(content).orElse("Main Layout");
			// LOGGER.debug("Using page title from MenuConfiguration: {}", pageTitle);
		}
		mainToolbar.setPageTitle(pageTitle); // Set the page title in the toolbar
	}

	/** Applies the font size scale from system settings. */
	private void applyFontSizeFromSettings() {
		try {
			// Get font size scale from system settings
			final String fontSizeScale = systemSettingsService.getFontSizeScale();
			LOGGER.info("Applying font size scale from settings: {}", fontSizeScale);
			// Apply font size scale to UI
			CFontSizeService.applyFontSizeScale(fontSizeScale);
			// Store in session for persistence
			CFontSizeService.storeFontSizeScale(fontSizeScale);
		} catch (final Exception e) {
			LOGGER.error("Error applying font size from settings, using default", e);
			// Fall back to medium if error occurs
			CFontSizeService.applyFontSizeScale("medium");
		}
	}

	
	private Div createHeader() {
		// Application logo and branding
		final Icon icon = CColorUtils.setIconClassSize(VaadinIcon.CALENDAR_BRIEFCASE.create(), IconSize.LARGE);
		icon.getStyle().set("color", "var(--lumo-primary-color)");
		icon.getStyle().set("min-width", "var(--lumo-icon-size-l)");
		icon.getStyle().set("min-height", "var(--lumo-icon-size-l)");
		final var appName = new Span("Derbent");
		appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);
		final var header = new Div(icon, appName);
		header.addClassNames(Display.FLEX, Padding.MEDIUM, Gap.MEDIUM, AlignItems.CENTER);
		// Make the header clickable to navigate to dashboard
		header.getStyle().set("cursor", "pointer");
		header.addClickListener(event -> {
			LOGGER.debug("Header clicked - navigating to home");
			UI.getCurrent().navigate("home");
		});
		// Add hover effects
		header.getElement().addEventListener("mouseenter", event -> header.getStyle().set("background-color", "var(--lumo-contrast-5pct)"));
		header.getElement().addEventListener("mouseleave", event -> header.getStyle().remove("background-color"));
		return header;
	}

	private Div createNavBar() throws Exception {
		final Div navBar = new Div();
		// dont add any other compoents to the navbar, just the toolbar otherwise call
		// it
		// with ,xyz,xyz etc..
		mainToolbar = new CViewToolbar("Main Layout", sessionService, layoutService, authenticationContext, systemSettingsService,
				routeDiscoveryService, pageMenuService);
		navBar.add(mainToolbar);
		return navBar;
	}

	private Div createSlidingHeader() throws Exception {
		// Add hierarchical side menu below the header content
		final var hierarchicalMenu = new CHierarchicalSideMenu(pageMenuService, pageTestAuxillaryService);
		hierarchicalMenu.addClassNames(Margin.Top.MEDIUM);
		// Create container for the complete sliding header with menu
		final var completeHeader = new Div();
		/// final var slidingHeader = createAppMarker(); dont add header: slidingHeader
		completeHeader.add(hierarchicalMenu);
		LOGGER.info("Sliding header with hierarchical menu created successfully");
		return completeHeader;
	}

	
	private Component createUserMenu() {
		final var user = currentUser;
		Check.notNull(user, "Current user cannot be null when creating user menu");
		final var avatar = new Avatar();
		avatar.addThemeVariants(AvatarVariant.LUMO_SMALL); // Changed from XSMALL to SMALL
															// for better visibility
		avatar.addClassNames(Margin.Right.SMALL);
		avatar.setColorIndex(5);
		// Set user name for avatar
		avatar.setName(user.getUsername());
		avatar.setAbbreviation(user.getUsername().length() > 0 ? user.getUsername().substring(0, 1).toUpperCase() : "U");
		// Try to get current user's profile picture
		try {
			final var currentUserOptional = sessionService.getActiveUser();
			if (currentUserOptional.isPresent()) {
				final CUser currentCUser = currentUserOptional.get();
				setAvatarImage(avatar, currentCUser);
			} else {
				LOGGER.debug("No active user found, using default avatar");
			}
		} catch (final Exception e) {
			LOGGER.error("Error loading user profile picture, using default: {}", e.getMessage());
			throw e;
		}
		final var userMenu = new MenuBar();
		userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
		userMenu.addClassNames(Margin.MEDIUM);
		final var userMenuItem = userMenu.addItem(avatar);
		final String login = user.getUsername().split("@")[0];
		// final String companyName = sessionService.getActiveCompany().map(c -> c.getName()).orElse("Unknown Company");
		userMenuItem.add(login);
		userMenuItem.setId("user-menu-item");
		userMenuItem.getSubMenu().addItem("Edit Profile", event -> openUserProfileDialog());
		// Additional user menu items could be added here (preferences, settings, etc.)
		final MenuItem menuItem = userMenuItem.getSubMenu().addItem("Logout", event -> {
			sessionService.clearSession(); // Clear session on logout
			authenticationContext.logout();
		});
		menuItem.setId("logout-menu-item");
		return userMenu;
	}

	/** Opens the user profile dialog for the current user. */
	private void openUserProfileDialog() {
		LOGGER.info("Opening user profile dialog for user: {}", currentUser != null ? currentUser.getUsername() : "null");
		try {
			// Get current user from session service
			final var currentUserOptional = sessionService.getActiveUser();
			if (currentUserOptional.isEmpty()) {
				LOGGER.warn("No active user found in session");
				CNotificationService.showWarning("Unable to load user profile. Please try logging in again.");
				return;
			}
			final CUser currentCUser = currentUserOptional.get();
			// Create and open profile dialog
			final CDialogUserProfile profileDialog = new CDialogUserProfile(currentCUser, this::saveUserProfile, passwordEncoder);
			profileDialog.open();
			LOGGER.debug("User profile dialog opened successfully");
		} catch (final Exception e) {
			LOGGER.error("Error opening user profile dialog", e);
			CNotificationService.showWarning("Failed to open profile dialog: " + e.getMessage());
		}
	}

	/** Saves the user profile after editing.
	 * @param user The updated user object */
	private void saveUserProfile(final CUser user) {
		LOGGER.info("Saving user profile for user: {}", user != null ? user.getLogin() : "null");
		try {
			if (user == null) {
				throw new IllegalArgumentException("User cannot be null");
			}
			// Save user using user service
			final CUser savedUser = userService.save(user);
			// Update session with saved user
			// sessionService.reloadUser(savedUser);
			LOGGER.info("User profile saved successfully for user: {}", savedUser.getLogin());
		} catch (final Exception e) {
			LOGGER.error("Error saving user profile", e);
			throw e;
		}
	}

	/** Sets the avatar image based on the user's profile picture data. This method properly creates a StreamResource for the Avatar component.
	 * @param avatar The avatar component to update
	 * @param user   The user whose profile picture should be displayed */
	@SuppressWarnings ({
			"removal"
	})
	private void setAvatarImage(final Avatar avatar, final CUser user) {
		if (user == null) {
			return; // Avatar will use default behavior
		}
		final byte[] profilePictureData = user.getProfilePictureData();
		if (profilePictureData != null && profilePictureData.length > 0) {
			try {
				// Create a StreamResource from the profile picture data
				final StreamResource imageResource =
						new StreamResource("profile-" + user.getId() + ".jpg", () -> new ByteArrayInputStream(profilePictureData));
				imageResource.setContentType("image/jpeg");
				// Set the image resource to the avatar
				avatar.setImageResource(imageResource);
				// LOGGER.debug("Set avatar image from user profile picture data for user: {}",
				// user.getLogin());
				return;
			} catch (final Exception e) {
				LOGGER.warn("Error creating StreamResource from profile picture: {}", e.getMessage());
			}
		}
		// Fall back to user initials if no profile picture is available
		setupAvatarInitials(avatar, user);
	}

	private void setSessionUserFromContext() {
		// LOGGER.info("Setting session user from authentication context");
		Check.notNull(currentUser, "No authenticated user found in security context");
		Check.notNull(currentUser.getUsername(), "Authenticated user must have a username");
		Check.notNull(sessionService, "Session service cannot be null");
		final String loginname = currentUser.getUsername();
		Check.notNull(loginname, "Authenticated user login name cannot be null");
		Check.isTrue(loginname.contains("@"), "Login name must contain '@' with format 'login@companyID'");
		// split loginname at @
		final String login = loginname.split("@")[0];
		final String companyIDStr = loginname.split("@")[1];
		final Long companyID = Long.parseLong(companyIDStr);
		final CUser user = userService.findByLogin(login, companyID);
		Check.notNull(user, "No user found for login: " + login + " and company ID: " + companyID);
		sessionService.setActiveCompany(user.getCompany());
		sessionService.setActiveUser(user);
	}
}
