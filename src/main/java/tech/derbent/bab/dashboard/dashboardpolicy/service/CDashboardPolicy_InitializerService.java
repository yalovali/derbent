package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
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
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CDashboardPolicy;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/**
 * CDashboardPolicy_InitializerService - Initializer service for BAB Actions Dashboard entities.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Initializer service for entity detail view configuration.
 * 
 * Creates detail view sections for BAB Actions Dashboard entities with:
 * - Basic dashboard configuration fields
 * - BAB component placeholders for UI rendering
 * - Standard composition sections (attachments, comments, links)
 * 
 * Implements the BAB @Transient placeholder pattern for CFormBuilder integration.
 */
@Service
@Profile("bab")
public final class CDashboardPolicy_InitializerService extends CInitializerServiceBase {

    private static final Class<?> clazz = CDashboardPolicy.class;
    private static final Logger LOGGER = LoggerFactory.getLogger(CDashboardPolicy_InitializerService.class);
    private static final String menuOrder = "30";
    public static final String menuTitle = "BAB Actions Dashboard";
    private static final String pageDescription = "BAB Actions Dashboard for network node policy management";
    private static final String pageTitle = "BAB Actions Dashboard";
    private static final boolean showInQuickToolbar = true;

    /**
     * Create basic detail view for BAB Actions Dashboard entity.
     * 
     * @param project the project context
     * @return configured detail section
     * @throws Exception if initialization fails
     */
    public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
        final CDetailSection scr = createBaseScreenEntity(project, clazz);
        CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
        
        // Basic Dashboard Configuration Section
        scr.addScreenLine(CDetailLinesService.createSection("Basic Configuration"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "dashboardLayout"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "nodeListWidth"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "refreshIntervalSeconds"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "autoApplyPolicy"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "showInactiveNodes"));
        
        // Active Policy Configuration Section
        scr.addScreenLine(CDetailLinesService.createSection("Active Policy"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "activePolicy"));
        
        // Dashboard Statistics Section (Read-only)
        scr.addScreenLine(CDetailLinesService.createSection("Dashboard Statistics"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "totalNodes"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "activeNodes"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "totalRules"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "activeRules"));
        
        // BAB Dashboard Components Section (MANDATORY: All placeholder fields MUST be added here)
        scr.addScreenLine(CDetailLinesService.createSection("Dashboard Interface"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentDashboardSplitLayout"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentNodeList"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentWorkArea"));
        
        // Policy Monitoring Section
        scr.addScreenLine(CDetailLinesService.createSection("Policy Monitoring"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentPolicyStatusMonitor"));
        
        // Standard composition sections
        CAttachmentInitializerService.addDefaultSection(scr, clazz);
        CLinkInitializerService.addDefaultSection(scr, clazz);
        CCommentInitializerService.addDefaultSection(scr, clazz);
        
        return scr;
    }

    public static CGridEntity createGridEntity(final CProject<?> project) {
        final CGridEntity grid = createBaseGridEntity(project, clazz);
        grid.setColumnFields(List.of("id", "name", "description", "project", "dashboardLayout", "totalNodes", "activeNodes"));
        return grid;
    }

    public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
            final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
        LOGGER.info("Initializing BAB Actions Dashboard for project: {}", project.getName());
        
        final CDetailSection detailSection = createBasicView(project);
        final CGridEntity grid = createGridEntity(project);
        
        initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, 
                menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder);
    }

    public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
        final String[][] nameAndDescriptions = {
                {
                        "Main Actions Dashboard", "Primary BAB Actions Dashboard for network policy management"
                },
                {
                        "Test Policy Dashboard", "Test dashboard for policy development and validation"
                }
        };
        initializeProjectEntity(nameAndDescriptions,
                (CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
                (item, index) -> {
                    final CDashboardPolicy dashboard = (CDashboardPolicy) item;
                    dashboard.setAutoApplyPolicy(index == 0); // Main dashboard auto-applies, test doesn't
                    dashboard.setDashboardLayout("SPLIT_PANE");
                    dashboard.setNodeListWidth(30);
                    dashboard.setRefreshIntervalSeconds(60);
                    dashboard.setShowInactiveNodes(true);
                });
    }

    private CDashboardPolicy_InitializerService() {
        // Utility class - no instantiation
    }
}
