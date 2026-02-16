package tech.derbent.bab.project.service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.bab.project.domain.CProject_Bab;

public class CProject_BabInitializerService extends CInitializerServiceBase {

	private static final String BAB_PROJECT_DESCRIPTION = "Initial BAB Gateway project for UI configuration.";
	private static final String BAB_PROJECT_NAME = "BAB Gateway Core";
	private static final Class<?> clazz = CProject_Bab.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CProject_BabInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".1.2";
	private static final String menuTitle = "BAB Gateway Projects";
	private static final String pageDescription = "BAB Gateway projects with IP address configuration";
	private static final String pageTitle = "BAB Gateway Project Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			final CDetailLines companyLine = CDetailLinesService.createLineFromDefaults(clazz, "company");
			companyLine.setIsReadonly(true);
			detailSection.addScreenLine(companyLine);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "ipAddress"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "authToken"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			final CDetailLines line = CDetailLinesService.createLineFromDefaults(clazz, "userSettings");
			line.setRelationFieldName("userSettings");
			line.setFieldCaption("userSettings");
			line.setEntityProperty("Component:createProjectUserSettingsComponent");
			line.setDataProviderBean("CProject_BabService");
			detailSection.addScreenLine(line);
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB Gateway project view: {}", e.getMessage(), e);
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "ipAddress", "authToken", "active", "createdDate", "lastModifiedDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
				MenuTitle_DEVELOPMENT + menuTitle + "_devel", pageTitle, pageDescription, showInQuickToolbar, Menu_Order_DEVELOPMENT + menuOrder);
	}

	@SuppressWarnings ("unchecked")
	public static CProject_Bab initializeSampleBab(final CCompany company, final boolean minimal) throws Exception {
		final String[][] seeds = {
				{
						BAB_PROJECT_NAME, BAB_PROJECT_DESCRIPTION
				}
		};
		final List<CProject_Bab> created = new ArrayList<>();
		initializeCompanyEntity(seeds,
				(CEntityOfCompanyService<CProject_Bab>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), company, minimal,
				(final CProject_Bab item, final int index) -> {
					item.setIpAddress("127.0.0.1");
					item.setAuthToken("test-token-123"); // Default auth token for Calimero server (matches config/http_server.json)
					created.add(item);
				});
		return created.isEmpty() ? null : created.get(0);
	}
}
