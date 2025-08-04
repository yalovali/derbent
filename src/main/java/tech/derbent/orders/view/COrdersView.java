package tech.derbent.orders.view;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.orders.domain.COrder;
import tech.derbent.orders.service.COrderService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/**
 * COrdersView - Vaadin view for managing orders in the system. Layer: View (MVC) Provides a comprehensive user
 * interface for managing company orders including creation, editing, and viewing of orders with project-aware
 * functionality. This view extends CProjectAwareMDPage to inherit standard master-detail functionality with project
 * context awareness, grid display, and automatic form generation using MetaData annotations. Key Features: -
 * Project-aware order management - Master-detail interface with order grid and form - Automatic form generation from
 * COrder MetaData annotations - Standard CRUD operations (Create, Read, Update, Delete) - Integration with order
 * service layer - Consistent UI patterns with other entity views
 */
@Route("orders/:order_id?/:action?(edit)")
@PageTitle("Orders Master Detail")
@Menu(order = 7.1, icon = "class:tech.derbent.orders.view.COrdersView", title = "Project.Orders")
@PermitAll // When security is enabled, allow all authenticated users
public class COrdersView extends CProjectAwareMDPage<COrder> implements CInterfaceIconSet {

    private static final long serialVersionUID = 1L;

    public static String getIconColorCode() {
        return COrder.getIconColorCode(); // Use the static method from COrder
    }

    public static String getIconFilename() { return COrder.getIconFilename(); }

    private final String ENTITY_ID_FIELD = "order_id";

    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "orders/%s/edit";

    /**
     * Constructor for COrdersView.
     * 
     * @param entityService
     *            the COrderService for business logic operations
     * @param sessionService
     *            the SessionService for session management
     */
    public COrdersView(final COrderService entityService, final CSessionService sessionService) {
        super(COrder.class, entityService, sessionService);
        addClassNames("orders-view");
    }

    /**
     * Creates the entity details section for order management. This method uses the automatic form generation
     * capabilities based on
     * 
     * @MetaData annotations from the COrder entity to create appropriate form fields. The form will include all the
     *           order fields such as: - Order Type (ComboBox) - Provider Company Name (TextField) - Requestor
     *           (ComboBox) - Status (ComboBox) - Currency and Cost (ComboBox and NumberField) - Description (TextArea)
     *           - Approval management
     */
    @Override
    protected void createDetailsLayout() {
        final com.vaadin.flow.component.html.Div detailsLayout = tech.derbent.abstracts.annotations.CEntityFormBuilder
                .buildForm(COrder.class, getBinder());
        getBaseDetailsLayout().add(detailsLayout);
    }

    @Override
    protected void createGridForEntity() {
        // Add grid columns for order display
        grid.addShortTextColumn(COrder::getProjectName, "Project", "project");
        grid.addShortTextColumn(COrder::getName, "Order Name", "name");
        grid.addReferenceColumn(order -> order.getOrderType() != null ? order.getOrderType().getName() : "No Type",
                "Type");
        grid.addShortTextColumn(COrder::getProviderCompanyName, "Provider", null);
        grid.addReferenceColumn(order -> order.getStatus() != null ? order.getStatus().getName() : "No Status",
                "Status");
        grid.addShortTextColumn(
                order -> order.getCurrency() != null ? order.getCurrency().getCurrencyCode() : "No Currency",
                "Currency", null);
        grid.addColumn(order -> order.getEstimatedCost() != null ? order.getEstimatedCost().toString() : "0.00",
                "Est. Cost", null);
        grid.addColumn(order -> {
            final String desc = order.getDescription();

            if (desc == null) {
                return "Not set";
            }
            return desc.length() > 50 ? desc.substring(0, 50) + "..." : desc;
        }, "Description", null);
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
    protected void setProjectForEntity(final COrder entity, final CProject project) {
        entity.setProject(project);
    }

    @Override
    protected void setupToolbar() {
        // Standard toolbar setup - can be customized if needed
    }
}