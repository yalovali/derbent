package tech.derbent.screens.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.views.grids.CGrid;
import tech.derbent.api.views.grids.CGridViewBaseProject;
import tech.derbent.screens.domain.CMasterSection;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CMasterSectionService;
import tech.derbent.session.service.ISessionService;

@Route ("cmastersectionview")
@PageTitle ("Master Section Detail")
@Menu (order = 1.5, icon = "class:tech.derbent.screens.view.CMasterSectionView", title = "Setup.UI.MasterSections")
@PermitAll
public class CMasterSectionView extends CGridViewBaseProject<CMasterSection> {

	public static final String DEFAULT_COLOR = "#002a36";
	public static final String DEFAULT_ICON = "vaadin:chart";
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Master Section View";
	private final String ENTITY_ID_FIELD = "master_section_id";

	public CMasterSectionView(final CMasterSectionService entityService, final ISessionService sessionService,
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
