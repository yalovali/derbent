package tech.derbent.bab.policybase.node.can;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
@Profile({"bab", "default", "test"})
public final class CBabCanNodeInitializerService extends CInitializerServiceBase {

	private static final Class<CBabCanNode> clazz = CBabCanNode.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabCanNodeInitializerService.class);
	private static final Path SAMPLE_A2L_MIN_PATH = Path.of("others/protocolsamples/ECU_Variables_Min.a2l");
	private static final Path SAMPLE_A2L_FULL_PATH = Path.of("others/protocolsamples/ECU_Variables.a2l");

	/** Create detail view with all CAN node fields. */
	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "physicalInterface"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "connectionStatus"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "bitrate"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "protocolType"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentProtocolFileData"));
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
		node1 = service.save(node1);
		node1 = loadSampleA2LProtocol(node1, service, true);
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
		node3 = service.save(node3);
		node3 = loadSampleA2LProtocol(node3, service, false);
		CBabPolicyFilterCANInitializerService.createSampleForNode(node3);
		LOGGER.info("Created sample CAN node: {}", node3.getName());
		// Sample CAN Node 2 - Low Speed CAN (125 kbps)
		CBabCanNode node2 = new CBabCanNode("CAN LS", project);
		node2.setPhysicalInterface("can1");
		node2.setBitrate(125000);
		node2.setProtocolType("UDS"); // UDS protocol for diagnostics
		node2.setConnectionStatus("CONNECTED");
		node2 = service.save(node2);
		node2 = loadSampleA2LProtocol(node2, service, true);
		CBabPolicyFilterCANInitializerService.createSampleForNode(node2);
		LOGGER.info("Created sample CAN node: {}", node2.getName());
	}

	private static CBabCanNode loadSampleA2LProtocol(final CBabCanNode node, final CBabCanNodeService service, final boolean preferMinFile) {
		if (node == null || service == null) {
			return node;
		}
		final Path preferred = preferMinFile ? SAMPLE_A2L_MIN_PATH : SAMPLE_A2L_FULL_PATH;
		final Path fallback = preferMinFile ? SAMPLE_A2L_FULL_PATH : SAMPLE_A2L_MIN_PATH;
		final Path a2lPath = Files.exists(preferred) ? preferred : Files.exists(fallback) ? fallback : null;
		if (a2lPath == null) {
			LOGGER.warn("No sample A2L file found. Checked '{}' and '{}'", preferred, fallback);
			return node;
		}
		try {
			final String protocolContent = Files.readString(a2lPath, StandardCharsets.UTF_8);
			final String parsedJson = service.parseA2LContentAsJson(protocolContent);
			node.setProtocolFileData(protocolContent);
			node.setProtocolFileJson(parsedJson);
			node.setNodeConfigJson(parsedJson);
			node.setProtocolFileSummaryJson(service.createParsedSummaryJson(parsedJson, protocolContent.getBytes(StandardCharsets.UTF_8).length));
			LOGGER.info("Loaded sample A2L '{}' into CAN node '{}' (nodeId={})", a2lPath, node.getName(), node.getId());
			return service.save(node);
		} catch (final Exception e) {
			node.setProtocolFileSummaryJson(service.createParseErrorSummaryJson("Sample A2L load failed: " + e.getMessage(), 0L));
			LOGGER.error("Failed to load sample A2L '{}' into CAN node '{}' reason={}", a2lPath, node.getName(), e.getMessage());
			return service.save(node);
		}
	}
}
