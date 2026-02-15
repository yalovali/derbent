package tech.derbent.bab.policybase.node.ip;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;

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

	public List<String> getAvailableProtocolTypes() {
		return List.of("UDP", "TCP");
	}

	public List<String> getAvailableSyslogFacilities() {
		return List.of("KERN", "USER", "LOCAL0", "LOCAL1", "LOCAL2", "LOCAL3", "LOCAL4", "LOCAL5", "LOCAL6", "LOCAL7");
	}

	public List<String> getAvailableSeverityLevels() {
		return List.of("DEBUG", "INFO", "NOTICE", "WARNING", "ERROR", "CRIT", "ALERT", "EMERG");
	}
}
