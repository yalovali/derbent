package tech.derbent.bab.policybase.filter.service;

import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilter;

public class CPageServiceBabPolicyFilter extends CPageServiceDynamicPage<CBabPolicyFilter> {

	public CPageServiceBabPolicyFilter(final IPageServiceImplementer<CBabPolicyFilter> view) {
		super(view);
	}
}
