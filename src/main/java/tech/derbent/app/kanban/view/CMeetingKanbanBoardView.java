package tech.derbent.app.kanban.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.ui.CBaseKanbanBoardView;
import tech.derbent.api.ui.CBaseKanbanColumn;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.domain.CMeetingStatus;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.base.session.service.ISessionService;

/** CMeetingKanbanBoardView - Kanban board view for meetings using the generic base classes. Layer: View (MVC) This implementation demonstrates how
 * the generic kanban system can be used for different entity types like meetings. */
@Route ("meetings-kanban")
@PageTitle ("Meeting Kanban Board")
@Menu (order = 3.1001, icon = "class:tech.derbent.app.kanban.view.CMeetingKanbanBoardView", title = "Project.MeetingsKanban")
@PermitAll
public class CMeetingKanbanBoardView extends CBaseKanbanBoardView<CMeeting, CMeetingStatus> {

	public static final String DEFAULT_COLOR = "#fd7e14";
	public static final String DEFAULT_ICON = "vaadin:kanban";
	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingKanbanBoardView.class);
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Kanban View";

	/** Constructor for CMeetingKanbanBoardView.
	 * @param meetingService the meeting service for data operations
	 * @param sessionService the session service for project context */
	public CMeetingKanbanBoardView(final CMeetingService meetingService, final ISessionService sessionService) {
		super(meetingService, sessionService);
		LOGGER.info("Initialized CMeetingKanbanBoardView");
	}

	@Override
	protected CBaseKanbanColumn<CMeeting, CMeetingStatus> createKanbanColumn(final CMeetingStatus status, final List<CMeeting> entities) {
		return new CMeetingKanbanColumn(status, entities);
	}

	@Override
	protected String getBoardCssClass() { return "meeting-kanban-board"; }

	@Override
	protected String getBoardTitle() { return "Meeting Kanban Board"; }
}
