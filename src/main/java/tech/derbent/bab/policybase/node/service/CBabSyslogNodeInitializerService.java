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
import tech.derbent.bab.policybase.node.domain.CBabSyslogNode;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** CBabSyslogNodeInitializerService - Initializer for Syslog nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent
 * pattern: Entity initializer with UI definition. Creates dynamic pages and grids for Syslog node management. Defines form layout with node
 * configuration and Syslog-specific fields. */
@Service
@Profile ("bab")
public final class CBabSyslogNodeInitializerService extends CInitializerServiceBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabSyslogNodeInitializerService.class);
	private static final Class<CBabSyslogNode> clazz = CBabSyslogNode.class;

	/** Create detail view with all Syslog node fields. */
	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		// Base Node Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("Node Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "physicalInterface"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "connectionStatus"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priorityLevel"));
		// Syslog Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("Syslog Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "listenPort"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "protocol"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "facility"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "severityLevel"));
		// Storage Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("Storage Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logFilePath"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maxMessageSize"));
		// Security Section
		scr.addScreenLine(CDetailLinesService.createSection("Security"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableTls"));
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
		grid.setColumnFields(List.of("id", "name", "physicalInterface", "isActive", "connectionStatus", "listenPort", "protocol", "facility",
				"enableTls", "createdBy", "createdDate"));
		return grid;
	}

	/** Initialize Syslog node pages for project. Creates menu entry, grid, and detail views. */
	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, "Policies.Syslog Nodes", // Menu
																																			// title
																																			// (hierarchical)
				CBabSyslogNode.VIEW_NAME, // Page title
				"Syslog server virtual network nodes for centralized logging and monitoring", // Description
				true, // Show in toolbar
				"10.50"); // Menu order
	}

	/** Initialize sample Syslog nodes for project. Creates sample nodes for log collection and monitoring. */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		LOGGER.info("Initializing Syslog Node sample data for project: {}", project.getName());
		final CBabSyslogNodeService service = (CBabSyslogNodeService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		// Check if sample nodes already exist
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("Syslog nodes already exist for project: {}", project.getName());
			return;
		}
		// Sample Syslog Node 1 - UDP Server
		CBabSyslogNode node1 = new CBabSyslogNode("Syslog UDP Server", project);
		node1.setPhysicalInterface("eth0");
		node1.setListenPort(514);
		node1.setProtocol("UDP");
		node1.setFacility("LOCAL0");
		node1.setSeverityLevel("INFO");
		node1.setLogFilePath("/var/log/syslog");
		node1.setMaxMessageSize(2048);
		node1.setEnableTls(false);
		node1.setIsActive(true);
		node1.setConnectionStatus("CONNECTED");
		node1.setPriorityLevel(90);
		node1 = service.save(node1);
		LOGGER.info("Created sample Syslog node: {}", node1.getName());
		if (minimal) {
			return;
		}
		// Sample Syslog Node 2 - TCP Server with TLS
		CBabSyslogNode node2 = new CBabSyslogNode("Syslog TCP/TLS Server", project);
		node2.setPhysicalInterface("eth1");
		node2.setListenPort(6514);
		node2.setProtocol("TCP");
		node2.setFacility("LOCAL1");
		node2.setSeverityLevel("WARNING");
		node2.setLogFilePath("/var/log/secure-syslog");
		node2.setMaxMessageSize(4096);
		node2.setEnableTls(true);
		node2.setIsActive(true);
		node2.setConnectionStatus("CONNECTED");
		node2.setPriorityLevel(80);
		node2 = service.save(node2);
		LOGGER.info("Created sample Syslog node: {}", node2.getName());
	}
}
