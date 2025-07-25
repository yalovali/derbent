package tech.derbent.projects.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;

/**
 * CProjectsView - View for managing projects. Layer: View (MVC) Provides CRUD operations for projects using the
 * abstract master-detail pattern.
 */
@Route("projects/:project_id?/:action?(edit)")
@PageTitle("Project Master Detail")
@Menu(order = 0, icon = "vaadin:briefcase", title = "Settings.Projects")
@PermitAll // When security is enabled, allow all authenticated users
public class CProjectsView extends CAbstractMDPage<CProject> {

    private static final long serialVersionUID = 1L;
    private final String ENTITY_ID_FIELD = "project_id";
    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "projects/%s/edit";

    public CProjectsView(final CProjectService entityService) {
        super(CProject.class, entityService);
        addClassNames("projects-view");
        // createDetailsLayout();
        LOGGER.info("CProjectsView initialized successfully");
    }

    @Override
    protected void createDetailsLayout() {
        LOGGER.info("Creating enhanced details layout for CProjectsView");
        
        // Create main form wrapper with card styling
        final VerticalLayout formWrapper = new VerticalLayout();
        formWrapper.setClassName("details-form-card");
        formWrapper.setSpacing(true);
        formWrapper.setPadding(false);
        formWrapper.setSizeFull();
        
        // Create form header with icon
        final H3 formHeader = new H3();
        formHeader.add(new Icon(VaadinIcon.FOLDER), new Span("Project Details"));
        formWrapper.add(formHeader);
        
        // Build the form using the existing form builder
        final Div formContent = CEntityFormBuilder.buildForm(CProject.class, getBinder());
        formWrapper.add(formContent);
        
        // Add additional project information section
        final Div projectInfoCard = createProjectInfoCard();
        formWrapper.add(projectInfoCard);
        
        getBaseDetailsLayout().add(formWrapper);
    }

    @Override
    protected Div createDetailsTabLeftContent() {
        // Create enhanced tab content with icon
        final Div detailsTabLabel = new Div();
        detailsTabLabel.setClassName("details-tab-label");
        
        final Icon projectIcon = new Icon(VaadinIcon.BRIEFCASE);
        final Span labelText = new Span("Project Information");
        
        detailsTabLabel.add(projectIcon, labelText);
        return detailsTabLabel;
    }

    @Override
    protected void createGridForEntity() {
        LOGGER.info("Creating enhanced grid for projects");
        
        // Configure grid columns with better styling
        grid.addComponentColumn(this::createProjectRowComponent)
            .setAutoWidth(true)
            .setHeader("Projects")
            .setSortable(false);
            
        // Add row styling
        grid.setClassNameGenerator(project -> "project-row");
        
        // Enhanced selection listener
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(CProjectsView.class);
            }
        });
    }

    @Override
    protected String getEntityRouteIdField() {
        return ENTITY_ID_FIELD;
    }

    @Override
    protected String getEntityRouteTemplateEdit() {
        return ENTITY_ROUTE_TEMPLATE_EDIT;
    }

    @Override
    protected void initPage() {
        // Initialize the page components and layout This method can be overridden to
        // set up the view's components
    }

    @Override
    protected CProject newEntity() {
        return new CProject();
    }

    @Override
    protected void setupToolbar() {
        // TODO: Implement toolbar setup if needed
    }
    
    /**
     * Creates an enhanced project row component with icon and metadata
     * @param project The project to display
     * @return HorizontalLayout containing the project information
     */
    private HorizontalLayout createProjectRowComponent(final CProject project) {
        final HorizontalLayout card = new HorizontalLayout();
        card.setClassName("project-info-card");
        card.setDefaultVerticalComponentAlignment(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        card.setSizeFull();
        
        // Project icon
        final Div projectIcon = new Div();
        projectIcon.setClassName("project-icon");
        projectIcon.setText(project.getName() != null && !project.getName().isEmpty() 
            ? project.getName().substring(0, 1).toUpperCase() 
            : "P");
        
        // Project details
        final VerticalLayout projectDetails = new VerticalLayout();
        projectDetails.setClassName("project-details");
        projectDetails.setSpacing(false);
        projectDetails.setPadding(false);
        
        final Span projectName = new Span(project.getName() != null ? project.getName() : "Unnamed Project");
        projectName.setClassName("project-name");
        
        final Span projectMeta = new Span("ID: " + project.getId());
        projectMeta.setClassName("project-meta");
        
        projectDetails.add(projectName, projectMeta);
        
        card.add(projectIcon, projectDetails);
        return card;
    }
    
    /**
     * Creates additional project information card
     * @return Div containing project statistics and info
     */
    private Div createProjectInfoCard() {
        final Div infoCard = new Div();
        infoCard.setClassName("details-form-card");
        
        final H3 infoHeader = new H3();
        infoHeader.add(new Icon(VaadinIcon.INFO_CIRCLE), new Span("Project Statistics"));
        
        final Div statsContent = new Div();
        final Span statusIndicator = new Span("Active");
        statusIndicator.setClassName("status-indicator status-active");
        
        final VerticalLayout stats = new VerticalLayout();
        stats.setSpacing(false);
        stats.setPadding(false);
        stats.add(
            new Span("Status: "),
            statusIndicator,
            new Span("Created: " + java.time.LocalDate.now().toString()),
            new Span("Last modified: " + java.time.LocalDate.now().toString())
        );
        
        statsContent.add(stats);
        infoCard.add(infoHeader, statsContent);
        
        return infoCard;
    }
}
