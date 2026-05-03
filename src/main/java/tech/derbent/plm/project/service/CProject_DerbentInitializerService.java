package tech.derbent.plm.project.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.service.CProjectInitializerService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityOfProjectInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.project.domain.CProject_Derbent;

public class CProject_DerbentInitializerService extends CProjectInitializerService {

	private static final Class<?> clazz = CProject_Derbent.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CProject_DerbentInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".1.1";
	private static final String menuTitle = MenuTitle_PROJECT + ".Derbent Projects";
	private static final String pageDescription = "Derbent projects with Kanban board support";
	private static final String pageTitle = "Derbent Project Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject_Derbent project) throws Exception {
		try {
			final CDetailSection detailSection = CEntityOfProjectInitializerService.createBasicView(project, clazz);
			final CDetailLines companyLine = CDetailLinesService.createLineFromDefaults(clazz, "company");
			companyLine.setIsReadonly(true);
			detailSection.addScreenLine(companyLine);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "kanbanLine"));
			final CDetailLines line = CDetailLinesService.createLineFromDefaults(clazz, "userSettings");
			line.setRelationFieldName("userSettings");
			line.setFieldCaption("userSettings");
			line.setEntityProperty("Component:createProjectUserSettingsComponent");
			line.setDataProviderBean("CProject_DerbentService");
			detailSection.addScreenLine(line);
			// Attachments section
			CAttachmentInitializerService.addDefaultSection(detailSection, clazz);
			// Comments section
			CCommentInitializerService.addDefaultSection(detailSection, clazz);
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating Derbent project view: {}", e.getMessage());
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject_Derbent project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(
				List.of("id", "name", "description", "kanbanLine", "active", "createdDate", "lastModifiedDate"));
		return grid;
	}

	public static void initialize(final CProject_Derbent project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService)
			throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
				menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder, null);
	}

	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		final String[][] nameAndDescription = {
				{
						"Derbent PM Demo",
						"End-to-end project management demo (backlog, sprints, requirements, deliverables, workflows)."
				}, {
						"Derbent API Platform",
						"API platform delivery (gateway, auth, versioning, observability, SDKs)."
				}, {
						"BAB Integration Program",
						"Integration workstream for BAB gateway + device onboarding + policy automation."
				}, {
						"Mobile App Delivery",
						"Mobile app program (iOS/Android) with releases, sprint planning, testing, and rollout."
				}, {
						"Data & Analytics Platform",
						"Data ingestion, warehouse, BI dashboards, and governance."
				}, {
						"Customer Portal Revamp",
						"Customer self-service portal redesign with accessibility and performance improvements."
				}
		};
		initializeCompanyEntity(nameAndDescription,
				(CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)),
				company, minimal, null);
	}
}
