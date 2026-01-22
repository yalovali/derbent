package tech.derbent.plm.comments.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.comments.domain.CComment;

/** Initializer service for CComment entities. Provides standard comment section creation for ALL entity detail views. **Key Feature**:
 * addCommentsSection() ensures ALL entities have identical comment sections with consistent naming, behavior, and appearance. **Important**: Comments
 * are child entities with NO standalone views or pages. They are managed exclusively through their parent entities. */
public final class CCommentInitializerService extends CInitializerServiceBase {

	/** Standard field name - must match entity field name */
	public static final String FIELD_NAME_COMMENTS = "comments";
	private static final Logger LOGGER = LoggerFactory.getLogger(CCommentInitializerService.class);
	/** Standard section name - same for ALL entities */
	public static final String SECTION_NAME_COMMENTS = "Comments";

	/** Add standard Comments section to any entity detail view. **This is the ONLY method that creates comment sections.** ALL entity initializers
	 * (Activity, Risk, Meeting, Sprint, Project, User, etc.) MUST call this method to ensure consistent comment sections.
	 * Note: For the BAB profile, the comments section is intentionally skipped during initialization. Creates: - Section header: "Comments" - Field:
	 * "comments" (renders comment component via factory)
	 * @param detailSection the detail section to add comments to
	 * @param entityClass   the entity class (must implement IHasComments and have @OneToMany comments field)
	 * @throws Exception if adding section fails */
	public static void addDefaultSection(final CDetailSection detailSection, final Class<?> entityClass) throws Exception {
		Check.notNull(detailSection, "detailSection cannot be null");
		Check.notNull(entityClass, "entityClass cannot be null");
		if (isBabProfile()) {
			LOGGER.debug("Skipping Comments section for BAB profile on {}", entityClass.getSimpleName());
			return;
		}
		try {
			// Section header - IDENTICAL for all entities
			detailSection.addScreenLine(CDetailLinesService.createSection(SECTION_NAME_COMMENTS));
			// Comments field - IDENTICAL for all entities
			// Renders via component factory (referenced in entity's @AMetaData)
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, FIELD_NAME_COMMENTS));
			// LOGGER.debug("Added standard Comments section for {}", entityClass.getSimpleName());
		} catch (final Exception e) {
			LOGGER.error("Error adding Comments section for {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
			throw e;
		}
	}

	private CCommentInitializerService() {
		// Utility class - no instantiation
	}

	private static boolean isBabProfile() {
		final Environment environment = CSpringContext.getBean(Environment.class);
		return environment.acceptsProfiles(Profiles.of("bab"));
	}
}
