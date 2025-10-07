package tech.derbent.page.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.views.grids.CGrid;
import tech.derbent.api.views.grids.CGridViewBaseProject;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.ISessionService;

@Route ("cpageentityview")
@PageTitle ("Page Master Detail")
@Menu (order = 1.1, icon = "class:tech.derbent.page.view.CPageEntityView", title = "Setup.UI.Pages")
@PermitAll // When security is enabled, allow all authenticated users
public final class CPageEntityView extends CGridViewBaseProject<CPageEntity> {

	public static final String DEFAULT_COLOR = tech.derbent.page.domain.CPageEntity.DEFAULT_COLOR;
	public static final String DEFAULT_ICON = tech.derbent.page.domain.CPageEntity.DEFAULT_ICON;
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Pages View";
	private final String ENTITY_ID_FIELD = "pageentity_id";

	public CPageEntityView(final CPageEntityService entityService, final ISessionService sessionService, final CDetailSectionService screenService) {
		super(CPageEntity.class, entityService, sessionService, screenService);
	}

	@Override
	public void createGridForEntity(final CGrid<CPageEntity> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
	}

	@Override
	protected String getEntityRouteIdField() { // TODO Auto-generated method stub
		return null;
	}
}
