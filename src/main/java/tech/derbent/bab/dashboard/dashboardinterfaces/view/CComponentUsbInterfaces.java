package tech.derbent.bab.dashboard.dashboardinterfaces.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dashboardinterfaces.dto.CDTOUsbDevice;
import tech.derbent.bab.dashboard.dashboardinterfaces.service.CInterfaceDataCalimeroClient;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.base.session.service.ISessionService;

/**
 * CComponentUsbInterfaces - Component for displaying USB device information.
 * <p>
 * Displays USB devices for BAB Gateway projects with real-time data from Calimero server.
 * Shows USB device information including:
 * <ul>
 * <li>Device name and vendor information</li>
 * <li>USB port and bus assignments</li>
 * <li>Vendor/Product ID (VID:PID)</li>
 * <li>Device class and driver information</li>
 * <li>Connection speed and status</li>
 * </ul>
 * <p>
 * Uses CInterfaceDataCalimeroClient to fetch USB device data via getUsbDevices operation.
 */
public class CComponentUsbInterfaces extends CComponentInterfaceBase {

	public static final String ID_GRID = "custom-usb-interfaces-grid";
	public static final String ID_REFRESH_BUTTON = "custom-usb-refresh-button";
	public static final String ID_ROOT = "custom-usb-interfaces-component";
	public static final String ID_DETAILS_BUTTON = "custom-usb-details-button";

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentUsbInterfaces.class);
	private static final long serialVersionUID = 1L;

	// UI Components
	private CButton buttonDetails;
	private CGrid<CDTOUsbDevice> grid;

	public CComponentUsbInterfaces(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	@Override
	protected void addAdditionalToolbarButtons(final CHorizontalLayout toolbarLayout) {
		buttonDetails = new CButton("Device Details", VaadinIcon.INFO_CIRCLE.create());
		buttonDetails.setId(ID_DETAILS_BUTTON);
		buttonDetails.addThemeVariants(ButtonVariant.LUMO_SMALL);
		buttonDetails.setEnabled(false); // Enabled when device selected
		buttonDetails.addClickListener(e -> on_buttonDetails_clicked());
		toolbarLayout.add(buttonDetails);
	}

	private void configureGridColumns() {
		// Port column
		grid.addColumn(CDTOUsbDevice::getPort)
			.setHeader("Port")
			.setWidth("80px")
			.setFlexGrow(0)
			.setSortable(true)
			.setResizable(true);

		// Device name column
		grid.addColumn(CDTOUsbDevice::getName)
			.setHeader("Device Name")
			.setWidth("300px")
			.setFlexGrow(1)
			.setSortable(true)
			.setResizable(true);

		// VID:PID column
		grid.addColumn(CDTOUsbDevice::getVendorProductId)
			.setHeader("VID:PID")
			.setWidth("100px")
			.setFlexGrow(0)
			.setSortable(true)
			.setResizable(true);

		// Class column
		grid.addColumn(CDTOUsbDevice::getDeviceClass)
			.setHeader("Class")
			.setWidth("120px")
			.setFlexGrow(0)
			.setSortable(true)
			.setResizable(true);

		// Driver column
		grid.addColumn(CDTOUsbDevice::getDriver)
			.setHeader("Driver")
			.setWidth("150px")
			.setFlexGrow(0)
			.setSortable(true)
			.setResizable(true);

		// Speed column with colored indicator
		grid.addComponentColumn(device -> {
			final CSpan speedSpan = new CSpan(device.getSpeed());
			speedSpan.getStyle().set("padding", "4px 8px");
			speedSpan.getStyle().set("border-radius", "12px");
			speedSpan.getStyle().set("font-size", "0.8em");
			speedSpan.getStyle().set("font-weight", "bold");
			
			final String color = device.getStatusColor();
			speedSpan.getStyle().set("background", color + "20"); // 20% opacity
			speedSpan.getStyle().set("color", color);
			
			return speedSpan;
		}).setHeader("Speed")
		  .setWidth("80px")
		  .setFlexGrow(0)
		  .setSortable(false)
		  .setResizable(true);
	}

	private void createGrid() {
		grid = new CGrid<>(CDTOUsbDevice.class);
		grid.setId(ID_GRID);
		
		// Configure columns for USB device display
		configureGridColumns();
		
		// Selection listener for Details button
		grid.asSingleSelect().addValueChangeListener(e -> buttonDetails.setEnabled(e.getValue() != null));
		
		add(grid);
	}

	@Override
	protected String getHeaderText() {
		return "USB Devices";
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
		
		// Create standard toolbar with refresh and additional buttons
		add(createStandardToolbar());
		
		// Create grid
		createGrid();
		
		// Load initial data
		loadUsbDeviceData();
	}

	private void loadUsbDeviceData() {
		try {
			hideCalimeroUnavailableWarning();
			
			// Check if interface data is available
			if (!isInterfaceDataAvailable()) {
				showInterfaceDataUnavailableWarning();
				updateSummary(null);
				grid.setItems();
				return;
			}
			
			final Optional<CInterfaceDataCalimeroClient> clientOpt = getInterfaceDataClient();
			if (clientOpt.isEmpty()) {
				showInterfaceDataUnavailableWarning();
				updateSummary(null);
				grid.setItems();
				return;
			}
			
			// Fetch USB devices from Calimero
			final CCalimeroResponse response = clientOpt.get().getUsbDevices();
			
			if (response.isSuccess()) {
				final List<CDTOUsbDevice> devices = response.getDataField("devices", new ArrayList<>());
				grid.setItems(devices);
				
				// Update summary
				final long highSpeedDevices = devices.stream()
					.filter(CDTOUsbDevice::isHighSpeed)
					.count();
				final long devicesWithDrivers = devices.stream()
					.filter(device -> !device.getDriver().isEmpty() && !"unknown".equals(device.getDriver()))
					.count();
					
				updateSummary("%d devices (%d high-speed, %d with drivers)".formatted(devices.size(), highSpeedDevices, devicesWithDrivers));
				
				LOGGER.debug("Loaded {} USB devices ({} high-speed, {} with drivers)",
					devices.size(), highSpeedDevices, devicesWithDrivers);
			} else {
				CNotificationService.showError("Failed to load USB devices: " + response.getErrorMessage());
				grid.setItems();
				updateSummary(null);
			}
			
		} catch (final Exception e) {
			LOGGER.error("Error loading USB device data", e);
			CNotificationService.showException("Failed to load USB devices", e);
			grid.setItems();
			updateSummary(null);
		}
	}

	private void on_buttonDetails_clicked() {
		final CDTOUsbDevice selectedDevice = grid.asSingleSelect().getValue();
		if (selectedDevice == null) {
			return;
		}
		final String details = "Device: %s%nPort: %s%nBus: %s Device: %s%nVendor ID: %s%nProduct ID: %s%nClass: %s%nDriver: %s%nSpeed: %s%nPath: %s".formatted(selectedDevice.getName(), selectedDevice.getPort(), selectedDevice.getBus(), selectedDevice.getDevice(), selectedDevice.getVendorId(), selectedDevice.getProductId(), selectedDevice.getDeviceClass(), selectedDevice.getDriver(), selectedDevice.getSpeed(), selectedDevice.getDevicePath());
		CNotificationService.showInfo("USB Device Details:\n" + details);
	}

	@Override
	protected void refreshComponent() {
		LOGGER.debug("Refreshing USB device data");
		loadUsbDeviceData();
	}
}