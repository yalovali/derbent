package tech.derbent.bab.policybase.node.can;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;

/** CPageServiceCanNode - Page service for CAN Bus nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent pattern: Page
 * service for dynamic page management. Handles CAN node page operations and UI integration. Note: View is nullable as polymorphic node entities may
 * not have dedicated views. */
@Service
@Profile ("bab")
public class CPageServiceCanNode extends CPageServiceDynamicPage<CBabCanNode> {
	public CPageServiceCanNode(@Nullable final IPageServiceImplementer<CBabCanNode> view) {
		super(view);
	}

	/** Get available CAN protocol types for ComboBox data provider. Following existing pattern: Data source method for @AMetaData dataProviderMethod.
	 * @return List of supported CAN protocol types (XCP, UDS) */
	public List<String> getAvailableProtocolTypes() { return List.of("XCP", "UDS", "DBC"); }

	public List<Integer> getAvailableBitrates() {
		return List.of(125000, 250000, 500000, 1000000);
	}
}
