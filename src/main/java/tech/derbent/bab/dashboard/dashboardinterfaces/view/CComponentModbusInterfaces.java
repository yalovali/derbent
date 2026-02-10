package tech.derbent.bab.dashboard.dashboardinterfaces.view;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.session.service.ISessionService;

/** CComponentModbusInterfaces - Component for displaying and configuring Modbus interface settings.
 * <p>
 * Displays Modbus interfaces for BAB Gateway projects with configuration options. Shows Modbus device information including:
 * <ul>
 * <li>Modbus device addresses and types</li>
 * <li>Connection status and protocol settings</li>
 * <li>Register mappings and data points</li>
 * <li>Communication timeouts and error rates</li>
 * </ul>
 * <p>
 * Currently shows sample data structure - will be enhanced with real Calimero API integration. */
public class CComponentModbusInterfaces extends CComponentInterfaceBase {

	// Simple data structure for demonstration
	public static class ModbusDevice {

		public String address;
		public String protocol;
		public String status;
		public String type;

		public ModbusDevice(String address, String type, String status, String protocol) {
			this.address = address;
			this.type = type;
			this.status = status;
			this.protocol = protocol;
		}
	}

	public static final String ID_CONFIG_BUTTON = "custom-modbus-config-button";
	public static final String ID_GRID = "custom-modbus-interfaces-grid";
	public static final String ID_REFRESH_BUTTON = "custom-modbus-refresh-button";
	public static final String ID_ROOT = "custom-modbus-interfaces-component";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentModbusInterfaces.class);
	private static final long serialVersionUID = 1L;
	// UI Components
	private CButton buttonConfig;
	private CGrid<ModbusDevice> grid;

	public CComponentModbusInterfaces(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	@Override
	protected void addAdditionalToolbarButtons(final CHorizontalLayout toolbarLayout) {
		buttonConfig = new CButton("Configure", VaadinIcon.COG.create());
		buttonConfig.setId(ID_CONFIG_BUTTON);
		buttonConfig.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
		buttonConfig.setEnabled(false); // Enabled when device selected
		buttonConfig.addClickListener(e -> on_buttonConfig_clicked());
		toolbarLayout.add(buttonConfig);
	}

	@Override
	protected void configureComponent() {
		super.configureComponent();
		createGrid();
	}

	private void configureGridColumns() {
		// Address column
		grid.addColumn(device -> device.address).setHeader("Address").setWidth("100px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Type column
		grid.addColumn(device -> device.type).setHeader("Device Type").setWidth("150px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Protocol column
		grid.addColumn(device -> device.protocol).setHeader("Protocol").setWidth("120px").setFlexGrow(0).setSortable(true).setResizable(true);
		// Status column with colored indicator
		grid.addComponentColumn(device -> {
			final CSpan statusSpan = new CSpan(device.status);
			statusSpan.getStyle().set("padding", "4px 8px");
			statusSpan.getStyle().set("border-radius", "12px");
			statusSpan.getStyle().set("font-size", "0.8em");
			statusSpan.getStyle().set("font-weight", "bold");
			if ("Connected".equals(device.status)) {
				statusSpan.getStyle().set("background", "var(--lumo-success-color-10pct)");
				statusSpan.getStyle().set("color", "var(--lumo-success-color)");
			} else {
				statusSpan.getStyle().set("background", "var(--lumo-error-color-10pct)");
				statusSpan.getStyle().set("color", "var(--lumo-error-color)");
			}
			return statusSpan;
		}).setHeader("Status").setWidth("120px").setFlexGrow(0).setSortable(false).setResizable(true);
	}

	private void createGrid() {
		grid = new CGrid<ModbusDevice>(ModbusDevice.class);
		grid.setId(ID_GRID);
		// Configure columns for Modbus device display
		configureGridColumns();
		// Selection listener for Config button
		grid.asSingleSelect().addValueChangeListener(e -> buttonConfig.setEnabled(e.getValue() != null));
		add(grid);
	}

	@Override
	protected String getHeaderText() { return "Modbus Interfaces"; }

	@Override
	protected String getID_ROOT() { // TODO Auto-generated method stub
		return ID_ROOT;
	}

	@Override
	protected String getRefreshButtonId() { return ID_REFRESH_BUTTON; }

	@Override
	protected boolean hasRefreshButton() {
		return false; // Page-level refresh used
	}

	private void on_buttonConfig_clicked() {
		final ModbusDevice selectedDevice = grid.asSingleSelect().getValue();
		if (selectedDevice != null) {
			CNotificationService.showInfo("Modbus configuration for device " + selectedDevice.address + " - Feature coming soon");
			// TODO: Open Modbus configuration dialog
		}
	}

	@Override
	protected void refreshComponent() {
		LOGGER.debug("🔄 Refreshing Modbus interfaces component");
		try {
			// Sample data - will be replaced with real Calimero API integration
			final List<ModbusDevice> devices = new ArrayList<>();
			devices.add(new ModbusDevice("001", "Temperature Sensor", "Connected", "Modbus RTU"));
			devices.add(new ModbusDevice("002", "Flow Meter", "Connected", "Modbus TCP"));
			devices.add(new ModbusDevice("003", "Pressure Sensor", "Disconnected", "Modbus RTU"));
			grid.setItems(devices);
			final long connectedDevices = devices.stream().filter(device -> "Connected".equals(device.status)).count();
			updateSummary("%d devices (%d connected, %d offline)".formatted(devices.size(), connectedDevices, devices.size() - connectedDevices));
			LOGGER.debug("✅ Modbus interfaces component refreshed: {} devices ({} connected)", devices.size(), connectedDevices);
		} catch (final Exception e) {
			LOGGER.error("Error loading Modbus device data", e);
			CNotificationService.showException("Failed to load Modbus devices", e);
			grid.setItems();
			updateSummary(null);
		}
	}
}
