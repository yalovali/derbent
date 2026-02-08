package tech.derbent.bab.policybase.node.service;

import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.node.domain.CBabSyslogNode;

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
}
