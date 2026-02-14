package tech.derbent.bab.policybase.filter.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilter;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** CBabPolicyFilterInitializerService - Initializer for BAB policy filter entities. Creates UI forms and grids for filter management with sections
 * for: - Basic filter information (name, type, description) - Filter configuration (type, conditions, transformations) - Processing settings (logic
 * operator, null handling, caching) - Performance settings (execution order, processing time limits) - Node type filtering (enable/disable by node
 * type) - Standard compositions (attachments, comments, links) Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent pattern:
 * Initializer service with form building */
@Service
@Profile ("bab")
public final class CBabPolicyFilterInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyFilter> clazz = CBabPolicyFilter.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyFilterInitializerService.class);
	private static final String menuOrder = Menu_Order_POLICIES + ".30";
	private static final String menuTitle = MenuTitle_POLICIES + ".Filters";
	private static final String pageDescription = "Manage policy filters for data validation and transformation";
	private static final String pageTitle = "Policy Filters";
	private static final boolean showInQuickToolbar = false;

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
		CAttachmentInitializerService.addDefaultSection(scr, clazz);
		CLinkInitializerService.addDefaultSection(scr, clazz);
		CCommentInitializerService.addDefaultSection(scr, clazz);
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "filterType", "description", "isEnabled", "executionOrder", 
				"logicOperator", "caseSensitive", "cacheEnabled"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, 
				menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder);
	}

	/** Initialize sample policy filters for a project.
	 * 
	 * @param project the project to create filters for
	 * @param minimal if true, creates only 1 filter; if false, creates 8 filters */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final CBabPolicyFilterService service = CSpringContext.getBean(CBabPolicyFilterService.class);
		
		// Guard clause - check if already has data
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("Policy filters already exist for project: {}", project.getName());
			return;
		}
		
		LOGGER.debug("Creating sample policy filters for project: {}", project.getName());
		
		// Sample filter seeds: name, type, description, order, caseSensitive
		final Object[][] samples = {
			{"CSV Data Filter", CBabPolicyFilter.FILTER_TYPE_CSV, "Filter CSV data files for sensor readings", 1, false},
			{"JSON API Filter", CBabPolicyFilter.FILTER_TYPE_JSON, "Filter JSON data from API endpoints", 2, false},
			{"XML Config Filter", CBabPolicyFilter.FILTER_TYPE_XML, "Filter XML configuration files", 3, true},
			{"Text Pattern Filter", CBabPolicyFilter.FILTER_TYPE_REGEX, "Filter text data using regex patterns", 4, false},
			{"Numeric Range Filter", CBabPolicyFilter.FILTER_TYPE_RANGE, "Filter numeric data within valid ranges", 5, false},
			{"Business Rule Filter", CBabPolicyFilter.FILTER_TYPE_CONDITION, "Apply business logic conditions", 6, false},
			{"Data Transform Filter", CBabPolicyFilter.FILTER_TYPE_TRANSFORM, "Transform data structure and format", 7, false},
			{"Schema Validation Filter", CBabPolicyFilter.FILTER_TYPE_VALIDATE, "Validate data against JSON schema", 0, false}
		};
		
		for (final Object[] sample : samples) {
			final CBabPolicyFilter filter = new CBabPolicyFilter((String) sample[0], project);
			filter.setFilterType((String) sample[1]);
			filter.setDescription((String) sample[2]);
			filter.setExecutionOrder((Integer) sample[3]);
			filter.setCaseSensitive((Boolean) sample[4]);
			service.save(filter);
			
			if (minimal) {
				break;
			}
		}
		
		LOGGER.info("Created {} sample policy filter(s) for project: {}", minimal ? 1 : samples.length, project.getName());
	}

	private CBabPolicyFilterInitializerService() {
		// Utility class - no instantiation
	}
}
