package tech.derbent.plm.ui.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.view.CAbstractPage;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.service.CProjectService;
import tech.derbent.api.ui.component.enhanced.CDashboardStatCard;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.project.domain.CProject_Derbent;

/** CDashboardView - System summary dashboard that serves as the default landing page. Layer: View (MVC) Displays key system metrics including total
 * projects, users per project, and activities per project. This view responds to the default route ("") and provides an overview of the application
 * state. */
@Route (value = "home", registerAtStartup = false)
@PageTitle ("Dashboard")
@PermitAll // When security is enabled, allow all authenticated users
public final class CDashboardView extends CAbstractPage {

	public static final String DEFAULT_COLOR = "#6B5FA7"; // CDE Purple - main dashboard
	public static final String DEFAULT_ICON = "vaadin:dashboard";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDashboardView.class);
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Dashboard View";

	/** Creates a detail row for a single project showing its metrics.
	 * @param projectName   the name of the project
	 * @param userCount     the number of users in the project
	 * @param activityCount the number of activities in the project
	 * @return the horizontal layout for the project row */
	private static HorizontalLayout createProjectDetailRow(final String projectName, final long userCount, final long activityCount) {
		final CDashboardStatCard projectCard = new CDashboardStatCard(projectName, "Project", VaadinIcon.FOLDER_O.create());
		final CDashboardStatCard usersCard = new CDashboardStatCard("Users", userCount, VaadinIcon.USER.create());
		final CDashboardStatCard activitiesCard = new CDashboardStatCard("Activities", activityCount, VaadinIcon.CLIPBOARD_CHECK.create());
		final HorizontalLayout projectRow = new HorizontalLayout();
		projectRow.addClassNames(Gap.MEDIUM, Margin.Bottom.SMALL);
		projectRow.setWidthFull();
		projectRow.setSpacing(true);
		projectRow.add(projectCard, usersCard, activitiesCard);
		projectRow.setFlexGrow(2, projectCard); // Give project name more space
		projectRow.setFlexGrow(1, usersCard);
		projectRow.setFlexGrow(1, activitiesCard);
		return projectRow;
	}

	private final CActivityService activityService;
	private VerticalLayout projectDetailsLayout;
	private final CProjectService<CProject_Derbent> projectService;
	private CDashboardStatCard totalActivitiesCard;
	private CDashboardStatCard totalProjectsCard;
	private CDashboardStatCard totalUsersCard;
	private final CUserService userService;

	/** Constructor for CDashboardView.
	 * @param projectService  the project service for fetching project data
	 * @param userService     the user service for fetching user counts
	 * @param activityService the activity service for fetching activity counts */
	public CDashboardView(final CProjectService<CProject_Derbent> projectService, final CUserService userService,
			final CActivityService activityService) {
		super();
		// LOGGER.info("Creating CDashboardView");
		this.projectService = projectService;
		this.userService = userService;
		this.activityService = activityService;
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		// LOGGER.debug("BeforeEnter event for CDashboardView");
		// No specific navigation logic needed for the dashboard
	}

	/** Creates the project details section showing per-project statistics. */
	private void createProjectDetailsSection() {
		// LOGGER.debug("Creating project details section");
		projectDetailsLayout = new VerticalLayout();
		projectDetailsLayout.addClassNames(Padding.NONE, Gap.MEDIUM);
		projectDetailsLayout.setWidthFull();
		final H1 sectionTitle = new H1("Project Details");
		sectionTitle.addClassNames(Margin.Bottom.MEDIUM);
		projectDetailsLayout.add(sectionTitle);
	}

	/** Creates the summary statistics cards. */
	private void createSummaryCards() {
		// LOGGER.debug("Creating summary statistics cards");
		totalProjectsCard = new CDashboardStatCard("Total Projects", "0", VaadinIcon.BRIEFCASE.create());
		totalUsersCard = new CDashboardStatCard("Total Users", "0", VaadinIcon.USERS.create());
		totalActivitiesCard = new CDashboardStatCard("Total Activities", "0", VaadinIcon.TASKS.create());
	}

	/** Creates the layout for summary cards with responsive design.
	 * @return the horizontal layout containing the cards */
	private HorizontalLayout createSummaryCardsLayout() {
		final HorizontalLayout cardsLayout = new HorizontalLayout();
		cardsLayout.addClassNames(Gap.MEDIUM, Margin.Bottom.LARGE);
		cardsLayout.setWidthFull();
		cardsLayout.setSpacing(true);
		// Add cards with equal distribution
		cardsLayout.add(totalProjectsCard, totalUsersCard, totalActivitiesCard);
		cardsLayout.setFlexGrow(1, totalProjectsCard);
		cardsLayout.setFlexGrow(1, totalUsersCard);
		cardsLayout.setFlexGrow(1, totalActivitiesCard);
		return cardsLayout;
	}

	@Override
	public String getPageTitle() { return "Dashboard"; }

	@Override
	protected void initPage() {
		LOGGER.info("Initializing dashboard page");
		// Page title
		final H1 pageTitle = new H1("System Dashboard");
		pageTitle.addClassNames(Margin.Bottom.LARGE);
		// Create summary statistics cards
		createSummaryCards();
		// Create project details section
		createProjectDetailsSection();
		// Layout the page
		add(pageTitle);
		add(createSummaryCardsLayout());
		add(projectDetailsLayout);
		// Data will be loaded in postConstruct after dependency injection is complete
	}

	/** Initialize dashboard data after all dependencies are injected. This method is called after the constructor and dependency injection are
	 * complete. */
	@PostConstruct
	private void postConstruct() {
		// LOGGER.info("PostConstruct - loading dashboard data");
		// Load data after all dependencies are properly injected
		refreshDashboardData();
	}

	/** Refreshes all dashboard data by fetching current metrics from the database. */
	private void refreshDashboardData() {
		LOGGER.info("Refreshing dashboard data");
		try {
			// Fetch total project count
			final long totalProjects = projectService.getTotalProjectCount();
			totalProjectsCard.updateValue(totalProjects);
			// Fetch all projects to calculate detailed metrics
			final List<CProject_Derbent> allProjects = projectService.findAll();
			// Calculate total users and activities across all projects
			long totalUsers = 0;
			long totalActivities = 0;
			final Map<String, Map<String, Long>> projectMetrics = new HashMap<>();
			for (final CProject<?> project : allProjects) {
				final long usersInProject = userService.countUsersByProjectId(project.getId());
				final long activitiesInProject = activityService.countByProject(project);
				totalUsers += usersInProject;
				totalActivities += activitiesInProject;
				// Store project metrics for detailed view
				final Map<String, Long> metrics = new HashMap<>();
				metrics.put("users", usersInProject);
				metrics.put("activities", activitiesInProject);
				projectMetrics.put(project.getName(), metrics);
			}
			// Update summary cards
			totalUsersCard.updateValue(totalUsers);
			totalActivitiesCard.updateValue(totalActivities);
			// Update project details
			updateProjectDetails(projectMetrics);
			// LOGGER.info("Dashboard data refreshed successfully - Projects: {}, Users: {}, Activities: {}", totalProjects,
			// totalUsers,totalActivities);
		} catch (final Exception e) {
			LOGGER.error("Error refreshing dashboard data", e);
			// In case of error, show zero values rather than crashing
			totalProjectsCard.updateValue(0);
			totalUsersCard.updateValue(0);
			totalActivitiesCard.updateValue(0);
		}
	}

	@Override
	protected void setupToolbar() {
		// Dashboard doesn't need a custom toolbar - using default from MainLayout
	}

	/** Updates the project details section with per-project metrics.
	 * @param projectMetrics map of project names to their metrics */
	private void updateProjectDetails(final Map<String, Map<String, Long>> projectMetrics) {
		// LOGGER.debug("Updating project details for {} projects", projectMetrics.size());
		// Clear existing project details (keep the title)
		if (projectDetailsLayout.getComponentCount() > 1) {
			projectDetailsLayout.removeAll();
			final H1 sectionTitle = new H1("Project Details");
			sectionTitle.addClassNames(Margin.Bottom.MEDIUM);
			projectDetailsLayout.add(sectionTitle);
		}
		if (projectMetrics.isEmpty()) {
			final Div noProjectsMessage = new Div("No projects found in the system.");
			noProjectsMessage.addClassNames(Padding.MEDIUM);
			projectDetailsLayout.add(noProjectsMessage);
			return;
		}
		// Create cards for each project
		for (final Map.Entry<String, Map<String, Long>> entry : projectMetrics.entrySet()) {
			final String projectName = entry.getKey();
			final Map<String, Long> metrics = entry.getValue();
			final long users = metrics.get("users");
			final long activities = metrics.get("activities");
			final HorizontalLayout projectRow = createProjectDetailRow(projectName, users, activities);
			projectDetailsLayout.add(projectRow);
		}
	}
}
