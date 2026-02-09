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
import tech.derbent.bab.policybase.node.domain.CVehicleNode;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** CVehicleNodeInitializerService - Initializer for Vehicle nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent
 * pattern: Entity initializer with UI definition. Creates dynamic pages and grids for Vehicle node management. Defines form layout with node
 * configuration and vehicle-specific fields. */
@Service
@Profile ("bab")
public final class CVehicleNodeInitializerService extends CInitializerServiceBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(CVehicleNodeInitializerService.class);
	private static final Class<CVehicleNode> clazz = CVehicleNode.class;

	/** Create detail view with all Vehicle node fields. */
	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		// Base Node Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("Node Configuration"));
		// NOTE: nodeType is managed by @DiscriminatorColumn - displayed via getNodeType() which returns class name
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "physicalInterface"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "connectionStatus"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priorityLevel"));
		// Vehicle Identification Section
		scr.addScreenLine(CDetailLinesService.createSection("Vehicle Identification"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "vehicleId"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "vehicleType"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "manufacturer"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "modelYear"));
		// CAN Bus Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("CAN Bus Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "canAddress"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "baudRate"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "canProtocol"));
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
		// NOTE: Removed nodeType from grid - it's displayed in entity title/class name
		grid.setColumnFields(List.of("id", "name", "physicalInterface", "isActive", "connectionStatus", "vehicleId", "vehicleType", "canAddress",
				"baudRate", "createdBy", "createdDate"));
		return grid;
	}

	/** Initialize Vehicle node pages for project. Creates menu entry, grid, and detail views. */
	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, "Policies.Vehicle Nodes", // Menu
																																			// title
																																			// (hierarchical)
				CVehicleNode.VIEW_NAME, // Page title
				"Vehicle virtual network nodes for CAN bus automotive communication", // Description
				true, // Show in toolbar
				"10.30"); // Menu order
	}

	/** Initialize sample vehicle nodes for project. Creates sample nodes for CAN bus vehicle communication. */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		LOGGER.info("Initializing Vehicle Node sample data for project: {}", project.getName());
		final CVehicleNodeService service = (CVehicleNodeService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		// Check if sample nodes already exist
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("Vehicle nodes already exist for project: {}", project.getName());
			return;
		}
		// Sample Vehicle Node 1 - Electric Vehicle
		CVehicleNode node1 = new CVehicleNode("Electric Vehicle 01", project);
		node1.setPhysicalInterface("can0");
		node1.setVehicleId("EV-2024-001");
		node1.setVehicleType("Electric");
		node1.setManufacturer("Tesla");
		node1.setModelYear(2024);
		node1.setCanAddress(0x123);
		node1.setBaudRate(500000);
		node1.setCanProtocol("CAN 2.0B");
		node1.setIsActive(true);
		node1.setConnectionStatus("CONNECTED");
		node1.setPriorityLevel(95);
		node1 = service.save(node1);
		LOGGER.info("Created sample vehicle node: {}", node1.getName());
		if (!minimal) {
			// Sample Vehicle Node 2 - Truck
			CVehicleNode node2 = new CVehicleNode("Delivery Truck 01", project);
			node2.setPhysicalInterface("can1");
			node2.setVehicleId("TRUCK-2024-002");
			node2.setVehicleType("Truck");
			node2.setManufacturer("Volvo");
			node2.setModelYear(2024);
			node2.setCanAddress(0x456);
			node2.setBaudRate(250000);
			node2.setCanProtocol("CAN 2.0A");
			node2.setIsActive(true);
			node2.setConnectionStatus("CONNECTED");
			node2.setPriorityLevel(85);
			node2 = service.save(node2);
			LOGGER.info("Created sample vehicle node: {}", node2.getName());
		}
	}
}
