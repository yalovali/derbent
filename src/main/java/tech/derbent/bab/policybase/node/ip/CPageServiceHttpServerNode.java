package tech.derbent.bab.policybase.node.ip;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;

/**
 * CPageServiceHttpServerNode - Page service for HTTP Server nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Page service for dynamic page management.
 * 
 * Handles HTTP Server node page operations and UI integration.
 * Note: View is nullable as polymorphic node entities may not have dedicated views.
 */
@Service
@Profile("bab")
public class CPageServiceHttpServerNode extends CPageServiceDynamicPage<CBabHttpServerNode> {
	
	public CPageServiceHttpServerNode(@Nullable final IPageServiceImplementer<CBabHttpServerNode> view) {
		super(view);
	}

	public List<String> getAvailableProtocolTypes() {
		return List.of("HTTP", "HTTPS");
	}

	public List<Integer> getAvailableServerPorts() {
		return List.of(80, 443, 8080, 8443);
	}

	public List<Integer> getAvailableTimeoutSeconds() {
		return List.of(5, 10, 15, 30, 60, 120, 300);
	}
}
