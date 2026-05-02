package tech.derbent.plm.documenttypes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.documenttypes.domain.CDocumentType;

public class CPageServiceDocumentType extends CPageServiceDynamicPage<CDocumentType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceDocumentType.class);
	Long serialVersionUID = 1L;

	public CPageServiceDocumentType(final IPageServiceImplementer<CDocumentType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CDocumentType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CDocumentType> gridView = (CGridViewBaseDBEntity<CDocumentType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}
}
