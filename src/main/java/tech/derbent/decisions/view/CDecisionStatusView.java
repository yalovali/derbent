package tech.derbent.decisions.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.components.CGridCell;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.decisions.service.CDecisionStatusService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

/** CDecisionStatusView - View for managing decision statuses. Layer: View (MVC) Provides CRUD operations for decision statuses using the abstract
 * master-detail pattern. Manages different status types that decisions can have throughout their lifecycle. */
@Route ("cdecisionstatusview/:decision_status_id?/:action?(edit)")
@PageTitle ("Decision Statuses")
@Menu (order = 11.2, icon = "class:tech.derbent.decisions.view.CDecisionStatusView", title = "Types.Decision Statuses")
@PermitAll
public class CDecisionStatusView extends CProjectAwareMDPage<CDecisionStatus> implements CInterfaceIconSet {
	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CDecisionStatus.getIconColorCode(); // Use the static method from
													// CDecisionStatus
	}

	public static String getIconFilename() { return CDecisionStatus.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "decision_status_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "cdecisionstatusview/%s/edit";

	/** Constructor for CDecisionStatusView.
	 * @param entityService  the service for decision status operations
	 * @param sessionService */
	public CDecisionStatusView(final CDecisionStatusService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(CDecisionStatus.class, entityService, sessionService, screenService);
	}

	@Override
	protected void createGridForEntity() {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addStatusColumn(status -> status, "Status", "status");
		grid.addShortTextColumn(entity -> entity.getColor(), "Color", "color");
		grid.addComponentColumn(entity -> {
			final CGridCell statusCell = new CGridCell();
			statusCell.setFinalActiveValue(entity.getIsFinal());
			return statusCell;
		}).setHeader("Type").setWidth("100px").setFlexGrow(0);
		grid.addShortTextColumn(entity -> String.valueOf(entity.getSortOrder()), "Order", "sortOrder");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	public void setProjectForEntity(final CDecisionStatus entity, final CProject project) {
		assert entity != null : "Entity must not be null";
		assert project != null : "Project must not be null";
		entity.setProject(project);
	}

	@Override
	protected void setupToolbar() {
		// Toolbar setup is handled by the parent class
	}
}
