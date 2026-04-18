package tech.derbent.plm.agile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;

/** Initializer service for common Agile entity UI sections.
 * <p>
 * Adds the standard "Children" section that renders via the transient placeholder field in {@code CAgileEntity}.
 * </p> */
public final class CAgileEntityInitializerService extends CInitializerServiceBase {

	public static final String FIELD_NAME_AGILE_CHILDREN = "placeHolder_createComponentAgileChildren";
	private static final Logger LOGGER = LoggerFactory.getLogger(CAgileEntityInitializerService.class);
	public static final String SECTION_NAME_CHILDREN = "Children";

	public static void addDefaultChildrenSection(final CDetailSection detailSection, final Class<?> entityClass, final CProject<?> project)
			throws Exception {
		Check.notNull(detailSection, "detailSection cannot be null");
		Check.notNull(entityClass, "entityClass cannot be null");
		Check.notNull(project, "project cannot be null");

		if (CSpringContext.isBabProfile()) {
			LOGGER.debug("Skipping Agile Children section for BAB profile on {}", entityClass.getSimpleName());
			return;
		}

		try {
			detailSection.addScreenLine(CDetailLinesService.createSection(SECTION_NAME_CHILDREN));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(entityClass, FIELD_NAME_AGILE_CHILDREN));
		} catch (final Exception e) {
			LOGGER.error("Error adding Agile Children section for {}: {}", entityClass.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	private CAgileEntityInitializerService() {
		// Utility class
	}
}
