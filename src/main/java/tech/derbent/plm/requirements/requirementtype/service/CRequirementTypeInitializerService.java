package tech.derbent.plm.requirements.requirementtype.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityOfProjectInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.services.CEntityTypeInitializerService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.requirements.requirementtype.domain.CRequirementType;

public class CRequirementTypeInitializerService extends CEntityTypeInitializerService {

	private static final Class<?> clazz = CRequirementType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CRequirementTypeInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".16";
	private static final String menuTitle = MenuTitle_TYPES + "." + MenuTitle_TYPES_PROJECT + ".Requirement Types";
	private static final String pageDescription = "Manage requirement type categories and hierarchy levels";
	private static final String pageTitle = "Requirement Type Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		Check.notNull(project, "project cannot be null");
		try {
			final CDetailSection detailSection =
					CEntityOfProjectInitializerService.createBasicView(project, clazz);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "workflow"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Hierarchy Configuration"));
			// Level and canHaveChildren drive every hierarchy selector, so both stay on the primary form section.
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "level"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "canHaveChildren"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeNonDeletable"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating requirement type view.");
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
		final String[][] seeds = {
				{
						"Requirement Theme", "Top-level business theme that can own requirement groups"
				}, {
						"Capability Requirement", "Intermediate requirement package for grouped work"
				}, {
						"Detailed Requirement", "Leaf requirement that captures an actionable or testable need"
				}
		};
		final CCompany company = project.getCompany();
		// Use the concrete service bean to keep this initializer type-safe (no unchecked casts).
		final CRequirementTypeService requirementTypeService = CSpringContext.getBean(CRequirementTypeService.class);
		initializeCompanyEntity(seeds, requirementTypeService, company, minimal, (requirementType, index) -> {
			if (index == 0) {
				requirementType.setLevel(0);
				requirementType.setCanHaveChildren(true);
			} else if (index == 1) {
				requirementType.setLevel(1);
				requirementType.setCanHaveChildren(true);
			} else {
				requirementType.setLevel(-1);
				requirementType.setCanHaveChildren(false);
			}
		});
	}
}
