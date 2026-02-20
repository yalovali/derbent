package tech.derbent.bab.policybase.node.ros;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterROSInitializerService;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** CBabROSNodeInitializerService - Initializer for ROS nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent pattern:
 * Entity initializer with UI definition. Creates dynamic pages and grids for ROS node management. Defines form layout with node configuration and
 * ROS-specific fields. */
@Service
@Profile ("bab")
public final class CBabROSNodeInitializerService extends CInitializerServiceBase {

	private static final Class<CBabROSNode> clazz = CBabROSNode.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabROSNodeInitializerService.class);

	/** Create detail view with all ROS node fields. */
	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "physicalInterface"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "connectionStatus"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priorityLevel"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "rosMasterUri"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "rosMasterPort"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "rosVersion"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "nodeNamespace"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "topics"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "services"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "queueSize"));
		// Advanced Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("Advanced"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "nodeConfigJson"));
		// Standard composition sections
		CAttachmentInitializerService.addDefaultSection(scr, clazz);
		CCommentInitializerService.addDefaultSection(scr, clazz);
		CLinkInitializerService.addDefaultSection(scr, clazz);
		return scr;
	}

	/** Create grid entity with standard configuration. */
	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "physicalInterface", "active", "connectionStatus", "rosMasterPort", "rosVersion", "nodeNamespace",
				"createdBy", "createdDate"));
		return grid;
	}

	/** Initialize ROS node pages for project. Creates menu entry, grid, and detail views. */
	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, "Policies.ROS Nodes", // Menu title
																																		// (hierarchical)
				CBabROSNode.VIEW_NAME, // Page title
				"ROS virtual network nodes for robotics and autonomous systems communication", // Description
				true, // Show in toolbar
				"10.70"); // Menu order
	}

	/** Initialize sample ROS nodes for project. Creates sample nodes for robotics communication. */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		LOGGER.info("Initializing ROS Node sample data for project: {}", project.getName());
		final CBabROSNodeService service = (CBabROSNodeService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		// Check if sample nodes already exist
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("ROS nodes already exist for project: {}", project.getName());
			return;
		}
		// Sample ROS Node 1 - ROS1 Master
		CBabROSNode node1 = new CBabROSNode("ROS1 Master", project);
		node1.setNodeNamespace("/robot1");
		node1.setTopics("/cmd_vel,/odom,/scan");
		node1.setServices("/get_state,/set_mode");
		node1.setConnectionStatus("CONNECTED");
		node1.setPriorityLevel(90);
		node1 = service.save(node1);
		CBabPolicyFilterROSInitializerService.createSampleForNode(node1);
		LOGGER.info("Created sample ROS node: {}", node1.getName());
		if (minimal) {
			return;
		}
		// Sample ROS Node 2 - ROS2 Master
		CBabROSNode node2 = new CBabROSNode("ROS2 Master", project);
		node2.setPhysicalInterface("eth1");
		node2.setRosMasterUri("http://localhost:11312");
		node2.setRosMasterPort(11312);
		node2.setRosVersion("ROS2");
		node2.setNodeNamespace("/robot2");
		node2.setTopics("/cmd_vel,/odom,/camera/image");
		node2.setServices("/get_map,/save_map");
		node2.setQueueSize(20);
		node2.setConnectionStatus("CONNECTED");
		node2.setPriorityLevel(80);
		node2 = service.save(node2);
		CBabPolicyFilterROSInitializerService.createSampleForNode(node2);
		LOGGER.info("Created sample ROS node: {}", node2.getName());
	}
}
