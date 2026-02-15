package tech.derbent.bab.policybase.node.ip;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CComboBoxOption;

/**
 * CPageServiceSyslogNode - Page service for Syslog nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Page service for dynamic page management.
 * 
 * Handles Syslog node page operations and UI integration.
 * Note: View is nullable as polymorphic node entities may not have dedicated views.
 */
@Service
@Profile("bab")
public class CPageServiceSyslogNode extends CPageServiceDynamicPage<CBabSyslogNode> {
	
	public CPageServiceSyslogNode(@Nullable final IPageServiceImplementer<CBabSyslogNode> view) {
		super(view);
	}

	public List<CComboBoxOption> getComboValuesOfProtocolType() {
		return List.of(new CComboBoxOption("UDP", "#607D8B", "vaadin:arrow-down"), new CComboBoxOption("TCP", "#1976D2", "vaadin:exchange"));
	}

	public List<CComboBoxOption> getComboValuesOfSyslogFacility() {
		return List.of(new CComboBoxOption("KERN", "#5D4037", "vaadin:cog"), new CComboBoxOption("USER", "#3949AB", "vaadin:user"),
				new CComboBoxOption("LOCAL0", "#6A1B9A", "vaadin:archive"), new CComboBoxOption("LOCAL1", "#00897B", "vaadin:archive"),
				new CComboBoxOption("LOCAL2", "#2E7D32", "vaadin:archive"), new CComboBoxOption("LOCAL3", "#EF6C00", "vaadin:archive"),
				new CComboBoxOption("LOCAL4", "#C62828", "vaadin:archive"), new CComboBoxOption("LOCAL5", "#0277BD", "vaadin:archive"),
				new CComboBoxOption("LOCAL6", "#AD1457", "vaadin:archive"), new CComboBoxOption("LOCAL7", "#455A64", "vaadin:archive"));
	}

	public List<CComboBoxOption> getComboValuesOfSeverityLevel() {
		return List.of(new CComboBoxOption("DEBUG", "#78909C", "vaadin:bug"), new CComboBoxOption("INFO", "#1E88E5", "vaadin:info-circle"),
				new CComboBoxOption("NOTICE", "#00897B", "vaadin:bell"), new CComboBoxOption("WARNING", "#F9A825", "vaadin:warning"),
				new CComboBoxOption("ERROR", "#E53935", "vaadin:close-circle"), new CComboBoxOption("CRIT", "#D32F2F", "vaadin:fire"),
				new CComboBoxOption("ALERT", "#C62828", "vaadin:alarm"), new CComboBoxOption("EMERG", "#B71C1C", "vaadin:bolt"));
	}
}
