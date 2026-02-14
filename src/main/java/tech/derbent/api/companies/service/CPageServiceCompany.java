package tech.derbent.api.companies.service;

import tech.derbent.api.grid.view.CGridViewBaseDBEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.companies.domain.CCompany;

public class CPageServiceCompany extends CPageServiceDynamicPage<CCompany> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceCompany.class);
	Long serialVersionUID = 1L;

	public CPageServiceCompany(IPageServiceImplementer<CCompany> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CCompany");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CCompany> gridView = (CGridViewBaseDBEntity<CCompany>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
