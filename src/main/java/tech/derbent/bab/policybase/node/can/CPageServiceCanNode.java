package tech.derbent.bab.policybase.node.can;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CComboBoxOption;

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
	public List<CComboBoxOption> getComboValuesOfProtocolType() {
		return List.of(new CComboBoxOption("XCP", "#FB8C00", "vaadin:chart"), new CComboBoxOption("UDS", "#6D4C41", "vaadin:ambulance"),
				new CComboBoxOption("DBC", "#5E35B1", "vaadin:file-tree"));
	}

	public List<Integer> getComboValuesOfBitrate() {
		return List.of(250000, 500000, 1000000);
	}
}
