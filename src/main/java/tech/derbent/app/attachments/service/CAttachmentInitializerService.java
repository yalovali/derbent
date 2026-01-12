package tech.derbent.app.attachments.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;
import tech.derbent.app.attachments.domain.CAttachment;

/**
 * Initializer service for CAttachment entities.
 * 
 * Provides screen and grid initialization for attachment management views.
 * Creates detail sections and grid configurations for viewing all attachments.
 */
public final class CAttachmentInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Attachment Details";
	private static final Class<?> clazz = CAttachment.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CAttachmentInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".7";
	private static final String menuTitle = MenuTitle_PROJECT + ".Attachments";
	private static final String pageDescription = "Manage document attachments for project items";
	private static final String pageTitle = "Attachment Management";
	private static final boolean showInQuickToolbar = false;

	/** Create basic detail view for attachments.
	 * @param project the project
	 * @return the detail section
	 * @throws Exception if creation fails */
	public static CDetailSection createBasicView(final CProject project) throws Exception {
		Check.notNull(project, "project cannot be null");
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			
			// File Information section
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fileName"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fileSize"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fileType"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "documentType"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			
			// Version Information section
			detailSection.addScreenLine(CDetailLinesService.createSection("Version Information"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "versionNumber"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "previousVersion"));
			
			// Upload Information section
			detailSection.addScreenLine(CDetailLinesService.createSection("Upload Information"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "uploadedBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "uploadDate"));
			
			// Links section
			detailSection.addScreenLine(CDetailLinesService.createSection("Linked To"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "activity"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "risk"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "meeting"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sprint"));
			
			// System section
			detailSection.addScreenLine(CDetailLinesService.createSection("System"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id"));
			
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating attachment view.", e);
			throw e;
		}
	}

	/** Create grid entity for attachments.
	 * @param project the project
	 * @return the grid entity */
	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of(
				"id", 
				"versionNumber", 
				"fileName", 
				"fileSize", 
				"documentType", 
				"uploadDate", 
				"uploadedBy",
				"activity",
				"risk",
				"meeting",
				"sprint",
				"active"));
		return grid;
	}

	/** Initialize attachment management page.
	 * @param project the project
	 * @param gridEntityService grid service
	 * @param detailSectionService detail section service
	 * @param pageEntityService page service
	 * @throws Exception if initialization fails */
	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, 
				detailSection, grid, menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder);
	}

	private CAttachmentInitializerService() {
		// Utility class - no instantiation
	}
}
