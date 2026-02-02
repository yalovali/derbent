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
import tech.derbent.api.ui.component.basic.CH4;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dto.CCpuInfo;
import tech.derbent.bab.dashboard.service.CCpuInfoCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

/** CComponentCpuUsage - Component for displaying detailed CPU usage from Calimero server.
 * <p>
 * Displays CPU information for BAB Gateway projects including:
 * <ul>
 * <li>CPU model and architecture</li>
 * <li>Core and thread count</li>
 * <li>Current frequency and max frequency</li>
 * <li>Overall usage percentage with progress bar</li>
 * <li>User, system, idle, and iowait percentages</li>
 * <li>CPU temperature</li>
 * </ul>
 * <p>
 * Calimero API: POST /api/request with type="system", operation="cpuInfo"
 * <p>
 * Usage:
 *
 * <pre>
 * CComponentCpuUsage component = new CComponentCpuUsage(sessionService);
 * layout.add(component);
 * </pre>
 */
public class CComponentCpuUsage extends CComponentBabBase {

	public static final String ID_CPU_CARD = "custom-cpu-card";
	public static final String ID_HEADER = "custom-cpu-usage-header";
	public static final String ID_REFRESH_BUTTON = "custom-cpu-usage-refresh-button";
	public static final String ID_ROOT = "custom-cpu-usage-component";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentCpuUsage.class);
	private static final long serialVersionUID = 1L;
	private CSpan architectureLabel;
	private CButton buttonRefresh;
	private CSpan coresLabel;
	private CCpuInfoCalimeroClient cpuClient;
	private CSpan frequencyLabel;
	private CSpan idleLabel;
	private CSpan iowaitLabel;
	// UI Components
	private CSpan modelLabel;
	private final ISessionService sessionService;
	private CSpan systemLabel;
	private CSpan temperatureLabel;
	private CSpan usageLabel;
	private ProgressBar usageProgressBar;
	private CSpan userLabel;

	/** Constructor for CPU usage component.
	 * @param sessionService the session service */
	public CComponentCpuUsage(final ISessionService sessionService) {
		this.sessionService = sessionService;
		initializeComponents();
	}

	/** Factory method for refresh button. */
	protected CButton create_buttonRefresh() {
		final CButton button = new CButton("Refresh", VaadinIcon.REFRESH.create());
		button.setId(ID_REFRESH_BUTTON);
		button.addThemeVariants(ButtonVariant.LUMO_SMALL);
		button.addClickListener(e -> on_buttonRefresh_clicked());
		return button;
	}

	/** Create CPU info card. */
	private void createCpuCard() {
		final Div card = new Div();
		card.setId(ID_CPU_CARD);
		card.addClassName("cpu-card");
		card.getStyle().set("padding", "20px").set("border-radius", "8px").set("border", "1px solid var(--lumo-contrast-10pct)").set("background",
				"var(--lumo-base-color)");
		final CVerticalLayout layout = new CVerticalLayout();
		layout.setPadding(false);
		layout.setSpacing(false);
		layout.getStyle().set("gap", "16px");
		// Model section
		final CH4 modelHeader = new CH4("CPU Model");
		modelHeader.getStyle().set("margin", "0").set("color", "var(--lumo-contrast-70pct)");
		modelLabel = new CSpan("Loading...");
		modelLabel.getStyle().set("font-size", "0.95rem");
		layout.add(modelHeader, modelLabel);
		// Specifications grid
		final CHorizontalLayout specsGrid = new CHorizontalLayout();
		specsGrid.setSpacing(true);
		specsGrid.getStyle().set("gap", "24px");
		final CVerticalLayout col1 = new CVerticalLayout();
		col1.setPadding(false);
		col1.setSpacing(false);
		col1.getStyle().set("gap", "8px");
		final CVerticalLayout col2 = new CVerticalLayout();
		col2.setPadding(false);
		col2.setSpacing(false);
		col2.getStyle().set("gap", "8px");
		// Column 1: Cores, Architecture
		col1.add(createInfoRow("Cores:", coresLabel = new CSpan("-")));
		col1.add(createInfoRow("Architecture:", architectureLabel = new CSpan("-")));
		// Column 2: Frequency, Temperature
		col2.add(createInfoRow("Frequency:", frequencyLabel = new CSpan("-")));
		col2.add(createInfoRow("Temperature:", temperatureLabel = new CSpan("-")));
		specsGrid.add(col1, col2);
		layout.add(specsGrid);
		// Usage section
		final CH4 usageHeader = new CH4("Current Usage");
		usageHeader.getStyle().set("margin", "0").set("color", "var(--lumo-contrast-70pct)");
		layout.add(usageHeader);
		// Overall usage with progress bar
		usageLabel = new CSpan("0%");
		usageLabel.getStyle().set("font-size", "1.8rem").set("font-weight", "bold").set("color", "var(--lumo-error-color)");
		usageProgressBar = new ProgressBar();
		usageProgressBar.setValue(0);
		usageProgressBar.setWidth("100%");
		usageProgressBar.getStyle().set("margin-top", "8px");
		layout.add(usageLabel, usageProgressBar);
		// Usage breakdown
		final CHorizontalLayout breakdownGrid = new CHorizontalLayout();
		breakdownGrid.setSpacing(true);
		breakdownGrid.getStyle().set("gap", "16px").set("margin-top", "8px");
		breakdownGrid.add(createUsageBreakdown("User", userLabel = new CSpan("0%")));
		breakdownGrid.add(createUsageBreakdown("System", systemLabel = new CSpan("0%")));
		breakdownGrid.add(createUsageBreakdown("Idle", idleLabel = new CSpan("100%")));
		breakdownGrid.add(createUsageBreakdown("I/O Wait", iowaitLabel = new CSpan("0%")));
		layout.add(breakdownGrid);
		card.add(layout);
		add(card);
	}

	/** Create header component. */
	private void createHeader() {
		final CH3 header = new CH3("CPU Usage");
		header.setHeight(null);
		header.setId(ID_HEADER);
		header.getStyle().set("margin", "0");
		add(header);
	}

	/** Create info row with label and value. */
	private CHorizontalLayout createInfoRow(final String label, final CSpan valueSpan) {
		final CHorizontalLayout row = new CHorizontalLayout();
		row.setSpacing(true);
		row.getStyle().set("gap", "8px");
		final CSpan labelSpan = new CSpan(label);
		labelSpan.getStyle().set("font-weight", "600").set("color", "var(--lumo-contrast-70pct)");
		valueSpan.getStyle().set("color", "var(--lumo-contrast-90pct)");
		row.add(labelSpan, valueSpan);
		return row;
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

	/** Create usage breakdown item. */
	private CVerticalLayout createUsageBreakdown(final String label, final CSpan valueSpan) {
		final CVerticalLayout col = new CVerticalLayout();
		col.setPadding(false);
		col.setSpacing(false);
		col.getStyle().set("gap", "4px").set("align-items", "center");
		final CSpan labelSpan = new CSpan(label);
		labelSpan.getStyle().set("font-size", "0.85rem").set("color", "var(--lumo-contrast-60pct)");
		valueSpan.getStyle().set("font-weight", "600").set("font-size", "1.1rem");
		col.add(valueSpan, labelSpan);
		return col;
	}

	@Override
	protected void initializeComponents() {
		setId(ID_ROOT);
		configureComponent();
		createHeader();
		createToolbar();
		createCpuCard();
		loadCpuInfo();
	}

	/** Load CPU info from Calimero server. */
	private void loadCpuInfo() {
		try {
			LOGGER.debug("Loading CPU info from Calimero server");
			buttonRefresh.setEnabled(false);
			final Optional<CClientProject> clientOptional = resolveClientProject();
			if (clientOptional.isEmpty()) {
				updateCpuDisplay(null);
				return;
			}
			cpuClient = new CCpuInfoCalimeroClient(clientOptional.get());
			final Optional<CCpuInfo> cpuOpt = cpuClient.fetchCpuInfo();
			if (cpuOpt.isPresent()) {
				updateCpuDisplay(cpuOpt.get());
				LOGGER.info("Loaded CPU info successfully");
				CNotificationService.showSuccess("CPU info refreshed");
			} else {
				// Graceful degradation - no notification, just display N/A
				LOGGER.debug("CPU info not available - displaying N/A (Calimero may not be connected)");
				updateCpuDisplay(null);
			}
		} catch (final Exception e) {
			// Graceful degradation - log but don't show exception to user
			LOGGER.warn("Failed to load CPU info: {} (Calimero connection issue - normal in test environments)", e.getMessage());
			updateCpuDisplay(null);
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
		loadCpuInfo();
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

	/** Update CPU display with new data. */
	private void updateCpuDisplay(final CCpuInfo cpu) {
		if (cpu == null) {
			modelLabel.setText("N/A");
			coresLabel.setText("-");
			architectureLabel.setText("-");
			frequencyLabel.setText("-");
			temperatureLabel.setText("-");
			usageLabel.setText("N/A");
			usageProgressBar.setValue(0);
			userLabel.setText("0%");
			systemLabel.setText("0%");
			idleLabel.setText("100%");
			iowaitLabel.setText("0%");
			return;
		}
		// Model and specs
		modelLabel.setText(cpu.getModel());
		coresLabel.setText(cpu.getCoreDisplay());
		architectureLabel.setText(cpu.getArchitecture());
		frequencyLabel.setText(cpu.getFrequencyDisplay());
		// Temperature with color coding
		temperatureLabel.setText(cpu.getTemperatureDisplay());
		if (cpu.isHighTemperature()) {
			temperatureLabel.getStyle().set("color", "var(--lumo-error-color)").set("font-weight", "bold");
		} else {
			temperatureLabel.getStyle().remove("color").remove("font-weight");
		}
		// Usage
		usageLabel.setText(String.format("%.1f%%", cpu.getUsagePercent()));
		usageProgressBar.setValue(cpu.getUsagePercent() / 100.0);
		// Usage breakdown
		userLabel.setText(String.format("%.1f%%", cpu.getUserPercent()));
		systemLabel.setText(String.format("%.1f%%", cpu.getSystemPercent()));
		idleLabel.setText(String.format("%.1f%%", cpu.getIdlePercent()));
		iowaitLabel.setText(String.format("%.1f%%", cpu.getIowaitPercent()));
	}
}
