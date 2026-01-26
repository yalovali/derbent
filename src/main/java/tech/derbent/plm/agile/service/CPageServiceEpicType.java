package tech.derbent.plm.agile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.agile.domain.CEpicType;

public class CPageServiceEpicType extends CPageServiceDynamicPage<CEpicType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceEpicType.class);

	public CPageServiceEpicType(final IPageServiceImplementer<CEpicType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CEpicType");
		super.actionReport();
	}

	@Override
	public CComponentWidgetEntity<CEpicType> getComponentWidget(final CEpicType entity) { return null; }
}
