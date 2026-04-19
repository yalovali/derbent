package tech.derbent.bab.policybase.filter.service;

import org.springframework.context.annotation.Profile;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterROS;

@Profile ("bab")
public class CPageServiceBabPolicyFilterROS extends CPageServiceDynamicPage<CBabPolicyFilterROS> {

	public CPageServiceBabPolicyFilterROS(final IPageServiceImplementer<CBabPolicyFilterROS> view) {
		super(view);
	}

	@Override
	public void actionCreate() {
		CNotificationService.showWarning("Policy filters are created from nodes. Please select a node and add filters from there.");
	}

	@Override
	protected void configureToolbar(final CCrudToolbar toolbar) {
		toolbar.configureButtonVisibility(false, true, true, true);
	}
}
