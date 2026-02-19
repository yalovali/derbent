package tech.derbent.bab.policybase.node.file;

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
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** CBabFileOutputNodeInitializerService - Initializer for File Output nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following
 * Derbent pattern: Entity initializer with UI definition. Creates dynamic pages and grids for File Output node management. Defines form layout with
 * node configuration and file sink fields. */
@Service
@Profile ("bab")
public final class CBabFileOutputNodeInitializerService extends CInitializerServiceBase {

	private static final Class<CBabFileOutputNode> clazz = CBabFileOutputNode.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabFileOutputNodeInitializerService.class);

	/** Create detail view with all File Output node fields. */
	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "physicalInterface"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "connectionStatus"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "filePath"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fileFormat"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "filePattern"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maxFileSizeMb"));
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
		grid.setColumnFields(
				List.of("id", "name", "physicalInterface", "active", "connectionStatus", "filePath", "fileFormat", "createdBy", "createdDate"));
		return grid;
	}

	/** Initialize File Output node pages for project. Creates menu entry, grid, and detail views. */
	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, "Policies.File Output Nodes",
				CBabFileOutputNode.VIEW_NAME, "File output virtual network nodes for outbound data export and file sink routing", true, "10.41");
	}

	/** Initialize sample file output nodes for project. Creates sample nodes for outbound data export and archival. */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		LOGGER.info("Initializing File Output Node sample data for project: {}", project.getName());
		final CBabFileOutputNodeService service = (CBabFileOutputNodeService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		// Check if sample nodes already exist
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("File Output nodes already exist for project: {}", project.getName());
			return;
		}
		// Sample File Output Node 1 - CSV Export Sink
		CBabFileOutputNode node1 = new CBabFileOutputNode("CSV Export Sink", project);
		node1.setFilePath("/data/output/export.csv");
		node1.setFileFormat("CSV");
		node1.setFilePattern("export_*.csv");
		node1.setConnectionStatus("CONNECTED");
		node1.setPriorityLevel(65);
		node1 = service.save(node1);
		LOGGER.info("Created sample file output node: {}", node1.getName());
		if (minimal) {
			return;
		}
		// Sample File Output Node 2 - JSON Archive Sink
		CBabFileOutputNode node2 = new CBabFileOutputNode("JSON Archive Sink", project);
		node2.setFilePath("/logs/output/archive.json");
		node2.setFileFormat("JSON");
		node2.setFilePattern("archive_*.json");
		node2.setMaxFileSizeMb(100);
		node2.setConnectionStatus("CONNECTED");
		node2.setPriorityLevel(55);
		node2 = service.save(node2);
		LOGGER.info("Created sample file output node: {}", node2.getName());
	}
}
