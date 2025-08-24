package tech.derbent.decisions.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.IIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.decisions.service.CDecisionTypeService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

@Route ("cdecisiontypeview/:decisiontype_id?/:action?(edit)")
@PageTitle ("Decision Types")
@Menu (order = 11.1, icon = "class:tech.derbent.decisions.view.CDecisionTypeView", title = "Types.Decision Types")
@PermitAll
public class CDecisionTypeView extends CProjectAwareMDPage<CDecisionType> implements IIconSet {
	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() { return CDecisionType.getIconColorCode(); }

	public static String getIconFilename() { return CDecisionType.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "decisiontype_id";

	public CDecisionTypeView(final CDecisionTypeService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(CDecisionType.class, entityService, sessionService, screenService);
	}

	@Override
	protected void createGridForEntity() {
		grid.addStatusColumn(type -> type, "Type", "type");
		grid.addShortTextColumn(CDecisionType::getName, "Name", "name");
		grid.addLongTextColumn(CDecisionType::getDescription, "Description", "description");
		grid.addShortTextColumn(CDecisionType::getColor, "Color", "color");
		grid.addBooleanColumn(CDecisionType::isActive, "Active", "Active", "Inactive");
		grid.addBooleanColumn(CDecisionType::requiresApproval, "Requires Approval", "Required", "Optional");
		grid.addShortTextColumn(CDecisionType::getProjectName, "Project", "project");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }
}
