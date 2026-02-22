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
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterROS;
import tech.derbent.bab.policybase.node.ros.CBabROSNode;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** Initializer for ROS policy filters. */
@Service
@Profile ("bab")
public final class CBabPolicyFilterROSInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyFilterROS> clazz = CBabPolicyFilterROS.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyFilterROSInitializerService.class);
	private static final String menuOrder = Menu_Order_POLICIES + ".999.33";
	private static final String menuTitle = MenuTitle_POLICIES + ".Developer.Filters.ROS";
	private static final String pageDescription = "Manage ROS topic and message-type filters";
	private static final String pageTitle = "ROS Policy Filters";
	private static final String SAMPLE_FILTER_NAME_SUFFIX = " Filter";
	private static final boolean showInQuickToolbar = false;

	private static String buildSampleFilterName(final CBabROSNode parentNode) {
		return parentNode.getName() + SAMPLE_FILTER_NAME_SUFFIX;
	}

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		scr.addScreenLine(CDetailLinesService.createSection("ROS Matching"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "topicRegularExpression"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "messageTypePattern"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "namespaceFilter"));
		scr.addScreenLine(CDetailLinesService.createSection("Processing Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maxProcessingTimeMs"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "cacheSizeLimit"));
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
		grid.setColumnFields(List.of("name", "parentNode", "topicRegularExpression", "messageTypePattern", "namespaceFilter", "createdBy",
				"createdDate"));
		return grid;
	}

	public static CBabPolicyFilterROS createSampleForNode(final CBabROSNode parentNode) throws Exception {
		Check.notNull(parentNode, "Parent ROS node cannot be null");
		Check.notNull(parentNode.getId(), "Parent ROS node must be persisted before creating sample filter");
		final CBabPolicyFilterROSService service = CSpringContext.getBean(CBabPolicyFilterROSService.class);
		final List<CBabPolicyFilterROS> existingFilters = service.listByParentNode(parentNode);
		if (!existingFilters.isEmpty()) {
			return existingFilters.get(0);
		}
		CBabPolicyFilterROS filter = new CBabPolicyFilterROS(buildSampleFilterName(parentNode), parentNode);
		filter.setTopicRegularExpression(CBabPolicyFilterROS.DEFAULT_TOPIC_REGULAR_EXPRESSION);
		filter.setMessageTypePattern(CBabPolicyFilterROS.DEFAULT_MESSAGE_TYPE_PATTERN);
		filter.setNamespaceFilter(resolveNamespaceFilter(parentNode));
		filter.setCanNodeEnabled(false);
		filter.setModbusNodeEnabled(false);
		filter.setHttpNodeEnabled(false);
		filter.setFileNodeEnabled(false);
		filter.setSyslogNodeEnabled(false);
		filter.setRosNodeEnabled(true);
		filter = service.save(filter);
		LOGGER.info("Created sample ROS policy filter '{}' for node '{}'", filter.getName(), parentNode.getName());
		return filter;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	private static String resolveNamespaceFilter(final CBabROSNode parentNode) {
		final String namespace = parentNode.getNodeNamespace();
		return namespace == null || namespace.isBlank() ? CBabPolicyFilterROS.DEFAULT_NAMESPACE_FILTER : namespace.trim();
	}

	private CBabPolicyFilterROSInitializerService() {
		// Utility class
	}
}
