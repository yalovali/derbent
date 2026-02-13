package tech.derbent.bab.policybase.action.service;

import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;

public class CPageServiceBabPolicyAction extends CPageServiceDynamicPage<CBabPolicyAction> {

	public CPageServiceBabPolicyAction(final IPageServiceImplementer<CBabPolicyAction> view) {
		super(view);
	}
}
