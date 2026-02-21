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
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskCAN;
import tech.derbent.bab.policybase.node.can.CBabCanNode;

@Service
@Profile ("bab")
public final class CBabPolicyActionMaskCANInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyActionMaskCAN> clazz = CBabPolicyActionMaskCAN.class;
	private static final String menuOrder = Menu_Order_POLICIES + ".41";
	private static final String menuTitle = MenuTitle_POLICIES + ".ActionMasks.CAN";
	private static final String pageDescription = "Manage CAN action masks for destination CAN nodes";
	private static final String pageTitle = "CAN Action Masks";
	private static final String sampleNameSuffix = " CAN Mask";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		scr.addScreenLine(CDetailLinesService.createSection("Mask Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentNode"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionOrder"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "targetFrameIdHex"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "payloadTemplateJson"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maskConfigurationJson"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maskTemplateJson"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "parentNode", "targetFrameIdHex", "executionOrder", "active", "createdBy", "createdDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static CBabPolicyActionMaskCAN createSampleForNode(final CBabCanNode parentNode) throws Exception {
		Check.notNull(parentNode, "Parent CAN node cannot be null");
		Check.notNull(parentNode.getId(), "Parent CAN node must be persisted before creating sample action mask");
		final CBabPolicyActionMaskCANService service = CSpringContext.getBean(CBabPolicyActionMaskCANService.class);
		final List<CBabPolicyActionMaskCAN> existingMasks = service.listByParentNode(parentNode);
		if (!existingMasks.isEmpty()) {
			return existingMasks.get(0);
		}
		CBabPolicyActionMaskCAN mask = new CBabPolicyActionMaskCAN(parentNode.getName() + sampleNameSuffix, parentNode);
		mask.setDescription("Sample CAN action mask for node '" + parentNode.getName() + "'.");
		mask.setExecutionOrder(10);
		mask.setTargetFrameIdHex("0x100");
		mask.setPayloadTemplateJson("{\"value\":\"${value}\"}");
		mask.setMaskConfigurationJson("{\"mode\":\"can-forward\"}");
		mask.setMaskTemplateJson("{\"template\":\"default\"}");
		return service.save(mask);
	}

	private CBabPolicyActionMaskCANInitializerService() {
		// Utility class
	}
}
