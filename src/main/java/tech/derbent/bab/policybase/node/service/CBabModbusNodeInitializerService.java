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
import tech.derbent.bab.policybase.node.domain.CBabModbusNode;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/**
 * CBabModbusNodeInitializerService - Initializer for Modbus nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Entity initializer with UI definition.
 * 
 * Creates dynamic pages and grids for Modbus node management.
 * Defines form layout with node configuration and Modbus-specific fields.
 */
@Service
@Profile("bab")
public final class CBabModbusNodeInitializerService extends CInitializerServiceBase {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabModbusNodeInitializerService.class);
	private static final Class<CBabModbusNode> clazz = CBabModbusNode.class;
	
	/**
	 * Initialize Modbus node pages for project.
	 * Creates menu entry, grid, and detail views.
	 */
	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
			"Network.Modbus Nodes",           // Menu title (hierarchical)
			CBabModbusNode.VIEW_NAME,         // Page title
			"Modbus RTU/ASCII virtual network nodes for industrial device communication",  // Description
			true,                              // Show in toolbar
			"10.40");                          // Menu order
	}
	
	/**
	 * Create detail view with all Modbus node fields.
	 */
	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		
		// Base Node Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("Node Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "physicalInterface"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "connectionStatus"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priorityLevel"));
		
		// Modbus Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("Modbus Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "slaveId"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "modbusMode"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "timeoutMs"));
		
		// Serial Port Settings Section
		scr.addScreenLine(CDetailLinesService.createSection("Serial Port Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "baudrate"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "dataBits"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "stopBits"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parity"));
		
		// Advanced Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("Advanced"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "nodeConfigJson"));
		
		// Standard composition sections
		CAttachmentInitializerService.addDefaultSection(scr, clazz);
		CCommentInitializerService.addDefaultSection(scr, clazz);
		CLinkInitializerService.addDefaultSection(scr, clazz);
		
		return scr;
	}
	
	/**
	 * Create grid entity with standard configuration.
	 */
	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "physicalInterface", "isActive", 
			"connectionStatus", "slaveId", "baudrate", "modbusMode", "createdBy", "createdDate"));
		return grid;
	}
	
	/**
	 * Initialize sample Modbus nodes for project.
	 * Creates sample nodes for industrial device communication.
	 */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		LOGGER.info("Initializing Modbus Node sample data for project: {}", project.getName());
		
		final CBabModbusNodeService service = 
			(CBabModbusNodeService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		
		// Check if sample nodes already exist
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("Modbus nodes already exist for project: {}", project.getName());
			return;
		}
		
		// Sample Modbus Node 1 - RTU Mode
		CBabModbusNode node1 = new CBabModbusNode("Modbus RTU Slave 1", project);
		node1.setPhysicalInterface("/dev/ttyS0");
		node1.setSlaveId(1);
		node1.setBaudrate(9600);
		node1.setDataBits(8);
		node1.setStopBits(1);
		node1.setParity("NONE");
		node1.setModbusMode("RTU");
		node1.setTimeoutMs(1000);
		node1.setIsActive(true);
		node1.setConnectionStatus("CONNECTED");
		node1.setPriorityLevel(90);
		node1 = service.save(node1);
		LOGGER.info("Created sample Modbus node: {}", node1.getName());
		
		if (minimal) {
			return;
		}
		// Sample Modbus Node 2 - ASCII Mode
		CBabModbusNode node2 = new CBabModbusNode("Modbus ASCII Slave 2", project);
		node2.setPhysicalInterface("/dev/ttyUSB0");
		node2.setSlaveId(2);
		node2.setBaudrate(19200);
		node2.setDataBits(7);
		node2.setStopBits(1);
		node2.setParity("EVEN");
		node2.setModbusMode("ASCII");
		node2.setTimeoutMs(2000);
		node2.setIsActive(true);
		node2.setConnectionStatus("CONNECTED");
		node2.setPriorityLevel(80);
		node2 = service.save(node2);
		LOGGER.info("Created sample Modbus node: {}", node2.getName());
	}
}
