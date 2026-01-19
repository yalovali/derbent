package tech.derbent.app.validation.validationcasetype.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.app.validation.validationcasetype.domain.CValidationCaseType;

public class CValidationCaseTypeInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CValidationCaseType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationCaseTypeInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".30";
	private static final String menuTitle = MenuTitle_TYPES + ".Validation Case Types";
	private static final String pageDescription = "Manage validation case type categories";
	private static final String pageTitle = "Validation Case Types";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "workflow"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating validation case type view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "color", "icon", "workflow", "company", "createdDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final String[][] nameAndDescriptions = {
				{ "Functional", "Functional testing of features" },
				{ "Integration", "Integration testing between components" },
				{ "Performance", "Performance and load testing" },
				{ "Security", "Security and vulnerability testing" },
				{ "Regression", "Regression testing of existing functionality" },
				{ "User Acceptance", "User acceptance testing" }
		};
		initializeCompanyEntity(nameAndDescriptions,
				(CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)),
				project.getCompany(), minimal, null);
	}
}
