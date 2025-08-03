package tech.derbent.activities.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/**
 * CActivityTypeView - View for managing activity types. Layer: View (MVC) Provides CRUD
 * operations for activity types using the abstract master-detail pattern with project
 * awareness.
 */
@Route ("activity-types/:activity_type_id?/:action?(edit)")
@PageTitle ("Activity Types")
@Menu (order = 10.4, icon = "vaadin:tags", title = "Types.Activity Types")
@PermitAll
public class CActivityTypeView extends CProjectAwareMDPage<CActivityType> {

	private static final long serialVersionUID = 1L;

	private final String ENTITY_ID_FIELD = "activity_type_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "activity-types/%s/edit";


	public CActivityTypeView(final CActivityTypeService entityService,
		final CSessionService sessionService) {
		super(CActivityType.class, entityService, sessionService);
		addClassNames("activity-types-view");
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
		grid.addLongTextColumn(CActivityType::getDescription, "Description",
			"description");
		grid.addShortTextColumn(CActivityType::getProjectName, "Project", "project");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected void setProjectForEntity(final CActivityType entity,
		final CProject project) {
		entity.setProject(project);
	}

	@Override
	protected void setupToolbar() {
		// TODO: Implement toolbar setup if needed
	}
}