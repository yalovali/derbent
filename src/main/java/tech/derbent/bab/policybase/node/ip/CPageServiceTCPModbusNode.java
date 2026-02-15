package tech.derbent.bab.policybase.node.ip;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.node.modbus.CBabTCPModbusNode;

/**
 * CPageServiceTCPModbusNode - Page service for TCP Modbus nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Page service for dynamic page management.
 * 
 * Handles TCP Modbus node page operations and UI integration.
 * Note: View is nullable as polymorphic node entities may not have dedicated views.
 */
@Service
@Profile("bab")
public class CPageServiceTCPModbusNode extends CPageServiceDynamicPage<CBabTCPModbusNode> {
	
	public CPageServiceTCPModbusNode(@Nullable final IPageServiceImplementer<CBabTCPModbusNode> view) {
		super(view);
	}

	public List<Integer> getAvailableServerPorts() {
		return List.of(502, 1502, 8502);
	}

	public List<Integer> getAvailableConnectionTimeoutMs() {
		return List.of(500, 1000, 2000, 5000, 10000, 30000);
	}

	public List<Integer> getAvailableResponseTimeoutMs() {
		return List.of(100, 250, 500, 1000, 2000, 5000);
	}
}
