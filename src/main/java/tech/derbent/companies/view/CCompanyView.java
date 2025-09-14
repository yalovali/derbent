package tech.derbent.companies.view;

import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseNamed;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.CSessionService;

@Route ("ccompanyview")
@PageTitle ("Company Master Detail")
@Menu (order = 3.4, icon = "class:tech.derbent.companies.view.CCompanyView", title = "Settings.Companies")
@PermitAll // When security is enabled, allow all authenticated users
public class CCompanyView extends CGridViewBaseNamed<CCompany> {

	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Company View";

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() {
		return CCompany.getStaticIconColorCode(); // Use the static method from CCompany
	}

	public static String getStaticIconFilename() { return CCompany.getStaticIconFilename(); }

	private final String ENTITY_ID_FIELD = "company_id";

	/** Constructor for CCompanyView Annotated with @Autowired to let Spring inject dependencies
	 * @param entityService the CCompanyService instance */
	@Autowired
	public CCompanyView(final CCompanyService entityService, final CSessionService sessionService, final CDetailSectionService screenService) {
		super(CCompany.class, entityService, sessionService, screenService);
	}

	@Override
	public void createGridForEntity(final CGrid<CCompany> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addShortTextColumn(CCompany::getAddress, "Address", "address");
		grid.addShortTextColumn(CCompany::getPhone, "Phone", "phone");
		grid.addShortTextColumn(CCompany::getEmail, "Email", "email");
		grid.addBooleanColumn(CCompany::isEnabled, "Status", "Active", "Inactive");
		grid.addShortTextColumn(CCompany::getWebsite, "Website", "website");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected void updateDetailsComponent() throws Exception {
		CAccordionDBEntity<CCompany> panel;
		panel = new CPanelCompanyDescription(getCurrentEntity(), getBinder(), (CCompanyService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelCompanySystemStatus(getCurrentEntity(), getBinder(), (CCompanyService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelCompanyUsers(getCurrentEntity(), getBinder(), (CCompanyService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelCompanyContactDetails(getCurrentEntity(), getBinder(), (CCompanyService) entityService);
		// final var formLayout = CEntityFormBuilder.buildForm(CCompany.class,
		// getBinder()); getBaseDetailsLayout().add(formLayout);
	}
}
