package tech.derbent.risks.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseProject;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.service.CRiskService;
import tech.derbent.risks.service.CRiskViewService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

@PageTitle ("Project Risks")
@Route ("criskview")
@Menu (order = 1.3, icon = "class:tech.derbent.risks.view.CRiskView", title = "Project.Risks")
@PermitAll
public class CRiskView extends CGridViewBaseProject<CRisk> {
	private static final long serialVersionUID = 1L;
	private static final String ENTITY_ID_FIELD = "risk_id";

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return CRisk.getIconColorCode(); // Use the static method from CRisk
	}

	public static String getIconFilename() { return CRisk.getIconFilename(); }

	public CRiskView(final CRiskService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(CRisk.class, entityService, sessionService, screenService);
	}

	@Override
	public void createGridForEntity(final CGrid<CRisk> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addDateTimeColumn(CEntityNamed::getCreatedDate, "Created", null);
		// grid.addColumnEntityNamed(CRisk::getRiskSeverity, "Severity");
		grid.addShortTextColumn(risk -> {
			return risk.getRiskSeverity().name();
		}, "Severity", null);
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected void updateDetailsComponent() throws Exception {
		buildScreen(CRiskViewService.BASE_VIEW_NAME);
	}
}
