package tech.derbent.api.views;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.widget.CEntityWidget;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.activities.view.CActivityWidget;
import tech.derbent.base.session.service.ISessionService;

/** CWidgetGridDemo - Demo page showcasing the widget-based grid display for entities.
 * <p>
 * This demo demonstrates the CEntityWidget system which displays entities in a rich, visual format within grids. The widget displays: - Entity name
 * and description - Status with color coding - Dates and timestamps - Assigned user - Progress indicators - Action buttons (edit, delete)
 * </p>
 * <p>
 * The demo uses CActivity entities as an example, with the CActivityWidget providing activity-specific rendering.
 * </p>
 *
 * @author Derbent Framework
 * @since 1.0
 * @see CEntityWidget
 * @see CActivityWidget
 * @see CGrid#addWidgetColumn */
@Route ("widget-grid-demo")
@PageTitle ("Widget Grid Demo")
@PermitAll
public class CWidgetGridDemo extends Main {

	public static final String DEFAULT_COLOR = "#1976D2";
	public static final String DEFAULT_ICON = "vaadin:grid-small";
	private static final Logger LOGGER = LoggerFactory.getLogger(CWidgetGridDemo.class);
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Widget Grid Demo";
	private final CActivityService activityService;
	private CGrid<CActivity> grid;
	private final ISessionService sessionService;

	public CWidgetGridDemo(final CActivityService activityService, final ISessionService sessionService) {
		super();
		this.activityService = activityService;
		this.sessionService = sessionService;
	}

	/** Creates a demo section with title, description, and the widget grid. */
	private CDiv createWidgetGridSection() {
		final CDiv section = new CDiv();
		section.getStyle().set("margin", "20px 0").set("padding", "20px").set("border", "2px solid #ddd").set("border-radius", "8px")
				.set("background-color", "#f9f9f9");
		// Section title
		final H2 sectionTitle = new H2("Activity Widget Grid");
		sectionTitle.getStyle().set("margin-top", "0");
		section.add(sectionTitle);
		// Description
		final Paragraph sectionDesc = new Paragraph("This grid displays activities as rich widgets. Each widget shows the activity name, "
				+ "description, status, priority, dates, and provides quick action buttons. " + "Click on a widget to select it.");
		section.add(sectionDesc);
		// Create the grid with widget column
		grid = new CGrid<>(CActivity.class);
		grid.setHeight("500px");
		grid.setWidthFull();
		// Add widget column using the CActivityWidget
		grid.addWidgetColumn(activity -> {
			final CActivityWidget widget = new CActivityWidget(activity);
			// Handle widget actions
			widget.addActionListener(event -> on_widget_actionTriggered(event));
			return widget;
		});
		// Add selection listener
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				LOGGER.debug("Activity selected: {}", event.getValue().getName());
			}
		});
		section.add(grid);
		// Instructions
		final Paragraph instructions = new Paragraph(
				"The widget displays all relevant activity information in a compact, visual format. " + "Use the action buttons to edit or delete activities. "
						+ "Selection is indicated by the colored left border and background highlight.");
		instructions.getStyle().set("font-style", "italic").set("color", "#666").set("margin-top", "10px");
		section.add(instructions);
		return section;
	}

	@Override
	public String toString() {
		return "CWidgetGridDemo{VIEW_NAME='" + VIEW_NAME + "'}";
	}

	/** Loads activity data into the grid. */
	private void loadActivityData() {
		try {
			// Check if there's an active project in the session
			if (sessionService.getActiveProject().isEmpty()) {
				LOGGER.warn("No active project in session - cannot load activities");
				CNotificationService.showWarning("Please select a project first to view activities.");
				return;
			}
			// Load activities from the service
			final List<CActivity> activities = activityService.listByProject(sessionService.getActiveProject().get());
			if (activities.isEmpty()) {
				LOGGER.info("No activities found for the current project");
				CNotificationService.showInfo("No activities found. Create some activities to see them in the widget grid.");
			} else {
				LOGGER.info("Loaded {} activities for widget grid demo", activities.size());
			}
			grid.setItems(activities);
		} catch (final Exception e) {
			LOGGER.error("Error loading activity data: {}", e.getMessage());
			CNotificationService.showException("Error loading activities", e);
		}
	}

	/** Handles widget action events (edit, delete, etc.). */
	private void on_widget_actionTriggered(final CEntityWidget.CEntityWidgetEvent<CActivity> event) {
		final CActivity activity = event.getEntity();
		switch (event.getActionType()) {
		case EDIT -> {
			LOGGER.info("Edit action triggered for activity: {}", activity.getName());
			CNotificationService.showInfo("Edit: " + activity.getName());
			// In a real implementation, this would open an edit dialog
		}
		case DELETE -> {
			LOGGER.info("Delete action triggered for activity: {}", activity.getName());
			try {
				CNotificationService.showConfirmationDialog("Delete activity '" + activity.getName() + "'?", () -> {
					try {
						activityService.delete(activity.getId());
						loadActivityData(); // Refresh the grid
						CNotificationService.showSuccess("Activity deleted successfully");
					} catch (final Exception e) {
						CNotificationService.showException("Error deleting activity", e);
					}
				});
			} catch (final Exception e) {
				CNotificationService.showException("Error showing confirmation dialog", e);
			}
		}
		case VIEW -> {
			LOGGER.info("View action triggered for activity: {}", activity.getName());
			CNotificationService.showInfo("View: " + activity.getName());
		}
		case CUSTOM -> {
			LOGGER.info("Custom action '{}' triggered for activity: {}", event.getCustomAction(), activity.getName());
		}
		}
	}

	/** Post-construct initialization. */
	@PostConstruct
	private void postConstruct() {
		LOGGER.debug("Initializing CWidgetGridDemo");
		try {
			final CVerticalLayout layout = new CVerticalLayout();
			layout.setPadding(true);
			layout.setSpacing(true);
			layout.setWidthFull();
			// Title
			final H2 title = new H2("Widget-Based Grid Demo");
			layout.add(title);
			// Description
			final Paragraph description = new Paragraph("This demo showcases the new widget-based grid display for entities. "
					+ "Instead of traditional table columns, entities are displayed as rich, visual widgets "
					+ "that show all relevant information at a glance with action buttons for quick operations.");
			layout.add(description);
			// Widget grid section
			layout.add(createWidgetGridSection());
			add(layout);
			// Load data
			loadActivityData();
			LOGGER.info("CWidgetGridDemo initialized successfully");
		} catch (final Exception e) {
			LOGGER.error("Error initializing CWidgetGridDemo", e);
			CNotificationService.showException("Error initializing demo page", e);
		}
	}
}
