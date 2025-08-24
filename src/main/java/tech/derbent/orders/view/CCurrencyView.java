package tech.derbent.orders.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.orders.domain.CCurrency;
import tech.derbent.orders.service.CCurrencyService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

@Route ("ccurrencyview/:order_id?/:action?(edit)")
@PageTitle ("Orders Master Detail")
@Menu (order = 7.1, icon = "class:tech.derbent.orders.view.CCurrencyView", title = "Types.Currencies")
@PermitAll // When security is enabled, allow all authenticated users
public class CCurrencyView extends CProjectAwareMDPage<CCurrency> implements CInterfaceIconSet {
	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CCurrency.getIconColorCode(); // Use the static method from COrder
	}

	public static String getIconFilename() { return CCurrency.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "order_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "ccurrencyview/%s/edit";

	public CCurrencyView(final CCurrencyService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(CCurrency.class, entityService, sessionService, screenService);
		addClassNames("orders-view");
	}

	@Override
	protected void createGridForEntity() {
		// Add grid columns for order display
		grid.addShortTextColumn(CCurrency::getProjectName, "Project", "project");
		grid.addShortTextColumn(CCurrency::getName, "Currency Name", "name");
		grid.addShortTextColumn(CCurrency::getCurrencyCode, "Code", "currencyCode");
		grid.addShortTextColumn(CCurrency::getCurrencySymbol, "Symbol", "currencySymbol");
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
