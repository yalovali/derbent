package tech.derbent.users.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.users.domain.CUserType;
import tech.derbent.users.service.CUserTypeService;

/**
 * CUserTypeView - View for managing user types. Layer: View (MVC) Provides CRUD
 * operations for user types using the abstract master-detail pattern.
 */
@Route ("user-types/:user_type_id?/:action?(edit)")
@PageTitle ("User Types")
@Menu (order = 3.3, icon = "vaadin:group", title = "Settings.User Types")
@PermitAll
public class CUserTypeView extends CAbstractMDPage<CUserType> {

	private static final long serialVersionUID = 1L;

	private final String ENTITY_ID_FIELD = "user_type_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "user-types/%s/edit";

	/**
	 * Constructor for CUserTypeView.
	 * @param entityService the service for user type operations
	 */
	public CUserTypeView(final CUserTypeService entityService) {
		super(CUserType.class, entityService);
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
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected CUserType newEntity() {
		return new CUserType();
	}

	@Override
	protected void setupToolbar() {
		// TODO: Implement toolbar setup if needed
	}
}