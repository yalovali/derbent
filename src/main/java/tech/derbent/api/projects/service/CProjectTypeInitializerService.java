package tech.derbent.api.projects.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.utils.Check;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.domain.CProjectType;

public class CProjectTypeInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CProjectType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectTypeInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".2";
	private static final String menuTitle = MenuTitle_TYPES + ".Project Types";
	private static final String pageDescription = "Manage project type categories for organization";
	private static final String pageTitle = "Project Type Management";
	private static final boolean showInQuickToolbar = false;
	private static final String BAB_PROJECT_TYPE_NAME = "IoT Gateway";
	private static final String BAB_PROJECT_TYPE_DESCRIPTION = "BAB Gateway core IoT management projects";

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		Check.notNull(project, "project cannot be null");
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "workflow"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Display Configuration"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeNonDeletable"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));

			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating project type view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "color", "sortOrder", "active", "company"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		final String[][] nameAndDescriptions = {
				{
						"Internal Development", "Internal software and infrastructure projects"
				}, {
						"Client Project", "Customer-facing project deliverables"
				}, {
						"Research & Innovation", "R&D and innovation initiatives"
				}, {
						"Maintenance", "System maintenance and support projects"
				}, {
						"Strategic Initiative", "Company-wide strategic initiatives"
				}
		};
		initializeCompanyEntity(nameAndDescriptions,
				(CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), company, minimal, null);
	}

	public static CProjectType initializeSampleBab(final CCompany company, final boolean minimal) throws Exception {
		final String[][] seeds = {
				{
						BAB_PROJECT_TYPE_NAME, BAB_PROJECT_TYPE_DESCRIPTION
				}
		};
		final CEntityOfCompanyService<?> service =
				(CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		initializeCompanyEntity(seeds, service, company, minimal, null);
		return service.listByCompany(company).stream()
				.map(item -> (CProjectType) item)
				.filter(type -> BAB_PROJECT_TYPE_NAME.equals(type.getName()))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("BAB project type not found after initialization"));
	}
}
