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
import tech.derbent.session.service.CSessionService;

@Route("decision-types/:decisiontype_id?/:action?(edit)")
@PageTitle("Decision Types")
@Menu(order = 11.1, icon = "vaadin:tags", title = "Types.Decision Types")
@PermitAll
public class CDecisionTypeView extends CAbstractMDPage<CDecisionType> {

	private static final long serialVersionUID = 1L;

	private final String ENTITY_ID_FIELD = "decisiontype_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "decision-types/%s/edit";

	/**
	 * Constructor for CDecisionTypeView.
	 * @param entityService the service for decision type operations
	 * @param sessionService
	 */
	public CDecisionTypeView(final CDecisionTypeService entityService, final CSessionService sessionService) {
		super(CDecisionType.class, entityService, sessionService);
		addClassNames("decision-types-view");
		LOGGER.info("CDecisionTypeView initialized with route: "
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
		grid.addLongTextColumn(CDecisionType::getDescription, "Description", "description");
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
			final Div approvalDiv = new Div();
			approvalDiv.setText(entity.isRequiresApproval() ? "Yes" : "No");
			approvalDiv.getStyle().set("padding", "4px 8px");
			approvalDiv.getStyle().set("border-radius", "12px");
			approvalDiv.getStyle().set("font-size", "12px");
			approvalDiv.getStyle().set("font-weight", "bold");
			if (entity.isRequiresApproval()) {
				approvalDiv.getStyle().set("background-color", "#fff3e0");
				approvalDiv.getStyle().set("color", "#ef6c00");
			} else {
				approvalDiv.getStyle().set("background-color", "#e8f5e8");
				approvalDiv.getStyle().set("color", "#2e7d32");
			}
			return approvalDiv;
		}).setHeader("Requires Approval").setWidth("150px").setFlexGrow(0);

		grid.addShortTextColumn(entity -> String.valueOf(entity.getSortOrder()), "Order", "sortOrder");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected CDecisionType newEntity() {
		LOGGER.info("newEntity called - creating new CDecisionType with defaults");
		final CDecisionType newType = new CDecisionType();
		// Default values are set via MetaData annotations and constructor
		return newType;
	}

	@Override
	protected void setupToolbar() {
		// Toolbar setup is handled by the parent class
	}
}