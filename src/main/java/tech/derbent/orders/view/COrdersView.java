package tech.derbent.orders.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.orders.domain.COrder;
import tech.derbent.orders.service.COrderService;
import tech.derbent.orders.service.COrdersViewService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

@Route ("cordersview/:order_id?/:action?(edit)")
@PageTitle ("Orders Master Detail")
@Menu (order = 7.1, icon = "class:tech.derbent.orders.view.COrdersView", title = "Project.Orders")
@PermitAll // When security is enabled, allow all authenticated users
public class COrdersView extends CProjectAwareMDPage<COrder> implements CInterfaceIconSet {
	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return COrder.getIconColorCode(); // Use the static method from COrder
	}

	public static String getIconFilename() { return COrder.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "order_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "cordersview/%s/edit";

	public COrdersView(final COrderService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(COrder.class, entityService, sessionService, screenService);
		addClassNames("orders-view");
	}

	@Override
	protected void createDetailsLayout() {
		buildScreen(COrdersViewService.BASE_VIEW_NAME);
	}

	@Override
	protected void createGridForEntity() {
		// Add grid columns for order display
		grid.addShortTextColumn(COrder::getProjectName, "Project", "project");
		grid.addShortTextColumn(COrder::getName, "Order Name", "name");
		grid.addReferenceColumn(order -> order.getOrderType() != null ? order.getOrderType().getName() : "No Type", "Type");
		grid.addShortTextColumn(COrder::getProviderCompanyName, "Provider", null);
		grid.addReferenceColumn(order -> order.getStatus() != null ? order.getStatus().getName() : "No Status", "Status");
		grid.addShortTextColumn(order -> order.getCurrency() != null ? order.getCurrency().getCurrencyCode() : "No Currency", "Currency", null);
		grid.addColumn(order -> order.getEstimatedCost() != null ? order.getEstimatedCost().toString() : "0.00", "Est. Cost", null);
		grid.addColumn(order -> {
			final String desc = order.getDescription();
			if (desc == null) {
				return "Not set";
			}
			return desc.length() > 50 ? desc.substring(0, 50) + "..." : desc;
		}, "Description", null);
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected void setupToolbar() {}
}
