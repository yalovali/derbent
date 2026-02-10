package tech.derbent.bab.dashboard.dashboardinterfaces.view;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dashboardinterfaces.dto.CDTOAudioDevice;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.project.service.CProject_BabService;
import tech.derbent.api.session.service.ISessionService;

/** CComponentAudioDevices - Component for displaying audio device information.
 * <p>
 * Displays Audio devices for BAB Gateway projects with real-time data from Calimero server. Shows audio device information including:
 * <ul>
 * <li>Device name and description</li>
 * <li>Audio card and device IDs</li>
 * <li>Direction (playback/capture)</li>
 * <li>Channel count and sample rate</li>
 * <li>Device availability status</li>
 * </ul>
 * <p>
 * Uses CInterfaceDataCalimeroClient to fetch audio device data via getAudioDevices operation. */
public class CComponentAudioDevices extends CComponentInterfaceBase {

	public static final String ID_GRID = "custom-audio-devices-grid";
	public static final String ID_ROOT = "custom-audio-devices-component";
	public static final String ID_TEST_BUTTON = "custom-audio-test-button";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentAudioDevices.class);
	private static final long serialVersionUID = 1L;
	// UI Components
	private CButton buttonTest;
	private CGrid<CDTOAudioDevice> grid;

	public CComponentAudioDevices(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	@Override
	protected void addAdditionalToolbarButtons(final CHorizontalLayout toolbarLayout) {
		buttonTest = new CButton("Test Audio", VaadinIcon.VOLUME_UP.create());
		buttonTest.setId(ID_TEST_BUTTON);
		buttonTest.addThemeVariants(ButtonVariant.LUMO_SMALL);
		buttonTest.setEnabled(false); // Enabled when device selected
		buttonTest.addClickListener(e -> on_buttonTest_clicked());
		toolbarLayout.add(buttonTest);
	}

	@Override
	protected void configureComponent() {
		super.configureComponent();
		createGrid();
	}

	private void configureGridColumns() {
		// Device ID column
		grid.addColumn(CDTOAudioDevice::getDeviceId).setHeader("Device ID").setWidth("100px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Device name column
		grid.addColumn(CDTOAudioDevice::getDisplayName).setHeader("Device Name").setWidth("300px").setFlexGrow(1).setSortable(true)
				.setResizable(true);
		// Direction column with colored indicator
		grid.addComponentColumn(device -> {
			final String direction = device.getDirection();
			final CSpan directionSpan = new CSpan(direction);
			directionSpan.getStyle().set("padding", "4px 8px");
			directionSpan.getStyle().set("border-radius", "12px");
			directionSpan.getStyle().set("font-size", "0.8em");
			directionSpan.getStyle().set("font-weight", "bold");
			final String color = device.getStatusColor();
			directionSpan.getStyle().set("background", color + "20"); // 20% opacity
			directionSpan.getStyle().set("color", color);
			return directionSpan;
		}).setHeader("Direction").setWidth("100px").setFlexGrow(0).setSortable(false).setResizable(true);
		// Channels column
		grid.addColumn(device -> {
			final Integer channels = device.getChannels();
			return channels != null ? channels.toString() : "";
		}).setHeader("Channels").setWidth("80px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Sample Rate column
		grid.addColumn(CDTOAudioDevice::getSampleRate).setHeader("Sample Rate").setWidth("100px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Status column
		grid.addComponentColumn(device -> {
			final CSpan statusSpan = new CSpan(Boolean.TRUE.equals(device.getAvailable()) ? "Available" : "Busy");
			statusSpan.getStyle().set("padding", "4px 8px");
			statusSpan.getStyle().set("border-radius", "12px");
			statusSpan.getStyle().set("font-size", "0.8em");
			statusSpan.getStyle().set("font-weight", "bold");
			if (Boolean.TRUE.equals(device.getAvailable())) {
				statusSpan.getStyle().set("background", "var(--lumo-success-color-10pct)");
				statusSpan.getStyle().set("color", "var(--lumo-success-color)");
			} else {
				statusSpan.getStyle().set("background", "var(--lumo-error-color-10pct)");
				statusSpan.getStyle().set("color", "var(--lumo-error-color)");
			}
			// Add default indicator
			if (Boolean.TRUE.equals(device.getDefaultDevice())) {
				final CSpan defaultSpan = new CSpan("⭐");
				defaultSpan.getStyle().set("margin-left", "4px");
				statusSpan.add(defaultSpan);
			}
			return statusSpan;
		}).setHeader("Status").setWidth("100px").setFlexGrow(0).setSortable(false).setResizable(true);
	}

	protected void createGrid() {
		grid = new CGrid<>(CDTOAudioDevice.class);
		grid.setId(ID_GRID);
		// Configure columns for audio device display
		configureGridColumns();
		// Selection listener for Test button
		grid.asSingleSelect()
				.addValueChangeListener(e -> buttonTest.setEnabled(e.getValue() != null && Boolean.TRUE.equals(e.getValue().getAvailable())));
		add(grid);
	}

	@Override
	protected String getHeaderText() { return "Audio Devices"; }

	@Override
	protected String getID_ROOT() { // TODO Auto-generated method stub
		return ID_ROOT;
	}

	@Override
	protected boolean hasRefreshButton() {
		return false; // Page-level refresh used
	}

	private void on_buttonTest_clicked() {
		final CDTOAudioDevice selectedDevice = grid.asSingleSelect().getValue();
		if (selectedDevice != null) {
			CNotificationService.showInfo("Audio test for " + selectedDevice.getDisplayName() + " - Feature coming soon");
			// TODO: Implement audio test functionality
		}
	}

	@Override
	protected void refreshComponent() {
		LOGGER.debug("🔄 Refreshing audio devices component");
		try {
			hideCalimeroUnavailableWarning();
			final Optional<CProject_Bab> projectOpt = sessionService.getActiveProject().map(p -> (CProject_Bab) p);
			if (projectOpt.isEmpty()) {
				handleMissingInterfaceData("Audio Devices");
				updateSummary(null);
				grid.setItems();
				return;
			}
			final CProject_Bab project = projectOpt.get();
			final String cachedJson = project.getInterfacesJson();
			if (cachedJson == null || cachedJson.isBlank() || "{}".equals(cachedJson)) {
				handleMissingInterfaceData("Audio Devices");
				updateSummary(null);
				grid.setItems();
				return;
			}
			final CProject_BabService service = CSpringContext.getBean(CProject_BabService.class);
			final List<CDTOAudioDevice> devices = service.getAudioDevices(project);
			grid.setItems(devices);
			final long playbackDevices = devices.stream().filter(CDTOAudioDevice::isPlayback).count();
			final long captureDevices = devices.stream().filter(CDTOAudioDevice::isCapture).count();
			final long availableDevices = devices.stream().filter(device -> Boolean.TRUE.equals(device.getAvailable())).count();
			updateSummary("%d device%s (%d playback, %d capture, %d available)".formatted(devices.size(), devices.size() == 1 ? "" : "s",
					playbackDevices, captureDevices, availableDevices));
		} catch (final Exception e) {
			LOGGER.error("❌ Error loading audio devices", e);
			CNotificationService.showException("Failed to load audio devices", e);
			updateSummary(null);
			grid.setItems();
			handleMissingInterfaceData("Audio Devices");
		}
	}
}
