package tech.derbent.activities.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.service.CActivityStatusService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/**
 * CActivityStatusView - View for managing activity statuses. Layer: View (MVC) Provides CRUD operations for activity
 * statuses using the abstract master-detail pattern. Allows users to create, read, update, and delete activity status
 * definitions.
 */
@Route("activity-statuses/:activity_status_id?/:action?(edit)")
@PageTitle("Activity Statuses")
@Menu(order = 2.1, icon = "vaadin:flag", title = "Types.Activity Statuses")
@PermitAll
public class CActivityStatusView extends CProjectAwareMDPage<CActivityStatus> {

    private static final long serialVersionUID = 1L;

    private final String ENTITY_ID_FIELD = "activity_status_id";

    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "activity-statuses/%s/edit";

    /**
     * Constructor for CActivityStatusView.
     * 
     * @param entityService
     *            the service for activity status operations
     * @param sessionService
     */
    public CActivityStatusView(final CActivityStatusService entityService, final CSessionService sessionService) {
        super(CActivityStatus.class, entityService, sessionService);
        LOGGER.debug("CActivityStatusView constructor called with service: {}",
                entityService.getClass().getSimpleName());
        addClassNames("activity-statuses-view");
        LOGGER.info("CActivityStatusView initialized with route: {}", CSpringAuxillaries.getRoutePath(this.getClass()));
    }

    /**
     * Creates the details layout for editing activity status entities. Uses CEntityFormBuilder to automatically
     * generate form fields based on MetaData annotations.
     */
    @Override
    protected void createDetailsLayout() {
        LOGGER.debug("Creating details layout for CActivityStatusView");

        try {
            final Div detailsLayout = CEntityFormBuilder.buildForm(CActivityStatus.class, getBinder());
            // Note: Buttons are now automatically added to the details tab by the parent
            // class
            getBaseDetailsLayout().add(detailsLayout);
            LOGGER.debug("Details layout created successfully for CActivityStatusView");
        } catch (final Exception e) {
            LOGGER.error("Error creating details layout for CActivityStatusView", e);
            throw new RuntimeException("Failed to create details layout for activity status view", e);
        }
    }

    /**
     * Creates the grid for displaying activity status entities. Sets up columns for name and description with
     * appropriate headers and sorting. Also includes a color-aware column to show the status colors.
     */
    @Override
    protected void createGridForEntity() {
        grid.addStatusColumn(status -> status, "Status", "status");
        grid.addShortTextColumn(CActivityStatus::getName, "Status Name", "name");
        grid.addLongTextColumn(CActivityStatus::getDescription, "Description", "description");
        grid.addShortTextColumn(CActivityStatus::getColor, "Color Code", "color");
        grid.addBooleanColumn(CActivityStatus::isFinal, "Is Final", "Final", "Not Final");
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
    protected void setProjectForEntity(final CActivityStatus entity, final CProject project) {
        entity.setProject(project);
    }

    @Override
    protected void setupToolbar() {
        LOGGER.debug("Setting up toolbar for CActivityStatusView");
        // TODO: Implement toolbar setup if needed Currently using default toolbar from
        // parent class
    }
}