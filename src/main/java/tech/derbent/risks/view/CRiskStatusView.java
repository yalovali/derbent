package tech.derbent.risks.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.components.CGridCell;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.risks.domain.CRiskStatus;
import tech.derbent.risks.service.CRiskStatusService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/**
 * CRiskStatusView - View for managing risk statuses. Layer: View (MVC) Provides CRUD
 * operations for risk statuses using the abstract master-detail pattern. Allows users to
 * create, read, update, and delete risk status definitions.
 */
@Route ("criskstatusview/:risk_status_id?/:action?(edit)")
@PageTitle ("Risk Statuses")
@Menu (
	order = 4.1, icon = "class:tech.derbent.risks.view.CRiskStatusView",
	title = "Types.Risk Statuses"
)
@PermitAll
public class CRiskStatusView extends CProjectAwareMDPage<CRiskStatus>
	implements CInterfaceIconSet {

	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CRiskStatus.getIconColorCode(); // Use the static method from CRiskStatus
	}

	public static String getIconFilename() { return CRiskStatus.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "risk_status_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "criskstatusview/%s/edit";

	/**
	 * Constructor for CRiskStatusView.
	 * @param entityService  the service for risk status operations
	 * @param sessionService
	 */
	public CRiskStatusView(final CRiskStatusService entityService,
		final CSessionService sessionService) {
		super(CRiskStatus.class, entityService, sessionService);
	}

	@Override
	protected void createDetailsLayout() {
		final Div detailsLayout =
			CEntityFormBuilder.buildForm(CRiskStatus.class, getBinder());
		getBaseDetailsLayout().add(detailsLayout);
	}

	@Override
	protected void createGridForEntity() {
		// Use enhanced color-aware status column that shows both color and icon
		grid.addStatusColumn(status -> status, "Status", "status");
		grid.addShortTextColumn(CRiskStatus::getName, "Name", "name");
		grid.addLongTextColumn(CRiskStatus::getDescription, "Description",
			"description");
		// Color column for reference (hex value)
		grid.addShortTextColumn(entity -> entity.getColor(), "Color", "color");
		// Enhanced Type column that shows Final/Active status using CGridCell
		grid.addComponentColumn(entity -> {
			final CGridCell statusCell = new CGridCell();
			statusCell.setFinalActiveValue(entity.getIsFinal());
			return statusCell;
		}).setHeader("Type").setWidth("100px").setFlexGrow(0);
		grid.addShortTextColumn(entity -> String.valueOf(entity.getSortOrder()), "Order",
			"sortOrder");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	public void setProjectForEntity(final CRiskStatus entity,
		final CProject project) {
		assert entity != null : "Entity must not be null";
		assert project != null : "Project must not be null";
		entity.setProject(project);
	}

	@Override
	protected void setupToolbar() {
		// Toolbar setup is handled by the parent class
	}
}