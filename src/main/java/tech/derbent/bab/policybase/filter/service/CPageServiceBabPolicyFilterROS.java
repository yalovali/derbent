package tech.derbent.bab.policybase.filter.service;

import org.springframework.context.annotation.Profile;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterROS;

@Profile ("bab")
public class CPageServiceBabPolicyFilterROS extends CPageServiceDynamicPage<CBabPolicyFilterROS> {

	public CPageServiceBabPolicyFilterROS(final IPageServiceImplementer<CBabPolicyFilterROS> view) {
		super(view);
	}
}
