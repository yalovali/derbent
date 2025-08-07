package tech.derbent.risks.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRiskStatus;
import tech.derbent.risks.service.CRiskStatusService;
import tech.derbent.session.service.CSessionService;

/**
 * CRiskStatusView - View for managing risk statuses. Layer: View (MVC) Provides CRUD
 * operations for risk statuses using the abstract master-detail pattern. Allows users to
 * create, read, update, and delete risk status definitions.
 */
@Route ("criskstatusview/:risk_status_id?/:action?(edit)")
@PageTitle ("Risk Statuses")
@Menu (
	order = 4.1, icon = "class:tech.derbent.risks.view.CRiskStatusView",
	title = "Types.Risk Statuses"
)
@PermitAll
public class CRiskStatusView extends CProjectAwareMDPage<CRiskStatus>
	implements CInterfaceIconSet {

	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CRiskStatus.getIconColorCode(); // Use the static method from CRiskStatus
	}

	public static String getIconFilename() { return CRiskStatus.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "risk_status_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "criskstatusview/%s/edit";

	/**
	 * Constructor for CRiskStatusView.
	 * @param entityService  the service for risk status operations
	 * @param sessionService
	 */
	public CRiskStatusView(final CRiskStatusService entityService,
		final CSessionService sessionService) {
		super(CRiskStatus.class, entityService, sessionService);
		LOGGER.debug("CRiskStatusView constructor called with service: {}",
			entityService.getClass().getSimpleName());
		addClassNames("risk-statuses-view");
		LOGGER.info("CRiskStatusView initialized with route: {}",
			CSpringAuxillaries.getRoutePath(this.getClass()));
	}

	/**
	 * Creates the details layout for editing risk status entities. Uses
	 * CEntityFormBuilder to automatically generate form fields based on MetaData
	 * annotations.
	 */
	@Override
	protected void createDetailsLayout() {
		LOGGER.debug("Creating details layout for CRiskStatusView");

		try {
			final Div detailsLayout =
				CEntityFormBuilder.buildForm(CRiskStatus.class, getBinder());
			// Note: Buttons are now automatically added to the details tab by the parent
			// class
			getBaseDetailsLayout().add(detailsLayout);
			LOGGER.debug("Details layout created successfully for CRiskStatusView");
		} catch (final Exception e) {
			LOGGER.error("Error creating details layout for CRiskStatusView", e);
			throw new RuntimeException(
				"Failed to create details layout for risk status view", e);
		}
	}

	/**
	 * Creates the grid for displaying risk status entities. Sets up columns for name and
	 * description with appropriate headers and sorting.
	 */
	@Override
	protected void createGridForEntity() {
		grid.addShortTextColumn(CRiskStatus::getName, "Status Name", "name");
		grid.addLongTextColumn(CRiskStatus::getDescription, "Description", "description");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected void setProjectForEntity(final CRiskStatus entity, final CProject project) {
		entity.setProject(project);
	}

	@Override
	protected void setupToolbar() {
		LOGGER.debug("Setting up toolbar for CRiskStatusView");
		// TODO: Implement toolbar setup if needed Currently using default toolbar from
		// parent class
	}
}