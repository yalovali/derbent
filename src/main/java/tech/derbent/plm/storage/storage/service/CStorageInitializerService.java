package tech.derbent.plm.storage.storage.service;

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
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.storage.storage.domain.CStorage;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.page.service.CPageEntityService;

public class CStorageInitializerService extends CInitializerServiceBase {

    private static final Class<?> clazz = CStorage.class;
    private static final Logger LOGGER = LoggerFactory.getLogger(CStorageInitializerService.class);
    private static final String menuOrder = Menu_Order_PRODUCTS + ".40";
    private static final String menuTitle = "Storage";
    private static final String pageDescription = "Manage storage locations";
    private static final String pageTitle = "Storage";
    private static final boolean showInQuickToolbar = false;

    public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
        Check.notNull(project, "project cannot be null");
        final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentStorage"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "responsibleUser"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "capacity"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "capacityUnit"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "currentUtilization"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "temperatureControl"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "climateControl"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "secureStorage"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "address"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "building"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "floor"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "zone"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "binCode"));
        CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);
        CCommentInitializerService.addCommentsSection(detailSection, clazz);
        detailSection.debug_printScreenInformation();
        return detailSection;
    }

    public static CGridEntity createGridEntity(final CProject<?> project) {
        final CGridEntity grid = createBaseGridEntity(project, clazz);
        grid.setColumnFields(List.of("id", "name", "entityType", "parentStorage", "capacity", "capacityUnit", "currentUtilization", "active"));
        return grid;
    }

    public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
            final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
        final CDetailSection detailSection = createBasicView(project);
        final CGridEntity grid = createGridEntity(project);
        initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
                pageDescription, showInQuickToolbar, menuOrder);
    }
}
