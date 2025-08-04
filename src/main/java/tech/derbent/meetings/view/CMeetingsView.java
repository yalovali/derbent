package tech.derbent.meetings.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.utils.PageableUtils;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingStatusService;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.service.CUserService;

@Route("meetings/:meeting_id?/:action?(edit)")
@PageTitle("Meeting Master Detail")
@Menu(order = 1.4, icon = "vaadin:group", title = "Project.Meetings")
@PermitAll // When security is enabled, allow all authenticated users
public class CMeetingsView extends CProjectAwareMDPage<CMeeting> {

    private static final long serialVersionUID = 1L;

    private final String ENTITY_ID_FIELD = "meeting_id";

    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "meetings/%s/edit";

    private final CMeetingTypeService meetingTypeService;

    private final CUserService userService;

    private final CMeetingStatusService meetingStatusService;

    private TextField searchField;

    public CMeetingsView(final CMeetingService entityService, final CSessionService sessionService,
            final CMeetingTypeService meetingTypeService, final CUserService userService,
            final CMeetingStatusService meetingStatusService) {
        super(CMeeting.class, entityService, sessionService);
        addClassNames("meetings-view");
        this.meetingTypeService = meetingTypeService;
        this.userService = userService;
        this.meetingStatusService = meetingStatusService;
        // createDetailsLayout();
    }

    @Override
    protected void createDetailsLayout() {
        CAccordionDBEntity<CMeeting> panel;
        panel = new CPanelMeetingBasicInfo(getCurrentEntity(), getBinder(), (CMeetingService) entityService,
                meetingTypeService);
        addAccordionPanel(panel);
        panel = new CPanelMeetingParticipants(getCurrentEntity(), getBinder(), (CMeetingService) entityService,
                meetingTypeService, userService);
        addAccordionPanel(panel);
        panel = new CPanelMeetingSchedule(getCurrentEntity(), getBinder(), (CMeetingService) entityService);
        addAccordionPanel(panel);
        panel = new CPanelMeetingAgenda(getCurrentEntity(), getBinder(), (CMeetingService) entityService);
        addAccordionPanel(panel);
        panel = new CPanelMeetingMinutes(getCurrentEntity(), getBinder(), (CMeetingService) entityService);
        addAccordionPanel(panel);
        panel = new CPanelMeetingStatus(getCurrentEntity(), getBinder(), (CMeetingService) entityService,
                meetingStatusService);
        addAccordionPanel(panel);
    }

    @Override
    protected void createGridForEntity() {
        LOGGER.info("Creating enhanced grid for meetings with project and participant details");
        // Project Name - Important for context
        grid.addColumn(meeting -> meeting.getProject() != null ? meeting.getProject().getName() : "No Project")
                .setAutoWidth(true).setHeader("Project").setSortable(true).setFlexGrow(0);
        // Meeting Name
        grid.addColumn(meeting -> meeting.getName()).setAutoWidth(true).setHeader("Meeting Name").setSortable(true)
                .setFlexGrow(1);
        // Meeting Type
        grid.addColumn(meeting -> meeting.getMeetingType() != null ? meeting.getMeetingType().getName() : "No Type")
                .setAutoWidth(true).setHeader("Type").setSortable(true).setFlexGrow(0);
        // Start Time with proper formatting
        grid.addColumn(meeting -> {

            if (meeting.getMeetingDate() != null) {
                return meeting.getMeetingDate()
                        .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
            }
            return "Not set";
        }).setAutoWidth(true).setHeader("Start Time").setSortable(true).setFlexGrow(0);
        // End Time with proper formatting
        grid.addColumn(meeting -> {

            if (meeting.getEndDate() != null) {
                return meeting.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
            }
            return "Not set";
        }).setAutoWidth(true).setHeader("End Time").setSortable(true).setFlexGrow(0);
        // Participants with names instead of just count
        grid.addColumn(meeting -> {

            if (meeting.getParticipants().isEmpty()) {
                return "No participants";
            }
            return meeting.getParticipants().stream()
                    .map(user -> user.getName() != null ? user.getName() : "User #" + user.getId())
                    .collect(java.util.stream.Collectors.joining(", "));
        }).setAutoWidth(true).setHeader("Participants").setSortable(false).setFlexGrow(1);
        // Description - shortened for grid display
        grid.addColumn(meeting -> {

            if ((meeting.getDescription() == null) || meeting.getDescription().trim().isEmpty()) {
                return "No description";
            }
            final String desc = meeting.getDescription().trim();
            return desc.length() > 50 ? desc.substring(0, 47) + "..." : desc;
        }).setAutoWidth(true).setHeader("Description").setSortable(false).setFlexGrow(1);
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
    protected void setProjectForEntity(final CMeeting entity, final tech.derbent.projects.domain.CProject project) {
        entity.setProject(project);
    }

    @Override
    protected void setupToolbar() {
        // Create search field
        searchField = new TextField();
        searchField.setPlaceholder("Search meetings...");
        searchField.setPrefixComponent(new com.vaadin.flow.component.icon.Icon(com.vaadin.flow.component.icon.VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(300); // 300ms delay
        
        // Add value change listener to filter the grid
        searchField.addValueChangeListener(event -> {
            String searchText = event.getValue();
            if (searchText == null || searchText.trim().isEmpty()) {
                // Clear filter - use the existing grid refresh mechanism
                refreshProjectAwareGrid();
            } else {
                // Apply filter
                if (entityService instanceof tech.derbent.abstracts.services.CEntityOfProjectService) {
                    final tech.derbent.abstracts.services.CEntityOfProjectService<CMeeting> projectService = 
                            (tech.derbent.abstracts.services.CEntityOfProjectService<CMeeting>) entityService;
                    
                    final Optional<tech.derbent.projects.domain.CProject> activeProject = sessionService.getActiveProject();
                    if (activeProject.isPresent()) {
                        List<CMeeting> allMeetings = projectService.findAllByProject(activeProject.get(), 
                                org.springframework.data.domain.Pageable.unpaged());
                        List<CMeeting> filteredMeetings = allMeetings.stream()
                                .filter(meeting -> matchesSearchText(meeting, searchText.toLowerCase().trim()))
                                .collect(java.util.stream.Collectors.toList());
                        grid.setItems(filteredMeetings);
                    } else {
                        grid.setItems(Collections.emptyList());
                    }
                }
            }
        });
        
        // Create toolbar layout
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        toolbar.setWidthFull();
        toolbar.setPadding(true);
        toolbar.add(searchField);
        
        // Add toolbar to the top of the page
        Div toolbarWrapper = new Div();
        toolbarWrapper.setClassName("toolbar-wrapper");
        toolbarWrapper.add(toolbar);
        
        // Insert toolbar at the beginning of the page
        getElement().insertChild(0, toolbarWrapper.getElement());
        
        LOGGER.info("Search toolbar added to meetings view");
    }
    
    /**
     * Checks if a meeting matches the search text by searching across all string fields.
     */
    private boolean matchesSearchText(CMeeting meeting, String searchText) {
        if (meeting == null || searchText == null || searchText.isEmpty()) {
            return true;
        }
        
        // Search in project name
        if (meeting.getProject() != null && meeting.getProject().getName() != null) {
            if (meeting.getProject().getName().toLowerCase().contains(searchText)) {
                return true;
            }
        }
        
        // Search in meeting name
        if (meeting.getName() != null && meeting.getName().toLowerCase().contains(searchText)) {
            return true;
        }
        
        // Search in meeting type name
        if (meeting.getMeetingType() != null && meeting.getMeetingType().getName() != null) {
            if (meeting.getMeetingType().getName().toLowerCase().contains(searchText)) {
                return true;
            }
        }
        
        // Search in formatted dates (start and end time)
        if (meeting.getMeetingDate() != null) {
            String formattedStartTime = meeting.getMeetingDate()
                    .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
            if (formattedStartTime.toLowerCase().contains(searchText)) {
                return true;
            }
        }
        
        if (meeting.getEndDate() != null) {
            String formattedEndTime = meeting.getEndDate()
                    .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
            if (formattedEndTime.toLowerCase().contains(searchText)) {
                return true;
            }
        }
        
        // Search in participants names
        if (!meeting.getParticipants().isEmpty()) {
            String participants = meeting.getParticipants().stream()
                    .map(user -> user.getName() != null ? user.getName() : "User #" + user.getId())
                    .collect(java.util.stream.Collectors.joining(", "));
            if (participants.toLowerCase().contains(searchText)) {
                return true;
            }
        }
        
        // Search in description
        if (meeting.getDescription() != null && meeting.getDescription().toLowerCase().contains(searchText)) {
            return true;
        }
        
        return false;
    }
}