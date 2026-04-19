package tech.derbent.bab.policybase.filter.service;

import java.util.List;
import org.springframework.context.annotation.Profile;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCSV;

@Profile ("bab")
public class CPageServiceBabPolicyFilterCSV extends CPageServiceDynamicPage<CBabPolicyFilterCSV> {

	public CPageServiceBabPolicyFilterCSV(final IPageServiceImplementer<CBabPolicyFilterCSV> view) {
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

	public List<String> getComboValuesOfColumnSeparator() {
		return List.of(CBabPolicyFilterCSV.COLUMN_SEPARATOR_SEMICOLON, CBabPolicyFilterCSV.COLUMN_SEPARATOR_COMMA);
	}

}
