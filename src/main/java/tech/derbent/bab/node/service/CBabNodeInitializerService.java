package tech.derbent.bab.node.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.node.domain.CBabNode;

/**
 * Initializer service for BAB node.
 * Following Derbent pattern: createBasicView, createGridEntity, initialize, initializeSample.
 */
public class CBabNodeInitializerService extends CInitializerServiceBase {

private static final Class<?> clazz = CBabNode.class;
private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeInitializerService.class);
private static final String menuOrder = Menu_Order_SYSTEM + ".2";
private static final String menuTitle = MenuTitle_SYSTEM + ".Nodes";
private static final String pageDescription = "Communication node management and configuration";
private static final String pageTitle = "Node Management";
private static final boolean showInQuickToolbar = true;

public static CDetailSection createBasicView(final CProject project) throws Exception {
Check.notNull(project, "project cannot be null");
try {
final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);

detailSection.addScreenLine(CDetailLinesService.createSection("Node Information"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "device"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "nodeType"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enabled"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "nodeStatus"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "portNumber"));

detailSection.addScreenLine(CDetailLinesService.createSection("System"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));

detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));

detailSection.debug_printScreenInformation();
return detailSection;
} catch (final Exception e) {
LOGGER.error("Error creating node view.", e);
throw e;
}
}

public static CGridEntity createGridEntity(final CProject project) {
final CGridEntity grid = createBaseGridEntity(project, clazz);
grid.setColumnFields(List.of("id", "name", "description", "device", "nodeType", "enabled", 
"nodeStatus", "portNumber", "company", "createdBy", "active", 
"createdDate", "lastModifiedDate"));
return grid;
}

public static void initialize(final CProject project, final CGridEntityService gridEntityService,
final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
final CDetailSection detailSection = createBasicView(project);
final CGridEntity grid = createGridEntity(project);
initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, 
menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder);
}

/**
 * Initialize sample BAB nodes.
 * Note: Nodes are created via CBabDeviceInitializerService, not independently.
 * 
 * @param project the project
 * @param minimal if true, create minimal sample data
 */
public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
LOGGER.debug("CBabNode sample data created via CBabDeviceInitializerService");
// Nodes are created as part of device initialization
// This method is here for consistency with Derbent pattern
}
}
