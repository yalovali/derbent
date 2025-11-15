package tech.derbent.app.components.componentversiontype.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.components.componentversiontype.domain.CComponentVersionType;

public class CComponentVersionTypeInitializerService extends CInitializerServiceBase {

public static final String BASE_PANEL_NAME = "ComponentVersionType Information";
private static final Class<?> clazz = CComponentVersionType.class;
private static final Logger LOGGER = LoggerFactory.getLogger(CComponentVersionTypeInitializerService.class);
private static final String menuOrder = Menu_Order_TYPES + ".30";
private static final String menuTitle = MenuTitle_TYPES + ".ComponentVersionTypes";
private static final String pageDescription = "Manage componentversiontype type categories";
private static final String pageTitle = "ComponentVersionType Management";
private static final boolean showInQuickToolbar = false;

public static CDetailSection createBasicView(final CProject project) throws Exception {
Check.notNull(project, "project cannot be null");
try {
final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "workflow"));
detailSection.addScreenLine(CDetailLinesService.createSection("Display Configuration"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeNonDeletable"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
detailSection.debug_printScreenInformation();
return detailSection;
} catch (final Exception e) {
LOGGER.error("Error creating componentversiontype type view.");
throw e;
}
}

public static CGridEntity createGridEntity(final CProject project) {
final CGridEntity grid = createBaseGridEntity(project, clazz);
grid.setColumnFields(List.of("id", "name", "description", "color", "sortOrder", "active", "project"));
return grid;
}

public static void initialize(final CProject project, final CGridEntityService gridEntityService,
final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
final CDetailSection detailSection = createBasicView(project);
final CGridEntity grid = createGridEntity(project);
initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, 
grid, menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder);
}
}
