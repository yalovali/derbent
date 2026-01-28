package tech.derbent.plm.attachments.service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import tech.derbent.api.companies.domain.CCompany;
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
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.plm.attachments.domain.CAttachment;

/** Initializer service for CAttachment entities. Provides: 1. Screen and grid initialization for standalone attachment management views 2. Standard
 * attachment section creation for ALL entity detail views **Key Feature**: addAttachmentsSection() ensures ALL entities have identical attachment
 * sections with consistent naming, behavior, and appearance. */
public final class CAttachmentInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Attachment Details";
	private static final Class<?> clazz = CAttachment.class;
	/** Standard field name - must match entity field name */
	public static final String FIELD_NAME_ATTACHMENTS = "attachments";
	private static final Logger LOGGER = LoggerFactory.getLogger(CAttachmentInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".7";
	private static final String menuTitle = MenuTitle_PROJECT + ".Attachments";
	private static final String pageDescription = "Manage document attachments for project items";
	private static final String pageTitle = "Attachment Management";
	/** Standard section name - same for ALL entities */
	public static final String SECTION_NAME_ATTACHMENTS = "Attachments";
	private static final boolean showInQuickToolbar = false;

	/** Add standard Attachments section to any entity detail view. **This is the ONLY method that creates attachment sections.** ALL entity
	 * initializers (Activity, Risk, Meeting, Sprint, Project, User, etc.) MUST call this method to ensure consistent attachment sections. Note: For
	 * the BAB profile, the attachments section is intentionally skipped during initialization. Creates: - Section header: "Attachments" - Field:
	 * "attachments" (renders CComponentListAttachments via factory) Usage in ANY entity initializer:
	 *
	 * <pre>
	 * // CActivityInitializerService.java
	 * public static CDetailSection createBasicView(CProject<?> project) {
	 *     CDetailSection scr = createBaseScreenEntity(project, CActivity.class);
	 *     // ... other sections ...
	 *     CAttachmentInitializerService.addAttachmentsSection(scr, CActivity.class);
	 *     // ... more sections ...
	 * }
	 *
	 * // Same for ALL entities:
	 * CAttachmentInitializerService.addAttachmentsSection(scr, CRisk.class);
	 * CAttachmentInitializerService.addAttachmentsSection(scr, CMeeting.class);
	 * CAttachmentInitializerService.addAttachmentsSection(scr, CSprint.class);
	 * CAttachmentInitializerService.addAttachmentsSection(scr, CProject.class);
	 * CAttachmentInitializerService.addAttachmentsSection(scr, CUser.class);
	 * </pre>
	 *
	 * @param detailSection the detail section to add attachments to
	 * @param entityClass   the entity class (must implement IHasAttachments and have @OneToMany attachments field)
	 * @throws Exception if adding section fails */
	public static void addDefaultSection(final CDetailSection detailSection, final Class<?> entityClass) throws Exception {
		Check.notNull(detailSection, "detailSection cannot be null");
		Check.notNull(entityClass, "entityClass cannot be null");
		if (isBabProfile()) {
			LOGGER.debug("Skipping Attachments section for BAB profile on {}", entityClass.getSimpleName());
			return;
		}
		try {
			detailSection.addScreenLine(CDetailLinesService.createSection(SECTION_NAME_ATTACHMENTS));
			final var detailLine = CDetailLinesService.createLineFromDefaults(entityClass, FIELD_NAME_ATTACHMENTS);
			detailLine.setIsCaptionVisible(false);
			detailSection.addScreenLine(detailLine);
		} catch (final Exception e) {
			LOGGER.error("Error adding Attachments section for {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
			throw e;
		}
	}

	/** Create basic detail view for standalone attachment management page.
	 * @param project the project
	 * @return the detail section
	 * @throws Exception if creation fails */
	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
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
			// System section
			detailSection.addScreenLine(CDetailLinesService.createSection("System"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating attachment view.", e);
			throw e;
		}
	}

	/** Create grid entity for standalone attachment management page.
	 * @param project the project
	 * @return the grid entity */
	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(
				List.of("id", "versionNumber", "fileName", "fileSize", "fileType", "documentType", "uploadDate", "uploadedBy", "company", "active"));
		return grid;
	}

	/** Create sample attachments for any entity. Provides realistic attachment metadata.
	 * @param attachmentInfos array of attachment info: [filename, description, fileSize]
	 * @param company         the company for the attachments
	 * @return list of created CAttachment objects (not yet persisted - caller must add to entity and save) */
	public static List<CAttachment> createSampleAttachments(final String[][] attachmentInfos, final CCompany company) {
		final List<CAttachment> attachments = new ArrayList<>();
		try {
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CUser user = userService.getRandom(company);
			if (user == null) {
				LOGGER.warn("No users available for creating sample attachments");
				return attachments;
			}
			for (final String[] info : attachmentInfos) {
				final String filename = info[0];
				final String description = info.length > 1 ? info[1] : "";
				final long fileSize = info.length > 2 ? Long.parseLong(info[2]) : 10240L;
				final String contentPath = "samples/" + filename;
				final CAttachment attachment = new CAttachment(filename, fileSize, contentPath, user);
				attachment.setDescription(description);
				attachment.setCompany(company);
				attachments.add(attachment);
			}
		} catch (final Exception e) {
			LOGGER.warn("Error creating sample attachments: {}", e.getMessage(), e);
		}
		return attachments;
	}

	/** Initialize standalone attachment management page.
	 * @param project              the project
	 * @param gridEntityService    grid service
	 * @param detailSectionService detail section service
	 * @param pageEntityService    page service
	 * @throws Exception if initialization fails */
	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		if (isBabProfile()) {
			LOGGER.info("Skipping attachment management page initialization for BAB profile.");
			return;
		}
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	private static boolean isBabProfile() {
		final Environment environment = CSpringContext.getBean(Environment.class);
		return environment.acceptsProfiles(Profiles.of("bab"));
	}

	private CAttachmentInitializerService() {
		// Utility class - no instantiation
	}
}
