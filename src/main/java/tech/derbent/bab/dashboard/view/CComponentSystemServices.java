package tech.derbent.bab.dashboard.view;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.service.CSystemServiceCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

/**
 * CComponentSystemServices - Component for displaying systemd services from Calimero server.
 * <p>
 * Displays system services for BAB Gateway projects with real-time data from Calimero HTTP API.
 * Shows service information including:
 * <ul>
 *   <li>Service name</li>
 *   <li>Description</li>
 *   <li>Load state (loaded, not-found, etc.)</li>
 *   <li>Active state (active, inactive, failed)</li>
 *   <li>Sub-state (running, exited, dead, etc.)</li>
 *   <li>Unit file state (enabled, disabled, static)</li>
 * </ul>
 * <p>
 * Calimero API: POST /api/request with type="servicediscovery", operation="list"
 * <p>
 * Usage:
 * <pre>
 * CComponentSystemServices component = new CComponentSystemServices(sessionService);
 * layout.add(component);
 * </pre>
 */
public class CComponentSystemServices extends CComponentBabBase {
	
	public static final String ID_GRID = "custom-services-grid";
	public static final String ID_HEADER = "custom-services-header";
	public static final String ID_REFRESH_BUTTON = "custom-services-refresh-button";
	public static final String ID_ROOT = "custom-services-component";
	public static final String ID_TOOLBAR = "custom-services-toolbar";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentSystemServices.class);
	private static final long serialVersionUID = 1L;
	
	private CButton buttonRefresh;
	private CGrid<CSystemService> grid;
	private CSystemServiceCalimeroClient serviceClient;
	private final ISessionService sessionService;
	
	/**
	 * Constructor for system services component.
	 * @param sessionService the session service
	 */
	public CComponentSystemServices(final ISessionService sessionService) {
		this.sessionService = sessionService;
		initializeComponents();
	}
	
	private void configureGrid() {
		// Service name column
		CGrid.styleColumnHeader(
				grid.addColumn(CSystemService::getName)
						.setWidth("220px")
						.setFlexGrow(0)
						.setKey("name")
						.setSortable(true)
						.setResizable(true),
				"Service");
		
		// Description column (flexible)
		CGrid.styleColumnHeader(
				grid.addColumn(CSystemService::getDescription)
						.setWidth("300px")
						.setFlexGrow(1)
						.setKey("description")
						.setSortable(true)
						.setResizable(true),
				"Description");
		
		// Load state column
		CGrid.styleColumnHeader(grid.addComponentColumn(service -> {
			final CSpan loadSpan = new CSpan(service.getLoadState());
			if (service.isLoaded()) {
				loadSpan.getStyle().set("color", "var(--lumo-success-color)");
			} else {
				loadSpan.getStyle().set("color", "var(--lumo-error-color)");
			}
			return loadSpan;
		}).setWidth("100px").setFlexGrow(0).setKey("loadState").setSortable(true).setResizable(true),
		"Load");
		
		// Active state column with color coding
		CGrid.styleColumnHeader(grid.addComponentColumn(service -> {
			final CSpan activeSpan = new CSpan(service.getActiveState());
			if (service.isActive()) {
				activeSpan.getStyle().set("color", "var(--lumo-success-color)").set("font-weight", "bold");
			} else if (service.isFailed()) {
				activeSpan.getStyle().set("color", "var(--lumo-error-color)").set("font-weight", "bold");
			} else {
				activeSpan.getStyle().set("color", "var(--lumo-contrast-50pct)");
			}
			return activeSpan;
		}).setWidth("100px").setFlexGrow(0).setKey("activeState").setSortable(true).setResizable(true),
		"State");
		
		// Sub-state column
		CGrid.styleColumnHeader(grid.addComponentColumn(service -> {
			final CSpan subSpan = new CSpan(service.getSubState());
			if (service.isRunning()) {
				subSpan.getStyle().set("color", "var(--lumo-success-color)");
			}
			return subSpan;
		}).setWidth("100px").setFlexGrow(0).setKey("subState").setSortable(true).setResizable(true),
		"Sub-State");
		
		// Unit file state column
		CGrid.styleColumnHeader(grid.addComponentColumn(service -> {
			final CSpan unitSpan = new CSpan(service.getUnitFileState());
			if (service.isEnabled()) {
				unitSpan.getStyle().set("color", "var(--lumo-success-color)");
			} else {
				unitSpan.getStyle().set("color", "var(--lumo-contrast-50pct)");
			}
			return unitSpan;
		}).setWidth("100px").setFlexGrow(0).setKey("unitFileState").setSortable(true).setResizable(true),
		"Enabled");
	}
	
	/** Factory method for refresh button. */
	protected CButton create_buttonRefresh() {
		final CButton button = new CButton("Refresh", VaadinIcon.REFRESH.create());
		button.setId(ID_REFRESH_BUTTON);
		button.addThemeVariants(ButtonVariant.LUMO_SMALL);
		button.addClickListener(e -> on_buttonRefresh_clicked());
		return button;
	}
	
	/** Create grid component. */
	private void createGrid() {
		grid = new CGrid<>(CSystemService.class);
		grid.setId(ID_GRID);
		configureGrid();
		grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
		grid.setHeight("500px");
		add(grid);
	}
	
	/** Create header component. */
	private void createHeader() {
		final CH3 header = new CH3("System Services");
		header.setHeight(null);
		header.setId(ID_HEADER);
		header.getStyle().set("margin", "0");
		add(header);
	}
	
	/** Create toolbar with action buttons. */
	private void createToolbar() {
		final CHorizontalLayout layoutToolbar = new CHorizontalLayout();
		layoutToolbar.setId(ID_TOOLBAR);
		layoutToolbar.setSpacing(true);
		layoutToolbar.getStyle().set("gap", "8px");
		
		buttonRefresh = create_buttonRefresh();
		layoutToolbar.add(buttonRefresh);
		
		add(layoutToolbar);
	}
	
	@Override
	protected void initializeComponents() {
		setId(ID_ROOT);
		configureComponent();
		createHeader();
		createToolbar();
		createGrid();
		loadServices();
	}
	
	/** Load system services from Calimero server. */
	private void loadServices() {
		try {
			LOGGER.debug("Loading system services from Calimero server");
			buttonRefresh.setEnabled(false);
			
			final Optional<CClientProject> clientOptional = resolveClientProject();
			if (clientOptional.isEmpty()) {
				grid.setItems(Collections.emptyList());
				return;
			}
			
			serviceClient = new CSystemServiceCalimeroClient(clientOptional.get());
			final List<CSystemService> services = serviceClient.fetchServices();
			
			grid.setItems(services);
			LOGGER.info("Loaded {} system services", services.size());
			CNotificationService.showSuccess("Loaded " + services.size() + " services");
			
		} catch (final Exception e) {
			LOGGER.error("Failed to load system services: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load system services", e);
			grid.setItems(Collections.emptyList());
		} finally {
			buttonRefresh.setEnabled(true);
		}
	}
	
	/** Handle refresh button click. */
	protected void on_buttonRefresh_clicked() {
		LOGGER.debug("Refresh button clicked");
		refreshComponent();
	}
	
	@Override
	protected void refreshComponent() {
		loadServices();
	}
	
	private Optional<CProject_Bab> resolveActiveBabProject() {
		return sessionService.getActiveProject()
				.filter(CProject_Bab.class::isInstance)
				.map(CProject_Bab.class::cast);
	}
	
	private Optional<CClientProject> resolveClientProject() {
		final Optional<CProject_Bab> projectOpt = resolveActiveBabProject();
		if (projectOpt.isEmpty()) {
			return Optional.empty();
		}
		
		final CProject_Bab babProject = projectOpt.get();
		CClientProject httpClient = babProject.getHttpClient();
		
		if (httpClient == null || !httpClient.isConnected()) {
			LOGGER.info("HTTP client not connected - connecting now");
			final var connectionResult = babProject.connectToCalimero();
			if (!connectionResult.isSuccess()) {
				CNotificationService.showError("Calimero connection failed: " + connectionResult.getMessage());
				return Optional.empty();
			}
			httpClient = babProject.getHttpClient();
		}
		
		return Optional.ofNullable(httpClient);
	}
}
