package tech.derbent.activities.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.comments.service.CCommentService;
import tech.derbent.comments.view.CPanelActivityComments;
import tech.derbent.session.service.CSessionService;

@Route("cactivitiesview/:activity_id?/:action?(edit)")
@PageTitle("Activity Master Detail")
@Menu(order = 1.1, icon = "class:tech.derbent.activities.view.CActivitiesView", title = "Project.Activities")
@PermitAll // When security is enabled, allow all authenticated users
public final class CActivitiesView extends CProjectAwareMDPage<CActivity> {

    private static final long serialVersionUID = 1L;

    public static String getIconColorCode() {
        return CActivity.getIconColorCode(); // Use the static method from CActivity
    }

    public static String getIconFilename() {
        return CActivity.getIconFilename();
    }

    private final String ENTITY_ID_FIELD = "activity_id";

    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "cactivitiesview/%s/edit";

    private final CCommentService commentService;

    public CActivitiesView(final CActivityService entityService, final CSessionService sessionService,
            final CCommentService commentService) {
        super(CActivity.class, entityService, sessionService);
        this.commentService = commentService;
        addClassNames("activities-view");
    }

    /**
     * Creates the entity details section using CPanelActivityDescription. Follows the same pattern as CUsersView for
     * consistency.
     */
    @Override
    protected void createDetailsLayout() {
        // getBaseDetailsLayout().add(CEntityFormBuilder.buildForm(CActivity.class,
        // getBinder(), null));
        CAccordionDBEntity<CActivity> panel;
        panel = new CPanelActivityDescription(getCurrentEntity(), getBinder(), (CActivityService) entityService);
        addAccordionPanel(panel);
        panel = new CPanelActivityStatusPriority(getCurrentEntity(), getBinder(), (CActivityService) entityService);
        addAccordionPanel(panel);
        panel = new CPanelActivityResourceManagement(getCurrentEntity(), getBinder(), (CActivityService) entityService);
        addAccordionPanel(panel);
        panel = new CPanelActivityTimeTracking(getCurrentEntity(), getBinder(), (CActivityService) entityService);
        addAccordionPanel(panel);
        panel = new CPanelActivityHierarchy(getCurrentEntity(), getBinder(), (CActivityService) entityService);
        addAccordionPanel(panel);
        panel = new CPanelActivityProject(getCurrentEntity(), getBinder(), (CActivityService) entityService);
        addAccordionPanel(panel);
        panel = new CPanelActivityBudgetManagement(getCurrentEntity(), getBinder(), (CActivityService) entityService);
        addAccordionPanel(panel);
        // Add comments panel
        panel = new CPanelActivityComments(getCurrentEntity(), getBinder(), (CActivityService) entityService,
                commentService, sessionService);
        addAccordionPanel(panel);
    }

    @Override
    protected void createGridForEntity() {
        grid.addShortTextColumn(CActivity::getProjectName, "Project", "project");
        grid.addShortTextColumn(CActivity::getName, "Activity Name", "name");
        grid.addReferenceColumn(item -> item.getActivityType() != null ? item.getActivityType().getName() : "No Type",
                "Type");
        grid.addShortTextColumn(item -> item.getStatus() != null ? item.getStatus().getName() : "No Status", "Status",
                null);
        // grid.addShortTextColumn(activity -> activity.getPriority() != null ?
        // activity.getPriority().getName() : "No Priority", "Priority", null);
        grid.addShortTextColumn(item -> item.getStartDate() != null ? item.getStartDate().toString() : "", "Start Date",
                null);
        grid.addShortTextColumn(item -> item.getDueDate() != null ? item.getDueDate().toString() : "", "Due Date",
                null);
        grid.addShortTextColumn(
                item -> item.getParentActivity() != null ? item.getParentActivity().getName() : "No Parent Activity",
                "Parent", null);
        grid.addColumn(item -> {
            final String desc = item.getDescription();

            if (desc == null) {
                return "Not set";
            }
            return desc.length() > 50 ? desc.substring(0, 50) + "..." : desc;
        }, "Description", null);
        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            LOGGER.debug("Grid selection changed: {}", event.getValue());

            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(CActivitiesView.class);
            }
        });
    }

    @Override
    protected String getEntityRouteIdField() { // TODO Auto-generated method stub
        return ENTITY_ID_FIELD;
    }

    @Override
    protected String getEntityRouteTemplateEdit() { // TODO Auto-generated method stub
        return ENTITY_ROUTE_TEMPLATE_EDIT;
    }

    @Override
    protected void setupToolbar() {
        // TODO Auto-generated method stub
    }
}