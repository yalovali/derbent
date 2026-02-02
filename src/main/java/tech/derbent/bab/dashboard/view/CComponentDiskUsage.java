package tech.derbent.bab.dashboard.view;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.progressbar.ProgressBar;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dto.CDiskInfo;
import tech.derbent.bab.dashboard.service.CDiskUsageCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

/** CComponentDiskUsage - Component for displaying disk/filesystem usage from Calimero server.
 * <p>
 * Displays disk usage for BAB Gateway projects with real-time data from Calimero HTTP API. Shows filesystem information including:
 * <ul>
 * <li>Filesystem device name</li>
 * <li>Mount point</li>
 * <li>Filesystem type (ext4, xfs, etc.)</li>
 * <li>Total size, used space, available space</li>
 * <li>Usage percentage with progress bar</li>
 * <li>Inode usage</li>
 * </ul>
 * <p>
 * Calimero API: POST /api/request with type="disk", operation="usage"
 * <p>
 * Usage:
 *
 * <pre>
 * CComponentDiskUsage component = new CComponentDiskUsage(sessionService);
 * layout.add(component);
 * </pre>
 */
public class CComponentDiskUsage extends CComponentBabBase {

	public static final String ID_GRID = "custom-disk-usage-grid";
	public static final String ID_HEADER = "custom-disk-usage-header";
	public static final String ID_REFRESH_BUTTON = "custom-disk-usage-refresh-button";
	public static final String ID_ROOT = "custom-disk-usage-component";
	public static final String ID_TOOLBAR = "custom-disk-usage-toolbar";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentDiskUsage.class);
	private static final long serialVersionUID = 1L;
	private CButton buttonRefresh;
	private CDiskUsageCalimeroClient diskClient;
	private CGrid<CDiskInfo> grid;
	private final ISessionService sessionService;

	/** Constructor for disk usage component.
	 * @param sessionService the session service */
	public CComponentDiskUsage(final ISessionService sessionService) {
		this.sessionService = sessionService;
		initializeComponents();
	}

	private void configureGrid() {
		// Mount point column
		CGrid.styleColumnHeader(
				grid.addColumn(CDiskInfo::getMountPoint).setWidth("150px").setFlexGrow(0).setKey("mountPoint").setSortable(true).setResizable(true),
				"Mount Point");
		// Filesystem column
		CGrid.styleColumnHeader(
				grid.addColumn(CDiskInfo::getFilesystem).setWidth("180px").setFlexGrow(0).setKey("filesystem").setSortable(true).setResizable(true),
				"Filesystem");
		// Type column
		CGrid.styleColumnHeader(
				grid.addColumn(CDiskInfo::getType).setWidth("80px").setFlexGrow(0).setKey("type").setSortable(true).setResizable(true), "Type");
		// Usage display column
		CGrid.styleColumnHeader(
				grid.addColumn(CDiskInfo::getUsageDisplay).setWidth("220px").setFlexGrow(0).setKey("usage").setSortable(true).setResizable(true),
				"Usage");
		// Available column
		CGrid.styleColumnHeader(grid.addComponentColumn(disk -> {
			final CSpan availSpan = new CSpan(disk.getAvailableDisplay());
			if (disk.isCritical()) {
				availSpan.getStyle().set("color", "var(--lumo-error-color)").set("font-weight", "bold");
			} else if (disk.isWarning()) {
				availSpan.getStyle().set("color", "var(--lumo-warning-color)");
			} else {
				availSpan.getStyle().set("color", "var(--lumo-success-color)");
			}
			return availSpan;
		}).setWidth("120px").setFlexGrow(0).setKey("available").setSortable(true).setResizable(true), "Available");
		// Progress bar column
		CGrid.styleColumnHeader(grid.addComponentColumn(disk -> {
			final CHorizontalLayout layout = new CHorizontalLayout();
			layout.setSpacing(true);
			layout.getStyle().set("gap", "8px").set("align-items", "center");
			final ProgressBar progressBar = new ProgressBar();
			progressBar.setValue(disk.getUsagePercent() / 100.0);
			progressBar.setWidth("100px");
			// Color based on usage
			if (disk.isCritical()) {
				progressBar.getElement().getThemeList().add("error");
			} else if (disk.isWarning()) {
				progressBar.getElement().getThemeList().add("contrast");
			}
			final CSpan percentLabel = new CSpan(String.format("%.1f%%", disk.getUsagePercent()));
			if (disk.isCritical()) {
				percentLabel.getStyle().set("color", "var(--lumo-error-color)").set("font-weight", "bold");
			} else if (disk.isWarning()) {
				percentLabel.getStyle().set("color", "var(--lumo-warning-color)");
			}
			layout.add(progressBar, percentLabel);
			return layout;
		}).setWidth("180px").setFlexGrow(0).setKey("progress").setSortable(true).setResizable(true), "Usage %");
		// Inodes column
		CGrid.styleColumnHeader(
				grid.addColumn(CDiskInfo::getInodesDisplay).setWidth("220px").setFlexGrow(1).setKey("inodes").setSortable(true).setResizable(true),
				"Inodes");
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
		grid = new CGrid<>(CDiskInfo.class);
		grid.setId(ID_GRID);
		configureGrid();
		grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
		grid.setHeight("400px");
		add(grid);
	}

	/** Create header component. */
	private void createHeader() {
		final CH3 header = new CH3("Disk Usage");
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
		loadDiskUsage();
	}

	/** Load disk usage from Calimero server. */
	private void loadDiskUsage() {
		try {
			LOGGER.debug("Loading disk usage from Calimero server");
			buttonRefresh.setEnabled(false);
			final Optional<CClientProject> clientOptional = resolveClientProject();
			if (clientOptional.isEmpty()) {
				grid.setItems(Collections.emptyList());
				return;
			}
			diskClient = new CDiskUsageCalimeroClient(clientOptional.get());
			final List<CDiskInfo> disks = diskClient.fetchDiskUsage();
			grid.setItems(disks);
			LOGGER.info("Loaded {} disk entries", disks.size());
			CNotificationService.showSuccess("Loaded " + disks.size() + " disk entries");
		} catch (final Exception e) {
			LOGGER.error("Failed to load disk usage: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load disk usage", e);
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
		loadDiskUsage();
	}

	private Optional<CProject_Bab> resolveActiveBabProject() {
		return sessionService.getActiveProject().filter(CProject_Bab.class::isInstance).map(CProject_Bab.class::cast);
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
				// Graceful degradation - log warning but DON'T show error dialog
				// Connection refused is expected when Calimero server is not running
				LOGGER.warn("⚠️ Calimero connection failed (graceful degradation): {}", connectionResult.getMessage());
				return Optional.empty();
			}
			httpClient = babProject.getHttpClient();
		}
		return Optional.ofNullable(httpClient);
	}
}
