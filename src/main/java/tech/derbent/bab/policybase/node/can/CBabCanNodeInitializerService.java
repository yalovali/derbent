package tech.derbent.bab.policybase.node.can;

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
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterCANInitializerService;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** CBabCanNodeInitializerService - Initializer for CAN Bus nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent
 * pattern: Entity initializer with UI definition. Creates dynamic pages and grids for CAN node management. Defines form layout with node
 * configuration and CAN-specific fields. */
@Service
@Profile ("bab")
public final class CBabCanNodeInitializerService extends CInitializerServiceBase {

	private static final Class<CBabCanNode> clazz = CBabCanNode.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabCanNodeInitializerService.class);

	/** Create detail view with all CAN node fields. */
	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "physicalInterface"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "connectionStatus"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priorityLevel"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "bitrate"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "protocolType"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentProtocolFileData"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentCanPolicyFilters"));
		scr.addScreenLine(CDetailLinesService.createSection("Advanced CAN Settings"));
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
		grid.setColumnFields(
				List.of("id", "name", "physicalInterface", "active", "connectionStatus", "bitrate", "protocolType", "createdBy", "createdDate"));
		return grid;
	}

	/** Initialize CAN node pages for project. Creates menu entry, grid, and detail views. */
	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, "Policies.CAN Bus Nodes",
				CBabCanNode.VIEW_NAME, "CAN bus virtual network nodes for vehicle communication and automotive applications", true, "10.30");
	}

	/** Initialize sample CAN nodes for project. Creates sample nodes for vehicle communication and testing. */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		LOGGER.info("Initializing CAN Bus Node sample data for project: {}", project.getName());
		final CBabCanNodeService service = (CBabCanNodeService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		// Check if sample nodes already exist
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("CAN nodes already exist for project: {}", project.getName());
			return;
		}
		// Sample CAN Node 1 - High Speed CAN (500 kbps)
		CBabCanNode node1 = new CBabCanNode("CAN HS", project);
		node1.setBitrate(500000);
		node1.setProtocolType("XCP"); // XCP protocol for measurement and calibration
		node1.setConnectionStatus("CONNECTED");
		node1.setPriorityLevel(90);
		node1 = service.save(node1);
		CBabPolicyFilterCANInitializerService.createSampleForNode(node1);
		LOGGER.info("Created sample CAN node: {}", node1.getName());
		if (minimal) {
			return;
		}
		// Sample CAN Node 1 - High Speed CAN (500 kbps)
		CBabCanNode node3 = new CBabCanNode("CAN UDS", project);
		node3.setBitrate(500000);
		node3.setProtocolType("UDS"); // XCP protocol for measurement and calibration
		node3.setConnectionStatus("CONNECTED");
		node3.setPriorityLevel(90);
		node3 = service.save(node3);
		CBabPolicyFilterCANInitializerService.createSampleForNode(node3);
		LOGGER.info("Created sample CAN node: {}", node3.getName());
		// Sample CAN Node 2 - Low Speed CAN (125 kbps)
		CBabCanNode node2 = new CBabCanNode("CAN LS", project);
		node2.setPhysicalInterface("can1");
		node2.setBitrate(125000);
		node2.setProtocolType("UDS"); // UDS protocol for diagnostics
		node2.setConnectionStatus("CONNECTED");
		node2.setPriorityLevel(80);
		node2 = service.save(node2);
		CBabPolicyFilterCANInitializerService.createSampleForNode(node2);
		LOGGER.info("Created sample CAN node: {}", node2.getName());
	}
}
