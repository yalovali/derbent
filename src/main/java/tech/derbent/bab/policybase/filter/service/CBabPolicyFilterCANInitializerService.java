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
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCAN;
import tech.derbent.bab.policybase.node.can.CBabCanNode;
import tech.derbent.bab.policybase.node.can.CBabCanNodeService;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** Initializer for CAN policy filters. */
@Service
@Profile({"bab", "default", "test"})
public final class CBabPolicyFilterCANInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyFilterCAN> clazz = CBabPolicyFilterCAN.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyFilterCANInitializerService.class);
	private static final String menuOrder = Menu_Order_POLICIES + ".999.32";
	private static final String menuTitle = MenuTitle_POLICIES + ".Developer.Filters.CAN";
	private static final String pageDescription = "Manage CAN filters for frame-id and payload filtering";
	private static final String pageTitle = "CAN Policy Filters";
	private static final String SAMPLE_FILTER_NAME_SUFFIX = " Filter";
	private static final boolean showInQuickToolbar = false;

	private static String buildSampleFilterName(final CBabCanNode parentNode) {
		return parentNode.getName() + SAMPLE_FILTER_NAME_SUFFIX;
	}

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		scr.addScreenLine(CDetailLinesService.createSection("CAN Matching"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "canFrameIdRegularExpression"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "canPayloadRegularExpression"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "requireExtendedFrame"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "protocolVariableNames"));
		// scr.addScreenLine(CDetailLinesService.createSection("Processing Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logExecution"));
		scr.addScreenLine(CDetailLinesService.createSection("Node Compatibility"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "canNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "modbusNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "httpNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fileNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "syslogNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "rosNodeEnabled"));
		CAttachmentInitializerService.addDefaultSection(scr, clazz);
		CLinkInitializerService.addDefaultSection(scr, clazz);
		CCommentInitializerService.addDefaultSection(scr, clazz);
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("name", "parentNode", "canFrameIdRegularExpression", "canPayloadRegularExpression", "requireExtendedFrame",
				"createdBy", "createdDate"));
		return grid;
	}

	public static CBabPolicyFilterCAN createSampleForNode(final CBabCanNode parentNode) throws Exception {
		Check.notNull(parentNode, "Parent CAN node cannot be null");
		Check.notNull(parentNode.getId(), "Parent CAN node must be persisted before creating sample filter");
		final CBabPolicyFilterCANService service = CSpringContext.getBean(CBabPolicyFilterCANService.class);
		final CBabCanNodeService canNodeService = CSpringContext.getBean(CBabCanNodeService.class);
		final List<CBabPolicyFilterCAN> existingFilters = service.listByParentNode(parentNode);
		if (!existingFilters.isEmpty()) {
			final CBabPolicyFilterCAN existing = existingFilters.get(0);
			populateProtocolVariablesFromA2L(existing, parentNode, canNodeService);
			return service.save(existing);
		}
		CBabPolicyFilterCAN filter = new CBabPolicyFilterCAN(buildSampleFilterName(parentNode), parentNode);
		filter.setCanFrameIdRegularExpression(CBabPolicyFilterCAN.DEFAULT_CAN_FRAME_ID_REGULAR_EXPRESSION);
		filter.setCanPayloadRegularExpression(CBabPolicyFilterCAN.DEFAULT_CAN_PAYLOAD_REGULAR_EXPRESSION);
		filter.setRequireExtendedFrame(false);
		filter.setCanNodeEnabled(true);
		filter.setModbusNodeEnabled(false);
		filter.setHttpNodeEnabled(false);
		filter.setFileNodeEnabled(false);
		filter.setSyslogNodeEnabled(false);
		filter.setRosNodeEnabled(false);
		populateProtocolVariablesFromA2L(filter, parentNode, canNodeService);
		filter = service.save(filter);
		LOGGER.info("Created sample CAN policy filter '{}' for node '{}'", filter.getName(), parentNode.getName());
		return filter;
	}

	private static void populateProtocolVariablesFromA2L(final CBabPolicyFilterCAN filter, final CBabCanNode parentNode,
			final CBabCanNodeService canNodeService) {
		if (filter == null || parentNode == null || canNodeService == null) {
			return;
		}
		final String protocolJson = canNodeService.getOrLoadProtocolFileJson(parentNode, false);
		final List<String> protocolVariables = canNodeService.extractProtocolVariableNames(protocolJson).stream().limit(25).toList();
		if (protocolVariables.isEmpty()) {
			LOGGER.warn("No protocol variables available from A2L for CAN filter '{}' on node '{}'", filter.getName(), parentNode.getName());
			return;
		}
		filter.setProtocolVariableNames(protocolVariables);
		LOGGER.info("Loaded {} protocol variables from A2L into CAN filter '{}' (nodeId={})",
				protocolVariables.size(), filter.getName(), parentNode.getId());
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder, null);
	}

	private CBabPolicyFilterCANInitializerService() {
		// Utility class
	}
}
