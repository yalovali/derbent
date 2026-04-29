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
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskROS;
import tech.derbent.bab.policybase.node.ros.CBabROSNode;

@Service
@Profile ({
		"bab", "default", "test"
})
public final class CBabPolicyActionMaskROSInitializerService extends CBabPolicyActionMaskBaseInitializationService {

	private static final Class<CBabPolicyActionMaskROS> clazz = CBabPolicyActionMaskROS.class;
	private static final String menuOrder = Menu_Order_POLICIES + ".999.43";
	private static final String menuTitle = MenuTitle_POLICIES + ".Developer.ActionMasks.ROS";
	private static final String pageDescription = "Manage ROS action masks for destination ROS nodes";
	private static final String pageTitle = "ROS Action Masks";
	private static final String sampleNameSuffix = " ROS Mask";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		// CEntityNamedInitializerService.createBasicView(scr, clazz, project, true);
		scr.addScreenLine(CDetailLinesService.createSection("Output Methods"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "outputMethod"));
		scr.addScreenLine(CDetailLinesService.createSection("Mask Settings"));
		// scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "policyAction"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionOrder"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "targetTopic"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "messageType"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "messageTemplateJson"));
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("name", "policyAction", "targetTopic", "messageType", "executionOrder",
				"createdBy", "createdDate"));
		return grid;
	}

	public static CBabPolicyActionMaskROS createSampleForAction(final CBabPolicyAction policyAction) throws Exception {
		Check.notNull(policyAction, "Policy action cannot be null");
		Check.notNull(policyAction.getId(), "Policy action must be persisted before creating sample action mask");
		Check.isTrue(policyAction.getDestinationNode() instanceof CBabROSNode,
				"Policy action destination must be ROS node for ROS action mask");
		final CBabPolicyActionMaskROS existingMask = policyAction.getActionMask() instanceof CBabPolicyActionMaskROS
				? (CBabPolicyActionMaskROS) policyAction.getActionMask() : null;
		if (existingMask != null) {
			return existingMask;
		}
		final CBabPolicyActionMaskROSService service = CSpringContext.getBean(CBabPolicyActionMaskROSService.class);
		final CBabPolicyActionMaskROS mask =
				new CBabPolicyActionMaskROS(policyAction.getName() + sampleNameSuffix, policyAction);
		mask.setExecutionOrder(10);
		mask.setTargetTopic("/actions/event");
		mask.setMessageType("std_msgs/String");
		mask.setMessageTemplateJson("{\"data\":\"${value}\"}");
		return service.save(mask);
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService)
			throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
				menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder, null);
	}

	private CBabPolicyActionMaskROSInitializerService() {
		// Utility class
	}
}
