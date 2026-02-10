package tech.derbent.plm.gannt.ganntviewentity.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CGridViewBaseProject;
import tech.derbent.api.menu.MyMenu;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.views.CDetailsBuilder;
import tech.derbent.plm.gannt.ganntviewentity.domain.CGanntViewEntity;
import tech.derbent.plm.gannt.ganntviewentity.service.CPageServiceGanntViewEntity;
import tech.derbent.plm.gannt.ganntviewentity.service.CGanntViewEntityService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.gannt.ganntviewentity.view.CGanntViewEntityView;


@Profile("derbent")
@Route("cganntviewentityview")
@PageTitle("Gannt Views Master Detail")
@MyMenu(order = "1.5", icon = "class:tech.derbent.plm.gannt.ganntviewentity.view.CGanntViewEntityView", title = "Project.Gannt Entity View", profile = {"derbent"})
@PermitAll
public class CGanntViewEntityView extends CGridViewBaseProject<CGanntViewEntity> {

    public static final String DEFAULT_COLOR = "#4B4382"; // CDE Titlebar Purple - gantt chart view
    public static final String DEFAULT_ICON = "vaadin:chart-timeline";
    private static final Logger LOGGER = LoggerFactory.getLogger(CGanntViewEntityView.class);
    private static final long serialVersionUID = 1L;
    public static final String VIEW_NAME = "Gannt View Entity Settings View";
    private final String ENTITY_ID_FIELD = "screen_id";
    private final CPageServiceGanntViewEntity pageService;

    protected CGanntViewEntityView(final CGanntViewEntityService entityService, final ISessionService sessionService,
            final CDetailSectionService screenService) throws Exception {
        super(CGanntViewEntity.class, entityService, sessionService, screenService);
        pageService = new CPageServiceGanntViewEntity(this);
        LOGGER.debug("Initialized CGanntViewEntityView");
    }

    @Override
    public void createGridForEntity(final CGrid<CGanntViewEntity> grid) {
        LOGGER.debug("Creating grid for CGanntViewEntity");
        grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
        grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
        grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
        grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
    }

    @Override
    public CEntityDB<?> createNewEntityInstance() throws Exception {
        return null;
    }

    @Override
    public CDetailsBuilder getDetailsBuilder() {
        return null;
    }

    @Override
    protected String getEntityRouteIdField() {
        return ENTITY_ID_FIELD;
    }

    @Override
    public CPageService<CGanntViewEntity> getPageService() {
        return pageService;
    }

    @Override
    public ISessionService getSessionService() {
        return sessionService;
    }

    @Override
    public void selectFirstInGrid() {
        /**/}

    @Override
    public void setValue(CEntityDB<?> entity) {
        super.setValue(entity);
    }
}
