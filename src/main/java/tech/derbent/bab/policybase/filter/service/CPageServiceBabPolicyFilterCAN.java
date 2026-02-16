package tech.derbent.bab.policybase.filter.service;

import java.util.List;
import org.springframework.context.annotation.Profile;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterBase;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCAN;

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
}
