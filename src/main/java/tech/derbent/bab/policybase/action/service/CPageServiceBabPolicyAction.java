package tech.derbent.bab.policybase.action.service;

import java.util.List;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;

public class CPageServiceBabPolicyAction extends CPageServiceDynamicPage<CBabPolicyAction> {

	public CPageServiceBabPolicyAction(final IPageServiceImplementer<CBabPolicyAction> view) {
		super(view);
	}

	public List<String> getAvailableActionTypes() {
		return List.of(CBabPolicyAction.ACTION_TYPE_FORWARD, CBabPolicyAction.ACTION_TYPE_TRANSFORM, CBabPolicyAction.ACTION_TYPE_STORE,
				CBabPolicyAction.ACTION_TYPE_NOTIFY, CBabPolicyAction.ACTION_TYPE_EXECUTE, CBabPolicyAction.ACTION_TYPE_FILTER,
				CBabPolicyAction.ACTION_TYPE_VALIDATE, CBabPolicyAction.ACTION_TYPE_LOG);
	}

	public List<Integer> getAvailableTimeoutSeconds() {
		return List.of(1, 2, 5, 10, 15, 30, 60, 120, 300);
	}
}
