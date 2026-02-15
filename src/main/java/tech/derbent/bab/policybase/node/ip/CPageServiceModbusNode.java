package tech.derbent.bab.policybase.node.ip;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.node.domain.CBabModbusNode;

/**
 * CPageServiceModbusNode - Page service for Modbus nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Page service for dynamic page management.
 * 
 * Handles Modbus node page operations and UI integration.
 * Note: View is nullable as polymorphic node entities may not have dedicated views.
 */
@Service
@Profile("bab")
public class CPageServiceModbusNode extends CPageServiceDynamicPage<CBabModbusNode> {
	
	public CPageServiceModbusNode(@Nullable final IPageServiceImplementer<CBabModbusNode> view) {
		super(view);
	}

	public List<Integer> getAvailableBaudrates() {
		return List.of(1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200);
	}

	public List<Integer> getAvailableDataBits() {
		return List.of(7, 8);
	}

	public List<String> getAvailableModbusModes() {
		return List.of("RTU", "ASCII");
	}

	public List<String> getAvailableParityTypes() {
		return List.of("NONE", "EVEN", "ODD");
	}

	public List<Integer> getAvailableStopBits() {
		return List.of(1, 2);
	}

	public List<Integer> getAvailableTimeoutMs() {
		return List.of(100, 250, 500, 1000, 2000, 5000, 10000);
	}
}
