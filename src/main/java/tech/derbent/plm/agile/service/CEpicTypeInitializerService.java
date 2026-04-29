package tech.derbent.plm.agile.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityNamedInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.services.CEntityTypeInitializerService;
import tech.derbent.plm.agile.domain.CEpicType;

/** Initializer for epic type screens and seed data.
 * <p>
 * Epic types stay at level 0 and parent-capable by default so teams can see the root of the hierarchy model directly in the type management UI.
 * </p>
 */
public class CEpicTypeInitializerService extends CEntityTypeInitializerService {

	private static final Class<?> clazz = CEpicType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CEpicTypeInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".10";
	private static final String menuTitle = MenuTitle_TYPES + "." + MenuTitle_TYPES_PROJECT + ".Epic Types";
	private static final String pageDescription = "Manage epic type categories for agile planning";
	private static final String pageTitle = "Epic Type Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			return CEntityNamedInitializerService.createTypeEntityView(project, clazz, "Hierarchy Configuration", true,
					"level", "canHaveChildren");
		} catch (final Exception e) {
			LOGGER.error("Error creating epic type view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "level", "canHaveChildren", "color", "sortOrder",
				"active", "company"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService)
			throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
				menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder, null);
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final String[][] nameAndDescriptions = {
				{
						"Strategic Initiative", "Large strategic initiatives that span multiple quarters"
				}, {
						"Business Epic", "Major business capabilities or initiatives"
				}, {
						"Technical Epic", "Large technical improvements or infrastructure work"
				}, {
						"Product Epic", "Major product features or enhancements"
				}
		};
		final CCompany company = project.getCompany();
		// Use the concrete service bean to keep this initializer type-safe (no unchecked casts).
		final CEpicTypeService epicTypeService = CSpringContext.getBean(CEpicTypeService.class);
		initializeCompanyEntity(nameAndDescriptions, epicTypeService, company, minimal, (epicType, index) -> {
			epicType.setLevel(0);
			epicType.setCanHaveChildren(true);
		});
	}
}
