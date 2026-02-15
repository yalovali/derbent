package tech.derbent.bab.node.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CComboBoxOption;
import tech.derbent.bab.node.domain.CBabNodeModbus;

/** Page service for CBabNodeModbus entity. Following Derbent pattern: Concrete page service for Modbus node views. Provides UI action handling and
 * component lifecycle for Modbus node views. */
public class CPageServiceBabNodeModbus extends CPageServiceBabNode<CBabNodeModbus> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceBabNodeModbus.class);

	public CPageServiceBabNodeModbus(final IPageServiceImplementer<CBabNodeModbus> view) {
		super(view);
		LOGGER.debug("CPageServiceBabNodeModbus initialized for view: {}", view.getClass().getSimpleName());
	}

	public List<Integer> getComboValuesOfBaudRate() {
		return List.of(1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200);
	}

	public List<Integer> getComboValuesOfDataBits() {
		return List.of(7, 8);
	}

	public List<CComboBoxOption> getComboValuesOfParityType() {
		return List.of(new CComboBoxOption("None", "#616161", "vaadin:minus-circle"),
					new CComboBoxOption("Even", "#3949AB", "vaadin:check-circle"), new CComboBoxOption("Odd", "#D81B60", "vaadin:circle"));
	}

	public List<CComboBoxOption> getComboValuesOfProtocolType() {
		return List.of(new CComboBoxOption("RTU", "#1565C0", "vaadin:usb"), new CComboBoxOption("TCP", "#1976D2", "vaadin:exchange"));
	}

	public List<Integer> getComboValuesOfStopBits() {
		return List.of(1, 2);
	}
}
