package tech.derbent.activities.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.activities.service.CActivityTypeService;

/**
 * CActivityTypeView - View for managing activity types. Layer: View (MVC) Provides CRUD
 * operations for activity types using the abstract master-detail pattern.
 */
@Route ("activity-types/:activity_type_id?/:action?(edit)")
@PageTitle ("Activity Types")
@Menu (order = 2.4, icon = "vaadin:tags", title = "Types.Activity Types")
@PermitAll
public class CActivityTypeView extends CAbstractMDPage<CActivityType> {

	private static final long serialVersionUID = 1L;

	private final String ENTITY_ID_FIELD = "activity_type_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "activity-types/%s/edit";

	/**
	 * Constructor for CActivityTypeView.
	 * @param entityService the service for activity type operations
	 */
	public CActivityTypeView(final CActivityTypeService entityService) {
		super(CActivityType.class, entityService);
		addClassNames("activity-types-view");
		// createDetailsLayout();
		LOGGER.info("CActivityTypeView initialized with route: "
			+ CSpringAuxillaries.getRoutePath(this.getClass()));
	}

	@Override
	protected void createDetailsLayout() {
		final Div detailsLayout =
			CEntityFormBuilder.buildForm(CActivityType.class, getBinder());
		getBaseDetailsLayout().add(detailsLayout);
	}

	@Override
	protected void createGridForEntity() {
		grid.addShortTextColumn(CActivityType::getName, "Name", "name");
		grid.addLongTextColumn(CActivityType::getDescription, "Description", "description");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected CActivityType newEntity() {
		return new CActivityType();
	}

	@Override
	protected void setupToolbar() {
		// TODO: Implement toolbar setup if needed
	}
}