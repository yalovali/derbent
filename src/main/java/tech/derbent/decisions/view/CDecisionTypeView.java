package tech.derbent.decisions.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.decisions.service.CDecisionTypeService;

@Route ("decision-types/:decisiontype_id?/:decision?(edit)")
@PageTitle ("Decision Types")
@Menu (order = 10.4, icon = "vaadin:tags", title = "Types.Decision Types")
@PermitAll
public class CDecisionTypeView extends CAbstractMDPage<CDecisionType> {

	private static final long serialVersionUID = 1L;

	private final String ENTITY_ID_FIELD = "decisiontype_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "decision-types/%s/edit";

	/**
	 * Constructor for CActivityTypeView.
	 * @param entityService the service for activity type operations
	 */
	public CDecisionTypeView(final CDecisionTypeService entityService) {
		super(CDecisionType.class, entityService);
		addClassNames("activity-types-view");
		// createDetailsLayout();
		LOGGER.info("CActivityTypeView initialized with route: "
			+ CSpringAuxillaries.getRoutePath(this.getClass()));
	}

	@Override
	protected void createDetailsLayout() {
		final Div detailsLayout =
			CEntityFormBuilder.buildForm(CDecisionType.class, getBinder());
		getBaseDetailsLayout().add(detailsLayout);
	}

	@Override
	protected void createGridForEntity() {
		grid.addShortTextColumn(CDecisionType::getName, "Name", "name");
		grid.addLongTextColumn(CDecisionType::getDescription, "Description",
			"description");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected CDecisionType newEntity() {
		return new CDecisionType();
	}

	@Override
	protected void setupToolbar() {
		// TODO: Implement toolbar setup if needed
	}
}