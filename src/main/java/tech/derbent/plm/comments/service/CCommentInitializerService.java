package tech.derbent.plm.comments.service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.plm.comments.domain.CComment;

/** Initializer service for CComment entities. Provides standard comment section creation for ALL entity detail views AND sample comment generation.
 * **Key Features**: 1. addDefaultSection() ensures ALL entities have identical comment sections 2. createSampleComments() provides reusable sample
 * comment generation **Important**: Comments are child entities with NO standalone views or pages. They are managed exclusively through their parent
 * entities. */
public final class CCommentInitializerService extends CInitializerServiceBase {

	/** Standard field name - must match entity field name */
	public static final String FIELD_NAME_COMMENTS = "comments";
	private static final Logger LOGGER = LoggerFactory.getLogger(CCommentInitializerService.class);
	/** Standard section name - same for ALL entities */
	public static final String SECTION_NAME_COMMENTS = "Comments";

	/** Add standard Comments section to any entity detail view. **This is the ONLY method that creates comment sections.** ALL entity initializers
	 * (Activity, Risk, Meeting, Sprint, Project, User, etc.) MUST call this method to ensure consistent comment sections. Note: For the BAB profile,
	 * the comments section is intentionally skipped during initialization. Creates: - Section header: "Comments" - Field: "comments" (renders comment
	 * component via factory)
	 * @param detailSection the detail section to add comments to
	 * @param entityClass   the entity class (must implement IHasComments and have @OneToMany comments field)
	 * @throws Exception if adding section fails */
	public static void addDefaultSection(final CDetailSection detailSection, final Class<?> entityClass) throws Exception {
		Check.notNull(detailSection, "detailSection cannot be null");
		Check.notNull(entityClass, "entityClass cannot be null");
		if (CSpringContext.isBabProfile()) {
			LOGGER.debug("Skipping Comments section for BAB profile on {}", entityClass.getSimpleName());
			return;
		}
		try {
			detailSection.addScreenLine(CDetailLinesService.createSection(SECTION_NAME_COMMENTS));
			final var detailLine = CDetailLinesService.createLineFromDefaults(entityClass, FIELD_NAME_COMMENTS);
			detailLine.setIsCaptionVisible(false);
			detailSection.addScreenLine(detailLine);
		} catch (final Exception e) {
			LOGGER.error("Error adding Comments section for {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
			throw e;
		}
	}

	/** Create sample comments with simple texts (all non-important).
	 * @param commentTexts array of comment texts to create
	 * @return list of created CComment objects (not yet persisted) */
	public static List<CComment> createSampleComments(final String... commentTexts) {
		return createSampleComments(commentTexts, null);
	}

	/** Create sample comments for any entity. Provides realistic, contextual comments.
	 * @param commentTexts   array of comment texts to create
	 * @param importantFlags array of flags indicating which comments are important (optional, can be null)
	 * @return list of created CComment objects (not yet persisted - caller must add to entity and save) */
	public static List<CComment> createSampleComments(final String[] commentTexts, final boolean[] importantFlags) {
		final List<CComment> comments = new ArrayList<>();
		try {
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CUser user = userService.getRandom();
			if (user == null) {
				LOGGER.warn("No users available for creating sample comments");
				return comments;
			}
			for (int i = 0; i < commentTexts.length; i++) {
				final CComment comment = new CComment(commentTexts[i], user);
				if (importantFlags != null && i < importantFlags.length && importantFlags[i]) {
					comment.setImportant(true);
				}
				comments.add(comment);
			}
		} catch (final Exception e) {
			LOGGER.warn("Error creating sample comments: {}", e.getMessage(), e);
		}
		return comments;
	}

	private CCommentInitializerService() {
		// Utility class - no instantiation
	}
}
