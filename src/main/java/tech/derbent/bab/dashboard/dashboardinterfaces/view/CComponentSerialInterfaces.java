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
import tech.derbent.bab.dashboard.dashboardinterfaces.dto.CDTOSerialPort;
import tech.derbent.bab.dashboard.dashboardinterfaces.service.CInterfaceDataCalimeroClient;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.base.session.service.ISessionService;

/** CComponentSerialInterfaces - Component for displaying and configuring Serial interface settings.
 * <p>
 * Displays Serial interfaces for BAB Gateway projects with real-time data from Calimero server. Shows serial port information including:
 * <ul>
 * <li>Port device name and description</li>
 * <li>Serial port type (UART, USB, etc.)</li>
 * <li>Vendor and manufacturer information</li>
 * <li>Availability status</li>
 * <li>Port configuration options</li>
 * </ul>
 * <p>
 * Uses CInterfaceDataCalimeroClient to fetch serial port data via getSerialPorts operation. */
public class CComponentSerialInterfaces extends CComponentInterfaceBase {

	public static final String ID_CONFIGURE_BUTTON = "custom-serial-configure-button";
	public static final String ID_GRID = "custom-serial-interfaces-grid";
	public static final String ID_REFRESH_BUTTON = "custom-serial-refresh-button";
	public static final String ID_ROOT = "custom-serial-interfaces-component";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentSerialInterfaces.class);
	private static final long serialVersionUID = 1L;
	// UI Components
	private CButton buttonConfigure;
	private CGrid<CDTOSerialPort> grid;

	public CComponentSerialInterfaces(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	@Override
	protected void addAdditionalToolbarButtons(final CHorizontalLayout toolbarLayout) {
		buttonConfigure = new CButton("Configure", VaadinIcon.COG.create());
		buttonConfigure.setId(ID_CONFIGURE_BUTTON);
		buttonConfigure.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
		buttonConfigure.setEnabled(false); // Enabled when port selected
		buttonConfigure.addClickListener(e -> on_buttonConfigure_clicked());
		toolbarLayout.add(buttonConfigure);
	}

	private void configureGridColumns() {
		// Device column
		grid.addColumn(CDTOSerialPort::getDevice).setHeader("Device").setWidth("120px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Description column
		grid.addColumn(CDTOSerialPort::getDisplayName).setHeader("Description").setWidth("250px").setFlexGrow(1).setSortable(true).setResizable(true);
		// Type column
		grid.addColumn(CDTOSerialPort::getType).setHeader("Type").setWidth("100px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Vendor column
		grid.addColumn(CDTOSerialPort::getVendorInfo).setHeader("Vendor").setWidth("180px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Port path column
		grid.addColumn(CDTOSerialPort::getPort).setHeader("Port Path").setWidth("140px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Status column with colored indicator
		grid.addComponentColumn(port -> {
			final CSpan statusSpan = new CSpan(Boolean.TRUE.equals(port.getAvailable()) ? "Available" : "In Use");
			statusSpan.getStyle().set("padding", "4px 8px");
			statusSpan.getStyle().set("border-radius", "12px");
			statusSpan.getStyle().set("font-size", "0.8em");
			statusSpan.getStyle().set("font-weight", "bold");
			final String color = port.getStatusColor();
			statusSpan.getStyle().set("background", color + "20"); // 20% opacity
			statusSpan.getStyle().set("color", color);
			return statusSpan;
		}).setHeader("Status").setWidth("100px").setFlexGrow(0).setSortable(false).setResizable(true);
	}

	private void createGrid() {
		grid = new CGrid<CDTOSerialPort>(CDTOSerialPort.class);
		grid.setId(ID_GRID);
		// Configure columns for serial port display
		configureGridColumns();
		// Selection listener for Configure button
		grid.asSingleSelect().addValueChangeListener(e -> buttonConfigure.setEnabled(e.getValue() != null));
		add(grid);
	}

	@Override
	protected String getHeaderText() { return "Serial Interfaces"; }

	@Override
	protected String getRefreshButtonId() { return ID_REFRESH_BUTTON; }

	@Override
	public ISessionService getSessionService() { return sessionService; }

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
		loadSerialPortData();
	}

	private void loadSerialPortData() {
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
			// Fetch serial ports from Calimero
			final CCalimeroResponse response = clientOpt.get().getSerialPorts();
			if (response.isSuccess()) {
				final List<CDTOSerialPort> ports = response.getDataField("ports", new ArrayList<>());
				grid.setItems(ports);
				// Update summary
				final long availablePorts = ports.stream().filter(port -> Boolean.TRUE.equals(port.getAvailable())).count();
				final long usbSerialPorts = ports.stream().filter(CDTOSerialPort::isUsbSerial).count();
				updateSummary(String.format("%d ports (%d available, %d USB)", ports.size(), availablePorts, usbSerialPorts));
				LOGGER.debug("Loaded {} serial ports ({} available, {} USB)", ports.size(), availablePorts, usbSerialPorts);
			} else {
				CNotificationService.showError("Failed to load serial ports: " + response.getErrorMessage());
				grid.setItems();
				updateSummary(null);
			}
		} catch (final Exception e) {
			LOGGER.error("Error loading serial port data", e);
			CNotificationService.showException("Failed to load serial ports", e);
			grid.setItems();
			updateSummary(null);
		}
	}

	private void on_buttonConfigure_clicked() {
		final CDTOSerialPort selectedPort = grid.asSingleSelect().getValue();
		if (selectedPort != null) {
			CNotificationService.showInfo("Port configuration for " + selectedPort.getDevice() + " - Feature coming soon");
			// TODO: Open configuration dialog for selected serial port
		}
	}

	@Override
	protected void refreshComponent() {
		LOGGER.debug("Refreshing serial interfaces data");
		loadSerialPortData();
	}
}
