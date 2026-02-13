package tech.derbent.bab.policybase.trigger.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.trigger.domain.CBabPolicyTrigger;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/**
 * CBabPolicyTriggerInitializerService - Initializer for BAB policy trigger entities.
 * 
 * Creates UI forms and grids for trigger management with sections for:
 * - Basic trigger information (name, type, description)
 * - Trigger configuration (type, cron expression, conditions)
 * - Execution settings (priority, order, timeout)
 * - Node type filtering (enable/disable by node type)
 * - Standard compositions (attachments, comments, links)
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Initializer service with form building
 */
@Service
@Profile("bab")
public final class CBabPolicyTriggerInitializerService extends CInitializerServiceBase {

    private static final Class<CBabPolicyTrigger> clazz = CBabPolicyTrigger.class;

    /**
     * Create basic view for trigger entity.
     */
    public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
        final CDetailSection scr = createBaseScreenEntity(project, clazz);
        
        // Basic Information Section
        scr.addScreenLine(CDetailLinesService.createSection("Basic Information"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "triggerType"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isEnabled"));

        // Trigger Configuration Section
        scr.addScreenLine(CDetailLinesService.createSection("Trigger Configuration"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "cronExpression"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "conditionJson"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logicOperator"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "nullHandling"));

        // Execution Settings Section
        scr.addScreenLine(CDetailLinesService.createSection("Execution Settings"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionPriority"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionOrder"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "timeoutSeconds"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "retryCount"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logExecution"));

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

    private CBabPolicyTriggerInitializerService() {
        // Utility class - no instantiation
    }
}