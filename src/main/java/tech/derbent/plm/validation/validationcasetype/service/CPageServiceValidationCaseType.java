package tech.derbent.plm.validation.validationcasetype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.validation.validationcasetype.domain.CValidationCaseType;

public class CPageServiceValidationCaseType extends CPageServiceDynamicPage<CValidationCaseType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceValidationCaseType.class);
	Long serialVersionUID = 1L;

	public CPageServiceValidationCaseType(IPageServiceImplementer<CValidationCaseType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CValidationCaseType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CValidationCaseType> gridView = (CGridViewBaseDBEntity<CValidationCaseType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
