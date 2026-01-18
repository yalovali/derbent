package tech.derbent.app.validation.validationsession.service;

import java.time.LocalDateTime;
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
import tech.derbent.app.attachments.service.CAttachmentInitializerService;
import tech.derbent.app.comments.service.CCommentInitializerService;
import tech.derbent.app.validation.validationsession.domain.CValidationResult;
import tech.derbent.app.validation.validationsession.domain.CValidationSession;
import tech.derbent.app.validation.validationsuite.domain.CValidationSuite;
import tech.derbent.app.validation.validationsuite.service.CValidationSuiteService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;

public class CValidationSessionInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CValidationSession.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationSessionInitializerService.class);
	private static final String menuOrder = Menu_Order_TESTS + ".30";
	private static final String menuTitle = MenuTitle_TESTS + ".Validation Sessions";
	private static final String pageDescription = "Validation execution tracking and results";
	private static final String pageTitle = "Validation Session Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, false);
			detailSection.addScreenLine(CDetailLinesService.createSection("Execution Details"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "validationSuite"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "result"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executedBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionStart"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionEnd"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "durationMs"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Validation Results Summary"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "totalValidationCases"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "passedValidationCases"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "failedValidationCases"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "totalValidationSteps"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "passedValidationSteps"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "failedValidationSteps"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionNotes"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "buildNumber"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "environment"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Validation Case Results"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "validationCaseResults"));
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
			LOGGER.error("Error creating validation session view.");
			throw e;
		}
	}

	/** Creates the execution view for single-page test execution interface. This view shows only the test execution component in full-screen mode. */
	private static CDetailSection createExecutionView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			// Minimal header info - just validation session name and description
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			// Full-screen execution component
			detailSection.addScreenLine(CDetailLinesService.createSection("Validation Execution"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "validationExecutionComponent"));
			// LOGGER.debug("Created execution view for validation session");
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating validation session execution view: {}", e.getMessage(), e);
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "validationSuite", "result", "executedBy", "executionStart", "executionEnd", "durationMs",
				"totalValidationCases", "passedValidationCases", "failedValidationCases", "project", "createdDate"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		// View 1: Standard CRUD for validation session management
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
		// View 2: Single-page execution view (full-screen validation execution interface)
		final CDetailSection executionSection = createExecutionView(project);
		final CGridEntity executionGrid = createGridEntity(project);
		// Set unique names for execution view
		executionSection.setName("Validation Execution Section");
		executionGrid.setName("Validation Execution Grid");
		// CRITICAL: Hide grid to show only execution component
		executionGrid.setAttributeNone(true);
		// Register single-page execution view as separate menu item
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, executionSection, executionGrid,
				menuTitle + ".Execute Validation", // Submenu: Tests.Validation Sessions.Execute Validation
				"Validation Execution", // Page title
				"Execute validations step-by-step with result recording", // Description
				true, // Show in quick toolbar
				menuOrder + ".1"); // Submenu order
		LOGGER.info("Initialized validation session views: standard management + execution interface");
	}

	public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
		final CValidationSessionService validationSessionService =
				(CValidationSessionService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		final List<CValidationSession> existingValidationSessions = validationSessionService.findAll();
		if (!existingValidationSessions.isEmpty()) {
			LOGGER.info("Clearing {} existing validation sessions for project: {}", existingValidationSessions.size(), project.getName());
			for (final CValidationSession existingValidationSession : existingValidationSessions) {
				try {
					validationSessionService.delete(existingValidationSession);
				} catch (final Exception e) {
					LOGGER.warn("Could not delete existing validation session {}: {}", existingValidationSession.getId(), e.getMessage());
				}
			}
		}
		final String[][] nameAndDescriptions = {
				{
						"Sprint 1 Regression Validation", "Full regression validation for Sprint 1 release"
				}, {
						"UAT Round 1", "User acceptance validation first iteration"
				}, {
						"Performance Validation Session", "System performance and load validation"
				}, {
						"Integration Validation Suite", "Third-party integration validation"
				}, {
						"Smoke Validation - Build 2026.01.15", "Quick smoke validation of latest build"
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					final CValidationSession validationSession = (CValidationSession) item;
					final CUser user = CSpringContext.getBean(CUserService.class).getRandom(project.getCompany());
					validationSession.setExecutedBy(user);
					// Get a random validation suite
					final CValidationSuiteService scenarioService = CSpringContext.getBean(CValidationSuiteService.class);
					final List<CValidationSuite> scenarios = scenarioService.findAll();
					if (!scenarios.isEmpty()) {
						validationSession.setValidationSuite(scenarios.get(index % scenarios.size()));
					}
					// Set execution times
					validationSession.setExecutionStart(LocalDateTime.now().minusDays(10 - index).minusHours(2));
					validationSession.setExecutionEnd(validationSession.getExecutionStart().plusHours(1).plusMinutes(30));
					// Set test results
					validationSession.setTotalValidationCases(10 + index * 2);
					validationSession.setPassedValidationCases(8 + index);
					validationSession.setFailedValidationCases(index % 3 == 0 ? 2 : 1);
					// Set validation steps results
					validationSession.setTotalValidationSteps(validationSession.getTotalValidationCases() * 5); // Assume 5 steps per validation case
					validationSession.setPassedValidationSteps((int) (validationSession.getTotalValidationSteps() * 0.85)); // 85% pass rate
					validationSession
							.setFailedValidationSteps(validationSession.getTotalValidationSteps() - validationSession.getPassedValidationSteps());
					// Set execution metadata
					validationSession.setBuildNumber("Build-2026.01." + (15 + index));
					validationSession.setEnvironment(index % 2 == 0 ? "Staging" : "Production");
					validationSession.setExecutionNotes("Validation session completed. " + validationSession.getPassedValidationCases()
							+ " validation cases passed, " + validationSession.getFailedValidationCases() + " failed.");
					// Set overall result
					if (validationSession.getFailedValidationCases() > 0) {
						validationSession.setResult(CValidationResult.FAILED);
					} else {
						validationSession.setResult(CValidationResult.PASSED);
					}
				});
	}
}
