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
import tech.derbent.bab.dashboard.service.CSystemProcessCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

/**
 * CComponentSystemProcessList - Component for displaying running system processes from Calimero server.
 * <p>
 * Displays system processes for BAB Gateway projects with real-time data from Calimero HTTP API.
 * Shows process information including:
 * <ul>
 *   <li>Process ID (PID)</li>
 *   <li>Process name</li>
 *   <li>User running the process</li>
 *   <li>CPU usage percentage</li>
 *   <li>Memory usage (MB and percentage)</li>
 *   <li>Process status</li>
 *   <li>Command line</li>
 * </ul>
 * <p>
 * Calimero API: POST /api/request with type="system", operation="processes"
 * <p>
 * Usage:
 * <pre>
 * CComponentSystemProcessList component = new CComponentSystemProcessList(sessionService);
 * layout.add(component);
 * </pre>
 */
public class CComponentSystemProcessList extends CComponentBabBase {
	
	public static final String ID_GRID = "custom-processes-grid";
	public static final String ID_HEADER = "custom-processes-header";
	public static final String ID_REFRESH_BUTTON = "custom-processes-refresh-button";
	public static final String ID_ROOT = "custom-processes-component";
	public static final String ID_TOOLBAR = "custom-processes-toolbar";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentSystemProcessList.class);
	private static final long serialVersionUID = 1L;
	
	private CButton buttonRefresh;
	private CGrid<CSystemProcess> grid;
	private CSystemProcessCalimeroClient processClient;
	private final ISessionService sessionService;
	
	/**
	 * Constructor for process list component.
	 * @param sessionService the session service
	 */
	public CComponentSystemProcessList(final ISessionService sessionService) {
		this.sessionService = sessionService;
		initializeComponents();
	}
	
	private void configureGrid() {
		// PID column
		CGrid.styleColumnHeader(
				grid.addColumn(CSystemProcess::getPid)
						.setWidth("80px")
						.setFlexGrow(0)
						.setKey("pid")
						.setSortable(true)
						.setResizable(true),
				"PID");
		
		// Process name column
		CGrid.styleColumnHeader(
				grid.addColumn(CSystemProcess::getName)
						.setWidth("150px")
						.setFlexGrow(0)
						.setKey("name")
						.setSortable(true)
						.setResizable(true),
				"Process");
		
		// User column
		CGrid.styleColumnHeader(
				grid.addColumn(CSystemProcess::getUser)
						.setWidth("100px")
						.setFlexGrow(0)
						.setKey("user")
						.setSortable(true)
						.setResizable(true),
				"User");
		
		// Status column with color coding
		CGrid.styleColumnHeader(grid.addComponentColumn(process -> {
			final CSpan statusSpan = new CSpan(process.getStatus());
			if (process.isRunning()) {
				statusSpan.getStyle().set("color", "var(--lumo-success-color)");
				statusSpan.getStyle().set("font-weight", "bold");
			} else {
				statusSpan.getStyle().set("color", "var(--lumo-error-color)");
			}
			return statusSpan;
		}).setWidth("100px").setFlexGrow(0).setKey("status").setSortable(true).setResizable(true),
		"Status");
		
		// CPU usage column
		CGrid.styleColumnHeader(
				grid.addColumn(CSystemProcess::getCpuDisplay)
						.setWidth("80px")
						.setFlexGrow(0)
						.setKey("cpu")
						.setSortable(true)
						.setResizable(true),
				"CPU %");
		
		// Memory usage column
		CGrid.styleColumnHeader(
				grid.addColumn(CSystemProcess::getMemoryDisplay)
						.setWidth("150px")
						.setFlexGrow(0)
						.setKey("memory")
						.setSortable(true)
						.setResizable(true),
				"Memory");
		
		// Command column (flexible width)
		CGrid.styleColumnHeader(
				grid.addColumn(CSystemProcess::getCommand)
						.setWidth("300px")
						.setFlexGrow(1)
						.setKey("command")
						.setSortable(true)
						.setResizable(true),
				"Command");
	}
	
	/** Factory method for refresh button. Subclasses can override to customize button. */
	protected CButton create_buttonRefresh() {
		final CButton button = new CButton("Refresh", VaadinIcon.REFRESH.create());
		button.setId(ID_REFRESH_BUTTON);
		button.addThemeVariants(ButtonVariant.LUMO_SMALL);
		button.addClickListener(e -> on_buttonRefresh_clicked());
		return button;
	}
	
	/** Create grid component. */
	private void createGrid() {
		grid = new CGrid<>(CSystemProcess.class);
		grid.setId(ID_GRID);
		configureGrid();
		grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
		grid.setHeight("500px");
		add(grid);
	}
	
	/** Create header component. */
	private void createHeader() {
		final CH3 header = new CH3("System Processes");
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
		loadProcesses();
	}
	
	/** Load system processes from Calimero server. */
	private void loadProcesses() {
		try {
			LOGGER.debug("Loading system processes from Calimero server");
			buttonRefresh.setEnabled(false);
			
			final Optional<CClientProject> clientOptional = resolveClientProject();
			if (clientOptional.isEmpty()) {
				grid.setItems(Collections.emptyList());
				return;
			}
			
			processClient = new CSystemProcessCalimeroClient(clientOptional.get());
			final List<CSystemProcess> processes = processClient.fetchProcesses();
			
			grid.setItems(processes);
			LOGGER.info("Loaded {} system processes", processes.size());
			CNotificationService.showSuccess("Loaded " + processes.size() + " processes");
			
		} catch (final Exception e) {
			LOGGER.error("Failed to load system processes: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load system processes", e);
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
		loadProcesses();
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
