package tech.derbent.plm.issues.issue.service;

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
import tech.derbent.plm.issues.issue.domain.CIssue;
import tech.derbent.plm.issues.issue.domain.EIssuePriority;
import tech.derbent.plm.issues.issue.domain.EIssueSeverity;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;

public class CIssueInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CIssue.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CIssueInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".15";
	private static final String menuTitle = MenuTitle_PROJECT + ".Issues";
	private static final String pageDescription = "Issue and bug tracking management";
	private static final String pageTitle = "Issue Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "issueSeverity"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "issuePriority"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "issueResolution"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Details"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "stepsToReproduce"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "expectedResult"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actualResult"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Context"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "linkedActivity"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "dueDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "resolvedDate"));
			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addCommentsSection(detailSection, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating issue view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "issueSeverity", "issuePriority", "status", "project", "assignedTo", "dueDate",
				"resolvedDate", "createdBy", "createdDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	@SuppressWarnings ("unused")
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		// Check if issues already exist for this project
		final CIssueService issueService = (CIssueService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		final List<CIssue> existingIssues = issueService.listByProject(project);
		if (!existingIssues.isEmpty()) {
			LOGGER.info("Issues already exist for project '{}', skipping initialization", project.getName());
			return;
		}
		final String[][] nameAndDescriptions = {
				{
						"Login button not responding", "Button click event not firing on login page"
				}, {
						"Data validation error", "Form submission fails with incorrect validation message"
				}, {
						"Performance issue on dashboard", "Dashboard takes too long to load with large datasets"
				}, {
						"UI rendering problem", "Layout breaks on mobile devices below 768px width"
				}, {
						"Memory leak in background task", "Background worker process consuming excessive memory over time"
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					final CIssue issue = (CIssue) item;
					final CUser user = CSpringContext.getBean(CUserService.class).getRandom(project.getCompany());
					issue.setAssignedTo(user);
					// Set required enum fields
					issue.setIssueSeverity(EIssueSeverity.MINOR);
					issue.setIssuePriority(EIssuePriority.MEDIUM);
				});
	}
}
