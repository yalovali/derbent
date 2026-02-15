package tech.derbent.bab.node.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.node.domain.CBabNodeModbus;

/** Page service for CBabNodeModbus entity. Following Derbent pattern: Concrete page service for Modbus node views. Provides UI action handling and
 * component lifecycle for Modbus node views. */
public class CPageServiceBabNodeModbus extends CPageServiceBabNode<CBabNodeModbus> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceBabNodeModbus.class);

	public CPageServiceBabNodeModbus(final IPageServiceImplementer<CBabNodeModbus> view) {
		super(view);
		LOGGER.debug("CPageServiceBabNodeModbus initialized for view: {}", view.getClass().getSimpleName());
	}

	public List<Integer> getAvailableBaudRates() {
		return List.of(1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200);
	}

	public List<String> getAvailableParityTypes() {
		return List.of("None", "Even", "Odd");
	}

	public List<String> getAvailableProtocolTypes() {
		return List.of("RTU", "TCP");
	}
}
