package tech.derbent.bab.policybase.filter.service;

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
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCAN;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** Initializer for CAN policy filters. */
@Service
@Profile ("bab")
public final class CBabPolicyFilterCANInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyFilterCAN> clazz = CBabPolicyFilterCAN.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyFilterCANInitializerService.class);
	private static final String menuOrder = Menu_Order_POLICIES + ".32";
	private static final String menuTitle = MenuTitle_POLICIES + ".Filters.CAN";
	private static final String pageDescription = "Manage CAN filters for frame-id and payload filtering";
	private static final String pageTitle = "CAN Policy Filters";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		scr.addScreenLine(CDetailLinesService.createSection("CAN Matching"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "canFrameIdRegularExpression"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "canPayloadRegularExpression"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "requireExtendedFrame"));
		addCommonSections(scr, clazz);
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "canFrameIdRegularExpression", "canPayloadRegularExpression", "requireExtendedFrame",
				"isEnabled", "executionOrder", "cacheEnabled", "createdBy", "createdDate"));
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
		final CBabPolicyFilterCANService service = CSpringContext.getBean(CBabPolicyFilterCANService.class);
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("CAN policy filters already exist for project: {}", project.getName());
			return;
		}
		final CBabPolicyFilterCAN canFrameFilter = new CBabPolicyFilterCAN("CAN Frame Filter", project);
		canFrameFilter.setDescription("Accept powertrain frames 0x100-0x1FF");
		canFrameFilter.setCanFrameIdRegularExpression("^0x1[0-9A-F]{2}$");
		canFrameFilter.setCanPayloadRegularExpression(".*");
		canFrameFilter.setRequireExtendedFrame(false);
		canFrameFilter.setExecutionOrder(1);
		service.save(canFrameFilter);
		if (minimal) {
			return;
		}
		final CBabPolicyFilterCAN canSafetyFilter = new CBabPolicyFilterCAN("CAN Safety Filter", project);
		canSafetyFilter.setDescription("Accept only extended safety frames carrying brake state");
		canSafetyFilter.setCanFrameIdRegularExpression("^0x18FF[0-9A-F]{4}$");
		canSafetyFilter.setCanPayloadRegularExpression(".*BRAKE.*");
		canSafetyFilter.setRequireExtendedFrame(true);
		canSafetyFilter.setExecutionOrder(2);
		service.save(canSafetyFilter);
	}

	private static void addCommonSections(final CDetailSection scr, final Class<?> entityClass) throws Exception {
		scr.addScreenLine(CDetailLinesService.createSection("Processing Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "isEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "executionOrder"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "logicOperator"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "caseSensitive"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "nullHandling"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "maxProcessingTimeMs"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "cacheEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "cacheSizeLimit"));
		scr.addScreenLine(CDetailLinesService.createSection("Logging Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "logMatches"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "logRejections"));
		scr.addScreenLine(CDetailLinesService.createSection("Node Compatibility"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "canNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "modbusNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "httpNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "fileNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "syslogNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "rosNodeEnabled"));
		CAttachmentInitializerService.addDefaultSection(scr, entityClass);
		CLinkInitializerService.addDefaultSection(scr, entityClass);
		CCommentInitializerService.addDefaultSection(scr, entityClass);
	}

	private CBabPolicyFilterCANInitializerService() {
		// Utility class
	}
}
