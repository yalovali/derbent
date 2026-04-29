package tech.derbent.plm.orders.type.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityNamedInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.services.CEntityTypeInitializerService;
import tech.derbent.plm.orders.type.domain.COrderType;

public class COrderTypeInitializerService extends CEntityTypeInitializerService {

	private static final Class<?> clazz = COrderType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(COrderTypeInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".14";
	private static final String menuTitle = MenuTitle_TYPES + "." + MenuTitle_TYPES_FINANCE + ".Order Types";
	private static final String pageDescription = "Manage order type categories";
	private static final String pageTitle = "Order Type Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			return CEntityNamedInitializerService.createTypeEntityView(project, clazz, "Display Configuration", true);
		} catch (final Exception e) {
			LOGGER.error("Error creating order type view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "color", "sortOrder", "active", "company"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService)
			throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
				menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder, null);
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final String[][] nameAndDescriptions = {
				{
						"Hardware", "Hardware procurement orders"
				}, {
						"Software", "Software licensing and subscription orders"
				}, {
						"Service", "Professional services and consulting orders"
				}, {
						"Training", "Training and certification orders"
				}, {
						"Maintenance", "Maintenance and support service orders"
				}, {
						"Infrastructure", "Infrastructure and hosting service orders"
				}, {
						"Equipment", "Equipment rental and leasing orders"
				}, {
						"Supplies", "Office supplies and materials orders"
				}
		};
		final CCompany company = project.getCompany();
		initializeCompanyEntity(nameAndDescriptions,
				(CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)),
				company, minimal, null);
	}
}
