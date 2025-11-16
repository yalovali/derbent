package tech.derbent.app.orders.currency.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.app.orders.currency.domain.CCurrency;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

public class CCurrencyInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Currency Information";
	private static final Class<?> clazz = CCurrency.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CCurrencyInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".11";
	private static final String menuTitle = MenuTitle_TYPES + ".Currencies";
	private static final String pageDescription = "Currencies management";
	private static final String pageTitle = "Currencies Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "currencyCode"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "currencySymbol"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Display Configuration"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating currency view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "currencyCode", "currencySymbol", "description", "active", "project"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
		final String[][] data = {
				{
						"US Dollar", "USD - International currency for business transactions", "USD", "$ "
				}, {
						"Euro", "EUR - European currency for business transactions", "EUR", "€"
				}, {
						"Turkish Lira", "TRY - Turkish currency for business transactions", "TRY", "₺"
				}
		};
		// Use consumer pattern to set currency-specific fields
		initializeProjectEntity(data, (CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project,
				minimal, (item, index) -> {
					final CCurrency currency = (CCurrency) item;
					currency.setCurrencyCode(data[index][2]);
					currency.setCurrencySymbol(data[index][3]);
				});
	}
}
