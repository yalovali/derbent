package tech.derbent.screens.view;

import java.lang.reflect.InvocationTargetException;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CScreenLinesService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.screens.service.CViewsService;
import tech.derbent.session.service.CSessionService;

@Route("cscreensview/:screen_id?/:action?(edit)")
@PageTitle("Screen Master Detail")
@Menu(order = 1.5, icon = "class:tech.derbent.screens.view.CScreenView", title = "Project.Screens")
@PermitAll // When security is enabled, allow all authenticated users
public final class CScreenView extends CProjectAwareMDPage<CScreen> {

    private static final long serialVersionUID = 1L;

    public static String getIconColorCode() {
        return CScreen.getIconColorCode(); // Use the static method from CScreen
    }

    public static String getIconFilename() {
        return CScreen.getIconFilename();
    }

    private final String ENTITY_ID_FIELD = "screen_id";

    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "cscreensview/%s/edit";

    private final CScreenLinesService screenLinesService;

    private final CEntityFieldService entityFieldService;

    private final CViewsService viewsService;

    public CScreenView(final CScreenService entityService, final CSessionService sessionService,
            final CScreenLinesService screenLinesService, final CEntityFieldService entityFieldService,
            final CViewsService viewsService) {
        super(CScreen.class, entityService, sessionService);
        this.screenLinesService = screenLinesService;
        this.entityFieldService = entityFieldService;
        this.viewsService = viewsService;
        addClassNames("screens-view");
    }

    /**
     * Creates the entity details section using accordion panels. Follows the same pattern as CActivitiesView for
     * consistency.
     * 
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Override
    protected void createDetailsLayout()
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        addAccordionPanel(new CPanelScreenBasicInfo(getCurrentEntity(), getBinder(), (CScreenService) entityService));
        addAccordionPanel(new CPanelScreenLines(getCurrentEntity(), getBinder(), (CScreenService) entityService,
                screenLinesService, entityFieldService, viewsService));
        addAccordionPanel(
                new CPanelScreenRelatedEntities(getCurrentEntity(), getBinder(), (CScreenService) entityService));
    }

    @Override
    protected void createGridForEntity() {
        grid.addShortTextColumn(CScreen::getProjectName, "Project", "project");
        grid.addShortTextColumn(CScreen::getName, "Screen Name", "name");
        grid.addShortTextColumn(CScreen::getEntityType, "Entity Type", "entityType");
        grid.addShortTextColumn(CScreen::getScreenTitle, "Screen Title", "screenTitle");
        // Show related entity information
        grid.addColumn(screen -> {

            if (screen.getRelatedActivity() != null) {
                return "Activity: " + screen.getRelatedActivity().getName();
            } else if (screen.getRelatedMeeting() != null) {
                return "Meeting: " + screen.getRelatedMeeting().getName();
            } else if (screen.getRelatedRisk() != null) {
                return "Risk: " + screen.getRelatedRisk().getName();
            }
            return "No Related Entity";
        }, "Related Entity", null);
        // Show active status
        grid.addColumn(screen -> screen.getIsActive() ? "Active" : "Inactive", "Status", null);
        // Show number of screen lines
        grid.addColumn(screen -> {

            try {
                return String.valueOf(screenLinesService.countByScreen(screen));
            } catch (final Exception e) {
                return "0";
            }
        }, "Lines Count", null);
        grid.addColumn(screen -> {
            final String desc = screen.getDescription();

            if (desc == null) {
                return "Not set";
            }
            return desc.length() > 50 ? desc.substring(0, 50) + "..." : desc;
        }, "Description", null);
        // Selection handling is now managed by the base class - no custom logic needed
        // This follows the coding guidelines for child class simplicity
    }

    @Override
    protected String getEntityRouteIdField() {
        return ENTITY_ID_FIELD;
    }

    @Override
    protected String getEntityRouteTemplateEdit() {
        return ENTITY_ROUTE_TEMPLATE_EDIT;
    }

    @Override
    protected void setupToolbar() {
        // TODO: Implement toolbar setup if needed
    }
}