package tech.derbent.risks.view;

import java.lang.reflect.InvocationTargetException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.service.CRiskService;
import tech.derbent.session.service.CSessionService;

/**
 * CRiskView - View for managing project risks. Layer: View (MVC) Provides CRUD operations
 * for risks using the project-aware master-detail pattern.
 */
@PageTitle ("Project Risks")
@Route ("criskview/:risk_id?/:action?(edit)")
@Menu (
	order = 1.3, icon = "class:tech.derbent.risks.view.CRiskView", title = "Project.Risks"
)
@PermitAll
public class CRiskView extends CProjectAwareMDPage<CRisk> implements CInterfaceIconSet {

	private static final long serialVersionUID = 1L;

	private static final String ENTITY_ID_FIELD = "risk_id";

	private static final String ENTITY_ROUTE_TEMPLATE_EDIT = "criskview/%s/edit";

	public static String getIconColorCode() {
		return CRisk.getIconColorCode(); // Use the static method from CRisk
	}

	public static String getIconFilename() { return CRisk.getIconFilename(); }

	public CRiskView(final CRiskService entityService,
		final CSessionService sessionService) {
		super(CRisk.class, entityService, sessionService);
		addClassNames("risk-view");
		// createDetailsLayout();
	}

	@Override
	protected void createDetailsLayout() throws NoSuchMethodException, SecurityException,
		IllegalAccessException, InvocationTargetException {
		final CAccordionDBEntity<CRisk> panel;
		panel = new CPanelRiskBasicInfo(getCurrentEntity(), getBinder(),
			(CRiskService) entityService);
		addAccordionPanel(panel);
	}

	@Override
	protected void createGridForEntity() {
		grid.addShortTextColumn(CRisk::getName, "Name", "name");
		grid.addShortTextColumn(risk -> {
			return risk.getRiskSeverity().name();
		}, "Severity", null);
		grid.addColumn(item -> {
			final String desc = item.getDescription();

			if (desc == null) {
				return "Not set";
			}
			return desc.length() > 50 ? desc.substring(0, 50) + "..." : desc;
		}, "Description", null);
		/***/
		grid.asSingleSelect().addValueChangeListener(event -> {

			if (event.getValue() != null) {
				UI.getCurrent().navigate(
					String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
			}
			else {
				clearForm();
				UI.getCurrent().navigate("risks");
			}
		});
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
