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
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskFile;
import tech.derbent.bab.policybase.node.file.CBabFileOutputNode;

@Service
@Profile ("bab")
public final class CBabPolicyActionMaskFileInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyActionMaskFile> clazz = CBabPolicyActionMaskFile.class;
	private static final String menuOrder = Menu_Order_POLICIES + ".42";
	private static final String menuTitle = MenuTitle_POLICIES + ".ActionMasks.File";
	private static final String pageDescription = "Manage file-output action masks for destination file output nodes";
	private static final String pageTitle = "File Action Masks";
	private static final String sampleNameSuffix = " File Mask";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		scr.addScreenLine(CDetailLinesService.createSection("Mask Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentNode"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionOrder"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "outputFilePattern"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "serializationMode"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maskConfigurationJson"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maskTemplateJson"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "parentNode", "outputFilePattern", "serializationMode", "executionOrder", "active",
				"createdBy", "createdDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static CBabPolicyActionMaskFile createSampleForNode(final CBabFileOutputNode parentNode) throws Exception {
		Check.notNull(parentNode, "Parent file output node cannot be null");
		Check.notNull(parentNode.getId(), "Parent file output node must be persisted before creating sample action mask");
		final CBabPolicyActionMaskFileService service = CSpringContext.getBean(CBabPolicyActionMaskFileService.class);
		final List<CBabPolicyActionMaskFile> existingMasks = service.listByParentNode(parentNode);
		if (!existingMasks.isEmpty()) {
			return existingMasks.get(0);
		}
		CBabPolicyActionMaskFile mask = new CBabPolicyActionMaskFile(parentNode.getName() + sampleNameSuffix, parentNode);
		mask.setDescription("Sample file action mask for node '" + parentNode.getName() + "'.");
		mask.setExecutionOrder(10);
		mask.setOutputFilePattern("action_*.json");
		mask.setSerializationMode("JSON_APPEND");
		mask.setMaskConfigurationJson("{\"mode\":\"file-append\"}");
		mask.setMaskTemplateJson("{\"template\":\"default\"}");
		return service.save(mask);
	}

	private CBabPolicyActionMaskFileInitializerService() {
		// Utility class
	}
}
