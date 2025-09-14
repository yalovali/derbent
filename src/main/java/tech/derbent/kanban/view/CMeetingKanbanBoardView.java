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

/** CMeetingKanbanBoardView - Kanban board view for meetings using the generic base classes. Layer: View (MVC) This implementation demonstrates how
 * the generic kanban system can be used for different entity types like meetings. */
@Route ("meetings-kanban")
@PageTitle ("Meeting Kanban Board")
@Menu (order = 3.1, icon = "class:tech.derbent.kanban.view.CMeetingKanbanBoardView", title = "Projects.MeetingsKanban")
@PermitAll
public class CMeetingKanbanBoardView extends CBaseKanbanBoardView<CMeeting, CMeetingStatus> {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingKanbanBoardView.class);

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() {
		return "#28a745"; // Green color for meetings
	}

	public static String getStaticIconFilename() { return "vaadin:calendar"; }

	/** Constructor for CMeetingKanbanBoardView.
	 * @param meetingService the meeting service for data operations
	 * @param sessionService the session service for project context */
	public CMeetingKanbanBoardView(final CMeetingService meetingService, final CSessionService sessionService) {
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
