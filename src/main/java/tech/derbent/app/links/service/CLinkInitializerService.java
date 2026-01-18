package tech.derbent.app.links.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;
import tech.derbent.app.links.domain.CLink;

/**
 * Initializer service for CLink entities.
 * Provides standard link section creation for ALL entity detail views.
 * 
 * **Key Feature**: addLinksSection() ensures ALL entities have identical link sections
 * with consistent naming, behavior, and appearance.
 * 
 * **Important**: Links are child entities with NO standalone views or pages.
 * They are managed exclusively through their parent entities.
 */
public final class CLinkInitializerService extends CInitializerServiceBase {

    private static final Class<?> clazz = CLink.class;
    /** Standard field name - must match entity field name */
    public static final String FIELD_NAME_LINKS = "links";
    private static final Logger LOGGER = LoggerFactory.getLogger(CLinkInitializerService.class);
    /** Standard section name - same for ALL entities */
    public static final String SECTION_NAME_LINKS = "Links";

    /**
     * Add standard Links section to any entity detail view.
     * 
     * **This is the ONLY method that creates link sections.**
     * ALL entity initializers (Activity, Risk, Meeting, Sprint, Project, etc.)
     * MUST call this method to ensure consistent link sections.
     * 
     * Creates:
     * - Section header: "Links"
     * - Field: "links" (renders link component via factory)
     * 
     * @param detailSection the detail section to add links to
     * @param entityClass the entity class (must implement IHasLinks and have @OneToMany links field)
     * @throws Exception if adding section fails
     */
    public static void addLinksSection(final CDetailSection detailSection, final Class<?> entityClass) throws Exception {
        Check.notNull(detailSection, "detailSection cannot be null");
        Check.notNull(entityClass, "entityClass cannot be null");
        try {
            // Section header - IDENTICAL for all entities
            detailSection.addScreenLine(CDetailLinesService.createSection(SECTION_NAME_LINKS));
            // Links field - IDENTICAL for all entities
            // Renders via component factory (referenced in entity's @AMetaData)
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, FIELD_NAME_LINKS));
            // LOGGER.debug("Added standard Links section for {}", entityClass.getSimpleName());
        } catch (final Exception e) {
            LOGGER.error("Error adding Links section for {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    private CLinkInitializerService() {
        // Utility class - no instantiation
    }
}
