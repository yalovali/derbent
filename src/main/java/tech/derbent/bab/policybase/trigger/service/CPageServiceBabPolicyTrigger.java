package tech.derbent.bab.policybase.trigger.service;

import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.trigger.domain.CBabPolicyTrigger;

public class CPageServiceBabPolicyTrigger extends CPageServiceDynamicPage<CBabPolicyTrigger> {

	public CPageServiceBabPolicyTrigger(final IPageServiceImplementer<CBabPolicyTrigger> view) {
		super(view);
	}
}
