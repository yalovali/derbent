package tech.derbent.bab.policybase.rule.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
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
import tech.derbent.api.screens.service.CEntityOfProjectInitializerService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskCAN;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskFile;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskROS;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterBase;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterService;
import tech.derbent.bab.policybase.node.can.CBabCanNode;
import tech.derbent.bab.policybase.node.can.CBabCanNodeService;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.policybase.node.file.CBabFileOutputNode;
import tech.derbent.bab.policybase.node.file.CBabFileInputNodeService;
import tech.derbent.bab.policybase.node.file.CBabFileOutputNodeService;
import tech.derbent.bab.policybase.node.ip.CBabHttpServerNodeService;
import tech.derbent.bab.policybase.node.modbus.CBabModbusNodeService;
import tech.derbent.bab.policybase.node.ros.CBabROSNode;
import tech.derbent.bab.policybase.node.ros.CBabROSNodeService;
import tech.derbent.bab.policybase.rule.domain.CBabPolicyRule;
import tech.derbent.bab.policybase.trigger.domain.CBabPolicyTrigger;
import tech.derbent.bab.policybase.trigger.service.CBabPolicyTriggerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;

@Service
@Profile({"bab", "default", "test"})
public final class CBabPolicyRuleInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CBabPolicyRule.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyRuleInitializerService.class);
	private static final String menuOrder = "20.0";
	private static final String menuTitle = "Policies.Policy Rules";
	private static final String pageDescription = "BAB policy rule configuration and management";
	private static final String pageTitle = "Policy Rules";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection scr = CEntityOfProjectInitializerService.createBasicView(project, clazz);
			// Policy rule specific fields
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active", true, ""));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "rulePriority", true, ""));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionOrder", false, "100%"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "trigger"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sourceNode", true, ""));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "filter"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actions", false, ""));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logEnabled"));
			scr.addScreenLine(CDetailLinesService.createSection("Project Context"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo", true, ""));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate", true, ""));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			CCommentInitializerService.addDefaultSection(scr, clazz);
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Failed to create policy rule view for projectId={}. reason={}", project != null ? project.getId() : null,
					e.getMessage());
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "active", "rulePriority", "executionOrder", "sourceNode", "actions", "project", "assignedTo",
				"createdBy", "createdDate"));
		return grid;
	}

	private static List<CBabNodeEntity<?>> getAvailableNodesForProject(final CProject<?> project) {
		final List<CBabNodeEntity<?>> nodes = new ArrayList<>();
		try {
			nodes.addAll(CSpringContext.getBean(CBabHttpServerNodeService.class).listByProject(project));
			nodes.addAll(CSpringContext.getBean(CBabFileInputNodeService.class).listByProject(project));
			nodes.addAll(CSpringContext.getBean(CBabFileOutputNodeService.class).listByProject(project));
			nodes.addAll(CSpringContext.getBean(CBabCanNodeService.class).listByProject(project));
			nodes.addAll(CSpringContext.getBean(CBabModbusNodeService.class).listByProject(project));
			nodes.addAll(CSpringContext.getBean(CBabROSNodeService.class).listByProject(project));
		} catch (final Exception e) {
			LOGGER.debug("Node service not available while creating policy rule samples: {}", e.getMessage());
		}
		return nodes;
	}

	private static List<CBabPolicyFilterBase<?>> getAvailableFiltersForNode(final CBabNodeEntity<?> sourceNode) {
		if (sourceNode == null) {
			return List.of();
		}
		try {
			return CSpringContext.getBean(CBabPolicyFilterService.class).listByParentNode(sourceNode);
		} catch (final Exception e) {
			LOGGER.debug("Policy filter service not available while creating policy rule samples: {}", e.getMessage());
			return List.of();
		}
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder, null);
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final CBabPolicyTriggerService triggerService = CSpringContext.getBean(CBabPolicyTriggerService.class);
		final List<CBabPolicyTrigger> availableTriggers = triggerService.listByProject(project);
		final List<CBabNodeEntity<?>> availableNodes = getAvailableNodesForProject(project);
		final String[][] nameAndDescriptions = {
				{
						"Forward CAN to ROS", "Forward CAN bus messages to ROS topic"
				}, {
						"Log HTTP Requests", "Log all HTTP server requests to syslog"
				}, {
						"Modbus to File", "Write Modbus data to file output"
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					// Type-safe cast
					if (item instanceof CBabPolicyRule) {
						final CBabPolicyRule rule = (CBabPolicyRule) item;
						// Set rule-specific defaults for sample data
						rule.setRulePriority(50 + index * 10);
						rule.setExecutionOrder(index);
						rule.setLogEnabled(true);
						Check.notEmpty(availableNodes, "No available nodes found for project - cannot create meaningful policy rule samples without nodes");
						Check.notEmpty(availableTriggers,
								"No available triggers found for project - cannot create meaningful policy rule samples without triggers");
						// Attach real policy component entities to each sample rule.
						rule.setTrigger(availableTriggers.get(index % availableTriggers.size()));
						final CBabNodeEntity<?> sourceNode = availableNodes.get(ThreadLocalRandom.current().nextInt(availableNodes.size()));
						rule.setSourceNode(sourceNode);
						rule.setFilter(getAvailableFiltersForNode(sourceNode).stream().findFirst().orElse(null));
						final CBabNodeEntity<?> destinationNode = getCompatibleActionDestinationNode(availableNodes, index);
						if (destinationNode != null) {
							final CBabPolicyAction action = new CBabPolicyAction("Rule Action " + (index + 1), rule);
							action.setDestinationNode(destinationNode);
							final CBabPolicyActionMaskBase<?> mask = createMaskForAction(action, destinationNode);
							action.setActionMask(mask);
							action.setExecutionOrder(0);
							action.setExecutionPriority(70);
							rule.setActions(new HashSet<>(List.of(action)));
						}
					}
				});
	}

	private static CBabPolicyActionMaskBase<?> createMaskForAction(final CBabPolicyAction action,
			final CBabNodeEntity<?> destinationNode) {
		if (destinationNode instanceof CBabCanNode) {
			return new CBabPolicyActionMaskCAN(action.getName() + " CAN Mask", action);
		}
		if (destinationNode instanceof CBabFileOutputNode) {
			return new CBabPolicyActionMaskFile(action.getName() + " File Mask", action);
		}
		if (destinationNode instanceof CBabROSNode) {
			return new CBabPolicyActionMaskROS(action.getName() + " ROS Mask", action);
		}
		return null;
	}

	private static CBabNodeEntity<?> getCompatibleActionDestinationNode(final List<CBabNodeEntity<?>> availableNodes, final int seedIndex) {
		if (availableNodes == null || availableNodes.isEmpty()) {
			return null;
		}
		final List<CBabNodeEntity<?>> compatibleNodes = availableNodes.stream()
				.filter(node -> node instanceof CBabCanNode || node instanceof CBabFileOutputNode || node instanceof CBabROSNode).toList();
		if (compatibleNodes.isEmpty()) {
			return null;
		}
		return compatibleNodes.get(Math.floorMod(seedIndex, compatibleNodes.size()));
	}

	private CBabPolicyRuleInitializerService() {
		// Utility class - no instantiation
	}
}
