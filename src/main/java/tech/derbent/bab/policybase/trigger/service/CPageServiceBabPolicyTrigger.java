package tech.derbent.bab.policybase.trigger.service;

import java.util.List;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.trigger.domain.CBabPolicyTrigger;

public class CPageServiceBabPolicyTrigger extends CPageServiceDynamicPage<CBabPolicyTrigger> {

	public CPageServiceBabPolicyTrigger(final IPageServiceImplementer<CBabPolicyTrigger> view) {
		super(view);
	}

	public List<String> getComboValuesOfTriggerType() {
		return List.of(CBabPolicyTrigger.TRIGGER_TYPE_PERIODIC, CBabPolicyTrigger.TRIGGER_TYPE_AT_START, CBabPolicyTrigger.TRIGGER_TYPE_MANUAL,
				CBabPolicyTrigger.TRIGGER_TYPE_ALWAYS, CBabPolicyTrigger.TRIGGER_TYPE_ONCE);
	}

	public List<Integer> getComboValuesOfTimeoutSeconds() {
		return List.of(1, 2, 5, 10, 15, 30, 60, 120, 300);
	}
}
