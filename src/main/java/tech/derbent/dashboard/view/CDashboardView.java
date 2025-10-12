package tech.derbent.dashboard.view;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.api.ui.dialogs.CExceptionDialog;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CAbstractPage;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserService;

/** CDashboardView - Dashboard view showing system summary statistics. Layer: View (MVC) Displays total projects, users per project, and activities
 * per project. Provides a comprehensive overview of the system's current state. */
@Route ("cdashboardview")
@PageTitle ("Home")
@Menu (order = 0, icon = "class:tech.derbent.dashboard.view.CDashboardView", title = "Home")
@PermitAll
public final class CDashboardView extends CAbstractPage {

	public static final String DEFAULT_COLOR = "var(--lumo-primary-color)";
	public static final String DEFAULT_ICON = "vaadin:home";
	private static final long serialVersionUID = 1L;
	private final CActivityService activityService;
	private final CProjectService projectService;
	private final CUserService userService;

	/** Constructor for CDashboardView.
	 * @param projectService  Service for project-related operations, must not be null
	 * @param userService     Service for user-related operations, must not be null
	 * @param activityService Service for activity-related operations, must not be null
	 * @throws IllegalArgumentException if any service parameter is null */
	public CDashboardView(final CProjectService projectService, final CUserService userService, final CActivityService activityService) {
		LOGGER.info("CDashboardView constructor called with projectService={}, userService={}, activityService={}",
				projectService != null ? projectService.getClass().getSimpleName() : "null",
				userService != null ? userService.getClass().getSimpleName() : "null",
				activityService != null ? activityService.getClass().getSimpleName() : "null");
		Check.notNull(projectService, "ProjectService cannot be null");
		Check.notNull(userService, "UserService cannot be null");
		Check.notNull(activityService, "ActivityService cannot be null");
		this.projectService = projectService;
		this.userService = userService;
		this.activityService = activityService;
		LOGGER.info("CDashboardView constructor completed successfully");
	}

	/** Implements the BeforeEnterObserver interface to handle navigation events.
	 * @param event The navigation event */
	@Override
	public final void beforeEnter(final BeforeEnterEvent event) {
		Check.notNull(event, "BeforeEnterEvent cannot be null");
	}

	/** Calculates the number of activities per project.
	 * @param projects List of projects to calculate activities for, must not be null
	 * @return Map containing project names as keys and activity counts as values, never null */
	private final Map<String, Long> calculateActivitiesByProject(final List<CProject> projects) {
		Check.notNull(projects, "Projects list cannot be null");
		return projects.stream().filter(project -> (project != null) && (project.getName() != null))
				.collect(Collectors.toMap(CProject::getName, project -> {
					if (activityService == null) {
						LOGGER.error("ActivityService is null");
						throw new IllegalStateException("ActivityService is null");
					}
					final var activities = activityService.listByProject(project);
					final long activityCount = activities != null ? activities.size() : 0L;
					LOGGER.debug("Project {} has {} activities", project.getName(), activityCount);
					return activityCount;
				}));
	}

	/** Calculates the number of users per project.
	 * @param projects List of projects to calculate users for, must not be null
	 * @return Map containing project names as keys and user counts as values, never null */
	private final Map<String, Long> calculateUsersByProject(final List<CProject> projects) {
		Check.notNull(projects, "Projects list cannot be null");
		return projects.stream().filter(project -> (project != null) && (project.getName() != null))
				.collect(Collectors.toMap(CProject::getName, project -> {
					if (userService == null) {
						LOGGER.error("UserService is null");
						throw new IllegalStateException("UserService is null");
					}
					// Get all users and count those that have project settings for this project
					final List<CUser> allUsers = userService.list(Pageable.unpaged()).getContent();
					if (allUsers == null) {
						LOGGER.error("User service returned null list");
						throw new IllegalStateException("User service returned null list");
					}
					final long userCount = allUsers.stream().filter(user -> (user != null) && (user.getProjectSettings() != null))
							.flatMap(user -> user.getProjectSettings().stream()).filter(settings -> settings instanceof CUserProjectSettings)
							.map(settings -> settings).filter(settings -> settings.getProject() != null)
							.filter(settings -> settings.getProject().getId().equals(project.getId())).count();
					LOGGER.debug("Project {} has {} users", project.getName(), userCount);
					return userCount;
				}));
	}

	/** Creates a card displaying the total number of activities across all projects.
	 * @param activitiesByProject Map containing activity counts per project, must not be null
	 * @return Div containing the activities card */
	private final Div createActivitiesCard(final Map<String, Long> activitiesByProject) {
		LOGGER.debug("createActivitiesCard called with activitiesByProject size={}", activitiesByProject != null ? activitiesByProject.size() : 0);
		Check.notNull(activitiesByProject, "activitiesByProject cannot be null");
		final Div card = createCard();
		Check.notNull(card, "Card creation failed");
		final Icon icon = VaadinIcon.TASKS.create();
		if (icon != null) {
			icon.addClassNames(LumoUtility.IconSize.LARGE, LumoUtility.TextColor.WARNING);
		}
		final long totalActivities = activitiesByProject.values().stream().mapToLong(Long::longValue).sum();
		final Span count = new Span(String.valueOf(totalActivities));
		Check.notNull(count, "Count creation failed");
		count.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD);
		final Span label = new Span("Total Activities");
		Check.notNull(label, "Label creation failed");
		label.addClassNames(LumoUtility.TextColor.SECONDARY);
		final VerticalLayout content = new VerticalLayout(icon, count, label);
		Check.notNull(content, "Content layout creation failed");
		content.setAlignItems(FlexComponent.Alignment.CENTER);
		content.setSpacing(false);
		card.add(content);
		return card;
	}

	/** Creates a styled card component with consistent appearance and hover effects.
	 * @return Div configured as a card */
	private final Div createCard() {
		LOGGER.debug("createCard called");
		final Div card = new Div();
		card.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.LARGE, LumoUtility.Padding.LARGE, LumoUtility.Border.ALL,
				LumoUtility.BorderColor.CONTRAST_10);
		card.getStyle().set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)").set("transition", "transform 0.2s ease, box-shadow 0.2s ease")
				.set("min-height", "150px").set("flex", "1");
		// Add hover effect
		card.getElement().addEventListener("mouseenter", e -> {
			if (card.getStyle() != null) {
				card.getStyle().set("transform", "translateY(-2px)").set("box-shadow", "0 4px 8px rgba(0,0,0,0.15)");
			}
		});
		card.getElement().addEventListener("mouseleave", e -> {
			if (card.getStyle() != null) {
				card.getStyle().set("transform", "translateY(0)").set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
			}
		});
		return card;
	}

	/** Creates the detailed project breakdown section showing individual project statistics.
	 * @param projects            List of projects to display, must not be null
	 * @param usersByProject      Map of user counts per project, must not be null
	 * @param activitiesByProject Map of activity counts per project, must not be null
	 * @return Div containing the project breakdown, or null if creation fails */
	private final Div createDetailedBreakdown(final List<CProject> projects, final Map<String, Long> usersByProject,
			final Map<String, Long> activitiesByProject) {
		LOGGER.debug("createDetailedBreakdown called with {} projects", projects != null ? projects.size() : 0);
		Check.notNull(projects, "Projects list cannot be null");
		Check.notNull(usersByProject, "UsersByProject map cannot be null");
		Check.notNull(activitiesByProject, "ActivitiesByProject map cannot be null");
		final Div breakdown = new Div();
		breakdown.addClassNames(LumoUtility.Margin.Top.XLARGE);
		final H3 title = new H3("Project Breakdown");
		if (title != null) {
			title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
			breakdown.add(title);
		}
		final VerticalLayout projectList = new VerticalLayout();
		if (projectList != null) {
			projectList.setSpacing(true);
			projectList.setWidthFull();
			for (final CProject project : projects) {
				if (project != null) {
					final Div projectCard = createProjectBreakdownCard(project, usersByProject, activitiesByProject);
					if (projectCard != null) {
						projectList.add(projectCard);
					}
				}
			}
			breakdown.add(projectList);
		}
		return breakdown;
	}

	/** Creates a fallback error card when dialog creation fails.
	 * @param message The error message to display */
	private final void createFallbackErrorCard(final String message) {
		LOGGER.debug("createFallbackErrorCard called with message: {}", message);
		try {
			final Div card = createCard();
			if (card != null) {
				card.addClassNames(LumoUtility.Background.ERROR_10);
				final Icon icon = VaadinIcon.WARNING.create();
				if (icon != null) {
					icon.addClassNames(LumoUtility.IconSize.LARGE, LumoUtility.TextColor.ERROR);
				}
				final String displayMessage = message != null ? message : "An error occurred";
				final Span errorMessage = new Span(displayMessage);
				if (errorMessage != null) {
					errorMessage.addClassNames(LumoUtility.TextColor.ERROR);
				}
				final VerticalLayout content = new VerticalLayout(icon, errorMessage);
				if (content != null) {
					content.setAlignItems(FlexComponent.Alignment.CENTER);
					card.add(content);
				}
				add(card);
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating fallback error card.");
		}
	}

	/** Creates a card for individual project breakdown showing project name and statistics.
	 * @param project             The project to display, must not be null
	 * @param usersByProject      Map of user counts per project, must not be null
	 * @param activitiesByProject Map of activity counts per project, must not be null
	 * @return Div containing the project breakdown card, or null if creation fails */
	private final Div createProjectBreakdownCard(final CProject project, final Map<String, Long> usersByProject,
			final Map<String, Long> activitiesByProject) {
		LOGGER.debug("createProjectBreakdownCard called for project: {}", project != null ? project.getName() : "null");
		if ((project == null) || (usersByProject == null) || (activitiesByProject == null)) {
			LOGGER.warn("One or more parameters are null");
			return null;
		}
		try {
			final Div card = new Div();
			card.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM, LumoUtility.Padding.MEDIUM, LumoUtility.Border.ALL,
					LumoUtility.BorderColor.CONTRAST_10);
			card.setWidthFull();
			final HorizontalLayout content = new HorizontalLayout();
			if (content != null) {
				content.setWidthFull();
				content.setAlignItems(FlexComponent.Alignment.CENTER);
				content.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
				// Project name
				final String projectName = project.getName() != null ? project.getName() : "Unknown Project";
				final Span projectNameSpan = new Span(projectName);
				if (projectNameSpan != null) {
					projectNameSpan.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
				}
				// Stats
				final HorizontalLayout stats = createProjectStats(project, usersByProject, activitiesByProject);
				if ((projectNameSpan != null) && (stats != null)) {
					content.add(projectNameSpan, stats);
				}
				card.add(content);
			}
			return card;
		} catch (final Exception e) {
			LOGGER.error("Error creating project breakdown card.");
			return null;
		}
	}

	/** Creates a card displaying the total number of projects.
	 * @param totalProjects The total number of projects to display
	 * @return Div containing the projects card, or null if creation fails */
	private final Div createProjectsCard(final int totalProjects) {
		LOGGER.debug("createProjectsCard called with totalProjects={}", totalProjects);
		try {
			final Div card = createCard();
			if (card == null) {
				LOGGER.error("Failed to create base card");
				return null;
			}
			final Icon icon = VaadinIcon.BRIEFCASE.create();
			if (icon != null) {
				icon.addClassNames(LumoUtility.IconSize.LARGE, LumoUtility.TextColor.PRIMARY);
			}
			final Span count = new Span(String.valueOf(totalProjects));
			if (count != null) {
				count.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD);
			}
			final Span label = new Span("Total Projects");
			if (label != null) {
				label.addClassNames(LumoUtility.TextColor.SECONDARY);
			}
			final VerticalLayout content = new VerticalLayout(icon, count, label);
			if (content != null) {
				content.setAlignItems(FlexComponent.Alignment.CENTER);
				content.setSpacing(false);
				card.add(content);
			}
			return card;
		} catch (final Exception e) {
			LOGGER.error("Error creating projects card.");
			return null;
		}
	}

	/** Creates the statistics layout for a project showing user and activity counts.
	 * @param project             The project to create stats for, must not be null
	 * @param usersByProject      Map of user counts per project, must not be null
	 * @param activitiesByProject Map of activity counts per project, must not be null
	 * @return HorizontalLayout containing the stats, or null if creation fails */
	private final HorizontalLayout createProjectStats(final CProject project, final Map<String, Long> usersByProject,
			final Map<String, Long> activitiesByProject) {
		LOGGER.debug("createProjectStats called for project: {}", project != null ? project.getName() : "null");
		if ((project == null) || (usersByProject == null) || (activitiesByProject == null)) {
			LOGGER.warn("One or more parameters are null");
			return null;
		}
		try {
			final HorizontalLayout stats = new HorizontalLayout();
			stats.setSpacing(true);
			stats.setAlignItems(FlexComponent.Alignment.CENTER);
			final String projectName = project.getName();
			if (projectName != null) {
				final long userCount = usersByProject.getOrDefault(projectName, 0L);
				final Span users = new Span(userCount + " users");
				if (users != null) {
					users.addClassNames(LumoUtility.TextColor.SECONDARY);
					stats.add(users);
				}
				final long activityCount = activitiesByProject.getOrDefault(projectName, 0L);
				final Span activities = new Span(activityCount + " activities");
				if (activities != null) {
					activities.addClassNames(LumoUtility.TextColor.SECONDARY);
					stats.add(activities);
				}
			}
			return stats;
		} catch (final Exception e) {
			LOGGER.error("Error creating project stats.");
			return null;
		}
	}

	/** Creates the statistics container with proper layout configuration.
	 * @return HorizontalLayout configured for statistics cards, or null if creation fails */
	private final HorizontalLayout createStatsContainer() {
		LOGGER.debug("createStatsContainer called");
		try {
			final HorizontalLayout statsContainer = new HorizontalLayout();
			if (statsContainer != null) {
				statsContainer.setSpacing(true);
				statsContainer.setWidthFull();
				statsContainer.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.STRETCH);
				return statsContainer;
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating stats container.");
		}
		return null;
	}

	/** Creates a card displaying the total number of users across all projects.
	 * @param usersByProject Map containing user counts per project, must not be null
	 * @return Div containing the users card, or null if creation fails */
	private final Div createUsersCard(final Map<String, Long> usersByProject) {
		LOGGER.debug("createUsersCard called with usersByProject size={}", usersByProject != null ? usersByProject.size() : 0);
		if (usersByProject == null) {
			LOGGER.warn("usersByProject is null");
			return null;
		}
		try {
			final Div card = createCard();
			if (card == null) {
				LOGGER.error("Failed to create base card");
				return null;
			}
			final Icon icon = VaadinIcon.USERS.create();
			if (icon != null) {
				icon.addClassNames(LumoUtility.IconSize.LARGE, LumoUtility.TextColor.SUCCESS);
			}
			final long totalUsers = usersByProject.values().stream().mapToLong(Long::longValue).sum();
			final Span count = new Span(String.valueOf(totalUsers));
			if (count != null) {
				count.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD);
			}
			final Span label = new Span("Total Users");
			if (label != null) {
				label.addClassNames(LumoUtility.TextColor.SECONDARY);
			}
			final VerticalLayout content = new VerticalLayout(icon, count, label);
			if (content != null) {
				content.setAlignItems(FlexComponent.Alignment.CENTER);
				content.setSpacing(false);
				card.add(content);
			}
			return card;
		} catch (final Exception e) {
			LOGGER.error("Error creating users card.");
			return null;
		}
	}

	@Override
	public String getPageTitle() { // TODO Auto-generated method stub
		return "Home";
	}

	/** Handles errors by displaying them to the user using CExceptionDialog.
	 * @param message   User-friendly error message
	 * @param exception The exception that occurred */
	private final void handleError(final String message, final Exception exception) {
		LOGGER.error("handleError called with message: {}, exception: {}", message, exception != null ? exception.getMessage() : "null");
		try {
			if ((message != null) && (exception != null)) {
				final CExceptionDialog dialog = new CExceptionDialog(exception);
				if (dialog != null) {
					dialog.open();
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating exception dialog.");
			// Fallback: create a simple error card
			createFallbackErrorCard(message);
		}
	}

	/** Initializes the page with dashboard components and layout. Sets up the main container with title and subtitle. */
	@Override
	protected final void initPage() {
		LOGGER.info("initPage called for CDashboardView");
		try {
			// Configure main layout
			addClassName("cdashboard-view");
			// Add title
			final H1 title = new H1("System Dashboard");
			if (title != null) {
				title.addClassNames(LumoUtility.Margin.Bottom.LARGE);
				add(title);
			}
			// Add subtitle
			final Paragraph subtitle = new Paragraph("Overview of projects, users, and activities");
			if (subtitle != null) {
				subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Bottom.XLARGE);
				add(subtitle);
			}
			// Load dashboard data
			loadDashboardData();
			LOGGER.info("initPage completed successfully");
		} catch (final Exception e) {
			LOGGER.error("Error in initPage.");
			handleError("Failed to initialize dashboard page", e);
		}
	}

	/** Loads and displays the dashboard data including statistics cards and project breakdown. Handles any errors gracefully and displays them to the
	 * user. */
	private final void loadDashboardData() {
		LOGGER.info("loadDashboardData called");
		try {
			// Get all projects with null check
			final List<CProject> projects = projectService != null ? projectService.findAll() : Collections.emptyList();
			if (projects == null) {
				LOGGER.warn("Projects list is null, using empty list");
				return;
			}
			LOGGER.debug("Found {} projects", projects.size());
			// Create statistics cards container
			final HorizontalLayout statsContainer = createStatsContainer();
			if (statsContainer == null) {
				LOGGER.error("Failed to create stats container");
				return;
			}
			// Add total projects card
			final Div projectsCard = createProjectsCard(projects.size());
			if (projectsCard != null) {
				statsContainer.add(projectsCard);
			}
			// Calculate user statistics per project
			final Map<String, Long> usersByProject = calculateUsersByProject(projects);
			if (usersByProject != null) {
				final Div usersCard = createUsersCard(usersByProject);
				if (usersCard != null) {
					statsContainer.add(usersCard);
				}
			}
			// Calculate activity statistics per project
			final Map<String, Long> activitiesByProject = calculateActivitiesByProject(projects);
			if (activitiesByProject != null) {
				final Div activitiesCard = createActivitiesCard(activitiesByProject);
				if (activitiesCard != null) {
					statsContainer.add(activitiesCard);
				}
			}
			add(statsContainer);
			// Add detailed breakdown if there are projects
			if (!projects.isEmpty() && (usersByProject != null) && (activitiesByProject != null)) {
				final Div breakdown = createDetailedBreakdown(projects, usersByProject, activitiesByProject);
				if (breakdown != null) {
					add(breakdown);
				}
			}
			LOGGER.info("loadDashboardData completed successfully");
		} catch (final Exception e) {
			LOGGER.error("Error loading dashboard data.");
			handleError("Error loading dashboard data", e);
		}
	}

	/** Sets up the toolbar for the dashboard page. Currently no toolbar is needed for the dashboard. */
	@Override
	protected final void setupToolbar() {
		LOGGER.info("setupToolbar called for CDashboardView - no toolbar setup needed");
		// No toolbar needed for dashboard view
	}
}
