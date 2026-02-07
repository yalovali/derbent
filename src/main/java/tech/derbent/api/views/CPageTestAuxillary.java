package tech.derbent.api.views;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.IconSize;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.menu.MyMenu;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CFlexLayout;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.views.CPageTestAuxillaryService.RouteEntry;

@Route ("cpagetestauxillary")
@PageTitle ("Test Support Page")
@MyMenu (order = "999.1001", icon = "vaadin:progressbar", title = "Development.Test Support Page")
@PermitAll
public class CPageTestAuxillary extends Main {

	public static final String DEFAULT_COLOR = "#6B5FA7"; // CDE Purple - test pages
	public static final String DEFAULT_ICON = "vaadin:progressbar";
	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
	public static final String VIEW_NAME = "Test View";

	/** Generate a stable, unique button ID from title and index for Playwright testing.
	 * @param title       Button title
	 * @param buttonIndex Button index
	 * @return Sanitized button ID */
	private static String generateButtonId(final String title, final int buttonIndex) {
		// Sanitize title to create a valid DOM ID
		final String sanitized = title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
		return "test-aux-btn-" + sanitized + "-" + buttonIndex;
	}

	private ScheduledExecutorService clockExecutor;
	// Push demonstration - live clock
	private final Span clockLabel = new Span();
	// Header is a persistent component for this view; create once and add in constructor
	private final CDiv header = new CDiv();
	Logger LOGGER = LoggerFactory.getLogger(CPageTestAuxillary.class);
	private final CFlexLayout pageLinksLayout = new CFlexLayout();
	private final CPageTestAuxillaryService pageTestAuxillaryService;

	public CPageTestAuxillary(final CPageTestAuxillaryService pageTestAuxillaryService) throws Exception {
		this.pageTestAuxillaryService = pageTestAuxillaryService;
		LOGGER.debug("Initializing CPageTestAuxillary");
		// Push demonstration header
		final CDiv pushDemoHeader = new CDiv();
		pushDemoHeader.setText("🔴 PUSH DEMONSTRATION - Live Server Clock:");
		pushDemoHeader.getStyle().set("font-weight", "bold").set("font-size", "1.2rem").set("color", "#E53935").set("padding", "20px")
				.set("background-color", "#FFEBEE").set("border-radius", "8px").set("margin", "10px").set("text-align", "center");
		// Clock display
		clockLabel.setId("push-demo-clock");
		clockLabel.getStyle().set("font-size", "2rem").set("font-weight", "bold").set("color", "#1976D2").set("padding", "15px")
				.set("background-color", "#E3F2FD").set("border-radius", "8px").set("margin", "10px").set("text-align", "center")
				.set("display", "block");
		clockLabel.setText("Initializing clock...");
		// Info text
		final CDiv infoText = new CDiv();
		infoText.setText("If this clock updates every second WITHOUT you touching the page, Push is working! ✅");
		infoText.getStyle().set("padding", "10px").set("margin", "10px").set("text-align", "center").set("color", "#666");
		add(pushDemoHeader);
		add(clockLabel);
		add(infoText);
		// Original header
		header.setText("Auxillary Test Page Links:");
		add(header);
		add(pageLinksLayout);
		// prepare dynamic route buttons
		prepareRoutes();
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		// Start clock when component is attached
		final UI ui = attachEvent.getUI();
		clockExecutor = Executors.newSingleThreadScheduledExecutor();
		clockExecutor.scheduleAtFixedRate(() -> {
			// Update clock every second
			final String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
			// CRITICAL: Use ui.access() for thread-safe UI update from background thread
			ui.access(() -> {
				clockLabel.setText("Server Time: " + currentTime);
				// LOGGER.debug("Clock updated via Push: {}", currentTime);
			});
		}, 0, 1, TimeUnit.SECONDS);
		LOGGER.info("✅ Push demo clock started - updates every second");
	}

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		// Stop clock when component is detached
		if (clockExecutor != null) {
			clockExecutor.shutdown();
			LOGGER.info("❌ Push demo clock stopped");
		}
	}

	protected void prepareRoutes() {
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
		// Add metadata div for Playwright to read button count and route information
		final CDiv metadataDiv = new CDiv();
		metadataDiv.setId("test-auxillary-metadata");
		metadataDiv.getStyle().set("display", "none"); // Hidden from UI
		metadataDiv.getElement().setAttribute("data-button-count", String.valueOf(pageTestAuxillaryService.getRoutes().size()));
		add(metadataDiv);
		// Add route links here as needed with clickable navigation links and add icon as button
		int buttonIndex = 0;
		for (final var routeEntry : routes) {
			final Icon icon = CColorUtils.setIconClassSize(CColorUtils.createStyledIcon(routeEntry.iconName, routeEntry.iconColor), IconSize.MEDIUM);
			LOGGER.debug("Adding test auxillary navigation button: {} -> {}", routeEntry.title, routeEntry.route);
			final CButton routeButton = new CButton(routeEntry.title, icon, event -> getUI().ifPresent(ui -> ui.navigate(routeEntry.route)));
			// Generate unique, stable ID for each button to enable Playwright testing
			final String buttonId = generateButtonId(routeEntry.title, buttonIndex);
			routeButton.setId(buttonId);
			// Add data attributes for Playwright metadata
			routeButton.getElement().setAttribute("data-route", routeEntry.route);
			routeButton.getElement().setAttribute("data-title", routeEntry.title);
			routeButton.getElement().setAttribute("data-button-index", String.valueOf(buttonIndex));
			pageLinksLayout.add(routeButton);
			buttonIndex++;
		}
		LOGGER.info("Created {} test auxillary navigation buttons", buttonIndex);
	}
}
