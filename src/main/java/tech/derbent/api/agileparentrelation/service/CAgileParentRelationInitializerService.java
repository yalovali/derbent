package tech.derbent.api.agileparentrelation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.interfaces.IHasAgileParentRelation;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;

/** Initializer service for CAgileParentRelation entities. Provides standard agile parent relation section creation for ALL entity detail views that
 * support agile hierarchy. **Key Feature**: addAgileParentSection() ensures ALL entities implementing IHasAgileParentRelation have identical agile
 * parent sections with consistent naming, behavior, and appearance. **Important**: Agile parent relations are OneToOne composition entities with NO
 * standalone views or pages. They are managed exclusively through their parent entities via the agile parent selector component. */
public final class CAgileParentRelationInitializerService extends CInitializerServiceBase {

	/** Standard field name - must match entity field name */
	public static final String FIELD_NAME_AGILE_PARENT_RELATION = "agileParentRelation";
	private static final Logger LOGGER = LoggerFactory.getLogger(CAgileParentRelationInitializerService.class);
	/** Standard section name - same for ALL entities */
	public static final String SECTION_NAME_AGILE_PARENT = "Agile Hierarchy";

	/** Add standard Agile Hierarchy section to any entity detail view. **This is the ONLY method that creates agile parent sections.** ALL entity
	 * initializers (Activity, Meeting, Milestone, etc.) that implement IHasAgileParentRelation MUST call this method to ensure consistent agile
	 * parent sections. Note: For the BAB profile, the agile parent section is intentionally skipped during initialization. Creates: - Section header:
	 * "Agile Hierarchy" - Field: "agileParentRelation" (renders component via CComponentAgileParentSelector)
	 * @param detailSection the detail section to add agile parent to
	 * @param entityClass   the entity class (must implement IHasAgileParentRelation and have @OneToOne agileParentRelation field)
	 * @param project       the project context for filtering parent activities
	 * @throws Exception if adding section fails */
	public static void addDefaultSection(final CDetailSection detailSection, final Class<?> entityClass, final CProject<?> project)
			throws Exception {
		Check.notNull(detailSection, "detailSection cannot be null");
		Check.notNull(entityClass, "entityClass cannot be null");
		Check.notNull(project, "project cannot be null");
		if (isBabProfile()) {
			LOGGER.debug("Skipping Agile Parent section for BAB profile on {}", entityClass.getSimpleName());
			return;
		}
		// Verify entity implements IHasAgileParentRelation
		if (!IHasAgileParentRelation.class.isAssignableFrom(entityClass)) {
			LOGGER.warn("Entity {} does not implement IHasAgileParentRelation, skipping agile parent section", entityClass.getSimpleName());
			return;
		}
		try {
			// Section header - IDENTICAL for all entities
			detailSection.addScreenLine(CDetailLinesService.createSection(SECTION_NAME_AGILE_PARENT));
			// Agile parent relation field - IDENTICAL for all entities
			// Renders via CComponentAgileParentSelector (referenced in entity's @AMetaData or via service.createComponent())
			// Note: The agileParentRelation field is typically hidden=true in entities (composition pattern)
			// The UI binding mechanism maps CComponentAgileParentSelector to the parentActivity interface methods
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, FIELD_NAME_AGILE_PARENT_RELATION));
			// LOGGER.debug("Added standard Agile Parent section for {}", entityClass.getSimpleName());
		} catch (final Exception e) {
			LOGGER.error("Error adding Agile Parent section for {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
			throw e;
		}
	}

	private static boolean isBabProfile() {
		final Environment environment = CSpringContext.getBean(Environment.class);
		return environment.acceptsProfiles(Profiles.of("bab"));
	}

	private CAgileParentRelationInitializerService() {
		// Utility class - no instantiation
	}
}
