package tech.derbent.plm.validation.validationsuite.service;

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
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.validation.validationsuite.domain.CValidationSuite;

public class CValidationSuiteInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CValidationSuite.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationSuiteInitializerService.class);
	private static final String menuOrder = Menu_Order_TESTS + ".20";
	private static final String menuTitle = MenuTitle_TESTS + ".Validation Suites";
	private static final String pageDescription = "Validation suite and workflow management";
	private static final String pageTitle = "Validation Suite Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, false);
			detailSection.addScreenLine(CDetailLinesService.createSection("Suite Details"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "objective"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "prerequisites"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Validation Cases"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "validationCases"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Context"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			// Attachments section
			CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);
			// Comments section
			CCommentInitializerService.addCommentsSection(detailSection, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating validation suite view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "objective", "project", "createdBy", "createdDate"));
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
		final CValidationSuiteService validationSuiteService =
				(CValidationSuiteService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		final List<CValidationSuite> existingScenarios = validationSuiteService.findAll();
		if (!existingScenarios.isEmpty()) {
			LOGGER.info("Clearing {} existing validation suites for project: {}", existingScenarios.size(), project.getName());
			for (final CValidationSuite existingScenario : existingScenarios) {
				try {
					validationSuiteService.delete(existingScenario);
				} catch (final Exception e) {
					LOGGER.warn("Could not delete existing validation suite {}: {}", existingScenario.getId(), e.getMessage());
				}
			}
		}
		final String[][] nameAndDescriptions = {
				{
						"User Authentication Flow", "Complete user authentication and authorization workflow"
				}, {
						"E-Commerce Checkout", "End-to-end checkout process from cart to payment"
				}, {
						"Report Generation", "Multi-step report generation and download workflow"
				}, {
						"Data Import/Export", "Complete data import and export functionality validation"
				}, {
						"Multi-User Collaboration", "Concurrent user collaboration and conflict resolution"
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					final CValidationSuite scenario = (CValidationSuite) item;
					// Set objectives
					scenario.setObjective("Verify all functionality works correctly in the " + scenario.getName() + " workflow");
					// Set prerequisites
					scenario.setPrerequisites("Validation environment configured, validation data available, user accounts created");
				});
	}
}
