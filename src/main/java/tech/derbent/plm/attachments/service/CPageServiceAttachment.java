package tech.derbent.plm.attachments.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.attachments.domain.CAttachment;

/**
 * Page service for CAttachment entities.
 * 
 * Provides dynamic page functionality for attachment management views.
 */
public class CPageServiceAttachment extends CPageServiceDynamicPage<CAttachment> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceAttachment.class);
	public CPageServiceAttachment(final IPageServiceImplementer<CAttachment> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CAttachment");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CAttachment> gridView = (CGridViewBaseDBEntity<CAttachment>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
