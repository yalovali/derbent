package tech.derbent.users.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseProject;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserTypeService;

/** CUserTypeView - View for managing user types. Layer: View (MVC) Provides CRUD operations for user types using the abstract master-detail pattern
 * with project awareness. */
@Route ("cusertypeview/:user_type_id?/:action?(edit)")
@PageTitle ("User Types")
@Menu (order = 10.3, icon = "class:tech.derbent.users.view.CUserTypeView", title = "Settings.User Types")
@PermitAll
public class CUserTypeView extends CGridViewBaseProject<CUserType> {
	private static final long serialVersionUID = 1L;

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() { return CUserType.getIconColorCode(); }

	public static String getIconFilename() { return CUserType.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "user_type_id";

	public CUserTypeView(final CUserTypeService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(CUserType.class, entityService, sessionService, screenService);
	}

	@Override
	public void createGridForEntity(final CGrid<CUserType> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addDateTimeColumn(CEntityNamed::getCreatedDate, "Created", null);
		// Add profile picture column first
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected void updateDetailsComponent() throws Exception {
		final Div detailsLayout = new Div();
		detailsLayout.setClassName("editor-layout");
		detailsLayout.add(CEntityFormBuilder.buildForm(CUserType.class, getBinder()));
		getBaseDetailsLayout().add(detailsLayout);
	}
}
