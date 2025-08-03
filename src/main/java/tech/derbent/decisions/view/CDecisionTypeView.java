package tech.derbent.decisions.view;

import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.decisions.service.CDecisionTypeService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

@Route("decision-types/:decisiontype_id?/:action?(edit)")
@PageTitle("Decision Types")
@Menu(order = 11.1, icon = "vaadin:tags", title = "Types.Decision Types")
@PermitAll
public class CDecisionTypeView extends CProjectAwareMDPage<CDecisionType> {

    private static final long serialVersionUID = 1L;

    private final String ENTITY_ID_FIELD = "decisiontype_id";

    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "decision-types/%s/edit";

    /**
     * Constructor for CDecisionTypeView.
     * 
     * @param entityService
     *            the service for decision type operations
     * @param sessionService
     */
    public CDecisionTypeView(final CDecisionTypeService entityService, final CSessionService sessionService) {
        super(CDecisionType.class, entityService, sessionService);
        addClassNames("decision-types-view");
        LOGGER.info("CDecisionTypeView initialized with route: " + CSpringAuxillaries.getRoutePath(this.getClass()));
    }

    @Override
    protected void createDetailsLayout() {
        // Now we can include all fields with @MetaData annotation including the boolean
        // fields
        final Div detailsLayout = CEntityFormBuilder.buildForm(CDecisionType.class, getBinder(),
                List.of("name", "description", "color"));
        getBaseDetailsLayout().add(detailsLayout);
        /*
         * final Div detailsLayout = CEntityFormBuilder.buildForm(CActivityType.class, getBinder());
         * getBaseDetailsLayout().add(detailsLayout);
         */
    }

    @Override
    protected void createGridForEntity() {
        grid.addShortTextColumn(CDecisionType::getName, "Name", "name");
        grid.addLongTextColumn(CDecisionType::getDescription, "Description", "description");
        grid.addShortTextColumn(CDecisionType::getProjectName, "Project", "project");
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
    protected void setProjectForEntity(final CDecisionType entity, final CProject project) {
        entity.setProject(project);
    }

    @Override
    protected void setupToolbar() {
        // Toolbar setup is handled by the parent class
    }
}