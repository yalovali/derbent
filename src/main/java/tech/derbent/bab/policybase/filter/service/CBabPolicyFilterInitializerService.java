package tech.derbent.bab.policybase.filter.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilter;

/** CBabPolicyFilterInitializerService - Initializer for BAB policy filter entities. Creates UI forms and grids for filter management with sections
 * for: - Basic filter information (name, type, description) - Filter configuration (type, conditions, transformations) - Processing settings (logic
 * operator, null handling, caching) - Performance settings (execution order, processing time limits) - Node type filtering (enable/disable by node
 * type) - Standard compositions (attachments, comments, links) Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent pattern:
 * Initializer service with form building */
@Service
@Profile ("bab")
public final class CBabPolicyFilterInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyFilter> clazz = CBabPolicyFilter.class;

	/** Create basic view for filter entity. */
	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		// Basic Information Section
		scr.addScreenLine(CDetailLinesService.createSection("Basic Information"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "filterType"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isEnabled"));
		// Filter Configuration Section
		scr.addScreenLine(CDetailLinesService.createSection("Filter Configuration"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "configurationJson"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "conditionsJson"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "transformationJson"));
		// Processing Settings Section
		scr.addScreenLine(CDetailLinesService.createSection("Processing Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logicOperator"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "caseSensitive"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "nullHandling"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "executionOrder"));
		// Performance Settings Section
		scr.addScreenLine(CDetailLinesService.createSection("Performance Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maxProcessingTimeMs"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "cacheEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "cacheSizeLimit"));
		// Logging Settings Section
		scr.addScreenLine(CDetailLinesService.createSection("Logging Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logMatches"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logRejections"));
		// Node Type Filtering Section
		scr.addScreenLine(CDetailLinesService.createSection("Node Type Filtering"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "canNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "modbusNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "httpNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fileNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "syslogNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "rosNodeEnabled"));
		// Standard composition sections
		// CAttachmentInitializerService.addAttachmentsSection(scr, clazz);
		// CCommentInitializerService.addCommentsSection(scr, clazz);
		// CLinkInitializerService.addLinksSection(scr, clazz);
		return scr;
	}

	private CBabPolicyFilterInitializerService() {
		// Utility class - no instantiation
	}
}
