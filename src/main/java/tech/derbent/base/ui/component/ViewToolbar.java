package tech.derbent.base.ui.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
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

/* ViewToolbar.java
 *
 * This class defines a toolbar for views in the application, providing a
 * consistent header with a title and optional action components.
 *
 * It extends Composite to allow for easy composition of the toolbar's content.
 */
public final class ViewToolbar extends Composite<Header> {

	private static final long serialVersionUID = 1L;

	/*
	 * just used to create a group of components with nice styling. Not related to a
	 * toolbar
	 */
	public static Component group(final Component... components) {
		final var group = new Div(components);
		group.addClassNames(Display.FLEX, FlexDirection.COLUMN, AlignItems.STRETCH, Gap.SMALL, FlexDirection.Breakpoint.Medium.ROW, AlignItems.Breakpoint.Medium.CENTER);
		return group;
	}

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private final H1 title;

	/**
	 * Constructs a ViewToolbar with a title and optional components.
	 * @param viewTitle  The title of the view to be displayed in the toolbar.
	 * @param components Optional components to be added to the toolbar.
	 */
	public ViewToolbar(final String viewTitle, final Component... components) {
		LOGGER.debug("Creating ViewToolbar for {}", viewTitle);
		addClassNames(Display.FLEX, FlexDirection.COLUMN, JustifyContent.BETWEEN, AlignItems.STRETCH, Gap.MEDIUM, FlexDirection.Breakpoint.Medium.ROW, AlignItems.Breakpoint.Medium.CENTER);
		// this is a button that toggles the drawer in the app layout
		final var drawerToggle = new DrawerToggle();
		drawerToggle.addClassNames(Margin.NONE);
		title = new H1(viewTitle);
		title.addClassNames(FontSize.XLARGE, Margin.NONE, FontWeight.LIGHT);
		// put them together
		final var toggleAndTitle = new Div(drawerToggle, title);
		toggleAndTitle.addClassNames(Display.FLEX, AlignItems.CENTER);
		// add them to the content of the header
		getContent().add(toggleAndTitle);
		// add more if passed as a parameter
		if (components.length > 0) {
			// If there are additional components, add them to the toolbar
			final var actions = new Div(components);
			actions.addClassNames(Display.FLEX, FlexDirection.COLUMN, JustifyContent.BETWEEN, Flex.GROW, Gap.SMALL, FlexDirection.Breakpoint.Medium.ROW);
			getContent().add(actions);
		}
		final MultiSelectComboBox<String> multiSelectComboBox = new MultiSelectComboBox<String>();
		multiSelectComboBox.setItems("Project A", "Project B", "Project C", "Project D", "Project E");
		getContent().add(new Div("Select projects:"));
		getContent().add(multiSelectComboBox);
	}

	public void setPageTitle(final String title) {
		this.title.setText(title);
	}
}
