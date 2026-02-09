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
import tech.derbent.bab.policybase.node.domain.CBabFileInputNode;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** CBabFileInputNodeInitializerService - Initializer for File Input nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following
 * Derbent pattern: Entity initializer with UI definition. Creates dynamic pages and grids for File Input node management. Defines form layout with
 * node configuration and file monitoring fields. */
@Service
@Profile ("bab")
public final class CBabFileInputNodeInitializerService extends CInitializerServiceBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabFileInputNodeInitializerService.class);
	private static final Class<CBabFileInputNode> clazz = CBabFileInputNode.class;

	/** Create detail view with all File Input node fields. */
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
		// File Input Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("File Input Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "filePath"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fileFormat"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "watchDirectory"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "filePattern"));
		// Monitoring Settings Section
		scr.addScreenLine(CDetailLinesService.createSection("Monitoring Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "pollingIntervalSeconds"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maxFileSizeMb"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "autoDeleteProcessed"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "backupProcessedFiles"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "backupDirectory"));
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
		grid.setColumnFields(List.of("id", "name", "physicalInterface", "isActive", "connectionStatus", "filePath", "fileFormat", "watchDirectory",
				"pollingIntervalSeconds", "createdBy", "createdDate"));
		return grid;
	}

	/** Initialize File Input node pages for project. Creates menu entry, grid, and detail views. */
	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, "Policies.File Input Nodes", // Menu
																																				// title
																																				// (hierarchical)
				CBabFileInputNode.VIEW_NAME, // Page title
				"File input virtual network nodes for file system monitoring and data import", // Description
				true, // Show in toolbar
				"10.40"); // Menu order
	}

	/** Initialize sample file input nodes for project. Creates sample nodes for file system monitoring and data import. */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		LOGGER.info("Initializing File Input Node sample data for project: {}", project.getName());
		final CBabFileInputNodeService service = (CBabFileInputNodeService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		// Check if sample nodes already exist
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("File Input nodes already exist for project: {}", project.getName());
			return;
		}
		// Sample File Input Node 1 - CSV Data Import
		CBabFileInputNode node1 = new CBabFileInputNode("CSV Data Import", project);
		node1.setPhysicalInterface("file");
		node1.setFilePath("/data/input/sensors.csv");
		node1.setFileFormat("CSV");
		node1.setWatchDirectory(true);
		node1.setFilePattern("*.csv");
		node1.setPollingIntervalSeconds(30);
		node1.setMaxFileSizeMb(100);
		node1.setAutoDeleteProcessed(false);
		node1.setBackupProcessedFiles(true);
		node1.setBackupDirectory("/data/backup/");
		node1.setIsActive(true);
		node1.setConnectionStatus("CONNECTED");
		node1.setPriorityLevel(70);
		node1 = service.save(node1);
		LOGGER.info("Created sample file input node: {}", node1.getName());
		if (!minimal) {
			// Sample File Input Node 2 - JSON Log Monitor
			CBabFileInputNode node2 = new CBabFileInputNode("JSON Log Monitor", project);
			node2.setPhysicalInterface("file");
			node2.setFilePath("/logs/system.json");
			node2.setFileFormat("JSON");
			node2.setWatchDirectory(true);
			node2.setFilePattern("*.json");
			node2.setPollingIntervalSeconds(60);
			node2.setMaxFileSizeMb(50);
			node2.setAutoDeleteProcessed(true);
			node2.setBackupProcessedFiles(false);
			node2.setIsActive(true);
			node2.setConnectionStatus("CONNECTED");
			node2.setPriorityLevel(60);
			node2 = service.save(node2);
			LOGGER.info("Created sample file input node: {}", node2.getName());
		}
	}
}
