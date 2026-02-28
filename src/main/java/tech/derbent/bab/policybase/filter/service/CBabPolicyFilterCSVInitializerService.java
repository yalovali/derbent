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
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCSV;
import tech.derbent.bab.policybase.node.file.CBabFileInputNode;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

/** Initializer for CSV policy filters. */
@Service
@Profile ("bab")
public final class CBabPolicyFilterCSVInitializerService extends CInitializerServiceBase {

	private static final Class<CBabPolicyFilterCSV> clazz = CBabPolicyFilterCSV.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyFilterCSVInitializerService.class);
	private static final String menuOrder = Menu_Order_POLICIES + ".999.31";
	private static final String menuTitle = MenuTitle_POLICIES + ".Developer.Filters.CSV";
	private static final String pageDescription = "Manage CSV filters for row, column, and line-pattern filtering";
	private static final String pageTitle = "CSV Policy Filters";
	private static final String SAMPLE_FILTER_NAME_SUFFIX = " Filter";
	private static final boolean showInQuickToolbar = false;

	private static String buildSampleFilterName(final CBabFileInputNode parentNode) {
		return parentNode.getName() + SAMPLE_FILTER_NAME_SUFFIX;
	}

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		scr.addScreenLine(CDetailLinesService.createSection("CSV Capture Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "captureColumnRange"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "columnSeparator"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lineRegularExpression"));
		scr.addScreenLine(CDetailLinesService.createSection("Processing Settings"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "logEnabled"));
		scr.addScreenLine(CDetailLinesService.createSection("Node Compatibility"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "canNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "modbusNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "httpNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fileNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "syslogNodeEnabled"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "rosNodeEnabled"));
		CAttachmentInitializerService.addDefaultSection(scr, clazz);
		CLinkInitializerService.addDefaultSection(scr, clazz);
		CCommentInitializerService.addDefaultSection(scr, clazz);
		return scr;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("name", "parentNode", "captureColumnRange", "columnSeparator", "lineRegularExpression", "createdBy",
				"createdDate"));
		return grid;
	}

	public static CBabPolicyFilterCSV createSampleForNode(final CBabFileInputNode parentNode) throws Exception {
		Check.notNull(parentNode, "Parent file input node cannot be null");
		Check.notNull(parentNode.getId(), "Parent file input node must be persisted before creating sample filter");
		final CBabPolicyFilterCSVService service = CSpringContext.getBean(CBabPolicyFilterCSVService.class);
		final List<CBabPolicyFilterCSV> existingFilters = service.listByParentNode(parentNode);
		if (!existingFilters.isEmpty()) {
			return existingFilters.get(0);
		}
		CBabPolicyFilterCSV filter = new CBabPolicyFilterCSV(buildSampleFilterName(parentNode), parentNode);
		filter.setCaptureColumnRange(CBabPolicyFilterCSV.DEFAULT_CAPTURE_COLUMN_RANGE);
		filter.setColumnSeparator(CBabPolicyFilterCSV.COLUMN_SEPARATOR_COMMA);
		filter.setLineRegularExpression(CBabPolicyFilterCSV.DEFAULT_LINE_REGULAR_EXPRESSION);
		filter.setCanNodeEnabled(false);
		filter.setModbusNodeEnabled(false);
		filter.setHttpNodeEnabled(false);
		filter.setFileNodeEnabled(true);
		filter.setSyslogNodeEnabled(false);
		filter.setRosNodeEnabled(false);
		filter = service.save(filter);
		LOGGER.info("Created sample CSV policy filter '{}' for node '{}'", filter.getName(), parentNode.getName());
		return filter;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	private CBabPolicyFilterCSVInitializerService() {
		// Utility class
	}
}
