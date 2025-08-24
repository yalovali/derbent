package tech.derbent.meetings.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.domains.IIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

@Route ("cmeetingtypeview/:meetingtype_id?/:action?(edit)")
@PageTitle ("Meeting Types")
@Menu (order = 10.2, icon = "class:tech.derbent.meetings.view.CMeetingTypeView", title = "Types.Meeting Types")
@PermitAll
public class CMeetingTypeView extends CProjectAwareMDPage<CMeetingType> implements IIconSet {
	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() { return CMeetingType.getIconColorCode(); }

	public static String getIconFilename() { return CMeetingType.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "meetingtype_id";

	public CMeetingTypeView(final CMeetingTypeService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(CMeetingType.class, entityService, sessionService, screenService);
	}

	@Override
	protected void createGridForEntity() {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addDateTimeColumn(CEntityNamed::getCreatedDate, "Created", null);
		grid.addStatusColumn(type -> type, "Type", "type");
		grid.addShortTextColumn(CMeetingType::getColor, "Color", "color");
		grid.addBooleanColumn(CMeetingType::isActive, "Active", "Active", "Inactive");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }
}
