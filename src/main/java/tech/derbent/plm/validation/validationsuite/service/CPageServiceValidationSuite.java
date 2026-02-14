package tech.derbent.plm.validation.validationsuite.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.validation.validationsuite.domain.CValidationSuite;

public class CPageServiceValidationSuite extends CPageServiceDynamicPage<CValidationSuite> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceValidationSuite.class);
	Long serialVersionUID = 1L;

	public CPageServiceValidationSuite(IPageServiceImplementer<CValidationSuite> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CValidationSuite");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CValidationSuite> gridView = (CGridViewBaseDBEntity<CValidationSuite>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
