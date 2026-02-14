package tech.derbent.api.users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.roles.domain.CUserProjectRole;

public class CPageServiceUserProjectRole extends CPageServiceDynamicPage<CUserProjectRole> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceUserProjectRole.class);
	Long serialVersionUID = 1L;

	public CPageServiceUserProjectRole(IPageServiceImplementer<CUserProjectRole> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CUserProjectRole");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CUserProjectRole> gridView = (CGridViewBaseDBEntity<CUserProjectRole>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
