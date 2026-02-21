package tech.derbent.bab.policybase.actionmask.service;

import org.springframework.context.annotation.Profile;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskCAN;

@Profile ("bab")
public class CPageServiceBabPolicyActionMaskCAN extends CPageServiceDynamicPage<CBabPolicyActionMaskCAN> {

	public CPageServiceBabPolicyActionMaskCAN(final IPageServiceImplementer<CBabPolicyActionMaskCAN> view) {
		super(view);
	}
}
