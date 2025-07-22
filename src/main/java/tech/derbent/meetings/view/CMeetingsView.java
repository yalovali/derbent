package tech.derbent.meetings.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.session.service.SessionService;

@Route("meetings/:meeting_id?/:action?(edit)")
@PageTitle("Meeting Master Detail")
@Menu(order = 3, icon = "vaadin:group", title = "Settings.Meetings")
@PermitAll // When security is enabled, allow all authenticated users
public class CMeetingsView extends CProjectAwareMDPage<CMeeting> {

    private static final long serialVersionUID = 1L;
    private final String ENTITY_ID_FIELD = "meeting_id";
    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "meetings/%s/edit";
    private final CMeetingTypeService meetingTypeService;

    public CMeetingsView(final CMeetingService entityService, final SessionService sessionService, final CMeetingTypeService meetingTypeService) {
        super(CMeeting.class, entityService, sessionService);
        addClassNames("meetings-view");
        this.meetingTypeService = meetingTypeService;
        // createDetailsLayout();
    }

    @Override
    protected void createDetailsLayout() {
        LOGGER.info("Creating details layout for CMeetingsView using annotation-based data providers");
        final Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");
        
        // NEW APPROACH: No data provider needed! 
        // The @MetaData annotation on CMeeting.meetingType specifies dataProviderBean = "CMeetingTypeService"
        // The @MetaData annotation on CMeeting.participants specifies dataProviderBean = "CUserService"
        // This makes the code much simpler and more maintainable
        editorLayoutDiv.add(CEntityFormBuilder.buildForm(CMeeting.class, getBinder()));
        
        getBaseDetailsLayout().add(editorLayoutDiv);
    }

    @Override
    protected void createGridForEntity() {
        LOGGER.info("Creating grid for meetings");
        
        grid.addColumn(meeting -> meeting.getName())
            .setAutoWidth(true)
            .setHeader("Name")
            .setSortable(true);
            
        grid.addColumn(meeting -> meeting.getMeetingType() != null ? meeting.getMeetingType().getName() : "")
            .setAutoWidth(true)
            .setHeader("Type")
            .setSortable(true);
            
        grid.addColumn(meeting -> meeting.getDescription())
            .setAutoWidth(true)
            .setHeader("Description")
            .setSortable(true);
            
        grid.addColumn(meeting -> meeting.getMeetingDate())
            .setAutoWidth(true)
            .setHeader("Start Time")
            .setSortable(true);
            
        grid.addColumn(meeting -> meeting.getEndDate())
            .setAutoWidth(true)
            .setHeader("End Time")
            .setSortable(true);
            
        grid.addColumn(meeting -> meeting.getParticipants().size())
            .setAutoWidth(true)
            .setHeader("Participants")
            .setSortable(true);
    }

    @Override
    protected CMeeting createNewEntityInstance() {
        return new CMeeting();
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
    protected java.util.List<CMeeting> getProjectFilteredData(final tech.derbent.projects.domain.CProject project, final org.springframework.data.domain.Pageable pageable) {
        return ((CMeetingService) entityService).listByProject(project, pageable).getContent();
    }

    @Override
    protected void initPage() {
        // Initialize page components if needed
    }

    @Override
    protected CMeeting newEntity() {
        return super.newEntity(); // Uses the project-aware implementation from parent
    }

    @Override
    protected void setProjectForEntity(final CMeeting entity, final tech.derbent.projects.domain.CProject project) {
        entity.setProject(project);
    }

    @Override
    protected void setupToolbar() {
        // TODO: Implement toolbar setup if needed
    }
}