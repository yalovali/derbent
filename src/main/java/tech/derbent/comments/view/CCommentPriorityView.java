package tech.derbent.comments.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.comments.domain.CCommentPriority;
import tech.derbent.comments.service.CCommentPriorityService;

/**
 * CCommentPriorityView - View for managing comment priorities. Layer: View (MVC) 
 * Provides CRUD operations for comment priorities using the abstract master-detail pattern.
 * Manages different priority levels that comments can have to categorize their importance.
 */
@Route("comment-priorities/:ccommentpriority_id?/:action?(edit)")
@PageTitle("Comment Priorities")
@Menu(order = 12.1, icon = "vaadin:exclamation-circle", title = "Types.Comment Priorities")
@PermitAll
public class CCommentPriorityView extends CAbstractMDPage<CCommentPriority> {

    private static final long serialVersionUID = 1L;

    private final String ENTITY_ID_FIELD = "ccommentpriority_id";
    private final String ENTITY_ROUTE_TEMPLATE_EDIT = "comment-priorities/%s/edit";

    /**
     * Constructor for CCommentPriorityView.
     * @param entityService the service for comment priority operations
     */
    public CCommentPriorityView(final CCommentPriorityService entityService) {
        super(CCommentPriority.class, entityService);
        addClassNames("comment-priorities-view");
        LOGGER.info("CCommentPriorityView initialized with route: " 
            + CSpringAuxillaries.getRoutePath(this.getClass()));
    }

    @Override
    protected void createDetailsLayout() {
        final Div detailsLayout = CEntityFormBuilder.buildForm(CCommentPriority.class, getBinder());
        getBaseDetailsLayout().add(detailsLayout);
    }

    @Override
    protected void createGridForEntity() {
        grid.addShortTextColumn(CCommentPriority::getName, "Name", "name");
        grid.addLongTextColumn(CCommentPriority::getDescription, "Description", "description");
        
        grid.addShortTextColumn(entity -> String.valueOf(entity.getPriorityLevel()), 
            "Priority Level", "priorityLevel");
            
        grid.addShortTextColumn(entity -> entity.getColor(), "Color", "color");
        
        grid.addComponentColumn(entity -> {
            final Div colorDiv = new Div();
            colorDiv.getStyle().set("width", "20px");
            colorDiv.getStyle().set("height", "20px");
            colorDiv.getStyle().set("background-color", entity.getColor());
            colorDiv.getStyle().set("border", "1px solid #ccc");
            colorDiv.getStyle().set("border-radius", "3px");
            return colorDiv;
        }).setHeader("Preview").setWidth("80px").setFlexGrow(0);
        
        grid.addComponentColumn(entity -> {
            final Div defaultDiv = new Div();
            defaultDiv.setText(entity.isDefault() ? "Default" : "");
            if (entity.isDefault()) {
                defaultDiv.getStyle().set("padding", "4px 8px");
                defaultDiv.getStyle().set("border-radius", "12px");
                defaultDiv.getStyle().set("background-color", "#e3f2fd");
                defaultDiv.getStyle().set("color", "#1976d2");
                defaultDiv.getStyle().set("font-size", "12px");
                defaultDiv.getStyle().set("font-weight", "bold");
            }
            return defaultDiv;
        }).setHeader("Default").setWidth("80px").setFlexGrow(0);
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
    protected CCommentPriority newEntity() {
        LOGGER.info("newEntity called - creating new CCommentPriority with defaults");
        final CCommentPriority newPriority = new CCommentPriority();
        // Default values are set via MetaData annotations and constructor
        return newPriority;
    }

    @Override
    protected void setupToolbar() {
        // Toolbar setup is handled by the parent class
    }
}