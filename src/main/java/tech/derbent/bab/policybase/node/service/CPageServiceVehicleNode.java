package tech.derbent.bab.policybase.node.service;

import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.node.domain.CVehicleNode;

/** CPageServiceVehicleNode - Page service for Vehicle nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent pattern:
 * Page service for dynamic page management. Handles Vehicle node page operations and UI integration. Note: View is nullable as polymorphic node
 * entities may not have dedicated views. */
@Service
@Profile ("bab")
public class CPageServiceVehicleNode extends CPageServiceDynamicPage<CVehicleNode> {

	public CPageServiceVehicleNode(@Nullable final IPageServiceImplementer<CVehicleNode> view) {
		super(view);
	}
}
