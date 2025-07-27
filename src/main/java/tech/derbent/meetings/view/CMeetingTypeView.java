package tech.derbent.meetings.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.meetings.service.CMeetingTypeService;

/**
 * CMeetingTypeView - View for managing meeting types. Layer: View (MVC) Provides CRUD
 * operations for meeting types using the abstract master-detail pattern.
 */
@Route ("meeting-types/:meeting_type_id?/:action?(edit)")
@PageTitle ("Meeting Types")
@Menu (order = 2.2, icon = "vaadin:tags", title = "Types.Meeting Types")
@PermitAll
public class CMeetingTypeView extends CAbstractMDPage<CMeetingType> {

	private static final long serialVersionUID = 1L;

	private final String ENTITY_ID_FIELD = "meeting_type_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "meeting-types/%s/edit";

	/**
	 * Constructor for CMeetingTypeView.
	 * @param entityService the service for meeting type operations
	 */
	public CMeetingTypeView(final CMeetingTypeService entityService) {
		super(CMeetingType.class, entityService);
		addClassNames("meeting-types-view");
		// createDetailsLayout();
		LOGGER.info("CMeetingTypeView initialized with route: "
			+ CSpringAuxillaries.getRoutePath(this.getClass()));
	}

	@Override
	protected void createDetailsLayout() {
		LOGGER.info("Creating details layout for CMeetingTypeView");
		final Div detailsLayout =
			CEntityFormBuilder.buildForm(CMeetingType.class, getBinder());
		// Note: Buttons are now automatically added to the details tab by the parent
		// class
		getBaseDetailsLayout().add(detailsLayout);
	}

	@Override
	protected void createGridForEntity() {
		grid.addShortTextColumn(CMeetingType::getName, "Name", "name");
		grid.addLongTextColumn(CMeetingType::getDescription, "Description", "description");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected CMeetingType newEntity() {
		return new CMeetingType();
	}

	@Override
	protected void setupToolbar() {
		// TODO: Implement toolbar setup if needed
	}
}