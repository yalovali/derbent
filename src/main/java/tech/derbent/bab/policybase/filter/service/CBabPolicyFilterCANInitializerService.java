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
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterBase;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCAN;
import tech.derbent.bab.policybase.node.can.CBabCanNode;
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
	private static final String SAMPLE_FILTER_NAME_SUFFIX = " Filter";
	private static final boolean showInQuickToolbar = false;

	private static void addCommonSections(final CDetailSection scr, final Class<?> entityClass) throws Exception {
		scr.addScreenLine(CDetailLinesService.createSection("Processing Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "active"));
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

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		scr.addScreenLine(CDetailLinesService.createSection("CAN Matching"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "canFrameIdRegularExpression"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "canPayloadRegularExpression"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "requireExtendedFrame"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "protocolVariableNames"));
		addCommonSections(scr, clazz);
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "parentNode", "canFrameIdRegularExpression", "canPayloadRegularExpression",
				"requireExtendedFrame", "active", "executionOrder", "cacheEnabled", "createdBy", "createdDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static CBabPolicyFilterCAN createSampleForNode(final CBabCanNode parentNode) throws Exception {
		Check.notNull(parentNode, "Parent CAN node cannot be null");
		Check.notNull(parentNode.getId(), "Parent CAN node must be persisted before creating sample filter");
		final CBabPolicyFilterCANService service = CSpringContext.getBean(CBabPolicyFilterCANService.class);
		final List<CBabPolicyFilterCAN> existingFilters = service.listByParentNode(parentNode);
		if (!existingFilters.isEmpty()) {
			return existingFilters.get(0);
		}
		CBabPolicyFilterCAN filter = new CBabPolicyFilterCAN(buildSampleFilterName(parentNode), parentNode);
		filter.setDescription("Sample CAN filter for node '" + parentNode.getName() + "'.");
		filter.setCanFrameIdRegularExpression(CBabPolicyFilterCAN.DEFAULT_CAN_FRAME_ID_REGULAR_EXPRESSION);
		filter.setCanPayloadRegularExpression(CBabPolicyFilterCAN.DEFAULT_CAN_PAYLOAD_REGULAR_EXPRESSION);
		filter.setRequireExtendedFrame(false);
		filter.setExecutionOrder(10);
		filter.setLogicOperator(CBabPolicyFilterBase.LOGIC_OPERATOR_AND);
		filter.setNullHandling(CBabPolicyFilterBase.NULL_HANDLING_IGNORE);
		filter.setCanNodeEnabled(true);
		filter.setModbusNodeEnabled(false);
		filter.setHttpNodeEnabled(false);
		filter.setFileNodeEnabled(false);
		filter.setSyslogNodeEnabled(false);
		filter.setRosNodeEnabled(false);
		filter = service.save(filter);
		LOGGER.info("Created sample CAN policy filter '{}' for node '{}'", filter.getName(), parentNode.getName());
		return filter;
	}

	private static String buildSampleFilterName(final CBabCanNode parentNode) {
		return parentNode.getName() + SAMPLE_FILTER_NAME_SUFFIX;
	}

	private CBabPolicyFilterCANInitializerService() {
		// Utility class
	}
}
