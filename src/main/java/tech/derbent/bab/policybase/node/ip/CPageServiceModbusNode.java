package tech.derbent.bab.policybase.node.ip;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CComboBoxOption;
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

	public List<Integer> getComboValuesOfBaudRate() {
		return List.of(1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200);
	}

	public List<Integer> getComboValuesOfDataBits() {
		return List.of(7, 8);
	}

	public List<CComboBoxOption> getComboValuesOfModbusMode() {
		return List.of(new CComboBoxOption("RTU", "#1565C0", "vaadin:usb"), new CComboBoxOption("ASCII", "#00897B", "vaadin:file-text"));
	}

	public List<CComboBoxOption> getComboValuesOfParityType() {
		return List.of(new CComboBoxOption("NONE", "#616161", "vaadin:minus-circle"),
				new CComboBoxOption("EVEN", "#3949AB", "vaadin:check-circle"), new CComboBoxOption("ODD", "#D81B60", "vaadin:circle"));
	}

	public List<Integer> getComboValuesOfStopBits() {
		return List.of(1, 2);
	}

	public List<Integer> getComboValuesOfTimeoutMs() {
		return List.of(100, 250, 500, 1000, 2000, 5000, 10000);
	}
}
