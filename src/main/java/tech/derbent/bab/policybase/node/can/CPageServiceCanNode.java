package tech.derbent.bab.policybase.node.can;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CComboBoxOption;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterCANService;
import tech.derbent.bab.policybase.node.can.view.CComponentCanPolicyFilters;
import tech.derbent.bab.policybase.node.can.view.CComponentCanProtocolFileData;

/** CPageServiceCanNode - Page service for CAN Bus nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent pattern: Page
 * service for dynamic page management. Handles CAN node page operations and UI integration. Note: View is nullable as polymorphic node entities may
 * not have dedicated views. */
@Service
@Profile ("bab")
public class CPageServiceCanNode extends CPageServiceDynamicPage<CBabCanNode> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceCanNode.class);

	public CPageServiceCanNode(@Nullable final IPageServiceImplementer<CBabCanNode> view) {
		super(view);
	}

	/** Creates protocol file data component for CAN node.
	 * @return custom component for upload/delete of in-memory protocol file data */
	public Component createComponentProtocolFileData() {
		try {
			final CBabCanNodeService canNodeService = CSpringContext.getBean(CBabCanNodeService.class);
			final CComponentCanProtocolFileData component = new CComponentCanProtocolFileData(canNodeService);
			registerComponent(component.getComponentName(), component);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating CAN protocol file data component: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load CAN protocol file component", e);
			return CDiv.errorDiv("Failed to load CAN protocol file component: " + e.getMessage());
		}
	}

	/** Creates CAN policy filter management component scoped to current CAN node. */
	public Component createComponentCanPolicyFilters() {
		try {
			final CBabPolicyFilterCANService filterService = CSpringContext.getBean(CBabPolicyFilterCANService.class);
			final CBabCanNodeService canNodeService = CSpringContext.getBean(CBabCanNodeService.class);
			final CComponentCanPolicyFilters component = new CComponentCanPolicyFilters(filterService, canNodeService);
			registerComponent(component.getComponentName(), component);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating CAN policy filters component: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load CAN policy filters component", e);
			return CDiv.errorDiv("Failed to load CAN policy filters component: " + e.getMessage());
		}
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
