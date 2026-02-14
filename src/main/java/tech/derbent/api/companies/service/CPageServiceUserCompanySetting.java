package tech.derbent.api.companies.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.users.domain.CUserCompanySetting;

public class CPageServiceUserCompanySetting extends CPageServiceDynamicPage<CUserCompanySetting> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceUserCompanySetting.class);
	Long serialVersionUID = 1L;

	public CPageServiceUserCompanySetting(IPageServiceImplementer<CUserCompanySetting> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CUserCompanySetting");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CUserCompanySetting> gridView = (CGridViewBaseDBEntity<CUserCompanySetting>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
