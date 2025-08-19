package tech.derbent.activities.view;

import java.lang.reflect.InvocationTargetException;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.abstracts.views.CVerticalLayout;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.session.service.CSessionService;

/**
 * CActivityTypeView - View for managing activity types. Layer: View (MVC) Provides CRUD
 * operations for activity types using the abstract master-detail pattern with project
 * awareness.
 */
@Route ("cactivitytypeview/:activity_type_id?/:action?(edit)")
@PageTitle ("Activity Types")
@Menu (
	order = 10.4, icon = "class:tech.derbent.activities.view.CActivityTypeView",
	title = "Types.Activity Types"
)
@PermitAll
public class CActivityTypeView extends CProjectAwareMDPage<CActivityType>
	implements CInterfaceIconSet {

	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CActivityType.getIconColorCode(); // Use the static method from
													// CActivityType
	}

	public static String getIconFilename() { return CActivityType.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "activity_type_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "cactivitytypeview/%s/edit";

	public CActivityTypeView(final CActivityTypeService entityService,
		final CSessionService sessionService) {
		super(CActivityType.class, entityService, sessionService);
		addClassNames("activity-types-view");
	}

	@Override
	protected void createDetailsLayout() throws NoSuchMethodException, SecurityException,
		IllegalAccessException, InvocationTargetException {
		final CVerticalLayout formLayout =
			CEntityFormBuilder.buildForm(CActivityType.class, getBinder());
		getBaseDetailsLayout().add(formLayout);
	}

	@Override
	protected void createGridForEntity() {
		// Add color-aware type column to show the type with color
		grid.addStatusColumn(type -> type, "Type", "type");
		grid.addShortTextColumn(CActivityType::getName, "Name", "name");
		grid.addLongTextColumn(CActivityType::getDescription, "Description",
			"description");
		grid.addShortTextColumn(CActivityType::getColor, "Color", "color");
		grid.addBooleanColumn(CActivityType::isActive, "Active", "Active", "Inactive");
		grid.addShortTextColumn(CActivityType::getProjectName, "Project", "project");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected void setupToolbar() {
		// TODO: Implement toolbar setup if needed
	}
}