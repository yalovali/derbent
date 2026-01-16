package tech.derbent.app.testcases.testcase.service;

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
import tech.derbent.app.testcases.testcase.domain.CTestCase;
import tech.derbent.app.testcases.testcase.domain.CTestPriority;
import tech.derbent.app.testcases.testcase.domain.CTestSeverity;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;

public class CTestCaseInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CTestCase.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CTestCaseInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".30";
	private static final String menuTitle = MenuTitle_PROJECT + ".Test Cases";
	private static final String pageDescription = "Test case design and management";
	private static final String pageTitle = "Test Case Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priority"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "severity"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Test Details"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "preconditions"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "automated"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "automatedTestPath"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Test Steps"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "testSteps"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Context"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "testScenario"));

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
			LOGGER.error("Error creating test case view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "entityType", "priority", "severity", "status",
				"automated", "project", "assignedTo", "testScenario", "createdDate"));
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
		final CTestCaseService testCaseService = (CTestCaseService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		final List<CTestCase> existingTestCases = testCaseService.findAll();
		if (!existingTestCases.isEmpty()) {
			LOGGER.info("Clearing {} existing test cases for project: {}", existingTestCases.size(), project.getName());
			for (final CTestCase existingTestCase : existingTestCases) {
				try {
					testCaseService.delete(existingTestCase);
				} catch (final Exception e) {
					LOGGER.warn("Could not delete existing test case {}: {}", existingTestCase.getId(), e.getMessage());
				}
			}
		}

		final String[][] nameAndDescriptions = {
				{ "User Login Validation", "Verify user can login with valid credentials and access dashboard" },
				{ "Password Reset Flow", "Test password reset functionality with email verification" },
				{ "Data Export Feature", "Verify data export generates correct CSV and Excel file formats" },
				{ "Form Validation Rules", "Test all validation rules on user registration form" },
				{ "Dashboard Loading Performance", "Measure and validate dashboard load time under 2 seconds" },
				{ "Mobile Responsive Layout", "Verify UI adapts correctly to mobile and tablet viewports" },
				{ "API Error Handling", "Test API error responses return proper HTTP status codes and messages" },
				{ "Session Timeout Handling", "Verify session timeout after 30 minutes redirects to login page" },
				{ "File Upload Functionality", "Test file upload with various formats and size limits" },
				{ "Search and Filter Operations", "Verify search returns accurate results with proper filtering" }
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					final CTestCase testCase = (CTestCase) item;
					final CUser user = CSpringContext.getBean(CUserService.class).getRandom(project.getCompany());
					testCase.setAssignedTo(user);

					// Set priority and severity
					testCase.setPriority(index % 3 == 0 ? CTestPriority.HIGH : CTestPriority.MEDIUM);
					testCase.setSeverity(index % 2 == 0 ? CTestSeverity.CRITICAL : CTestSeverity.NORMAL);

					// Set automated flag for some tests
					testCase.setAutomated(index % 4 == 0);
					if (testCase.getAutomated()) {
						testCase.setAutomatedTestPath("src/test/java/automated_tests/test_" + index + ".java");
					}

					// Set preconditions
					testCase.setPreconditions("User must be logged in with appropriate permissions");
				});
	}
}
