package tech.derbent.bab.policybase.actionmask.service;

import org.springframework.context.annotation.Profile;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskROS;

@Profile ("bab")
public class CPageServiceBabPolicyActionMaskROS extends CPageServiceDynamicPage<CBabPolicyActionMaskROS> {

	public CPageServiceBabPolicyActionMaskROS(final IPageServiceImplementer<CBabPolicyActionMaskROS> view) {
		super(view);
	}
}
