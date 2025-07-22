package tech.derbent.users.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserTypeService;

/**
 * CUserTypeView - View for managing user types. Layer: View (MVC) Provides CRUD operations for user types using the
 * abstract master-detail pattern.
 */
@Route("user-types/:user_type_id?/:action?(edit)")
@PageTitle("User Types")
@Menu(order = 1, icon = "vaadin:group", title = "Settings.User Types")
@PermitAll
public class CUserTypeView extends CAbstractMDPage<CUserType> {

    private static final long serialVersionUID = 1L;
    private final String ENTITY_ID_FIELD = "user_type_id";
    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "user-types/%s/edit";

    /**
     * Constructor for CUserTypeView.
     * 
     * @param entityService
     *            the service for user type operations
     */
    public CUserTypeView(final CUserTypeService entityService) {
        super(CUserType.class, entityService);
        addClassNames("user-types-view");
        // createDetailsLayout();
        LOGGER.info("CUserTypeView initialized");
    }

    @Override
    protected void createDetailsLayout() {
        LOGGER.info("Creating details layout for CUserTypeView");
        final Div detailsLayout = new Div();
        detailsLayout.setClassName("editor-layout");
        detailsLayout.add(CEntityFormBuilder.buildForm(CUserType.class, getBinder()));
        // Note: Buttons are now automatically added to the details tab by the parent class
        getBaseDetailsLayout().add(detailsLayout);
    }

    @Override
    protected void createGridForEntity() {
        LOGGER.info("Creating grid for user types");
        grid.addColumn(CUserType::getName).setAutoWidth(true).setHeader("Name").setSortable(true);
        grid.addColumn(CUserType::getDescription).setAutoWidth(true).setHeader("Description").setSortable(true);
        grid.setItems(query -> entityService.list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
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
    protected void initPage() {
        // Initialize page components if needed
    }

    @Override
    protected CUserType newEntity() {
        return new CUserType();
    }

    @Override
    protected void setupToolbar() {
        // TODO: Implement toolbar setup if needed
    }
}