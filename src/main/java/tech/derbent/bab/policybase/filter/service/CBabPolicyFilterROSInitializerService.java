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
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterROS;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** Initializer for ROS policy filters. */
@Service
@Profile ("bab")
public final class CBabPolicyFilterROSInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyFilterROS> clazz = CBabPolicyFilterROS.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyFilterROSInitializerService.class);
	private static final String menuOrder = Menu_Order_POLICIES + ".33";
	private static final String menuTitle = MenuTitle_POLICIES + ".Filters.ROS";
	private static final String pageDescription = "Manage ROS topic and message-type filters";
	private static final String pageTitle = "ROS Policy Filters";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		scr.addScreenLine(CDetailLinesService.createSection("ROS Matching"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "topicRegularExpression"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "messageTypePattern"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "namespaceFilter"));
		addCommonSections(scr, clazz);
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "topicRegularExpression", "messageTypePattern", "namespaceFilter", "isEnabled",
				"executionOrder", "cacheEnabled", "createdBy", "createdDate"));
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
		final CBabPolicyFilterROSService service = CSpringContext.getBean(CBabPolicyFilterROSService.class);
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("ROS policy filters already exist for project: {}", project.getName());
			return;
		}
		final CBabPolicyFilterROS rosCommandFilter = new CBabPolicyFilterROS("ROS Command Filter", project);
		rosCommandFilter.setDescription("Accept only cmd_vel and drive topics");
		rosCommandFilter.setTopicRegularExpression("^/(cmd_vel|drive)/.*$");
		rosCommandFilter.setMessageTypePattern(".*Twist.*");
		rosCommandFilter.setNamespaceFilter("/robot1");
		rosCommandFilter.setExecutionOrder(1);
		service.save(rosCommandFilter);
		if (minimal) {
			return;
		}
		final CBabPolicyFilterROS rosTelemetryFilter = new CBabPolicyFilterROS("ROS Telemetry Filter", project);
		rosTelemetryFilter.setDescription("Accept telemetry topics from robot2 namespace");
		rosTelemetryFilter.setTopicRegularExpression("^/telemetry/.*$");
		rosTelemetryFilter.setMessageTypePattern(".*(Odometry|Imu).*" );
		rosTelemetryFilter.setNamespaceFilter("/robot2");
		rosTelemetryFilter.setExecutionOrder(2);
		service.save(rosTelemetryFilter);
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

	private CBabPolicyFilterROSInitializerService() {
		// Utility class
	}
}
