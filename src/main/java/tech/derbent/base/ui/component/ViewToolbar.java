package tech.derbent.base.ui.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Flex;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;

public final class ViewToolbar extends Composite<Header> {

	private static final long serialVersionUID = 1L;

	public static Component group(final Component... components) {
		final var group = new Div(components);
		group.addClassNames(Display.FLEX, FlexDirection.COLUMN, AlignItems.STRETCH, Gap.SMALL, FlexDirection.Breakpoint.Medium.ROW, AlignItems.Breakpoint.Medium.CENTER);
		return group;
	}

	public ViewToolbar(final String viewTitle, final Component... components) {
		addClassNames(Display.FLEX, FlexDirection.COLUMN, JustifyContent.BETWEEN, AlignItems.STRETCH, Gap.MEDIUM, FlexDirection.Breakpoint.Medium.ROW, AlignItems.Breakpoint.Medium.CENTER);
		final var drawerToggle = new DrawerToggle();
		drawerToggle.addClassNames(Margin.NONE);
		final var title = new H1(viewTitle);
		title.addClassNames(FontSize.XLARGE, Margin.NONE, FontWeight.LIGHT);
		final var toggleAndTitle = new Div(drawerToggle, title);
		toggleAndTitle.addClassNames(Display.FLEX, AlignItems.CENTER);
		getContent().add(toggleAndTitle);
		if (components.length > 0) {
			final var actions = new Div(components);
			actions.addClassNames(Display.FLEX, FlexDirection.COLUMN, JustifyContent.BETWEEN, Flex.GROW, Gap.SMALL, FlexDirection.Breakpoint.Medium.ROW);
			getContent().add(actions);
		}
	}
}
