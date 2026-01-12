package tech.derbent.api.screens.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.domain.CMasterSection;
import tech.derbent.api.utils.Check;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;

public class CMasterInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CMasterSection.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CMasterInitializerService.class);
	private static final String menuOrder = Menu_Order_SYSTEM + ".11";
	private static final String menuTitle = MenuTitle_SETUP + ".UI.Master Sections";
	private static final String pageDescription = "Manage reusable master section templates";
	private static final String pageTitle = "Master Section Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		Check.notNull(project, "project cannot be null");
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			scr.addScreenLine(CDetailLinesService.createSection("Configuration"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sectionType"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sectionDBName"));
			scr.addScreenLine(CDetailLinesService.createSection("Audit"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating master section view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "sectionType", "sectionDBName", "project", "active"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
		Check.notNull(project, "project cannot be null");
		final String[][] sections = {
				{
						"General Master Section", "Reusable section template for project pages"
				}, {
						"Timeline Master Section", "Timeline-focused master layout"
				}
		};
		final CMasterSectionService masterSectionService = CSpringContext.getBean(CMasterSectionService.class);
		initializeProjectEntity(sections, masterSectionService, project, minimal, (section, index) -> {
			final List<String> availableTypes = CMasterSectionService.getAvailableTypes();
			final String defaultType = availableTypes.isEmpty() ? "None" : availableTypes.get(Math.min(index, availableTypes.size() - 1));
			section.setSectionType(defaultType);
			section.setSectionDBName((section.getName() + "_" + project.getId()).toLowerCase().replaceAll("[^a-z0-9]+", "_"));
		});
	}
}
