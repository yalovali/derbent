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
import tech.derbent.bab.policybase.node.domain.CBabHttpServerNode;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/**
 * CBabHttpServerNodeInitializerService - Initializer for HTTP Server nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Entity initializer with UI definition.
 * 
 * Creates dynamic pages and grids for HTTP Server node management.
 * Defines form layout with node configuration and HTTP-specific fields.
 */
@Service
@Profile("bab")
public final class CBabHttpServerNodeInitializerService extends CInitializerServiceBase {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabHttpServerNodeInitializerService.class);
	private static final Class<CBabHttpServerNode> clazz = CBabHttpServerNode.class;
	
	/**
	 * Initialize HTTP Server node pages for project.
	 * Creates menu entry, grid, and detail views.
	 */
	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
			"Network.HTTP Servers",           // Menu title (hierarchical)
			CBabHttpServerNode.VIEW_NAME,     // Page title
			"HTTP server virtual network nodes for web service routing and API management",  // Description
			true,                              // Show in toolbar
			"10.20");                          // Menu order
	}
	
	/**
	 * Create detail view with all HTTP Server node fields.
	 */
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
		
		// HTTP Server Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("HTTP Server Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "serverPort"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "endpointPath"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "protocol"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sslEnabled"));
		
		// Performance Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("Performance Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maxConnections"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "timeoutSeconds"));
		
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
		// NOTE: Removed nodeType from grid - it's displayed in entity title/class name
		grid.setColumnFields(List.of("id", "name", "physicalInterface", "isActive", 
			"connectionStatus", "serverPort", "protocol", "sslEnabled", "createdBy", "createdDate"));
		return grid;
	}
	
	/**
	 * Initialize sample HTTP server nodes for project.
	 * Creates sample nodes for web service routing and API management.
	 */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		LOGGER.info("Initializing HTTP Server Node sample data for project: {}", project.getName());
		
		final CBabHttpServerNodeService service = 
			(CBabHttpServerNodeService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		
		// Check if sample nodes already exist
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("HTTP Server nodes already exist for project: {}", project.getName());
			return;
		}
		
		// Sample HTTP Server Node 1 - REST API Server
		CBabHttpServerNode node1 = new CBabHttpServerNode("API Server", project);
		node1.setPhysicalInterface("eth0");
		node1.setServerPort(8080);
		node1.setEndpointPath("/api");
		node1.setProtocol("HTTP");
		node1.setSslEnabled(false);
		node1.setMaxConnections(100);
		node1.setTimeoutSeconds(30);
		node1.setIsActive(true);
		node1.setConnectionStatus("CONNECTED");
		node1.setPriorityLevel(90);
		node1 = service.save(node1);
		LOGGER.info("Created sample HTTP server node: {}", node1.getName());
		
		if (minimal) {
			return;
		}
		// Sample HTTP Server Node 2 - HTTPS Web Service
		CBabHttpServerNode node2 = new CBabHttpServerNode("Web Service", project);
		node2.setPhysicalInterface("eth1");
		node2.setServerPort(443);
		node2.setEndpointPath("/service");
		node2.setProtocol("HTTPS");
		node2.setSslEnabled(true);
		node2.setMaxConnections(200);
		node2.setTimeoutSeconds(60);
		node2.setIsActive(true);
		node2.setConnectionStatus("CONNECTED");
		node2.setPriorityLevel(80);
		node2 = service.save(node2);
		LOGGER.info("Created sample HTTP server node: {}", node2.getName());
	}
}
