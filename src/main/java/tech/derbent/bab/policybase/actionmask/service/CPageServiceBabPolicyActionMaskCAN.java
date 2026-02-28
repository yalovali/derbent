package tech.derbent.bab.policybase.actionmask.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.springframework.context.annotation.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskCAN;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCAN;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterBase;
import tech.derbent.bab.policybase.actionmask.view.CComponentActionMaskOutputActionMappings;
import tech.derbent.bab.policybase.filter.domain.ROutputStructure;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterCANService;
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

	private CBabCanNode resolveDestinationCanNode(final CBabPolicyActionMaskCAN mask, final CBabCanNodeService canNodeService) {
		if (mask == null || mask.getDestinationNode() == null) {
			return null;
		}
		if (mask.getDestinationNode() instanceof final CBabCanNode destinationCanNode) {
			return destinationCanNode;
		}
		final Long destinationNodeId = mask.getDestinationNode().getId();
		if (destinationNodeId == null) {
			return null;
		}
		return canNodeService.getById(destinationNodeId).orElse(null);
	}

	private CBabCanNode resolveParentCanNode(final CBabPolicyFilterCAN sourceFilter, final CBabCanNodeService canNodeService) {
		if (sourceFilter == null || sourceFilter.getParentNode() == null) {
			return null;
		}
		if (sourceFilter.getParentNode() instanceof final CBabCanNode parentCanNode) {
			return parentCanNode;
		}
		final Long parentNodeId = sourceFilter.getParentNode().getId();
		if (parentNodeId == null) {
			return null;
		}
		return canNodeService.getById(parentNodeId).orElse(null);
	}

	private CBabPolicyFilterCAN resolveSourceCanFilter(final CBabPolicyRule policyRule) {
		if (policyRule == null || policyRule.getFilter() == null) {
			return null;
		}
		final CBabPolicyFilterBase<?> sourceFilter = policyRule.getFilter();
		if (sourceFilter instanceof final CBabPolicyFilterCAN canFilter) {
			return canFilter;
		}
		if (sourceFilter.getId() == null) {
			return null;
		}
		return CSpringContext.getBean(CBabPolicyFilterCANService.class).getById(sourceFilter.getId()).orElse(null);
	}

	private List<ROutputStructure> rebuildSourceOutputsFromFilterState(final CBabPolicyFilterCAN sourceFilter) {
		if (sourceFilter == null || sourceFilter.getProtocolVariableNames() == null || sourceFilter.getProtocolVariableNames().isEmpty()) {
			return List.of();
		}
		final CBabCanNodeService canNodeService = CSpringContext.getBean(CBabCanNodeService.class);
		final CBabCanNode parentCanNode = resolveParentCanNode(sourceFilter, canNodeService);
		if (parentCanNode == null) {
			return List.of();
		}
		String protocolJson = parentCanNode.getProtocolFileJson();
		if ((protocolJson == null || protocolJson.isBlank()) && parentCanNode.getId() != null) {
			protocolJson = canNodeService.loadProtocolContentFromDb(parentCanNode.getId(), CBabCanNodeService.EProtocolContentField.JSON);
		}
		if (protocolJson == null || protocolJson.isBlank()) {
			return List.of();
		}
		final Map<String, ROutputStructure> outputsByVariableName = CBabPolicyFilterCAN.getOutputStructureByVariableName(protocolJson);
		if (outputsByVariableName.isEmpty()) {
			return List.of();
		}
		final List<ROutputStructure> outputStructure = new ArrayList<>();
		sourceFilter.getProtocolVariableNames().stream().filter(variableName -> variableName != null && !variableName.isBlank())
				.forEach(variableName -> {
					final ROutputStructure resolvedOutput = outputsByVariableName.get(CBabPolicyFilterCAN.normalizeVariableName(variableName));
					if (resolvedOutput != null) {
						outputStructure.add(resolvedOutput);
					}
				});
		return outputStructure;
	}

	public List<RDestinationProtocolVariable> getDestinationProtocolVariables(final CBabPolicyActionMaskCAN mask) {
		final CBabCanNodeService canNodeService = CSpringContext.getBean(CBabCanNodeService.class);
		final CBabCanNode destinationCanNode = resolveDestinationCanNode(mask, canNodeService);
		if (destinationCanNode == null) {
			return List.of();
		}
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
		if (policyRule == null || policyRule.getFilter() == null) {
			return List.of();
		}
		final CBabPolicyFilterCAN sourceFilter = resolveSourceCanFilter(policyRule);
		LOGGER.debug("Loading source filter output structure maskId={} actionId={} ruleId={} filterId={} filterRuntimeClass={} filterResolvedClass={}",
				mask.getId(), mask.getPolicyAction().getId(), policyRule.getId(), policyRule.getFilter().getId(),
				policyRule.getFilter().getClass().getSimpleName(), sourceFilter != null ? sourceFilter.getClass().getSimpleName() : null);
		if (sourceFilter == null) {
			return List.of();
		}
		try {
			final List<ROutputStructure> outputStructure = sourceFilter.getOutputStructure();
			if (outputStructure != null && !outputStructure.isEmpty()) {
				return outputStructure;
			}
			return rebuildSourceOutputsFromFilterState(sourceFilter);
		} catch (final RuntimeException e) {
			LOGGER.error(
					"Failed loading source filter output structure maskId={} actionId={} ruleId={} filterId={} filterRuntimeClass={} filterResolvedClass={} reason={}",
					mask.getId(), mask.getPolicyAction().getId(), policyRule.getId(), policyRule.getFilter().getId(),
					policyRule.getFilter().getClass().getSimpleName(), sourceFilter.getClass().getSimpleName(), e.getMessage());
			return rebuildSourceOutputsFromFilterState(sourceFilter);
		}
	}
}
