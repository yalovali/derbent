package tech.derbent.activities.view;

import java.lang.reflect.InvocationTargetException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseProject;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.comments.service.CCommentService;
import tech.derbent.comments.view.CPanelActivityComments;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

@Route ("cactivitiesview/:activity_id?/:action?(edit)")
@PageTitle ("Activity Master Detail")
@Menu (order = 1.1, icon = "class:tech.derbent.activities.view.CActivitiesView", title = "Project.Activities")
@PermitAll // When security is enabled, allow all authenticated users
public final class CActivitiesView extends CGridViewBaseProject<CActivity> {
	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CActivity.getIconColorCode(); // Use the static method from CActivity
	}

	public static String getIconFilename() { return CActivity.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "activity_id";
	private final CCommentService commentService;

	public CActivitiesView(final CActivityService entityService, final CSessionService sessionService, final CCommentService commentService,
			final CScreenService screenService) {
		super(CActivity.class, entityService, sessionService, screenService);
		this.commentService = commentService;
	}

	@Override
	public void createGridForEntity(final CGrid<CActivity> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumnEntityNamed(CActivity::getActivityType, "Type");
		grid.addColumnEntityNamed(CActivity::getStatus, "Status");
		grid.addDateColumn(CActivity::getStartDate, "Start Time", "meetingDate");
		grid.addDateColumn(CActivity::getDueDate, "End Time", "endDate");
		// grid.addColumnEntityNamed(CActivity::getParentActivity, "Parent");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
	}

	@Override
	protected String getEntityRouteIdField() { // TODO Auto-generated method stub
		return ENTITY_ID_FIELD;
	}

	@Override
	protected void updateDetailsComponent() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		// getBaseDetailsLayout().add(CEntityFormBuilder.buildForm(CActivity.class,
		// getBinder(), null));
		CAccordionDBEntity<CActivity> panel;
		panel = new CPanelActivityDescription(getCurrentEntity(), getBinder(), (CActivityService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelActivityStatusPriority(getCurrentEntity(), getBinder(), (CActivityService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelActivityResourceManagement(getCurrentEntity(), getBinder(), (CActivityService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelActivityTimeTracking(getCurrentEntity(), getBinder(), (CActivityService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelActivityHierarchy(getCurrentEntity(), getBinder(), (CActivityService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelActivityProject(getCurrentEntity(), getBinder(), (CActivityService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelActivityBudgetManagement(getCurrentEntity(), getBinder(), (CActivityService) entityService);
		addAccordionPanel(panel);
		// Add comments panel
		panel = new CPanelActivityComments(getCurrentEntity(), getBinder(), (CActivityService) entityService, commentService, sessionService);
		addAccordionPanel(panel);
	}
}
