package tech.derbent.bab.dashboard.dashboardproject_bab.view;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.progressbar.ProgressBar;
import tech.derbent.api.ui.component.basic.CH4;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTOCpuInfo;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CAbstractCalimeroClient;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CCpuInfoCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
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
	// buttonRefresh inherited from CComponentBabBase
	private CSpan architectureLabel;
	private CSpan coresLabel;
	private CSpan frequencyLabel;
	private CSpan idleLabel;
	private CSpan iowaitLabel;
	private CSpan modelLabel;
	private CSpan systemLabel;
	private CSpan temperatureLabel;
	private CSpan usageLabel;
	private ProgressBar usageProgressBar;
	private CSpan userLabel;

	/** Constructor for CPU usage component.
	 * @param sessionService the session service */
	public CComponentCpuUsage(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	@Override
	protected void configureComponent() {
		super.configureComponent();
		createCpuCard();
	}

	@Override
	protected CAbstractCalimeroClient createCalimeroClient(final CClientProject clientProject) {
		return new CCpuInfoCalimeroClient(clientProject);
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
	protected String getHeaderText() { return "CPU Usage"; }

	@Override
	protected String getID_ROOT() { // TODO Auto-generated method stub
		return ID_ROOT;
	}

	/** Load CPU info from Calimero server. */
	@Override
	protected void refreshComponent() {
		LOGGER.debug("Refreshing CPU usage component");
		try {
			LOGGER.debug("Loading CPU info from Calimero server");
			buttonRefresh.setEnabled(false);
			final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
			if (clientOpt.isEmpty()) {
				showCalimeroUnavailableWarning("Calimero service not available");
				updateCpuDisplay(null);
				return;
			}
			hideCalimeroUnavailableWarning();
			final CCpuInfoCalimeroClient cpuClient = (CCpuInfoCalimeroClient) clientOpt.get();
			final Optional<CDTOCpuInfo> cpuOpt = cpuClient.fetchCpuInfo();
			cpuOpt.ifPresentOrElse(value -> {
				updateCpuDisplay(value);
				LOGGER.info("Loaded CPU info successfully");
				CNotificationService.showSuccess("CPU info refreshed");
			}, () -> {
				LOGGER.debug("CPU info not available - displaying N/A");
				updateCpuDisplay(null);
			});
		} catch (final Exception e) {
			LOGGER.warn("Failed to load CPU info: {} (Calimero connection issue)", e.getMessage());
			showCalimeroUnavailableWarning("Failed to load CPU info");
			updateCpuDisplay(null);
		} finally {
			buttonRefresh.setEnabled(true);
		}
	}

	/** Update CPU display with new data. */
	private void updateCpuDisplay(final CDTOCpuInfo cpu) {
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
		usageLabel.setText("%.1f%%".formatted(cpu.getUsagePercent()));
		usageProgressBar.setValue(cpu.getUsagePercent() / 100.0);
		// Usage breakdown
		userLabel.setText("%.1f%%".formatted(cpu.getUserPercent()));
		systemLabel.setText("%.1f%%".formatted(cpu.getSystemPercent()));
		idleLabel.setText("%.1f%%".formatted(cpu.getIdlePercent()));
		iowaitLabel.setText("%.1f%%".formatted(cpu.getIowaitPercent()));
	}
}
