package tech.derbent.base.demo.view;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.view.CAbstractPage;
import tech.derbent.api.grid.widget.CActivityDisplayWidget;
import tech.derbent.api.grid.widget.CWidgetGrid;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CPageableUtils;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;

/**
 * CWidgetGridDemoView - Demo page for the widget-based grid component.
 * Shows how to use CWidgetGrid with entity display widgets.
 *
 * This demo page demonstrates:
 * - CWidgetGrid with ID and widget columns
 * - CActivityDisplayWidget for rich activity display
 * - Same grid patterns as traditional CGrid (binding, selection, master-detail)
 */
@Route("cwidgetgriddemoview")
@PageTitle("Widget Grid Demo")
@Menu(order = 100.2, icon = "class:tech.derbent.base.demo.view.CWidgetGridDemoView", title = "Demo.Widget Grid")
@PermitAll
public class CWidgetGridDemoView extends CAbstractPage {

	public static final String DEFAULT_COLOR = "#3498db";
	public static final String DEFAULT_ICON = "vaadin:grid-small";
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Widget Grid Demo View";

	private final CActivityService activityService;
	private final ISessionService sessionService;

	private CWidgetGrid<CActivity> widgetGrid;
	private Div detailsPanel;
	private SplitLayout splitLayout;

	public CWidgetGridDemoView(final CActivityService activityService, final ISessionService sessionService) {
		this.activityService = activityService;
		this.sessionService = sessionService;
		LOGGER.info("CWidgetGridDemoView constructor called");
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		LOGGER.debug("beforeEnter called for CWidgetGridDemoView");
	}

	@Override
	public String getPageTitle() {
		return "Widget Grid Demo";
	}

	@Override
	protected void initPage() {
		LOGGER.debug("initPage called for CWidgetGridDemoView");
		try {
			createHeaderSection();
			createMainLayout();
			LOGGER.debug("Widget Grid Demo view initialized successfully");
		} catch (final Exception e) {
			LOGGER.error("Error initializing widget grid demo view", e);
			CNotificationService.showException("Error initializing view", e);
		}
	}

	/**
	 * Creates the header section with title and description.
	 */
	private void createHeaderSection() {
		LOGGER.debug("createHeaderSection called");
		final VerticalLayout header = new VerticalLayout();
		header.addClassName("header-section");
		header.setPadding(true);
		header.setSpacing(false);

		final H2 title = new H2("Widget Grid Demo");
		title.addClassName("view-title");

		final Paragraph description = new Paragraph(
				"This demo page showcases the CWidgetGrid component which displays entities " +
				"using rich display widgets. Each activity is shown with its name, description, " +
				"dates, responsible user, progress, and status badges. " +
				"The grid maintains the same binding, selection, and master-detail patterns as the standard grid.");
		description.addClassName("view-description");

		header.add(title, description);
		add(header);
	}

	/**
	 * Creates the main layout with the widget grid and details panel.
	 */
	private void createMainLayout() {
		LOGGER.debug("createMainLayout called");

		splitLayout = new SplitLayout();
		splitLayout.setSizeFull();
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		splitLayout.setSplitterPosition(60);

		// Create the widget grid
		createWidgetGrid();

		// Create the details panel
		createDetailsPanel();

		// Add to split layout
		final CVerticalLayout gridContainer = new CVerticalLayout(false, true, false);
		gridContainer.add(widgetGrid);
		splitLayout.addToPrimary(gridContainer);
		splitLayout.addToSecondary(detailsPanel);

		add(splitLayout);
	}

	/**
	 * Creates the widget grid with activity display widget.
	 */
	private void createWidgetGrid() {
		LOGGER.debug("createWidgetGrid called");

		widgetGrid = new CWidgetGrid<>(CActivity.class);
		widgetGrid.setDisplayWidget(new CActivityDisplayWidget());
		widgetGrid.addStandardColumns("Activity");

		// Add selection listener for master-detail
		widgetGrid.asSingleSelect().addValueChangeListener(event -> {
			final CActivity selectedActivity = event.getValue();
			updateDetailsPanel(selectedActivity);
		});
	}

	/**
	 * Creates the details panel for showing selected activity details.
	 */
	private void createDetailsPanel() {
		LOGGER.debug("createDetailsPanel called");

		detailsPanel = new Div();
		detailsPanel.addClassName("details-panel");
		detailsPanel.getStyle().set("padding", "16px");
		detailsPanel.getStyle().set("background-color", "#f9f9f9");
		detailsPanel.getStyle().set("border-radius", "8px");
		detailsPanel.getStyle().set("height", "100%");
		detailsPanel.getStyle().set("overflow", "auto");

		updateDetailsPanel(null);
	}

	/**
	 * Updates the details panel with the selected activity.
	 *
	 * @param activity the selected activity or null
	 */
	private void updateDetailsPanel(final CActivity activity) {
		detailsPanel.removeAll();

		if (activity == null) {
			final Paragraph noSelection = new Paragraph("Select an activity from the grid above to see details.");
			noSelection.getStyle().set("color", "#666");
			noSelection.getStyle().set("font-style", "italic");
			detailsPanel.add(noSelection);
			return;
		}

		final CVerticalLayout details = new CVerticalLayout(false, true, false);

		final H2 activityTitle = new H2(activity.getName() != null ? activity.getName() : "Unnamed Activity");
		details.add(activityTitle);

		if (activity.getDescription() != null && !activity.getDescription().isBlank()) {
			final Paragraph desc = new Paragraph("Description: " + activity.getDescription());
			details.add(desc);
		}

		if (activity.getStatus() != null) {
			final Paragraph status = new Paragraph("Status: " + activity.getStatus().getName());
			details.add(status);
		}

		if (activity.getAssignedTo() != null) {
			final Paragraph assignee = new Paragraph("Assigned To: " + activity.getAssignedTo().getName());
			details.add(assignee);
		}

		if (activity.getProgressPercentage() != null) {
			final Paragraph progress = new Paragraph("Progress: " + activity.getProgressPercentage() + "%");
			details.add(progress);
		}

		if (activity.getStartDate() != null) {
			final Paragraph startDate = new Paragraph("Start Date: " + activity.getStartDate());
			details.add(startDate);
		}

		if (activity.getDueDate() != null) {
			final Paragraph dueDate = new Paragraph("Due Date: " + activity.getDueDate());
			details.add(dueDate);
		}

		detailsPanel.add(details);
	}

	/**
	 * Loads activities into the grid.
	 */
	@PostConstruct
	private void loadData() {
		LOGGER.debug("loadData called");
		try {
			// Get active project
			final Optional<CProject> activeProject = sessionService.getActiveProject();
			if (activeProject.isEmpty()) {
				LOGGER.warn("No active project found, grid will be empty");
				widgetGrid.setItems(List.of());
				return;
			}

			// Create data provider for the grid
			final CallbackDataProvider<CActivity, Void> dataProvider = new CallbackDataProvider<>(
					query -> {
						final List<QuerySortOrder> sortOrders = Optional.ofNullable(query.getSortOrders())
								.orElse(java.util.Collections.emptyList());
						final Sort springSort = sortOrders.isEmpty() ? Sort.unsorted()
								: Sort.by(sortOrders.stream()
										.map(so -> new Sort.Order(
												so.getDirection() == com.vaadin.flow.data.provider.SortDirection.DESCENDING
														? Sort.Direction.DESC
														: Sort.Direction.ASC,
												so.getSorted()))
										.toList());

						final int limit = query.getLimit();
						final int offset = query.getOffset();
						final int page = (limit > 0) ? (offset / limit) : 0;
						final Pageable pageable = CPageableUtils.validateAndFix(PageRequest.of(page, Math.max(limit, 1), springSort));

						return activityService.listByProject(activeProject.get(), pageable).stream();
					},
					query -> {
						final long total = activityService.countByProject(activeProject.get());
						return (int) Math.min(total, Integer.MAX_VALUE);
					});

			widgetGrid.setDataProvider(dataProvider);
			LOGGER.info("Widget grid data loaded successfully");

		} catch (final Exception e) {
			LOGGER.error("Error loading data for widget grid", e);
			CNotificationService.showException("Error loading data", e);
		}
	}

	@Override
	protected void setupToolbar() {
		LOGGER.debug("setupToolbar called for CWidgetGridDemoView");
		// No specific toolbar needed for this demo view
	}
}
