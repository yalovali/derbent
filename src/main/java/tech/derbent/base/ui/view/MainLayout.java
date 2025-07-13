package tech.derbent.base.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.IconSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

import jakarta.annotation.security.PermitAll;
import tech.derbent.security.CurrentUser;

@Layout
@PermitAll // When security is enabled, allow all authenticated users
public final class MainLayout extends AppLayout {

	private static final long serialVersionUID = 1L;
	private final CurrentUser currentUser;
	private final AuthenticationContext authenticationContext;

	MainLayout(final CurrentUser currentUser, final AuthenticationContext authenticationContext) {
		this.currentUser = currentUser;
		this.authenticationContext = authenticationContext;
		setPrimarySection(Section.DRAWER);
		addToDrawer(createHeader(), new Scroller(createSideNav()), createUserMenu());
	}

	private Div createHeader() {
		// TODO Replace with real application logo and name
		final var appLogo = VaadinIcon.CUBES.create();
		appLogo.addClassNames(TextColor.PRIMARY, IconSize.LARGE);
		final var appName = new Span("Derbent");
		appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);
		final var header = new Div(appLogo, appName);
		header.addClassNames(Display.FLEX, Padding.MEDIUM, Gap.MEDIUM, AlignItems.CENTER);
		return header;
	}

	/**
	 * Creates the side navigation menu. The navigation menu is dynamically
	 * populated with menu entries from `MenuConfiguration`. Each entry is
	 * represented as a `SideNavItem` with optional icons.
	 * @return A `SideNav` component containing the navigation items.
	 */
	private SideNav createSideNav() {
		final var nav = new SideNav(); // Create the side navigation
		nav.addClassNames(Margin.Horizontal.MEDIUM); // Style the navigation
		MenuConfiguration.getMenuEntries().forEach(entry -> createSideNavItem(nav, entry)); // Add menu entries
		return nav;
	}

	/**
	 * Creates a side navigation item for a given menu entry. Each menu entry is
	 * represented as a `SideNavItem` with optional icons.
	 * @param menuEntry The menu entry to create a navigation item for.
	 * @return A `SideNavItem` representing the menu entry.
	 */
	private void createSideNavItem(final SideNav nav, final MenuEntry menuEntry) {
		if (menuEntry == null) {
			return; // Return null if the menu entry is null
		}
		// read the menu entry properties
		String title = menuEntry.title();
		final String path = menuEntry.path();
		final String icon = menuEntry.icon();
		// if title contains a dot, it is a sub-menu entry
		if (title.contains(".")) {
			final var parts = title.split("\\.");
			title = parts[parts.length - 1]; // Use the last part as the title
			final String parent_title = parts[0]; // Use the first part as the parent title
			// find the parent menu entry
			SideNavItem parentItem = nav.getItems().stream().filter(item -> item.getLabel().equals(parent_title)).findFirst().orElse(null);
			if (parentItem == null) {
				parentItem = new SideNavItem(parent_title);
				parentItem.setPrefixComponent(new Icon(icon)); // Set the icon for the parent item
				nav.addItem(parentItem); // Add the parent item to the navigation
			}
			// Create a sub-menu item under the parent entry
			parentItem.addItem(new SideNavItem(title, path, new Icon(icon)));
		}
		else {
			// Create a top-level menu item
			nav.addItem(new SideNavItem(title, path, new Icon(icon))); // Create item with
		}
	}

	private Component createUserMenu() {
		final var user = currentUser.require();
		final var avatar = new Avatar(user.getFullName(), user.getPictureUrl());
		avatar.addThemeVariants(AvatarVariant.LUMO_XSMALL);
		avatar.addClassNames(Margin.Right.SMALL);
		avatar.setColorIndex(5);
		final var userMenu = new MenuBar();
		userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
		userMenu.addClassNames(Margin.MEDIUM);
		final var userMenuItem = userMenu.addItem(avatar);
		userMenuItem.add(user.getFullName());
		if (user.getProfileUrl() != null) {
			userMenuItem.getSubMenu().addItem("View Profile", event -> UI.getCurrent().getPage().open(user.getProfileUrl()));
		}
		// TODO Add additional items to the user menu if needed
		userMenuItem.getSubMenu().addItem("Logout", event -> authenticationContext.logout());
		return userMenu;
	}
}
