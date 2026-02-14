package tech.derbent.api.users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.users.domain.CUserProjectSettings;

public class CPageServiceUserProjectSettings extends CPageServiceDynamicPage<CUserProjectSettings> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceUserProjectSettings.class);
	Long serialVersionUID = 1L;

	public CPageServiceUserProjectSettings(IPageServiceImplementer<CUserProjectSettings> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CUserProjectSettings");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CUserProjectSettings> gridView = (CGridViewBaseDBEntity<CUserProjectSettings>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
