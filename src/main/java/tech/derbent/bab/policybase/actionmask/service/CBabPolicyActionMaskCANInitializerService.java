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
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskCAN;
import tech.derbent.bab.policybase.node.can.CBabCanNode;

@Service
@Profile({"bab", "default", "test"})
public final class CBabPolicyActionMaskCANInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyActionMaskCAN> clazz = CBabPolicyActionMaskCAN.class;
	private static final String menuOrder = Menu_Order_POLICIES + ".999.41";
	private static final String menuTitle = MenuTitle_POLICIES + ".Developer.ActionMasks.CAN";
	private static final String pageDescription = "Manage CAN action masks for destination CAN nodes";
	private static final String pageTitle = "CAN Action Masks";
	private static final String sampleNameSuffix = " CAN Mask";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		// CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		scr.addScreenLine(CDetailLinesService.createSection("Output Methods"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "outputMethod"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "outputActionMappings"));
		scr.addScreenLine(CDetailLinesService.createSection("Mask Settings"));
		// scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "policyAction"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionOrder"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "targetFrameIdHex"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "payloadTemplateJson"));
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("name", "policyAction", "targetFrameIdHex", "executionOrder", "createdBy", "createdDate"));
		return grid;
	}

	public static CBabPolicyActionMaskCAN createSampleForAction(final CBabPolicyAction policyAction) throws Exception {
		Check.notNull(policyAction, "Policy action cannot be null");
		Check.notNull(policyAction.getId(), "Policy action must be persisted before creating sample action mask");
		Check.isTrue(policyAction.getDestinationNode() instanceof CBabCanNode,
				"Policy action destination must be CAN node for CAN action mask");
		final CBabPolicyActionMaskCAN existingMask = policyAction.getActionMask() instanceof CBabPolicyActionMaskCAN ? (CBabPolicyActionMaskCAN) policyAction.getActionMask() : null;
		if (existingMask != null) {
			return existingMask;
		}
		final CBabPolicyActionMaskCANService service = CSpringContext.getBean(CBabPolicyActionMaskCANService.class);
		final CBabPolicyActionMaskCAN mask = new CBabPolicyActionMaskCAN(policyAction.getName() + sampleNameSuffix, policyAction);
		mask.setExecutionOrder(10);
		mask.setOutputMethod(CBabPolicyActionMaskCAN.OUTPUT_METHOD_XCP_DOWNLOAD);
		mask.setTargetFrameIdHex("0x100");
		mask.setPayloadTemplateJson("{\"value\":\"${value}\"}");
		return service.save(mask);
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder, null);
	}

	private CBabPolicyActionMaskCANInitializerService() {
		// Utility class
	}
}
