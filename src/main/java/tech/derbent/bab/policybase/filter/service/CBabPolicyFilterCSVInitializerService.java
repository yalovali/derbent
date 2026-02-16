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
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCSV;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** Initializer for CSV policy filters. */
@Service
@Profile ("bab")
public final class CBabPolicyFilterCSVInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyFilterCSV> clazz = CBabPolicyFilterCSV.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyFilterCSVInitializerService.class);
	private static final String menuOrder = Menu_Order_POLICIES + ".31";
	private static final String menuTitle = MenuTitle_POLICIES + ".Filters.CSV";
	private static final String pageDescription = "Manage CSV filters for row, column, and line-pattern filtering";
	private static final String pageTitle = "CSV Policy Filters";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
		scr.addScreenLine(CDetailLinesService.createSection("CSV Capture Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "captureColumnRange"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "columnSeparator"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lineRegularExpression"));
		addCommonSections(scr, clazz);
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "captureColumnRange", "columnSeparator", "lineRegularExpression", "isEnabled",
				"executionOrder", "cacheEnabled", "createdBy", "createdDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final CBabPolicyFilterCSVService service = CSpringContext.getBean(CBabPolicyFilterCSVService.class);
		if (!service.listByProject(project).isEmpty()) {
			LOGGER.info("CSV policy filters already exist for project: {}", project.getName());
			return;
		}
		final CBabPolicyFilterCSV csvSensorFilter = new CBabPolicyFilterCSV("CSV Sensor Filter", project);
		csvSensorFilter.setDescription("Capture sensor columns 1-4 and accept all data rows");
		csvSensorFilter.setCaptureColumnRange("1-4");
		csvSensorFilter.setColumnSeparator(CBabPolicyFilterCSV.COLUMN_SEPARATOR_COMMA);
		csvSensorFilter.setLineRegularExpression(".*");
		csvSensorFilter.setExecutionOrder(1);
		service.save(csvSensorFilter);
		if (minimal) {
			return;
		}
		final CBabPolicyFilterCSV csvAlarmFilter = new CBabPolicyFilterCSV("CSV Alarm Filter", project);
		csvAlarmFilter.setDescription("Capture alarm columns and only pass rows that include ALARM level");
		csvAlarmFilter.setCaptureColumnRange("2-6");
		csvAlarmFilter.setColumnSeparator(CBabPolicyFilterCSV.COLUMN_SEPARATOR_SEMICOLON);
		csvAlarmFilter.setLineRegularExpression(".*ALARM.*");
		csvAlarmFilter.setExecutionOrder(2);
		csvAlarmFilter.setCaseSensitive(false);
		service.save(csvAlarmFilter);
	}

	private static void addCommonSections(final CDetailSection scr, final Class<?> entityClass) throws Exception {
		scr.addScreenLine(CDetailLinesService.createSection("Processing Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "isEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "executionOrder"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "logicOperator"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "caseSensitive"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "nullHandling"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "maxProcessingTimeMs"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "cacheEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "cacheSizeLimit"));
		scr.addScreenLine(CDetailLinesService.createSection("Logging Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "logMatches"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "logRejections"));
		scr.addScreenLine(CDetailLinesService.createSection("Node Compatibility"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "canNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "modbusNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "httpNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "fileNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "syslogNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, "rosNodeEnabled"));
		CAttachmentInitializerService.addDefaultSection(scr, entityClass);
		CLinkInitializerService.addDefaultSection(scr, entityClass);
		CCommentInitializerService.addDefaultSection(scr, entityClass);
	}

	private CBabPolicyFilterCSVInitializerService() {
		// Utility class
	}
}
