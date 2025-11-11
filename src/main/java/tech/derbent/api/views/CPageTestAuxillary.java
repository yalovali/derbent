package tech.derbent.api.views;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.IconSize;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.services.CPageTestAuxillaryService;
import tech.derbent.api.services.CPageTestAuxillaryService.RouteEntry;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.views.components.CButton;
import tech.derbent.api.views.components.CDiv;
import tech.derbent.api.views.components.CFlexLayout;

@Route ("cpagetestauxillary")
@PageTitle ("Test Support Page")
@Menu (order = 3.1001, icon = "class:tech.derbent.app.gannt.view.CProjectGanntView", title = "Setup.Test Support Page")
@PermitAll
public class CPageTestAuxillary extends Main {
	public static final String DEFAULT_COLOR = "#31701F";
	public static final String DEFAULT_ICON = "vaadin:progressbar";
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Test View";
	// Header is a persistent component for this view; create once and add in constructor
	private final CDiv header = new CDiv();
	Logger LOGGER = LoggerFactory.getLogger(CPageTestAuxillary.class);
	// private final String ENTITY_ID_FIELD = "ganntview_id";
	private final CFlexLayout pageLinksLayout = new CFlexLayout();
	private final CPageTestAuxillaryService pageTestAuxillaryService;

	public CPageTestAuxillary(final CPageTestAuxillaryService pageTestAuxillaryService) throws Exception {
		super();
		this.pageTestAuxillaryService = pageTestAuxillaryService;
		LOGGER.debug("Initializing CPageTestAuxillary");
		// prepare static parts
		header.setText("Auxillary Test Page Links:");
		add(header);
		add(pageLinksLayout);
		// prepare dynamic route buttons
		prepareRoutes();
	}

	protected void prepareRoutes() {
		LOGGER.debug("Preparing routes for CPageTestAuxillary");
		pageLinksLayout.removeAll();
		pageLinksLayout.setWidthFull();
		pageLinksLayout.setFlexWrap(FlexWrap.WRAP); // allow wrapping to next row
		pageLinksLayout.setJustifyContentMode(JustifyContentMode.CENTER); // center items horizontally
		pageLinksLayout.setAlignItems(Alignment.CENTER); // vertically align within row
		pageLinksLayout.getStyle().set("gap", "10px"); // space between buttons
		pageLinksLayout.getStyle().set("padding", "10px");
		// border for layout
		pageLinksLayout.getStyle().set("border", "1px solid #ccc");
		final List<RouteEntry> routes = new ArrayList<>(pageTestAuxillaryService.getRoutes());
		routes.sort(Comparator.comparing(r -> r.title == null ? "" : r.title.toLowerCase(), Comparator.naturalOrder()));
		routes.forEach(routeEntry -> {
			final Icon icon = CColorUtils.setIconClassSize(CColorUtils.createStyledIcon(routeEntry.iconName, routeEntry.iconColor), IconSize.MEDIUM);
			final CButton routeButton = new CButton(routeEntry.title, icon, e -> {
				getUI().ifPresent(ui -> ui.navigate(routeEntry.route));
			});
			pageLinksLayout.add(routeButton);
		});
	}
}
