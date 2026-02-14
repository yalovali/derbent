package tech.derbent.api.companies.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.roles.domain.CUserCompanyRole;

public class CPageServiceUserCompanyRole extends CPageServiceDynamicPage<CUserCompanyRole> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceUserCompanyRole.class);
	Long serialVersionUID = 1L;

	public CPageServiceUserCompanyRole(IPageServiceImplementer<CUserCompanyRole> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CUserCompanyRole");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CUserCompanyRole> gridView = (CGridViewBaseDBEntity<CUserCompanyRole>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
