package tech.derbent.bab.policybase.node.service;

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
import tech.derbent.bab.policybase.node.domain.CBabTCPModbusNode;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** CBabTCPModbusNodeInitializerService - Initializer for TCP Modbus nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following
 * Derbent pattern: Entity initializer with UI definition. Creates dynamic pages and grids for Modbus TCP node management. Defines form layout with
 * node configuration and Modbus TCP-specific fields. */
@Service
@Profile ("bab")
public final class CBabTCPModbusNodeInitializerService extends CInitializerServiceBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabTCPModbusNodeInitializerService.class);
	private static final Class<CBabTCPModbusNode> clazz = CBabTCPModbusNode.class;

	/** Create detail view with all TCP Modbus node fields. */
	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		// Base Node Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("Node Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "physicalInterface"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "connectionStatus"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priorityLevel"));
		// Modbus TCP Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("Modbus TCP Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "serverAddress"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "serverPort"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "unitId"));
		// TCP Connection Settings Section
		scr.addScreenLine(CDetailLinesService.createSection("Connection Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "connectionTimeoutMs"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "responseTimeoutMs"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maxConnections"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "keepAlive"));
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
		grid.setColumnFields(List.of("id", "name", "physicalInterface", "isActive", "connectionStatus", "serverAddress", "serverPort", "unitId",
				"createdBy", "createdDate"));
		return grid;
	}

	/** Initialize TCP Modbus node pages for project. Creates menu entry, grid, and detail views. */
	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, "Policies.TCP Modbus Nodes", // Menu
																																				// title
																																				// (hierarchical)
				CBabTCPModbusNode.VIEW_NAME, // Page title
				"Modbus TCP virtual network nodes for industrial Ethernet device communication", // Description
				true, // Show in toolbar
				"10.60"); // Menu order
	}

	/** Initialize sample TCP Modbus nodes for project. Creates sample nodes for industrial Ethernet communication. */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		LOGGER.info("Initializing TCP Modbus Node sample data for project: {}", project.getName());
		final CBabTCPModbusNodeService service = (CBabTCPModbusNodeService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		// Check if sample nodes already exist
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("TCP Modbus nodes already exist for project: {}", project.getName());
			return;
		}
		// Sample TCP Modbus Node 1 - Standard Port
		CBabTCPModbusNode node1 = new CBabTCPModbusNode("Modbus TCP Server", project);
		node1.setPhysicalInterface("eth0");
		node1.setServerAddress("192.168.1.100");
		node1.setServerPort(502);
		node1.setUnitId(1);
		node1.setConnectionTimeoutMs(5000);
		node1.setResponseTimeoutMs(1000);
		node1.setMaxConnections(5);
		node1.setKeepAlive(true);
		node1.setIsActive(true);
		node1.setConnectionStatus("CONNECTED");
		node1.setPriorityLevel(90);
		node1 = service.save(node1);
		LOGGER.info("Created sample TCP Modbus node: {}", node1.getName());
		if (minimal) {
			return;
		}
		// Sample TCP Modbus Node 2 - Gateway
		CBabTCPModbusNode node2 = new CBabTCPModbusNode("Modbus TCP Gateway", project);
		node2.setPhysicalInterface("eth1");
		node2.setServerAddress("192.168.1.101");
		node2.setServerPort(502);
		node2.setUnitId(2);
		node2.setConnectionTimeoutMs(10000);
		node2.setResponseTimeoutMs(2000);
		node2.setMaxConnections(10);
		node2.setKeepAlive(true);
		node2.setIsActive(true);
		node2.setConnectionStatus("CONNECTED");
		node2.setPriorityLevel(80);
		node2 = service.save(node2);
		LOGGER.info("Created sample TCP Modbus node: {}", node2.getName());
	}
}
