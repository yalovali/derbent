package tech.derbent.api.parentrelation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.interfaces.IHasParentRelation;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;

/**
 * Initializer service for CParentRelation components. Provides factory methods to create standard
 * parent hierarchy sections in entity detail screens.
 */
public final class CParentRelationInitializerService extends CInitializerServiceBase {

    /** The field name of the placeholder used for createComponentParent method lookup in page services. */
    public static final String FIELD_NAME_PARENT_PLACEHOLDER = "placeHolder_createComponentParent";
    /** The field name of the placeholder used for createComponentParentChildren lookup. */
    public static final String FIELD_NAME_PARENT_CHILDREN_PLACEHOLDER = "placeHolder_createComponentParentChildren";
    /** The field name of the parent relation JPA field. */
    public static final String FIELD_NAME_PARENT_RELATION = "parentRelation";
    private static final Logger LOGGER =
            LoggerFactory.getLogger(CParentRelationInitializerService.class);
    /** Section name constant. */
    public static final String SECTION_NAME_PARENT = "Parent Hierarchy";
    /** Section name constant for child management. */
    public static final String SECTION_NAME_CHILDREN = "Children";

    private CParentRelationInitializerService() {
        // utility class - not instantiable
    }

    /**
     * Add a default parent relation section to an entity detail screen. Checks if entity class
     * implements IHasParentRelation and adds the parent selector component.
     *
     * @param detailSection the detail section to add the parent section to
     * @param entityClass   the entity class (must implement IHasParentRelation)
     * @param project       the project context
     * @throws Exception    if adding section fails
     */
    public static void addDefaultSection(final CDetailSection detailSection,
            final Class<?> entityClass, final CProject<?> project) throws Exception {
        Check.notNull(detailSection, "detailSection cannot be null");
        Check.notNull(entityClass, "entityClass cannot be null");
        Check.notNull(project, "project cannot be null");
        if (CSpringContext.isBabProfile()) {
            LOGGER.debug("Skipping Parent Hierarchy section for BAB profile on {}",
                    entityClass.getSimpleName());
            return;
        }
        if (!IHasParentRelation.class.isAssignableFrom(entityClass)) {
            LOGGER.debug("Entity {} does not implement IHasParentRelation, skipping parent hierarchy section",
                    entityClass.getSimpleName());
            return;
        }
        detailSection.addScreenLine(CDetailLinesService.createSection(SECTION_NAME_PARENT));
        final String fieldName = hasFieldInHierarchy(entityClass, FIELD_NAME_PARENT_PLACEHOLDER)
                ? FIELD_NAME_PARENT_PLACEHOLDER
                : FIELD_NAME_PARENT_RELATION;
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, fieldName));
    }

    /**
     * Add the generic children management section when the entity exposes the standard placeholder.
     *
     * @param detailSection the detail section to add the child section to
     * @param entityClass   the entity class to inspect
     * @param project       the project context
     * @throws Exception    if metadata lookup fails
     */
    public static void addDefaultChildrenSection(final CDetailSection detailSection,
            final Class<?> entityClass, final CProject<?> project) throws Exception {
        Check.notNull(detailSection, "detailSection cannot be null");
        Check.notNull(entityClass, "entityClass cannot be null");
        Check.notNull(project, "project cannot be null");
        if (CSpringContext.isBabProfile()
                || !hasFieldInHierarchy(entityClass, FIELD_NAME_PARENT_CHILDREN_PLACEHOLDER)) {
            return;
        }
        detailSection.addScreenLine(CDetailLinesService.createSection(SECTION_NAME_CHILDREN));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass,
                FIELD_NAME_PARENT_CHILDREN_PLACEHOLDER));
    }

    private static boolean hasFieldInHierarchy(final Class<?> entityClass, final String fieldName) {
        Class<?> current = entityClass;
        while (current != null) {
            try {
                current.getDeclaredField(fieldName);
                return true;
            } catch (@SuppressWarnings("unused") final Exception e) {
                current = current.getSuperclass();
            }
        }
        return false;
    }
}
