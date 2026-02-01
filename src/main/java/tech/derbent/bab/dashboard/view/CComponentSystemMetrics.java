package tech.derbent.bab.dashboard.view;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.progressbar.ProgressBar;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.service.CSystemMetricsCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

/**
 * CComponentSystemMetrics - Component for displaying system resource metrics from Calimero server.
 * <p>
 * Displays real-time system metrics for BAB Gateway projects including:
 * <ul>
 *   <li>CPU usage percentage with progress bar</li>
 *   <li>Memory usage (used/total MB) with progress bar</li>
 *   <li>Disk usage (used/total GB) with progress bar</li>
 *   <li>System uptime in human-readable format</li>
 *   <li>Load average (1min, 5min, 15min)</li>
 * </ul>
 * <p>
 * Calimero API: POST /api/request with type="system", operation="metrics"
 * <p>
 * Usage:
 * <pre>
 * CComponentSystemMetrics component = new CComponentSystemMetrics(sessionService);
 * layout.add(component);
 * </pre>
 */
public class CComponentSystemMetrics extends CComponentBabBase {
	
	public static final String ID_ROOT = "custom-system-metrics-component";
	public static final String ID_HEADER = "custom-system-metrics-header";
	public static final String ID_REFRESH_BUTTON = "custom-system-metrics-refresh-button";
	public static final String ID_CPU_CARD = "custom-cpu-card";
	public static final String ID_MEMORY_CARD = "custom-memory-card";
	public static final String ID_DISK_CARD = "custom-disk-card";
	public static final String ID_UPTIME_CARD = "custom-uptime-card";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentSystemMetrics.class);
	private static final long serialVersionUID = 1L;
	
	private CButton buttonRefresh;
	private CSystemMetricsCalimeroClient metricsClient;
	private final ISessionService sessionService;
	
	// UI Components for metrics display
	private CSpan cpuValueLabel;
	private ProgressBar cpuProgressBar;
	private CSpan memoryValueLabel;
	private ProgressBar memoryProgressBar;
	private CSpan diskValueLabel;
	private ProgressBar diskProgressBar;
	private CSpan uptimeValueLabel;
	private CSpan loadAverageValueLabel;
	
	/**
	 * Constructor for system metrics component.
	 * @param sessionService the session service
	 */
	public CComponentSystemMetrics(final ISessionService sessionService) {
		this.sessionService = sessionService;
		initializeComponents();
	}
	
	/** Factory method for refresh button. Subclasses can override to customize button. */
	protected CButton create_buttonRefresh() {
		final CButton button = new CButton("Refresh", VaadinIcon.REFRESH.create());
		button.setId(ID_REFRESH_BUTTON);
		button.addThemeVariants(ButtonVariant.LUMO_SMALL);
		button.addClickListener(e -> on_buttonRefresh_clicked());
		return button;
	}
	
	/** Create metric card with label, value, and progress bar. */
	private Div createMetricCard(final String id, final String title, final String icon, final String color) {
		final Div card = new Div();
		card.setId(id);
		card.addClassName("metric-card");
		card.getStyle()
				.set("padding", "16px")
				.set("border-radius", "8px")
				.set("border", "1px solid var(--lumo-contrast-10pct)")
				.set("background", "var(--lumo-base-color)")
				.set("min-width", "250px");
		
		// Title row with icon
		final CHorizontalLayout titleLayout = new CHorizontalLayout();
		titleLayout.setSpacing(true);
		titleLayout.getStyle().set("gap", "8px").set("margin-bottom", "8px");
		
		final CSpan titleSpan = new CSpan(title);
		titleSpan.getStyle().set("font-weight", "600").set("font-size", "0.9rem").set("color", color);
		titleLayout.add(titleSpan);
		
		card.add(titleLayout);
		
		return card;
	}
	
	/** Create CPU usage card. */
	private void createCpuCard(final CVerticalLayout container) {
		final Div cpuCard = createMetricCard(ID_CPU_CARD, "CPU Usage", "cpu", "var(--lumo-error-color)");
		
		cpuValueLabel = new CSpan("0%");
		cpuValueLabel.getStyle()
				.set("font-size", "1.5rem")
				.set("font-weight", "bold")
				.set("color", "var(--lumo-error-color)");
		
		cpuProgressBar = new ProgressBar();
		cpuProgressBar.setValue(0);
		cpuProgressBar.setWidth("100%");
		cpuProgressBar.getStyle().set("margin-top", "8px");
		
		cpuCard.add(cpuValueLabel, cpuProgressBar);
		container.add(cpuCard);
	}
	
	/** Create memory usage card. */
	private void createMemoryCard(final CVerticalLayout container) {
		final Div memoryCard = createMetricCard(ID_MEMORY_CARD, "Memory Usage", "database", "var(--lumo-primary-color)");
		
		memoryValueLabel = new CSpan("0 MB / 0 MB (0%)");
		memoryValueLabel.getStyle()
				.set("font-size", "1rem")
				.set("font-weight", "600")
				.set("color", "var(--lumo-primary-color)");
		
		memoryProgressBar = new ProgressBar();
		memoryProgressBar.setValue(0);
		memoryProgressBar.setWidth("100%");
		memoryProgressBar.getStyle().set("margin-top", "8px");
		
		memoryCard.add(memoryValueLabel, memoryProgressBar);
		container.add(memoryCard);
	}
	
	/** Create disk usage card. */
	private void createDiskCard(final CVerticalLayout container) {
		final Div diskCard = createMetricCard(ID_DISK_CARD, "Disk Usage", "harddrive", "var(--lumo-success-color)");
		
		diskValueLabel = new CSpan("0 GB / 0 GB (0%)");
		diskValueLabel.getStyle()
				.set("font-size", "1rem")
				.set("font-weight", "600")
				.set("color", "var(--lumo-success-color)");
		
		diskProgressBar = new ProgressBar();
		diskProgressBar.setValue(0);
		diskProgressBar.setWidth("100%");
		diskProgressBar.getStyle().set("margin-top", "8px");
		
		diskCard.add(diskValueLabel, diskProgressBar);
		container.add(diskCard);
	}
	
	/** Create uptime and load average card. */
	private void createUptimeCard(final CVerticalLayout container) {
		final Div uptimeCard = createMetricCard(ID_UPTIME_CARD, "System Info", "clock", "var(--lumo-contrast-70pct)");
		
		final CVerticalLayout infoLayout = new CVerticalLayout();
		infoLayout.setPadding(false);
		infoLayout.setSpacing(false);
		infoLayout.getStyle().set("gap", "8px");
		
		// Uptime row
		final CHorizontalLayout uptimeRow = new CHorizontalLayout();
		uptimeRow.setSpacing(true);
		uptimeRow.getStyle().set("gap", "8px");
		
		final CSpan uptimeLabel = new CSpan("Uptime:");
		uptimeLabel.getStyle().set("font-weight", "600").set("color", "var(--lumo-contrast-70pct)");
		
		uptimeValueLabel = new CSpan("-");
		uptimeValueLabel.getStyle().set("color", "var(--lumo-contrast-90pct)");
		
		uptimeRow.add(uptimeLabel, uptimeValueLabel);
		
		// Load average row
		final CHorizontalLayout loadRow = new CHorizontalLayout();
		loadRow.setSpacing(true);
		loadRow.getStyle().set("gap", "8px");
		
		final CSpan loadLabel = new CSpan("Load Avg:");
		loadLabel.getStyle().set("font-weight", "600").set("color", "var(--lumo-contrast-70pct)");
		
		loadAverageValueLabel = new CSpan("-");
		loadAverageValueLabel.getStyle().set("color", "var(--lumo-contrast-90pct)");
		
		loadRow.add(loadLabel, loadAverageValueLabel);
		
		infoLayout.add(uptimeRow, loadRow);
		uptimeCard.add(infoLayout);
		container.add(uptimeCard);
	}
	
	/** Create header component. */
	private void createHeader() {
		final CH3 header = new CH3("System Metrics");
		header.setHeight(null);
		header.setId(ID_HEADER);
		header.getStyle().set("margin", "0");
		add(header);
	}
	
	/** Create metrics cards layout. */
	private void createMetricsCards() {
		final CHorizontalLayout cardsLayout = new CHorizontalLayout();
		cardsLayout.setSpacing(true);
		cardsLayout.getStyle().set("gap", "16px");
		cardsLayout.setWidthFull();
		
		final CVerticalLayout column1 = new CVerticalLayout();
		column1.setPadding(false);
		column1.setSpacing(false);
		column1.getStyle().set("gap", "16px");
		
		final CVerticalLayout column2 = new CVerticalLayout();
		column2.setPadding(false);
		column2.setSpacing(false);
		column2.getStyle().set("gap", "16px");
		
		createCpuCard(column1);
		createMemoryCard(column1);
		
		createDiskCard(column2);
		createUptimeCard(column2);
		
		cardsLayout.add(column1, column2);
		add(cardsLayout);
	}
	
	/** Create toolbar with action buttons. */
	private void createToolbar() {
		final CHorizontalLayout layoutToolbar = new CHorizontalLayout();
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
		createMetricsCards();
		loadMetrics();
	}
	
	/** Load system metrics from Calimero server. */
	private void loadMetrics() {
		try {
			LOGGER.debug("Loading system metrics from Calimero server");
			buttonRefresh.setEnabled(false);
			
			final Optional<CClientProject> clientOptional = resolveClientProject();
			if (clientOptional.isEmpty()) {
				updateMetricsDisplay(null);
				return;
			}
			
			metricsClient = new CSystemMetricsCalimeroClient(clientOptional.get());
			final Optional<CSystemMetrics> metricsOpt = metricsClient.fetchMetrics();
			
			if (metricsOpt.isPresent()) {
				final CSystemMetrics metrics = metricsOpt.get();
				updateMetricsDisplay(metrics);
				LOGGER.info("Loaded system metrics successfully");
				CNotificationService.showSuccess("System metrics refreshed");
			} else {
				updateMetricsDisplay(null);
				CNotificationService.showWarning("Failed to load system metrics");
			}
			
		} catch (final Exception e) {
			LOGGER.error("Failed to load system metrics: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load system metrics", e);
			updateMetricsDisplay(null);
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
		loadMetrics();
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
	
	/** Update metrics display with new data. */
	private void updateMetricsDisplay(final CSystemMetrics metrics) {
		if (metrics == null) {
			// Show empty state
			cpuValueLabel.setText("N/A");
			cpuProgressBar.setValue(0);
			memoryValueLabel.setText("N/A");
			memoryProgressBar.setValue(0);
			diskValueLabel.setText("N/A");
			diskProgressBar.setValue(0);
			uptimeValueLabel.setText("-");
			loadAverageValueLabel.setText("-");
			return;
		}
		
		// Update CPU
		cpuValueLabel.setText(metrics.getCpuUsagePercent() + "%");
		cpuProgressBar.setValue(metrics.getCpuUsagePercent().doubleValue() / 100.0);
		
		// Update Memory
		memoryValueLabel.setText(metrics.getMemoryDisplay());
		memoryProgressBar.setValue(metrics.getMemoryUsagePercent().doubleValue() / 100.0);
		
		// Update Disk
		diskValueLabel.setText(metrics.getDiskDisplay());
		diskProgressBar.setValue(metrics.getDiskUsagePercent().doubleValue() / 100.0);
		
		// Update Uptime
		uptimeValueLabel.setText(metrics.getUptimeDisplay());
		
		// Update Load Average
		loadAverageValueLabel.setText(metrics.getLoadAverageDisplay());
	}
}
