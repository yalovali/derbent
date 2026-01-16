package tech.derbent.app.testcases.testrun.service;

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
import tech.derbent.app.testcases.testrun.domain.CTestResult;
import tech.derbent.app.testcases.testrun.domain.CTestRun;
import tech.derbent.app.testcases.testscenario.domain.CTestScenario;
import tech.derbent.app.testcases.testscenario.service.CTestScenarioService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;

public class CTestRunInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CTestRun.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CTestRunInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".32";
	private static final String menuTitle = MenuTitle_PROJECT + ".Test Runs";
	private static final String pageDescription = "Test execution tracking and results";
	private static final String pageTitle = "Test Run Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, false);

			detailSection.addScreenLine(CDetailLinesService.createSection("Execution Details"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "testScenario"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "result"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executedBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionStart"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionEnd"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "durationMs"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Test Results Summary"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "totalTestCases"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "passedTestCases"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "failedTestCases"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "totalTestSteps"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "passedTestSteps"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "failedTestSteps"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionNotes"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "buildNumber"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "environment"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Test Case Results"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "testCaseResults"));

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
			LOGGER.error("Error creating test run view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "testScenario", "result", "executedBy", "executionStart",
				"executionEnd", "durationMs", "totalTestCases", "passedTestCases", "failedTestCases", "project", "createdDate"));
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
		final CTestRunService testRunService = (CTestRunService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		final List<CTestRun> existingTestRuns = testRunService.findAll();
		if (!existingTestRuns.isEmpty()) {
			LOGGER.info("Clearing {} existing test runs for project: {}", existingTestRuns.size(), project.getName());
			for (final CTestRun existingTestRun : existingTestRuns) {
				try {
					testRunService.delete(existingTestRun);
				} catch (final Exception e) {
					LOGGER.warn("Could not delete existing test run {}: {}", existingTestRun.getId(), e.getMessage());
				}
			}
		}

		final String[][] nameAndDescriptions = {
				{ "Sprint 1 Regression Test", "Full regression testing for Sprint 1 release" },
				{ "UAT Round 1", "User acceptance testing first iteration" },
				{ "Performance Test Run", "System performance and load testing" },
				{ "Integration Test Suite", "Third-party integration testing" },
				{ "Smoke Test - Build 2026.01.15", "Quick smoke test of latest build" }
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					final CTestRun testRun = (CTestRun) item;
					final CUser user = CSpringContext.getBean(CUserService.class).getRandom(project.getCompany());
					testRun.setExecutedBy(user);

					// Get a random test scenario
					final CTestScenarioService scenarioService = CSpringContext.getBean(CTestScenarioService.class);
					final List<CTestScenario> scenarios = scenarioService.findAll();
					if (!scenarios.isEmpty()) {
						testRun.setTestScenario(scenarios.get(index % scenarios.size()));
					}

					// Set execution times
					testRun.setExecutionStart(LocalDateTime.now().minusDays(10 - index).minusHours(2));
					testRun.setExecutionEnd(testRun.getExecutionStart().plusHours(1).plusMinutes(30));

					// Set test results
					testRun.setTotalTestCases(10 + index * 2);
					testRun.setPassedTestCases(8 + index);
					testRun.setFailedTestCases(index % 3 == 0 ? 2 : 1);

					// Set test steps results
					testRun.setTotalTestSteps(testRun.getTotalTestCases() * 5); // Assume 5 steps per test case
					testRun.setPassedTestSteps((int) (testRun.getTotalTestSteps() * 0.85)); // 85% pass rate
					testRun.setFailedTestSteps(testRun.getTotalTestSteps() - testRun.getPassedTestSteps());

					// Set execution metadata
					testRun.setBuildNumber("Build-2026.01." + (15 + index));
					testRun.setEnvironment(index % 2 == 0 ? "Staging" : "Production");
					testRun.setExecutionNotes("Test run completed. " +
							testRun.getPassedTestCases() + " test cases passed, " +
							testRun.getFailedTestCases() + " failed.");

					// Set overall result
					if (testRun.getFailedTestCases() > 0) {
						testRun.setResult(CTestResult.FAILED);
					} else {
						testRun.setResult(CTestResult.PASSED);
					}
				});
	}
}
