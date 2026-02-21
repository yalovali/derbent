package tech.derbent.bab.policybase.action.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** Initializer for destination-aware policy actions. */
@Service
@Profile ("bab")
public final class CBabPolicyActionInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyAction> clazz = CBabPolicyAction.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyActionInitializerService.class);
	private static final String menuOrder = Menu_Order_POLICIES + ".20";
	private static final String menuTitle = MenuTitle_POLICIES + ".Actions";
	private static final String pageDescription = "Manage destination-aware policy actions and assigned action masks";
	private static final String pageTitle = "Policy Actions";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		scr.addScreenLine(CDetailLinesService.createSection("Basic Information"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "destinationNode"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actionMask"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "asyncExecution"));
		scr.addScreenLine(CDetailLinesService.createSection("Execution Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionPriority"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionOrder"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "timeoutSeconds"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "retryCount"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "retryDelaySeconds"));
		scr.addScreenLine(CDetailLinesService.createSection("Logging Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logExecution"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logInput"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logOutput"));
		CAttachmentInitializerService.addDefaultSection(scr, clazz);
		CLinkInitializerService.addDefaultSection(scr, clazz);
		CCommentInitializerService.addDefaultSection(scr, clazz);
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "destinationNode", "actionMask", "active", "executionPriority", "executionOrder",
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
		final CBabPolicyActionService service = CSpringContext.getBean(CBabPolicyActionService.class);
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("Policy actions already exist for project: {}", project.getName());
			return;
		}
		final List<CBabNodeEntity<?>> supportedDestinationNodes = service.listSupportedDestinationNodes(project);
		if (supportedDestinationNodes.isEmpty()) {
			LOGGER.warn("No supported destination nodes found for project {}, skipping action sample creation", project.getName());
			return;
		}
		final String[] sampleNames = {
				"Forward Telemetry", "Write Processed Snapshot", "Publish ROS Event", "Emit Syslog Alert"
		};
		for (int index = 0; index < sampleNames.length; index++) {
			final CBabNodeEntity<?> destinationNode = supportedDestinationNodes.get(ThreadLocalRandom.current().nextInt(supportedDestinationNodes.size()));
			final List<CBabPolicyActionMaskBase<?>> allowedMasks = service.listMasksForDestinationNode(destinationNode);
			if (allowedMasks.isEmpty()) {
				continue;
			}
			final CBabPolicyActionMaskBase<?> selectedMask = allowedMasks.get(0);
			final CBabPolicyAction action = new CBabPolicyAction(sampleNames[index], project);
			action.setDestinationNode(destinationNode);
			action.setActionMask(selectedMask);
			action.setDescription("Action using mask " + selectedMask.getName() + " on destination node " + destinationNode.getName());
			action.setExecutionPriority(70 - (index * 5));
			action.setAsyncExecution(index % 2 == 0);
			service.save(action);
			if (minimal) {
				break;
			}
		}
		LOGGER.info("Created sample policy actions for project: {}", project.getName());
	}

	private CBabPolicyActionInitializerService() {
		// Utility class
	}
}
