package tech.derbent.api.domains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;

/**
 * Initializer service for CAgileParentRelation entities.
 * Provides standard agile parent relation section creation for ALL entity detail views that support agile hierarchy.
 * 
 * <p><strong>Key Feature</strong>:</p>
 * <p>addAgileParentSection() ensures ALL entities implementing IHasAgileParentRelation have identical 
 * agile parent sections with consistent naming, behavior, and appearance.</p>
 * 
 * <p><strong>Important</strong>:</p>
 * <p>Agile parent relations are OneToOne composition entities with NO standalone views or pages. 
 * They are managed exclusively through their parent entities via the agile parent selector component.</p>
 * 
 * <p><strong>Pattern</strong>:</p>
 * <ul>
 * <li>Entity implements IHasAgileParentRelation</li>
 * <li>Entity has @OneToOne(cascade = ALL, orphanRemoval = true) CAgileParentRelation field</li>
 * <li>Initializer service calls addAgileParentSection() to add UI section</li>
 * <li>Component renders via CComponentAgileParentSelector for parent selection</li>
 * </ul>
 * 
 * @see CAgileParentRelation
 * @see tech.derbent.api.interfaces.IHasAgileParentRelation
 * @see CAgileParentRelationService
 * @see tech.derbent.api.ui.component.CComponentAgileParentSelector
 */
public final class CAgileParentRelationInitializerService extends CInitializerServiceBase {

    private static final Class<?> clazz = CAgileParentRelation.class;
    /**
     * Standard field name for agile parent relation - must match entity field name.
     * This is the @OneToOne field in entities that implement IHasAgileParentRelation.
     */
    public static final String FIELD_NAME_AGILE_PARENT_RELATION = "agileParentRelation";
    private static final Logger LOGGER = LoggerFactory.getLogger(CAgileParentRelationInitializerService.class);
    /**
     * Standard section name - same for ALL entities that support agile hierarchy.
     */
    public static final String SECTION_NAME_AGILE_PARENT = "Agile Hierarchy";

    /**
     * Add standard Agile Hierarchy section to any entity detail view.
     * 
     * <p><strong>This is the ONLY method that creates agile parent sections.</strong></p>
     * 
     * <p>ALL entity initializers (Activity, Meeting, Milestone, etc.) that implement IHasAgileParentRelation 
     * MUST call this method to ensure consistent agile parent sections.</p>
     * 
     * <p>Creates:</p>
     * <ul>
     * <li>Section header: "Agile Hierarchy"</li>
     * <li>Field: Component for selecting parent activity (renders via CComponentAgileParentSelector)</li>
     * </ul>
     * 
     * <p><strong>Note</strong>:</p>
     * <p>For the BAB profile, the agile parent section is intentionally skipped during initialization.</p>
     * 
     * @param detailSection the detail section to add agile parent to
     * @param entityClass the entity class (must implement IHasAgileParentRelation)
     * @param project the project context for filtering parent activities
     * @throws Exception if adding section fails
     */
    public static void addAgileParentSection(final CDetailSection detailSection, final Class<?> entityClass, 
            final CProject<?> project) throws Exception {
        Check.notNull(detailSection, "detailSection cannot be null");
        Check.notNull(entityClass, "entityClass cannot be null");
        Check.notNull(project, "project cannot be null");

        if (isBabProfile()) {
            LOGGER.debug("Skipping Agile Parent section for BAB profile on {}", entityClass.getSimpleName());
            return;
        }

        // Verify entity implements IHasAgileParentRelation
        if (!tech.derbent.api.interfaces.IHasAgileParentRelation.class.isAssignableFrom(entityClass)) {
            LOGGER.warn("Entity {} does not implement IHasAgileParentRelation, skipping agile parent section", 
                entityClass.getSimpleName());
            return;
        }

        try {
            // Section header - IDENTICAL for all entities
            detailSection.addScreenLine(CDetailLinesService.createSection(SECTION_NAME_AGILE_PARENT));

            // Parent activity selector field
            // Note: The actual component rendering is handled by the component factory
            // based on @AMetaData annotations in the entity
            // The agileParentRelation field itself is typically hidden=true, so we don't add it directly
            // Instead, we add a custom line or rely on the entity to expose a parentActivity property

            // For now, we create a placeholder line that will be enhanced in future iterations
            // when the component binding mechanism is fully implemented
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, FIELD_NAME_AGILE_PARENT_RELATION));

            LOGGER.debug("Added standard Agile Parent section for {}", entityClass.getSimpleName());
        } catch (final Exception e) {
            LOGGER.error("Error adding Agile Parent section for {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    private CAgileParentRelationInitializerService() {
        // Utility class - no instantiation
    }

    private static boolean isBabProfile() {
        final Environment environment = CSpringContext.getBean(Environment.class);
        return environment.acceptsProfiles(Profiles.of("bab"));
    }
}
