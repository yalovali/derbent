package tech.derbent.bab.policybase.filter.service;

import java.util.List;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilter;

public class CPageServiceBabPolicyFilter extends CPageServiceDynamicPage<CBabPolicyFilter> {

	public CPageServiceBabPolicyFilter(final IPageServiceImplementer<CBabPolicyFilter> view) {
		super(view);
	}

	public List<String> getAvailableFilterTypes() {
		return List.of(CBabPolicyFilter.FILTER_TYPE_CSV, CBabPolicyFilter.FILTER_TYPE_JSON, CBabPolicyFilter.FILTER_TYPE_XML,
				CBabPolicyFilter.FILTER_TYPE_REGEX, CBabPolicyFilter.FILTER_TYPE_RANGE, CBabPolicyFilter.FILTER_TYPE_CONDITION,
				CBabPolicyFilter.FILTER_TYPE_TRANSFORM, CBabPolicyFilter.FILTER_TYPE_VALIDATE);
	}

	public List<String> getAvailableLogicOperators() {
		return List.of("AND", "OR", "NOT");
	}

	public List<String> getAvailableNullHandlingStrategies() {
		return List.of("ignore", "reject", "pass", "default");
	}
}
