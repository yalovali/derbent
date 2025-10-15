package tech.derbent.kanban.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.api.ui.CBaseKanbanBoardView;
import tech.derbent.api.ui.CBaseKanbanColumn;
import tech.derbent.session.service.ISessionService;

/** CGenericActivityKanbanBoardView - Generic Kanban board view for activities using the base kanban classes. Layer: View (MVC) This implementation
 * uses the new generic kanban base classes to provide drag-and-drop functionality and better organization. */
@Route ("activities-kanban-generic")
@PageTitle ("Activity Kanban Board (Generic)")
@Menu (order = 1.300, icon = "class:tech.derbent.kanban.view.CGenericActivityKanbanBoardView", title = "Project.Generic Kanban")
@PermitAll
public class CGenericActivityKanbanBoardView extends CBaseKanbanBoardView<CActivity, CActivityStatus> {

	public static final String DEFAULT_COLOR = "#007bff";
	public static final String DEFAULT_ICON = "vaadin:kanban";
	private static final Logger LOGGER = LoggerFactory.getLogger(CGenericActivityKanbanBoardView.class);
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Kanban View";

	/** Constructor for CGenericActivityKanbanBoardView.
	 * @param activityService the activity service for data operations
	 * @param sessionService  the session service for project context */
	public CGenericActivityKanbanBoardView(final CActivityService activityService, final ISessionService sessionService) {
		super(activityService, sessionService);
		LOGGER.info("Initialized CGenericActivityKanbanBoardView");
	}

	@Override
	protected CBaseKanbanColumn<CActivity, CActivityStatus> createKanbanColumn(final CActivityStatus status, final List<CActivity> entities) {
		return new CGenericActivityKanbanColumn(status, entities);
	}

	@Override
	protected String getBoardCssClass() { return "activity-kanban-board"; }

	@Override
	protected String getBoardTitle() { return "Activity Kanban Board (Generic)"; }
}
