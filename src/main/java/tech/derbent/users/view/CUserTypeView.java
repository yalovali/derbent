package tech.derbent.users.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserTypeService;

/**
 * CUserTypeView - View for managing user types. Layer: View (MVC) Provides CRUD operations for user types using the
 * abstract master-detail pattern with project awareness.
 */
@Route("user-types/:user_type_id?/:action?(edit)")
@PageTitle("User Types")
@Menu(order = 10.3, icon = "vaadin:group", title = "Settings.User Types")
@PermitAll
public class CUserTypeView extends CProjectAwareMDPage<CUserType> {

    private static final long serialVersionUID = 1L;

    private final String ENTITY_ID_FIELD = "user_type_id";

    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "user-types/%s/edit";

    /**
     * Constructor for CUserTypeView.
     * 
     * @param entityService
     *            the service for user type operations
     * @param sessionService
     */
    public CUserTypeView(final CUserTypeService entityService, final CSessionService sessionService) {
        super(CUserType.class, entityService, sessionService);
        addClassNames("user-types-view");
    }

    @Override
    protected void createDetailsLayout() {
        final Div detailsLayout = new Div();
        detailsLayout.setClassName("editor-layout");
        detailsLayout.add(CEntityFormBuilder.buildForm(CUserType.class, getBinder()));
        getBaseDetailsLayout().add(detailsLayout);
    }

    @Override
    protected void createGridForEntity() {
        grid.addShortTextColumn(CUserType::getName, "Name", "name");
        grid.addLongTextColumn(CUserType::getDescription, "Description", "description");
        grid.addShortTextColumn(CUserType::getProjectName, "Project", "project");
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
    protected void setProjectForEntity(final CUserType entity, final CProject project) {
        entity.setProject(project);
    }

    @Override
    protected void setupToolbar() {
        // TODO: Implement toolbar setup if needed
    }
}