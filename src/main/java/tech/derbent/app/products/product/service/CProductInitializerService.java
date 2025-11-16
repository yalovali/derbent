package tech.derbent.app.products.product.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.products.product.domain.CProduct;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;

public class CProductInitializerService extends CInitializerServiceBase {

public static final String BASE_PANEL_NAME = "Product Information";
private static final Class<?> clazz = CProduct.class;
private static final Logger LOGGER = LoggerFactory.getLogger(CProductInitializerService.class);
private static final String menuTitle = MenuTitle_PROJECT + ".Products";
private static final String pageTitle = "Product Management";
private static final String pageDescription = "Product management";
private static final String menuOrder = Menu_Order_PROJECT + ".30";
private static final boolean showInQuickToolbar = false;

public static CDetailSection createBasicView(final CProject project) throws Exception {
try {
final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
detailSection.addScreenLine(CDetailLinesService.createSection(CProductInitializerService.BASE_PANEL_NAME));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
detailSection.debug_printScreenInformation();
return detailSection;
} catch (final Exception e) {
LOGGER.error("Error creating product view.");
throw e;
}
}

public static CGridEntity createGridEntity(final CProject project) {
final CGridEntity grid = createBaseGridEntity(project, clazz);
grid.setColumnFields(List.of("id", "name", "description", "status", "project", "assignedTo", "createdBy", "createdDate"));
return grid;
}

public static void initialize(final CProject project, final CGridEntityService gridEntityService,
final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
final CDetailSection detailSection = createBasicView(project);
final CGridEntity grid = createGridEntity(project);
initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, 
grid, menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder);
}

public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
final String[][] nameAndDescriptions = {
{
"Enterprise ERP System", "Comprehensive enterprise resource planning software"
}, {
"Cloud CRM Platform", "Customer relationship management platform"
}
};
initializeProjectEntity(nameAndDescriptions,
(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal, item -> {
final CProduct product = (CProduct) item;
final CUser user = CSpringContext.getBean(CUserService.class).getRandom();
product.setAssignedTo(user);
});
}
}
