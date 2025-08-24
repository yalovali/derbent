package tech.derbent.risks.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.service.CRiskService;
import tech.derbent.risks.service.CRiskViewService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

/** CRiskView - View for managing project risks. Layer: View (MVC) Provides CRUD operations for risks using the project-aware master-detail
 * pattern. */
@PageTitle ("Project Risks")
@Route ("criskview/:risk_id?/:action?(edit)")
@Menu (order = 1.3, icon = "class:tech.derbent.risks.view.CRiskView", title = "Project.Risks")
@PermitAll
public class CRiskView extends CProjectAwareMDPage<CRisk> implements CInterfaceIconSet {
	private static final long serialVersionUID = 1L;
	private static final String ENTITY_ID_FIELD = "risk_id";
	private static final String ENTITY_ROUTE_TEMPLATE_EDIT = "criskview/%s/edit";

	public static String getIconColorCode() {
		return CRisk.getIconColorCode(); // Use the static method from CRisk
	}

	public static String getIconFilename() { return CRisk.getIconFilename(); }

	public CRiskView(final CRiskService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(CRisk.class, entityService, sessionService, screenService);
		addClassNames("risk-view");
		// createDetailsLayout();
	}

	@Override
	protected void createDetailsLayout() throws Exception {
		buildScreen(CRiskViewService.BASE_VIEW_NAME);
	}

	@Override
	protected void createGridForEntity() {
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
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected void setupToolbar() {
		// TODO: Implement toolbar setup if needed
	}
}
