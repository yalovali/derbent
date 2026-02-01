package tech.derbent.api.services.pageservice;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.ui.notifications.CNotificationService;

public abstract class CPageServiceDynamicPage<EntityClass extends CEntityDB<EntityClass>> extends CPageService<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceDynamicPage.class);

	public CPageServiceDynamicPage(final IPageServiceImplementer<EntityClass> view) {
		super(view);
	}

	@Override
	@SuppressWarnings ("unchecked")
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for EntityClass");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<EntityClass> gridView = (CGridViewBaseDBEntity<EntityClass>) getView();
			gridView.generateGridReport();
			return;
		}
		final EntityClass entity = getValue();
		if (entity == null) {
			CNotificationService.showWarning("No data to export");
			return;
		}
		generateCSVReport(List.of(entity), (Class<EntityClass>) entity.getClass(), entity.getClass().getSimpleName().toLowerCase());
	}
}
