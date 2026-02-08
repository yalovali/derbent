package tech.derbent.bab.dashboard.dashboardproject_bab.view;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTOSystemProcess;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CAbstractCalimeroClient;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CSystemProcessCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

/** CComponentSystemProcessList - Component for displaying running system processes from Calimero server.
 * <p>
 * Displays system processes for BAB Gateway projects with real-time data from Calimero HTTP API. Shows process information including:
 * <ul>
 * <li>Process ID (PID)</li>
 * <li>Process name</li>
 * <li>User running the process</li>
 * <li>CPU usage percentage</li>
 * <li>Memory usage (MB and percentage)</li>
 * <li>Process status</li>
 * <li>Command line</li>
 * </ul>
 * <p>
 * Calimero API: POST /api/request with type="system", operation="processes"
 * <p>
 * Usage:
 *
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
	// buttonRefresh inherited from CComponentBabBase
	private CGrid<CDTOSystemProcess> grid;
	private CSystemProcessCalimeroClient processClient;

	/** Constructor for process list component.
	 * @param sessionService the session service */
	public CComponentSystemProcessList(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	@Override
	protected void configureComponent() {
		super.configureComponent();
		createGrid();
	}

	private void configureGrid() {
		// PID column
		CGrid.styleColumnHeader(
				grid.addColumn(CDTOSystemProcess::getPid).setWidth("80px").setFlexGrow(0).setKey("pid").setSortable(true).setResizable(true), "PID");
		// Process name column
		CGrid.styleColumnHeader(
				grid.addColumn(CDTOSystemProcess::getName).setWidth("150px").setFlexGrow(0).setKey("name").setSortable(true).setResizable(true),
				"Process");
		// User column
		CGrid.styleColumnHeader(
				grid.addColumn(CDTOSystemProcess::getUser).setWidth("100px").setFlexGrow(0).setKey("user").setSortable(true).setResizable(true),
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
		}).setWidth("100px").setFlexGrow(0).setKey("status").setSortable(true).setResizable(true), "Status");
		// CPU usage column
		CGrid.styleColumnHeader(
				grid.addColumn(CDTOSystemProcess::getCpuDisplay).setWidth("80px").setFlexGrow(0).setKey("cpu").setSortable(true).setResizable(true),
				"CPU %");
		// Memory usage column
		CGrid.styleColumnHeader(grid.addColumn(CDTOSystemProcess::getMemoryDisplay).setWidth("150px").setFlexGrow(0).setKey("memory")
				.setSortable(true).setResizable(true), "Memory");
		// Command column (flexible width)
		CGrid.styleColumnHeader(
				grid.addColumn(CDTOSystemProcess::getCommand).setWidth("300px").setFlexGrow(1).setKey("command").setSortable(true).setResizable(true),
				"Command");
	}

	@Override
	protected CAbstractCalimeroClient createCalimeroClient(final CClientProject clientProject) {
		return new CSystemProcessCalimeroClient(clientProject);
	}

	/** Create grid component. */
	private void createGrid() {
		grid = new CGrid<>(CDTOSystemProcess.class);
		grid.setId(ID_GRID);
		configureGrid();
		grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
		grid.setHeight("500px");
		add(grid);
	}

	@Override
	protected String getHeaderText() { return "System Process List"; }

	@Override
	protected String getID_ROOT() { // TODO Auto-generated method stub
		return ID_ROOT;
	}

	/** Load system processes from Calimero server. */
	private void loadProcesses() {
		try {
			LOGGER.debug("Loading system processes from Calimero server");
			buttonRefresh.setEnabled(false);
			final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
			if (clientOpt.isEmpty()) {
				showCalimeroUnavailableWarning("Calimero service not available");
				grid.setItems(Collections.emptyList());
				return;
			}
			hideCalimeroUnavailableWarning();
			processClient = (CSystemProcessCalimeroClient) clientOpt.get();
			final List<CDTOSystemProcess> processes = processClient.fetchProcesses();
			grid.setItems(processes);
			LOGGER.info("Loaded {} system processes", processes.size());
			// Update summary with process count
			updateSummary("%d processes listed".formatted(processes.size()));
			CNotificationService.showSuccess("Loaded " + processes.size() + " processes");
		} catch (final Exception e) {
			LOGGER.error("Failed to load system processes: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load system processes", e);
			showCalimeroUnavailableWarning("Failed to load system processes");
			grid.setItems(Collections.emptyList());
		} finally {
			buttonRefresh.setEnabled(true);
		}
	}

	@Override
	protected void refreshComponent() {
		loadProcesses();
	}
}
