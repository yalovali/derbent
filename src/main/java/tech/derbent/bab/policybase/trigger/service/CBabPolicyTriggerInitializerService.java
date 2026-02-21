package tech.derbent.bab.policybase.trigger.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.bab.policybase.trigger.domain.CBabPolicyTrigger;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** CBabPolicyTriggerInitializerService - Initializer for BAB policy trigger entities. Creates UI forms and grids for trigger management with sections
 * for: - Basic trigger information (name, type, description) - Trigger configuration (type, cron expression, conditions) - Execution settings
 * (priority, order, timeout) - Node type filtering (enable/disable by node type) - Standard compositions (attachments, comments, links) Layer:
 * Service (MVC) Active when: 'bab' profile is active Following Derbent pattern: Initializer service with form building */
@Service
@Profile ("bab")
public final class CBabPolicyTriggerInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyTrigger> clazz = CBabPolicyTrigger.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyTriggerInitializerService.class);
	private static final String menuOrder = Menu_Order_POLICIES + ".10";
	private static final String menuTitle = MenuTitle_POLICIES + ".Triggers";
	private static final String pageDescription = "Manage policy triggers for event detection and scheduling";
	private static final String pageTitle = "Policy Triggers";
	private static final boolean showInQuickToolbar = false;

	/** Create basic view for trigger entity. */
	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		// Basic Information Section
		scr.addScreenLine(CDetailLinesService.createSection("Basic Information"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "triggerType"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
		// Trigger Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("Trigger Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "cronExpression"));
		// Execution Settings Section
		scr.addScreenLine(CDetailLinesService.createSection("Execution Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionPriority"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionOrder"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "timeoutSeconds"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "retryCount"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logExecution"));
		// Node Type Filtering Section
		scr.addScreenLine(CDetailLinesService.createSection("Node Type Filtering"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "canNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "modbusNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "httpNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fileNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "syslogNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "rosNodeEnabled"));
		// Standard composition sections
		CAttachmentInitializerService.addDefaultSection(scr, clazz);
		CLinkInitializerService.addDefaultSection(scr, clazz);
		CCommentInitializerService.addDefaultSection(scr, clazz);
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "triggerType", "description", "active", "cronExpression", "executionPriority", "executionOrder",
				"timeoutSeconds", "retryCount"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	/** Initialize sample policy triggers for a project.
	 * @param project the project to create triggers for
	 * @param minimal if true, creates only 1 trigger; if false, creates 5 triggers */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final CBabPolicyTriggerService service = CSpringContext.getBean(CBabPolicyTriggerService.class);
		// Guard clause - check if already has data
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("Policy triggers already exist for project: {}", project.getName());
			return;
		}
		LOGGER.debug("Creating sample policy triggers for project: {}", project.getName());
		// Sample trigger seeds: name, type, description, cronExpression, priority, order, timeout
		final Object[][] samples = {
				{
						"Data Collection Periodic", CBabPolicyTrigger.TRIGGER_TYPE_PERIODIC, "Periodic data collection from sensors", "0 */5 * * * *",
						80, 1, 0
				}, {
						"System Startup", CBabPolicyTrigger.TRIGGER_TYPE_AT_START, "Initialize system on startup", null, 100, 0, 60
				}, {
						"Emergency Stop", CBabPolicyTrigger.TRIGGER_TYPE_MANUAL, "Manual emergency stop trigger", null, 100, 0, 0
				}, {
						"Continuous Monitor", CBabPolicyTrigger.TRIGGER_TYPE_ALWAYS, "Continuous monitoring of critical systems", null, 60, 0, 10
				}, {
						"Initial Configuration", CBabPolicyTrigger.TRIGGER_TYPE_ONCE, "One-time initial system configuration", null, 90, 0, 0
				}
		};
		for (final Object[] sample : samples) {
			final CBabPolicyTrigger trigger = new CBabPolicyTrigger((String) sample[0], project);
			trigger.setTriggerType((String) sample[1]);
			trigger.setDescription((String) sample[2]);
			if (sample[3] != null) {
				trigger.setCronExpression((String) sample[3]);
			}
			trigger.setExecutionPriority((Integer) sample[4]);
			trigger.setExecutionOrder((Integer) sample[5]);
			if ((Integer) sample[6] > 0) {
				trigger.setTimeoutSeconds((Integer) sample[6]);
			}
			service.save(trigger);
			if (minimal) {
				break;
			}
		}
		LOGGER.info("Created {} sample policy trigger(s) for project: {}", minimal ? 1 : samples.length, project.getName());
	}

	private CBabPolicyTriggerInitializerService() {
		// Utility class - no instantiation
	}
}
