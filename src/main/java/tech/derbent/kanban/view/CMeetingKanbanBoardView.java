package tech.derbent.kanban.view;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.base.ui.CBaseKanbanBoardView;
import tech.derbent.base.ui.CBaseKanbanColumn;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.session.service.CSessionService;

/**
 * CMeetingKanbanBoardView - Kanban board view for meetings using the generic base classes.
 * Layer: View (MVC)
 * 
 * This implementation demonstrates how the generic kanban system can be used
 * for different entity types like meetings.
 */
@Route("meetings-kanban")
@PageTitle("Meeting Kanban Board")
@Menu(order = 3.1, icon = "vaadin:calendar", title = "Meetings.Kanban")
@PermitAll
public class CMeetingKanbanBoardView extends CBaseKanbanBoardView<CMeeting, CMeetingStatus> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingKanbanBoardView.class);

    /**
     * Constructor for CMeetingKanbanBoardView.
     * 
     * @param meetingService the meeting service for data operations
     * @param sessionService the session service for project context
     */
    public CMeetingKanbanBoardView(final CMeetingService meetingService, final CSessionService sessionService) {
        super(meetingService, sessionService);
        LOGGER.info("Initialized CMeetingKanbanBoardView");
    }

    @Override
    protected String getBoardTitle() {
        return "Meeting Kanban Board";
    }

    @Override
    protected String getBoardCssClass() {
        return "meeting-kanban-board";
    }

    @Override
    protected CBaseKanbanColumn<CMeeting, CMeetingStatus> createKanbanColumn(final CMeetingStatus status, 
            final List<CMeeting> entities) {
        return new CMeetingKanbanColumn(status, entities);
    }
}