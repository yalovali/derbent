package tech.derbent.orders.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.domains.IIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.orders.domain.COrder;
import tech.derbent.orders.service.COrderService;
import tech.derbent.orders.service.COrdersViewService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

@Route ("cordersview/:order_id?/:action?(edit)")
@PageTitle ("Orders Master Detail")
@Menu (order = 7.1, icon = "class:tech.derbent.orders.view.COrdersView", title = "Project.Orders")
@PermitAll // When security is enabled, allow all authenticated users
public class COrdersView extends CProjectAwareMDPage<COrder> implements IIconSet {
	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return COrder.getIconColorCode(); // Use the static method from COrder
	}

	public static String getIconFilename() { return COrder.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "order_id";

	public COrdersView(final COrderService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(COrder.class, entityService, sessionService, screenService);
	}

	@Override
	protected void createDetailsLayout() {
		buildScreen(COrdersViewService.BASE_VIEW_NAME);
	}

	@Override
	public void createGridForEntity(final CGrid<COrder> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumnEntityNamed(COrder::getOrderType, "Type");
		grid.addColumnEntityNamed(COrder::getStatus, "Status");
		grid.addColumnEntityNamed(COrder::getCurrency, "Currency");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }
}
