package tech.derbent.bab.policybase.action.service;

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
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** CBabPolicyActionInitializerService - Initializer for BAB policy action entities. Creates UI forms and grids for action management with sections
 * for: - Basic action information (name, type, description) - Action configuration (type, parameters, templates) - Execution settings (priority,
 * order, timeout, retry) - Logging settings (input, output, execution logs) - Node type filtering (enable/disable by node type) - Standard
 * compositions (attachments, comments, links) Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent pattern: Initializer
 * service with form building */
@Service
@Profile ("bab")
public final class CBabPolicyActionInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyAction> clazz = CBabPolicyAction.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyActionInitializerService.class);
	private static final String menuOrder = Menu_Order_POLICIES + ".20";
	private static final String menuTitle = MenuTitle_POLICIES + ".Actions";
	private static final String pageDescription = "Manage policy actions for data processing and routing";
	private static final String pageTitle = "Policy Actions";
	private static final boolean showInQuickToolbar = false;

	/** Create basic view for action entity. */
	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		// Basic Information Section
		scr.addScreenLine(CDetailLinesService.createSection("Basic Information"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actionType"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "asyncExecution"));
		// Execution Settings Section
		scr.addScreenLine(CDetailLinesService.createSection("Execution Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionPriority"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionOrder"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "timeoutSeconds"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "retryCount"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "retryDelaySeconds"));
		// Logging Settings Section
		scr.addScreenLine(CDetailLinesService.createSection("Logging Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logExecution"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logInput"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logOutput"));
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
		grid.setColumnFields(List.of("id", "name", "actionType", "description", "isEnabled", "executionPriority", "executionOrder", "asyncExecution",
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

	/** Initialize sample policy actions for a project.
	 * @param project the project to create actions for
	 * @param minimal if true, creates only 1 action; if false, creates 8 actions */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final CBabPolicyActionService service = CSpringContext.getBean(CBabPolicyActionService.class);
		// Guard clause - check if already has data
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("Policy actions already exist for project: {}", project.getName());
			return;
		}
		LOGGER.debug("Creating sample policy actions for project: {}", project.getName());
		// Sample action seeds: name, type, description, priority, async, timeout, retry
		final Object[][] samples = {
				{
						"Forward to Database", CBabPolicyAction.ACTION_TYPE_FORWARD, "Forward sensor data to central database", 70, false, 0, 0
				}, {
						"Transform JSON", CBabPolicyAction.ACTION_TYPE_TRANSFORM, "Transform data format from CSV to JSON", 60, true, 30, 0
				}, {
						"Store to File", CBabPolicyAction.ACTION_TYPE_STORE, "Store processed data to file system", 50, true, 0, 5
				}, {
						"Email Alert", CBabPolicyAction.ACTION_TYPE_NOTIFY, "Send email notification for critical events", 90, true, 15, 0
				}, {
						"Restart Service", CBabPolicyAction.ACTION_TYPE_EXECUTE, "Restart system service on failure", 100, false, 120, 3
				}, {
						"Filter Invalid Data", CBabPolicyAction.ACTION_TYPE_FILTER, "Filter out invalid or corrupted data", 80, false, 0, 0
				}, {
						"Validate Schema", CBabPolicyAction.ACTION_TYPE_VALIDATE, "Validate data against predefined schema", 85, false, 0, 0
				}, {
						"System Logger", CBabPolicyAction.ACTION_TYPE_LOG, "Log system events and data processing", 30, true, 5, 0
				}
		};
		for (final Object[] sample : samples) {
			final CBabPolicyAction action = new CBabPolicyAction((String) sample[0], project);
			action.setActionType((String) sample[1]);
			action.setDescription((String) sample[2]);
			action.setExecutionPriority((Integer) sample[3]);
			action.setAsyncExecution((Boolean) sample[4]);
			if ((Integer) sample[5] > 0) {
				action.setTimeoutSeconds((Integer) sample[5]);
			}
			if ((Integer) sample[6] > 0) {
				action.setRetryCount((Integer) sample[6]);
			}
			service.save(action);
			if (minimal) {
				break;
			}
		}
		LOGGER.info("Created {} sample policy action(s) for project: {}", minimal ? 1 : samples.length, project.getName());
	}

	private CBabPolicyActionInitializerService() {
		// Utility class - no instantiation
	}
}
