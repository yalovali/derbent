package tech.derbent.decisions.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseProject;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.service.CDecisionService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.CSessionService;

/** CDecisionsView - Main view for decision management. Layer: View (MVC) Provides a complete decision management interface following the established
 * patterns from CActivitiesView. Includes grid listing, detail editing, and comprehensive panel organization. */
@Route ("cdecisionsview")
@PageTitle ("Decision Master Detail")
@Menu (order = 1.5, icon = "class:tech.derbent.decisions.view.CDecisionsView", title = "Project.Decisions")
@PermitAll // When security is enabled, allow all authenticated users
public class CDecisionsView extends CGridViewBaseProject<CDecision> {

	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Decisions View";

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() {
		return CDecision.getStaticIconColorCode(); // Use the static method from CDecision
	}

	public static String getStaticIconFilename() { return CDecision.getStaticIconFilename(); }

	private final String ENTITY_ID_FIELD = "decision_id";

	public CDecisionsView(final CDecisionService entityService, final CSessionService sessionService, final CDetailSectionService screenService) {
		super(CDecision.class, entityService, sessionService, screenService);
	}

	@Override
	public void createGridForEntity(final CGrid<CDecision> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumnEntityNamed(CDecision::getDecisionType, "Type");
		grid.addColumnEntityNamed(CDecision::getDecisionStatus, "Status");
		grid.addColumnEntityNamed(CDecision::getAccountableUser, "Accountable");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addColumn(decision -> decision.getEstimatedCost() != null ? decision.getEstimatedCost().toString() : "No Cost", "Est. Cost", null);
		grid.addDateTimeColumn(CEntityNamed::getCreatedDate, "Created", null);
	}

	protected CDecisionService getDecisionService() { return (CDecisionService) entityService; }

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected void updateDetailsComponent() throws Exception {
		// final CAccordionDBEntity<CDecision> panel;
		// panel = new CPanelDecisionTeamManagement(getCurrentEntity(), getBinder(), (CDecisionService) entityService);
		// addAccordionPanel(panel);
		buildScreen(CDecisionsView.VIEW_NAME);
	}
}
