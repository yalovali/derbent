package tech.derbent.api.projects.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject_Derbent;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.app.attachments.service.CAttachmentInitializerService;
import tech.derbent.app.comments.service.CCommentInitializerService;

public class CProject_DerbentInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CProject_Derbent.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CProject_DerbentInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".1.1";
	private static final String menuTitle = MenuTitle_PROJECT + ".Derbent Projects";
	private static final String pageDescription = "Derbent projects with Kanban board support";
	private static final String pageTitle = "Derbent Project Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject_Derbent project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "kanbanLine"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			final CDetailLines line = CDetailLinesService.createLineFromDefaults(clazz, "userSettings");
			line.setRelationFieldName("userSettings");
			line.setFieldCaption("userSettings");
			line.setEntityProperty("Component:createProjectUserSettingsComponent");
			line.setDataProviderBean("CProject_DerbentService");
			detailSection.addScreenLine(line);
			// Attachments section
			CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);
			// Comments section
			CCommentInitializerService.addCommentsSection(detailSection, clazz);
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating Derbent project view: {}", e.getMessage(), e);
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject_Derbent project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "kanbanLine", "active", "createdDate", "lastModifiedDate"));
		return grid;
	}

	public static void initialize(final CProject_Derbent project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		final String[][] nameAndDescription = {
				{
						"Digital Transformation Initiative", "Comprehensive digital transformation for enhanced customer experience"
				}, {
						"Infrastructure Upgrade Project", "Upgrading IT infrastructure for improved performance and scalability"
				}
		};
		initializeCompanyEntity(nameAndDescription,
				(CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), company, minimal,
				(item, index) -> ((CProject_Derbent) item).setActive(true));
	}
}
