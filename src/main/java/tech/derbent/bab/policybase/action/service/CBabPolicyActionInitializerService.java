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
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.rule.domain.CBabPolicyRule;
import tech.derbent.bab.policybase.rule.service.CBabPolicyRuleService;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** Initializer for destination-aware policy actions. */
@Service
@Profile ("bab")
public final class CBabPolicyActionInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyAction> clazz = CBabPolicyAction.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyActionInitializerService.class);
	private static final String menuOrder = Menu_Order_POLICIES + ".999.999";
	private static final String menuTitle = MenuTitle_POLICIES + ".Developer.Actions";
	private static final String pageDescription = "Manage destination-aware policy actions and assigned action masks";
	private static final String pageTitle = "Policy Actions";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		scr.addScreenLine(CDetailLinesService.createSection("Basic Information"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "policyRule"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "destinationNode"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actionMask"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentActionMaskDetails"));
		scr.addScreenLine(CDetailLinesService.createSection("Execution Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "asyncExecution"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionPriority"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionOrder"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "timeoutSeconds"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "retryCount"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "retryDelaySeconds"));
		scr.addScreenLine(CDetailLinesService.createSection("Logging Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logExecution"));
		CAttachmentInitializerService.addDefaultSection(scr, clazz);
		CLinkInitializerService.addDefaultSection(scr, clazz);
		CCommentInitializerService.addDefaultSection(scr, clazz);
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "policyRule", "destinationNode", "actionMask", "active", "executionPriority", "executionOrder",
				"asyncExecution", "timeoutSeconds", "retryCount"));
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
		final CBabPolicyActionService actionService = CSpringContext.getBean(CBabPolicyActionService.class);
		if (!actionService.listByProject(project).isEmpty()) {
			LOGGER.info("Policy actions already exist for project: {}", project.getName());
			return;
		}
		final CBabPolicyRuleService ruleService = CSpringContext.getBean(CBabPolicyRuleService.class);
		final List<CBabPolicyRule> rules = ruleService.listByProject(project);
		if (rules.isEmpty()) {
			LOGGER.info("No policy rules found for project {}, skipping action sample creation", project.getName());
			return;
		}
		final String[] sampleNames = {
				"Forward Telemetry", "Write Processed Snapshot", "Publish ROS Event", "Emit Syslog Alert"
		};
		int created = 0;
		for (int index = 0; index < sampleNames.length; index++) {
			final CBabPolicyRule rule = rules.get(index % rules.size());
			final CBabPolicyAction action = actionService.createDraftActionForRule(rule);
			action.setName(sampleNames[index]);
			final CBabPolicyActionMaskBase<?> selectedMask = action.getActionMask();
			final String destinationName = action.getDestinationNode() != null ? action.getDestinationNode().getName() : "N/A";
			final String maskName = selectedMask != null ? selectedMask.getName() : "N/A";
			action.setDescription("Action using mask " + maskName + " on destination node " + destinationName);
			action.setExecutionPriority(70 - index * 5);
			action.setAsyncExecution(index % 2 == 0);
			actionService.save(action);
			created++;
			if (minimal) {
				break;
			}
		}
		LOGGER.info("Created {} sample policy actions for project: {}", created, project.getName());
	}

	private CBabPolicyActionInitializerService() {
		// Utility class
	}
}
