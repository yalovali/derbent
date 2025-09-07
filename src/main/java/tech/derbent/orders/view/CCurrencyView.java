package tech.derbent.orders.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseProject;
import tech.derbent.orders.domain.CCurrency;
import tech.derbent.orders.service.CCurrencyService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

@Route ("ccurrencyview")
@PageTitle ("Currencies")
@Menu (order = 7.1, icon = "class:tech.derbent.orders.view.CCurrencyView", title = "Types.Currencies")
@PermitAll // When security is enabled, allow all authenticated users
public class CCurrencyView extends CGridViewBaseProject<CCurrency> {

	private static final long serialVersionUID = 1L;

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() { return CCurrency.getIconColorCode(); }

	public static String getIconFilename() { return CCurrency.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "order_id";

	public CCurrencyView(final CCurrencyService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(CCurrency.class, entityService, sessionService, screenService);
	}

	@Override
	public void createGridForEntity(final CGrid<CCurrency> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addShortTextColumn(CCurrency::getCurrencyCode, "Code", "currencyCode");
		grid.addShortTextColumn(CCurrency::getCurrencySymbol, "Symbol", "currencySymbol");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }
}
