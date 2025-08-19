package tech.derbent.meetings.view;

import java.lang.reflect.InvocationTargetException;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.abstracts.views.CVerticalLayout;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.session.service.CSessionService;

/**
 * CMeetingTypeView - View for managing meeting types. Layer: View (MVC) Provides CRUD operations for meeting types
 * using the abstract master-detail pattern with project awareness.
 */
@Route("cmeetingtypeview/:meetingtype_id?/:action?(edit)")
@PageTitle("Meeting Types")
@Menu(order = 10.2, icon = "class:tech.derbent.meetings.view.CMeetingTypeView", title = "Types.Meeting Types")
@PermitAll
public class CMeetingTypeView extends CProjectAwareMDPage<CMeetingType> implements CInterfaceIconSet {

    private static final long serialVersionUID = 1L;

    public static String getIconColorCode() {
        return CMeetingType.getIconColorCode(); // Use the static method from CMeetingType
    }

    public static String getIconFilename() {
        return CMeetingType.getIconFilename();
    }

    private final String ENTITY_ID_FIELD = "meetingtype_id";

    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "cmeetingtypeview/%s/edit";

    /**
     * Constructor for CMeetingTypeView.
     * 
     * @param entityService
     *            the service for meeting type operations
     * @param sessionService
     */
    public CMeetingTypeView(final CMeetingTypeService entityService, final CSessionService sessionService) {
        super(CMeetingType.class, entityService, sessionService);
        addClassNames("meeting-types-view");
        // createDetailsLayout();
        LOGGER.info("CMeetingTypeView initialized with route: " + CSpringAuxillaries.getRoutePath(this.getClass()));
    }

    @Override
    protected void createDetailsLayout()
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        LOGGER.info("Creating details layout for CMeetingTypeView");
        final CVerticalLayout formLayout = CEntityFormBuilder.buildForm(CMeetingType.class, getBinder());
        // Note: Buttons are now automatically added to the details tab by the parent
        // class
        getBaseDetailsLayout().add(formLayout);
    }

    @Override
    protected void createGridForEntity() {
        // Add color-aware type column to show the type with color
        grid.addStatusColumn(type -> type, "Type", "type");
        grid.addShortTextColumn(CMeetingType::getName, "Name", "name");
        grid.addLongTextColumn(CMeetingType::getDescription, "Description", "description");
        grid.addShortTextColumn(CMeetingType::getColor, "Color", "color");
        grid.addBooleanColumn(CMeetingType::isActive, "Active", "Active", "Inactive");
        grid.addShortTextColumn(CMeetingType::getProjectName, "Project", "project");
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