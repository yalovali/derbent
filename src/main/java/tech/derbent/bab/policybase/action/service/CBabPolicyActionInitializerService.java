package tech.derbent.bab.policybase.action.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/**
 * CBabPolicyActionInitializerService - Initializer for BAB policy action entities.
 * 
 * Creates UI forms and grids for action management with sections for:
 * - Basic action information (name, type, description)
 * - Action configuration (type, parameters, templates)
 * - Execution settings (priority, order, timeout, retry)
 * - Logging settings (input, output, execution logs)
 * - Node type filtering (enable/disable by node type)
 * - Standard compositions (attachments, comments, links)
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Initializer service with form building
 */
@Service
@Profile("bab")
public final class CBabPolicyActionInitializerService extends CInitializerServiceBase {

    private static final Class<CBabPolicyAction> clazz = CBabPolicyAction.class;

    /**
     * Create basic view for action entity.
     */
    public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
        final CDetailSection scr = createBaseScreenEntity(project, clazz);
        
        // Basic Information Section
        scr.addScreenLine(CDetailLinesService.createSection("Basic Information"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actionType"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isEnabled"));

        // Action Configuration Section
        scr.addScreenLine(CDetailLinesService.createSection("Action Configuration"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "configurationJson"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "templateJson"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "asyncExecution"));

        // Execution Settings Section
        scr.addScreenLine(CDetailLinesService.createSection("Execution Settings"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionPriority"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionOrder"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "timeoutSeconds"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "retryCount"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "retryDelaySeconds"));

        // Logging Settings Section
        scr.addScreenLine(CDetailLinesService.createSection("Logging Settings"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logExecution"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logInput"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logOutput"));

        // Node Type Filtering Section
        scr.addScreenLine(CDetailLinesService.createSection("Node Type Filtering"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "canNodeEnabled"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "modbusNodeEnabled"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "httpNodeEnabled"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fileNodeEnabled"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "syslogNodeEnabled"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "rosNodeEnabled"));

        // Standard composition sections
        CAttachmentInitializerService.addAttachmentsSection(scr, clazz);
        CCommentInitializerService.addCommentsSection(scr, clazz);
        CLinkInitializerService.addLinksSection(scr, clazz);

        return scr;
    }

    private CBabPolicyActionInitializerService() {
        // Utility class - no instantiation
    }
}