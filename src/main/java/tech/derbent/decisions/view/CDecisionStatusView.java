package tech.derbent.decisions.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.components.CGridCell;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CAbstractNamedEntityPage;
import tech.derbent.decisions.domain.CDecisionStatus;
import tech.derbent.decisions.service.CDecisionStatusService;
import tech.derbent.session.service.CSessionService;

/**
 * CDecisionStatusView - View for managing decision statuses. Layer: View (MVC) Provides
 * CRUD operations for decision statuses using the abstract master-detail pattern. Manages
 * different status types that decisions can have throughout their lifecycle.
 */
@Route ("cdecisionstatusview/:decision_status_id?/:action?(edit)")
@PageTitle ("Decision Statuses")
@Menu (
	order = 11.2, icon = "class:tech.derbent.decisions.view.CDecisionStatusView",
	title = "Types.Decision Statuses"
)
@PermitAll
public class CDecisionStatusView extends CAbstractNamedEntityPage<CDecisionStatus>
	implements CInterfaceIconSet {

	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CDecisionStatus.getIconColorCode(); // Use the static method from
													// CDecisionStatus
	}

	public static String getIconFilename() { return CDecisionStatus.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "decision_status_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "cdecisionstatusview/%s/edit";

	/**
	 * Constructor for CDecisionStatusView.
	 * @param entityService  the service for decision status operations
	 * @param sessionService
	 */
	public CDecisionStatusView(final CDecisionStatusService entityService,
		final CSessionService sessionService) {
		super(CDecisionStatus.class, entityService, sessionService);
	}

	@Override
	protected void createDetailsLayout() {
		final Div detailsLayout =
			CEntityFormBuilder.buildForm(CDecisionStatus.class, getBinder());
		getBaseDetailsLayout().add(detailsLayout);
	}

	@Override
	protected void createGridForEntity() {
		// Use enhanced color-aware status column that shows both color and icon
		grid.addStatusColumn(status -> status, "Status", "status");
		grid.addShortTextColumn(CDecisionStatus::getName, "Name", "name");
		grid.addLongTextColumn(CDecisionStatus::getDescription, "Description",
			"description");
		// Color column for reference (hex value)
		grid.addShortTextColumn(entity -> entity.getColor(), "Color", "color");
		// Enhanced Type column that shows Final/Active status using CGridCell
		grid.addComponentColumn(entity -> {
			final CGridCell statusCell = new CGridCell();
			statusCell.setFinalActiveValue(entity.isFinal());
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
	protected void setupToolbar() {
		// Toolbar setup is handled by the parent class
	}
}