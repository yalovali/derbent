package tech.derbent.activities.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.service.CActivityStatusService;

/**
 * CActivityStatusView - View for managing activity statuses. Layer: View (MVC) Provides
 * CRUD operations for activity statuses using the abstract master-detail pattern. Allows
 * users to create, read, update, and delete activity status definitions.
 */
@Route ("activity-statuses/:activity_status_id?/:action?(edit)")
@PageTitle ("Activity Statuses")
@Menu (order = 2, icon = "vaadin:flag", title = "Types.Activity Statuses")
@PermitAll
public class CActivityStatusView extends CAbstractMDPage<CActivityStatus> {

	private static final long serialVersionUID = 1L;

	private final String ENTITY_ID_FIELD = "activity_status_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "activity-statuses/%s/edit";

	/**
	 * Constructor for CActivityStatusView.
	 * @param entityService the service for activity status operations
	 */
	public CActivityStatusView(final CActivityStatusService entityService) {
		super(CActivityStatus.class, entityService);
		LOGGER.debug("CActivityStatusView constructor called with service: {}",
			entityService.getClass().getSimpleName());
		addClassNames("activity-statuses-view");
		LOGGER.info("CActivityStatusView initialized with route: {}",
			CSpringAuxillaries.getRoutePath(this.getClass()));
	}

	/**
	 * Creates the details layout for editing activity status entities. Uses
	 * CEntityFormBuilder to automatically generate form fields based on MetaData
	 * annotations.
	 */
	@Override
	protected void createDetailsLayout() {
		LOGGER.debug("Creating details layout for CActivityStatusView");

		try {
			final Div detailsLayout =
				CEntityFormBuilder.buildForm(CActivityStatus.class, getBinder());
			// Note: Buttons are now automatically added to the details tab by the parent
			// class
			getBaseDetailsLayout().add(detailsLayout);
			LOGGER.debug("Details layout created successfully for CActivityStatusView");
		} catch (final Exception e) {
			LOGGER.error("Error creating details layout for CActivityStatusView", e);
			throw new RuntimeException(
				"Failed to create details layout for activity status view", e);
		}
	}

	/**
	 * Creates the grid for displaying activity status entities. Sets up columns for name
	 * and description with appropriate headers and sorting.
	 */
	@Override
	protected void createGridForEntity() {
		LOGGER.debug("Creating grid for activity statuses");

		try {
			grid.addColumn(CActivityStatus::getName).setAutoWidth(true)
				.setHeader("Status Name").setKey("name").setSortable(true);
			grid.addColumn(CActivityStatus::getDescription).setAutoWidth(true)
				.setHeader("Description").setKey("description").setSortable(true);
			// Data provider is already set up in the base class
			// CAbstractMDPage.createGridLayout() No need to call grid.setItems() again as
			// it's already configured to handle sorting properly
			LOGGER.debug("Grid created successfully for activity statuses");
		} catch (final Exception e) {
			LOGGER.error("Error creating grid for CActivityStatusView", e);
			throw new RuntimeException("Failed to create grid for activity status view",
				e);
		}
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	/**
	 * Creates a new CActivityStatus entity instance.
	 * @return a new CActivityStatus entity
	 */
	@Override
	protected CActivityStatus newEntity() {
		LOGGER.debug("Creating new CActivityStatus entity");
		return new CActivityStatus();
	}

	@Override
	protected void setupToolbar() {
		LOGGER.debug("Setting up toolbar for CActivityStatusView");
		// TODO: Implement toolbar setup if needed Currently using default toolbar from
		// parent class
	}
}