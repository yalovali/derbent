package tech.derbent.plm.invoices.invoicetype.service;

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
import tech.derbent.plm.invoices.invoicetype.domain.CInvoiceType;

public class CInvoiceTypeInitializerService extends CEntityTypeInitializerService {

	private static final Class<?> clazz = CInvoiceType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CInvoiceTypeInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".20";
	private static final String menuTitle = MenuTitle_TYPES + "." + MenuTitle_TYPES_PROJECT + ".Invoice Types";
	private static final String pageDescription = "Manage invoice type categories";
	private static final String pageTitle = "Invoice Type Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			return CEntityNamedInitializerService.createTypeEntityView(project, clazz, "Display Configuration", true, "level");
		} catch (final Exception e) {
			LOGGER.error("Error creating invoice type view.");
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
				{"Standard Invoice", "General customer billing"},
				{"Milestone Payment", "Milestone acceptance invoice"},
				{"Subscription", "Recurring subscription invoice"}
		};
		final CCompany company = project.getCompany();
		initializeCompanyEntity(nameAndDescriptions,
				(CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)),
				company, minimal, null);
	}
}
