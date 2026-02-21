package tech.derbent.bab.policybase.actionmask.service;

import java.util.List;
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
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskROS;
import tech.derbent.bab.policybase.node.ros.CBabROSNode;

@Service
@Profile ("bab")
public final class CBabPolicyActionMaskROSInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyActionMaskROS> clazz = CBabPolicyActionMaskROS.class;
	private static final String menuOrder = Menu_Order_POLICIES + ".43";
	private static final String menuTitle = MenuTitle_POLICIES + ".ActionMasks.ROS";
	private static final String pageDescription = "Manage ROS action masks for destination ROS nodes";
	private static final String pageTitle = "ROS Action Masks";
	private static final String sampleNameSuffix = " ROS Mask";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		scr.addScreenLine(CDetailLinesService.createSection("Mask Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentNode"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionOrder"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "targetTopic"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "messageType"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "messageTemplateJson"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maskConfigurationJson"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maskTemplateJson"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "parentNode", "targetTopic", "messageType", "executionOrder", "active", "createdBy",
				"createdDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static CBabPolicyActionMaskROS createSampleForNode(final CBabROSNode parentNode) throws Exception {
		Check.notNull(parentNode, "Parent ROS node cannot be null");
		Check.notNull(parentNode.getId(), "Parent ROS node must be persisted before creating sample action mask");
		final CBabPolicyActionMaskROSService service = CSpringContext.getBean(CBabPolicyActionMaskROSService.class);
		final List<CBabPolicyActionMaskROS> existingMasks = service.listByParentNode(parentNode);
		if (!existingMasks.isEmpty()) {
			return existingMasks.get(0);
		}
		CBabPolicyActionMaskROS mask = new CBabPolicyActionMaskROS(parentNode.getName() + sampleNameSuffix, parentNode);
		mask.setDescription("Sample ROS action mask for node '" + parentNode.getName() + "'.");
		mask.setExecutionOrder(10);
		mask.setTargetTopic("/actions/event");
		mask.setMessageType("std_msgs/String");
		mask.setMessageTemplateJson("{\"data\":\"${value}\"}");
		mask.setMaskConfigurationJson("{\"mode\":\"ros-publish\"}");
		mask.setMaskTemplateJson("{\"template\":\"default\"}");
		return service.save(mask);
	}

	private CBabPolicyActionMaskROSInitializerService() {
		// Utility class
	}
}
