package tech.derbent.bab.policybase.filter.service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.context.annotation.Profile;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterBase;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCAN;
import tech.derbent.bab.policybase.node.can.CBabCanNode;
import tech.derbent.bab.policybase.node.can.CBabCanNodeService;

@Profile ("bab")
public class CPageServiceBabPolicyFilterCAN extends CPageServiceDynamicPage<CBabPolicyFilterCAN> {

	public CPageServiceBabPolicyFilterCAN(final IPageServiceImplementer<CBabPolicyFilterCAN> view) {
		super(view);
	}

	public List<String> getComboValuesOfLogicOperator() {
		return List.of(CBabPolicyFilterBase.LOGIC_OPERATOR_AND, CBabPolicyFilterBase.LOGIC_OPERATOR_OR, CBabPolicyFilterBase.LOGIC_OPERATOR_NOT);
	}

	public List<String> getComboValuesOfNullHandlingStrategy() {
		return List.of(CBabPolicyFilterBase.NULL_HANDLING_IGNORE, CBabPolicyFilterBase.NULL_HANDLING_REJECT,
				CBabPolicyFilterBase.NULL_HANDLING_PASS, CBabPolicyFilterBase.NULL_HANDLING_DEFAULT);
	}

	public List<String> getComboValuesOfProtocolVariableNames(final CBabPolicyFilterCAN entity) {
		if (entity == null) {
			return List.of();
		}

		final CBabCanNodeService canNodeService = CSpringContext.getBean(CBabCanNodeService.class);
		final Set<String> availableVariables = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		if (entity.getParentNode() instanceof final CBabCanNode parentCanNode) {
			String protocolJson = parentCanNode.getProtocolFileJson();
			if ((protocolJson == null || protocolJson.isBlank()) && parentCanNode.getId() != null) {
				protocolJson = canNodeService.loadProtocolContentFromDb(parentCanNode.getId(), CBabCanNodeService.EProtocolContentField.JSON);
			}
			availableVariables.addAll(canNodeService.extractProtocolVariableNames(protocolJson));
		}
		if (entity.getProtocolVariableNames() != null) {
			availableVariables.addAll(entity.getProtocolVariableNames());
		}
		return List.copyOf(availableVariables);
	}
}
