package tech.derbent.bab.policybase.node.service;

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
}
