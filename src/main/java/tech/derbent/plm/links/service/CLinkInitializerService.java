package tech.derbent.plm.links.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.links.domain.CLink;

/** Initializer service for CLink entities. Provides standard link section creation for ALL entity detail views. **Key Feature**: addLinksSection()
 * ensures ALL entities have identical link sections with consistent naming, behavior, and appearance. **Important**: Links are child entities with NO
 * standalone views or pages. They are managed exclusively through their parent entities. */
public final class CLinkInitializerService extends CInitializerServiceBase {

	/** Standard field name - must match entity field name */
	public static final String FIELD_NAME_LINKS = "links";
	private static final Logger LOGGER = LoggerFactory.getLogger(CLinkInitializerService.class);
	/** Standard section name - same for ALL entities */
	public static final String SECTION_NAME_LINKS = "Links";

	/** Add standard Links section to any entity detail view. **This is the ONLY method that creates link sections.** ALL entity initializers
	 * (Activity, Risk, Meeting, Sprint, Project, etc.) MUST call this method to ensure consistent link sections. Creates: - Section header: "Links" -
	 * Field: "links" (renders link component via factory)
	 * @param detailSection the detail section to add links to
	 * @param entityClass   the entity class (must implement IHasLinks and have @OneToMany links field)
	 * @throws Exception if adding section fails */
	public static void addDefaultSection(final CDetailSection detailSection, final Class<?> entityClass) throws Exception {
		Check.notNull(detailSection, "detailSection cannot be null");
		Check.notNull(entityClass, "entityClass cannot be null");
		try {
			// Section header - IDENTICAL for all entities
			detailSection.addScreenLine(CDetailLinesService.createSection(SECTION_NAME_LINKS));
			// Links field - IDENTICAL for all entities
			// Renders via component factory (referenced in entity's @AMetaData)
			final var detailLine = CDetailLinesService.createLineFromDefaults(entityClass, FIELD_NAME_LINKS);
			detailLine.setIsCaptionVisible(false); // Hide caption for cleaner look
			detailSection.addScreenLine(detailLine);
			// LOGGER.debug("Added standard Links section for {}", entityClass.getSimpleName());
		} catch (final Exception e) {
			LOGGER.error("Error adding Links section for {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
			throw e;
		}
	}

	/** Create a link from source entity to a random target entity.
	 * @param sourceEntity       the source entity
	 * @param project            the project (for finding random entities)
	 * @param targetEntityClass  the target entity class to link to
	 * @param targetServiceClass the service class for the target entity
	 * @param linkType           the type of link (e.g., "Related", "Depends On")
	 * @param linkDescription    description of the link
	 * @param company            the company for the link
	 * @return the created CLink or null if failed */
	public static tech.derbent.plm.links.domain.CLink createRandomLink(final tech.derbent.api.entity.domain.CEntityDB<?> sourceEntity,
			final tech.derbent.api.projects.domain.CProject<?> project, final Class<?> targetEntityClass, final Class<?> targetServiceClass,
			final String linkType, final String linkDescription, final CCompany company) {
		try {
			final CAbstractService<?> targetService = (CAbstractService<?>) CSpringContext.getBean(targetServiceClass);
			// Get random target entity from the same project
			final Object randomTarget = targetService.getClass().getMethod("getRandom", CProject.class).invoke(targetService, project);
			if (randomTarget != null && randomTarget instanceof CEntityDB) {
				final CEntityDB<?> targetEntity = (CEntityDB<?>) randomTarget;
				final CLink link = new CLink(sourceEntity.getClass().getSimpleName(), sourceEntity.getId(), targetEntityClass.getSimpleName(),
						targetEntity.getId(), linkType);
				link.setDescription(linkDescription);
				link.setCompany(company);
				return link;
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not create link from {} to {}: {}", sourceEntity.getClass().getSimpleName(), targetEntityClass.getSimpleName(),
					e.getMessage());
		}
		return null;
	}

	private CLinkInitializerService() {
		// Utility class - no instantiation
	}
}
