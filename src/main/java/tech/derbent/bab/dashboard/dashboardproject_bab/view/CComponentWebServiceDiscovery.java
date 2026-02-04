package tech.derbent.bab.dashboard.dashboardproject_bab.view;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTOWebServiceEndpoint;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CAbstractCalimeroClient;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CWebServiceDiscoveryCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

/**
 * CComponentWebServiceDiscovery - Component for displaying Calimero webservice API metadata.
 * <p>
 * Displays available HTTP API endpoints from Calimero server with their operations and parameters.
 * Useful for API introspection and development.
 * <p>
 * Shows:
 * <ul>
 * <li>API type (e.g., "systemservices", "network", "iot")</li>
 * <li>Operation/action (e.g., "list", "status", "start")</li>
 * <li>Description</li>
 * <li>Parameters (with required/optional indication)</li>
 * <li>Endpoint URL</li>
 * </ul>
 * <p>
 * Calimero API: POST /api/request with type="webservice", operation="list"
 * <p>
 * Usage:
 *
 * <pre>
 * CComponentWebServiceDiscovery component = new CComponentWebServiceDiscovery(sessionService);
 * layout.add(component);
 * </pre>
 */
public class CComponentWebServiceDiscovery extends CComponentBabBase {
	public static final String ID_GRID = "custom-webservices-grid";
	public static final String ID_HEADER = "custom-webservices-header";
	public static final String ID_REFRESH_BUTTON = "custom-webservices-refresh-button";
	public static final String ID_ROOT = "custom-webservices-component";
	public static final String ID_TOOLBAR = "custom-webservices-toolbar";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentWebServiceDiscovery.class);
	private static final long serialVersionUID = 1L;
	// buttonRefresh inherited from CComponentBabBase
	private CGrid<CDTOWebServiceEndpoint> grid;
	private CWebServiceDiscoveryCalimeroClient webserviceClient;

	/**
	 * Constructor for webservice discovery component.
	 * @param sessionService the session service
	 */
	public CComponentWebServiceDiscovery(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	private void configureGrid() {
		// Type column
		CGrid.styleColumnHeader(
				grid.addColumn(CDTOWebServiceEndpoint::getType).setWidth("150px").setFlexGrow(0).setKey("type").setSortable(true)
						.setResizable(true),
				"Type");
		// Action column
		CGrid.styleColumnHeader(
				grid.addColumn(CDTOWebServiceEndpoint::getAction).setWidth("120px").setFlexGrow(0).setKey("action").setSortable(true)
						.setResizable(true),
				"Action");
		// Description column (flexible)
		CGrid.styleColumnHeader(grid.addColumn(CDTOWebServiceEndpoint::getDescription).setWidth("300px").setFlexGrow(1).setKey("description")
				.setSortable(true).setResizable(true), "Description");
		// Parameters column
		CGrid.styleColumnHeader(grid.addComponentColumn(endpoint -> {
			final CSpan paramSpan = new CSpan(endpoint.getParameterList());
			if (endpoint.hasRequiredParameters()) {
				paramSpan.getStyle().set("color", "var(--lumo-error-color)").set("font-weight", "bold");
			} else {
				paramSpan.getStyle().set("color", "var(--lumo-contrast-70pct)");
			}
			return paramSpan;
		}).setWidth("200px").setFlexGrow(0).setKey("parameters").setSortable(true).setResizable(true), "Parameters");
		// Endpoint column
		CGrid.styleColumnHeader(
				grid.addColumn(CDTOWebServiceEndpoint::getEndpoint).setWidth("150px").setFlexGrow(0).setKey("endpoint").setSortable(true)
						.setResizable(true),
				"Endpoint");
	}

	@Override
	protected CAbstractCalimeroClient createCalimeroClient(final CClientProject clientProject) {
		return new CWebServiceDiscoveryCalimeroClient(clientProject);
	}

	/** Create grid component. */
	private void createGrid() {
		grid = new CGrid<>(CDTOWebServiceEndpoint.class);
		grid.setId(ID_GRID);
		configureGrid();
		grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
		grid.setHeight("500px");
		add(grid);
	}

	@Override
	protected String getHeaderText() {
		return "Webservice API Discovery";
	}

	@Override
	protected ISessionService getSessionService() {
		return sessionService;
	}

	@Override
	protected void initializeComponents() {
		setId(ID_ROOT);
		configureComponent();
		add(createHeader());
		add(createStandardToolbar());
		createGrid();
		loadEndpoints();
	}

	/** Load webservice endpoints from Calimero server. */
	private void loadEndpoints() {
		try {
			LOGGER.debug("Loading webservice endpoints from Calimero server");
			buttonRefresh.setEnabled(false);
			final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
			if (clientOpt.isEmpty()) {
				showCalimeroUnavailableWarning("Calimero service not available");
				grid.setItems(Collections.emptyList());
				return;
			}
			hideCalimeroUnavailableWarning();
			webserviceClient = (CWebServiceDiscoveryCalimeroClient) clientOpt.get();
			final List<CDTOWebServiceEndpoint> endpoints = webserviceClient.fetchEndpoints();
			grid.setItems(endpoints);
			LOGGER.info("Loaded {} webservice endpoints", endpoints.size());
			
			// Update summary with endpoint count
			updateSummary(String.format("%d API endpoints available", endpoints.size()));
			
			CNotificationService.showSuccess("Loaded " + endpoints.size() + " API endpoints");
		} catch (final Exception e) {
			LOGGER.error("Failed to load webservice endpoints: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load webservice endpoints", e);
			showCalimeroUnavailableWarning("Failed to load webservice endpoints");
			grid.setItems(Collections.emptyList());
		} finally {
			buttonRefresh.setEnabled(true);
		}
	}

	@Override
	protected void refreshComponent() {
		loadEndpoints();
	}
}
