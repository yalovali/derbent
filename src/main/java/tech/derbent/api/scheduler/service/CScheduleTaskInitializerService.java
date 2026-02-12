package tech.derbent.api.scheduler.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.scheduler.domain.CScheduleTask;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;

@Service
public final class CScheduleTaskInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CScheduleTask.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CScheduleTaskInitializerService.class);
	private static final String menuOrder = "9999.20";
	private static final String menuTitle = "System.Schedule Tasks";
	private static final String pageDescription = "Automated task scheduling with cron expressions";
	private static final String pageTitle = "Schedule Tasks";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		scr.addScreenLine(CDetailLinesService.createSection("Schedule Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "cronExpression"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "action"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actionParameters"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enabled"));
		scr.addScreenLine(CDetailLinesService.createSection("Execution Statistics"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastRun"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "nextRun"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionCount"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "successCount"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "failureCount"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastError"));
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("name", "cronExpression", "action", "enabled", "lastRun", "nextRun", "executionCount", "failureCount"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		LOGGER.info("Initializing Schedule Task entity");
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
		LOGGER.info("Schedule Task entity initialized successfully");
	}

	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		LOGGER.info("Creating sample schedule tasks");
		final CScheduleTaskService service = CSpringContext.getBean(CScheduleTaskService.class);
		if (!service.listByCompany(company).isEmpty()) {
			LOGGER.info("Schedule tasks already exist for company: {}", company.getName());
			return;
		}
		CScheduleTask task = new CScheduleTask("Email Queue Processor", "0 */5 * * * *", CScheduleTask.ACTION_PROCESS_EMAIL_QUEUE, company);
		task.setDescription("Processes queued emails every 5 minutes");
		task = service.save(task);
		service.calculateNextRun(task);
		service.save(task);
		LOGGER.info("Created sample task: {}", task.getName());
		if (minimal) {
			return;
		}
		task = new CScheduleTask("Nightly Cleanup", "0 0 2 * * *", "CLEANUP_OLD_DATA", company);
		task.setDescription("Cleanup old data at 2 AM daily");
		task.setEnabled(false);
		task = service.save(task);
		service.calculateNextRun(task);
		service.save(task);
		LOGGER.info("Created sample task: {}", task.getName());
	}

	private CScheduleTaskInitializerService() {}
}
