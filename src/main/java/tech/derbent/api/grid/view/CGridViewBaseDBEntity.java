package tech.derbent.api.grid.view;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entity.view.CAbstractEntityDBPage;
import tech.derbent.api.reports.service.CReportHelper;
import tech.derbent.base.session.service.ISessionService;

public abstract class CGridViewBaseDBEntity<EntityClass extends CEntityDB<EntityClass>> extends CAbstractEntityDBPage<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGridViewBaseDBEntity.class);
	private static final long serialVersionUID = 1L;

	public CGridViewBaseDBEntity(final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService,
			final ISessionService sessionService) {
		super(entityClass, entityService, sessionService);
	}

	@Override
	protected void createDetailsComponent() throws Exception {
		/**/
	}

	@Override
	protected void createMasterComponent() {
		masterViewSection = new CMasterViewSectionGrid<EntityClass>(entityClass, this);
	}

	/**
	 * Provides grid items for report generation.
	 * Gets all items from the grid's data provider.
	 * @return list of all items in the grid
	 */
	protected List<EntityClass> getGridItemsForReport() {
		if (masterViewSection instanceof CMasterViewSectionGrid) {
			@SuppressWarnings("unchecked")
			final CMasterViewSectionGrid<EntityClass> gridSection = (CMasterViewSectionGrid<EntityClass>) masterViewSection;
			return gridSection.getAllItems();
		}
		LOGGER.warn("Master view section is not a grid section, cannot get items for report");
		return List.of();
	}

	/**
	 * Generates a CSV report from the grid data.
	 * Override actionReport in the page service to call this method.
	 */
	public void generateGridReport() throws Exception {
		final List<EntityClass> items = getGridItemsForReport();
		CReportHelper.generateReport(items, entityClass);
	}
}
