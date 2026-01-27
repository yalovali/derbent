package tech.derbent.plm.agile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.agile.domain.CUserStoryType;

public class CPageServiceUserStoryType extends CPageServiceDynamicPage<CUserStoryType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceUserStoryType.class);

	public CPageServiceUserStoryType(final IPageServiceImplementer<CUserStoryType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CUserStoryType");
		super.actionReport();
	}

	@SuppressWarnings ("unused")
	public CComponentWidgetEntity<CUserStoryType> getComponentWidget(final CUserStoryType entity) {
		return null;
	}
}
