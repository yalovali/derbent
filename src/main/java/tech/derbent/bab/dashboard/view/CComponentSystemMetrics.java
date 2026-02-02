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
 * Displays real-time system metrics for BAB Gateway projects in a horizontal single-row layout:
 * <ul>
 *   <li><strong>CPU Usage</strong> - Percentage with progress bar</li>
 *   <li><strong>Memory</strong> - Used/Total GB with progress bar</li>
 *   <li><strong>Disk</strong> - Used/Total GB with progress bar</li>
 *   <li><strong>System Info</strong> - Uptime and Load Average (1min, 5min)</li>
 * </ul>
 * <p>
 * <strong>Layout Optimization:</strong> All metrics cards are arranged in a single horizontal row for 
 * optimal space utilization and better visual flow. Cards are responsive and will wrap on very small screens.
 * <p>
 * Calimero API: POST /api/request with type="system", operation="metrics"
 * <p>
 * Usage:
 * <pre>
 * CComponentSystemMetrics component = new CComponentSystemMetrics(sessionService);
 * layout.add(component);
 * </pre>
 * <p>
 * Card sizing: Each card has flex: 1 for equal distribution, min-width: 180px, max-width: 220px
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
	
	/** Create compact metric card optimized for horizontal layout. */
	private Div createCompactMetricCard(final String id, final String title, final String icon, final String color) {
		final Div card = new Div();
		card.setId(id);
		card.addClassName("metric-card");
		card.getStyle()
				.set("padding", "12px")  // Reduced padding
				.set("border-radius", "8px")
				.set("border", "1px solid var(--lumo-contrast-10pct)")
				.set("background", "var(--lumo-base-color)")
				.set("flex", "1")  // Equal width distribution
				.set("min-width", "180px")  // Smaller minimum width
				.set("max-width", "220px")  // Maximum width to prevent stretching
				.set("display", "flex")
				.set("flex-direction", "column")
				.set("justify-content", "space-between");
		
		// Title row with icon
		final CHorizontalLayout titleLayout = new CHorizontalLayout();
		titleLayout.setSpacing(true);
		titleLayout.getStyle().set("gap", "6px").set("margin-bottom", "6px");
		
		final CSpan titleSpan = new CSpan(title);
		titleSpan.getStyle()
				.set("font-weight", "600")
				.set("font-size", "0.85rem")  // Slightly smaller font
				.set("color", color);
		titleLayout.add(titleSpan);
		
		card.add(titleLayout);
		
		return card;
	}
	
	/** Create compact CPU usage card. */
	private void createCompactCpuCard(final CHorizontalLayout container) {
		final Div cpuCard = createCompactMetricCard(ID_CPU_CARD, "CPU Usage", "cpu", "var(--lumo-error-color)");
		
		cpuValueLabel = new CSpan("0%");
		cpuValueLabel.getStyle()
				.set("font-size", "1.3rem")  // Slightly smaller
				.set("font-weight", "bold")
				.set("color", "var(--lumo-error-color)")
				.set("margin-bottom", "4px");
		
		cpuProgressBar = new ProgressBar();
		cpuProgressBar.setValue(0);
		cpuProgressBar.setWidth("100%");
		cpuProgressBar.setHeight("6px");  // Thinner progress bar
		
		cpuCard.add(cpuValueLabel, cpuProgressBar);
		container.add(cpuCard);
	}
	
	/** Create compact memory usage card. */
	private void createCompactMemoryCard(final CHorizontalLayout container) {
		final Div memoryCard = createCompactMetricCard(ID_MEMORY_CARD, "Memory", "database", "var(--lumo-primary-color)");
		
		memoryValueLabel = new CSpan("0 MB / 0 MB");
		memoryValueLabel.getStyle()
				.set("font-size", "0.9rem")  // Smaller text to fit
				.set("font-weight", "600")
				.set("color", "var(--lumo-primary-color)")
				.set("margin-bottom", "4px")
				.set("line-height", "1.2");
		
		memoryProgressBar = new ProgressBar();
		memoryProgressBar.setValue(0);
		memoryProgressBar.setWidth("100%");
		memoryProgressBar.setHeight("6px");  // Thinner progress bar
		
		memoryCard.add(memoryValueLabel, memoryProgressBar);
		container.add(memoryCard);
	}
	
	/** Create compact disk usage card. */
	private void createCompactDiskCard(final CHorizontalLayout container) {
		final Div diskCard = createCompactMetricCard(ID_DISK_CARD, "Disk", "harddrive", "var(--lumo-success-color)");
		
		diskValueLabel = new CSpan("0 GB / 0 GB");
		diskValueLabel.getStyle()
				.set("font-size", "0.9rem")  // Smaller text to fit
				.set("font-weight", "600")
				.set("color", "var(--lumo-success-color)")
				.set("margin-bottom", "4px")
				.set("line-height", "1.2");
		
		diskProgressBar = new ProgressBar();
		diskProgressBar.setValue(0);
		diskProgressBar.setWidth("100%");
		diskProgressBar.setHeight("6px");  // Thinner progress bar
		
		diskCard.add(diskValueLabel, diskProgressBar);
		container.add(diskCard);
	}
	
	/** Create compact uptime and load average card. */
	private void createCompactUptimeCard(final CHorizontalLayout container) {
		final Div uptimeCard = createCompactMetricCard(ID_UPTIME_CARD, "System", "clock", "var(--lumo-contrast-70pct)");
		
		final CVerticalLayout infoLayout = new CVerticalLayout();
		infoLayout.setPadding(false);
		infoLayout.setSpacing(false);
		infoLayout.getStyle().set("gap", "4px");  // Reduced gap
		
		// Compact uptime display
		final CVerticalLayout uptimeSection = new CVerticalLayout();
		uptimeSection.setPadding(false);
		uptimeSection.setSpacing(false);
		
		final CSpan uptimeLabel = new CSpan("Uptime");
		uptimeLabel.getStyle()
				.set("font-weight", "600")
				.set("font-size", "0.8rem")
				.set("color", "var(--lumo-contrast-70pct)");
		
		uptimeValueLabel = new CSpan("-");
		uptimeValueLabel.getStyle()
				.set("color", "var(--lumo-contrast-90pct)")
				.set("font-size", "0.9rem")
				.set("line-height", "1.2");
		
		uptimeSection.add(uptimeLabel, uptimeValueLabel);
		
		// Compact load average display
		final CVerticalLayout loadSection = new CVerticalLayout();
		loadSection.setPadding(false);
		loadSection.setSpacing(false);
		
		final CSpan loadLabel = new CSpan("Load Avg");
		loadLabel.getStyle()
				.set("font-weight", "600")
				.set("font-size", "0.8rem")
				.set("color", "var(--lumo-contrast-70pct)");
		
		loadAverageValueLabel = new CSpan("-");
		loadAverageValueLabel.getStyle()
				.set("color", "var(--lumo-contrast-90pct)")
				.set("font-size", "0.9rem")
				.set("line-height", "1.2");
		
		loadSection.add(loadLabel, loadAverageValueLabel);
		
		infoLayout.add(uptimeSection, loadSection);
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
	
	/** Create metrics cards layout - optimized for single row display. */
	private void createMetricsCards() {
		final CHorizontalLayout cardsLayout = new CHorizontalLayout();
		cardsLayout.setSpacing(true);
		cardsLayout.getStyle()
				.set("gap", "12px")  // Reduced gap for better space utilization
				.set("flex-wrap", "wrap")  // Allow wrapping if needed on very small screens
				.set("align-items", "stretch");  // Ensure all cards have same height
		cardsLayout.setWidthFull();
		
		// Create all cards directly in horizontal layout (single row)
		createCompactCpuCard(cardsLayout);
		createCompactMemoryCard(cardsLayout);
		createCompactDiskCard(cardsLayout);
		createCompactUptimeCard(cardsLayout);
		
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
			LOGGER.info("Loading system metrics from Calimero server");
			buttonRefresh.setEnabled(false);
			
			final Optional<CClientProject> clientOptional = resolveClientProject();
			if (clientOptional.isEmpty()) {
				LOGGER.warn("No HTTP client available for loading metrics");
				updateMetricsDisplay(null);
				return;
			}
			
			metricsClient = new CSystemMetricsCalimeroClient(clientOptional.get());
			final Optional<CSystemMetrics> metricsOpt = metricsClient.fetchMetrics();
			
			if (metricsOpt.isPresent()) {
				final CSystemMetrics metrics = metricsOpt.get();
				updateMetricsDisplay(metrics);
				LOGGER.info("✅ Loaded system metrics successfully");
				CNotificationService.showSuccess("System metrics refreshed");
			} else {
				// Graceful degradation - no notification, just display N/A
				LOGGER.debug("System metrics not available - displaying N/A (Calimero may not be connected)");
				updateMetricsDisplay(null);
			}
			
		} catch (final IllegalStateException e) {
			// Authentication/Authorization exceptions - show as critical error
			LOGGER.error("🔐❌ Authentication/Authorization error while loading metrics: {}", e.getMessage(), e);
			CNotificationService.showException("Authentication Error", e);
			updateMetricsDisplay(null);
		} catch (final Exception e) {
			// Graceful degradation for other errors - log but don't show exception to user
			LOGGER.warn("⚠️ Failed to load system metrics: {} (Calimero connection issue - normal in test environments)", e.getMessage());
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
				// Graceful degradation - log warning but DON'T show error dialog
				// Connection refused is expected when Calimero server is not running
				LOGGER.warn("⚠️ Calimero connection failed (graceful degradation): {}", connectionResult.getMessage());
				return Optional.empty();
			}
			httpClient = babProject.getHttpClient();
		}
		
		return Optional.ofNullable(httpClient);
	}
	
	/** Update metrics display with new data - optimized for compact layout. */
	private void updateMetricsDisplay(final CSystemMetrics metrics) {
		if (metrics == null) {
			// Show empty state
			cpuValueLabel.setText("N/A");
			cpuProgressBar.setValue(0);
			memoryValueLabel.setText("No Data");
			memoryProgressBar.setValue(0);
			diskValueLabel.setText("No Data");
			diskProgressBar.setValue(0);
			uptimeValueLabel.setText("-");
			loadAverageValueLabel.setText("-");
			return;
		}
		
		// Update CPU - show percentage only
		final String cpuText = String.format("%.1f%%", metrics.getCpuUsagePercent());
		cpuValueLabel.setText(cpuText);
		cpuProgressBar.setValue(metrics.getCpuUsagePercent().doubleValue() / 100.0);
		
		// Update Memory - more compact format
		final String memoryText = String.format("%.1f GB / %.1f GB", 
				metrics.getMemoryUsedMB() / 1024.0, 
				metrics.getMemoryTotalMB() / 1024.0);
		memoryValueLabel.setText(memoryText);
		memoryProgressBar.setValue(metrics.getMemoryUsagePercent().doubleValue() / 100.0);
		
		// Update Disk - more compact format
		final String diskText = String.format("%.1f GB / %.1f GB", 
				metrics.getDiskUsedGB(), 
				metrics.getDiskTotalGB());
		diskValueLabel.setText(diskText);
		diskProgressBar.setValue(metrics.getDiskUsagePercent().doubleValue() / 100.0);
		
		// Update Uptime - keep as is (already compact)
		uptimeValueLabel.setText(metrics.getUptimeDisplay());
		
		// Update Load Average - more compact format (show only 1min and 5min)
		final String loadText = String.format("%.2f, %.2f", 
				metrics.getLoadAverage1(), 
				metrics.getLoadAverage5());
		loadAverageValueLabel.setText(loadText);
	}
}
