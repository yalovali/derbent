package tech.derbent.screens.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CMasterSection;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CMasterSectionService;
import tech.derbent.session.service.CSessionService;

@Route ("cmastersectionview")
@PageTitle ("Master Section Detail")
@Menu (order = 1.5, icon = "class:tech.derbent.screens.view.CMasterSectionView", title = "Setup.UI.MasterSections")
@PermitAll
public class CMasterSectionView extends CGridViewBaseProject<CMasterSection> {

	private static final long serialVersionUID = 1L;

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() {
		return CDetailSection.getStaticIconColorCode(); // Use the static method from CScreen
	}

	public static String getStaticIconFilename() { return CDetailSection.getStaticIconFilename(); }

	private final String ENTITY_ID_FIELD = "master_section_id";

	public CMasterSectionView(final CMasterSectionService entityService, final CSessionService sessionService,
			final CDetailSectionService screenService) {
		super(CMasterSection.class, entityService, sessionService, screenService);
	}

	@Override
	public void createGridForEntity(final CGrid<CMasterSection> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addDateTimeColumn(CEntityNamed::getCreatedDate, "Created", null);
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }
}
