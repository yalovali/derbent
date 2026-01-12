package tech.derbent.api.projects.service;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.projects.domain.CProject;

public class CProjectInitializerService extends CInitializerServiceBase {

	static final Class<?> clazz = CProject.class;
	static Map<String, EntityFieldInfo> fields;
	static EntityFieldInfo info;
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".1";
	private static final String menuTitle = MenuTitle_PROJECT + ".Projects";
	private static final String pageDescription = "Comprehensive project management with full CRUD operations";
	private static final String pageTitle = "Project Management";
	private static final boolean showInQuickToolbar = true;
	private static final String BAB_PROJECT_NAME = "BAB Gateway Core";
	private static final String BAB_PROJECT_DESCRIPTION = "Initial BAB Gateway project for UI configuration.";

	public static CDetailSection createBasicView(final CProject project) throws Exception {
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
			line.setDataProviderBean("CProjectService");
			detailSection.addScreenLine(line);
			
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating project view.");
			throw e;
		}
	}

        public static CGridEntity createGridEntity(final CProject project) {
                final CGridEntity grid = createBaseGridEntity(project, clazz);
                grid.setColumnFields(List.of("id", "name", "description", "kanbanLine", "active", "createdDate", "lastModifiedDate"));
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
                final String[][] nameAndDescription = {
                                {
                                                "Digital Transformation Initiative", "Comprehensive digital transformation for enhanced customer experience"
                                }, {
                                                "Infrastructure Upgrade Project", "Upgrading IT infrastructure for improved performance and scalability"
                                }
                };
                initializeCompanyEntity(nameAndDescription,
                                (CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), company,
                                minimal, (item, index) -> ((CProject) item).setActive(true));
        }

        public static CProject initializeSampleBab(final CCompany company, final boolean minimal) throws Exception {
                final String[][] seeds = {
                                {
                                                BAB_PROJECT_NAME, BAB_PROJECT_DESCRIPTION
                                }
                };
                final CEntityOfCompanyService<?> service =
                                (CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
                initializeCompanyEntity(seeds, service, company, minimal, (item, index) -> ((CProject) item).setActive(true));
                return (CProject) service.listByCompany(company).stream()
                                .filter(project -> BAB_PROJECT_NAME.equals(project.getName()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException("BAB project seed not found after initialization"));
        }
}
