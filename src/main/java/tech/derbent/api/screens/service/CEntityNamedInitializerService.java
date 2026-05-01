package tech.derbent.api.screens.service;

import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.utils.Check;

public abstract class CEntityNamedInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Description";

	protected static void createScreenLines(final CDetailSection scr, final Class<?> clazz,
			@SuppressWarnings ("unused") final CProject<?> project, final boolean newSection)
			throws NoSuchFieldException {
		if (newSection) {
			scr.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
		}
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id", true, "10%"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name", false, "100%"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
	}

	/** Creates a standard detail view for company-scoped type entities (CXxxType classes). Generates: named entity fields (id, name, description),
	 * company, workflow, a configurable display section with color/sortOrder/optional extras/attributeNonDeletable/active, and an optional audit section.
	 * @param project            the project context
	 * @param clazz              the entity class
	 * @param configSectionTitle title for the display/config section (e.g. "Display Configuration")
	 * @param includeAudit       whether to append an Audit section with createdDate/lastModifiedDate
	 * @param extraDisplayFields optional extra fields added inside the config section after sortOrder */
	public static CDetailSection createTypeEntityView(final CProject<?> project, final Class<?> clazz,
			final String configSectionTitle, final boolean includeAudit, final String... extraDisplayFields)
			throws Exception {
		Check.notNull(project, "project cannot be null");
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		createScreenLines(scr, clazz, project, true);
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "workflow"));
		scr.addScreenLine(CDetailLinesService.createSection(configSectionTitle));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
		for (final String field : extraDisplayFields) {
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, field));
		}
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeNonDeletable"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
		if (includeAudit) {
			scr.addScreenLine(CDetailLinesService.createSection("Audit"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
		}
		scr.debug_printScreenInformation();
		return scr;
	}
}
