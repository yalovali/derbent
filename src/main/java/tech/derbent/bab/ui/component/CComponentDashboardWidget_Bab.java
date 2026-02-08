package tech.derbent.bab.ui.component;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CDashboardProject_BabService;
import tech.derbent.base.session.service.ISessionService;

/** CComponentDashboardWidget_Bab - BAB-specific dashboard widget component. Layer: UI (MVC) Following Derbent pattern: Concrete component extending
 * CComponentBase. Displays project name and active status for BAB dashboard projects. */
public class CComponentDashboardWidget_Bab extends CComponentDashboardWidget {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentDashboardWidget_Bab.class);
	private static final long serialVersionUID = 1L;
	private Span activeStatusLabel;
	private Span dashboardTypeLabel;
	private Span projectNameLabel;
	private Div statusIndicator;
	// BAB-specific components
	private H3 titleLabel;

	/** Constructor for BAB dashboard widget. */
	public CComponentDashboardWidget_Bab(final CDashboardProject_BabService service, final ISessionService sessionService) {
		super(service, sessionService);
		initializeComponents(); // Concrete components MUST call this
	}

	@Override
	protected String getWidgetTitle() { return "BAB Dashboard Widget"; }

	@Override
	protected void initializeComponents() {
		setId("custom-dashboard-widget-bab");
		addClassName("bab-dashboard-widget");
		// Create base layout
		setSpacing(false);
		setMargin(false);
		setPadding(false);
		setWidthFull();
		addClassName("dashboard-widget");
		// Title
		titleLabel = new H3(getWidgetTitle());
		titleLabel.addClassName("dashboard-widget-title");
		titleLabel.getStyle().set("margin", "0 0 12px 0");
		titleLabel.getStyle().set("color", "#1976D2");
		add(titleLabel);
		// Create BAB-specific components
		projectNameLabel = new Span();
		projectNameLabel.addClassName("dashboard-project-name");
		projectNameLabel.setId("custom-dashboard-project-name");
		projectNameLabel.getStyle().set("font-weight", "500");
		projectNameLabel.getStyle().set("margin-bottom", "8px");
		activeStatusLabel = new Span();
		activeStatusLabel.addClassName("dashboard-active-status");
		activeStatusLabel.setId("custom-dashboard-active-status");
		activeStatusLabel.getStyle().set("margin-bottom", "8px");
		dashboardTypeLabel = new Span();
		dashboardTypeLabel.addClassName("dashboard-type-label");
		dashboardTypeLabel.setId("custom-dashboard-type");
		dashboardTypeLabel.getStyle().set("font-size", "0.875rem");
		dashboardTypeLabel.getStyle().set("color", "#666");
		statusIndicator = new Div();
		statusIndicator.addClassName("dashboard-status-indicator");
		statusIndicator.setId("custom-dashboard-status-indicator");
		statusIndicator.getStyle().set("width", "12px");
		statusIndicator.getStyle().set("height", "12px");
		statusIndicator.getStyle().set("border-radius", "50%");
		statusIndicator.getStyle().set("margin-right", "8px");
		statusIndicator.getStyle().set("display", "inline-block");
		// Create status row with indicator
		final Div statusRow = new Div();
		statusRow.getStyle().set("display", "flex");
		statusRow.getStyle().set("align-items", "center");
		statusRow.add(statusIndicator, activeStatusLabel);
		// Add components to layout
		add(projectNameLabel, statusRow, dashboardTypeLabel);
		// Apply widget styling
		getStyle().set("border", "1px solid #E0E0E0");
		getStyle().set("border-radius", "8px");
		getStyle().set("padding", "16px");
		getStyle().set("background", "#FAFAFA");
		getStyle().set("min-height", "120px");
		// Load initial data
		refreshComponent();
	}

	@Override
	protected void loadData() {
		refreshComponent(); // For this widget, loading data is the same as refreshing the component
	}

	@Override
	protected void refreshComponent() {
		try {
			// Get active project from session
			final Optional<CProject<?>> activeProjectOpt = sessionService.getActiveProject();
			activeProjectOpt.ifPresentOrElse(project -> {
				// Update project name
				projectNameLabel.setText("Project: " + project.getName());
				// Check if this is a BAB project
				if (project instanceof tech.derbent.bab.project.domain.CProject_Bab) {
					// Project is active (BAB projects don't have an active flag, they're active by existence)
					activeStatusLabel.setText("Status: Active");
					statusIndicator.getStyle().set("background-color", "#4CAF50");
					// Show project type info
					dashboardTypeLabel.setText("BAB Gateway Project");
				} else {
					// Non-BAB project
					activeStatusLabel.setText("Status: Non-BAB Project");
					statusIndicator.getStyle().set("background-color", "#FF9800");
					dashboardTypeLabel.setText("Generic Project");
				}
			}, () -> {
				projectNameLabel.setText("Project: No active project");
				activeStatusLabel.setText("Status: No project selected");
				statusIndicator.getStyle().set("background-color", "#9E9E9E");
				dashboardTypeLabel.setText("Please select a project");
			});
			LOGGER.debug("Loaded dashboard widget data");
		} catch (final Exception e) {
			LOGGER.error("Error loading dashboard data: {}", e.getMessage(), e);
			setWidgetError("Failed to load project data");
		}
	}

	private void setWidgetError(final String errorMessage) {
		removeAll();
		final Span errorSpan = new Span("Error: " + errorMessage);
		errorSpan.addClassName("dashboard-widget-error");
		errorSpan.getStyle().set("color", "#D32F2F");
		errorSpan.getStyle().set("font-style", "italic");
		add(errorSpan);
	}
}
