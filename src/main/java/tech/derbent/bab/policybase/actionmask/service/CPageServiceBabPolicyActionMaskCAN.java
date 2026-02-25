package tech.derbent.bab.policybase.actionmask.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import com.vaadin.flow.component.Component;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskCAN;
import tech.derbent.bab.policybase.actionmask.view.CComponentActionMaskOutputActionMappings;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCAN;
import tech.derbent.bab.policybase.filter.domain.ROutputStructure;
import tech.derbent.bab.policybase.node.can.CBabCanNode;
import tech.derbent.bab.policybase.node.can.CBabCanNodeService;
import tech.derbent.bab.policybase.rule.domain.CBabPolicyRule;

@Profile ("bab")
public class CPageServiceBabPolicyActionMaskCAN extends CPageServiceBabPolicyActionMaskBase<CBabPolicyActionMaskCAN> {

	public record RDestinationProtocolVariable(String name, String dataType) {

		public RDestinationProtocolVariable {
			name = name == null ? "" : name.trim();
			dataType = dataType == null ? "" : dataType.trim();
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceBabPolicyActionMaskCAN.class);

	public CPageServiceBabPolicyActionMaskCAN(final IPageServiceImplementer<CBabPolicyActionMaskCAN> view) {
		super(view);
	}

	public Component createComponentOutputActionMappings() {
		try {
			final CComponentActionMaskOutputActionMappings component = new CComponentActionMaskOutputActionMappings(this);
			registerComponent(component.getComponentName(), component);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create output action mappings component for maskId={}. reason={}",
					getValue() != null ? getValue().getId() : null, e.getMessage());
			CNotificationService.showException("Failed to create output action mappings component", e);
			return CDiv.errorDiv("Failed to create output action mappings component: " + e.getMessage());
		}
	}

	public List<RDestinationProtocolVariable> getDestinationProtocolVariables(final CBabPolicyActionMaskCAN mask) {
		if (mask == null || !(mask.getDestinationNode() instanceof final CBabCanNode destinationCanNode)) {
			return List.of();
		}
		final CBabCanNodeService canNodeService = CSpringContext.getBean(CBabCanNodeService.class);
		String protocolJson = destinationCanNode.getProtocolFileJson();
		if ((protocolJson == null || protocolJson.isBlank()) && destinationCanNode.getId() != null) {
			protocolJson = canNodeService.loadProtocolContentFromDb(destinationCanNode.getId(), CBabCanNodeService.EProtocolContentField.JSON);
		}
		if (protocolJson == null || protocolJson.isBlank()) {
			return List.of();
		}
		final Map<String, ROutputStructure> outputByVariableName = CBabPolicyFilterCAN.getOutputStructureByVariableName(protocolJson);
		if (outputByVariableName.isEmpty()) {
			return List.of();
		}
		final Map<String, RDestinationProtocolVariable> uniqueVariablesByName = new LinkedHashMap<>();
		outputByVariableName.values().forEach(output -> {
			if (output == null || output.name().isBlank()) {
				return;
			}
			final String normalizedName = CBabPolicyFilterCAN.normalizeVariableName(output.name());
			uniqueVariablesByName.putIfAbsent(normalizedName, new RDestinationProtocolVariable(output.name(), output.dataType()));
		});
		return uniqueVariablesByName.values().stream().sorted(
				Comparator.comparing(RDestinationProtocolVariable::name, Comparator.nullsLast(String::compareToIgnoreCase)))
				.toList();
	}

	public List<String> getComboValuesOfOutputMethod() {
		return List.of(CBabPolicyActionMaskCAN.OUTPUT_METHOD_XCP_DOWNLOAD, CBabPolicyActionMaskCAN.OUTPUT_METHOD_XCP_UPLOAD);
	}

	public List<ROutputStructure> getSourceFilterOutputStructure(final CBabPolicyActionMaskCAN mask) {
		if (mask == null || mask.getPolicyAction() == null) {
			return List.of();
		}
		final CBabPolicyRule policyRule = mask.getPolicyAction().getPolicyRule();
		if (policyRule == null) {
			return List.of();
		}
		if (policyRule.getFilter() == null) {
			return List.of();
		}
		return policyRule.getFilter().getOutputStructure();
	}
}
