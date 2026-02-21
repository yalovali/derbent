package tech.derbent.bab.policybase.actionmask.service;

import java.util.List;
import org.springframework.context.annotation.Profile;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskFile;

@Profile ("bab")
public class CPageServiceBabPolicyActionMaskFile extends CPageServiceDynamicPage<CBabPolicyActionMaskFile> {

	public CPageServiceBabPolicyActionMaskFile(final IPageServiceImplementer<CBabPolicyActionMaskFile> view) {
		super(view);
	}

	public List<String> getComboValuesOfSerializationMode() {
		return List.of("JSON_APPEND", "CSV_APPEND", "ROLLING_ARCHIVE");
	}
}
