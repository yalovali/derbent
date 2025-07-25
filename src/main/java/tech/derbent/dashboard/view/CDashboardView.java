package tech.derbent.dashboard.view;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.PermitAll;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserService;

/**
 * CDashboardView - Dashboard view showing system summary statistics.
 * Displays total projects, users per project, and activities per project.
 */
@Route("dashboard")
@PageTitle("Dashboard")
@Menu(order = 0, icon = "vaadin:dashboard", title = "Root Menu")
@PermitAll
public class CDashboardView extends VerticalLayout {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CDashboardView.class);

    private final CProjectService projectService;
    private final CUserService userService;
    private final CActivityService activityService;

    public CDashboardView(final CProjectService projectService, 
                         final CUserService userService,
                         final CActivityService activityService) {
        this.projectService = projectService;
        this.userService = userService;
        this.activityService = activityService;
        
        LOGGER.info("Creating CDashboardView");
        
        initializeView();
        loadDashboardData();
        
        LOGGER.info("CDashboardView initialized successfully");
    }

    private void initializeView() {
        // Configure main layout
        setSpacing(true);
        setPadding(true);
        setWidthFull();
        addClassName("dashboard-view");
        
        // Add title
        final H1 title = new H1("System Dashboard");
        title.addClassNames(LumoUtility.Margin.Bottom.LARGE);
        add(title);
        
        // Add subtitle
        final Paragraph subtitle = new Paragraph("Overview of projects, users, and activities");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Bottom.XLARGE);
        add(subtitle);
    }

    private void loadDashboardData() {
        try {
            // Get all projects
            final List<CProject> projects = projectService.findAll();
            
            // Create statistics cards container
            final HorizontalLayout statsContainer = new HorizontalLayout();
            statsContainer.setSpacing(true);
            statsContainer.setWidthFull();
            statsContainer.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.STRETCH);
            
            // Add total projects card
            statsContainer.add(createProjectsCard(projects.size()));
            
            // Calculate user statistics per project
            final Map<String, Long> usersByProject = calculateUsersByProject(projects);
            statsContainer.add(createUsersCard(usersByProject));
            
            // Calculate activity statistics per project
            final Map<String, Long> activitiesByProject = calculateActivitiesByProject(projects);
            statsContainer.add(createActivitiesCard(activitiesByProject));
            
            add(statsContainer);
            
            // Add detailed breakdown if there are projects
            if (!projects.isEmpty()) {
                add(createDetailedBreakdown(projects, usersByProject, activitiesByProject));
            }
            
        } catch (final Exception e) {
            LOGGER.error("Error loading dashboard data", e);
            add(createErrorCard("Error loading dashboard data: " + e.getMessage()));
        }
    }

    private Div createProjectsCard(final int totalProjects) {
        final Div card = createCard();
        
        final Icon icon = VaadinIcon.BRIEFCASE.create();
        icon.addClassNames(LumoUtility.IconSize.LARGE, LumoUtility.TextColor.PRIMARY);
        
        final Span count = new Span(String.valueOf(totalProjects));
        count.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD);
        
        final Span label = new Span("Total Projects");
        label.addClassNames(LumoUtility.TextColor.SECONDARY);
        
        final VerticalLayout content = new VerticalLayout(icon, count, label);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(false);
        
        card.add(content);
        return card;
    }

    private Div createUsersCard(final Map<String, Long> usersByProject) {
        final Div card = createCard();
        
        final Icon icon = VaadinIcon.USERS.create();
        icon.addClassNames(LumoUtility.IconSize.LARGE, LumoUtility.TextColor.SUCCESS);
        
        final long totalUsers = usersByProject.values().stream().mapToLong(Long::longValue).sum();
        final Span count = new Span(String.valueOf(totalUsers));
        count.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD);
        
        final Span label = new Span("Total Users");
        label.addClassNames(LumoUtility.TextColor.SECONDARY);
        
        final VerticalLayout content = new VerticalLayout(icon, count, label);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(false);
        
        card.add(content);
        return card;
    }

    private Div createActivitiesCard(final Map<String, Long> activitiesByProject) {
        final Div card = createCard();
        
        final Icon icon = VaadinIcon.TASKS.create();
        icon.addClassNames(LumoUtility.IconSize.LARGE, LumoUtility.TextColor.WARNING);
        
        final long totalActivities = activitiesByProject.values().stream().mapToLong(Long::longValue).sum();
        final Span count = new Span(String.valueOf(totalActivities));
        count.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD);
        
        final Span label = new Span("Total Activities");
        label.addClassNames(LumoUtility.TextColor.SECONDARY);
        
        final VerticalLayout content = new VerticalLayout(icon, count, label);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(false);
        
        card.add(content);
        return card;
    }

    private Div createCard() {
        final Div card = new Div();
        card.addClassNames(
            LumoUtility.Background.CONTRAST_5,
            LumoUtility.BorderRadius.LARGE,
            LumoUtility.Padding.LARGE,
            LumoUtility.Border.ALL,
            LumoUtility.BorderColor.CONTRAST_10
        );
        card.getStyle()
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
            .set("transition", "transform 0.2s ease, box-shadow 0.2s ease")
            .set("min-height", "150px")
            .set("flex", "1");
        
        // Add hover effect
        card.getElement().addEventListener("mouseenter", e -> 
            card.getStyle().set("transform", "translateY(-2px)")
                          .set("box-shadow", "0 4px 8px rgba(0,0,0,0.15)"));
        
        card.getElement().addEventListener("mouseleave", e -> 
            card.getStyle().set("transform", "translateY(0)")
                          .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)"));
        
        return card;
    }

    private Div createDetailedBreakdown(final List<CProject> projects, 
                                       final Map<String, Long> usersByProject,
                                       final Map<String, Long> activitiesByProject) {
        final Div breakdown = new Div();
        breakdown.addClassNames(LumoUtility.Margin.Top.XLARGE);
        
        final H3 title = new H3("Project Breakdown");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        breakdown.add(title);
        
        final VerticalLayout projectList = new VerticalLayout();
        projectList.setSpacing(true);
        projectList.setWidthFull();
        
        for (final CProject project : projects) {
            final Div projectCard = createProjectBreakdownCard(project, usersByProject, activitiesByProject);
            projectList.add(projectCard);
        }
        
        breakdown.add(projectList);
        return breakdown;
    }

    private Div createProjectBreakdownCard(final CProject project, 
                                          final Map<String, Long> usersByProject,
                                          final Map<String, Long> activitiesByProject) {
        final Div card = new Div();
        card.addClassNames(
            LumoUtility.Background.BASE,
            LumoUtility.BorderRadius.MEDIUM,
            LumoUtility.Padding.MEDIUM,
            LumoUtility.Border.ALL,
            LumoUtility.BorderColor.CONTRAST_10
        );
        card.setWidthFull();
        
        final HorizontalLayout content = new HorizontalLayout();
        content.setWidthFull();
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        // Project name
        final Span projectName = new Span(project.getName());
        projectName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        
        // Stats
        final HorizontalLayout stats = new HorizontalLayout();
        stats.setSpacing(true);
        stats.setAlignItems(FlexComponent.Alignment.CENTER);
        
        final long userCount = usersByProject.getOrDefault(project.getName(), 0L);
        final Span users = new Span(userCount + " users");
        users.addClassNames(LumoUtility.TextColor.SECONDARY);
        
        final long activityCount = activitiesByProject.getOrDefault(project.getName(), 0L);
        final Span activities = new Span(activityCount + " activities");
        activities.addClassNames(LumoUtility.TextColor.SECONDARY);
        
        stats.add(users, activities);
        
        content.add(projectName, stats);
        card.add(content);
        
        return card;
    }

    private Div createErrorCard(final String message) {
        final Div card = createCard();
        card.addClassNames(LumoUtility.Background.ERROR_10);
        
        final Icon icon = VaadinIcon.WARNING.create();
        icon.addClassNames(LumoUtility.IconSize.LARGE, LumoUtility.TextColor.ERROR);
        
        final Span errorMessage = new Span(message);
        errorMessage.addClassNames(LumoUtility.TextColor.ERROR);
        
        final VerticalLayout content = new VerticalLayout(icon, errorMessage);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        
        card.add(content);
        return card;
    }

    private Map<String, Long> calculateUsersByProject(final List<CProject> projects) {
        return projects.stream().collect(Collectors.toMap(
            CProject::getName,
            project -> {
                try {
                    // Count users that have project settings for this project
                    final List<CUserProjectSettings> allProjectSettings = userService.list(null)
                        .stream()
                        .flatMap(user -> user.getProjectSettings() != null ? 
                            user.getProjectSettings().stream() : java.util.stream.Stream.empty())
                        .filter(settings -> settings.getProjectId().equals(project.getId()))
                        .collect(Collectors.toList());
                    
                    return (long) allProjectSettings.size();
                } catch (final Exception e) {
                    LOGGER.warn("Error calculating users for project {}: {}", project.getName(), e.getMessage());
                    return 0L;
                }
            }
        ));
    }

    private Map<String, Long> calculateActivitiesByProject(final List<CProject> projects) {
        return projects.stream().collect(Collectors.toMap(
            CProject::getName,
            project -> {
                try {
                    return (long) activityService.findByProject(project).size();
                } catch (final Exception e) {
                    LOGGER.warn("Error calculating activities for project {}: {}", project.getName(), e.getMessage());
                    return 0L;
                }
            }
        ));
    }
}