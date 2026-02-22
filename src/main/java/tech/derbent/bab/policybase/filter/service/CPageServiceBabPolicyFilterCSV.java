package tech.derbent.bab.policybase.filter.service;

import java.util.List;
import org.springframework.context.annotation.Profile;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCSV;

@Profile ("bab")
public class CPageServiceBabPolicyFilterCSV extends CPageServiceDynamicPage<CBabPolicyFilterCSV> {

	public CPageServiceBabPolicyFilterCSV(final IPageServiceImplementer<CBabPolicyFilterCSV> view) {
		super(view);
	}

	public List<String> getComboValuesOfColumnSeparator() {
		return List.of(CBabPolicyFilterCSV.COLUMN_SEPARATOR_SEMICOLON, CBabPolicyFilterCSV.COLUMN_SEPARATOR_COMMA);
	}

}
