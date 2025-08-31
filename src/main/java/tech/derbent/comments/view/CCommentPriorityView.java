package tech.derbent.comments.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.components.CGridCell;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseProject;
import tech.derbent.comments.domain.CCommentPriority;
import tech.derbent.comments.service.CCommentPriorityService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

/** CCommentPriorityView - View for managing comment priorities. Layer: View (MVC) Provides CRUD operations for comment priorities using the abstract
 * master-detail pattern. Manages different priority levels that comments can have to categorize their importance. */
@Route ("ccommentpriorityview")
@PageTitle ("Comment Priorities")
@Menu (order = 12.1, icon = "class:tech.derbent.comments.view.CCommentPriorityView", title = "Types.Comment Priorities")
@PermitAll
public class CCommentPriorityView extends CGridViewBaseProject<CCommentPriority> {
	private static final long serialVersionUID = 1L;

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return CCommentPriority.getIconColorCode(); // Use the static method from
													// CActivity
	}

	public static String getIconFilename() { return CCommentPriority.getIconFileName(); }

	private final String ENTITY_ID_FIELD = "ccommentpriority_id";

	public CCommentPriorityView(final CCommentPriorityService entityService, final CSessionService sessionService,
			final CScreenService screenService) {
		super(CCommentPriority.class, entityService, sessionService, screenService);
	}

	@Override
	public void createGridForEntity(final CGrid<CCommentPriority> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addEntityColumn(entity -> String.valueOf(entity.getPriorityLevel()), "Priority", "priorityLevel");
		grid.addEntityColumn(entity -> entity.getColor(), "Color", "color");
		grid.addComponentColumn(entity -> {
			final CGridCell defaultCell = new CGridCell();
			defaultCell.setDefaultValue(entity.isDefault());
			return defaultCell;
		}).setHeader("Default").setWidth("80px").setFlexGrow(0);
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }
}
