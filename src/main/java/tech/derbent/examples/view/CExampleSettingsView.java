package tech.derbent.examples.view;

import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.IDisplayView;

/** Example view demonstrating responsive flex layout behavior. */
@Route ("examples/settings/advanced")
@PageTitle ("Advanced Settings Example")
@Menu (order = 998, icon = "class:tech.derbent.examples.view.CExampleSettingsView", title = "Examples.Settings.Advanced")
@PermitAll
public class CExampleSettingsView extends Div implements IDisplayView {
	private static final long serialVersionUID = 1L;

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return "var(--lumo-primary-color)"; // Primary color for examples
	}

	public static String getIconFilename() { return "vaadin:cogs"; }

	public CExampleSettingsView() {
		final H2 title = new H2("Flex Layout Example");
		final Paragraph description = new Paragraph("This example demonstrates responsive flex layout behavior. "
				+ "Components will stack vertically on narrow screens and horizontally (in rows) on wider screens.");
		// Create a flex layout
		final FlexLayout flexLayout = new FlexLayout();
		flexLayout.setWidthFull();
		// Configure the flex layout to wrap items
		flexLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
		flexLayout.setJustifyContentMode(JustifyContentMode.AROUND);
		flexLayout.setAlignItems(Alignment.STRETCH);
		// Create and add 6 card components
		for (int i = 1; i <= 6; i++) {
			final Card card = createCard("Component " + i, "This is the description for component " + i, VaadinIcon.values()[i * 10]); // Just picking
																																		// some icons
			// Set responsive flex item properties This makes each card take 100% width on
			// small screens and share space on larger screens (roughly 3 per row on wide
			// screens)
			flexLayout.add(card);
			card.setMinWidth("280px");
			card.setMaxWidth("400px");
			// Apply flex grow to allow cards to expand and fill available space
			flexLayout.setFlexGrow(1, card);
		}
		// Add all components to the view
		add(title, description, flexLayout);
		setPadding(true);
	}

	private Card createCard(final String title, final String content, final VaadinIcon iconType) {
		final Card card = new Card();
		// Create a header div with icon and title
		final Div header = new Div();
		header.getStyle().set("display", "flex").set("align-items", "center").set("gap", "8px").set("margin-bottom", "8px");
		final Icon icon = new Icon(iconType);
		icon.setSize("24px");
		final H2 cardTitle = new H2(title);
		cardTitle.getStyle().set("margin", "0");
		cardTitle.getStyle().set("font-size", "1.2em");
		header.add(icon, cardTitle);
		// Add content
		final Paragraph cardContent = new Paragraph(content);
		card.add(header, cardContent);
		card.getStyle().set("margin", "8px");
		return card;
	}

	private void setPadding(final boolean padding) {
		if (padding) {
			getStyle().set("padding", "2rem");
		} else {
			getStyle().remove("padding");
		}
	}
}
