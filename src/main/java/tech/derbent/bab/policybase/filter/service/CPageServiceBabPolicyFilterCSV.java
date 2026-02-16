package tech.derbent.bab.policybase.filter.service;

import java.util.List;
import org.springframework.context.annotation.Profile;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterBase;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCSV;

@Profile ("bab")
public class CPageServiceBabPolicyFilterCSV extends CPageServiceDynamicPage<CBabPolicyFilterCSV> {

	public CPageServiceBabPolicyFilterCSV(final IPageServiceImplementer<CBabPolicyFilterCSV> view) {
		super(view);
	}

	public List<String> getComboValuesOfColumnSeparator() {
		return List.of(CBabPolicyFilterCSV.COLUMN_SEPARATOR_SEMICOLON, CBabPolicyFilterCSV.COLUMN_SEPARATOR_COMMA);
	}

	public List<String> getComboValuesOfLogicOperator() {
		return List.of(CBabPolicyFilterBase.LOGIC_OPERATOR_AND, CBabPolicyFilterBase.LOGIC_OPERATOR_OR, CBabPolicyFilterBase.LOGIC_OPERATOR_NOT);
	}

	public List<String> getComboValuesOfNullHandlingStrategy() {
		return List.of(CBabPolicyFilterBase.NULL_HANDLING_IGNORE, CBabPolicyFilterBase.NULL_HANDLING_REJECT,
				CBabPolicyFilterBase.NULL_HANDLING_PASS, CBabPolicyFilterBase.NULL_HANDLING_DEFAULT);
	}
}
