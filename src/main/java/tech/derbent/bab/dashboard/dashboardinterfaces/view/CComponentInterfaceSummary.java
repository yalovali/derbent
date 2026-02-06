package tech.derbent.bab.dashboard.dashboardinterfaces.view;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.bab.dashboard.dashboardinterfaces.dto.CDTOInterfaceSummary;
import tech.derbent.bab.dashboard.dashboardinterfaces.service.CInterfaceDataCalimeroClient;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.api.ui.notifications.CNotificationService;

/**
 * CComponentInterfaceSummary - Overview component for all interface types and their status.
 * <p>
 * Displays summary information for BAB Gateway interfaces with real-time status from Calimero server.
 * Shows overview statistics for:
 * <ul>
 * <li>USB Device count and status</li>
 * <li>Serial Port count and availability</li>
 * <li>Network Interface count (from summary API)</li>
 * <li>Audio Device count (from summary API)</li>
 * <li>Overall interface health summary</li>
 * </ul>
 * <p>
 * Uses CInterfaceDataCalimeroClient to fetch real-time interface data via getAllInterfaces operation.
 */
public class CComponentInterfaceSummary extends CComponentInterfaceBase {

	public static final String ID_REFRESH_BUTTON = "custom-interface-summary-refresh-button";
	public static final String ID_ROOT = "custom-interface-summary-component";

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentInterfaceSummary.class);
	private static final long serialVersionUID = 1L;

	// UI Components
	private CVerticalLayout layoutSummary;
	private CSpan spanActiveCount;
	private CSpan spanAudioCount;
	private CSpan spanNetworkCount;
	private CSpan spanSerialCount;
	private CSpan spanTotalCount;
	private CSpan spanUsbCount;

	/**
	 * Constructor for interface summary component.
	 * @param sessionService the session service
	 */
	public CComponentInterfaceSummary(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	private void createSummaryItem(final String label, final CSpan valueSpan) {
		final CSpan labelSpan = new CSpan(label);
		labelSpan.getStyle().set("font-weight", "bold");
		labelSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
		valueSpan.getStyle().set("font-weight", "bold");
		valueSpan.getStyle().set("color", "var(--lumo-primary-text-color)");
		
		final CHorizontalLayout row = new CHorizontalLayout(labelSpan, valueSpan);
		row.setWidthFull();
		row.setJustifyContentMode(CHorizontalLayout.JustifyContentMode.BETWEEN);
		row.getStyle().set("padding", "4px 8px");
		row.getStyle().set("background", "var(--lumo-contrast-5pct)");
		row.getStyle().set("border-radius", "4px");
		
		layoutSummary.add(row);
	}

	private void createSummaryLayout() {
		layoutSummary = new CVerticalLayout();
		layoutSummary.setSpacing(false);
		layoutSummary.setPadding(false);
		layoutSummary.getStyle().set("gap", "8px");
		
		// Create summary statistics
		createSummaryItem("Total Interfaces:", spanTotalCount = new CSpan("0"));
		createSummaryItem("Network Interfaces:", spanNetworkCount = new CSpan("0"));
		createSummaryItem("USB Devices:", spanUsbCount = new CSpan("0"));
		createSummaryItem("Serial Ports:", spanSerialCount = new CSpan("0"));
		createSummaryItem("Audio Devices:", spanAudioCount = new CSpan("0"));
		createSummaryItem("Active/Available:", spanActiveCount = new CSpan("0"));
		
		add(layoutSummary);
	}

	@Override
	protected String getHeaderText() {
		return "Interface Summary";
	}

	@Override
	protected String getRefreshButtonId() {
		return ID_REFRESH_BUTTON;
	}

	@Override
	public ISessionService getSessionService() {
		return sessionService;
	}

	@Override
	protected void initializeComponents() {
		setId(ID_ROOT);
		
		// Configure component styling
		configureComponent();
		
		// Create header
		add(createHeader());
		
		// Create standard toolbar with refresh button
		add(createStandardToolbar());
		
		// Create summary statistics layout
		createSummaryLayout();
		
		// Load initial data
		loadSummaryData();
	}

	private void loadSummaryData() {
		try {
			hideCalimeroUnavailableWarning();
			
			// Check if interface data is available
			if (!isInterfaceDataAvailable()) {
				showInterfaceDataUnavailableWarning();
				resetCounts();
				return;
			}
			
			final Optional<CInterfaceDataCalimeroClient> clientOpt = getInterfaceDataClient();
			if (clientOpt.isEmpty()) {
				showInterfaceDataUnavailableWarning();
				resetCounts();
				return;
			}
			
			// Fetch complete interface summary from Calimero
			final CCalimeroResponse response = clientOpt.get().getAllInterfaces();
			
			if (response.isSuccess()) {
				final CDTOInterfaceSummary summary = (CDTOInterfaceSummary) response.getDataField("summary", new CDTOInterfaceSummary());
				
				// Calculate active/available count
				int activeCount = summary.getActiveUsbDevicesCount() + summary.getAvailableSerialPortsCount();
				
				// Update UI with real data
				updateCounts(
					summary.getTotalInterfaces(),
					summary.getNetworkCount(),
					summary.getUsbCount(),
					summary.getSerialCount(),
					summary.getAudioCount(),
					activeCount
				);
				
				LOGGER.debug("Loaded interface summary: total={}, network={}, usb={}, serial={}, audio={}, active={}",
					summary.getTotalInterfaces(), summary.getNetworkCount(), summary.getUsbCount(),
					summary.getSerialCount(), summary.getAudioCount(), activeCount);
					
			} else {
				CNotificationService.showError("Failed to load interface summary: " + response.getErrorMessage());
				resetCounts();
			}
			
		} catch (final Exception e) {
			LOGGER.error("Error loading interface summary data", e);
			CNotificationService.showException("Failed to load interface summary", e);
			resetCounts();
		}
	}

	@Override
	protected void refreshComponent() {
		LOGGER.debug("Refreshing interface summary data");
		loadSummaryData();
	}

	private void resetCounts() {
		updateCounts(0, 0, 0, 0, 0, 0);
	}

	private void updateCounts(final int total, final int network, final int usb, final int serial, final int audio, final int active) {
		spanTotalCount.setText(String.valueOf(total));
		spanNetworkCount.setText(String.valueOf(network));
		spanUsbCount.setText(String.valueOf(usb));
		spanSerialCount.setText(String.valueOf(serial));
		spanAudioCount.setText(String.valueOf(audio));
		spanActiveCount.setText(String.valueOf(active));
		
		// Update colors based on status
		if (total > 0) {
			spanActiveCount.getStyle().set("color", 
				active > 0 ? "var(--lumo-success-color)" : "var(--lumo-warning-color)");
		} else {
			spanActiveCount.getStyle().set("color", "var(--lumo-secondary-text-color)");
		}
	}
}
