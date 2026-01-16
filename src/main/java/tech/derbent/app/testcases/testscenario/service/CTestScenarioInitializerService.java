package tech.derbent.app.testcases.testscenario.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
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
import tech.derbent.app.testcases.testscenario.domain.CTestScenario;

public class CTestScenarioInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CTestScenario.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CTestScenarioInitializerService.class);
	private static final String menuOrder = Menu_Order_TESTS + ".20";
	private static final String menuTitle = MenuTitle_TESTS + ".Test Suites";
	private static final String pageDescription = "Test suite and workflow management";
	private static final String pageTitle = "Test Suite Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, false);

			detailSection.addScreenLine(CDetailLinesService.createSection("Scenario Details"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "objective"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "prerequisites"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Test Cases"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "testCases"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Context"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));

			// Attachments section
			tech.derbent.app.attachments.service.CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);

			// Comments section
			tech.derbent.app.comments.service.CCommentInitializerService.addCommentsSection(detailSection, clazz);

			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating test scenario view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "objective", "project", "createdBy", "createdDate"));
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
		final CTestScenarioService testScenarioService = (CTestScenarioService) CSpringContext
				.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		final List<CTestScenario> existingScenarios = testScenarioService.findAll();
		if (!existingScenarios.isEmpty()) {
			LOGGER.info("Clearing {} existing test scenarios for project: {}", existingScenarios.size(), project.getName());
			for (final CTestScenario existingScenario : existingScenarios) {
				try {
					testScenarioService.delete(existingScenario);
				} catch (final Exception e) {
					LOGGER.warn("Could not delete existing test scenario {}: {}", existingScenario.getId(), e.getMessage());
				}
			}
		}

		final String[][] nameAndDescriptions = {
				{ "User Authentication Flow", "Complete user authentication and authorization workflow" },
				{ "E-Commerce Checkout", "End-to-end checkout process from cart to payment" },
				{ "Report Generation", "Multi-step report generation and download workflow" },
				{ "Data Import/Export", "Complete data import and export functionality testing" },
				{ "Multi-User Collaboration", "Concurrent user collaboration and conflict resolution" }
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					final CTestScenario scenario = (CTestScenario) item;

					// Set objectives
					scenario.setObjective("Verify all functionality works correctly in the " + scenario.getName() + " workflow");

					// Set prerequisites
					scenario.setPrerequisites("Test environment configured, test data available, user accounts created");
				});
	}
}
